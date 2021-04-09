package com.nickmcdowall.lsd.http.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

import static com.google.gson.JsonParser.parseString;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.dom4j.DocumentHelper.parseText;

public class PrettyPrinter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final OutputFormat format = OutputFormat.createPrettyPrint();

    public static String parse(final String document) {
        if (isBlank(document)) {
            return document;
        }
        return indentJson(document).orElseGet(() -> indentXml(document).orElse(document));
    }

    private static Optional<String> indentJson(final String document) {
        final JsonElement jsonElement;
        try {
            jsonElement = parseString(document);
            return Optional.of(GSON.toJson(jsonElement));
        } catch (final JsonParseException e) {
            return empty();
        }
    }

    private static Optional<String> indentXml(final String document) {
        final StringWriter sw = new StringWriter();
        try {
            new XMLWriter(sw, format).write(parseText(document));
            return Optional.of(sw.toString());
        } catch (final IOException | DocumentException e) {
            return empty();
        }
    }
}
