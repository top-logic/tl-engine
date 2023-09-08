/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Iterator;

import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Common API of {@link TreeModelBuilder} and {@link StructureModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface TreeBuilderBase<N> extends ModelBuilder {

	/**
	 * Whether the given tree node model could be part of any tree provided by this
	 * {@link TreeModelBuilder}.
	 * 
	 * @param contextComponent
	 *        The context component.
	 * @param node
	 *        The potential tree node model.
	 * @return Whether the given node could be part of a tree provided by this builder.
	 */
	boolean supportsNode(LayoutComponent contextComponent, Object node);

	/**
	 * Whether it is safe to offer the "expand all" functionality, e.g. if the tree is finite and
	 * small.
	 */
	boolean canExpandAll();

	/**
	 * Report, whether the given node cannot have child nodes.
	 * 
	 * @param contextComponent
	 *        The context component
	 * @param node
	 *        The node in question.
	 * 
	 * @return <code>true</code>, if {@link #getChildIterator(LayoutComponent, Object)} would always
	 *         return an empty iterator. <code>false</code>, if
	 *         {@link #getChildIterator(LayoutComponent, Object)} could return a non-empty iterator.
	 */
	boolean isLeaf(LayoutComponent contextComponent, N node);

	/**
	 * An {@link Iterator} over the children nodes of the given node.
	 * 
	 * @param contextComponent
	 *        The context component
	 * @param node
	 *        The node of which the children are requested.
	 * 
	 * @return An iterator of the children of the given node.
	 */
	Iterator<? extends N> getChildIterator(LayoutComponent contextComponent, N node);

}
