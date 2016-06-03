package com.cathive.convex.geometry;

/**
 * Object oriented representation of an angle between two vectors.
 * Offers means to extract the angle in radians and degree values.
 * @author Alexander Erben
 */
public final class Angle implements Comparable<Angle> {

    /**
     * The angle with the radians value of 0
     */
    static final Angle ZERO = new Angle(0);

    /**
     * The angle extent is stored as radians value with double precision
     */
    private final double radians;

    /**
     * Private constructor
     * @param radians radians value of the angle
     */
    private Angle(double radians) {
        this.radians = radians;
    }

    /**
     * Derive an angle from the radians value
     * @param radians to use
     * @return angle with the given radians
     */
    public static Angle fromRad(final double radians) {
        return new Angle(radians);
    }

    /**
     * Derive an angle from the degree value
     * @param degrees to use
     * @return angle with the given degrees
     */
    public static Angle fromDeg(final double degrees) {
        return new Angle(Math.toRadians(degrees));
    }

    /**
     * Extract the radians of this angle
     * @return radians
     */
    public double rad() {
        return this.radians;
    }

    /**
     * Extract the degree value of this angle
     * @return degree
     */
    public double deg() {
        return Math.toDegrees(this.radians);
    }

    /**
     * Return a new angle with doubled radians
     * @return new angle with updated radians
     */
    Angle timesTwo() {
        return new Angle(this.radians * 2d);
    }

    /**
     * Subtract the radians of the given angle from this angle's radians and return
     * a new angle with the updated value
     * @param subtrahend to subtract
     * @return new angle with updated radians
     */
    Angle subtract(Angle subtrahend) {
        return new Angle(this.radians - subtrahend.rad());
    }

    /**
     * Add the radians of the given angle to this angle's radians and return
     * a new angle with the updated value
     * @param augend to add
     * @return new angle with updated radians
     */
    Angle add(Angle augend) {
        return new Angle(this.radians + augend.rad());
    }

    /**
     * {@inheritDoc}
     * Two angles are compared by their radians value. The angle with the
     * larger radians is the larger angle.
     * @param o other angle
     * @return see {@link Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(Angle o) {
        return new Double(this.radians).compareTo(o.radians);
    }

}
