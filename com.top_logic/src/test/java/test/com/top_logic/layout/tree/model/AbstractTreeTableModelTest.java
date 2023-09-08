/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel;

/**
 * Abstract super class for tests of {@link AbstractTreeTableModel}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTreeTableModelTest<N extends AbstractTreeTableModel.AbstractTreeTableNode<N>> extends
		AbstractTreeUINodeModelTest<N> {

	@Override
	protected final AbstractTreeUINodeModel<N> createTreeUINodeModel() {
		return createTreeTableModel();
	}

	/**
	 * Creates the {@link AbstractTreeTableModel} to tests.
	 */
	protected abstract AbstractTreeTableModel<N> createTreeTableModel();

}
