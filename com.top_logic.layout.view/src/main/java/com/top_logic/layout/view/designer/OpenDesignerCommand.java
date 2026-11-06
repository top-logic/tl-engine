/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
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
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;

/**
 * A {@link ViewCommand} that opens the View Designer in a new browser window on the application's
 * root view.
 *
 * <p>
 * Loads the designer view from {@code /WEB-INF/views/designer.view.xml}, starts a
 * {@link DesignSession} at the application's default root view, and opens it in a new window via
 * the {@link ReactWindowRegistry}. The action {@link OpenViewDesignerAction} opens the same
 * designer on a view stored in an attribute of a model object.
 * </p>
 *
 * <p>
 * The designer view finds the session in the channel {@value #SESSION_CHANNEL}, and the path of
 * the views from the root view to the view shown, as list of {@link DesignSource}s, in the channel
 * {@value #PATH_CHANNEL}.
 * </p>
 *
 * @see #openDesigner(ReactContext, DesignSource, String)
 * @implNote The window is opened through
 *           {@link ReactWindowRegistry#openWindow(ReactContext, ReactControlProvider, Object, WindowOptions)}.
 */
public class OpenDesignerCommand implements ViewCommand {

	/** Path to the designer view definition. */
	private static final String DESIGNER_VIEW_PATH = ViewLoader.VIEW_BASE_PATH + "designer.view.xml";

	/** Name of the channel holding the {@link DesignSession} of the designer window. */
	public static final String SESSION_CHANNEL = "designSession";

	/** Name of the channel holding the {@link DesignSession#bindPath(ViewChannel) path} of the session. */
	public static final String PATH_CHANNEL = "designPath";

	/** Name of the channel holding the {@link ReactContext} of the main application window. */
	public static final String APP_CONTEXT_CHANNEL = "appContext";

	/** Title of the designer window. */
	public static final String DESIGNER_TITLE = "View Designer";

	/**
	 * Prefix of the singleton key of a designer window, followed by the {@link DesignSource#getId()
	 * identifier} of the view it was opened on: opening the designer on a view whose designer is
	 * open already focuses that window.
	 */
	private static final String DESIGNER_WINDOW_KEY = "viewDesigner:";

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
	public void execute(ReactContext context, Object input) {
		String rootViewPath =
			ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView().fullPath();
		openDesigner(context, new FileDesignSource(rootViewPath), DESIGNER_TITLE);
	}

	/**
	 * Opens the View Designer in a new browser window, with a {@link DesignSession} on the given
	 * view.
	 *
	 * <p>
	 * A designer window already open on the same view is focused instead.
	 * </p>
	 *
	 * @param context
	 *        The context of the application window opening the designer. The designer picks views
	 *        in it and shows saved view files anew in it.
	 * @param root
	 *        The view the session is opened on.
	 * @param title
	 *        The title of the designer window.
	 */
	public static void openDesigner(ReactContext context, DesignSource root, String title) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return;
		}

		WindowOptions options = new WindowOptions()
			.setTitle(title)
			.setWidth(1200)
			.setHeight(800)
			.setResizable(true)
			.setSingletonKey(DESIGNER_WINDOW_KEY + root.getId());

		ReactControlProvider controlProvider = (windowContext, model) -> {
			ViewElement designerView;
			try {
				designerView = ViewLoader.getOrLoadView(DESIGNER_VIEW_PATH);
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to load designer view: " + DESIGNER_VIEW_PATH, ex);
			}

			// Create isolated ViewContext for the designer window.
			ViewContext designerContext = new DefaultViewContext(windowContext, DESIGNER_VIEW_PATH);

			DesignSession session = new DesignSession(root);
			// Fail with the problem of the root view rather than with an empty designer.
			session.getCurrentTree();

			DefaultViewChannel sessionChannel = new DefaultViewChannel(SESSION_CHANNEL);
			sessionChannel.set(session);
			designerContext.registerChannel(SESSION_CHANNEL, sessionChannel);

			DefaultViewChannel pathChannel = new DefaultViewChannel(PATH_CHANNEL);
			Registration pathBinding = session.bindPath(pathChannel);
			designerContext.registerChannel(PATH_CHANNEL, pathChannel);

			// Pass the main application window's ViewContext so the designer can
			// trigger hot-reload after saving.
			DefaultViewChannel appContextChannel = new DefaultViewChannel(APP_CONTEXT_CHANNEL);
			appContextChannel.set(context);
			designerContext.registerChannel(APP_CONTEXT_CHANNEL, appContextChannel);

			// Build the control tree from the designer view.
			ReactControl designer = (ReactControl) designerView.createControl(designerContext);
			designer.addCleanupAction(pathBinding::dispose);
			ObservingChannels.observeWhileDisplayed(designer,
				List.of(sessionChannel, pathChannel, appContextChannel), designerContext);
			return designer;
		};

		registry.openWindow(context, controlProvider, null, options);
	}
}
