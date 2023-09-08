/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

/**
 * {@link TLTreeModel} that manages an {@link UserObjectIndex}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IndexedTLTreeModel<N extends AbstractMutableTLTreeNode<N>> extends TLTreeModel<N> {

	/**
	 * The index of nodes.
	 */
	UserObjectIndex<N> getIndex();

}
