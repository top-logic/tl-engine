/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.util.Utils;

/**
 * Wrapper class containing informations of an model event in a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ComponentEvent implements Action {

	private final int _eventType;

	private final Object _sender;

	private final Object _model;

	private final LayoutComponent _eventSrc;

	/**
	 * Returns a new {@link ComponentEvent}.
	 * 
	 * @param sender
	 *        Must not be <code>null</code>.
	 * @param eventSrc
	 *        Must not be <code>null</code>.
	 */
	public ComponentEvent(LayoutComponent eventSrc, Object model, Object sender, int eventType) {
		if (sender == null) {
			throw new NullPointerException("'sender' must not be 'null'.");
		}
		if (eventSrc == null) {
			throw new NullPointerException("'eventSrc' must not be 'null'.");
		}
		_eventSrc = eventSrc;
		_model = model;
		_sender = sender;
		_eventType = eventType;
	}

	/**
	 * Dispatches this event to the components
	 */
	@Override
	public void execute() {
		switch (_eventType) {
			case ModelEventListener.MODEL_CREATED:
			case ModelEventListener.MODEL_MODIFIED:
			case ModelEventListener.MODEL_DELETED:
				_eventSrc.fireModelEvent(_model, _sender, _eventType);
				break;
			default:
				_eventSrc.internalFireModelEvent(_model, _sender, _eventType);
		}
	}

	@Override
	public String toString() {
		Object eventName = toStringType();

		StringBuilder toStringBuilder = new StringBuilder(getClass().getName());
		toStringBuilder.append('[');
		toStringBuilder.append("type: ").append(eventName).append(',');
		toStringBuilder.append(" model: ").append(_model).append(',');
		if (_sender == _eventSrc) {
			toStringBuilder.append(" sender:eventSource: ").append(_sender);
		} else {
			toStringBuilder.append(" sender: ").append(_sender).append(',');
			toStringBuilder.append(" eventSource: ").append(_eventSrc);
		}
		toStringBuilder.append(']');
		return toStringBuilder.toString();
	}

	private Object toStringType() {
		return toStringType(_eventType);
	}

	/**
	 * Translates an event type like {@link ModelEventListener#MODEL_MODIFIED} to a readable string
	 * name.
	 */
	public static String toStringType(int type) {
		switch (type) {
			case ModelEventListener.DIALOG_CLOSED:
				return "DIALOG_CLOSED";
			case ModelEventListener.DIALOG_OPENED:
				return "DIALOG_OPENED";
			case ModelEventListener.GLOBAL_REFRESH:
				return "GLOBAL_REFRESH";
			case ModelEventListener.MODEL_CREATED:
				return "MODEL_CREATED";
			case ModelEventListener.MODEL_DELETED:
				return "MODEL_DELETED";
			case ModelEventListener.MODEL_MODIFIED:
				return "MODEL_MODIFIED";
			case ModelEventListener.SECURITY_CHANGED:
				return "SECURITY_CHANGED";
			case ModelEventListener.WINDOW_CLOSED:
				return "WINDOW_CLOSED";
			case ModelEventListener.WINDOW_OPENED:
				return "WINDOW_OPENED";
			default:
				return "unknown(" + type + ")";
		}
	}

	/**
	 * Determines whether the given {@link Action} is an updated version of this event, i.e. whether
	 * it is of the same type, send by the same component, but has a different model.
	 * 
	 * @param potentialUpdate
	 *        The event to check. Must not be <code>null</code>
	 */
	@Override
	public boolean isUpdate(Action potentialUpdate) {
		if (potentialUpdate == null) {
			return false;
		}
		if (!(potentialUpdate instanceof ComponentEvent)) {
			return false;
		}
		ComponentEvent potentialUpdateEvent = (ComponentEvent) potentialUpdate;
		if (potentialUpdateEvent._eventType != _eventType) {
			return false;
		}
		if (!potentialUpdateEvent._sender.equals(_sender)) {
			return false;
		}
		if (!potentialUpdateEvent._eventSrc.equals(_eventSrc)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ComponentEvent)) {
			return false;
		}

		ComponentEvent other = (ComponentEvent) obj;
		if (other._eventType != _eventType) {
			return false;
		}
		if (!Utils.equals(other._model, _model)) {
			return false;
		}
		if (!other._sender.equals(_sender)) {
			return false;
		}
		if (!other._eventSrc.equals(_eventSrc)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = _eventType * 13;
		if (_model == null) {
			hash += 19;
		} else {
			hash += 19 * _model.hashCode();
		}
		hash += 17 * _sender.hashCode();
		hash += 17 * _eventSrc.hashCode();
		return hash;
	}

}
