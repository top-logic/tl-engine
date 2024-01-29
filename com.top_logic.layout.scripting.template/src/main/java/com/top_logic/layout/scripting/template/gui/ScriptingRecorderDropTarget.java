/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import static com.top_logic.layout.tree.model.TLTreeModelUtil.*;
import static java.util.Objects.*;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropEvent.Position;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Drop handling for the {@link ScriptRecorderTree}.
 *
 * @author <a href=mailto:Jens.Schaefer@top-logic.com>Jens Schäfer</a>
 */
public class ScriptingRecorderDropTarget implements TreeDropTarget {

	/** {@link ConfigurationItem} for the {@link ScriptingRecorderDropTarget}. */
	public interface Config extends PolymorphicConfiguration<ScriptingRecorderDropTarget> {
		// Nothing needed, yet.
	}

	private ScriptRecorderTree _scriptRecorderTree;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptingRecorderDropTarget}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptingRecorderDropTarget(InstantiationContext context, Config config) {
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, this::initScriptRecorderTree);
	}

	private ScriptRecorderTree initScriptRecorderTree(LayoutComponent value) {
		return _scriptRecorderTree = (ScriptRecorderTree) requireNonNull(value);
	}

	@Override
	public void handleDrop(TreeDropEvent event) {
		for (Object droppedObject : event.getData()) {
			DefaultMutableTLTreeNode target = (DefaultMutableTLTreeNode) event.getRefNode();
			DefaultMutableTLTreeNode source = (DefaultMutableTLTreeNode) droppedObject;
			if (isImpossibleMove(source, target)) {
				break;
			}
			boolean movedSelected = isSelection(source);
			// Delete the node that was dragged
			removeNodeFromSource(source);
			// Insert the dragged node at the desired target position
			insertNodeAtTarget(event.getPos(), source, target);
			updateSelection(source, movedSelected);
		}
	}

	private boolean isImpossibleMove(DefaultMutableTLTreeNode source, DefaultMutableTLTreeNode target) {
		if (Utils.equals(source, target)) {
			return true; // must not try to move a node before itself
		}
		if (target.getParent() == null) {
			return true; // must not try to move a node before root
		}
		if (createPathToRoot(target).contains(source)) {
			return true; // It is not possible to move a node into its own subtree.
		}
		return false;
	}

	private boolean isSelection(DefaultMutableTLTreeNode source) {
		return getScriptRecorderTree().getTreeData().getSelectionModel().isSelected(source);
	}

	private void removeNodeFromSource(DefaultMutableTLTreeNode source) {
		DefaultMutableTLTreeNode sourceParent = source.getParent();
		int sourceIndex = sourceParent.getIndex(source);
		List<ApplicationAction> sourceActions = getChildActions(sourceParent);
		sourceActions.remove(ScriptRecorderTree.action(source));
		sourceParent.removeChild(sourceIndex);
	}

	private List<ApplicationAction> getChildActions(DefaultMutableTLTreeNode sourceParent) {
		return ((ActionChain) ScriptRecorderTree.action(sourceParent)).getActions();
	}

	private void insertNodeAtTarget(Position position, DefaultMutableTLTreeNode source,
			DefaultMutableTLTreeNode target) {
		DefaultMutableTLTreeNode targetParent = target.getParent();
		int targetIndex = targetParent.getIndex(target);
		switch (position) {
			case ABOVE: {
				// Insert the removed node before target
				updateTree(targetParent, IndexPosition.before(targetIndex), source);
				return;
			}
			case ONTO:
			case WITHIN: {
				if (ScriptRecorderTree.action(target) instanceof ActionChain) {
					// Insert the node as child of target
					updateTree(target, IndexPosition.before(0), source);
				} else {
					// Insert the node after target
					updateTree(targetParent, IndexPosition.after(targetIndex), source);
				}
				return;
			}
			case BELOW: {
				// Insert the node after target
				updateTree(targetParent, IndexPosition.after(targetIndex), source);
				return;
			}
			default: {
				throw new UnreachableAssertion("Unexpected position: " + position);
			}

		}
	}

	private void updateTree(DefaultMutableTLTreeNode newParent, IndexPosition insertPosition,
			DefaultMutableTLTreeNode draggedNode) {
		newParent.addChild(insertPosition, draggedNode);
		ApplicationAction draggedAction = ScriptRecorderTree.action(draggedNode);
		getScriptRecorderTree().updateActionChain(newParent, insertPosition, draggedAction, draggedNode);
	}

	private void updateSelection(DefaultMutableTLTreeNode source, boolean movedSelected) {
		if (movedSelected) {
			getScriptRecorderTree().getTreeData().getSelectionModel().setSelected(source, true);
		}
	}

	private ScriptRecorderTree getScriptRecorderTree() {
		return _scriptRecorderTree;
	}

}
