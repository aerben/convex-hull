package com.cathive.convex.ui;

import com.cathive.convex.geometry.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * A stream-based writer for {@link Point} that outputs to a file as {@link Path}.
 * @author Alexander Erben
 */
final class PointsWriter {

    /**
     * Write a non-empty stream of {@link Point}s to a file system location.
     * @param path to write to. Must not be null and point to a file that either not exists or is writable. Will replace
     *             all contents of the file if it exists!
     *             If the parent directory of this path does not exist, it will silenty be created.
     * @param points to write. Must not be null nor empty.
     */
    static void write(final Path path, final Stream<Point> points) {
        try {
            Files.createDirectories(path.getParent());
            Files.deleteIfExists(path);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        points.forEach(point -> {
            try {
                Files.write(path, (point.getX()+" "+point.getY()+"\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
