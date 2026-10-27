/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.window.ElementPicker;
import com.top_logic.layout.react.window.PickKind;
import com.top_logic.layout.react.window.PickResult;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Toolbar command that starts "select view" mode in the main application window. The user then
 * clicks a view there; the matching design-tree node is selected in the designer.
 *
 * <p>
 * A view the design tree does not show yet - one the application opened from a path it computed,
 * for instance - is added to the tree in the group "Further views" below its root.
 * </p>
 *
 * @see ElementPicker
 * @see #findOrAddViewRoot(DesignTreeNode, String)
 */
public class SelectViewCommand implements ViewCommand {

	/** Channel name holding the main application window's {@link ReactContext}. */
	private static final String APP_CONTEXT = "appContext";

	/**
	 * Configuration for {@link SelectViewCommand}.
	 */
	@TagName("select-view")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getDesignTree()}. */
		String DESIGN_TREE = "design-tree";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(SelectViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Reference to the channel containing the root {@link DesignTreeNode}. */
		@Name(DESIGN_TREE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getDesignTree();

		/** Reference to the channel receiving the selected {@link DesignTreeNode}. */
		@Name(SELECTION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	/**
	 * Creates a new {@link SelectViewCommand}.
	 */
	@CalledByReflection
	public SelectViewCommand(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		if (!viewContext.hasChannel(APP_CONTEXT)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		Object appContextValue = viewContext.resolveChannel(new ChannelRef(APP_CONTEXT)).get();
		if (!(appContextValue instanceof ReactContext mainWindowContext)) {
			return HandlerResult.DEFAULT_RESULT;
		}

		String designerWindowId = context.getWindowName();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewChannel designTreeChannel = viewContext.resolveChannel(_config.getDesignTree());
		ViewChannel selectionChannel = viewContext.resolveChannel(_config.getSelection());

		Consumer<PickResult> onPicked = result -> {
			String path = ((PickResult.ViewPicked) result).sourcePath();
			Object root = designTreeChannel.get();
			if (root instanceof DesignTreeNode rootNode) {
				DesignTreeNode node = findOrAddViewRoot(rootNode, path);
				if (node != null) {
					selectionChannel.set(node);
					// The user picked the view in the application window, so the designer is now behind
					// it. Bring it back to the front to show the view that was selected there.
					registry.focusWindow(designerWindowId);
				}
			}
		};
		ElementPicker.start(context, mainWindowContext, PickKind.VIEW, onPicked);

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The view-root node for the given source file, adding the view to the tree if the tree does not
	 * show it.
	 *
	 * <p>
	 * A view that is not found by {@link #findViewRoot(DesignTreeNode, String)} is loaded and added
	 * to the group {@link I18NConstants#DESIGNER_FURTHER_VIEWS} below the root, which is created when
	 * the first view is added this way. The views the tree already shows are linked from the added
	 * view, not expanded a second time. The {@link DesignTreeNode#addStructureListener(Runnable)
	 * structure listeners} of the root are notified about the addition.
	 * </p>
	 *
	 * @param root
	 *        The root of the design tree.
	 * @param sourceFile
	 *        The full path of the view file.
	 * @return The node the view is expanded to, or {@code null} if the view cannot be loaded.
	 */
	public static DesignTreeNode findOrAddViewRoot(DesignTreeNode root, String sourceFile) {
		DesignTreeNode existing = findViewRoot(root, sourceFile);
		if (existing != null || root == null || sourceFile == null) {
			return existing;
		}

		DesignTreeNode view;
		try {
			ViewElement.Config config = ViewLoader.getOrLoadConfig(sourceFile);
			view = new DesignTreeBuilder().markExpanded(root).build(config, sourceFile);
		} catch (Exception ex) {
			Logger.warn("Select view: the view '" + sourceFile + "' cannot be added to the design tree.", ex,
				SelectViewCommand.class);
			return null;
		}

		DesignTreeBuilder.attach(furtherViews(root), view);
		root.fireStructureChanged();
		return view;
	}

	/**
	 * The group below the given root holding the views added through
	 * {@link #findOrAddViewRoot(DesignTreeNode, String)}, created if it does not exist yet.
	 */
	private static DesignTreeNode furtherViews(DesignTreeNode root) {
		for (DesignTreeNode child : root.getChildren()) {
			if (child instanceof VirtualDesignTreeNode group
				&& I18NConstants.DESIGNER_FURTHER_VIEWS.equals(group.getLabelKey())) {
				return group;
			}
		}
		// The group belongs to no file: it holds views of several files and is never saved.
		DesignTreeNode group = new VirtualDesignTreeNode(I18NConstants.DESIGNER_FURTHER_VIEWS, null);
		DesignTreeBuilder.attach(root, group);
		return group;
	}

	/**
	 * Finds the view-root node for the given source file: the shallowest node whose
	 * {@link DesignTreeNode#getSourceFile()} equals {@code sourceFile} (breadth-first), or
	 * {@code null} if none.
	 *
	 * <p>
	 * Public (rather than merely package-visible) because this project's unit tests live in a
	 * mirrored {@code test.*} package, a distinct top-level package that cannot see
	 * package-private members here.
	 * </p>
	 */
	public static DesignTreeNode findViewRoot(DesignTreeNode root, String sourceFile) {
		if (root == null || sourceFile == null) {
			return null;
		}
		Deque<DesignTreeNode> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			DesignTreeNode node = queue.removeFirst();
			if (sourceFile.equals(node.getSourceFile())) {
				return node;
			}
			queue.addAll(node.getChildren());
		}
		return null;
	}
}
