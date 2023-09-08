/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

import javax.swing.tree.TreeModel;

/**
 * Adapter that makes Swing {@link TreeModel}s compatible with {@link TreeView}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TreeViewAdapter implements TreeView<Object> {
	
	/* package protected */final TreeModel model;

	/**
	 * Creates a {@link TreeViewAdapter}.
	 *
	 * @param model A Swing {@link TreeModel}.
	 */
	public TreeViewAdapter(TreeModel model) {
		this.model = model;
	}

	@Override
	public boolean isLeaf(Object aNode) {
	    return model.isLeaf(aNode);
	}
	
	@Override
	public Iterator<?> getChildIterator(final Object node) {
		return new Iterator<Object>() {
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < model.getChildCount(node);
			}
	
			@Override
			public Object next() {
				return model.getChild(node, index++);
			}
	
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}