/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropEvent.Position;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.providers.OrderedTreeDropActionOp.OrderedTreeDropAction;

/**
 * {@link TreeDropTarget} occurs before, after or within the referenced target node itself.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "tree" })
@Label("Custom drop beside or in tree node")
public class OrderedTreeDropByExpression extends TreeDropTargetByExpression {

	/** {@link ConfigurationItem} for the {@link OrderedTreeDropByExpression}. */
	public interface Config extends TreeDropTargetByExpression.Config {

		/**
		 * Function executing a drop of dragged elements just before a reference node.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument, the parent of the
		 * referenced node as second argument and the referenced node itself as third argument.
		 * </p>
		 * 
		 * <p>
		 * The referenced node is the node before the dragged elements are dropped. If the
		 * referenced node is <code>null</code>, then the dragged elements are dropped at the end of
		 * the referenced parent's child nodes.
		 * </p>
		 * 
		 * <p>
		 * The value returned from the function is passed to a potential
		 * {@link #getPostCreateActions() post-drop actions}.
		 * </p>
		 */
		@Override
		Expr getHandleDrop();

		/**
		 * Function checking if a drop before a reference node can be performed.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument, the parent of the
		 * referenced node as second argument and the referenced node itself as third argument.
		 * </p>
		 * 
		 * <p>
		 * The referenced node is the node before the dragged elements are dropped. If the
		 * referenced node is <code>null</code>, then the dragged elements are dropped at the end of
		 * the referenced parent's child nodes.
		 * </p>
		 */
		@Override
		Expr getCanDrop();

	}

	/**
	 * Creates a {@link OrderedTreeDropByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public OrderedTreeDropByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public DropType getDropType() {
		return DropType.ORDERED;
	}

	@Override
	protected OrderedTreeDropAction record(TreeDropEvent event) {
		TreeData table = event.getTarget();
		Object dragSource = event.getSource();
		TLTreeNode<?> dropPositionParent = getDropPositionParentNode(event);
		TLTreeNode<?> dropPosition = getDropPositionNode(event);
		return OrderedTreeDropAction.create(table, dragSource, dropPositionParent, dropPosition,
			event.getDragDataName());
	}

	@Override
	public boolean canDrop(TreeDropEvent event) {
		TLTreeNode<?> parent = getReferenceParentNode(event, (TLTreeNode<?>) event.getRefNode());

		if (parent == null) {
			return false;
		}

		return super.canDrop(event);
	}

	private TLTreeNode<?> getReferenceParentNode(TreeDropEvent event, TLTreeNode<?> referenceNode) {
		if (event.getPos() == Position.WITHIN) {
			return referenceNode;
		}

		return referenceNode.getParent();
	}

	@Override
	protected Args getDropArguments(TreeDropEvent event) {
		Object referenceParent = getDropPositionParent(event);
		Object reference = getDropPosition(event);
		return Args.some(referenceParent, reference);
	}

	private Object getDropPositionParent(TreeDropEvent event) {
		return getDropPositionParentNode(event).getBusinessObject();
	}

	private TLTreeNode<?> getDropPositionParentNode(TreeDropEvent event) {
		TLTreeNode<?> node = (TLTreeNode<?>) event.getRefNode();
		return getReferenceParentNode(event, node);
	}

	private Object getDropPosition(TreeDropEvent event) {
		TLTreeNode<?> referenceNode = getDropPositionNode(event);
		if (referenceNode == null) {
			return null;
		}
		return referenceNode.getBusinessObject();
	}

	private TLTreeNode<?> getDropPositionNode(TreeDropEvent event) {
		TLTreeNode<?> node = (TLTreeNode<?>) event.getRefNode();
		Position position = event.getPos();
		if (position == Position.ABOVE) {
			return node;
		} else if (position == Position.BELOW) {
			TLTreeNode<?> nextSibling = getNextSibling(node);
			return nextSibling != null ? nextSibling : null;
		} else {
			return null;
		}
	}

	private TLTreeNode<?> getNextSibling(TLTreeNode<?> node) {
		TLTreeNode<?> parent = node.getParent();
		List<?> children = parent.getChildren();
		int childIndex = children.indexOf(node);

		if (childIndex + 1 < children.size()) {
			return ((TLTreeNode<?>) children.get(childIndex + 1));
		} else {
			return null;
		}
	}
}
