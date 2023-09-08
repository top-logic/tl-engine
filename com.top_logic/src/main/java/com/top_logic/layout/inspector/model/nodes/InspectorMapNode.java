/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.layout.inspector.resources.InspectorNodeResourceProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * {@link InspectorTreeNode} representing a {@link Map} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorMapNode extends InspectorTreeNode {

	/**
	 * Creates a {@link InspectorMapNode}.
	 */
	public InspectorMapNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty property, Map<?, ?> userObject) {
		super(model, parent, userObject, property);
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		Map<?, ?> map = map();
		ArrayList<InspectorTreeNode> result = new ArrayList<>(map.size());
		for (Entry<?, ?> entry : map.entrySet()) {
			result.add(makeValueNode(new Key(this, entry.getKey()), entry.getValue()));
		}

		Collections.sort(result, new Comparator<InspectorTreeNode>() {
			@Override
			public int compare(InspectorTreeNode o1, InspectorTreeNode o2) {
				return o1.getProperty().name().compareTo(o2.getProperty().name());
			}
		});

		return result;
	}

	private Map<?, ?> map() {
		return (Map<?, ?>) getBusinessObject();
	}

	@Override
	public boolean isLeaf() {
		return map().isEmpty();
	}

	/**
	 * Pseudo property of a {@link Map}.
	 */
	public static class Key extends InspectProperty {

		private final InspectorTreeNode _node;

		private final Object _key;

		/**
		 * Creates a {@link Key}.
		 *
		 * @param key
		 *        See {@link #getValue()}.
		 */
		public Key(InspectorTreeNode node, Object key) {
			_node = node;
			_key = key;
		}

		@Override
		public Object getValue() {
			return _key;
		}
		/**
		 * The underlying node.
		 */
		public InspectorTreeNode getNode() {
			return _node;
		}

		@Override
		public String name() {
			return "[" + InspectorNodeResourceProvider.toString(_node, _key) + "]";
		}

		@Override
		public Object staticType() {
			return null;
		}

	}

}
