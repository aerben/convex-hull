package com.cathive.convex.ui;

import com.cathive.convex.geometry.AngleHull;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Application wide settings.
 * @author Alexander Erben
 */
public class RenderingSettings {

    /**
     * Angles that can be configured for rendering {@link AngleHull}s
     */
    private static final int[] ALLOWED_ANGLES = {30, 45, 60, 75, 90, 120, 150};

    /**
     * Maps deegree angles to a property that determines if an angle hull should be rendered
     * for them when imprecise mode is active.
     */
    private final ObservableMap<Integer, BooleanProperty> drawnAnglesImprecise = FXCollections.observableHashMap();

    /**
     * Maps deegree angles to a property that determines if an angle hull should be rendered
     * for them when precise mode is active.
     */
    private final ObservableMap<Integer, BooleanProperty> drawnAnglesPrecise = FXCollections.observableHashMap();

    /**
     * Holds the information if the precise rendering mode is active
     */
    private final BooleanProperty preciseModeActive = new SimpleBooleanProperty(false);

    /**
     * Sets up some sensible defaults
     */
    public RenderingSettings(){
        for (int allowedAngle : ALLOWED_ANGLES) {
            this.drawnAnglesImprecise.put(allowedAngle, new SimpleBooleanProperty(true));
            this.drawnAnglesPrecise.put(allowedAngle, new SimpleBooleanProperty(false));
        }
        this.drawnAnglesPrecise.get(90).setValue(true);
    }

    /**
     * Maps deegree angles to a property that determines if an angle hull should be rendered
     * for them when imprecise mode is active.
     */
    public ObservableMap<Integer, BooleanProperty> getDrawnAnglesImprecise() {
        return this.drawnAnglesImprecise;
    }

    /**
     * Maps deegree angles to a property that determines if an angle hull should be rendered
     * for them when precise mode is active.
     */
    public ObservableMap<Integer, BooleanProperty> getDrawnAnglesPrecise() {
        return this.drawnAnglesPrecise;
    }

    /**
     * Holds the information if the precise rendering mode is active
     */
    public BooleanProperty preciseModeActiveProperty() {
        return this.preciseModeActive;
    }

    /**
     * Allows to register a listener that is triggered when any of the settings change
     * @param r to register
     */
    public void onChange(final Runnable r){
        final ChangeListener<Boolean> listener = (observableValue, aBoolean, t1) -> r.run();
        this.drawnAnglesPrecise.forEach((i, p) -> p.addListener(listener));
        this.drawnAnglesImprecise.forEach((i, p) -> p.addListener(listener));
        this.preciseModeActive.addListener(listener);
    }

}
