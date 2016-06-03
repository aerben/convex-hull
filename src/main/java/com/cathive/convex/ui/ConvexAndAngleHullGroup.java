package com.cathive.convex.ui;

import com.cathive.convex.geometry.Angle;
import com.cathive.convex.geometry.AngleHull;
import com.cathive.convex.geometry.Point;
import com.cathive.convex.geometry.ConvexHull;
import com.google.common.collect.ImmutableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javafx.scene.paint.Color.*;
import static javafx.scene.shape.ArcType.OPEN;

/**
 * Holds the {@link Polygon} and {@link com.cathive.convex.geometry.Arc}s
 * displaying the {@link ConvexHull} and {@link AngleHull} of the content in {@link PointsGroup}.
 * The children of both {@link PointsGroup} and the encapsulated {@link Polygon} and
 * {@link com.cathive.convex.geometry.Arc}s are bound via a listener.
 *
 * @author Alexander Erben
 */
final class ConvexAndAngleHullGroup extends Group {

    /**
     * The factory for creating {@link AngleHull}s
     */
    private static final AngleHull.Factory HULL_FACTORY = AngleHull.Factory.get();

    /**
     * Holds the polygon displaying the {@link ConvexHull}
     */
    private final Polygon convexHullPolygon = new Polygon();

    /**
     * Holds the arcs displaying the {@link AngleHull}s
     */
    private final Group arcGroup = new Group();

    /**
     * Contains important settings, e.g. which angles to render and which precision to use.
     */
    private RenderingSettings settings;

    /**
     * Holds the current state of the convex hull rendered in this group. Updated atomically
     * on each state change of the {@link PointsGroup} passed in the constructor.
     */
    private final AtomicReference<ConvexHull> cv = new AtomicReference<>(ConvexHull.setup(new ArrayList<>()));

    /**
     * Creates a function that transforms a center point, radius, start and extent angle to a renderable jfx arc.
     */
    private AngleHull.ArcCollector<Arc> createArcCollector(Use use) {
        return (Point z, double r, Angle beta, Angle rho) -> {
            final Arc jfxArc = new Arc();
            jfxArc.setCenterX(z.getX());
            jfxArc.setCenterY(z.getY());
            jfxArc.setRadiusX(r - use.radiusOffset());
            jfxArc.setRadiusY(r - use.radiusOffset());
            jfxArc.setStartAngle(-rho.deg());
            jfxArc.setLength(-beta.deg());
            jfxArc.setType(OPEN);
            jfxArc.setStrokeType(StrokeType.CENTERED);
            jfxArc.setStroke(use.stroke());
            jfxArc.setFill(use.fill());
            jfxArc.setStrokeWidth(use.strokeWidth());
            return jfxArc;
        };
    }


    /**
     * Ctor. Creates a styled {@link Polygon} to render the {@link ConvexHull} and attaches a change listener
     * to {@link PointsGroup} which is called on each point location change.
     *
     * @param pointsGroup backing group containing the drawn points.
     */
    ConvexAndAngleHullGroup(final PointsGroup pointsGroup) {
        this.convexHullPolygon.setFill(TRANSPARENT);
        this.convexHullPolygon.setStroke(BLACK);
        this.convexHullPolygon.setStrokeWidth(2);
        getChildren().add(this.arcGroup);
        getChildren().add(this.convexHullPolygon);
        pointsGroup.addOnChangeListener(points -> {
            final ConvexHull newHull = this.cv.updateAndGet(convexHull -> convexHull.update(points));
            clearGroup();
            drawConvexHull(newHull);
            drawAngleHulls(newHull);
        });
    }

    /**
     * Rids the group of all displayed elements
     */
    private void clearGroup() {
        this.arcGroup.getChildren().clear();
        this.convexHullPolygon.getPoints().clear();
    }

    /**
     * Render the convex hull by adding points to the polygon
     *
     * @param hull to render
     */
    private void drawConvexHull(final ConvexHull hull) {
        final List<Double> elements =
                hull.getPoints().stream()
                        .flatMap(p -> ImmutableList.of((double) p.getX(), (double) p.getY()).stream())
                        .collect(toList());
        this.convexHullPolygon.getPoints().addAll(elements.<Double>toArray(new Double[elements.size()]));
    }

    /**
     * Render all configured angle hulls.
     * Two scenarios exist: precise and unprecise rendering.
     *
     * Precise rendering
     * As the cutting algorithm in {@link AngleHull} is not 100% precise, we use a trick to render the
     * cleanly cut arcs. For each configured angle, we perform two passes. In the first pass, only the strokes
     * are rendered, causing the arcs two overlap. Afterwards, in the second pass, the same arcs are redrawed
     * upon the strokes with white fill. This causes the overlapping sections to vanish.
     * The resulting rendering is clean, but takes more time than the unprecise rendering with pre-cut arcs.
     *
     * Unprecise rendering
     * The cutting algorithm of {@link AngleHull} is used to create almost-non-overlapping arcs that can be
     * displayed in one rendering pass, in contrast to precise mode. This mode is more than twice as fast,
     * but the resulting structures contain small overlapping sections.
     *
     * @param hull used to derive the angle hulls
     */
    private void drawAngleHulls(final ConvexHull hull) {
        if (hull.getPoints().size() >= 4) {
            final boolean preciseMode = this.settings.preciseModeActiveProperty().get();
            final List<Arc> arcs;
            if(preciseMode){
                arcs = this.settings.getDrawnAnglesPrecise().entrySet().stream()
                        .filter(entry -> entry.getValue().get())
                        .map(Map.Entry::getKey)
                        .sorted()
                        .map(deg -> HULL_FACTORY.generateAngleHull(hull, Angle.fromDeg(deg)))
                        .flatMap(ah -> {
                            final Stream<Arc> strokes = ah.mapWith(createArcCollector(Use.STROKE), AngleHull.CuttingStrategy.UNCUT);
                            final Stream<Arc> overlays = ah.mapWith(createArcCollector(Use.OVERLAY), AngleHull.CuttingStrategy.UNCUT);
                            return Stream.concat(strokes, overlays);
                        })
                        .collect(toList());
            } else {
                arcs = this.settings.getDrawnAnglesImprecise().entrySet().stream()
                        .filter(entry -> entry.getValue().get())
                        .map(Map.Entry::getKey)
                        .sorted()
                        .map(deg -> HULL_FACTORY.generateAngleHull(hull, Angle.fromDeg(deg)))
                        .flatMap(angleHull -> angleHull.mapWith(createArcCollector(Use.STROKE), AngleHull.CuttingStrategy.CUT))
                        .collect(toList());
            }
            this.arcGroup.getChildren().addAll(arcs);
        }
    }

    /**
     * Apply the application wide settings instance. A listener is registered
     * that causes the angle hull to be rerendered when settings change.
     * @param settings to apply
     */
    public void setSettings(RenderingSettings settings) {
        this.settings = settings;
        this.settings.onChange(() -> {
            final ConvexHull hull = this.cv.get();
            clearGroup();
            drawConvexHull(hull);
            drawAngleHulls(hull);
        });
    }

    /**
     * Determines if a rendered arc is used as overlay or stroke unit.
     */
    private enum Use {
        /**
         * The arc is used as an overlay to hide overlapping arcs
         */
        OVERLAY {
            @Override
            Color fill() {
                return WHITE;
            }

            @Override
            Color stroke() {
                return TRANSPARENT;
            }

            @Override
            int radiusOffset() {
                return 1;
            }

            @Override
            int strokeWidth() {
                return 0;
            }
        },
        /**
         * The arc is used to actually stroke the hull
         */
        STROKE {
            @Override
            Color fill() {
                return TRANSPARENT;
            }

            @Override
            Color stroke() {
                return RED;
            }

            @Override
            int radiusOffset() {
                return 0;
            }

            @Override
            int strokeWidth() {
                return 2;
            }
        };

        /**
         * Fill color for the arc
         * @return fill color
         */
        abstract Color fill();

        /**
         * Stroke color of the arc
         * @return stroke color
         */
        abstract Color stroke();

        /**
         * Amount to reduce the radius. This property is used
         * to prevent the fill from overlapping the stroke of the underlying arc
         * @return offset
         */
        abstract int radiusOffset();

        /**
         * Stroke width of the arc
         * @return stroke width
         */
        abstract int strokeWidth();
    }
}
