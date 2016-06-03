package com.cathive.convex.geometry;

import com.cathive.convex.geometry.CircularList.Entry;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.cathive.convex.geometry.AngleCalculator.angleOf;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Given a point set and a fixed angle alpha, the angle hull is a path around the point set
 * such that a camera set to the aperture angle alpha taking images along the path
 * always displays all points of the point set,
 * but no empty space on the borders of the image.
 *
 * @author Alexander Erben
 */
public class AngleHull {

    /**
     * Arcs that construct this hull
     */
    private final List<CutArc> arcs;

    /**
     * Private constructor. Use {@link Factory#generateAngleHull(ConvexHull, Angle)}
     * to generate a hull.
     *
     * @param arcs of the hull
     */
    private AngleHull(List<CutArc> arcs) {
        this.arcs = arcs;
    }

    /**
     * Calls {@link ArcCollector#apply(Point, double, Angle, Angle)}
     * for each arc of this hull and collects the result of the function. Returns all calculated elements in a stream.
     * The selected strategy decides if cut or uncut arcs are used for collection.
     * The cut arcs might not be 100% precisely cut, so an external cutting algorithm might
     * want to use the uncut arcs.
     *
     * @param collector       accepted collector
     * @param cuttingStrategy determines if the collected arcs are cut, and thus non-overlapping, or uncut, thus overlapping.
     * @return result of all calls to the argument function
     */
    public <T> Stream<T> mapWith(final ArcCollector<T> collector, final CuttingStrategy cuttingStrategy) {
        switch (cuttingStrategy) {
            case CUT:
                return this.arcs.stream().map(CutArc::cut).map(a -> a.accept(collector));
            case UNCUT:
                return this.arcs.stream().map(CutArc::uncut).map(a -> a.accept(collector));
            default:
                throw new IllegalStateException("Unrecognized cutting strategy: " + cuttingStrategy);
        }
    }

    /**
     * Used to collect the components of the arcs of an {@link AngleHull}
     */
    public interface ArcCollector<T> {
        /**
         * Called with the components of an arc of the angle hull
         *
         * @param z    center point
         * @param r    radius in px
         * @param beta start angle
         * @param rho  extent angle
         * @return an arbitraty non-null result
         */
        T apply(Point z, double r, Angle beta, Angle rho);
    }

    /**
     * Provides the algorithm to generate {@link AngleHull}s
     *
     * @author Alexander Erben
     */
    public static class Factory {

        /*
         * Singleton boilerplate
         */

        private static final Factory INSTANCE = new Factory();

        private Factory() {
        }

        public static Factory get() {
            return INSTANCE;
        }

        /**
         * Generate the angle hull from a convex hull and fixed angle alpha
         *
         * @param cv    convex hull
         * @param alpha fixed angle alpha. Smaller than 180 degrees, bigger than 0 degrees.
         * @return angle hull
         */
        public AngleHull generateAngleHull(final ConvexHull cv, final Angle alpha) {
            checkArgument(alpha.deg() > 0d && alpha.deg() < 180d, "Alpha must be gt 0 and lt 180 degrees");
            final List<Point> points = Lists.reverse(cv.getPoints());

            /*
            We start by searching for an initial valid pair ls and rs of points
            that are part of the convex hull.
            To that end, we use the "Winkelvergleichstest". If the angle between
            the points A = prev(ls), B = ls, C = rs and D = next(rs)
            if too large, ls and rs do not yet match as base point for an arc
            of the hull. If the angle is smaller than alpha, they are part of
            the angle hull.
            If rs and ls are the same point, we select next(rs) as second
            base point.

            rhoss is used to cut the arc later so that the arcs of the hull do not
            overlap.
             */
            final CircularList<Point> circularList = new CircularList<>(points);
            final Entry<Point> ls = circularList.first();
            Entry<Point> rs = circularList.first();
            Angle rhoss;
            while (wvt(ls.prev(), ls, rs, rs.next(), alpha)) {
                rs = rs.next();
            }
            if (ls.equalContent(rs)) {
                rs = rs.next();
                rhoss = Angle.ZERO;
            } else {
                rhoss = angleOf(rs, ls, ls.prev()).subtract(alpha).timesTwo();
            }
            return walk(ls, rs, rhoss, alpha);
        }

        /**
         * Walk around the convex hull to find all arcs that are part of the angle
         * hull. A "rotating caterpillars" algorithm is used, where in each step,
         * one of the points or even both points advance. In every step of the
         * loop, one arc is added to the result list.
         *
         * @param ls    left initial point
         * @param rs    right initial point. Must not be equal to ls.
         * @param rhoss angle used to cut the generated angles so that they do not overlap.
         * @param alpha alpha angle of the angle hull
         * @return the finished angle hull containing all calculated arcs
         */
        private AngleHull walk(final Entry<Point> ls, final Entry<Point> rs, Angle rhoss, final Angle alpha) {
            Entry<Point> ll = ls;
            Entry<Point> rr = rs;
            final List<CutArc> arcs = new ArrayList<>();
            do {
                Entry<Point> l = ll;
                Entry<Point> r = rr;
                Angle rhos = rhoss;
                Angle rhoe;
                if (wvt(l, l.next(), r, r.next(), alpha)) {

                    if (wvt(l, r, r, r.next(), alpha)) {
                        rhoe = angleOf(r.next(), r, l).subtract(alpha).timesTwo();
                        rhoss = angleOf(l, r.next(), r).timesTwo();
                    } else {
                        rhoe = Angle.ZERO;
                        rhoss = Angle.ZERO;
                    }
                    rr = r.next();
                } else {
                    if (l.next().equalContent(r)) {
                        rhoe = Angle.ZERO;
                        rhoss = Angle.ZERO;
                        rr = r.next();
                    } else {
                        rhoe = angleOf(l.next(), l, r).timesTwo();
                        rhoss = angleOf(r, l.next(), l).subtract(alpha).timesTwo();
                    }
                    ll = l.next();
                }
                final Arc arc = Arc.of(l.get(), r.get(), alpha);
                arcs.add(new CutArc(arc, arc.cutArc(rhos, rhoe)));
            } while (!ll.equalContent(ls) || !rr.equalContent(rs));
            return new AngleHull(arcs);
        }

        /**
         * The "Winkelvergleichstest". For four points A, B, C and D which
         * are in this case given as entries of a ring, a check is performed
         * if the vectory AB and DC cut each other. If they do not, the test
         * fails. If they do, the cut angle between them is calculated.
         * If it is smaller than alpha, the test fails. If it is gt alpha,
         * the test succeeds.
         *
         * @param aE    A
         * @param bE    B
         * @param cE    C
         * @param dE    D
         * @param alpha compared angle
         * @return true if the test is successful, false if not.
         */
        private boolean wvt(Entry<Point> aE, Entry<Point> bE, Entry<Point> cE, Entry<Point> dE, Angle alpha) {
            Point a = aE.get();
            Point b = bE.get();
            Point c = cE.get();
            Point d = dE.get();
            long determinant = Determinant.of(b.subtract(a), d.subtract(c), Point.ORIGIN);
            return determinant > 0 && angleOf(a, b, c, d).rad() >= alpha.rad();
        }
    }


    /**
     * Wrapper around a pair of {@link Arc}s, one of them cut, the other uncut.
     * "Cut" means that the extent and start angles of the arcs have been reduced
     * so that they do not overlap when put together in the {@link AngleHull}.
     * Used so that for every arc of the hull, the cut and uncut version can be retrieved
     * where neccessary.
     * The cut arcs might not be 100% precisely cut, so an external cutting algorithm might
     * want to use the uncut arcs.
     */
    private static final class CutArc {

        /**
         * The cut arc that will overlap
         */
        private final Arc uncut;

        /**
         * The cut arc that will not overlap
         */
        private final Arc cut;

        /**
         * Ctor
         *
         * @param uncut the uncut arc that will overlap
         * @param cut   the cut arc that will not overlap
         */
        private CutArc(Arc uncut, Arc cut) {
            this.uncut = uncut;
            this.cut = cut;
        }

        /**
         * Get the uncut arc
         *
         * @return uncut arc
         */
        private Arc uncut() {
            return this.uncut;
        }

        /**
         * Get the cut arc
         *
         * @return cut arc
         */
        private Arc cut() {
            return this.cut;
        }
    }

    /**
     * Used to determine which variant of the arcs is wanted when collecting the arcs of this hull
     */
    public enum CuttingStrategy {
        /**
         * Collect the cut, non-overlapping arcs
         */
        CUT,
        /**
         * Collect the uncut, overlapping arcs
         */
        UNCUT
    }
}