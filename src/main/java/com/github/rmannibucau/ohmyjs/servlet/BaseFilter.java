package com.github.rmannibucau.ohmyjs.servlet;

import com.github.rmannibucau.ohmyjs.service.BaseService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public abstract class BaseFilter implements Filter {
    protected boolean isDev;
    protected boolean active;
    protected String sourcesRoot;
    protected String cacheRoot;
    protected Logger logger;
    protected List<String> excludes;
    protected List<Pattern> includes;
    protected final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, Long> lastModifieds = new ConcurrentHashMap<>();
    protected BaseService service;

    protected abstract String getConfigPrefix();

    protected boolean isFiltered(final HttpServletRequest req) {
        final String requestURI = req.getRequestURI();
        if (!excludes.isEmpty() && excludes.contains(requestURI)) {
            return false;
        }
        if (includes.isEmpty()) {
            return true;
        }
        for (final Pattern include : includes) {
            if (include.matcher(requestURI).matches()) {
                return true;
            }
        }
        return false;
    }

    protected String getConfig(final FilterConfig filterConfig, final String key, final String defaultValue) {
        return ofNullable(filterConfig).map(f -> f.getInitParameter(key))
            .orElseGet(() -> System.getProperty(getConfigPrefix() + "." + key, defaultValue));
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        active = Boolean.parseBoolean(getConfig(filterConfig, "active", "true"));
        if (!active) {
            return;
        }

        isDev = Boolean.parseBoolean(getConfig(filterConfig, "dev", "false"));
        sourcesRoot = getConfig(filterConfig, "sources", "webapp") + "/";
        cacheRoot = getConfig(filterConfig, "cache", null);
        excludes = ofNullable(getConfig(filterConfig, "excludes", null)).map(Arrays::asList).orElse(emptyList());
        includes = ofNullable(getConfig(filterConfig, "includes", ".*.js")).map(Arrays::asList).orElse(emptyList()).stream().map(Pattern::compile).collect(toList());
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!active || !HttpServletRequest.class.isInstance(request)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest req = HttpServletRequest.class.cast(request);
        if (isFiltered(req)) { // slurp the content and transpile it
            long lastModified = 0;
            long cachedLastModified = 0;
            final String requestURI = mapURI(req.getRequestURI());
            final String path = requestURI.replace(req.getContextPath(), "");

            if (!isDev || (cachedLastModified = isSame(path)) == (lastModified = findLastModified(path))) {
                final String output = cache.get(path);
                if (output != null) {
                    response.getWriter().write(output);
                    return;
                }
            }
            if (cacheRoot != null) { // pre-transformed files
                final File cacheFile = new File(cacheRoot, path);
                if (cacheFile.isFile() && (!isDev || (cachedLastModified == 0 || cachedLastModified == (lastModified = Math.max(lastModified, cacheFile.lastModified()))))) {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    synchronized (this) {
                        try (final InputStream is = new FileInputStream(cacheFile)) {
                            int len;
                            final byte[] buffer = new byte[1024 * 8];
                            while ((len = is.read(buffer)) >= 0) {
                                baos.write(buffer, 0, len);
                            }
                        }
                    }

                    final String value = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    cache.putIfAbsent(path, value);
                    if (isDev) {
                        lastModifieds.put(path, lastModified);
                    }
                    response.getWriter().write(value);
                    return;

                }
            }

            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            final ServletOutputStream sos = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(final WriteListener listener) {
                    // no-op
                }

                @Override
                public void write(final int b) throws IOException {
                    baos.write(b);
                }
            };

            final PrintWriter writer = new PrintWriter(baos);
            final HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(HttpServletResponse.class.cast(response)) {
                @Override
                public PrintWriter getWriter() throws IOException {
                    return writer;
                }

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return sos;
                }

                @Override
                public void flushBuffer() throws IOException { // we don't want to write yet
                    // no-op
                }

                @Override
                public void setContentLength(final int len) { // will be wrong since we transpile it after
                    // no-op
                }

                @Override
                public void setContentLengthLong(final long length) { // will be wrong since we transpile it after
                    // no-op
                }
            };

            chain.doFilter(new HttpServletRequestWrapper(req) {
                @Override // if we remapped te extension ensure the DefaultServlet can see the mapping
                public String getServletPath() {
                    return path;
                }
            }, responseWrapper);
            writer.flush();
            sos.flush();

            final String source = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            final String result = service.transform(source);
            response.getWriter().write(result);
            response.flushBuffer();
            cache.putIfAbsent(path, result);
            if (isDev) {
                logger.info("Re-transformed " + requestURI);
                lastModifieds.put(path, lastModified);
            }
            if (cacheRoot != null) {
                final File cacheFile = new File(cacheRoot, path);
                if (isOlder(lastModified, cacheFile)) {
                    synchronized (this) {
                        if (isOlder(lastModified, cacheFile)) {
                            if (!cacheFile.getParentFile().isDirectory() && !cacheFile.getParentFile().mkdirs()) {
                                throw new IllegalArgumentException("Can't create " + cacheFile);
                            }
                            try (final Writer fw = new FileWriter(cacheFile)) {
                                fw.write(result);
                            }
                        }
                    }
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isOlder(final long lastModified, final File cacheFile) {
        return !cacheFile.isFile() || cacheFile.lastModified() <= lastModified;
    }

    protected String mapURI(final String path) {
        return path;
    }

    private long findLastModified(final String path) { // either the template or the script
        return of(new File(sourcesRoot + path)).filter(File::isFile)
            .map(f -> Stream.of(f, getAlternativeSourceFile(f))
                .filter(s -> s != null)
                .filter(File::isFile).mapToLong(File::lastModified)
                .max().orElse(0))
            .orElse(0L);
    }

    protected File getAlternativeSourceFile(final File f) {
        return null;
    }

    private long isSame(final String path) {
        return lastModifieds.getOrDefault(path, 0L);
    }

    @Override
    public void destroy() {
        ofNullable(service).ifPresent(BaseService::close);
    }
}
