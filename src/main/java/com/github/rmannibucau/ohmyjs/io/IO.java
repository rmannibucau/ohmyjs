package com.github.rmannibucau.ohmyjs.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IO {
    private IO() {
        // no-op
    }

    public static String read(final InputStream is) throws IOException {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        final byte[] reader = new byte[1024];
        int len;
        final InputStream r = new BufferedInputStream(is);
        while ((len = r.read(reader)) >= 0) {
            outBuffer.write(reader, 0, len);
        }
        return new String(outBuffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
