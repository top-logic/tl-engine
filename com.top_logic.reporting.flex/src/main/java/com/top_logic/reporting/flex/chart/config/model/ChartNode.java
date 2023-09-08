/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.reporting.flex.chart.config.util.KeyCompare;

/**
 * A node in the tree-structure of a {@link ChartTree}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ChartNode extends LazyTypedAnnotatable implements Comparable<ChartNode> {

	private final ChartNode _parent;

	@Inspectable
	private final List<?> _objects;

	private Number _value;

	@Inspectable
	private Map<Comparable<?>, ChartNode> _children;

	private boolean _isLeaf;

	@Inspectable
	private final Comparable<?> _key;

	private final ChartTree _tree;

	private final int _level;

	private final String _id;

	/**
	 * Creates a new {@link ChartNode}
	 * 
	 * @param key
	 *        a unique key within its siblings
	 * @param parent
	 *        the parent node in the tree-structure
	 * @param objects
	 *        the context-objects used for calculation of the value
	 * @param value
	 *        the number-value to be visualized in a chart
	 */
	public ChartNode(Comparable<?> key, ChartNode parent, List<?> objects, Number value) {
		this(key, parent, objects, value, parent._tree, parent._level + 1);
	}

	/**
	 * Creates a new {@link ChartNode}
	 * 
	 * @param key
	 *        a unique key within its siblings
	 * @param parent
	 *        the parent node in the tree-structure
	 * @param objects
	 *        the context-objects used for calculation of the value
	 * @param value
	 *        the number-value to be visualized in a chart
	 * @param tree
	 *        the {@link ChartTree} this node should be part of
	 * @param level
	 *        the level in the tree where this node is located
	 */
	public ChartNode(Comparable<?> key, ChartNode parent, List<?> objects, Number value, ChartTree tree, int level) {
		_key = key;
		_parent = parent;
		_objects = objects;
		_value = value;
		_tree = tree == null ? new ChartTree(this) : tree;
		_level = level;
		_isLeaf = false;
		if (parent != null) {
			parent.addChild(this);
		}
		_id = _tree.registerNewNode(this);
	}

	/**
	 * the {@link ChartTree} this node is part of
	 */
	public ChartTree getTree() {
		return _tree;
	}

	/**
	 * the ID of this {@link ChartNode} unique within this {@link ChartTree}
	 */
	public String getID() {
		return _id;
	}

	/**
	 * @param chartNode
	 *        the node to register as child of this
	 */
	protected void addChild(ChartNode chartNode) {
		if (_children == null) {
			_children = new LinkedHashMap<>();
		}
		_children.put(chartNode.getKey(), chartNode);
		_tree.setChildSize(_level, _children.size());
	}

	/**
	 * the list of children, may be empty but never null
	 */
	public List<ChartNode> getChildren() {
		return _children == null ? Collections.<ChartNode> emptyList() : new ArrayList<>(_children.values());
	}

	/**
	 * @param key
	 *        the key of a {@link ChartNode} that is expected to be a child of this
	 * @return the child {@link ChartNode} with the given key or null if no such child exists
	 */
	public ChartNode getChildByKey(Comparable<?> key) {
		return _children == null ? null : _children.get(key);
	}

	/**
	 * the objects that are used to calculate the number value returned by
	 *         {@link #getValue()}
	 */
	public List<?> getObjects() {
		return _objects;
	}

	/**
	 * the number-value based on the objects of this node to be visualized in a chart.
	 */
	public Number getValue() {
		return _value;
	}

	/**
	 * see {@link #getValue()}
	 */
	public void setValue(Number value) {
		_value = value;
	}

	/**
	 * Indicates if this node is a (functional) leaf. This means it does not only have no children
	 * but also it could not have any children because its level is the max. depth of the tree. The
	 * value of leaf-nodes is visualized in a chart.
	 */
	public boolean isLeaf() {
		return _isLeaf;
	}

	/**
	 * see {@link #isLeaf()}
	 */
	public void setIsLeaf(boolean isLeaf) {
		_isLeaf = isLeaf;
	}

	/**
	 * the parent-node in the tree-structure of this {@link ChartTree}
	 */
	public ChartNode getParent() {
		return _parent;
	}

	/**
	 * the key of this node, must be unique within its siblings
	 */
	public Comparable<?> getKey() {
		return _key;
	}

	@Override
	public int compareTo(ChartNode o) {
		return KeyCompare.INSTANCE.compare(_key, o.getKey());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ChartNode parent = this;
		while (parent != null) {
			sb.append(parent.getKey());
			sb.append(" - ");
			parent = parent.getParent();
		}
		sb.append(_value);
		return sb.toString();
	}

}