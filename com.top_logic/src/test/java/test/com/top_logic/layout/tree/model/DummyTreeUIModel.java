/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * The class {@link DummyTreeUIModel} implements all methods empty
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DummyTreeUIModel<N> implements TreeUIModel<N> {

	@Override
	public Object getBusinessObject(N node) {
		return node;
	}

	@Override
	public Object getUserObject(N node) {
		return getBusinessObject(node);
	}

	@Override
	public boolean isDisplayed(N node) {
		return false;
	}

	@Override
	public boolean isExpanded(N node) {
		return false;
	}

	@Override
	public boolean isRootVisible() {
		return false;
	}

	@Override
	public boolean setExpanded(N node, boolean expanded) {
		return false;
	}

	@Override
	public List<N> createPathToRoot(N node) {
		return null;
	}

	@Override
	public N getParent(N node) {
		return null;
	}

	@Override
	public boolean addTreeModelListener(TreeModelListener listener) {
		return false;
	}

	@Override
	public boolean containsNode(Object node) {
		return false;
	}

	@Override
	public List<N> getChildren(N parent) {
		return Collections.emptyList();
	}

	@Override
	public boolean childrenInitialized(N parent) {
		return true;
	}

	@Override
	public void resetChildren(N parent) {
		// Ignore.
	}

	@Override
	public N getRoot() {
		return null;
	}

	@Override
	public boolean hasChild(Object parent, Object node) {
		return false;
	}

	@Override
	public boolean hasChildren(Object node) {
		return false;
	}

	@Override
	public boolean removeTreeModelListener(TreeModelListener listener) {
		return false;
	}

	@Override
	public Iterator<N> getChildIterator(N node) {
		return getChildren(node).iterator();
	}

	@Override
	public boolean isLeaf(N node) {
		return getChildren(node).isEmpty();
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

