package com.cathive.convex.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A simple reusable modal dialog component controller.
 *
 * @author Alexander Erben
 */
public class ModalDialog {

    @FXML
    private Text text;

    @FXML
    private Button closeButton;

    /**
     * Create a new modal dialog with the given text and parent window.
     *
     * @param text  to display
     * @param owner owner window
     */
    static void create(final Stage owner, final String text) {
        try {
            final FXMLLoader loader = new FXMLLoader(ModalDialog.class.getClassLoader().getResource("fxml/dialog.fxml"));
            final Stage stage = new Stage();
            final Scene scene = new Scene(loader.load());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setScene(scene);
            loader.<ModalDialog>getController().applyText(text);
            stage.show();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the text in this dialog
     *
     * @param text to set
     */
    private void applyText(final String text) {
        this.text.setText(text);
    }

    /**
     * Close the dialog
     */
    @FXML
    public void close() {
        ((Stage) this.closeButton.getScene().getWindow()).close();
    }
}
