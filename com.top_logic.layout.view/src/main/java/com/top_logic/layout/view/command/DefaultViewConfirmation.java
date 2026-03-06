/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;

/**
 * Default {@link ViewCommandConfirmation} that produces a generic "Do you really want to execute
 * ..." message.
 */
public class DefaultViewConfirmation implements ViewCommandConfirmation {

	/**
	 * Configuration for {@link DefaultViewConfirmation}.
	 */
	@TagName("default-confirmation")
	public interface Config extends ViewCommandConfirmation.Config {

		@Override
		@ClassDefault(DefaultViewConfirmation.class)
		Class<? extends ViewCommandConfirmation> getImplementationClass();
	}

	/**
	 * Creates a new {@link DefaultViewConfirmation} from configuration.
	 */
	@CalledByReflection
	public DefaultViewConfirmation(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ResKey getConfirmation(ResKey commandLabel, Object input) {
		return I18NConstants.CONFIRM_EXECUTE__LABEL.fill(commandLabel);
	}
}
