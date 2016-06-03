package com.cathive.convex.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point
 * @author Alexander Erben
 */
public class AppFxml extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        MainCtrl.createMainWindow(stage);
    }
}