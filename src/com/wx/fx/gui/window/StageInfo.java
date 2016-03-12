package com.wx.fx.gui.window;

import com.wx.fx.Lang;

import java.util.ResourceBundle;

/**
 * This interface provides all the basic information needed to build a specific a Stage (window).
 * <p>
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public interface StageInfo {

    /**
     * Defines if this stage is modal or not. For more information on modality, see {@link javafx.stage.Modality}
     *
     * @return {@code true} if this stage is modal
     */
    boolean isModal();

    /**
     * Defines the location of the corresponding {@code FXML} file.
     * <p>
     * e.g: /com/wx/fx/transfer/TransferDialog.fxml
     *
     * @return The location of this stage {@code FXML} file.
     */
    String location();

    /**
     * Defines the name of the {@link java.util.ResourceBundle} to be loaded with this stage (or {@code null} if no
     * bundle should be associated).
     *
     * @return The name of this stage's bundle or {@code null} if no bundle should be used
     */
    default ResourceBundle getBundleBase() {
        return Lang.getBundle();
    }

    /**
     * Defines this stage group that defines this stage behaviour in relation with other stages. For more information on
     * stage groups, see {@link StageManager}.
     *
     * @return This stage's group
     */
    default int stageGroup() {
        return -1;
    }

}
