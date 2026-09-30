/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that discards all in-memory modifications to the design tree and reloads
 * it from disk.
 *
 * <p>
 * Reads the application's default view path from {@link ViewConfig}, rebuilds the
 * {@link DesignTreeNode} tree using {@link DesignTreeBuilder}, and sets it on the configured
 * {@code design-tree} channel. This causes the {@link DesignerTreeElement} to rebuild its tree
 * from the new root. Optionally clears the selection channel.
 * </p>
 */
public class RevertDesignCommand implements ViewCommand {

	/**
	 * Configuration for {@link RevertDesignCommand}.
	 */
	@TagName("revert-design")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(RevertDesignCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getDesignTree()}. */
		String DESIGN_TREE = "design-tree";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * Reference to the channel containing the root {@link DesignTreeNode}.
		 *
		 * <p>
		 * After revert, the freshly built tree root is set on this channel, triggering a full
		 * rebuild of the designer tree UI.
		 * </p>
		 */
		@Name(DESIGN_TREE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getDesignTree();

		/**
		 * Optional reference to a selection channel to clear on revert.
		 *
		 * <p>
		 * If configured, this channel is set to {@code null} so that any previously selected node
		 * (which no longer exists in the new tree) is deselected.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	/**
	 * Creates a new {@link RevertDesignCommand}.
	 */
	@CalledByReflection
	public RevertDesignCommand(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		// 1. Determine the root view path from application configuration.
		String defaultView = ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView();
		String viewPath = ViewLoader.VIEW_BASE_PATH + defaultView;

		// 2. Build a fresh DesignTreeNode tree from disk.
		DesignTreeNode newRoot;
		try {
			newRoot = new DesignTreeBuilder().build(viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to reload design tree from: " + viewPath, ex);
		}

		// 3. Resolve the design-tree channel, dispose the old tree, and set the new root.
		if (context instanceof ViewContext viewContext) {
			ViewChannel designTreeChannel = viewContext.resolveChannel(_config.getDesignTree());
			if (designTreeChannel.get() instanceof DesignTreeNode oldRoot) {
				oldRoot.cleanup();
			}
			designTreeChannel.set(newRoot);

			// 4. Clear the selection channel if configured.
			ChannelRef selectionRef = _config.getSelection();
			if (selectionRef != null) {
				ViewChannel selectionChannel = viewContext.resolveChannel(selectionRef);
				selectionChannel.set(null);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}
}
