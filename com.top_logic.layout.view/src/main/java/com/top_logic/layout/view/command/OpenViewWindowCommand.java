/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowOptions;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ViewCommand} that pops out a new browser window whose content is a configured
 * {@code .view.xml}.
 *
 * <p>
 * The generic "open a window showing a view" behavior — any feature that needs a side-window (a
 * recorder panel, a help window, a detail pop-out) configures this command with the {@link
 * Config#getView() view} and window geometry instead of writing a bespoke open command. A {@link
 * Config#getSingletonKey() singleton key} makes repeated invocations focus the existing window rather
 * than open duplicates.
 * </p>
 */
public class OpenViewWindowCommand implements ViewCommand {

	/**
	 * Configuration for {@link OpenViewWindowCommand}.
	 */
	@TagName("open-window")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getHeight()}. */
		String HEIGHT = "height";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/** Configuration name for {@link #getSingletonKey()}. */
		String SINGLETON_KEY = "singleton-key";

		@Override
		@ClassDefault(OpenViewWindowCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The window content view, as a path relative to {@link ViewLoader#VIEW_BASE_PATH} (e.g.
		 * {@code demo/script-recorder.view.xml}).
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * The window title shown in the browser window chrome (resolved in the user's locale when the
		 * window opens).
		 */
		@Name(TITLE)
		ResKey getTitle();

		/**
		 * The initial window width in pixels.
		 */
		@Name(WIDTH)
		@IntDefault(600)
		int getWidth();

		/**
		 * The initial window height in pixels.
		 */
		@Name(HEIGHT)
		@IntDefault(400)
		int getHeight();

		/**
		 * Whether the window may be resized.
		 */
		@Name(RESIZABLE)
		@BooleanDefault(true)
		boolean getResizable();

		/**
		 * An optional key identifying this window as a singleton: opening it again focuses the existing
		 * window instead of creating a duplicate. Empty for a non-singleton window.
		 */
		@Name(SINGLETON_KEY)
		String getSingletonKey();
	}

	private final String _viewPath;

	private final ResKey _title;

	private final int _width;

	private final int _height;

	private final boolean _resizable;

	private final String _singletonKey;

	/**
	 * Creates a new {@link OpenViewWindowCommand}.
	 */
	@CalledByReflection
	public OpenViewWindowCommand(InstantiationContext context, Config config) {
		_viewPath = ViewLoader.VIEW_BASE_PATH + config.getView();
		_title = config.getTitle();
		_width = config.getWidth();
		_height = config.getHeight();
		_resizable = config.getResizable();
		_singletonKey = config.getSingletonKey();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		WindowOptions options = new WindowOptions()
			// Resolve the title in the acting user's locale, not at configuration load.
			.setTitle(_title == null ? null : Resources.getInstance().getString(_title))
			.setWidth(_width)
			.setHeight(_height)
			.setResizable(_resizable);
		if (_singletonKey != null && !_singletonKey.isEmpty()) {
			options.setSingletonKey(_singletonKey);
		}
		registry.openWindow(context, (windowContext, model) -> createContent(windowContext), null, options);
		return HandlerResult.DEFAULT_RESULT;
	}

	private ReactControl createContent(ReactContext windowContext) {
		ViewElement view;
		try {
			view = ViewLoader.getOrLoadView(_viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load window view: " + _viewPath, ex);
		}
		ViewContext viewContext = new DefaultViewContext(windowContext);
		return (ReactControl) view.createControl(viewContext);
	}
}
