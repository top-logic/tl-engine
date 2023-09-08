/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;


/**
 * Factory class for {@link SelectionModel}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SelectionModelFactory {

	/**
	 * Creates a new {@link SelectionModel} for the given {@link SelectionModelOwner}.
	 * 
	 * @param owner
	 *        The owner of the new {@link SelectionModel}.
	 */
	public abstract SelectionModel newSelectionModel(SelectionModelOwner owner);

	/**
	 * Creates a new {@link SelectionModel} without owner.
	 * 
	 * @see SelectionModelFactory#newSelectionModel(SelectionModelOwner)
	 * @see SelectionModelOwner#NO_OWNER
	 */
	public final SelectionModel newSelectionModel() {
		return newSelectionModel(SelectionModelOwner.NO_OWNER);
	}

}

