package com.github.rmannibucau.ohmyjs.servlet;

import com.github.rmannibucau.ohmyjs.service.BaseService;
import com.github.rmannibucau.ohmyjs.service.JadeService;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import static java.util.Optional.ofNullable;

public class JadeServerRenderer extends BaseFilter {
    private static final int HTML_LENGTH = "html".length();

    private boolean mapToJade;

    @Override
    protected String getConfigPrefix() {
        return "jade";
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (active) {
            mapToJade = Boolean.parseBoolean(getConfig(filterConfig, "mapToJade", "true"));
            service = new JadeService();
        }
    }

    @Override
    public void destroy() {
        ofNullable(service).ifPresent(BaseService::close);
        super.destroy();
    }

    @Override
    protected String mapURI(final String requestURI) {
        return !mapToJade || !requestURI.endsWith(".html") ? requestURI : requestURI.substring(0, requestURI.length() - HTML_LENGTH) + "jade";
    }
}
