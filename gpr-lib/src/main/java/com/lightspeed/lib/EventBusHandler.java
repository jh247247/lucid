package com.lightspeed.gpr.lib;

import com.google.common.eventbus.EventBus;

/**
 * This literally only handles the eventbus for the model-view
 * communication. Holds a singleton to an eventbus that any other
 * object can obtain/subscribe to.
 */

public class EventBusHandler {
    static final EventBus m_bus = new EventBus();;

    /**
     * Get the eventbus. Simple.
     */
    static public EventBus getEventBus() {
	return m_bus;
    }
}
