/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.demo.react.DemoReactCounterComponent.DemoCounterControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowOptions;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo command that opens a child window with a counter control.
 */
public class OpenWindowDemoCommand implements ViewCommand {

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
			(ctx, model) -> new DemoCounterControl(ctx, (String) model),
			windowLabel, options, () -> {
				if (errorSink != null) {
					errorSink.showInfo(Fragments.text(windowLabel + " closed."));
				}
			});

		return HandlerResult.DEFAULT_RESULT;
	}
}
