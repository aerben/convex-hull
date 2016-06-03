package com.cathive.convex.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Handler for {@link EditMode} transitions to be used application-wide.
 * @author Alexander Erben
 */
public class EditModeHandler {

    /**
     * Currently listening {@link EditModeAware}s to be notified on state transition.
     */
    private final Collection<EditModeAware> aware = Collections.synchronizedCollection(new ArrayList<>());

    /**
     * Previously set state.
     */
    private EditMode prev = EditMode.NONE;

    /**
     * Register a new listener.
     * @param editModeAware to register. Must not be null!
     */
    void register(final EditModeAware editModeAware) {
        this.aware.add(editModeAware);
    }

    /**
     * Transition the {@link EditMode}. Will call all {@link EditModeAware}s listening to this handler
     * to notify them that the previous state is deactivated and the new state is activated.
     * @param mode to set
     */
    void transition(final EditMode mode) {
        EditMode prev = this.prev;
        applyListeners(prev);
        applyListeners(mode);
        this.prev = mode;
    }

    private void applyListeners(EditMode prev) {
        switch (prev) {
            case DELETE:
                this.aware.forEach(EditModeAware::onDeleteDeactivated);
                break;
            case MOVE:
                this.aware.forEach(EditModeAware::onMoveDeactivated);
                break;
            case DRAW:
                this.aware.forEach(EditModeAware::onDrawDeactivated);
                break;
            case NONE:
                break;
        }
    }

}
