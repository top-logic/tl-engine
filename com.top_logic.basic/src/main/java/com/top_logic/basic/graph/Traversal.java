/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import com.top_logic.basic.col.Filter;

/**
 * Abstract strategy for traversing graphs.
 * 
 * <p>
 * The life-cycle of a {@link Traversal} is the following:
 * </p>
 * 
 * <ul>
 * <li>Create an instance</li>
 * <li>Configure by calling some setters.</li>
 * <li>If the instance should be shared with others, calling {@link #freeze()}
 * to prevent concurrent modifications of the configuration.</li>
 * <li>Calling {@link #traverse(Object, TraversalListener)} for executing the
 * traversal.</li>
 * </ul>
 * 
 * <p>
 * After a {@link Traversal} is configured and {@link #freeze()} has been
 * called, it's safe to use {@link #traverse(Object, TraversalListener)} in
 * multiple threads concurrently.
 * </p>
 * 
 * @param <T> The node type of the traversed graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Traversal<T> {

    /**
     * Constant for requesting traversal of unlimited depth.
     */
    public static final int UNLIMITED_DEPTH = Integer.MAX_VALUE;

    /**
     * Must only be modified by {@link #setExcludeStart(boolean)}, direct access for efficiency.
     * 
     * @see #setExcludeStart(boolean)
     */
    protected boolean excludeStart = false;

	/**
     * Must only be modified by {@link #setMaxDepth(int)}, direct access for efficiency.
     * 
     * @see #setMaxDepth(int)
	 */
    protected int maxDepth = UNLIMITED_DEPTH;

	/**
     * Must only be modified by {@link #setFilter(Filter)}, direct access for efficiency.
     * 
     * @see #setFilter(Filter)
	 */
    protected Filter<? super T> filter;

    /**
     * Whether the traversal config cannot be modified any longer.
     */
    private boolean frozen;

	protected final GraphAccess<T> access;

	/**
	 * Creates a {@link Traversal}.
	 * 
	 * @param access
	 *        The accessor of the traversed graph.
	 */
    public Traversal(GraphAccess<T> access) {
		this.access = access;
	}
    
    /**
	 * Sets whether to exclude the given element from the visit.
	 * 
	 * @return this strategy for call chaining.
     */
    public Traversal<T> setExcludeStart(boolean value) {
    	checkModify();
		this.excludeStart = value;
		return this;
	}
    
    /**
	 * Sets the maximum depth to which the traversal descends, or
	 * {@link #UNLIMITED_DEPTH} for no limit. element is 0).
	 * 
	 * @return this strategy for call chaining.
     */
    public Traversal<T> setMaxDepth(int maxDepth) {
    	checkModify();
		this.maxDepth = maxDepth;
		return this;
	}
    
    /**
	 * The {@link Filter} that selects eligible elements from the structure to
	 * traverse.
	 * 
	 * @return this strategy for call chaining.
     */
    public Traversal<T> setFilter(Filter<? super T> filter) {
    	checkModify();
		this.filter = filter;
		return this;
	}
    
    /**
     * Prevent modifying this {@link Traversal} configuration.
     * 
     * @return this strategy for call chaining.
     */
    public Traversal<T> freeze() {
    	this.frozen = true;
    	return this;
    }

	/**
	 * Prevent modifying {@link #freeze() frozen} traversal configurations.
	 * 
	 * @throws IllegalStateException
	 *         If {@link #freeze()} has been called before.
	 */
	protected final void checkModify() {
		if (frozen) {
			throw new IllegalStateException("Traversal is no longer modifyable.");
		}
	}

	/**
	 * Traverse the structure with this {@link Traversal} strategy starting with
	 * the given element.
	 * 
	 * @param start
	 *        The node to start the traversal with.
	 * @param visitor
	 *        The visitor that is called for each element found during the
	 *        traversal.
	 * @return Whether traversal finished after reporting all requested
	 *         elements. <code>false</code> is returned, if traversal has been
	 *         stopped by the given visitor.
	 */
	public abstract boolean traverse(T start, TraversalListener<? super T> visitor);
	

    /**
     * Locally visit the given element.
     * 
     * @return Whether traversing should unconditionally stop.
     */
	protected boolean visit(TraversalListener<? super T> visitor, T element, int depth) {
		return visitor.onVisit(element, depth);
	}
	
}
