/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * Trivial {@link ViewAction} that logs a greeting, used for context-menu demo.
 */
public class SayHelloAction implements ViewAction {

	/**
	 * Configuration for {@link SayHelloAction}.
	 */
	@TagName("say-hello")
	public interface Config extends PolymorphicConfiguration<SayHelloAction> {

		@Override
		@ClassDefault(SayHelloAction.class)
		Class<? extends SayHelloAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link SayHelloAction}.
	 */
	@CalledByReflection
	public SayHelloAction(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Logger.info("Hello from context menu. Input: " + input, SayHelloAction.class);
		return input;
	}
}
