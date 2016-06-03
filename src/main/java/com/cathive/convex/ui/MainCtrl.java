package com.cathive.convex.ui;

import com.cathive.convex.geometry.Point;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Main application controller that binds all major components bound in the view
 * fxml to some custom logic.
 *
 * @author Alexander Erben
 */
public class MainCtrl implements Initializable {

    /*
     * Load the resource bundle
     */
    static {
        try {
            i18n = ResourceBundle.getBundle("bundles.i18n");
        } catch (Exception e) {
            throw new RuntimeException(
                    "The resource bundles could not be found.\n"
                            + "The most likely reason is that the resources folder is not on the build path.\n"
                            + "Please include it in your IDE's preferences!");
        }
    }

    /**
     * Resource bundle that contains internationalized strings
     */
    private static final ResourceBundle i18n;

    /**
     * Builder for the main window of which this class is the controller. Used
     * to bootstrap the application in {@link AppFxml} and to open a new window
     * in {@link #newWindow()}
     *
     * @param stage to draw the scene into
     */
    static void createMainWindow(final Stage stage) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setResources(i18n);
            final InputStream resource = MainCtrl.class.getClassLoader()
                    .getResourceAsStream("fxml/main.fxml");
            Objects.requireNonNull(resource,
                            "\nThe fxml definition of the main view was not found.\n"
                                    + "The most likely reason is that the resources folder is not on the build path.\n"
                                    + "Please include it in your IDE's preferences!");
            final Parent parent = loader.load(resource);
            final Scene scene = new Scene(parent, 1000, 600);
            stage.setTitle(i18n.getString("application.title"));
            stage.setScene(scene);
            stage.show();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The root element of the component tree
     */
    @FXML
    private Parent root;

    /**
     * The main rendering pane where all points and shapes are drawn
     */
    @FXML
    private RenderingPane renderingPane;

    /**
     * Handles edit mode transition, e.g. from move to draw points mode
     */
    @FXML
    private EditModeHandler editModeHandler;

    /**
     * Orchestrates undo and redo functionality
     */
    @FXML
    private UndoRedoHandler undoRedoHandler;

    /**
     * The undo button
     */
    @FXML
    private MenuItem undo;

    /**
     * The redo button
     */
    @FXML
    private MenuItem redo;

    /**
     * The menu item to save the current state, overwriting it to the last file that has been opened with
     * open.
     */
    @FXML
    private MenuItem saveMenuItem;

    /**
     * Rendering settings, mainly used in angle hull rendering.
     */
    @FXML
    private RenderingSettings settings;

    /**
     * File chooser for files containing point sets
     */
    private FileChooser chooser;

    /**
     * The last file reference that has been sourced to load points. Used in the
     * save-method.
     */
    private final ObjectProperty<Optional<File>> lastLoadedFile
            = new SimpleObjectProperty<>(Optional.empty());


    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        this.chooser = new FileChooser();
        this.chooser.setSelectedExtensionFilter(new ExtensionFilter(i18n
                .getString("menu.file.extension"), ".points"));
        this.chooser.setInitialDirectory(Paths.get("../Tester/data").toFile());
        this.undo.disableProperty().bind(
                this.undoRedoHandler.undoAvailableProperty());
        this.redo.disableProperty().bind(
                this.undoRedoHandler.redoAvailableProperty());
        final BooleanProperty fileLoadedProperty = new SimpleBooleanProperty(true);
        this.lastLoadedFile.addListener((observable, oldValue, newValue) ->
                fileLoadedProperty.set(!newValue.isPresent()));
        this.saveMenuItem.disableProperty().bind(fileLoadedProperty);
    }

    /**
     * Open a new window - not replacing this one
     */
    @FXML
    public void newWindow() {
        final Stage stage = new Stage();
        stage.setX(this.root.getLayoutX() + 100);
        createMainWindow(stage);
    }

    /**
     * Load points from a file and replaces all points present on the
     * {@link RenderingPane} with the parsed points.
     */
    @FXML
    public void loadAndReplace() {
        ofNullable(
                this.chooser.showOpenDialog(this.root.getScene().getWindow()))
                .ifPresent(
                        selectedFile -> {
                            try {
                                final List<Point> points = PointsReader
                                        .readPath(selectedFile.toPath());
                                this.renderingPane.clearAndReplace(points);
                                this.lastLoadedFile.set(Optional.of(selectedFile));
                            } catch (final Exception e) {
                                ModalDialog.create((Stage) this.root.getScene()
                                        .getWindow(), i18n
                                        .getString("menu.file.notread"));
                            }
                        });
    }

    /**
     * Load points from a file and add them to the points present on the
     * {@link RenderingPane}.
     */
    @FXML
    public void loadAndAdd() {
        ofNullable(
                this.chooser.showOpenDialog(this.root.getScene().getWindow()))
                .ifPresent(
                        selectedFile -> {
                            try {
                                final List<Point> points = PointsReader
                                        .readPath(selectedFile.toPath());
                                this.renderingPane.addAll(points);
                                this.lastLoadedFile.set(Optional.of(selectedFile));
                            } catch (final Exception e) {
                                ModalDialog.create((Stage) this.root.getScene()
                                        .getWindow(), i18n
                                        .getString("menu.file.notread"));
                            }
                        });
    }

    /**
     * Open a save dialog and serialize all points on the {@link RenderingPane}
     * to the selected file.
     */
    @FXML
    public void saveAs() {
        ofNullable(
                this.chooser.showSaveDialog(this.root.getScene().getWindow()))
                .ifPresent(
                        selectedFile -> {
                            try {
                                PointsWriter.write(selectedFile.toPath(),
                                        this.renderingPane.getPoints());
                            } catch (Exception e) {
                                ModalDialog.create((Stage) this.root.getScene()
                                        .getWindow(), i18n
                                        .getString("menu.file.saveError"));
                            }
                        });
    }

    /**
     * Open a save dialog and serialize all points on the {@link RenderingPane}
     * to the selected file.
     *
     * @param actionEvent source event
     */
    @FXML
    public void save(final ActionEvent actionEvent) {
        if (this.lastLoadedFile.get().isPresent()) {
            try {
                PointsWriter.write(this.lastLoadedFile.get()
                        .orElseGet(() -> {throw new IllegalStateException("Loaded file not set!");})
                        .toPath(),
                        this.renderingPane.getPoints());
            } catch (final Exception e) {
                ModalDialog.create((Stage) this.root.getScene().getWindow(),
                        i18n.getString("menu.file.saveError"));
            }
        } else {
            saveAs();
        }
    }

    /**
     * Transition to {@link EditMode#DELETE}
     */
    @FXML
    public void deleteMode() {
        this.editModeHandler.transition(EditMode.DELETE);
    }

    /**
     * Transition to {@link EditMode#DRAW}
     */
    @FXML
    public void drawMode() {
        this.editModeHandler.transition(EditMode.DRAW);
    }

    /**
     * Transition to {@link EditMode#MOVE}
     */
    @FXML
    public void moveMode() {
        this.editModeHandler.transition(EditMode.MOVE);
    }

    /**
     * Rid the {@link RenderingPane} of all points
     */
    @FXML
    public void deleteAllPoints() {
        this.renderingPane.clearAndReplace(Collections.emptyList());
    }

    /**
     * Add a fixed count of randomly positioned points to the
     * {@link RenderingPane}. The count is determined by the fx-id of the action
     * event source. The id is expected to end with an integral number preceded
     * by a _ character. Example: "random_50". This hack is used to easily pass
     * the desired count without using a custom component mechanism.
     *
     * @param actionEvent source event
     */
    @FXML
    public void addRandomPoints(final ActionEvent actionEvent) {
        final MenuItem source = (MenuItem) actionEvent.getSource();
        final Integer count = Integer.valueOf(source.getId().split("_")[1]);
        this.renderingPane.addAll(randomPoints(this.renderingPane.getWidth(),
                this.renderingPane.getHeight(), count));
    }

    /**
     * Trigger undo of the last action eligible to undo.
     */
    @FXML
    public void undo() {
        this.undoRedoHandler.undo();

    }

    /**
     * Trigger redo of the last action that has been undone
     */
    @FXML
    public void redo() {
        this.undoRedoHandler.redo();
    }

    /**
     * Close the application gracefully. Bye!
     */
    @FXML
    public void exit() {
        ((Stage) this.root.getScene().getWindow()).close();
    }

    /**
     * Display the help dialog
     */
    @FXML
    public void showHelp() {
        HelpCtrl.create((Stage) this.root.getScene().getWindow());
    }

    /**
     * Displays the settings dialog
     */
    @FXML
    public void showSettings() {
        SettingsCrtl.create((Stage) this.root.getScene().getWindow(), this.settings);
    }

    /**
     * Generate some random points. The lowest possible x and y value is 0, the
     * maximum values are passed as parameters. A Gaussian distribution is used
     * for randomization of the coordinates.
     *
     * @param maxX  maximum x value in px
     * @param maxY  maximum y value in px
     * @param count points count
     * @return generated random points
     */
    private List<Point> randomPoints(final double maxX, final double maxY,
                                     final int count) {
        final Random r = new Random();
        return Stream
                .generate(
                        () -> Point.of(
                                nextGaussianInRange(r, maxX * 0.3, maxX * 0.7),
                                nextGaussianInRange(r, maxY * 0.3, maxY * 0.7)
                        )
                ).limit(count).collect(toList());
    }

    /**
     * Generate a random number between min and max.
     * The algorithm uses a gaussian distribution for random number generation.
     * If after 100 retries the random number algorithm does not return a valid number in the given range,
     * 0.5(max+min) is returned.
     *
     * @param max    maximum value that is allowed to be returned.
     * @param min    minimum value that is allowed to be returned
     * @param random random number generator
     * @return random number
     */
    private double nextGaussianInRange(final Random random, final double min, final double max) {
        if (min == max) return min;
        int maxRetries = 100;
        double value = min != Double.MIN_VALUE ? min - 1 : Double.MIN_VALUE; // initial value that is always out of the range, as long as min is not Double#MIN_VALUE
        while (value < min || value > max) {
            if (--maxRetries == 0) return (max + min) / 2;
            value = ((random.nextGaussian() * max / 5)) + (max - min) / 2;
        }
        return value;
    }
}