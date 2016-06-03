package com.cathive.convex.geometry;

/**
 * Object oriented representation of an arc with a center point Z,
 * a radius r, a start angle rho and an extend angle beta.
 * @author Alexander Erben
 */
class Arc {

    /**
     * Center point
     */
    private final Point z;

    /**
     * Radius
     */
    private final double r;

    /**
     * Start angle
     */
    private final Angle rho;

    /**
     * Extent angle
     */
    private final Angle beta;

    /**
     * Private ctor
     * @param z center
     * @param r radius
     * @param rho start
     * @param beta extent
     */
    private Arc(Point z, double r, Angle rho, Angle beta) {
        this.z = z;
        this.r = r;
        this.rho = rho;
        this.beta = beta;
    }

    /**
     * Derive an arc from to touching points A and B and the angle of the
     * constant bow angle alpha.
     * @param a first touching point
     * @param b second touching point
     * @param alpha bow angle
     * @return resulting arc
     */
    static Arc of(final Point a, final Point b, final Angle alpha) {
        final Point m = a.add(b).half();
        final double d = a.distanceTo(b);
        final double k = -(d / (2d * Math.tan(alpha.rad())));
        final double kDivD = k / d;
        final Point w = Point.of(a.getY() - b.getY(), b.getX() - a.getX()).multiply(kDivD);
        final Point z = m.add(w);
        final double r = d / (2d * Math.sin(alpha.rad()));
        final Point e = Point.of(1, 0);
        Angle rho = AngleCalculator.angleOf(e, Point.ORIGIN, a.subtract(z));
        if (a.getY() < z.getY()) {
            rho = Angle.fromRad(2d * Math.PI - rho.rad());
        }
        final Angle beta = Angle.fromRad(2d * (Math.PI - alpha.rad()));
        return new Arc(z, r, rho, beta);
    }

    /**
     * Cut this arc using the given rho angles
     * @param rhos rho_s angle
     * @param rhoe rho_e angle
     * @return the cut arc
     */
    Arc cutArc(final Angle rhos, Angle rhoe){
        return new Arc(this.z, this.r, this.rho.add(rhos), this.beta.subtract(rhos).subtract(rhoe));
    }

    /**
     * Apply the values of this arc to the given collector
     * @param collector to apply to
     * @return whatever the collector returns
     */
    <T> T accept(AngleHull.ArcCollector<T> collector){
        return collector.apply(this.z, this.r, this.beta, this.rho);
    }
    
}