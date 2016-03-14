package com.github.rmannibucau.ohmyjs.runner;

import com.github.rmannibucau.ohmyjs.io.IO;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JadeRunnerTest {
    @Test
    public void run() throws IOException {
        final File file = new File("target/jade-rendered");
        of(file).filter(File::isDirectory).ifPresent(d -> {
            Stream.of(d.listFiles()).forEach(File::delete);
            d.delete();
        });
        JadeRunner.main(new String[]{"--source", "src/test/resources/jade", "--target", "target/jade-rendered"});
        assertTrue(file.isDirectory());

        try (final InputStream is = new FileInputStream(new File(file, "j1.html"))) {
            assertEquals("<!DOCTYPE html><html><head><title>my jade template</title></head><body><h1>Hello </h1></body></html>", IO.read(is));
        }
        try (final InputStream is = new FileInputStream(new File(file, "sub/j2.html"))) {
            assertEquals("<ul id=\"books\"><li><a href=\"#book-a\">Book A</a></li><li><a href=\"#book-b\">Book B</a></li></ul>", IO.read(is));
        }
    }
}
