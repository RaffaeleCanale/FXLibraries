package com.wx.fx.gui.window;

import com.wx.properties.PropertiesManager;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * Interface that describes a stage controller that can be loaded with the {@link StageManager}.
 * <p>
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public interface StageController {

    /**
     * This method is called once at the stage's initialization. It provides a reference to the built stage as well as
     * the corresponding {@link ResourceBundle}. Note that the bundle might be {@code null} if the {@link StageInfo} did
     * not specify any bundle.
     *
     * @param stage  Reference to the stage corresponding to this controller
     * @param bundle Corresponding language bundle if any
     */
    default void setContext(Stage stage, PropertiesManager bundle) {
    }

    /**
     * This method is called every time this stage is activated. It receives the arguments passed along by the caller.
     *
     * @param args Optional arguments for this stage
     */
    default void setArguments(Object... args) {
    }

    /**
     * This method is called when this stage is closed
     */
    default void closing() {
    }

}
