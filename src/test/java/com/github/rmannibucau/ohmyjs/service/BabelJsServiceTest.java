package com.github.rmannibucau.ohmyjs.service;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BabelJsServiceTest {
    private static BabelJsService service;

    @BeforeClass
    public static void init() {
        service = new BabelJsService(false, null);
    }

    @AfterClass
    public static void clean() {
        service.close();
    }

    @Test
    public void simple() {
        assertEquals(
            "\"use strict\";\n" +
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
                "});",
            service.transform(
                "import Foo from \"Foo\";\n" +
                    "\n" +
                    "export default Foo.extend({});\n"));
    }

    @Test
    public void vueController() { // close to real code
        assertEquals(
            "'use strict';\n" +
                "\n" +
                "define(['exports', 'Vue'], function (exports, _Vue) {\n" +
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
                "        template: '<form id=\"entryForm\">\\n                    <div class=\"form-group\">\\n                        <label for=\"entryName\">Name</label>\\n                        <input v-model=\"formData.name\" type=\"text\" class=\"form-control\" id=\"entryName\" placeholder=\"Name...\">\\n                    </div>\\n                    <div class=\"form-group\">\\n                        <label for=\"entryValue\">Password</label>\\n                        <input v-model=\"formData.value\" type=\"password\" class=\"form-control\" id=\"entryValue\" placeholder=\"Value...\">\\n                    </div>\\n                    <div class=\"form-group\">\\n                        <label for=\"entryValue2\">Password (confirm)</label>\\n                        <input v-model=\"formData.value2\" type=\"password\" class=\"form-control\" id=\"entryValue2\" placeholder=\"Confirm...\">\\n                    </div>\\n                    <button type=\"submit\" class=\"btn btn-info\" @click=\"addValue()\">Create</button>\\n                </form>',\n" +
                "        data: function data() {\n" +
                "            return {\n" +
                "                formData: {}\n" +
                "            };\n" +
                "        },\n" +
                "        methods: {\n" +
                "            addValue: function addValue() {\n" +
                "                var _this = this;\n" +
                "\n" +
                "                if (this.formData.value != this.formData.value2 || !this.formData.value) {\n" +
                "                    alert('Bad password');\n" +
                "                    return;\n" +
                "                }\n" +
                "\n" +
                "                this.$http.post('api/values', this.formData).then(function (r) {\n" +
                "                    return _this.$router.go('/show-values');\n" +
                "                }, function (error) {\n" +
                "                    return alert(\"HTTP \" + error.status + \", please try again.\");\n" +
                "                });\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "});",
            service.transform(
                "import Vue from \"Vue\";\n" +
                    "\n" +
                    "export default Vue.extend({\n" +
                    "    template: `<form id=\"entryForm\">\n" +
                    "                    <div class=\"form-group\">\n" +
                    "                        <label for=\"entryName\">Name</label>\n" +
                    "                        <input v-model=\"formData.name\" type=\"text\" class=\"form-control\" id=\"entryName\" placeholder=\"Name...\">\n" +
                    "                    </div>\n" +
                    "                    <div class=\"form-group\">\n" +
                    "                        <label for=\"entryValue\">Password</label>\n" +
                    "                        <input v-model=\"formData.value\" type=\"password\" class=\"form-control\" id=\"entryValue\" placeholder=\"Value...\">\n" +
                    "                    </div>\n" +
                    "                    <div class=\"form-group\">\n" +
                    "                        <label for=\"entryValue2\">Password (confirm)</label>\n" +
                    "                        <input v-model=\"formData.value2\" type=\"password\" class=\"form-control\" id=\"entryValue2\" placeholder=\"Confirm...\">\n" +
                    "                    </div>\n" +
                    "                    <button type=\"submit\" class=\"btn btn-info\" @click=\"addValue()\">Create</button>\n" +
                    "                </form>`,\n" +
                    "    data() {\n" +
                    "        return {\n" +
                    "            formData: {}\n" +
                    "        };\n" +
                    "    },\n" +
                    "    methods: {\n" +
                    "        addValue() {\n" +
                    "            if (this.formData.value != this.formData.value2 || !this.formData.value) {\n" +
                    "                alert('Bad password');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            this.$http.post('api/values', this.formData)\n" +
                    "                .then(\n" +
                    "                    r => this.$router.go('/show-values'),\n" +
                    "                    error => alert(\"HTTP \" + error.status + \", please try again.\"));\n" +
                    "        }\n" +
                    "    }\n" +
                    "});\n"));
    }
}
