/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * A {@link UIElement} that builds a {@link ReactTreeControl} from a {@link DesignTreeNode}
 * hierarchy.
 *
 * <p>
 * Unlike {@link com.top_logic.layout.view.element.TreeElement}, which uses TL-Script expressions to
 * compute children, this element reads the tree structure directly from the {@link DesignTreeNode}
 * model. The input channel provides the root {@link DesignTreeNode}, and the selection channel
 * receives the currently selected node.
 * </p>
 */
public class DesignerTreeElement implements UIElement {

	/**
	 * Configuration for {@link DesignerTreeElement}.
	 */
	@TagName("design-tree")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(DesignerTreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the root {@link DesignTreeNode}.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional reference to a channel that receives the selected {@link DesignTreeNode}.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	/**
	 * Creates a new {@link DesignerTreeElement} from configuration.
	 */
	@CalledByReflection
	public DesignerTreeElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve the input channel providing the root DesignTreeNode.
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());
		DesignTreeNode rootNode = (DesignTreeNode) inputChannel.get();

		// 2. Build the tree model.
		TreeBuilder<DefaultTreeUINode> builder = createTreeBuilder();
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, rootNode);
		treeModel.setRootVisible(true);

		// 3. Create selection model.
		DefaultSingleSelectionModel<Object> selectionModel =
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// 4. Create the ReactTreeControl with a designer-specific label provider that renders each
		//    DesignTreeNode's display label and JavaDoc tooltip.
		ReactTreeControl treeControl =
			new ReactTreeControl(context, treeModel, selectionModel, DESIGN_NODE_CONTROL_PROVIDER);

		// 5. Wire selection: push selected DesignTreeNode to the selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			selectionModel.addSelectionListener(new SelectionListener<>() {
				@Override
				public void notifySelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
					Set<?> newSelection = event.getNewSelection();
					if (newSelection.size() == 1) {
						Object selected = newSelection.iterator().next();
						if (selected instanceof DefaultTreeUINode treeNode) {
							selectionChannel.set(treeNode.getBusinessObject());
						} else {
							selectionChannel.set(selected);
						}
					} else if (newSelection.isEmpty()) {
						selectionChannel.set(null);
					}
				}
			});
		}

		// 6. Wire context menu for structural editing commands.
		ChannelRef selRefForMenu = _config.getSelection();
		ViewChannel selChannelForMenu = selRefForMenu != null ? context.resolveChannel(selRefForMenu) : null;
		treeControl.setContextMenuProvider(
			(tree, node, x, y) -> openDesignContextMenu(tree, node, x, y, builder, selectionModel,
				selChannelForMenu, inputChannel));

		// 7. Listen on the input channel for root changes (e.g. after Revert) and rebuild tree.
		inputChannel.addListener((sender, oldValue, newValue) -> {
			if (newValue instanceof DesignTreeNode newRoot) {
				DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, newRoot);
				newTreeModel.setRootVisible(true);
				treeControl.setTreeModel(newTreeModel);
			}
		});

		return treeControl;
	}

	private static final String CMD_ADD_CHILD = "addChild";

	private static final String CMD_REMOVE = "remove";

	private static final String CMD_MOVE_UP = "moveUp";

	private static final String CMD_MOVE_DOWN = "moveDown";

	/**
	 * Opens a context menu with structural editing commands for the given tree node.
	 */
	private void openDesignContextMenu(ReactTreeControl tree, Object node, int x, int y,
			TreeBuilder<DefaultTreeUINode> builder,
			DefaultSingleSelectionModel<Object> selectionModel,
			ViewChannel selectionChannel, ViewChannel inputChannel) {

		DesignTreeNode designNode;
		if (node instanceof DefaultTreeUINode treeNode) {
			Object bo = treeNode.getBusinessObject();
			if (bo instanceof DesignTreeNode dn) {
				designNode = dn;
			} else {
				return;
			}
		} else {
			return;
		}

		// Build menu items based on what operations are possible.
		List<Map<String, Object>> items = new ArrayList<>();

		if (AddChildCommand.canExecute(designNode)) {
			items.add(menuItem(CMD_ADD_CHILD, "Add Child"));
		}

		if (RemoveElementCommand.canExecute(designNode)) {
			items.add(menuItem(CMD_REMOVE, "Remove"));
		}

		if (MoveElementCommand.canExecute(designNode, MoveElementCommand.Direction.UP)) {
			items.add(menuItem(CMD_MOVE_UP, "Move Up"));
		}

		if (MoveElementCommand.canExecute(designNode, MoveElementCommand.Direction.DOWN)) {
			items.add(menuItem(CMD_MOVE_DOWN, "Move Down"));
		}

		if (items.isEmpty()) {
			return;
		}

		tree.openContextMenu(items, itemId -> {
			handleContextMenuAction(itemId, designNode, tree, builder, selectionModel, selectionChannel,
				inputChannel);
		}, x, y);
	}

	/**
	 * Handles the selection of a context menu item.
	 */
	private void handleContextMenuAction(String itemId, DesignTreeNode designNode, ReactTreeControl tree,
			TreeBuilder<DefaultTreeUINode> builder,
			DefaultSingleSelectionModel<Object> selectionModel,
			ViewChannel selectionChannel, ViewChannel inputChannel) {

		switch (itemId) {
			case CMD_ADD_CHILD:
				AddChildCommand.execute(designNode);
				break;
			case CMD_REMOVE:
				RemoveElementCommand.execute(designNode);
				// Clear selection since the removed node is no longer valid.
				selectionModel.clear();
				if (selectionChannel != null) {
					selectionChannel.set(null);
				}
				break;
			case CMD_MOVE_UP:
				MoveElementCommand.execute(designNode, MoveElementCommand.Direction.UP);
				break;
			case CMD_MOVE_DOWN:
				MoveElementCommand.execute(designNode, MoveElementCommand.Direction.DOWN);
				break;
			default:
				return;
		}

		// Rebuild the tree model from the (modified) root DesignTreeNode.
		DesignTreeNode root = (DesignTreeNode) inputChannel.get();
		DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, root);
		tree.setTreeModel(newTreeModel);
	}

	private static Map<String, Object> menuItem(String id, String label) {
		Map<String, Object> item = new HashMap<>();
		item.put("id", id);
		item.put("label", label);
		return item;
	}

	/**
	 * Provider that renders a {@link DesignTreeNode} as a {@link ReactTextControl}, using the
	 * node's {@link DesignTreeNode#getDisplayLabel() display label} and
	 * {@link DesignTreeNode#getTooltipHtml() tooltip HTML}.
	 */
	private static final ReactControlProvider DESIGN_NODE_CONTROL_PROVIDER = (context, model) -> {
		Object target = model instanceof DefaultTreeUINode node ? node.getBusinessObject() : model;
		if (target instanceof DesignTreeNode designNode) {
			String label = designNode.getDisplayLabel();
			ReactTextControl control = new ReactTextControl(context, label);
			String tooltip = designNode.getTooltipHtml();
			if (tooltip != null && !tooltip.isEmpty()) {
				control.setTooltip(tooltip, label, true);
			}
			return control;
		}
		return new ReactTextControl(context, String.valueOf(model));
	};

	private TreeBuilder<DefaultTreeUINode> createTreeBuilder() {
		return new TreeBuilder<>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Object businessObj = node.getBusinessObject();
				if (businessObj instanceof DesignTreeNode designNode) {
					List<DesignTreeNode> designChildren = designNode.getChildren();
					List<DefaultTreeUINode> children = new ArrayList<>(designChildren.size());
					for (DesignTreeNode child : designChildren) {
						DefaultTreeUINode childNode = createNode(node.getModel(), node, child);
						if (childNode != null) {
							children.add(childNode);
						}
					}
					return children;
				}
				return Collections.emptyList();
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		};
	}
}
