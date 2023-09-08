/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

/**
 * Event set by {@link TLTreeModel} implementations to {@link TreeModelListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeModelEvent {

	/**
	 * The {@link #getNode()} was added to the {@link #getModel()}.
	 */
	public static final int AFTER_NODE_ADD     = 1;
	
	/**
	 * The {@link #getNode()} is being removed from the {@link #getModel()}.
	 */
	public static final int BEFORE_NODE_REMOVE = 2;
	
	/**
	 * The {@link #getNode()} is being expanded.
	 */
	public static final int BEFORE_EXPAND      = 3;
	
	/**
	 * The {@link #getNode()} is being collapsed.
	 */
	public static final int BEFORE_COLLAPSE    = 4;

	/**
	 * The {@link #getNode()} has changed internally its display might have
	 * change in an arbitrary way.
	 */
	public static final int NODE_CHANGED       = 5;

    /**
     * Sub-structure of node has changed.
     * 
     * <p>
     * This event is sent as substitute for multiple events with the following
     * {@link #getType() types} from within the sub-tree of the {@link #getNode() node} in
     * this event:
     * </p>
     * 
     * <ul>
     * <li>{@link #AFTER_NODE_REMOVE}</li>
     * <li>{@link #AFTER_NODE_ADD}</li>
     * <li>{@link #NODE_CHANGED}</li>
     * </ul>
     * 
     * @see #BEFORE_STRUCTURE_CHANGE
     */
    public static final int AFTER_STRUCTURE_CHANGE = 6;

    /**
     * Sub-structure of node is about to get changed.
     * 
     * <p>
     * After the change, a {@link #AFTER_STRUCTURE_CHANGE} event will be sent.
     * </p>
     */
    public static final int BEFORE_STRUCTURE_CHANGE = 7;

	/**
	 * The {@link #getNode()} has been removed from the {@link #getModel()}.
	 */
    public static final int AFTER_NODE_REMOVE = 8;

	/**
	 * The {@link #getNode()} was expanded.
	 */
	public static final int AFTER_EXPAND      = 9;

	/**
	 * The {@link #getNode()} was collapsed.
	 */
	public static final int AFTER_COLLAPSE    = 10;

	private final TreeModelBase model;
	
	/** One of the constants specified above */
	private final int type;
	private final Object node;
	
	/**
	 * Creates a {@link TreeModelEvent}.
	 *
	 * @param model See {@link #getModel()}.
	 * @param type See {@link #getType()}.
	 * @param node See {@link #getNode()}.
	 */
	public TreeModelEvent(TreeModelBase model, int type, Object node) {
		this.model = model;
		this.type = type;
		this.node = node;
	}

	/**
	 * The model that sent this {@link TreeModelEvent}.
	 */
	public TreeModelBase getModel() {
		return model;
	}

	/**
	 * The event type. 
	 * 
	 * @see #BEFORE_EXPAND
	 * @see #BEFORE_COLLAPSE
	 * 
	 * @see #AFTER_NODE_ADD
	 * 
	 * @see #BEFORE_NODE_REMOVE
	 * @see #AFTER_NODE_REMOVE
	 * 
	 * @see #NODE_CHANGED
	 * 
	 * @see #BEFORE_STRUCTURE_CHANGE
	 * @see #AFTER_STRUCTURE_CHANGE
	 *
	 * @see #AFTER_EXPAND
	 * @see #AFTER_COLLAPSE
	 */
	public int getType() {
		return type;
	}

	/**
	 * The affected node.
	 */
	public Object getNode() {
		return node;
	}
	
}
