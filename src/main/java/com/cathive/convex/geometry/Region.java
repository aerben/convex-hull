package com.cathive.convex.geometry;

/**
 * The four regions of a convex hull or outline.
 */
enum Region {
    /**
     * left to top extreme point
     */
    NW,
    /**
     * top to right extreme point
     */
    NE,
    /**
     * right to bottom extreme point
     */
    SE,
    /**
     * bottom to left extreme point
     */
    SW
}
