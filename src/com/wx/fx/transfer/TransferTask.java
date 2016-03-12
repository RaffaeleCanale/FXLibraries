package com.wx.fx.transfer;

import com.wx.io.AccessorUtil;
import com.wx.io.file.FileUtil;
import com.wx.util.Format;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class TransferTask extends Task<Void> {

    private static final int SOURCE_MAX_CHARS = 20;

    public enum Action {
        COPY(false),
        MOVE(false),
        ZIP(false),
        UNZIP(false),
        MKDIR(true),
        REMOVE(true);

        private final boolean isImmediate;

        Action(boolean isImmediate) {
            this.isImmediate = isImmediate;
        }
    }


    private final List<TaskInfo> tasks;
    private final List<TaskInfo> finallyTasks;
    private final int stepsCount;
    private final ResourceBundle bundle;
    private Runnable onFinished;

    private double progress = 0;
    private double progressRate;
    private double step = 0;




    private TransferTask(List<TaskInfo> tasks, List<TaskInfo> finallyTasks, ResourceBundle bundle) {
        this.tasks = tasks;
        this.finallyTasks = finallyTasks;
        this.bundle = bundle;
        this.stepsCount = (int) Stream.concat(tasks.stream(), finallyTasks.stream())
                .filter(t -> !t.action.isImmediate).count();
    }

    public int getStepsCount() {
        return stepsCount;
    }


    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    protected Void call() throws Exception {
        setDefaultMessage();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return null;
        }
        Exception ex = null;
        try {
            execute(tasks);
        } catch (Exception e) {
            ex = e;
        } finally {
            step = tasks.size();
            progress = step;
            updateProgress(progress, stepsCount);

            try {
                execute(finallyTasks);

                if (ex != null) {
                    throw ex;
                }

            } finally {
                if (onFinished != null) {
                    Platform.runLater(onFinished);
                }
            }
        }

        return null;
    }

    private void execute(List<TaskInfo> tasks) throws IOException {
        for (TaskInfo task : tasks) {
            setDefaultMessage();
            task.initialize();

            setMessage(task);

            progressRate = 1.0 / task.totalSize;

            executeTask(task);

            if (!task.action.isImmediate) {
                step++;
            }
            progress = step;
            updateProgress(progress, stepsCount);
        }
    }

    private void setDefaultMessage() {
        updateMessage(bundle.getString("transfer.preparing"));
    }

    private void setMessage(TaskInfo task) {
        String sources = Stream.of(task.sources).map(File::getName).collect(Collectors.joining(", "));
        if (sources.length() > SOURCE_MAX_CHARS) {
            sources = sources.substring(0, SOURCE_MAX_CHARS) + "...";
        }
        String destination = task.target == null ? "" : " -> " + task.target.getName();

        updateMessage(
                bundle.getString("transfer." + task.action.name().toLowerCase())
                        + "  (" + Format.formatSize(task.totalSize) + ")"
        );
    }


    private void executeTask(TaskInfo task) throws IOException {
        switch (task.action) {
            case ZIP:
                checkCancelled();
                AccessorUtil.zip(task.target, this::incrementProgress, task.sources);
                break;
            case UNZIP:
                for (File zip : task.sources) {
                    checkCancelled();
                    AccessorUtil.unzip(zip, task.target, this::incrementProgress);
                }
                break;
            case MKDIR:
                for (File file : task.sources) {
                    FileUtil.autoCreateDirectory(file);
                }
                break;
            case COPY:
            case MOVE:
                for (File file : task.sources) {
                    checkCancelled();
                    traverse(task.action, file, task.target);
                }
                break;
            case REMOVE:
                for (File file : task.sources) {
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            FileUtil.deleteDir(file);
                        } else {
                            file.delete();
                        }
                    }
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    private void traverse(Action action, File source, File destination) throws IOException {
        if (source.isDirectory()) {
            destination = new File(destination, source.getName());
            FileUtil.autoCreateDirectory(destination);

            for (File file : source.listFiles()) {
                traverse(action, file, destination);
            }
        } else {
            Path from = source.toPath();
            Path to = !source.isDirectory() && destination.isDirectory() ?
                    new File(destination, source.getName()).toPath() :
                    destination.toPath();
            switch (action) {
                case COPY:
                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                    break;
                case MOVE:
                    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
                    break;
                default:
                    throw new AssertionError();
            }

            incrementProgress(source);
        }
    }

    private void incrementProgress(File file) {
        progress += (file.length() * progressRate);
        updateProgress(progress, stepsCount);
    }

    private void checkCancelled() {
        if (isCancelled()) {
            throw new RuntimeException("Cancelled");
        }
    }

    private static class TaskInfo {

        private final Action action;
        private final Supplier<File[]> sourcesSupplier;
        private final Supplier<File> targetSupplier;

        private long totalSize;
        private File[] sources;
        private File target;

        public TaskInfo(Action action, Supplier<File[]> sourcesSupplier, Supplier<File> targetSupplier, long totalSize) {
            this.action = action;
            this.sourcesSupplier = sourcesSupplier;
            this.targetSupplier = targetSupplier;
            this.totalSize = totalSize;
        }

        public void initialize() {
            sources = sourcesSupplier.get();
            target = targetSupplier.get();

            if (totalSize < 0) {
                totalSize = computeTotalSize(sources);
            }
        }

        private long computeTotalSize(File[] files) {
            long size = 0;
            for (File file : files) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        size += computeTotalSize(file.listFiles());
                    } else {
                        size += file.length();
                    }
                }
            }

            return size;
        }

        @Override
        public String toString() {
            return action + " " + Arrays.toString(sources) + " " + target;
        }
    }

    public static class Builder {

        private final List<TaskInfo> tasks = new LinkedList<>();
        private final List<TaskInfo> finallyTasks = new LinkedList<>();
        private ResourceBundle resources;


        public Builder setResources(ResourceBundle resources) {
            this.resources = resources;

            return this;
        }

        public Builder action(Action action, File source, File target) {
            return action(action, () -> new File[]{source}, () -> target, -1);
        }

        public Builder action(Action action, File[] sources, File target) {
            return action(action, () -> sources, () -> target, -1);
        }

        public Builder action(Action action, Supplier<File[]> sourcesSupplier, File target) {
            return action(action, sourcesSupplier, () -> target, -1);
        }

        public Builder action(Action action, Supplier<File[]> sourcesSupplier, Supplier<File> targetSupplier) {
            return action(action, sourcesSupplier, targetSupplier, -1);
        }

        public Builder action(Action action, Supplier<File[]> sourcesSupplier, Supplier<File> targetSupplier, long totalSize) {
            this.tasks.add(new TaskInfo(action, sourcesSupplier, targetSupplier, totalSize));

            return this;
        }

        public Builder finallyAction(Action action, File source, File target) {
            return finallyAction(action, () -> new File[]{source}, () -> target, -1);
        }

        public Builder finallyAction(Action action, File[] sources, File target) {
            return finallyAction(action, () -> sources, () -> target, -1);
        }

        public Builder finallyAction(Action action, Supplier<File[]> sourcesSupplier, File target) {
            return finallyAction(action, sourcesSupplier, () -> target, -1);
        }

        public Builder finallyAction(Action action, Supplier<File[]> sourcesSupplier, Supplier<File> targetSupplier) {
            return finallyAction(action, sourcesSupplier, targetSupplier, -1);
        }

        public Builder finallyAction(Action action, Supplier<File[]> sourcesSupplier, Supplier<File> targetSupplier, long totalSize) {
            this.finallyTasks.add(new TaskInfo(action, sourcesSupplier, targetSupplier, totalSize));

            return this;
        }

        public TransferTask build() {
            if (resources == null) {
                resources = ResourceBundle.getBundle("fx_text");
            }

            return new TransferTask(tasks, finallyTasks, resources);
        }

    }

}
