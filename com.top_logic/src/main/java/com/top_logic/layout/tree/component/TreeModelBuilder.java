/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Collection;
import java.util.Set;

import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.listen.ModelListener;

/**
 * {@link ModelBuilder} providing a generalized tree.
 * 
 * <p>
 * In a generalized tree,
 * </p>
 * 
 * <ul>
 * <li>the parent relation is only defined in the context of a given root node.</li>
 * <li>the same application object may be displayed more than once in the same tree.</li>
 * </ul>
 * 
 * @see StructureModelBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeModelBuilder<N> extends TreeBuilderBase<N> {

	/**
	 * Find a tree root node so that
	 * {@link #supportsModel(Object, LayoutComponent)} for that root is
	 * <code>true</code> and the given node is part of the tree provided for
	 * that root node.
	 * 
	 * <p>
	 * The given node is part of the tree, if it can be reached from the
	 * returned root through recursively applying the
	 * {@link #getChildIterator(LayoutComponent, Object)} relation and the returned root can be
	 * reached by recursively applying the
	 * {@link #getParents(LayoutComponent, Object)} relation to the given node
	 * after installing the found root node as
	 * {@link LayoutComponent#getModel()} into the context component.
	 * </p>
	 * 
	 * @param contextComponent
	 *        The context component displaying the tree provided by this
	 *        builder.
	 * @param node
	 *        A potential tree node for which
	 *        {@link #supportsNode(LayoutComponent, Object)} is
	 *        <code>true</code>.
	 * @return The component model alias root node so that the given node is
	 *         part of the tree for that root.
	 */
    public Object retrieveModelFromNode(LayoutComponent contextComponent, N node);

	/**
	 * Find the parent nodes that
	 * 
	 * <ul>
	 * <li>report the given node in their {@link #getChildIterator(LayoutComponent, Object)} collection and</li>
	 * <li>are displayed in the context of the given component {@link LayoutComponent#getModel()}.</li>
	 * </ul>
	 * 
	 * @param contextComponent
	 *        The context component displaying the tree provided by this builder.
	 * @param node
	 *        A potential node for which {@link #supportsNode(LayoutComponent, Object)} returns
	 *        <code>true</code>.
	 * @return The set of potential parents.
	 */
	Collection<? extends N> getParents(LayoutComponent contextComponent, N node);

	/**
	 * Returns the preload operation to execute before the expansion of the nodes.
	 * 
	 * <p>
	 * The returned {@link PreloadOperation} expects that the "base objects" to prepare are the
	 * nodes going to expand.
	 * </p>
	 * 
	 * <p>
	 * It is recommend to return {@link NoPreload#INSTANCE}, if it is clear that no preload must be
	 * done.
	 * </p>
	 */
	PreloadOperation loadForExpansion();

	/**
	 * Returns the additional nodes to update if a given object changes.
	 * 
	 * <p>
	 * In a tree, the direct parents and children of the corresponding nodes are updated by default.
	 * </p>
	 */
	Collection<? extends N> getNodesToUpdate(LayoutComponent contextComponent, Object businessObject);

	/**
	 * The {@link TLStructuredType}s for which the tree needs to register itself as
	 * {@link ModelListener}.
	 * <p>
	 * The result has to be constant. It cannot be used for temporarily relevant objects.
	 * </p>
	 * 
	 * @see LayoutComponent#getTypesToObserve()
	 */
	Set<TLStructuredType> getTypesToObserve();

}
