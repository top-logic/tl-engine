/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
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
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Toolbar command that starts "select view" mode in the main application window. The user then
 * clicks a view there; the designer opens the file of that view and selects its root.
 *
 * <p>
 * The file is opened on the shortest trail of references from the application's root view. That
 * also covers a view the application opened from a path it computed, which no reference names: it
 * is shown directly behind the root view.
 * </p>
 *
 * @see ElementPicker
 * @see DesignSession#select(String)
 */
public class SelectViewCommand implements ViewCommand {

	/**
	 * Configuration for {@link SelectViewCommand}.
	 */
	@TagName("select-view")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getSession()}. */
		String SESSION = "session";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(SelectViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Reference to the channel containing the {@link DesignSession}. */
		@Name(SESSION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSession();

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
		if (!viewContext.hasChannel(OpenDesignerCommand.APP_CONTEXT_CHANNEL)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		Object appContextValue =
			viewContext.resolveChannel(new ChannelRef(OpenDesignerCommand.APP_CONTEXT_CHANNEL)).get();
		if (!(appContextValue instanceof ReactContext mainWindowContext)) {
			return HandlerResult.DEFAULT_RESULT;
		}

		String designerWindowId = context.getWindowName();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewChannel sessionChannel = viewContext.resolveChannel(_config.getSession());
		ViewChannel selectionChannel = viewContext.resolveChannel(_config.getSelection());

		Consumer<PickResult> onPicked = result -> {
			String path = ViewLoader.fullPath(((PickResult.ViewPicked) result).sourcePath());
			if (sessionChannel.get() instanceof DesignSession session) {
				session.select(path);
				// The picked file may be the one shown already, whose selection is elsewhere.
				selectionChannel.set(session.getCurrentTree());
				// The user picked the view in the application window, so the designer is now behind
				// it. Bring it back to the front to show the view that was selected there.
				registry.focusWindow(designerWindowId);
			}
		};
		ElementPicker.start(context, mainWindowContext, PickKind.VIEW, onPicked);

		return HandlerResult.DEFAULT_RESULT;
	}
}
