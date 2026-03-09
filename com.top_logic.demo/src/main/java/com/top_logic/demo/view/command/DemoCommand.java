/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A simple demo command that logs its execution.
 */
public class DemoCommand implements ViewCommand {

	/**
	 * Configuration for {@link DemoCommand}.
	 */
	@TagName("demo-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(DemoCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link DemoCommand}.
	 */
	@CalledByReflection
	public DemoCommand(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		Logger.info("Demo command executed with input: " + input, DemoCommand.class);
		return HandlerResult.DEFAULT_RESULT;
	}
}
