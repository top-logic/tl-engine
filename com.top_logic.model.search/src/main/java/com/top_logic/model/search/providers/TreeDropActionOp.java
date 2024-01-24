/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.dnd.DropActionOp;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.search.expr.query.Args;

/**
 * {@link DropActionOp} for dropping an object in a {@link TreeData tree} with a TL-Script based
 * {@link TreeDropTarget drop handler}.
 * 
 * <p>
 * The drop handler has to be a {@link TreeDropTargetByExpression} handler.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TreeDropActionOp<C extends TreeDropActionOp.TreeDropAction> extends DropActionOp<C> {

	/** {@link ApplicationAction} for the {@link TreeDropActionOp}. */
	public interface TreeDropAction extends DropActionOp.DropAction {

		@Override
		@ClassDefault(TreeDropActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * The business object that marks the drop position.
		 * 
		 * @see OntoTreeDropByExpression.Config#getHandleDrop()
		 * @see OrderedTreeDropByExpression.Config#getHandleDrop()
		 */
		@Override
		ModelName getDropPosition();

		/**
		 * Builds a {@link TreeDropAction}.
		 * <p>
		 * See
		 * {@link com.top_logic.layout.dnd.DropActionOp.DropAction#fill(DropActionOp.DropAction, Object, Object, Object, Function)}
		 * for the parameters.
		 * </p>
		 */
		static TreeDropAction create(TreeData dropView, Object dragView, TLTreeNode<?> dropPosition,
				Function<Object, ModelName> dragData) {
			TreeDropAction action = TypedConfiguration.newConfigItem(TreeDropAction.class);
			DropActionOp.DropAction.fill(action, dropView, dragView, dropPosition, dragData);
			return action;
		}

	}

	/** {@link TypedConfiguration} constructor for {@link TreeDropActionOp}. */
	public TreeDropActionOp(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void processDrop(ActionContext context, Object dropView, Object dropPosition, Object droppedObject) {
		TreeData dropTree = (TreeData) dropView;
		TreeDropTarget dropHandler = dropTree.getDropTarget();
		processDrop(context, dropTree, dropHandler, businessObjectForPosition(dropPosition), droppedObject);
	}

	/**
	 * If the given <code>dropPosition</code> is a {@link TLTreeNode} its business object is
	 * returned, otherwise the given drop position itself.
	 */
	protected Object businessObjectForPosition(Object dropPosition) {
		if (dropPosition instanceof TLTreeNode<?>) {
			return ((TLTreeNode<?>) dropPosition).getBusinessObject();
		}
		return dropPosition;
	}

	/** Performs the drop into the given tree. */
	protected void processDrop(ActionContext context, TreeData dropTree, TreeDropTarget javaDropHandler,
			Object dropPosition, Object droppedObject) {
		assertTLScriptDrop(javaDropHandler);
		TreeDropTargetByExpression tlScriptDropHandler = (TreeDropTargetByExpression) javaDropHandler;
		assertDropEnabled(dropTree, tlScriptDropHandler);
		List<?> droppedObjects = CollectionUtilShared.asList(droppedObject);
		assertDropPossible(context, dropTree, tlScriptDropHandler, dropPosition, droppedObjects);

		tlScriptDropHandler.handleDrop(droppedObjects, toDropArguments(context, dropTree, dropPosition));
	}

	private void assertTLScriptDrop(TreeDropTarget dropHandler) {
		if (!(dropHandler instanceof TreeDropTargetByExpression)) {
			fail("The drop handler has to be TL-Script based, i.e. it has to be a "
				+ TreeDropTargetByExpression.class.getSimpleName() + ". But it is: " + debug(dropHandler));
		}
	}

	private void assertDropEnabled(TreeData dropTree, TreeDropTargetByExpression dropHandler) {
		if (!dropHandler.dropEnabled(dropTree)) {
			fail("Dropping is here not possible. Context:" + dropTree.getOwner());
		}
	}

	private void assertDropPossible(ActionContext context, TreeData dropTree, TreeDropTargetByExpression dropHandler,
			Object dropPosition, Collection<?> droppedObjects) {
		if (!dropHandler.canDrop(droppedObjects, toDropArguments(context, dropTree, dropPosition))) {
			fail("The object cannot be droppped here."
				+ " Dropped object: " + droppedObjects + ". Drop position: " + dropPosition);
		}
	}

	/**
	 * The arguments to pass to TL-Script expressions.
	 * 
	 * @param context
	 *        For subclasses that need to resolve further arguments.
	 * @param dropTree
	 *        For subclasses that need to resolve further arguments and need the tree as the value
	 *        context.
	 */
	protected Args toDropArguments(ActionContext context, TreeData dropTree, Object dropPosition) {
		return Args.some(dropPosition);
	}

}
