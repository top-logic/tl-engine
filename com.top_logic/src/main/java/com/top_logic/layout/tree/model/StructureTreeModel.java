/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Objects;

import com.top_logic.basic.util.Utils;

/**
 * {@link TLTreeModel} with nodes that are equal if their business objects are equal.
 * 
 * @see DefaultMutableTLTreeModel
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructureTreeModel extends AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> {

	/**
	 * Creates a {@link StructureTreeModel}.
	 * 
	 * @param rootUserObject
	 *        The {@link TLTreeNode#getBusinessObject()} of the model's {@link #getRoot()} node.
	 */
	public StructureTreeModel(Object rootUserObject) {
		super(Builder.INSTANCE, rootUserObject);
	}

	/**
	 * {@link TreeBuilder} creating {@link Node}s.
	 */
	public static class Builder extends DefaultMutableTreeNodeBuilder {

		/**
		 * Singleton {@link StructureTreeModel.Builder} instance.
		 */
		@SuppressWarnings("hiding")
		public static final TreeBuilder<DefaultMutableTLTreeNode> INSTANCE = new Builder();

		private Builder() {
			// Singleton constructor.
		}

		@Override
		public DefaultMutableTLTreeNode createNode(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model,
				DefaultMutableTLTreeNode parent, Object userObject) {
			return new Node(model, parent, userObject);
		}
	}

	/**
	 * {@link MutableTLTreeNode} implementing {@link #equals(Object)} and {@link #hashCode()} based
	 * on equal {@link #getBusinessObject()}s.
	 */
	public static class Node extends DefaultMutableTLTreeNode {

		/**
		 * Creates a {@link Node}.
		 * 
		 * @param model
		 *        See {@link #getModel()}.
		 * @param parent
		 *        See {@link #getParent()}.
		 * @param businessObject
		 *        See {@link #getBusinessObject()}.
		 */
		public Node(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model, DefaultMutableTLTreeNode parent,
				Object businessObject) {
			super(model, parent, businessObject);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj instanceof Node) {
				return equalsNode((Node) obj);
			}
			return false;
		}

		/**
		 * Equality of {@link Node}s.
		 */
		private boolean equalsNode(Node other) {
			return Utils.equals(getBusinessObject(), other.getBusinessObject());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(getBusinessObject());
		}

	}

}
