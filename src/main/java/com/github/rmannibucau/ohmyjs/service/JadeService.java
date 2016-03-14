package com.github.rmannibucau.ohmyjs.service;

public class JadeService extends BaseService {
    public JadeService() {
        init("var jadeWrapper = function (input){return jade.render(input);};");
    }

    @Override
    protected int estimatedJsSize() {
        return 133200;
    }

    @Override
    protected String jsName() {
        return "jade.min.js";
    }

    @Override
    protected String getWrapper() {
        return "jadeWrapper";
    }
}
