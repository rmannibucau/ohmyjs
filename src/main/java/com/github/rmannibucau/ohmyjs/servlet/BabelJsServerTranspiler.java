package com.github.rmannibucau.ohmyjs.servlet;

import com.github.rmannibucau.ohmyjs.service.BabelJsService;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.File;

public class BabelJsServerTranspiler extends BaseFilter {
    private static final int JS_LENGTH = "js".length();

    private String templatesRelativePath;
    private String templateExtension;
    private boolean mapToEs6;

    @Override
    protected String getConfigPrefix() {
        return "babeljs";
    }

    @Override
    protected File getAlternativeSourceFile(final File f) {
        final String name = f.getName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? new File(f.getParentFile(), templatesRelativePath + name.substring(0, dot + 1) + templateExtension) : null;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (active) {
            templatesRelativePath = getConfig(filterConfig, "templates", "templates") + "/";
            templateExtension = getConfig(filterConfig, "templateExtension", "html");
            mapToEs6 = Boolean.parseBoolean(getConfig(filterConfig, "mapToEs6", "true"));
            service = new BabelJsService(isDev, getConfig(filterConfig, "module", "'amd'"));
        }
    }

    @Override
    protected String mapURI(final String requestURI) {
        return !mapToEs6 || !requestURI.endsWith(".js") ? requestURI : requestURI.substring(0, requestURI.length() - JS_LENGTH) + "es6";
    }
}
