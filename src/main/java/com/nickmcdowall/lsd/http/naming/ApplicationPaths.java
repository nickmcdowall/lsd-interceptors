package com.nickmcdowall.lsd.http.naming;

import lombok.Value;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Provides a way to supply the paths (prefixes) belonging to the application being tested.
 * This helps with determining the names to use for source and destination entities on the generated sequence diagrams.
 */
@Value
public class ApplicationPaths {
    Collection<String> pathPrefixes = new HashSet<>();

    public ApplicationPaths(Collection<String> pathPrefixes) {
        this.pathPrefixes.addAll(pathPrefixes);
    }

    public Stream<String> stream() {
        return pathPrefixes.stream();
    }

    public ApplicationPaths addAll(Collection<String> additionalPaths) {
        this.pathPrefixes.addAll(additionalPaths);
        return this;
    }
}
