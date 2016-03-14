package com.github.rmannibucau.ohmyjs.runner;

import com.github.rmannibucau.ohmyjs.io.IO;
import com.github.rmannibucau.ohmyjs.service.BaseService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

final class Runners {
    private Runners() {
        // no-op
    }

    static Map<String, String> getOptions(final String[] args) {
        final Map<String, String> options = new HashMap<>();
        { // parse inputs
            String optName = null;
            for (final String s : ofNullable(args).map(Arrays::asList).orElse(emptyList())) {
                if (optName == null) {
                    if (!s.startsWith("--")) {
                        throw new IllegalArgumentException("options should start with '--': " + s);
                    }
                    optName = s.substring(2);
                } else {
                    options.put(optName, s);
                    optName = null;
                }
            }
        }
        return options;
    }

    static void run(final String[] args, final Function<Map<String, String>, BaseService> serviceFactory, final String postExt, final String... defaultExts) throws IOException {
        final Map<String, String> opts = getOptions(args);
        final BaseService service = serviceFactory.apply(opts);
        final String inputPath = ofNullable(opts.get("source")).orElseThrow(() -> new IllegalArgumentException("No --source option"));
        final String outputPath = ofNullable(opts.get("target")).orElseThrow(() -> new IllegalArgumentException("No --target option"));
        final Pattern exclude = ofNullable(opts.get("exclude")).map(Pattern::compile).orElse(null);
        final Pattern include = ofNullable(opts.get("include")).map(Pattern::compile).orElse(null);

        // now browse all files matching include/exclude in directory and compile them
        final Collection<String> exts = ofNullable(defaultExts).map(Arrays::asList).orElse(emptyList());
        final Path from = Paths.get(inputPath);
        final Path to = Paths.get(outputPath);
        final Logger logger = Logger.getLogger(JadeRunner.class.getName());
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                final String name = file.getFileName().toString();
                final boolean matches = (include == null && exts.stream().filter(name::endsWith).findFirst().isPresent()) || (include != null && include.matcher(name).matches());
                if (matches && (exclude == null || !exclude.matcher(name).matches())) {
                    final int ext = name.lastIndexOf('.');
                    final Path out = ofNullable(from.relativize(file).getParent()).map(to::resolve).orElse(to).resolve((ext > 0 ? name.substring(0, ext + 1) : name) + postExt);
                    try (final InputStream in = new FileInputStream(file.toFile())) {
                        final String js = service.transform(IO.read(in));
                        final File outFile = out.toFile();
                        if (!outFile.getParentFile().isDirectory() && !outFile.getParentFile().mkdirs()) {
                            throw new IllegalArgumentException("Can't create " + out);
                        }
                        try (final Writer fileWriter = new BufferedWriter(new FileWriter(outFile))) {
                            fileWriter.write(js);
                        }
                    }
                    logger.info("Transformed " + file + " to " + out);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
