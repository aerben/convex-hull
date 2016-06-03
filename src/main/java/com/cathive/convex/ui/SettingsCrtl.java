package com.cathive.convex.ui;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the settings dialog accessible from the menu bar
 *
 * @author Alexander Erben
 */
public class SettingsCrtl {

    /**
     * The root pane
     */
    @FXML
    GridPane gridPane;

    /**
     * Load the settings dialog, set the given stage as owner and open it.
     *
     * @param owner    of this help pane
     * @param settings rendering specific settings
     */
    static void create(final Stage owner, final RenderingSettings settings) {
        try {
            final FXMLLoader loader = new FXMLLoader(ModalDialog.class.getClassLoader().getResource("fxml/settings.fxml"));
            final ResourceBundle i18n = ResourceBundle.getBundle("bundles.i18n");
            loader.setResources(i18n);
            final Stage stage = new Stage();
            final Scene scene = new Scene(loader.load());
            stage.initOwner(owner);
            stage.setScene(scene);
            stage.setTitle(i18n.getString("settings.title"));
            stage.show();
            loader.<SettingsCrtl>getController().init(settings, i18n);
            stage.sizeToScene();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the controller and the UI
     *
     * @param settings to apply
     * @param i18n     resources bundle
     */
    public void init(RenderingSettings settings, ResourceBundle i18n) {
        this.gridPane.add(new Label(i18n.getString("settings.precisionmode")), 0, 0);
        this.gridPane.add(precisionRbPane(settings, i18n), 1, 0);
        this.gridPane.add(new Label(i18n.getString("settings.precisionmode.preciseangles")), 0, 1);
        this.gridPane.add(anglesBox(settings.getDrawnAnglesPrecise()), 1, 1);
        this.gridPane.add(new Label(i18n.getString("settings.precisionmode.impreciseangles")), 0, 2);
        this.gridPane.add(anglesBox(settings.getDrawnAnglesImprecise()), 1, 2);
    }

    /**
     * Sets up the checkbox group that allows angle selection
     *
     * @param map angles and their enabled properties
     * @return the box
     */
    private HBox anglesBox(ObservableMap<Integer, BooleanProperty> map) {
        final HBox hBox = new HBox();
        for (Map.Entry<Integer, BooleanProperty> allowedAngle : map.entrySet()) {
            final CheckBox cb = new CheckBox(String.valueOf(allowedAngle.getKey()));
            cb.selectedProperty().bindBidirectional(allowedAngle.getValue());
            hBox.getChildren().add(cb);
        }
        hBox.setSpacing(5);
        return hBox;
    }

    /**
     * Sets up the radio button group that allows to configure the precision setting
     *
     * @param settings rendering settings to bind to
     * @param i18n     resources bundle
     * @return box
     */
    private HBox precisionRbPane(RenderingSettings settings, ResourceBundle i18n) {
        final ToggleGroup tg = new ToggleGroup();
        final RadioButton preciseRb = new RadioButton(i18n.getString("settings.precisionmode.precise"));
        preciseRb.setToggleGroup(tg);
        preciseRb.setSelected(true);
        preciseRb.selectedProperty().bindBidirectional(settings.preciseModeActiveProperty());
        final RadioButton unpreciseRb = new RadioButton(i18n.getString("settings.precisionmode.imprecise"));
        unpreciseRb.setSelected(true);
        unpreciseRb.setToggleGroup(tg);
        final HBox precisionRbGroup = new HBox(preciseRb, unpreciseRb);
        precisionRbGroup.setSpacing(5);
        return precisionRbGroup;
    }
}

