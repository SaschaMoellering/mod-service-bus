package com.sm.vertx.mods.exception;

/**
 * Created by sascha.moellering on 05/05/2015.
 */
public class EventBusException extends Exception {

    public EventBusException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public EventBusException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public EventBusException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public EventBusException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected EventBusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
