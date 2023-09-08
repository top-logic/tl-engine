/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link NodeSelectionVetoListener} can be added to {@link TreeControl} to
 * provide them from changing the selection state of some node.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface NodeSelectionVetoListener extends VetoListener {

	/**
	 * This method computes whether this listener has a veto for changing the
	 * selection of some node.
	 * 
	 * @param selectionModel
	 *        the {@link SelectionModel} which is used to change the selection
	 *        state of the given <code>changedSelection</code>
	 * @param changedSelection
	 *        the node whose selection state is about to change
	 * @param selected
	 *        whether the <code>changedSelection</code> will be selected or not
	 * @throws VetoException
	 *         iff this listener does not allow changing the selection state of
	 *         the given node.
	 */
	public void checkVeto(SelectionModel selectionModel, Object changedSelection, boolean selected) throws VetoException;

}
