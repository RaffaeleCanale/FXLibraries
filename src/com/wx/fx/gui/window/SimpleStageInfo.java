package com.wx.fx.gui.window;

/**
 * Basic implementation of a {@link StageInfo} that acts as a simple container for the various constants.
 * <p>
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class SimpleStageInfo implements StageInfo {

    private final String location;
    private final boolean isModal;
    private final int groupNumber;
    private final String bundle;

    /**
     * Build a simple {@link StageInfo} container.
     *
     * @param location    Location of the stage's {@code FXML} file
     * @param isModal     Defines if the stage is modal or not
     * @param groupNumber Defines the stage's group
     * @param bundle      Name of the {@link java.util.ResourceBundle} to load with this stage
     */
    public SimpleStageInfo(String location, boolean isModal, int groupNumber, String bundle) {
        this.location = location;
        this.isModal = isModal;
        this.groupNumber = groupNumber;
        this.bundle = bundle;
    }

    @Override
    public boolean isModal() {
        return isModal;
    }

    @Override
    public String location() {
        return location;
    }

    @Override
    public String getBundleBase() {
        return bundle;
    }

    @Override
    public int stageGroup() {
        return groupNumber;
    }
}
