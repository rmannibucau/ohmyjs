package com.github.rmannibucau.ohmyjs.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JadeServiceTest {
    @Test
    public void simple() {
        try (final JadeService service = new JadeService()) {
            assertEquals(
                "<h1 id=\"title\">Welcome to Jade</h1><button data-action=\"bea\" class=\"btn\">Be Awesome</button>",
                service.transform(
                    "h1(id=\"title\") Welcome to Jade\n" +
                        "button(class=\"btn\", data-action=\"bea\").\n" +
                        "  Be Awesome"));
        }
    }
}
