package com.nickmcdowall.lsd.http.common;

/**
 * Useful to keep the interaction messages consistent across varios http interceptors.
 */
public class HttpInteractionMessageTemplates {

    public static final String REQUEST_TEMPLATE = "%s %s from %s to %s";
    public static final String RESPONSE_TEMPLATE = "%s response from %s to %s";

    public static String requestOf(String method, String path, String sourceName, String destinationName) {
        return String.format(REQUEST_TEMPLATE, method, path, sourceName, destinationName);
    }

    public static String responseOf(String message, String destinationName, String sourceName) {
        String interaction = String.format(RESPONSE_TEMPLATE, message, destinationName, sourceName);
        if (interaction.startsWith("4") || interaction.startsWith("5")) {
            interaction += " [#red]";
        }
        return interaction;
    }
}
