/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.control;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import com.top_logic.ajax.client.compat.AJAX;
import com.top_logic.ajax.client.compat.AJAXArguments;

/**
 * Base class for client-side representations of controls.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractJSControl implements JSControl, HasHandlers, EventListener {

	/** See: <code>com.top_logic.layout.basic.component.ControlComponent.DispatchAction</code>. */
	private static final String COMMAND_ID_DISPATCH = "dispatchControlCommand";

	private final String _id;

	private HandlerManager _handlerManager;

	/**
	 * Creates a {@link AbstractJSControl}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 */
	public AbstractJSControl(String id) {
		_id = id;
	}

	@Override
	public final String getId() {
		return _id;
	}

	/**
	 * The top-level DOM {@link Element} that is controlled by this {@link JSControl}.
	 */
	public Element controlElement() {
		return getElement(getId());
	}

	@Override
	public void init(Object[] args) {
		DOM.setEventListener(controlElement(), this);
	}

	@Override
	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				// Only fire the mouse over event if it's coming from outside this
				// widget.
			case Event.ONMOUSEOUT:
				// Only fire the mouse out event if it's leaving this
				// widget.
				Element related = event.getRelatedEventTarget().cast();
				if (related != null && controlElement().isOrHasChild(related)) {
					return;
				}
				break;
		}
		DomEvent.fireNativeEvent(event, this, controlElement());
	}

	/**
	 * Sends a command to the server-side of this {@link JSControl}.
	 * 
	 * @param command
	 *        The command name identifying the server-side functionality.
	 * @param arguments
	 *        The arguments object that is transmitted in an AJAX call.
	 */
	protected void sendCommand(String command, AJAXArguments arguments) {
		arguments.setControlID(getId());
		arguments.setControlCommand(command);
		AJAX.execute(COMMAND_ID_DISPATCH, arguments, true);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (_handlerManager != null) {
			_handlerManager.fireEvent(event);
		}
	}

	private HandlerManager handlerManager() {
		return _handlerManager == null ? _handlerManager = createHandlerManager() : _handlerManager;
	}

	private HandlerManager createHandlerManager() {
		return new HandlerManager(this);
	}

	/**
	 * Adds a {@link DragEndHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
		return addBitlessDomHandler(handler, DragEndEvent.getType());
	}

	/**
	 * Adds a {@link DragEnterHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
		return addBitlessDomHandler(handler, DragEnterEvent.getType());
	}

	/**
	 * Adds a {@link DragHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragHandler(DragHandler handler) {
		return addBitlessDomHandler(handler, DragEvent.getType());
	}

	/**
	 * Adds a {@link DragLeaveHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
		return addBitlessDomHandler(handler, DragLeaveEvent.getType());
	}

	/**
	 * Adds a {@link DragOverHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
		return addBitlessDomHandler(handler, DragOverEvent.getType());
	}

	/**
	 * Adds a {@link DragStartHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
		return addBitlessDomHandler(handler, DragStartEvent.getType());
	}

	/**
	 * Adds a {@link DropHandler} that listens for events on {@link #controlElement()}.
	 */
	public HandlerRegistration addDropHandler(DropHandler handler) {
		return addBitlessDomHandler(handler, DropEvent.getType());
	}

	/**
	 * Adds a generic {@link EventHandler} that listens for events on {@link #controlElement()}.
	 */
	public final <H extends EventHandler> HandlerRegistration addBitlessDomHandler(
			final H handler, DomEvent.Type<H> type) {
		assert handler != null : "handler must not be null";
		assert type != null : "type must not be null";
		sinkBitlessEvent(type.getName());
		return handlerManager().addHandler(type, handler);
	}

	private void sinkBitlessEvent(String eventTypeName) {
		DOM.sinkBitlessEvent(controlElement(), eventTypeName);
	}

	/**
	 * Utility method for finding the {@link DivElement} with the given ID in the context document.
	 */
	protected static DivElement getDiv(String id) {
		return DivElement.as(getElement(id));
	}

	/**
	 * Utility method for finding the {@link Element} with the given ID in the context document.
	 */
	protected static Element getElement(String id) {
		return Document.get().getElementById(id);
	}

}
