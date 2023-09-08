/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.selection;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;


/**
 * Interface for checking a veto on changing a selection in a
 * {@link SingleSelectionModel}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface SingleSelectVetoListener extends VetoListener {

	/**
	 * Check, if this listener has a veto, when changing the selection of a
	 * {@link SingleSelectionModel}.
	 * 
	 * @param singleSelectionModel
	 *        The selection model whose selection shall be changed.
	 * @param newSelection
	 *        The new selected object.
	 * @param programmaticUpdate
	 *        whether the change of the selection is triggered by the
	 *        application or by the user
	 * @throws VetoException
	 *         iff this listener stops setting the new Selection
	 */
	public void checkVeto(SingleSelectionModel singleSelectionModel, Object newSelection, boolean programmaticUpdate) throws VetoException;
}
