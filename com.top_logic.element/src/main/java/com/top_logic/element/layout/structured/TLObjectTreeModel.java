/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * {@link TLTreeModel} navigating a single {@link TLReference} from a root {@link TLObject}.
 */
public class TLObjectTreeModel extends AbstractTreeModel<TLObject> {

	private TLObject _root;

	private TLReference _childrenRef;

	/**
	 * Creates a {@link TLObjectTreeModel}.
	 */
	public TLObjectTreeModel(TLObject root, TLReference childrenRef) {
		_root = root;
		_childrenRef = childrenRef;
	}

	@Override
	public Object getBusinessObject(TLObject node) {
		return node;
	}

	@Override
	public TLObject getRoot() {
		return _root;
	}

	@Override
	public List<? extends TLObject> getChildren(TLObject parent) {
		@SuppressWarnings("unchecked")
		List<? extends TLObject> result = (List<? extends TLObject>) CollectionUtil.asList(parent.tValue(_childrenRef));
		return result;
	}

	@Override
	public TLObject getParent(TLObject node) {
		if (node == _root) {
			return null;
		}

		Set<? extends TLObject> parents = _childrenRef.getReferers(node);
		return CollectionUtil.getSingleValueFromCollection(parents);
	}

	@Override
	public boolean childrenInitialized(TLObject parent) {
		return false;
	}

	@Override
	public void resetChildren(TLObject parent) {
		// Ignore.
	}

	@Override
	public boolean isLeaf(TLObject node) {
		return false;
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}
