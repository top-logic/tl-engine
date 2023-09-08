/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import static com.top_logic.config.xdiff.util.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for {@link Node}s that have a non-empty {@link #getChildren()} list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FragmentBase extends Node {

	private List<Node> lazyChildren;

	@Override
	public NodeType getNodeType() {
		return NodeType.FRAGMENT;
	}

	@Override
	public List<Node> getChildren() {
		return lazyChildren == null ? Collections.<Node> emptyList() : lazyChildren;
	}

	/**
	 * @see #setChildren(List)
	 */
	public final FragmentBase setChildren(Node... newChildren) {
		return setChildren(list(newChildren));
	}

	/**
	 * Sets the {@link #getChildren()} of this node.
	 * 
	 * <p>
	 * No reference to the given list is kept. <code>null</code> entries in the given list are
	 * skipped.
	 * </p>
	 * 
	 * @return This instance for call chaining.
	 */
	public FragmentBase setChildren(List<Node> newChildren) {
		if (newChildren.isEmpty()) {
			lazyChildren = null;
		} else {
			lazyChildren = nonNullList(newChildren);
		}
		return this;
	}

	/**
	 * Adds the given node to the list of {@link #getChildren()}.
	 */
	public void addChild(Node child) {
		initializedChildren().add(child);
	}

	/**
	 * Adds all given nodes to the list of {@link #getChildren()}.
	 */
	public void addChildren(List<Node> newChildren) {
		if (!newChildren.isEmpty()) {
			initializedChildren().addAll(newChildren);
		}
	}

	private List<Node> initializedChildren() {
		if (lazyChildren == null) {
			lazyChildren = new ArrayList<>();
		}
		return lazyChildren;
	}

}
