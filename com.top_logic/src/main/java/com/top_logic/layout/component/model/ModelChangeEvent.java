/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

/**
 * Data sent to {@link ModelChangeListener}s.
 * 
 * @deprecated Use {@link com.top_logic.model.listen.ModelChangeEvent}
 */
@Deprecated
class ModelChangeEvent {

	private final Object _sender;

	private final Object _oldModel;

	private final Object _newModel;

	/**
	 * Creates a {@link ModelChangeEvent}.
	 * 
	 * @param sender
	 *        See {@link ModelChangeListener#modelChanged(Object, Object, Object)}.
	 * @param oldList
	 *        See {@link ModelChangeListener#modelChanged(Object, Object, Object)}.
	 * @param newList
	 *        See {@link ModelChangeListener#modelChanged(Object, Object, Object)}.
	 */
	public ModelChangeEvent(Object sender, Object oldList, Object newList) {
		_sender = sender;
		_oldModel = oldList;
		_newModel = newList;
	}

	/**
	 * @see ModelChangeListener#modelChanged(Object, Object, Object)
	 */
	public Object getSender() {
		return _sender;
	}

	/**
	 * @see ModelChangeListener#modelChanged(Object, Object, Object)
	 */
	public Object getOldModel() {
		return _oldModel;
	}

	/**
	 * @see ModelChangeListener#modelChanged(Object, Object, Object)
	 */
	public Object getNewModel() {
		return _newModel;
	}

}
