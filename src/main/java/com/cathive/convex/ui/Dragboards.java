package com.cathive.convex.ui;

import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Helper methods to interact with {@link Dragboard} in a type-safe way with regards to storing and retrieving
 * indexed coordinate values.
 *
 * @author Alexander Erben
 */
final class Dragboards {

    private static final DataFormat INDEX = new DataFormat("convexHull.index");

    private static final DataFormat X_COORDINATE = new DataFormat("convexHull.xCoordinate");

    private static final DataFormat Y_COORDINATE = new DataFormat("convexHull.YCoordinate");

    /**
     * Put an {@link IndexedCoordinate} into the dragboard.
     *
     * @param dragboard to fill. Must not be null.
     * @param value     to set. Must not be null.
     */
    static void put(final Dragboard dragboard, final IndexedCoordinate value) {
        final Map<DataFormat, Object> content = new HashMap<>();
        content.put(INDEX, value.index);
        content.put(X_COORDINATE, value.x);
        content.put(Y_COORDINATE, value.y);
        dragboard.setContent(content);
    }

    /**
     * Retrieve an {@link IndexedCoordinate} from the dragboard, or nothing if none is found.
     *
     * @param dragboard to pull from. Must not be null.
     * @return an option for an IndexedCoordinate, resolving if one is found.
     */
    static Optional<IndexedCoordinate> read(final Dragboard dragboard) {
        final Integer index = (Integer) dragboard.getContent(INDEX);
        final Double x = (Double) dragboard.getContent(X_COORDINATE);
        final Double y = (Double) dragboard.getContent(Y_COORDINATE);
        if (index != null && x != null && y != null) {
            return Optional.of(new IndexedCoordinate(index, x, y));
        } else {
            return Optional.empty();
        }
    }

    static class IndexedCoordinate {
        final int index;
        final double x;
        final double y;

        IndexedCoordinate(final int index, final double x, final double y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }
    }
}