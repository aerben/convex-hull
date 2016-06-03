package com.cathive.convex.geometry;

import java.util.Comparator;

/**
 * This class represents an immutable point consisting of an X and Y coordinate given as integer numbers.
 * @author Alexander Erben
 */
public final class Point implements Comparable<Point> {

    public static final Point ORIGIN = new Point(0, 0);
    /**
     * The X value of the coordinate. Value range is {@link Integer#MIN_VALUE} until {@link Integer#MAX_VALUE}.
     */
    private final int x;

    /**
     * The Y value of the coordinate. Value range is {@link Integer#MIN_VALUE} until {@link Integer#MAX_VALUE}.
     */
    private final int y;

    /**
     * Create a new point from to given {@link Number}s of arbitrary type. The {@link Number#intValue()}-method
     * is used to derive an integral value from the given instances, which may perform necessary rounding or flooring.
     *
     * @param x X value of the coordinate
     * @param y Y value of the coordinate
     * @return new immutable point.
     */
    public static Point of(final Number x, final Number y) {
        return new Point(x.intValue(), y.intValue());
    }

    /**
     * Constructor
     *
     * @param x X value
     * @param y Y value
     */
    private Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieve the integral representation of the X coordinate.
     *
     * @return X value of the coordinate. Value range is {@link Integer#MIN_VALUE} until {@link Integer#MAX_VALUE}.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Retrieve the integral representation of the Y coordinate.
     *
     * @return Y value of the coordinate. Value range is {@link Integer#MIN_VALUE} until {@link Integer#MAX_VALUE}.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Check whether this points is contained in a rectangle constructed by the given X and Y minimum values, width
     * and height. The {@link Number#intValue()}-method
     * is used to derive an integral value from all given values.
     * The point must then satisfy the following properties:
     * - x(point) GT minX(rectangle) AND x(point) LT minX(rectangle) + width(rectangle)
     * - y(point) GT minY(rectangle) AND y(point) LT minY(rectangle) + height(rectangle)
     *
     * @param xMin         The minimal X value of the rectangle
     * @param yMin         The minimal Y value of the rectangle
     * @param widthNumber  width of the rectangle
     * @param heightNumber height of the rectangle
     * @return true if contained, false if not.
     */
    public boolean inBounds(final Number xMin, final Number yMin,
                            final Number widthNumber, final Number heightNumber) {
        final int width = widthNumber.intValue();
        final int height = heightNumber.intValue();
        final int x = xMin.intValue();
        final int y = yMin.intValue();
        return this.x < width + x && this.x > x && this.y < height + y && this.y > yMin.intValue();
    }

    /**
     * Add the components of another point to this point's components
     * and return a new point with updated values.
     * @param augend to add
     * @return point with the values of the augend added to this point's values
     */
    Point add(final Point augend) {
        return new Point(this.x + augend.x, this.y + augend.y);
    }

    /**
     * Subtract the components of another point from this point's components
     * and return a new point with updated values.
     * @param subtrahend to subtract
     * @return point with the values of the subtrahend subtracted from this point's values
     */
    Point subtract(final Point subtrahend) {
        return new Point(this.x - subtrahend.x, this.y - subtrahend.y);
    }

    /**
     * Calculate the distance from this point to the other
     * @param other to measure the distance to
     * @return distance
     */
    double distanceTo(final Point other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    /**
     * Return a new point with the values for x and y halfed
     * @return new point with updated values
     */
    Point half() {
        return Point.of(this.x / (double) 2, this.y / (double) 2);
    }

    /**
     * Multiply the components of this point with the given multiplicand and return
     * a point with updated values.
     * @param multiplicand to timesTwo with
     * @return new point with updated values
     */
    Point multiply(double multiplicand) {
        return Point.of(
                this.x * multiplicand,
                this.y * multiplicand
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        final Point point = (Point) o;
        return this.x == point.x && this.y == point.y;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        return result;
    }

    /**
     * {@inheritDoc}
     * Compare the points lexicographically.
     * <ul>
     * <li>If the x values of the points are not equal, the point with the higher x value is selected</li>
     * <li>Else: If the y values of the points are not equal, the point with the higher y value is selected</li>
     * <li>Else: The points are equal</li>
     * </ul>
     *
     * @param o comparee
     * @return see the {@link Comparable} interface
     */
    @Override
    public int compareTo(final Point o) {
        return ((Comparator<Point>) (p1, p2) -> {
            if (p1.x < p2.x) return -1;
            else if (p1.x > p2.x) return 1;
            else if (p1.y < p2.y) return -1;
            else if (p1.y > p2.y) return 1;
            return 0;
        }).compare(this, o);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + this.x +
                ", y=" + this.y +
                '}';
    }
}