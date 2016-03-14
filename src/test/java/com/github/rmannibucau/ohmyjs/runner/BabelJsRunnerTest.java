package com.github.rmannibucau.ohmyjs.runner;

import com.github.rmannibucau.ohmyjs.io.IO;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BabelJsRunnerTest {
    @Test
    public void run() throws IOException {
        final File file = new File("target/es6-transpiled");
        of(file).filter(File::isDirectory).ifPresent(d -> {
            Stream.of(d.listFiles()).forEach(File::delete);
            d.delete();
        });
        BabelJsRunner.main(new String[]{"--source", "src/test/resources/es6", "--target", "target/es6-transpiled"});
        assertTrue(file.isDirectory());

        try (final InputStream is = new FileInputStream(new File(file, "s1.js"))) {
            assertEquals("\"use strict\";\n" +
                "\n" +
                "define([\"exports\", \"Foo\"], function (exports, _Foo) {\n" +
                "  Object.defineProperty(exports, \"__esModule\", {\n" +
                "    value: true\n" +
                "  });\n" +
                "\n" +
                "  var _Foo2 = _interopRequireDefault(_Foo);\n" +
                "\n" +
                "  function _interopRequireDefault(obj) {\n" +
                "    return obj && obj.__esModule ? obj : {\n" +
                "      default: obj\n" +
                "    };\n" +
                "  }\n" +
                "\n" +
                "  exports.default = _Foo2.default.extend({});\n" +
                "});", IO.read(is));
        }
        try (final InputStream is = new FileInputStream(new File(file, "sub/s2.js"))) {
            assertEquals("\"use strict\";\n" +
                "\n" +
                "define([\"exports\", \"Vue\"], function (exports, _Vue) {\n" +
                "    Object.defineProperty(exports, \"__esModule\", {\n" +
                "        value: true\n" +
                "    });\n" +
                "\n" +
                "    var _Vue2 = _interopRequireDefault(_Vue);\n" +
                "\n" +
                "    function _interopRequireDefault(obj) {\n" +
                "        return obj && obj.__esModule ? obj : {\n" +
                "            default: obj\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    exports.default = _Vue2.default.extend({\n" +
                "        ready: function ready() {\n" +
                "            console.log('ready');\n" +
                "        }\n" +
                "    });\n" +
                "});", IO.read(is));
        }
    }
}
