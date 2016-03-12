package com.wx.fx.transfer;

import com.wx.fx.gui.window.SimpleStageInfo;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageInfo;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.util.callback.SimpleCallback;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.ResourceBundle;

/**
 * Created on 13/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class TransferController implements StageController {

    public static final StageInfo STAGE_INFO = new SimpleStageInfo("/com/wx/fx/transfer/TransferDialog.fxml",
            true,
            -1,
            ResourceBundle.getBundle("fx_text"));
    private static final Color LINE_COLOR = Color.color(0, 0, 0, 0.1);
    private static final double LINE_Y_INSETS = 5;
    private static final double LINE_WIDTH = 3;

    @FXML
    public Button cancelButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;
    @FXML
    private Canvas canvas;

    private TransferTask task;

    public void initialize() {
        canvas.widthProperty().bind(progressBar.widthProperty());
        canvas.heightProperty().bind(progressBar.heightProperty());
    }

    @Override
    public void setArguments(Object... args) {
        execute((TransferTask) args[0], (SimpleCallback) args[1]);
    }

    public void cancel() {
        if (task != null) {
            progressBar.setId("red_bar");
            task.cancel(true);
            cancelButton.setDisable(true);
        }
    }

    @Override
    public void closing() {
        cancel();
    }

    public void execute(TransferTask task, SimpleCallback callback) {


        drawLines(task.getStepsCount());

        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnFinished(() -> {
            this.task = null;
            StageManager.close(STAGE_INFO);
            if (task.isCancelled()) {
                callback.cancelled();
            } else if (task.getException() != null) {
                callback.failure(task.getException());
            } else {
                callback.success();
            }
        });

        this.task = task;
        new Thread(task).start();
    }

    private void drawLines(int count) {
        double height = canvas.getHeight();
        double width = canvas.getWidth();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setStroke(LINE_COLOR);
        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(StrokeLineCap.ROUND);

        for (int i = 1; i < count; i++) {
            double x = i * width / (double) count;
            gc.strokeLine(x, 0 + LINE_Y_INSETS, x, height - LINE_Y_INSETS);
        }
    }
}
