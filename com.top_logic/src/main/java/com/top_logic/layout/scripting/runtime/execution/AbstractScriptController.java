/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Common base class for {@link ScriptController} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractScriptController implements ScriptController {

	private final TLTreeNode<?> _root;

	private Set<TLTreeNode<?>> _breakPoints = new HashSet<>();


	/**
	 * Creates a {@link AbstractScriptController}.
	 *
	 * @param root
	 *        See {@link #getRoot()}.
	 */
	public AbstractScriptController(TLTreeNode<?> root) {
		_root = root;
	}

	@Override
	public TLTreeNode<?> getRoot() {
		return _root;
	}

	@Override
	public void setBreakPoint(TLTreeNode<?> node) {
		if (node != null) {
			_breakPoints.add(node);
		}
	}

	/**
	 * Whether the given action is {@link #setBreakPoint(TLTreeNode) marked as breakpoint}.
	 */
	protected final boolean isBreakPoint(TLTreeNode<?> node) {
		return _breakPoints.contains(node);
	}

	/**
	 * Utility method to find the action logically following the given action.
	 *
	 * @param node
	 *        The position of the action in the script.
	 * @return Position of the action to execute after the given one.
	 */
	public static TLTreeNode<?> followingNode(TLTreeNode<?> node) {
		if (node.getChildCount() > 0) {
			// Descend: Take first child.
			return node.getChildren().get(0);
		} else {
			return followingSibling(node);
		}
	}

	/**
	 * Utility method to find the action logically following the given action if sub-actions are
	 * skipped.
	 *
	 * @param node
	 *        The position of the action in the script.
	 * @return Position of the action to execute after the given one skipping sub-actions of the
	 *         given action.
	 */
	public static TLTreeNode<?> followingSibling(TLTreeNode<?> node) {
		while (true) {
			TLTreeNode<?> parent = node.getParent();
			if (parent == null) {
				// Root has no next.
				return null;
			}
			List<? extends TLTreeNode<?>> siblings = parent.getChildren();
			int nextIndex = parent.getIndex(node) + 1;
			if (nextIndex < siblings.size()) {
				// Take following sibling.
				return siblings.get(nextIndex);
			} else {
				// Ascend back to parent and continue.
				node = parent;
			}
		}
	}

}
