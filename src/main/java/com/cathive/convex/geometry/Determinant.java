package com.cathive.convex.geometry;

/**
 * Calculates the determinant of three points
 * @author Alexander Erben
 */
final class Determinant {

    /**
     * Calculate the determinant of three points
     * @param A point A
     * @param B point B
     * @param C point C
     * @return determinant
     */
    static long of(final Point A, final Point B, final Point C) {
        final long Ax = A.getX();
        final long Ay = A.getY();
        final long Bx = B.getX();
        final long By = B.getY();
        final long Cx = C.getX();
        final long Cy = C.getY();
        return (Cx - Ax) * (Cy + Ay) + (Bx - Cx) * (By + Cy) + (Ax - Bx) * (Ay + By);
    }
}
