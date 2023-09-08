/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.col.LazyTypedAnnotatable;

/**
 * Base implementation for {@link TLTreeNode} implementations.
 * 
 * @param <N>
 *        The concrete node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLTreeNode<N extends AbstractTLTreeNode<N>> extends LazyTypedAnnotatable
		implements TLTreeNode<N> {

	private N parent;

	private Object _businessObject;

	/**
	 * Creates a {@link AbstractTLTreeNode}.
	 *
	 * @param parent See {@link #getParent()}.
	 * @param businessObject See {@link #getBusinessObject()}.
	 */
	public AbstractTLTreeNode(N parent, Object businessObject) {
		this.parent = parent;
		_businessObject = businessObject;
	}
	
	/* package protected */void initParent(N newParent) {
		if (this.parent != null) {
			throw new IllegalStateException("Parent already initialized.");
		}
		this.parent = newParent;
	}
	
	@Override
	public N getParent() {
		return parent;
	}
	
	@Override
	public Object getBusinessObject() {
		return _businessObject;
	}

	/**
	 * Sets the user object of this node
	 * 
	 * @param businessObject
	 *        the new business object
	 * @see #getBusinessObject()
	 * @throws IllegalStateException
	 *         when this node was removed from its parent
	 */
	protected Object setBusinessObject(Object businessObject) {
		Object oldUserObject = _businessObject;
		_businessObject = businessObject;
		return oldUserObject;
	}

	@Override
	public N getChildAt(int childIndex) {
		return getChildren().get(childIndex);
	}

	@Override
	public int getChildCount() {
		return getChildren().size();
	}

	@Override
	public int getIndex(Object node) {
		return getChildren().indexOf(node);
	}

	@Override
	public boolean isLeaf() {
		return getChildren().isEmpty();
	}

	/**
	 * Notifies this node that it was removed from its parent.
	 */
	protected void notifyRemoved() {
		this.parent = null;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[userObject:" + getBusinessObject() + "]";
	}

}