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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowOptions;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that opens the View Designer in a new browser window.
 *
 * <p>
 * Loads the designer view from {@code /WEB-INF/views/designer.view.xml}, builds the
 * {@link DesignTreeNode} tree from the application's default root view, and opens it via
 * {@link ReactWindowRegistry#openWindow(ReactContext, ReactControlProvider, Object, WindowOptions)}.
 * </p>
 */
public class OpenDesignerCommand implements ViewCommand {

	/** Path to the designer view definition. */
	private static final String DESIGNER_VIEW_PATH = ViewLoader.VIEW_BASE_PATH + "designer.view.xml";

	/**
	 * Configuration for {@link OpenDesignerCommand}.
	 */
	@TagName("open-designer")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(OpenDesignerCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link OpenDesignerCommand}.
	 */
	@CalledByReflection
	public OpenDesignerCommand(InstantiationContext context, Config config) {
		// No configuration to store.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		WindowOptions options = new WindowOptions()
			.setTitle("View Designer")
			.setWidth(1200)
			.setHeight(800)
			.setResizable(true)
			.setSingletonKey("viewDesigner");

		ReactControlProvider controlProvider = (windowContext, model) -> {
			ViewElement designerView;
			try {
				designerView = ViewLoader.getOrLoadView(DESIGNER_VIEW_PATH);
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to load designer view: " + DESIGNER_VIEW_PATH, ex);
			}

			// Create isolated ViewContext for the designer window.
			ViewContext designerContext = new DefaultViewContext(windowContext);

			// Build the design tree from the application's default root view.
			String rootViewPath = ViewLoader.VIEW_BASE_PATH
				+ ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView();

			DesignTreeNode designTree;
			try {
				designTree = new DesignTreeBuilder().build(rootViewPath);
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to build design tree from: " + rootViewPath, ex);
			}

			// Register the design tree as a channel in the designer context.
			DefaultViewChannel designTreeChannel = new DefaultViewChannel("designTree");
			designTreeChannel.set(designTree);
			designerContext.registerChannel("designTree", designTreeChannel);

			// Pass the main application window's ViewContext so the designer can
			// trigger hot-reload after saving.
			DefaultViewChannel appContextChannel = new DefaultViewChannel("appContext");
			appContextChannel.set(context);
			designerContext.registerChannel("appContext", appContextChannel);

			// Build the control tree from the designer view.
			return (ReactControl) designerView.createControl(designerContext);
		};

		registry.openWindow(context, controlProvider, null, options);

		return HandlerResult.DEFAULT_RESULT;
	}
}
