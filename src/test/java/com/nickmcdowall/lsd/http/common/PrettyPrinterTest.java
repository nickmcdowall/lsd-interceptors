package com.nickmcdowall.lsd.http.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PrettyPrinterTest {

    @Test
    void handleNonJsonOrXml() {
        String inputText = "my random text";

        assertThat(PrettyPrinter.prettyPrint(inputText)).isEqualTo(inputText);
    }

    @Test
    void prettyPrintJson() {
        String inputText = "{\"name\":\"nick\"}";

        assertThat(PrettyPrinter.prettyPrint(inputText)).isEqualTo(
                "{\n" +
                        "  \"name\": \"nick\"\n" +
                        "}");
    }

    @Test
    void prettyPrintXml() {
        String inputText = "<names><name>nick</name></names>";

        assertThat(PrettyPrinter.prettyPrint(inputText)).isEqualTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "\n" +
                        "<names>\n" +
                        "  <name>nick</name>\n" +
                        "</names>\n");
    }
}