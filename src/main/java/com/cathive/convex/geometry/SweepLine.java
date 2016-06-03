package com.cathive.convex.geometry;

import java.util.*;

/**
 * Part of the algorithm that calculates the {@link Outline} from a {@link SortedPointSet}.
 * Implemented using a visitor pattern where all points of the set are discovered in order.
 * @author Alexander Erben
 */
final class SweepLine {

    /**
     * Is the sweep line in virgin state?
     */
    private boolean initial = true;

    /**
     * Y value of the point that has the lowest y value point yet discovered
     */
    private int minYSoFar;

    /**
     * Y value of the point that has the highest y value point yet discovered
     */
    private int maxYSoFar;

    /**
     * The upper half of the discovered points. Initial element included.
     */
    private final List<Point> upper = new ArrayList<>();

    /**
     * The lower half of the discovered points. Initial element included.
     */
    private final List<Point> lower = new ArrayList<>();

    /**
     * Discover a point. Update the internal state of this line. If it is the initial point, it is added to the points deque.
     * If the point is greater than yMax, the point will
     * be the new yMax and added to the back of the deque. If it is lower than yMin, it is the new yMin and added to
     * the front of the deque.
     * @param point to discover
     */
    void discover(final Point point) {
        if (this.initial) {
            this.initial = false;
            this.minYSoFar = point.getY();
            this.maxYSoFar = point.getY();
            this.upper.add(point);
            this.lower.add(point);
        } else if (point.getY() > this.maxYSoFar) {
            this.maxYSoFar = point.getY();
            this.upper.add(point);
        } else if (point.getY() < this.minYSoFar) {
            this.minYSoFar = point.getY();
            this.lower.add(point);
        }
    }

    /**
     * Return the upper part of the discovered points. The first discovered point is included.
     * @return upper half
     */
    List<Point> upper() {
        return this.upper;
    }

    /**
     * Return the lower part of the discovered points. The first discovered point is included.
     * @return lower half
     */
    List<Point> lower(){
        return this.lower;
    }

}
