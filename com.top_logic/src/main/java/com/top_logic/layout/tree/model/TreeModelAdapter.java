/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.WrappedModel;

/**
 * Adaptor for {@link TreeModelBase}.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeModelAdapter<N> extends AbstractTreeModelBase<N> implements WrappedModel {

	protected final TreeModelBase<N> impl;
	
	/**
	 * Forward the TreeModelEvents to the Listeners at the GUI.
	 */
	private final TreeModelListener eventForwarder = new TreeModelListener() {
		@Override
		public void handleTreeUIModelEvent(TreeModelEvent evt) {
			fireTreeModelEvent(evt.getType(), evt.getNode());
		}
	};
	
	/**
	 * Creates a {@link TreeModelAdapter}.
	 *
	 * @param impl The base model.
	 */
	public TreeModelAdapter(TreeModelBase<N> impl) {
		this.impl = impl;
		this.impl.addTreeModelListener(eventForwarder);
	}
	
	@Override
	public boolean containsNode(N node) {
		return impl.containsNode(node);
	}

	@Override
	public List<? extends N> getChildren(N parent) {
		return impl.getChildren(parent);
	}

	@Override
	public boolean childrenInitialized(N parent) {
		return impl.childrenInitialized(parent);
	}

	@Override
	public void resetChildren(N parent) {
		impl.resetChildren(parent);
	}

	@Override
	public N getRoot() {
		return impl.getRoot();
	}

	@Override
	public boolean hasChild(N parent, Object node) {
		return impl.hasChild(parent, node);
	}

	@Override
	public boolean hasChildren(N node) {
		return impl.hasChildren(node);
	}

	@Override
	public Iterator<? extends N> getChildIterator(N node) {
		return impl.getChildIterator(node);
	}

	@Override
	public boolean isLeaf(N node) {
		return impl.isLeaf(node);
	}

	@Override
	public boolean isFinite() {
		return impl.isFinite();
	}

	@Override
	public Object getWrappedModel() {
		if (impl instanceof WrappedModel) {
			return ((WrappedModel) impl).getWrappedModel();
		} else {
			return impl;
		}
	}
}
