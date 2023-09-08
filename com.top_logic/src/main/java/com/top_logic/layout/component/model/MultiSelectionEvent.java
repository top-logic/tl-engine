/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Set;

import com.top_logic.mig.html.SelectionModel;

/**
 * Event being dispatched to {@link SelectionListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiSelectionEvent {

	private final SelectionModel _sender;

	private final Set<?> _formerlySelectedObjects;

	private final Set<?> _newlySelectedObjects;

	/**
	 * Creates a {@link MultiSelectionEvent}.
	 * 
	 * @param sender
	 *        See {@link #getSender()}.
	 * @param formerlySelectedObjects
	 *        See {@link #getFormerlySelectedObjects()}.
	 * @param newlySelectedObjects
	 *        See {@link #getNewlySelectedObjects()}.
	 */
	public MultiSelectionEvent(SelectionModel sender, Set<?> formerlySelectedObjects,
			Set<?> newlySelectedObjects) {
		_sender = sender;
		_formerlySelectedObjects = formerlySelectedObjects;
		_newlySelectedObjects = newlySelectedObjects;
	}

	/**
	 * @see SelectionListener#notifySelectionChanged(SelectionModel, Set, Set)
	 */
	public SelectionModel getSender() {
		return _sender;
	}

	/**
	 * @see SelectionListener#notifySelectionChanged(SelectionModel, Set, Set)
	 */
	public Set<?> getFormerlySelectedObjects() {
		return _formerlySelectedObjects;
	}

	/**
	 * @see SelectionListener#notifySelectionChanged(SelectionModel, Set, Set)
	 */
	public Set<?> getNewlySelectedObjects() {
		return _newlySelectedObjects;
	}

}
