/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * The class {@link TLTreeModelAdapter} is an adapter for {@link TLTreeModel}.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLTreeModelAdapter<E extends TLTreeModel<N>, N> extends TreeModelBaseAdapter<E, N> implements
		TLTreeModel<N> {

	/**
	 * Creates a {@link TLTreeModelAdapter}.
	 *
	 * @param impl See {@link TreeModelBaseAdapter#TreeModelBaseAdapter(TreeModelBase)}.
	 */
	public TLTreeModelAdapter(E impl) {
		super(impl);
	}

	@Override
	public List<N> createPathToRoot(N aNode) {
		return getImplementation().createPathToRoot(aNode);
	}

	@Override
	public N getParent(N aNode) {
		return getImplementation().getParent(aNode);
	}

	@Override
	public Object getBusinessObject(N node) {
		return getImplementation().getBusinessObject(node);
	}

}
