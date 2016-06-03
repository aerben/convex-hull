package com.cathive.convex.ui;

import com.cathive.convex.geometry.ConvexHull;
import com.cathive.convex.geometry.Point;
import com.cathive.convex.ui.UndoRedoHandler.UndoRedoUnit;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Stream;

/**
 * The main rendering pane in which all points and the convex hull are rendered.
 *
 * @author Alexander Erben
 */
public class RenderingPane extends Pane implements EditModeAware {

    /**
     * Handles edit mode state transition.
     */
    private EditModeHandler editModeHandler;

    /**
     * Manages undo and redo.
     */
    private UndoRedoHandler undoRedoHandler;

    /**
     * Holds some rendering specific states
     */
    private RenderingSettings settings;

    /**
     * This group holds the source points from which the {@link ConvexHull}
     * is calculated on change.
     */
    private final PointsGroup pointsGroup = new PointsGroup();

    /**
     * This group holds the polygon displaying the {@link ConvexHull} of the {@link Point}s
     * displayed in {@link PointsGroup},
     */
    private final ConvexAndAngleHullGroup convexAndAngleHullGroup = new ConvexAndAngleHullGroup(this.pointsGroup);

    /**
     * Setup the component
     */
    public RenderingPane() {
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        this.getChildren().addAll(this.convexAndAngleHullGroup, this.pointsGroup);
    }

    /**
     * Inject the {@link EditModeHandler} and register this components and the {@link PointsGroup}
     *
     * @param editModeHandler to inject
     */
    @FXML
    public void setEditModeHandler(final EditModeHandler editModeHandler) {
        editModeHandler.register(this);
        editModeHandler.register(this.pointsGroup);
        this.editModeHandler = editModeHandler;
    }

    /**
     * Inject the {@link UndoRedoHandler} into this component as well as into the {@link PointsGroup}
     *
     * @param undoRedoHandler to inject
     */
    @FXML
    public void setUndoRedoHandler(final UndoRedoHandler undoRedoHandler) {
        this.pointsGroup.setUndoRedoHandler(undoRedoHandler);
        this.undoRedoHandler = undoRedoHandler;
    }

    /**
     * Getter for {@link EditModeHandler}
     *
     * @return edit mode handler
     */
    @FXML
    public EditModeHandler getEditModeHandler() {
        return this.editModeHandler;
    }

    /**
     * Getter for {@link UndoRedoHandler}
     *
     * @return undo redo handler
     */
    @FXML
    public UndoRedoHandler getUndoRedoHandler() {
        return this.undoRedoHandler;
    }

    /**
     * Getter for {@link RenderingSettings}
     * @return rendering settings
     */
    @FXML
    public RenderingSettings getSettings() {
        return this.settings;
    }

    /**
     * Setter for {@link RenderingSettings}
     * @param settings rendering settings
     */
    @FXML
    public void setSettings(RenderingSettings settings) {
        this.convexAndAngleHullGroup.setSettings(settings);
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     * Activates drag-and-drop on this {@link Pane}. The events received on drag-over will be used
     * to createAndPerformOnce a new {@link Point} to be drawn in {@link PointsGroup}, replacing the currently dragged point.
     * The {@link javafx.scene.input.Dragboard} is filled in {@link PointsGroup}, holding the index of the currently
     * dragged point to use in {@link PointsGroup#movePoint(int, Point)}.
     * <p>
     * Also propagates the performed move to the {@link UndoRedoHandler}. The initial coordinate is read from the dragboard
     * and used to construct a {@link UndoRedoUnit} that may be used to reset the move operation to the state before
     * dragging.
     */
    @Override
    public void onMoveActivated() {
        setCursor(Cursor.HAND);
        this.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            Dragboards.read(event.getDragboard()).ifPresent(indexedCoordinate -> {
                final Point newPoint = Point.of(event.getX(), event.getY());
                if (newPoint.inBounds(0, 0, getWidth(), getHeight())) {
                    this.pointsGroup.movePoint(indexedCoordinate.index, newPoint);
                }
                event.consume();
            });
            ((Node) event.getSource()).setCursor(Cursor.CLOSED_HAND);
        });
        this.setOnDragDropped(dropped -> {
            final double dropX = dropped.getX();
            final double dropY = dropped.getY();
            Dragboards.read(dropped.getDragboard()).ifPresent(
                    oldCoord -> this.undoRedoHandler.addUnit(
                            UndoRedoUnit.factory.createAndPerformOnce(
                                    () -> this.pointsGroup.movePoint(oldCoord.index, Point.of(dropX, dropY)), () -> this.pointsGroup.movePoint(oldCoord.index, Point.of(oldCoord.x, oldCoord.y))
                            )));
            RenderingPane.this.setCursor(Cursor.HAND);
            dropped.setDropCompleted(true);
        });
    }

    /**
     * {@inheritDoc}
     * Deactivates drag and drop on this {@link Pane} and resets the cursor.
     */
    @Override
    public void onMoveDeactivated() {
        this.setOnDragOver(null);
        setCursor(Cursor.DEFAULT);
    }

    /**
     * {@inheritDoc}
     * Activates drawing of {@link Point}s by registering a {@link javafx.scene.input.MouseEvent} listening to
     * {@link javafx.scene.input.MouseEvent#MOUSE_CLICKED}. New points are added to {@link PointsGroup} on-click.
     * Sets a sensible cursor icon as well.
     */
    @Override
    public void onDrawActivated() {
        setCursor(Cursor.CROSSHAIR);
        this.setOnMouseClicked((mouseEvent) -> this.pointsGroup.add(Point.of(mouseEvent.getX(), mouseEvent.getY())));
    }

    /**
     * {@inheritDoc}
     * Deactivates drawing of {@link Point}s by deregistering the {@link javafx.scene.input.MouseEvent#MOUSE_CLICKED}
     * event. Resets the cursor as well.
     */
    @Override
    public void onDrawDeactivated() {
        setCursor(Cursor.DEFAULT);
        this.setOnMouseClicked(null);
    }

    /**
     * Add a {@link List} of {@link Point}s to this pane. Forwards the points to {@link PointsGroup}, which in turn
     * will handle the drawing of the points on the pane. Undo is handled there as well.
     *
     * @param points to draw. Must not be null nor empty.
     */
    void addAll(final List<Point> points) {
        this.pointsGroup.addAll(points);
    }

    /**
     * Retrieve all currently drawn {@link Point}s on the backing {@link PointsGroup} of this pane as
     * {@link Stream}.
     *
     * @return point stream. May be empty.
     */
    Stream<Point> getPoints() {
        return this.pointsGroup.getPoints();
    }

    /**
     * Clear all {@link Point}s from the backing {@link PointsGroup} and add the passed points.
     * Undo is handled in the {@link PointsGroup}.
     *
     * @param with to replace with. Must not be null nor empty.
     */
    void clearAndReplace(final List<Point> with) {
        this.pointsGroup.clearAndReplace(with);
    }
}