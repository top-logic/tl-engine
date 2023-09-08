/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.Iterator;

import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.tree.model.TreeModelBase;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;

/**
 * {@link IdentityProvider} that creates IDs for tree nodes.
 * 
 * <p>
 * A {@link TreeNodeIdentification} observes the model after {@link #attach()} and clears IDs for
 * removed or invalidated nodes.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeNodeIdentification extends DefaultIdentityProvider implements TreeModelListener {
	
	private TreeModelBase model;
	
	/**
	 * Create a {@link TreeNodeIdentification}.
	 * 
	 * @param idSource
	 *        The source of IDs.
	 */
	public TreeNodeIdentification(IdentifierSource idSource) {
		super(idSource);
	}

	/**
	 * Sets the tree model whose nodes are identified.
	 */
	public void setModel(TreeModelBase newModel) {
		boolean wasAttached = detach();
		
		this.model = newModel;
		
		if (wasAttached) {
			attach();
		}
	}
	
	@Override
	protected void internalAttach() {
		super.internalAttach();
		model.addTreeModelListener(this);
	}

	@Override
	protected void internalDetach() {
		model.removeTreeModelListener(this);
		super.internalDetach();
	}
	
	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		switch (evt.getType()) {
		case TreeModelEvent.BEFORE_STRUCTURE_CHANGE:
		case TreeModelEvent.BEFORE_NODE_REMOVE: {
			Object node = evt.getNode();
			invalidateSubtree(node);
		}
		}
	}
	
	private void invalidateSubtree(Object node) {
		boolean changed = invalidateId(node);
		if (!changed) {
			return;
		}

		if (!model.childrenInitialized(node)) {
			/* Children not initialized, therefore there are no id's created by this
			 * TreeNodeIdentification. */
			return;
		}

		for (Iterator<?> it = model.getChildIterator(node); it.hasNext();) {
			invalidateSubtree(it.next());
		}
	}
	
}
