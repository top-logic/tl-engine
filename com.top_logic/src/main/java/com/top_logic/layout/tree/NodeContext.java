/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.ArrayList;


/**
 * Describes the context of a tree node being rendered.
 * 
 * @see TreeRenderer for the consumer of those context objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeContext {
	
	/** 
	 * Constant indicating that a tree node has no preceding siblings.
	 * 
	 * @see #getNodePosition(int)
	 */
	public static final int FIRST_NODE  = 1;
	
	/** 
	 * Constant indicating that a tree node has preceding and following siblings. 
	 * 
	 * @see #getNodePosition(int)
	 */
	public static final int MIDDLE_NODE = 0;
	
	/** 
	 * Constant indicating that a tree node has no following siblings. 
	 * 
	 * @see #getNodePosition(int)
	 */
	public static final int LAST_NODE   = 2;
	
	/** 
	 * Constant indicating that a tree node has no siblings. 
	 * 
	 * @see #getNodePosition(int)
	 */
	public static final int SINGLE_NODE  = FIRST_NODE | LAST_NODE;
	
	/**
	 * Context data per node.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private class Data {
		Object node;
		int nodePosition;
		
		public Data(Object node, int nodePosition) {
			super();
			this.node = node;
			this.nodePosition = nodePosition;
		}
	}
	
	/**
	 * {@link Data} for each ancestor-or-self node of the currently rendered
	 * node.
	 * 
	 * <p>
	 * The first element is the root node data, the last element is the data
	 * of the currently rendered node.
	 * </p>
	 */
	private final ArrayList<Data> data;
	private final TreeControl tree;

	public NodeContext(TreeControl tree) {
		this.tree = tree;
		this.data = new ArrayList<>();
	}
	
	/**
	 * The {@link TreeControl} in which context the {@link #currentNode()} is rendered.
	 */
	public TreeControl getTree() {
		return tree;
	}
	/**
	 * The currently rendered node of the {@link #getTree() tree's} model.
	 */
	public Object currentNode() {
		return data.get(data.size() - 1).node;
	}
	
	/**
	 * A position classification of the {@link #currentNode()}.
	 * 
	 * @see #FIRST_NODE
	 * @see #MIDDLE_NODE
	 * @see #LAST_NODE
	 * @see #SINGLE_NODE
	 */
	public int currentNodePosition() {
		return data.get(data.size() - 1).nodePosition;
	}

	/**
	 * The number of ancestor-or-self nodes of the {@link #currentNode()}.
	 * 
	 * @see #getNode(int)
	 * @see #getNodePosition(int)
	 */
	public int getSize() {
		return data.size();
	}
	
	/**
	 * The node at the given rendering depth.
	 * 
	 * @param depth
	 *        The depth of the requested node. The top-level node has depth
	 *        <code>0</code>, the {@link #currentNode()} has depth
	 *        <code>{@link #getSize()} - 1</code>.
	 */
	public Object getNode(int depth) {
		return data.get(depth).node;
	}
	
	/**
	 * The node position of the node at the given rendering depth.
	 * 
	 * @see #currentNodePosition() for a description of possible positions.
	 * @see #getNode(int) for a description of the depth parameter. 
	 */
	public int getNodePosition(int depth) {
		return data.get(depth).nodePosition;
	}

	
	public void push(Object node, int nodePosition) {
		data.add(new Data(node, nodePosition));
	}
	
	public void pop() {
		data.remove(data.size() - 1);
	}

}
