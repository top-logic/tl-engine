/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Iterator;

import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeView} based on a {@link TreeBuilderBase} and a given {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeBuilderTreeView<N> implements TreeView<N> {

	private final LayoutComponent _component;

	private final TreeBuilderBase<N> _treeBuilder;

	/**
	 * Creates a new {@link TreeBuilderTreeView}.
	 * 
	 * @param component
	 *        The component to serve as argument for all calls to given {@link TreeBuilderBase}
	 *        methods.
	 * @param treeBuilder
	 *        The {@link TreeBuilderBase} this {@link TreeView} is a view onto.
	 */
	public TreeBuilderTreeView(LayoutComponent component, TreeBuilderBase<N> treeBuilder) {
		_component = component;
		_treeBuilder = treeBuilder;
	}

	@Override
	public boolean isLeaf(N node) {
		return _treeBuilder.isLeaf(_component, node);
	}

	@Override
	public Iterator<? extends N> getChildIterator(N node) {
		return _treeBuilder.getChildIterator(_component, node);
	}

	@Override
	public boolean isFinite() {
		return _treeBuilder.canExpandAll();
	}

}

