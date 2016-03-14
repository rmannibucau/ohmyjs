package com.github.rmannibucau.ohmyjs.service;

import static java.util.Optional.ofNullable;

public class BabelJsService extends BaseService {
    public BabelJsService(final boolean isDev, final String module) {
        init("var babelWrapper=function(bwc){return Babel.transform(bwc," +
            "{" +
            "presets:['es2015']," +
            "sourceMaps:" + (isDev ? "'inline'" : "false") + "," +
            "comments: " + isDev + "," +
            "plugins:['" + ofNullable(module).map(s -> "transform-es2015-modules-" + s).orElse("transform-es2015-modules-amd") + "']" +
            "}).code;}");
    }

    @Override
    protected int estimatedJsSize() {
        return 1102500;
    }

    @Override
    protected String jsName() {
        return "babel.min.js";
    }

    @Override
    protected String getWrapper() {
        return "babelWrapper";
    }
}
