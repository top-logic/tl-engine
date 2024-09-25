/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Dummy {@link TLTreeModel} consisting only of the given root node.
 */
public class DummyTreeModel extends AbstractTreeModel<Object> {

	private Object _root;

	/**
	 * Creates a {@link DummyTreeModel}.
	 */
	public DummyTreeModel(Object root) {
		_root = root;
	}

	@Override
	public Object getBusinessObject(Object node) {
		return node;
	}

	@Override
	public Object getRoot() {
		return _root;
	}

	@Override
	public List<? extends Object> getChildren(Object parent) {
		return Collections.emptyList();
	}

	@Override
	public Object getParent(Object node) {
		return null;
	}

	@Override
	public boolean childrenInitialized(Object parent) {
		return false;
	}

	@Override
	public void resetChildren(Object parent) {
		// Ignore.
	}

	@Override
	public boolean isLeaf(Object node) {
		return true;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}
