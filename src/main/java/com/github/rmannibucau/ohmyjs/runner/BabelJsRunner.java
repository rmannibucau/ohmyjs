package com.github.rmannibucau.ohmyjs.runner;

import com.github.rmannibucau.ohmyjs.service.BabelJsService;

import java.io.IOException;

public class BabelJsRunner {
    private BabelJsRunner() {
        // no-op
    }

    public static void main(final String[] args) throws IOException {
        Runners.run(
            args,
            opts -> new BabelJsService(Boolean.parseBoolean(opts.getOrDefault("dev", "false")), opts.getOrDefault("module", "amd")),
            "js",
            "js", "es6");
    }
}
