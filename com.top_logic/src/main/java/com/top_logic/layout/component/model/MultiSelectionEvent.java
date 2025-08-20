/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.mig.html.SelectionModel;

/**
 * Event being dispatched to {@link SelectionListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiSelectionEvent<T> implements SelectionEvent<T> {

	private final SelectionModel<T> _sender;

	private final Set<? extends T> _oldSelection;

	private final Set<? extends T> _newSelection;

	private Set<?> _updated;

	/**
	 * Creates a {@link MultiSelectionEvent}.
	 * 
	 * @param sender
	 *        See {@link #getSender()}.
	 * @param oldSelection
	 *        See {@link #getOldSelection()}.
	 * @param newSelection
	 *        See {@link #getNewSelection()}.
	 */
	public MultiSelectionEvent(SelectionModel<T> sender, Set<? extends T> oldSelection, Set<? extends T> newSelection) {
		_sender = sender;
		_oldSelection = oldSelection;
		_newSelection = newSelection;
	}

	@Override
	public SelectionModel<T> getSender() {
		return _sender;
	}

	@Override
	public Set<? extends T> getOldSelection() {
		return _oldSelection;
	}

	@Override
	public Set<? extends T> getNewSelection() {
		return _newSelection;
	}

	@Override
	public Set<?> getUpdatedObjects() {
		if (_updated == null) {
			_updated = CollectionUtil.symmetricDifference(_oldSelection, _newSelection);
		}
		return _updated;
	}

}
