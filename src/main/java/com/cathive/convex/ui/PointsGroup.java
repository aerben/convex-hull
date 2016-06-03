package com.cathive.convex.ui;

import com.cathive.convex.geometry.Point;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

/**
 * Backing group for {@link RenderingPane}, holding all currently set points to display them to the user.
 *
 * @author Alexander Erben
 */
final class PointsGroup extends Group implements EditModeAware {

    /**
     * An empty icon to override any operating system specific dragging image.
     */
    private final Image dragImage = new Image(this.getClass().getClassLoader().getResourceAsStream("fxml/spacer.png"));

    /**
     * Handles undo and redo functionality
     */
    private UndoRedoHandler undoRedoHandler;

    @Override
    public void onMoveActivated() {
        circlePoints(getChildren()).forEach(CirclePoint::activateDrag);
    }

    @Override
    public void onMoveDeactivated() {
        circlePoints(getChildren()).forEach(CirclePoint::deactivateDrag);
    }

    /**
     * {@inheritDoc}
     * Deleting points with the click event used on the points is an undoable operation.
     */
    @Override
    public void onDeleteActivated() {
        getChildren().forEach(n -> n.setOnMouseClicked(event ->
                this.undoRedoHandler.addUnit(UndoRedoHandler.UndoRedoUnit.factory.createAndPerformOnce(
                        () -> getChildren().remove(n), () -> getChildren().add(n)
                ))));
    }

    @Override
    public void onDeleteDeactivated() {
        getChildren().forEach(n -> n.setOnMouseClicked(null));
    }

    /**
     * Retrieve all currently drawn {@link Point}s on this group as {@link Stream}.
     *
     * @return point stream. May be empty.
     */
    Stream<Point> getPoints() {
        return circlePoints(getChildren()).map(CirclePoint::getPoint);
    }

    /**
     * Add a {@link List} of {@link Point}s to this group.
     * This operation is undoable.
     *
     * @param points to draw. Must not be null nor empty.
     */
    void addAll(final List<Point> points) {
        final List<CirclePoint> cp = points.stream().map(CirclePoint::new).collect(toList());
        this.undoRedoHandler.addUnit(UndoRedoHandler.UndoRedoUnit.factory.createAndPerformOnce(
                () -> getChildren().addAll(cp), () -> cp.forEach((p) -> getChildren().remove(getChildren().size() - 1))
        ));
    }

    /**
     * Add a single {@link Point} to this group.
     * This operation is undoable.
     *
     * @param point to draw. Must not be null.
     */
    void add(final Point point) {
        final CirclePoint cp = new CirclePoint(point);
        this.undoRedoHandler.addUnit(UndoRedoHandler.UndoRedoUnit.factory.createAndPerformOnce(
                () -> getChildren().add(cp), () -> getChildren().remove(cp)
        ));
    }

    /**
     * Move a {@link Point} identified by its index in this group's children to the location marked
     * by the passed new point.
     * This operation is NOT undoable by itself. The {@link RenderingPane} handles move undo.
     *
     * @param index    index of the point to move in this group's children. Must be natural and smaller or equal than
     *                 the count of children of this.
     * @param newPoint to move the point to. Must not be null.
     */
    synchronized void movePoint(final int index, final Point newPoint) {
        checkArgument(index < getChildren().size(), "Index out of bounds.");
        final CirclePoint cp = new CirclePoint(newPoint);
        cp.activateDrag();
        getChildren().set(index, cp);
    }

    /**
     * Register a new change listener on all children of this group.
     *
     * @param consumer that should be notified if a change occurs on the list of children of this group.
     */
    void addOnChangeListener(final Consumer<List<Point>> consumer) {
        getChildren().addListener(
                (ListChangeListener<Node>) c ->
                        consumer.accept(
                                circlePoints(c.getList())
                                        .map(CirclePoint::getPoint)
                                        .collect(toList())));
    }

    /**
     * Clear all {@link Point}s and add the passed points.
     * This operation is undoable.
     *
     * @param with to replace with. Must not be null nor empty.
     */
    synchronized void clearAndReplace(final List<Point> with) {
        final ArrayList<Node> previous = new ArrayList<>(this.getChildren());
        this.undoRedoHandler.addUnit(UndoRedoHandler.UndoRedoUnit.factory.createAndPerformOnce(
                () -> {
                    this.getChildren().removeAll(this.getChildren());
                    addAll(with);
                }, () -> {
                    this.getChildren().removeAll(this.getChildren());
                    this.getChildren().addAll(previous);
                }
        ));
    }

    /**
     * Inject the {@link UndoRedoHandler}
     * @param undoRedoHandler to inject
     */
    void setUndoRedoHandler(final UndoRedoHandler undoRedoHandler) {
        this.undoRedoHandler = undoRedoHandler;
    }

    /**
     * Filter a list of {@link Node}s and return only the ones that are instance of {@link CirclePoint}.
     *
     * @param children to filter. Must not be null.
     * @return instances of {@link CirclePoint}.
     */
    private Stream<CirclePoint> circlePoints(final ObservableList<? extends Node> children) {
        return children.stream()
                .filter(child -> child instanceof CirclePoint)
                .map(child -> (CirclePoint) child);
    }

    /**
     * Enhances a simple {@link Point} with the capability to render it as as {@link Circle}.
     * The user does not need to hit the exact location of the point to drag it. This is achieved by
     * using a much larger size for the circle and using the style to render it small.
     *
     * @author Alexander Erben
     */
    private final class CirclePoint extends Circle {

        /**
         * Delegate
         */
        private final Point point;

        /**
         * Ctor. Applies the style.
         *
         * @param point delegate to display.
         */
        private CirclePoint(final Point point) {
            super(point.getX(), point.getY(), 10d, new RadialGradient(
                    0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.15, Color.TRANSPARENT),
                    new Stop(0.1, Color.BLUE)
            ));
            setSmooth(false);
            this.point = point;
        }

        /**
         * Get the delegate.
         *
         * @return delegate
         */
        private Point getPoint() {
            return this.point;
        }

        /**
         * Dragging is activated by setting an appropriate event handler.
         * On drag detect, the {@link Dragboard} is filled with the index of the current circle
         * in the children of {@link PointsGroup}. The {@link RenderingPane} uses this index
         * to handle the state transition.
         */
        private void activateDrag() {
            setOnDragDetected(event -> {
                final Dragboard dragboard = getScene().startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(PointsGroup.this.dragImage);
                final int index = PointsGroup.this.getChildren().indexOf(this);
                Dragboards.put(dragboard, new Dragboards.IndexedCoordinate(index, event.getX(), event.getY()));
                event.consume();
                ((Node) event.getSource()).setCursor(Cursor.CLOSED_HAND);
            });
        }

        /**
         * Reset the drag handle to deactivate dragging.
         */
        private void deactivateDrag() {
            setOnDragDetected(null);
        }
    }
}
