package com.cathive.convex.ui;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Loads and displays a help-dialog.
 *
 * @author Alexander Erben
 */
public class HelpCtrl implements Initializable {

    @FXML
    private WebView webView;

    /**
     * Load the help dialog, set the given stage as owner and open it.
     *
     * @param owner of this help pane
     */
    static void create(final Stage owner) {
        try {
            final FXMLLoader loader = new FXMLLoader(ModalDialog.class.getClassLoader().getResource("fxml/help.fxml"));
            final ResourceBundle i18n = ResourceBundle.getBundle("bundles.i18n");
            loader.setResources(i18n);
            final Stage stage = new Stage();
            final Scene scene = new Scene(loader.load());
            stage.initOwner(owner);
            stage.setScene(scene);
            stage.show();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final URL resource = this.getClass().getClassLoader().getResource("./help/help.html");
        Objects.requireNonNull(resource);
        this.webView.getEngine().load(resource.toExternalForm());
    }

    /**
     * Browser back
     */
    @FXML
    public void back() {
        final WebHistory history = this.webView.getEngine().getHistory();
        Platform.runLater(() -> history.go(-1));
    }

    /**
     * Browser forward
     */
    @FXML
    public void forward() {
        final WebHistory history = this.webView.getEngine().getHistory();
        Platform.runLater(() -> history.go(1));
    }

    /**
     * Go back to the index page.
     */
    @FXML
    public void index() {
        final URL resource = this.getClass().getClassLoader().getResource("./help/help.html");
        Preconditions.checkState(resource != null, "Help.html not found!");
        this.webView.getEngine().load(resource.toExternalForm());
    }


}
