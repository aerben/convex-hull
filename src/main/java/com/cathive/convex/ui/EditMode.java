package com.cathive.convex.ui;

/**
 * Marks the edit states in which the application may be. These states are used to notify all components
 * of a necessary change in behaviour once the state changes, e.g. making points drawable or the pane clickable.
 * @author Alexander Erben
 */
enum EditMode {
    MOVE, DRAW, NONE, DELETE
}