package com.cathive.convex.ui;

import com.cathive.convex.geometry.Point;
import com.google.common.collect.ImmutableList;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.toList;

/**
 * Reads points from an input file. Each line must contain the two values for x and y of each point, separated
 * by a whitespace character. All lines that do not contain exactly one integral number followed by a whitespace character
 * and then another integral number are ignored.
 * <p>
 * The allowed charsets are all charsets in {@link StandardCharsets}
 *
 * @author Alexander Erben
 */
class PointsReader {

    private static final Pattern P = Pattern.compile("^(-?\\d+) (-?\\d+)\\s*$");

    private static final List<Charset> supportedCharsets = ImmutableList.of(
            UTF_8,
            ISO_8859_1,
            US_ASCII,
            UTF_16,
            UTF_16BE,
            UTF_16LE
    );

    /**
     * Read a point file from a source path. See class header for information about supported input.
     * All supported charsets are tried before throwing an {@link UnsupportedCharsetException}
     *
     * @param source source path. Must point to an existing file.
     * @return parsed points
     */
    static List<Point> readPath(final Path source) {
        checkArgument(Files.exists(source), "File must exist!");
        for (final Charset charset : supportedCharsets) {
            try {
                return readStream(Files.lines(source, charset));
            } catch (Exception ignored) {
                // We must catch Exception here, not IOException.
                // Yes, NIO also throws unchecked IO Exceptions. Who would have thought that.

                // continue
            }
        }
        throw new UnsupportedCharsetException("Could not read input file!");

    }

    private static List<Point> readStream(final Stream<String> lines) {
        return lines
                .map(P::matcher)
                .filter(Matcher::matches)
                .map(matcher -> Point.of(parseInt(matcher.group(1)), parseInt(matcher.group(2))))
                .collect(toList());
    }
}
