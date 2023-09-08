/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * An {@link EventListener} that dispatches based on the event type.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class DispatchingEventListener implements EventListener {

	private final Map<String, EventListener> _listeners = new HashMap<>();

	/**
	 * Convenience variant of {@link #DispatchingEventListener(Map, EventListener)} where the
	 * fallback {@link EventListener} does nothing.
	 */
	public DispatchingEventListener(Map<String, ? extends EventListener> listener) {
		this(listener, null);
	}

	/**
	 * Creates a {@link DispatchingEventListener}.
	 * 
	 * @param listener
	 *        The {@link EventListener}s to call, indexed by the event type they are responsible
	 *        for. If GWT doesn't know about an event, its name (as specified for HTML) is still a
	 *        legal value and it will work.
	 * @param fallback
	 *        Is called for events without a listener. That means, it is called for every event for
	 *        whose type no {@link EventListener} is registered in the given {@link Map}. If null,
	 *        the fallback is to do nothing.
	 */
	public DispatchingEventListener(Map<String, ? extends EventListener> listener, EventListener fallback) {
		_listeners.putAll(listener != null ? listener : Collections.<String, EventListener> emptyMap());
	}

	@Override
	public void onBrowserEvent(Event event) {
		EventListener listener = getListener(event.getType());

		if (listener != null) {
			listener.onBrowserEvent(event);
		}
	}

	private EventListener getListener(String eventName) {
		EventListener listener = _listeners.get(eventName);

		if (listener != null) {
			return listener;
		}

		return null;
	}

}
