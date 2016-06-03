package com.cathive.convex.geometry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.EnumMap;
import java.util.List;

import static com.cathive.convex.geometry.Region.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * An outline ("Konturpolygon") is a path around a point set such that all points are inside the polygon
 * outlined by the path. It is used in the {@link ConvexHull} algorithm as intermediate state.
 *
 * @author Alexander Erben
 */
final class Outline {

    private final EnumMap<Region, List<Point>> regions;

    private Outline(EnumMap<Region, List<Point>> regions) {
        for (final Region region : Region.values()) {
            Preconditions.checkArgument(regions.containsKey(region), "Region not defined: " + region);
        }
        this.regions = regions;
    }

    /**
     * Factory that calculates the outline of a sorted set of input {@link Point}s
     * To that end, the left and right sweep is performed on the point set. The two polygon parts
     * that are the result of the sweeps are joined such that no point is contained twice in the result.
     *
     * @param input to calculate the outline for. Must be longer than 3 elements.
     * @return the outline
     */
    static Outline of(final SortedPointSet input) {
        checkState(input.asList().size() > 3, "The input list size must be gt 3!");
        final SweepLine left = input.apply(new SweepLine(), SortedPointSet.ApplicationOrder.LEFT_TO_RIGHT);
        final SweepLine right = input.apply(new SweepLine(), SortedPointSet.ApplicationOrder.RIGHT_TO_LEFT);
        final EnumMap<Region, List<Point>> regions = new EnumMap<>(Region.class);
        regions.put(NW, left.upper());
        regions.put(NE, Lists.reverse(right.upper()));
        regions.put(SE, right.lower());
        regions.put(SW, Lists.reverse(left.lower()));
        return new Outline(regions);
    }

    List<Point> partForRegion(Region region) {
        return this.regions.get(region);
    }
}