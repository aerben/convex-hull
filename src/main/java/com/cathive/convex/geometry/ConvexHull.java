package com.cathive.convex.geometry;

import java.util.EnumMap;
import java.util.List;

import static com.cathive.convex.geometry.ConvexHullPart.calculate;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;


/**
 * The convex hull of a point set is the shortest path around it such that all points
 * are inside the polygon outlined by the path. Of all possible {@link Outline}s of a {@link SortedPointSet},
 * it has the smallest sum of all vertices.
 * <p>
 * More formally, the convex hull CH of a point set S of cardinality n is the smallest convex set that contains S.
 * A polygon is convex if all contained polygons are convex.
 * A point set is convex if all paths between two points of the sets are completely contained in the set.
 * <p>
 * This implementation allows for updates of the convex hull while retaining its immutability:
 * each call to {@link #update(List)} returns a new hull with updated hull segments where updates are needed.
 *
 * @author Alexander Erben
 */
public abstract class ConvexHull {

    /**
     * Setup a updateable convex hull from points.
     * Depending on the number of points in the set, the correct internal implementation for small
     * hulls (lt 4 pp) and large hulls (lteq 4 points) is selected.
     *
     * @param pp to setup from
     * @return updateable hull
     */
    public static ConvexHull setup(List<Point> pp) {
        SortedPointSet sorted = SortedPointSet.of(pp);
        if (sorted.size() < 4) {
            return new SmallState(sorted.asList());
        }
        return BigState.initial(Outline.of(sorted));
    }

    /**
     * Return a new updateable hull with updated points.
     *
     * @param pp new points
     * @return updated hull
     */
    public abstract ConvexHull update(List<Point> pp);

    /**
     * Return the points of which the hull is comprised.
     *
     * @return points
     */
    public abstract List<Point> getPoints();

    /**
     * This state represents a hull with less than 4 points. No algorithm has to be applied in that case.
     */
    private static class SmallState extends ConvexHull {
        private final List<Point> points;

        private SmallState(List<Point> points) {
            this.points = points;
        }

        @Override
        public List<Point> getPoints() {
            return this.points;
        }

        @Override
        public ConvexHull update(final List<Point> pp) {
            if (pp.size() < 4) {
                return new SmallState(pp);
            } else {
                return BigState.initial(Outline.of(SortedPointSet.of(pp)));
            }
        }
    }

    /**
     * This state represents a hull with more than 3 points. The four segments of a convex hull must be calculated in
     * that case. The four segments are the following:
     * - north west: left to top extreme points
     * - north east: top to right extreme points
     * - south east: right to south extreme points
     * - south west: south to left extreme points
     */
    private static class BigState extends ConvexHull {

        private final Outline previousOutline;
        private final EnumMap<Region, ConvexHullPart> convexHullParts;

        private BigState(final Outline previousOutline, final EnumMap<Region, ConvexHullPart> convexHullParts) {
            for (final Region region : Region.values()) {
                checkArgument(convexHullParts.containsKey(region), "Region not defined: " + region);
            }
            this.previousOutline = previousOutline;
            this.convexHullParts = convexHullParts;
        }

        private static BigState initial(Outline initial) {
            final EnumMap<Region, ConvexHullPart> parts = new EnumMap<>(Region.class);
            for (final Region region : Region.values()) {
                parts.put(region, calculate(initial.partForRegion(region)));
            }
            return new BigState(initial, parts);
        }

        @Override
        public ConvexHull update(final List<Point> newPoints) {
            if (newPoints.size() < 4) {
                return new SmallState(newPoints);
            }
            final Outline newOutline = Outline.of(SortedPointSet.of(newPoints));
            final EnumMap<Region, ConvexHullPart> parts = new EnumMap<>(Region.class);
            for (final Region region : Region.values()) {
                parts.put(region, newPart(newOutline, region));
            }
            return new BigState(newOutline, parts);
        }

        @Override
        public List<Point> getPoints() {
            return this.convexHullParts.values().stream()
                    .flatMap(pp -> pp.getPoints().stream())
                    .distinct()
                    .collect(toList());
        }

        private ConvexHullPart newPart(Outline newOutline, Region region) {
            final List<Point> previousOutlinePoints = this.previousOutline.partForRegion(region);
            final List<Point> newRegion = newOutline.partForRegion(region);
            return newRegion.equals(previousOutlinePoints) ?
                    this.convexHullParts.get(region) :
                    calculate(newRegion);
        }
    }
}