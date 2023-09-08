/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * {@link InspectorTreeNode} representing a {@link Collection} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorCollectionNode extends InspectorTreeNode {

	/**
	 * Creates a {@link InspectorCollectionNode}.
	 */
	public InspectorCollectionNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty property, Collection<?> value) {
		super(model, parent, value, property);
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		Collection<?> collection = collection();
		ArrayList<InspectorTreeNode> result = new ArrayList<>(collection.size());

		int index = 0;
		for (Object entry : collection) {
			result.add(makeValueNode(new Index(index++), entry));
		}

		return result;
	}

	private Collection<?> collection() {
		return (Collection<?>) getBusinessObject();
	}

	@Override
	public boolean isLeaf() {
		return collection().isEmpty();
	}

	/**
	 * Pseudo "properties" of a collection.
	 */
	public static class Index extends InspectProperty {

		private int _index;

		/**
		 * Creates a {@link Index}.
		 * 
		 * @param index
		 *        See {@link #getIndex()}.
		 */
		public Index(int index) {
			_index = index;
		}

		/**
		 * The index in the collection.
		 */
		public int getIndex() {
			return _index;
		}

		@Override
		public Object getValue() {
			return Integer.valueOf(_index);
		}

		@Override
		public String name() {
			return "[" + _index + "]";
		}

		@Override
		public Object staticType() {
			return null;
		}

	}

}
