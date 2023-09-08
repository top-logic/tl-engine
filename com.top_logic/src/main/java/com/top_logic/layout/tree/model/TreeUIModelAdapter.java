/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

/**
 * Adapter implementation for the {@link TreeUIModel} interface.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeUIModelAdapter<N> extends TLTreeModelAdapter<TreeUIModel<N>, N> implements TreeUIModel<N> {

	/**
	 * Creates a {@link TreeUIModelAdapter}.
	 * 
	 * @param impl
	 *        See {@link #getImpl()}.
	 */
	public TreeUIModelAdapter(TreeUIModel<N> impl) {
		super(impl);
	}
	
	/**
	 * The underlying {@link TreeUIModel} implementation.
	 */
	public final TreeUIModel<N> getImpl() {
		return getImplementation();
	}

	@Override
	public boolean isDisplayed(N node) {
		return getImpl().isDisplayed(node);
	}

	@Override
	public boolean isExpanded(N node) {
		return getImpl().isExpanded(node);
	}

	@Override
	public boolean isRootVisible() {
		return getImpl().isRootVisible();
	}

	@Override
	public boolean setExpanded(N node, boolean expanded) {
		return getImpl().setExpanded(node, expanded);
	}

	@Override
	public Object getUserObject(N node) {
		return getImpl().getUserObject(node);
	}
	
}
