package com.cathive.convex.geometry;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Each instance represents one of the four parts of which a convex hull is comprised if it contains more than 3
 * points.
 * This class contains the algorithm to derive the hull path from an outline path. Spoken in a metaphor,
 * the algorithm straightens out the path so that walking over the path would result in right turn always.
 * @author Alexander Erben
 */
class ConvexHullPart {

    private final List<Point> points;

    private ConvexHullPart(List<Point> points) {
        this.points = points;
    }

    /**
     * Setup the hull part from an outline segment
     * @param points to setup from. Must be greater than 0.
     * @return corresponding hull part
     */
    static ConvexHullPart calculate(final List<Point> points) {
        List<Point> correct = correct(points);
        return new ConvexHullPart(correct);
    }

    private static List<Point> correct(final List<Point> points) {
        List<Point> current = new ArrayList<>(points);
        OptionalInt pjOpt;
        while ((pjOpt = advance(current)).isPresent()) {
            final int pj = pjOpt.orElseGet(() -> {throw new IllegalStateException("pj optional not set!");});
            final int i = walkBack(current, pj);
            current = concat(current.subList(0, i + 1).stream(), current.subList(pj + 1, current.size()).stream()).collect(toList());
        }
        return current;
    }

    private static int walkBack(final List<Point> current, final int pj) {
        final Point pjPlus1 = current.get(pj + 1);
        for (int i = pj; i > 0; i--) {
            if (Side.determine(current.get(i - 1), current.get(i), pjPlus1) == Side.RIGHT) {
                return i;
            }
        }
        return 0;
    }

    private static OptionalInt advance(final List<Point> ps) {
        for (int i = 0; i < ps.size() - 2; i++) {
            if (Side.determine(ps.get(i), ps.get(i + 1), ps.get(i + 2)) == Side.LEFT_OR_COLLINEAR) {
                return OptionalInt.of(i + 1);
            }
        }
        return OptionalInt.empty();
    }

    public List<Point> getPoints() {
        return points;
    }

    private enum Side {
        LEFT_OR_COLLINEAR, RIGHT;

        private static Side determine(final Point A, final Point B, final Point C) {
            final long result = Determinant.of(A, B, C);
            return result >= 0 ? LEFT_OR_COLLINEAR : RIGHT;
        }
    }
}
