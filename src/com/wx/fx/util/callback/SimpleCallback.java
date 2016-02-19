package com.wx.fx.util.callback;

/**
 * Callback interface for any process that may succeed, fail or be cancelled by the user.
 * <p>
 * Created on 11/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface SimpleCallback {

    /**
     * Callback notifying a success of the procedure.
     *
     * @param result Result variables
     */
    void success(Object... result);

    /**
     * Callback notifying a failure of the procedure.
     *
     * @param ex Exception that caused the failure
     */
    default void failure(Throwable ex) {
    }

    /**
     * Callback notifying that the procedure has been cancelled by the user
     */
    default void cancelled() {
    }

}
