package com.wx.fx.util.callback;

/**
 * This is a {@link SimpleCallback} that makes no distinction between success, failure or cancelling. It only describes
 * one callback, called when the procedure is finished (independently of how it finished).
 * <p>
 * Created on 16/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@FunctionalInterface
public interface LazyCallback extends SimpleCallback {

    /**
     * Callback notifying the end of the procedure.
     */
    void finished();

    @Override
    default void success(Object... result) {
        finished();
    }

    @Override
    default void cancelled() {
        finished();
    }

    @Override
    default void failure(Throwable ex) {
        finished();
    }
}
