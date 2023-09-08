/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.layout.SingleSelectionModel;

/**
 * Event to dispatch to {@link SingleSelectionListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingleSelectionEvent {

	private final SingleSelectionModel _sender;

	private final Object _formerlySelectedObject;

	private final Object _newlySelectedObject;

	/**
	 * Creates a {@link SingleSelectionEvent}.
	 * 
	 * @param sender
	 *        See {@link #getSender()}.
	 * @param formerlySelectedObject
	 *        See {@link #getFormerlySelectedObject()}.
	 * @param newlySelectedObject
	 *        See {@link #getNewlySelectedObject()}.
	 */
	public SingleSelectionEvent(SingleSelectionModel sender, Object formerlySelectedObject, Object newlySelectedObject) {
		_sender = sender;
		_formerlySelectedObject = formerlySelectedObject;
		_newlySelectedObject = newlySelectedObject;
	}

	/**
	 * @see SingleSelectionListener#notifySelectionChanged(SingleSelectionModel, Object, Object)
	 */
	public SingleSelectionModel getSender() {
		return _sender;
	}

	/**
	 * @see SingleSelectionListener#notifySelectionChanged(SingleSelectionModel, Object, Object)
	 */
	public Object getFormerlySelectedObject() {
		return _formerlySelectedObject;
	}

	/**
	 * @see SingleSelectionListener#notifySelectionChanged(SingleSelectionModel, Object, Object)
	 */
	public Object getNewlySelectedObject() {
		return _newlySelectedObject;
	}

}
