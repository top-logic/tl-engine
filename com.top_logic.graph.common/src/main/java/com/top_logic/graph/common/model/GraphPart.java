/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import com.top_logic.common.remote.listener.AttributeObservable;

/**
 * Common interface for entities in a graph, like {@link Node}s, {@link Edge}s but also
 * {@link Label}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphPart extends TagOwner, AttributeObservable {

	/**
	 * Name of the {@link #isVisible()} property.
	 */
	String VISIBLE = "visible";

	/**
	 * Prefix to shared object properties that should be accessible as JavaScript properties of the
	 * client-side version of a shared object.
	 */
	String USER_PROPERTY_PREFIX = "user-";

	/**
	 * The {@link GraphModel} of this {@link GraphPart}.
	 */
	GraphModel getGraph();

	/**
	 * Initializer for {@link #getGraph()}.
	 * <p>
	 * Has to be called exactly once.
	 * </p>
	 */
	void initGraph(GraphModel graph);

	/**
	 * Deletes this {@link GraphModel} part.
	 */
	void delete();

	/**
	 * Whether this {@link GraphPart} is actually displayed in the view graph.
	 * 
	 * <p>
	 * A content {@link Node} of in a {@link Node#setExpansion(boolean) collapsed} group is not
	 * removed from the {@link GraphModel} but only marked invisible. The same happens to its
	 * {@link Label}s, {@link Edge}s and their {@link Label}s
	 * </p>
	 */
	boolean isVisible();

	/**
	 * @see #isVisible()
	 */
	void setVisible(boolean value);

	/**
	 * The builder that is responsible for synchronizing this {@link GraphPart} with the persistency
	 * layer.
	 * <p>
	 * This property is necessary for the builder to recognize its {@link GraphPart}s and deleting
	 * them when they should no longer be part of the graph. Without it the builders would delete
	 * each others objects when there is more than one builder for one type of {@link GraphPart}.
	 * </p>
	 */
	Object getBuilder();

	/** @see #getBuilder() */
	void setBuilder(Object builder);

}
