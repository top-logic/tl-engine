/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Set;

/**
 * {@link AbstractRestrainedSelectionModel} for multi selection having a lead selection, i.e. the
 * most last selected element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMultiSelectionModel<T> extends AbstractRestrainedSelectionModel<T> {

	private static final Object NO_LEAD = new Object();

	private Object _lastSelected = NO_LEAD;

	/**
	 * Creates a {@link AbstractMultiSelectionModel}.
	 */
	public AbstractMultiSelectionModel(SelectionModelOwner owner) {
		super(owner);
	}

	/**
	 * Same as {@link #setSelected(Object, boolean)}, but without specifying a lead object.
	 */
	@Override
	public final void setSelection(Set<? extends T> newSelection) {
		setSelection(newSelection, NO_LEAD);
	}

	/**
	 * Sets the new overall selection, whereby lead object specifies the selection item, that shall
	 * mark the most important part (e.g. scrolling to according table row).
	 */
	public abstract void setSelection(Set<? extends T> newSelection, Object leadObject);

	/**
	 * Object, that has been (de-)selected at last time of modification of this selection model.
	 * 
	 * <p>
	 * Returns <code>null</code> if the selection has no lead object.
	 * </p>
	 */
	public Object getLastSelected() {
		if (_lastSelected == NO_LEAD) {
			return null;
		}

		return _lastSelected;
	}

	/**
	 * Installs {@link #getLastSelected()}.
	 */
	protected void setLastSelected(Object lastSelected) {
		_lastSelected = lastSelected;
	}

}
