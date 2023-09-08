/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.dnd.DropActionOp;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.search.expr.query.Args;

/**
 * A specialization of the {@link TreeDropActionOp} for the {@link OrderedTreeDropByExpression}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class OrderedTreeDropActionOp extends TreeDropActionOp<OrderedTreeDropActionOp.OrderedTreeDropAction> {

	/** {@link ApplicationAction} for the {@link OrderedTreeDropActionOp}. */
	public interface OrderedTreeDropAction extends TreeDropActionOp.TreeDropAction {

		/** Property name of {@link #getDropPositionParent()}. */
		String DROP_POSITION_PARENT = "drop-position-parent";

		@Override
		@ClassDefault(OrderedTreeDropActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * The parent of the business object that marks the drop position.
		 * 
		 * @see OrderedTreeDropByExpression.Config#getHandleDrop()
		 */
		@Name(DROP_POSITION_PARENT)
		ModelName getDropPositionParent();

		/** @see #getDropPositionParent() */
		void setDropPositionParent(ModelName dropPositionParent);

		/**
		 * Builds a {@link OrderedTreeDropAction}.
		 * <p>
		 * See
		 * {@link com.top_logic.layout.dnd.DropActionOp.DropAction#fill(DropActionOp.DropAction, Object, Object, Object, Function)}
		 * for the parameters.
		 * </p>
		 * 
		 * @param dropPositionParent
		 *        See: {@link #getDropPositionParent()}
		 */
		static OrderedTreeDropAction create(TreeData dropView, Object dragView, TLTreeNode<?> dropPositionParent,
				TLTreeNode<?> dropPosition, Function<ModelName, ModelName> dragData) {
			OrderedTreeDropAction action = TypedConfiguration.newConfigItem(OrderedTreeDropAction.class);
			DropActionOp.DropAction.fill(action, dropView, dragView, dropPosition, dragData);
			action.setDropPositionParent(ModelResolver.buildModelName(dropView, dropPositionParent));
			return action;
		}

	}

	/** {@link TypedConfiguration} constructor for {@link OrderedTreeDropActionOp}. */
	public OrderedTreeDropActionOp(InstantiationContext context, OrderedTreeDropAction config) {
		super(context, config);
	}

	@Override
	protected Args toDropArguments(ActionContext context, TreeData dropTree, Object dropPosition) {
		Object dropPositionParent = context.resolve(getConfig().getDropPositionParent(), dropTree);
		return Args.some(businessObjectForPosition(dropPositionParent), dropPosition);
	}

}
