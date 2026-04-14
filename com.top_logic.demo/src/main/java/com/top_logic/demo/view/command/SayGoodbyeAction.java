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
 * Trivial {@link ViewAction} that logs a farewell, used for context-menu demo.
 */
public class SayGoodbyeAction implements ViewAction {

	/**
	 * Configuration for {@link SayGoodbyeAction}.
	 */
	@TagName("say-goodbye")
	public interface Config extends PolymorphicConfiguration<SayGoodbyeAction> {

		@Override
		@ClassDefault(SayGoodbyeAction.class)
		Class<? extends SayGoodbyeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link SayGoodbyeAction}.
	 */
	@CalledByReflection
	public SayGoodbyeAction(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Logger.info("Goodbye from context menu. Input: " + input, SayGoodbyeAction.class);
		return input;
	}
}
