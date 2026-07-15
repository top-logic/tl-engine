/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
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
import com.top_logic.layout.react.protocol.ViewPickEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.PendingViewPick;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Toolbar command that starts "select view" mode in the main application window. The user then
 * clicks a view there; the matching design-tree node is selected in the designer.
 *
 * @see PendingViewPick
 * @see ViewPickEvent
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

		String mainWindowId = mainWindowContext.getWindowName();
		String designerWindowId = context.getWindowName();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		SSEUpdateQueue mainQueue = registry.getQueue(mainWindowId);
		if (mainQueue == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewChannel designTreeChannel = viewContext.resolveChannel(_config.getDesignTree());
		ViewChannel selectionChannel = viewContext.resolveChannel(_config.getSelection());

		String token = UUID.randomUUID().toString();
		Consumer<String> onPicked = path -> {
			Object root = designTreeChannel.get();
			if (root instanceof DesignTreeNode rootNode) {
				DesignTreeNode node = findViewRoot(rootNode, path);
				if (node != null) {
					selectionChannel.set(node);
				} else {
					Logger.info("Select view: no design node for source '" + path + "'.",
						SelectViewCommand.class);
				}
			}
		};
		registry.registerPick(token, new PendingViewPick(designerWindowId, onPicked));

		mainQueue.enqueue(ViewPickEvent.create()
			.setToken(token)
			.setTargetWindowId(mainWindowId));

		return HandlerResult.DEFAULT_RESULT;
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
