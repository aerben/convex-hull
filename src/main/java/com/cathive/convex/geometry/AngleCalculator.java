package com.cathive.convex.geometry;

/**
 * Helper methods to calculate angles from vectors determined by {@link Point}s
 * @author Alexander Erben
 */
class AngleCalculator {

    /**
     * Calculate the angle between two vectors BA and BC. Works only for angles
     * with a maximum value of 180 degrees.
     * @param a A
     * @param b B
     * @param c C
     * @return angle
     */
    static Angle angleOf(Point a, Point b, Point c) {
        double scalarProduct = scalarProduct(a.subtract(b), c.subtract(b));
        return Angle.fromRad(Math.acos(scalarProduct / (a.distanceTo(b) * c.distanceTo(b))));
    }

    /**
     * See {@link AngleCalculator#angleOf(Point, Point, Point)}
     * @param aE A
     * @param bE B
     * @param cE C
     * @return angle between the two vectors determined by the input points
     */
    static Angle angleOf(CircularList.Entry<Point> aE, CircularList.Entry<Point> bE, CircularList.Entry<Point> cE) {
        return angleOf(aE.get(), bE.get(), cE.get());
    }

    /**
     * Calculate the angle between two vectors AB and DC
     * @param a A
     * @param b B
     * @param c C
     * @param d D
     * @return angle between the two vectors determined by the input points
     */
    static Angle angleOf(Point a, Point b, Point c, Point d) {
        return angleOf(a.subtract(b), Point.ORIGIN, d.subtract(c));
    }

    /**
     * Calculate the scalar product of two points
     * @param a A
     * @param b B
     * @return scalar product
     */
    private static double scalarProduct(Point a, Point b){
        return a.getX() * b.getX() + a.getY() * b.getY();
    }
}