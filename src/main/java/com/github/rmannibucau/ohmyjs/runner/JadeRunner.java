package com.github.rmannibucau.ohmyjs.runner;

import com.github.rmannibucau.ohmyjs.service.JadeService;

import java.io.IOException;

public class JadeRunner {
    private JadeRunner() {
        // no-op
    }

    public static void main(final String[] args) throws IOException {
        Runners.run(args, opts -> new JadeService(), "html", "jade");
    }
}
