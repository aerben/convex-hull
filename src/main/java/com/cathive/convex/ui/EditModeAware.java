package com.cathive.convex.ui;

/**
 * Implementations are to be notified on {@link EditMode} transition by {@link EditModeHandler}
 * @author Alexander Erben
 */
interface EditModeAware {

    /**
     * Called when {@link EditMode#MOVE} is activated
     */
    default void onMoveActivated() {

    }

    /**
     * Called when {@link EditMode#MOVE} is deactivated
     */
    default void onMoveDeactivated() {

    }

    /**
     * Called when {@link EditMode#DRAW} is activated
     */
    default void onDrawActivated() {

    }

    /**
     * Called when {@link EditMode#DRAW} is deactivated
     */
    default void onDrawDeactivated() {

    }

    /**
     * Called when {@link EditMode#DELETE} is activated
     */
    default void onDeleteActivated() {

    }

    /**
     * Called when {@link EditMode#DELETE} is deactivated
     */
    default void onDeleteDeactivated() {

    }
}
