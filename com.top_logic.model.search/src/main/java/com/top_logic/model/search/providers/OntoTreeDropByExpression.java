/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.providers.TreeDropActionOp.TreeDropAction;

/**
 * {@link TreeDropTarget} occurs onto referenced target node itself.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "tree" })
@Label("Custom drop onto tree node")
public class OntoTreeDropByExpression extends TreeDropTargetByExpression {

	/** {@link ConfigurationItem} for the {@link OntoTreeDropByExpression}. */
	public interface Config extends TreeDropTargetByExpression.Config {

		/**
		 * Function executing a drop onto the referenced node.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument and the referenced node as
		 * second argument.
		 * </p>
		 * 
		 * <p>
		 * The value returned from the function is passed to potential
		 * {@link #getPostCreateActions() post-drop actions}.
		 * </p>
		 */
		@Override
		Expr getHandleDrop();

		/**
		 * Function checking if a drop onto the referenced node can be performed.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument and the referenced node as
		 * second argument.
		 * </p>
		 */
		@Override
		Expr getCanDrop();

	}

	/**
	 * Creates a {@link OntoTreeDropByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public OntoTreeDropByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public DropType getDropType() {
		return DropType.CHILD;
	}

	@Override
	protected Args getDropArguments(TreeDropEvent event) {
		return Args.some(getDropPositionNode(event).getBusinessObject());
	}

	/** The node at which the drop happened. */
	private TLTreeNode<?> getDropPositionNode(TreeDropEvent event) {
		TLTreeNode<?> node = (TLTreeNode<?>) event.getRefNode();
		return node;
	}

	@Override
	protected TreeDropAction record(TreeDropEvent event) {
		TreeData table = event.getTarget();
		Object dragSource = event.getSource();
		TLTreeNode<?> dropPosition = getDropPositionNode(event);
		return TreeDropAction.create(table, dragSource, dropPosition, event.getDragDataName());
	}

}
