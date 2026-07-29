/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.configedit.ConfigTypeChoice;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

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

		// Holder for the tree model currently displayed by treeControl. The control itself does not
		// expose a getter for its current model, and the model is replaced (not mutated) whenever the
		// tree is rebuilt (e.g. after Revert, see the input-channel listener below, or after a
		// context-menu structural edit). Listeners that need to search the *currently displayed* tree
		// must consult this holder instead of a captured local, which would go stale after a rebuild.
		DefaultTreeUINodeModel[] currentModel = { treeModel };

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

			// Reflect an externally set selection (e.g. from the "select view" picker) in the tree.
			selectionChannel.addListener((sender, oldValue, newValue) -> {
				if (newValue instanceof DesignTreeNode target) {
					DefaultTreeUINode uiNode = findUINode(currentModel[0].getRoot(), target);
					if (uiNode != null) {
						revealNode(uiNode);
						selectionModel.setSelected(uiNode, true);
						// Push the server-side expansion+selection change to the client. Unlike a
						// client-initiated select/expand (which flows through the control's own
						// command handlers that rebuild this state), this change is made directly on
						// the models, so the control's visible node state must be rebuilt explicitly.
						treeControl.updateVisibleState();
					}
				}
			});
		}

		// 6. Wire context menu for structural editing commands.
		ChannelRef selRefForMenu = _config.getSelection();
		ViewChannel selChannelForMenu = selRefForMenu != null ? context.resolveChannel(selRefForMenu) : null;
		installContextMenu(context, treeControl, builder, selectionModel, selChannelForMenu, inputChannel,
			currentModel);

		// 7. Listen on the input channel for root changes (e.g. after Revert) and rebuild tree.
		ViewChannel.ChannelListener rootListener = (sender, oldValue, newValue) -> {
			if (newValue instanceof DesignTreeNode newRoot) {
				DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, newRoot);
				newTreeModel.setRootVisible(true);
				treeControl.setTreeModel(newTreeModel);
				currentModel[0] = newTreeModel;
			}
		};
		inputChannel.addListener(rootListener);
		treeControl.addCleanupAction(() -> inputChannel.removeListener(rootListener));

		return treeControl;
	}

	/** Clique of the element-creating commands. */
	private static final String CLIQUE_ADD = "add";

	/** Clique of the commands changing an element's position. */
	private static final String CLIQUE_ORDER = "order";

	/**
	 * Builds the structural editing commands and opens them as a context menu on right-click.
	 *
	 * <p>
	 * The commands are built once and read the right-clicked node from the target published by the
	 * {@link ContextMenuContribution}, so their executability is evaluated against the node the menu
	 * is opened for.
	 * </p>
	 */
	private void installContextMenu(ViewContext context, ReactTreeControl treeControl,
			TreeBuilder<DefaultTreeUINode> builder, DefaultSingleSelectionModel<Object> selectionModel,
			ViewChannel selectionChannel, ViewChannel inputChannel, DefaultTreeUINodeModel[] currentModel) {

		ContextMenuOpener opener = context.getContextMenuOpener();
		if (opener == null) {
			// Without an overlay to render into, no context menu can be shown. The tree stays
			// usable for navigation.
			Logger.warn("No context menu overlay available; structural editing is not offered.",
				DesignerTreeElement.class);
			return;
		}

		// The node the menu is currently opened for, published through the contribution's target.
		DesignTreeNode[] target = new DesignTreeNode[1];

		// The position of the last opened menu, reused for the element-type menu.
		int[] menuPosition = new int[2];

		Runnable rebuild = () -> {
			DesignTreeNode root = (DesignTreeNode) inputChannel.get();
			DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, root);
			treeControl.setTreeModel(newTreeModel);
			currentModel[0] = newTreeModel;
		};

		List<CommandModel> commands = List.of(
			SimpleCommandModel.create("designerAddChild", Resources.getInstance().getString(
				I18NConstants.DESIGNER_ADD_CHILD),
				ctx -> addChild(target[0], opener, menuPosition, selectionModel, selectionChannel,
					currentModel, rebuild))
				.setClique(CLIQUE_ADD)
				.setExecutable(() -> AddChildCommand.canExecute(target[0])),

			SimpleCommandModel.create("designerRemove", Resources.getInstance().getString(
				I18NConstants.DESIGNER_REMOVE),
				ctx -> remove(target[0], selectionModel, selectionChannel, rebuild))
				.setClique(CLIQUE_ORDER)
				.setExecutable(() -> RemoveElementCommand.canExecute(target[0])),

			SimpleCommandModel.create("designerMoveUp", Resources.getInstance().getString(
				I18NConstants.DESIGNER_MOVE_UP),
				ctx -> move(target[0], MoveElementCommand.Direction.UP, rebuild))
				.setClique(CLIQUE_ORDER)
				.setExecutable(() -> MoveElementCommand.canExecute(target[0], MoveElementCommand.Direction.UP)),

			SimpleCommandModel.create("designerMoveDown", Resources.getInstance().getString(
				I18NConstants.DESIGNER_MOVE_DOWN),
				ctx -> move(target[0], MoveElementCommand.Direction.DOWN, rebuild))
				.setClique(CLIQUE_ORDER)
				.setExecutable(() -> MoveElementCommand.canExecute(target[0], MoveElementCommand.Direction.DOWN)));

		ContextMenuContribution contribution =
			new ContextMenuContribution(value -> target[0] = (DesignTreeNode) value, commands);

		treeControl.setContextMenuProvider((tree, node, x, y) -> {
			DesignTreeNode designNode = designNodeOf(node);
			if (designNode == null) {
				return;
			}

			// Right-click also selects, so that the configuration editor shows the node the menu
			// acts on.
			if (node instanceof DefaultTreeUINode uiNode) {
				selectionModel.setSelected(uiNode, true);
				tree.updateVisibleState();
			}

			// Remember where the menu was opened, so that a follow-up type menu appears there too.
			menuPosition[0] = x;
			menuPosition[1] = y;
			opener.open(x, y, List.of(new Targeted(contribution, designNode)));
		});
	}

	/**
	 * Adds a child element to the given parent, asking for the element type when the target property
	 * accepts more than one.
	 */
	private HandlerResult addChild(DesignTreeNode parent, ContextMenuOpener opener, int[] menuPosition,
			DefaultSingleSelectionModel<Object> selectionModel, ViewChannel selectionChannel,
			DefaultTreeUINodeModel[] currentModel, Runnable rebuild) {

		ConfigTypeChoice types = ConfigTypeChoice.of(parent.getChildContainer());
		if (types.isUnique()) {
			return createChild(parent, types.single(), selectionModel, selectionChannel, currentModel, rebuild);
		}

		// Offer the element types as a second menu at the position of the first one.
		List<ConfigTypeChoice.Choice> choices = types.choices();
		List<CommandModel> typeCommands = new ArrayList<>(choices.size());
		for (ConfigTypeChoice.Choice choice : choices) {
			typeCommands.add(SimpleCommandModel.create(null, choice.label(),
				ctx -> createChild(parent, choice.option(), selectionModel, selectionChannel, currentModel,
					rebuild)));
		}
		opener.open(menuPosition[0], menuPosition[1],
			List.of(new Targeted(new ContextMenuContribution(value -> {
				// The commands carry their target, nothing to publish.
			}, typeCommands), parent)));
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult createChild(DesignTreeNode parent, Object typeOption,
			DefaultSingleSelectionModel<Object> selectionModel, ViewChannel selectionChannel,
			DefaultTreeUINodeModel[] currentModel, Runnable rebuild) {

		DesignTreeNode child = AddChildCommand.execute(parent, typeOption);
		if (child == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		rebuild.run();

		// Select the new element, so its properties are ready to be edited.
		DefaultTreeUINode uiNode = findUINode(currentModel[0].getRoot(), child);
		if (uiNode != null) {
			revealNode(uiNode);
			selectionModel.setSelected(uiNode, true);
		} else if (selectionChannel != null) {
			selectionChannel.set(child);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult remove(DesignTreeNode node, DefaultSingleSelectionModel<Object> selectionModel,
			ViewChannel selectionChannel, Runnable rebuild) {
		if (RemoveElementCommand.execute(node)) {
			// Clear selection since the removed node is no longer valid.
			selectionModel.clear();
			if (selectionChannel != null) {
				selectionChannel.set(null);
			}
			rebuild.run();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult move(DesignTreeNode node, MoveElementCommand.Direction direction, Runnable rebuild) {
		if (MoveElementCommand.execute(node, direction)) {
			rebuild.run();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The {@link DesignTreeNode} displayed by the given tree node, or {@code null} if the node does
	 * not represent one.
	 */
	private static DesignTreeNode designNodeOf(Object node) {
		if (node instanceof DefaultTreeUINode treeNode) {
			return treeNode.getBusinessObject() instanceof DesignTreeNode designNode ? designNode : null;
		}
		return node instanceof DesignTreeNode designNode ? designNode : null;
	}

	/**
	 * Finds the {@link DefaultTreeUINode} whose business object is {@code target}. Returns
	 * {@code null} if not found.
	 *
	 * <p>
	 * Does not expand any node: {@link DefaultTreeUINode#getChildren()} materializes children
	 * lazily regardless of expansion state, so the search does not need to expand anything. Use
	 * {@link #revealNode(DefaultTreeUINode)} to expand the ancestors of a found node.
	 * </p>
	 */
	private static DefaultTreeUINode findUINode(DefaultTreeUINode node, DesignTreeNode target) {
		if (node.getBusinessObject() == target) {
			return node;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			DefaultTreeUINode found = findUINode(child, target);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Expands all ancestors of the given node so it becomes visible in the tree, without expanding
	 * the node itself.
	 */
	private static void revealNode(DefaultTreeUINode node) {
		for (DefaultTreeUINode parent = node.getParent(); parent != null; parent = parent.getParent()) {
			parent.setExpanded(true);
		}
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
				control.setTooltip(tooltip, label, false);
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
