/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.layout.SingleSelectionModel;

/**
 * No {@link NoSelectionModel} which is also a {@link SingleSelectionModel}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoSingleSelectionModel extends NoSelectionModel implements SingleSelectionModel {

	/** Singleton {@link NoSingleSelectionModel} instance. */
	public static final NoSingleSelectionModel SINGLE_SELECTION_INSTANCE = new NoSingleSelectionModel();

	/**
	 * Creates a new {@link NoSingleSelectionModel}.
	 * 
	 */
	protected NoSingleSelectionModel() {
		// nothing to set here
	}

	@Override
	public boolean addSingleSelectionListener(SingleSelectionListener listener) {
		// Immutable, no updates, no listener notification required.
		return true;
	}

	@Override
	public Object getSingleSelection() {
		return null;
	}

	@Override
	public boolean removeSingleSelectionListener(SingleSelectionListener listener) {
		// Immutable, no updates, no listener notification required.
		return true;
	}

	@Override
	public void setSingleSelection(Object obj) {
		// Immutable.
	}

}

