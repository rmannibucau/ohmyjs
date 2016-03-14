package com.github.rmannibucau.ohmyjs.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class DelegateFilterConfig implements FilterConfig {
    private final FilterConfig delegate;
    private final Map<String, String> overrides;

    public DelegateFilterConfig(final FilterConfig delegate, final Map<String, String> overrides) {
        this.delegate = delegate;
        this.overrides = overrides;
    }

    @Override
    public String getInitParameter(final String name) {
        return ofNullable(overrides.get(name)).orElseGet(() -> delegate.getInitParameter(name));
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();
    }

    @Override
    public String getFilterName() {
        return delegate.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }
}
