/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Iterator;
import java.util.List;

/**
 * The class {@link TreeModelBaseAdapter} is an adapter for
 * {@link TreeModelBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeModelBaseAdapter<E extends TreeModelBase<N>, N> extends AbstractTreeModelBase<N> implements TreeModelListener {
	
	private final E impl;

	/**
	 * Creates a {@link TreeModelBaseAdapter}.
	 *
	 * @param impl The base model.
	 */
	public TreeModelBaseAdapter(E impl) {
		assert impl != null : "Implementation must not be null.";
		this.impl = impl;
	}

	/**
	 * The base model of this adapter.
	 */
	protected E getImplementation() {
		return impl;
	}

	@Override
	public boolean addTreeModelListener(TreeModelListener listener) {
		boolean firstListener = !hasListeners();
		final boolean added = super.addTreeModelListener(listener);
		if (firstListener && added) {
			getImplementation().addTreeModelListener(this);
		}
		return added;
	}

	@Override
	public boolean removeTreeModelListener(TreeModelListener listener) {
		final boolean removed = super.removeTreeModelListener(listener);
		if (removed && !hasListeners()) {
			getImplementation().removeTreeModelListener(this);
		}
		return removed;
	}
	
	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		notifyListeners(transform(evt));
	}

	/**
	 * Changes {@link TreeModelEvent#getModel()} to this instance.
	 */
	private TreeModelEvent transform(TreeModelEvent evt) {
		return new TreeModelEvent(this, evt.getType(), evt.getNode());
	}

	@Override
	public boolean containsNode(N aNode) {
		return getImplementation().containsNode(aNode);
	}

	@Override
	public List<? extends N> getChildren(N parent) {
		return getImplementation().getChildren(parent);
	}

	@Override
	public boolean childrenInitialized(N parent) {
		return getImplementation().childrenInitialized(parent);
	}

	@Override
	public void resetChildren(N parent) {
		getImplementation().resetChildren(parent);
	}

	@Override
	public N getRoot() {
		return getImplementation().getRoot();
	}

	@Override
	public boolean hasChild(N parent, Object node) {
		return getImplementation().hasChild(parent, node);
	}

	@Override
	public boolean hasChildren(N aNode) {
		return getImplementation().hasChildren(aNode);
	}

	@Override
	public Iterator<? extends N> getChildIterator(N node) {
		return getImplementation().getChildIterator(node);
	}

	@Override
	public boolean isLeaf(N node) {
		return getImplementation().isLeaf(node);
	}

	@Override
	public boolean isFinite() {
		return getImplementation().isFinite();
	}

}
