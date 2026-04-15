/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowOptions;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo command that opens a child window whose content is loaded from a {@code .view.xml} file.
 *
 * <p>
 * The window content is declared in {@code /WEB-INF/views/demo/open-window-demo.view.xml} and
 * includes an {@code <app-shell>} so that context menus (and other overlay features that depend on
 * the app shell) work in the sub-window.
 * </p>
 */
public class OpenWindowDemoCommand implements ViewCommand {

	private static final String WINDOW_VIEW_PATH = "/WEB-INF/views/demo/open-window-demo.view.xml";

	private static final AtomicInteger WINDOW_COUNTER = new AtomicInteger();

	/**
	 * Configuration for {@link OpenWindowDemoCommand}.
	 */
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(OpenWindowDemoCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link OpenWindowDemoCommand}.
	 */
	@CalledByReflection
	public OpenWindowDemoCommand(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		ErrorSink errorSink = context.getErrorSink();

		int windowNumber = WINDOW_COUNTER.incrementAndGet();
		String windowLabel = "Child Window #" + windowNumber;

		WindowOptions options = new WindowOptions()
			.setWidth(600)
			.setHeight(400)
			.setTitle(windowLabel)
			.setResizable(true);

		registry.openWindow(context,
			(ctx, model) -> createWindowContent(ctx),
			windowLabel, options, () -> {
				if (errorSink != null) {
					errorSink.showInfo(Fragments.text(windowLabel + " closed."));
				}
			});

		return HandlerResult.DEFAULT_RESULT;
	}

	private static ReactControl createWindowContent(ReactContext windowContext) {
		ViewElement view;
		try {
			view = ViewLoader.getOrLoadView(WINDOW_VIEW_PATH);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load window view: " + WINDOW_VIEW_PATH, ex, OpenWindowDemoCommand.class);
			throw new RuntimeException("Failed to load window view: " + WINDOW_VIEW_PATH, ex);
		}
		ViewContext viewContext = new DefaultViewContext(windowContext);
		return (ReactControl) view.createControl(viewContext);
	}
}
