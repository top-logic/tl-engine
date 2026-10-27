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
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
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
import com.top_logic.layout.view.model.NodeLocator;
import com.top_logic.layout.view.model.TreeNodes;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.layout.view.model.TreeSelectionDisplay;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
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
 * model. It shows the tree of the file the {@link DesignSession} of the session channel shows, and
 * the selection channel receives the currently selected node.
 * </p>
 *
 * <p>
 * Selecting a {@link ViewLinkDesignTreeNode} opens the linked file in the session instead of
 * selecting the link. Whenever the session shows another file, the tree displays that file's tree
 * with the nodes expanded the user had expanded when it was displayed last, and selects its root.
 * </p>
 *
 * <p>
 * The rows follow the {@link DesignTreeDisplay}: a property group with a single entry shares the row
 * of that entry. The row stands for the entry - selecting it selects the entry, and selecting the
 * entry reveals the row - and its context menu offers the commands of the group below those of the
 * entry.
 * </p>
 */
public class DesignerTreeElement implements UIElement {

	/**
	 * Configuration for {@link DesignerTreeElement}.
	 */
	@TagName("design-tree")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getSession()}. */
		String SESSION = "session";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(DesignerTreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the {@link DesignSession}.
		 */
		@Name(SESSION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSession();

		/**
		 * Reference to the channel that receives the selected {@link DesignTreeNode}.
		 */
		@Name(SELECTION)
		@Mandatory
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
		// 1. Resolve the session providing the tree to show.
		DesignSession session = (DesignSession) context.resolveChannel(_config.getSession()).get();
		DesignTreeNode rootNode = session.getCurrentTree();

		// 2. Build the tree model.
		TreeBuilder<DefaultTreeUINode> builder = createTreeBuilder();
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, rootNode);
		treeModel.setRootVisible(true);

		// 3. Create selection model.
		DefaultSingleSelectionModel<Object> selectionModel =
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// Holder for the tree model currently displayed by treeControl. The control itself does not
		// expose a getter for its current model, and the model is replaced (not mutated) whenever the
		// tree is rebuilt (e.g. after another file was opened, or after a context-menu structural
		// edit). Listeners that need to search the *currently displayed* tree must consult this holder
		// instead of a captured local, which would go stale after a rebuild.
		DefaultTreeUINodeModel[] currentModel = { treeModel };

		// 4. Create the ReactTreeControl with a designer-specific label provider that renders each
		//    DesignTreeNode's display label and JavaDoc tooltip. The provider needs the control it
		//    renders into to refresh a node whose label changed, so it is handed a holder that is
		//    filled in right after construction.
		ReactTreeControl[] treeRef = new ReactTreeControl[1];
		ReactTreeControl treeControl = new ReactTreeControl(context, treeModel, selectionModel,
			designNodeControlProvider(treeRef, currentModel));
		treeRef[0] = treeControl;
		treeControl.setCssClass(_config.getCssClass());

		// 5. Wire the selection channel, which the tree reads as well as writes: the selected
		//    DesignTreeNode is written to it, and a node another writer puts on it is revealed and
		//    selected in the tree displayed now. A selected link is no node to work on: it opens the
		//    linked file, whose root then becomes the selection.
		ViewChannel selectionChannel = context.resolveChannel(_config.getSelection());
		TreeSelectionDisplay display = TreeSelectionDisplay.intercepting(
			TreeSelectionDisplay.of(treeControl, selectionModel),
			uiNode -> openLink(session, uiNode));
		TreeSelectionBinding selectionBinding =
			new TreeSelectionBinding(display, () -> currentModel[0], DISPLAYED_NODE_LOCATOR, selectionChannel);
		treeControl.addCleanupAction(selectionBinding::dispose);

		// 6. Wire context menu for structural editing commands.
		installContextMenu(context, treeControl, builder, selectionModel, selectionChannel, currentModel);

		// 7. Follow the session to the file it shows, keeping the expansion of every file's tree.
		Map<DesignTreeNode, Set<Object>> expansion = new WeakHashMap<>();
		Runnable sessionListener = () -> {
			DesignTreeNode newRoot = session.getCurrentTree();
			DefaultTreeUINode oldRoot = currentModel[0].getRoot();
			if (oldRoot.getBusinessObject() == newRoot) {
				return;
			}
			expansion.put((DesignTreeNode) oldRoot.getBusinessObject(), TreeNodes.collectExpanded(oldRoot));

			DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, newRoot);
			newTreeModel.setRootVisible(true);
			treeControl.setTreeModel(newTreeModel);
			currentModel[0] = newTreeModel;
			Set<Object> expanded = expansion.get(newRoot);
			if (expanded != null) {
				TreeNodes.restoreExpansion(newTreeModel.getRoot(), expanded);
			} else {
				newTreeModel.getRoot().setExpanded(true);
			}

			// The file is entered at its root. The nodes of the tree displayed before are gone, so the
			// selection is expressed on the nodes displayed now.
			selectionModel.setSelection(Set.of(newTreeModel.getRoot()));
			selectionBinding.structureChanged();
			treeControl.updateVisibleState();
		};
		Registration sessionRegistration = session.addListener(sessionListener);
		treeControl.addCleanupAction(sessionRegistration::dispose);

		return treeControl;
	}

	/**
	 * Opens the file the given node links to, if it is a {@link ViewLinkDesignTreeNode}.
	 *
	 * @return Whether the node is a link, which is not selected itself.
	 */
	private static boolean openLink(DesignSession session, DefaultTreeUINode uiNode) {
		if (designNodeOf(uiNode) instanceof ViewLinkDesignTreeNode link) {
			session.open(link.getTargetFile());
			return true;
		}
		return false;
	}

	/**
	 * Builds the tree model anew from the root of the tree displayed now and displays it, keeping
	 * the parts of the tree the user had opened expanded.
	 *
	 * @return The tree model displayed now.
	 */
	private static DefaultTreeUINodeModel rebuildKeepingExpansion(ReactTreeControl treeControl,
			TreeBuilder<DefaultTreeUINode> builder, DefaultTreeUINodeModel[] currentModel) {
		DefaultTreeUINode oldRoot = currentModel[0].getRoot();
		Set<Object> expanded = TreeNodes.collectExpanded(oldRoot);

		DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, oldRoot.getBusinessObject());
		newTreeModel.setRootVisible(true);
		treeControl.setTreeModel(newTreeModel);
		currentModel[0] = newTreeModel;
		TreeNodes.restoreExpansion(newTreeModel.getRoot(), expanded);
		return newTreeModel;
	}

	/**
	 * Locator finding the row displaying a {@link DesignTreeNode}, which is the row of its single
	 * entry for a group sharing that row, see {@link DesignTreeDisplay#displayedNode(DesignTreeNode)}.
	 */
	private static final NodeLocator DISPLAYED_NODE_LOCATOR =
		(root, businessObject) -> NodeLocator.SEARCHING.locate(root, displayedObject(businessObject));

	/**
	 * The business object of the row displaying the given object.
	 */
	private static Object displayedObject(Object businessObject) {
		return businessObject instanceof DesignTreeNode node ? DesignTreeDisplay.displayedNode(node) : businessObject;
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
			ViewChannel selectionChannel, DefaultTreeUINodeModel[] currentModel) {

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

		// Rebuilds the tree after a structural edit and selects the given node, keeping the parts of
		// the tree the user had opened expanded.
		Consumer<DesignTreeNode> rebuild = toSelect -> {
			DefaultTreeUINodeModel newTreeModel =
				rebuildKeepingExpansion(treeControl, builder, currentModel);

			DefaultTreeUINode uiNode =
				toSelect == null ? null : DISPLAYED_NODE_LOCATOR.locate(newTreeModel.getRoot(), toSelect);
			if (uiNode != null) {
				TreeNodes.revealNode(uiNode);
				selectionModel.setSelected(uiNode, true);
			} else {
				selectionModel.clear();
				if (selectionChannel != null) {
					selectionChannel.set(null);
				}
			}
			treeControl.updateVisibleState();
		};

		List<CommandModel> commands = List.of(
			addChildCommand(target, opener, menuPosition, rebuild),

			SimpleCommandModel.create("designerRemove", Resources.getInstance().getString(
				I18NConstants.DESIGNER_REMOVE),
				ctx -> remove(target[0], rebuild))
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

		// The group sharing the row of its single entry, for which the menu offers the commands of
		// the group's container.
		DesignTreeNode[] groupTarget = new DesignTreeNode[1];
		List<CommandModel> groupCommands = List.of(addChildCommand(groupTarget, opener, menuPosition, rebuild));

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
			List<DesignTreeNode> targets = DesignTreeDisplay.commandTargets(designNode);
			List<Targeted> contributions = new ArrayList<>(targets.size());
			contributions.add(new Targeted(contribution, designNode));
			for (DesignTreeNode groupNode : targets.subList(1, targets.size())) {
				contributions.add(new Targeted(new ContextMenuContribution(
					value -> groupTarget[0] = (DesignTreeNode) value, groupCommands, groupNode.getDisplayLabel()),
					groupNode));
			}
			opener.open(x, y, contributions);
		});
	}

	/**
	 * The command adding an element to the container of the node the menu is opened for.
	 *
	 * @param target
	 *        Holder of the node the menu is opened for.
	 */
	private CommandModel addChildCommand(DesignTreeNode[] target, ContextMenuOpener opener, int[] menuPosition,
			Consumer<DesignTreeNode> rebuild) {
		return SimpleCommandModel.create("designerAddChild", Resources.getInstance().getString(
			I18NConstants.DESIGNER_ADD_CHILD),
			ctx -> addChild(target[0], opener, menuPosition, rebuild))
			.setClique(CLIQUE_ADD)
			.setExecutable(() -> AddChildCommand.canExecute(target[0]));
	}

	/**
	 * Adds a child element to the given parent, asking for the element type when the target property
	 * accepts more than one.
	 */
	private HandlerResult addChild(DesignTreeNode parent, ContextMenuOpener opener, int[] menuPosition,
			Consumer<DesignTreeNode> rebuild) {

		ConfigTypeChoice types = ConfigTypeChoice.of(parent.getChildContainer());
		if (types.isUnique()) {
			return createChild(parent, types.single(), rebuild);
		}

		// Offer the element types as a second menu at the position of the first one.
		List<ConfigTypeChoice.Choice> choices = types.choices();
		List<CommandModel> typeCommands = new ArrayList<>(choices.size());
		for (ConfigTypeChoice.Choice choice : choices) {
			typeCommands.add(SimpleCommandModel.create(null, choice.label(),
				ctx -> createChild(parent, choice.option(), rebuild)));
		}
		opener.open(menuPosition[0], menuPosition[1],
			List.of(new Targeted(new ContextMenuContribution(value -> {
				// The commands carry their target, nothing to publish.
			}, typeCommands), parent)));
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult createChild(DesignTreeNode parent, Object typeOption,
			Consumer<DesignTreeNode> rebuild) {

		DesignTreeNode child = AddChildCommand.execute(parent, typeOption);
		if (child == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		// Select the new element, so its properties are ready to be edited.
		rebuild.accept(child);
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult remove(DesignTreeNode node, Consumer<DesignTreeNode> rebuild) {
		// The removed node is gone, so its parent takes the selection: it is the element the removal
		// was performed on, and keeping it selected leaves the tree where the user was working.
		DesignTreeNode parent = node.getParent();
		if (RemoveElementCommand.execute(node)) {
			rebuild.accept(parent);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult move(DesignTreeNode node, MoveElementCommand.Direction direction,
			Consumer<DesignTreeNode> rebuild) {
		if (MoveElementCommand.execute(node, direction)) {
			rebuild.accept(node);
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
	 * Provider that renders a {@link DesignTreeNode} as a {@link ReactTextControl}, using the
	 * {@link DesignTreeDisplay#displayLabel(DesignTreeNode) label} and
	 * {@link DesignTreeDisplay#tooltipHtml(DesignTreeNode) tooltip HTML} of its row.
	 */
	private static ReactControlProvider designNodeControlProvider(ReactTreeControl[] treeRef,
			DefaultTreeUINodeModel[] currentModel) {
		return (context, model) -> {
			if (model instanceof DesignTreeNode designNode) {
				String label = DesignTreeDisplay.displayLabel(designNode);
				ReactTextControl control = new ReactTextControl(context, label);
				String tooltip = DesignTreeDisplay.tooltipHtml(designNode);
				if (tooltip != null && !tooltip.isEmpty()) {
					control.setTooltip(tooltip, label, false);
				}

				// Re-render the node when an identifying property is edited in the configuration
				// form, so that the tree does not keep showing the previous label. The tree caches
				// its content controls by UI node, so the node displaying the edited object must be
				// looked up in the model currently displayed.
				Runnable labelListener = () -> {
					ReactTreeControl tree = treeRef[0];
					if (tree != null) {
						DefaultTreeUINode uiNode = TreeNodes.findNode(currentModel[0].getRoot(), designNode);
						if (uiNode != null) {
							tree.invalidateNodeControl(uiNode);
						}
						tree.updateVisibleState();
					}
				};
				designNode.addLabelListener(labelListener);
				control.addCleanupAction(() -> designNode.removeLabelListener(labelListener));

				return control;
			}
			return new ReactTextControl(context, String.valueOf(model));
		};
	}

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
					List<DesignTreeNode> designChildren = DesignTreeDisplay.displayedChildren(designNode);
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
