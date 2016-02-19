package com.wx.fx.util.alert;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

/**
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ErrorAlert {

    public static void showGenericErrorAlert(Throwable ex) {
        showGenericErrorAlert(ex, "");
    }

    public static void showGenericErrorAlert(Throwable ex, String content) {
        ResourceBundle bundle = ResourceBundle.getBundle("fx_text");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("error_alert.title"));
        alert.setHeaderText(bundle.getString("error_alert.header"));
        alert.setContentText(content);

        Label exceptionLabel = new Label("[" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
        exceptionLabel.setId("error");

        alert.getDialogPane().setExpandableContent(exceptionLabel);

        ex.printStackTrace();
        alert.showAndWait();
    }
    
}
