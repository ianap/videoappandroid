package com.androidvideoconverter.app.events;

import com.squareup.otto.Bus;

/**
 * Singelton for event bus object
 */
public class EventBusProvider {

    private static final Bus mBus = new Bus();

    public static Bus getEventBus() { return mBus; }

}
