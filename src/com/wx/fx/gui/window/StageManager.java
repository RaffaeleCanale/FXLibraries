package com.wx.fx.gui.window;

import com.wx.fx.util.BundleWrapper;
import com.wx.properties.PropertiesManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * This class allows to manage the stages of the application. It facilitates the loading of the stage and allows to pass
 * arguments to shown stages.
 * <p>
 * <h3>Usage</h3> To make use of this manager, for a given stage: <ul> <li>Ensure that the stage's controller class
 * implements {@link StageController}</li> <li>Create a {@link StageInfo} instance describing the stage</li> <li>Show or
 * close the stage simply by passing the {@code StageInfo} to {@link #show(StageInfo, Object...)} or {@link
 * #close(StageInfo)}</li> </ul> Notice that <b>a {@code StageInfo} represents a unique stage</b>. If a stage should be
 * displayed in several instances, then it should be represented by several {@code StageInfo}.
 * <p>
 * <h3>Stage groups</h3> Every stage has a group number. Then, the manager ensures a basic rule: <b>every group can only
 * have one visible stage at any time</b>. For instance, if stage A is visible and stage B (of the same group) is set
 * visible, then, stage A is hidden (not closed) and B is shown instead. If B is closed, then A is shown again.
 * <p>
 * If stages have different group numbers, they won't interfere between them.
 * <p>
 * Also, if a stage has a <b>negative group number</b>, it won't belong to any group (and thus, won't affect or be
 * affected by any other stage).
 * <p>
 * <p>
 * Created on 08/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class StageManager {

    private static final Map<Integer, LinkedList<Window>> groups = new HashMap<>();

    private static String styleSheet = StageManager.class.getResource("/defaultStyle.css").toExternalForm();
    private static Image appIcon;

    /**
     * Get the current global stylesheet.
     *
     * @return Current stylesheet.
     */
    public static String getStyleSheet() {
        return styleSheet;
    }

    /**
     * Set a global stylesheet that will be attached to any loaded stage.
     *
     * @param styleSheet Stylesheet to apply to stages
     */
    public static void setStyleSheet(String styleSheet) {
        StageManager.styleSheet = styleSheet;
    }

    /**
     * Set the App icon. This icon will be appear on all stage window.
     *
     * @param appIcon Icon to set to all stages
     */
    public static void setAppIcon(Image appIcon) {
        StageManager.appIcon = appIcon;
    }

//    /**
//     * Initialize a {@code FXMLLoader}.
//     *
//     * @param location Location of the {@code FXML} file
//     * @param bundle   Optional {@code ResourceBundle} to attach
//     *
//     * @return A {@code FXMLLoader} for the given file
//     */
//    public static FXMLLoader getLoader(String location, ResourceBundle bundle) {
//        FXMLLoader loader = new FXMLLoader();
//        loader.setLocation(StageManager.class.getResource(location));
//        if (bundle != null) {
//            loader.setResources(bundle);
//        }
//
//        return loader;
//    }

    /**
     * Close all stages.
     */
    public static void closeAll() {
        groups.values().stream().flatMap(List::stream).forEach(w -> w.stage.close());
        groups.clear();
    }

    /**
     * Close the given stage.
     *
     * @param stageInfo Stage to close
     */
    public static void close(StageInfo stageInfo) {
        int groupIndex = stageInfo.stageGroup();
        LinkedList<Window> group = group(groupIndex);

        Window stage = findWindow(stageInfo, group);
        closeAndRemove(groupIndex, group, stage);
    }


//    public static void hide(StageInfo stageInfo) {
//        Wrapper stage = getWindo(stageInfo);
//        if (stage == null) {
//            throw new IllegalArgumentException();
//        }
//
//        stage.stage.closeAndRemove();
//    }

    /**
     * Show a stage.
     *
     * @param stage Stage to show
     * @param args  Arguments to pass the stage's controller
     */
    public static void show(StageInfo stage, Object... args) {
        show(args, stage, false);
    }

    /**
     * Show a stage and wait until the it is closed.
     *
     * @param stage Stage to show
     * @param args  Arguments to pass the stage's controller
     */
    public static void showAndWait(StageInfo stage, Object... args) {
        show(args, stage, true);
    }

    private static void show(Object[] args, StageInfo stageInfo, boolean wait) {
        int groupIndex = stageInfo.stageGroup();
        LinkedList<Window> group = group(groupIndex);

        Window existingStage = findWindow(stageInfo, group);

        if (groupIndex >= 0 && !group.isEmpty()) {
            // Hide any other stage of the same group
            group.stream()
                    .filter(s -> !s.equals(existingStage))
                    .forEach(w -> w.stage.close());
        }

        if (existingStage != null) {  // This stage is already loaded, just re-activate it
            existingStage.stage.show();
            existingStage.stage.toFront();
            if (args != null && args.length > 0) {
                existingStage.controller.setArguments(args);
            }

        } else {
            try {
                // Load the stage
                FXMLLoader loader = new FXMLLoader(
                        StageManager.class.getResource(stageInfo.location()),
                        stageInfo.getBundleBase()
                        );

                Stage stage = loader.load();
                StageController controller = loader.getController();
                Window window = new Window(stageInfo, stage, controller);
                group.add(window);

                if (styleSheet != null) {
                    stage.getScene().getStylesheets().add(styleSheet);
                }
                if (appIcon != null) {
                    stage.getIcons().add(appIcon);
                }

                if (stageInfo.isModal()) {
                    stage.initModality(Modality.APPLICATION_MODAL);
                }

                stage.setOnHiding(e -> controller.closing());
                stage.setOnCloseRequest(e -> closeAndRemove(groupIndex, group, window));

                controller.setContext(stage);


                stage.show();

                if (args.length > 0) {
                    controller.setArguments(args);
                }

                if (wait) {
                    stage.showAndWait();
                }

            } catch (IOException e) {
                throw new RuntimeException("Internal error, failed to load the stage " + stageInfo, e);
            }
        }
    }

    private static void closeAndRemove(int groupIndex, LinkedList<Window> group, Window stage) {
        stage.stage.close();
        group.remove(stage);
        if (groupIndex >= 0 && !group.isEmpty()) {
            group.getLast().stage.show();
        }
    }

    private static Window findWindow(StageInfo info, List<Window> group) {
        return group.stream()
                .filter(w -> w.info.equals(info))
                .findAny().orElse(null);
    }

    private static LinkedList<Window> group(int number) {
        LinkedList<Window> group = groups.get(number);
        if (group == null) {
            group = new LinkedList<>();
            groups.put(number, group);
        }

        return group;
    }

    private static class Window {
        private final StageInfo info;
        private final Stage stage;
        private final StageController controller;

        private Window(StageInfo info, Stage stage, StageController controller) {
            this.info = info;
            this.stage = stage;
            this.controller = controller;
        }
    }

}
