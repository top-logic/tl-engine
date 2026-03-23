/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that throws a {@link TopLogicException} with a configured message.
 *
 * <p>
 * Useful for testing error handling in the command pipeline (e.g., snackbar error display).
 * </p>
 */
public class ThrowErrorAction implements ViewAction {

	/**
	 * Configuration for {@link ThrowErrorAction}.
	 */
	@TagName("throw-error")
	public interface Config extends PolymorphicConfiguration<ThrowErrorAction> {

		@Override
		@ClassDefault(ThrowErrorAction.class)
		Class<? extends ThrowErrorAction> getImplementationClass();

		/**
		 * The error message to display.
		 */
		@Name("message")
		@Mandatory
		String getMessage();
	}

	private final String _message;

	/**
	 * Creates a new {@link ThrowErrorAction}.
	 */
	@CalledByReflection
	public ThrowErrorAction(InstantiationContext context, Config config) {
		_message = config.getMessage();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		throw new TopLogicException(ResKey.text(_message));
	}
}
