/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * Adaptor implementation for a {@link TLTreeModel}.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeStructureModelAdaptor<N> extends TreeModelAdapter<N> implements TLTreeModel<N> {
	
	/**
	 * Creates a {@link TreeStructureModelAdaptor}.
	 *
	 * @param impl See {@link TreeModelAdapter#TreeModelAdapter(TreeModelBase)}.
	 */
	public TreeStructureModelAdaptor(TLTreeModel<N> impl) {
		super(impl);
	}

	@Override
	public List<N> createPathToRoot(N node) {
		return getTreeModelImpl().createPathToRoot(node);
	}

	@Override
	public N getParent(N node) {
		return getTreeModelImpl().getParent(node);
	}

	protected final TLTreeModel<N> getTreeModelImpl() {
		return (TLTreeModel<N>) impl;
	}

	@Override
	public Object getBusinessObject(N node) {
		return getTreeModelImpl().getBusinessObject(node);
	}

}
