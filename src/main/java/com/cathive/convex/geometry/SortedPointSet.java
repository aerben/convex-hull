package com.cathive.convex.geometry;

import java.util.*;

/**
 * Contains a sorted set of {@link Point}. The sorting order is determined by {@link Point}'s {@link Comparator}.
 * Cannot contain two points for which {@link Point#equals(Object)} is true.
 * @author Alexander Erben
 */
final class SortedPointSet {

    /**
     * The encapsulated data structure that holds the {@link Point} sorted by their {@link Comparator}
     */
    private final SortedSet<Point> sortedPoints;

    /**
     * Private constructor
     * @param sortedPoints point set
     */
    private SortedPointSet(final SortedSet<Point> sortedPoints) {
        this.sortedPoints = sortedPoints;
    }

    /**
     * Construct a new sorted point set by a given list of points. The list must not be <code>null</code>.
     * @param points to sort
     * @return sorted point set.
     */
    public static SortedPointSet of(final List<Point> points) {
        return new SortedPointSet(new TreeSet<>(points));
    }

    /**
     * Apply all points in this set to a {@link SweepLine} by calling {@link SweepLine#discover(Point)} on all points
     * according to the application order determined by the passed order parameter.
     * @param line to show the points
     * @param order to apply, which may be left-to-right or right-to-left
     * @return line after application
     */
    public SweepLine apply(final SweepLine line, final ApplicationOrder order) {
        order.apply(this.sortedPoints, line);
        return line;
    }

    int size() {
        return this.sortedPoints.size();
    }

    List<Point> asList() {
        return new ArrayList<>(this.sortedPoints);
    }

    /**
     * There may be two orders of application of {@link Point}s to {@link SweepLine}s: left-to-right and right-to-left.
     * The former calls {@link SweepLine#discover(Point)} on the points in ascending order with regard to their
     * {@link Comparator}, the latter in inverse order.
     */
    enum ApplicationOrder {
        LEFT_TO_RIGHT {
            @Override
            void apply(final SortedSet<Point> sortedPoints, final SweepLine line) {
                sortedPoints.forEach(line::discover);
            }
        },
        RIGHT_TO_LEFT {
            @Override
            void apply(final SortedSet<Point> sortedPoints, final SweepLine line) {
                final List<Point> reversed = new ArrayList<>(sortedPoints);
                Collections.reverse(reversed);
                reversed.forEach(line::discover);
            }
        };

        abstract void apply(final SortedSet<Point> sortedPoints, final SweepLine line);
    }
}
