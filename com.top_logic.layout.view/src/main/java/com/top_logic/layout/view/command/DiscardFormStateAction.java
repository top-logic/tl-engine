/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;

/**
 * {@link ViewAction} that discards the form overlay without applying changes.
 *
 * <p>
 * Delegates to {@link FormControl#executeCancel()} which resets the overlay and leaves edit mode.
 * Useful in cancel action chains for dialog forms where the dialog needs to be closed after
 * discarding changes.
 * </p>
 */
public class DiscardFormStateAction implements ViewAction {

	/**
	 * Configuration for {@link DiscardFormStateAction}.
	 */
	@TagName("discard-form-state")
	public interface Config extends PolymorphicConfiguration<DiscardFormStateAction> {

		@Override
		@ClassDefault(DiscardFormStateAction.class)
		Class<? extends DiscardFormStateAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link DiscardFormStateAction}.
	 */
	@CalledByReflection
	public DiscardFormStateAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext)) {
			return input;
		}

		FormModel formModel = ((ViewContext) context).getFormModel();
		if (!(formModel instanceof FormControl)) {
			return input;
		}

		((FormControl) formModel).executeCancel();
		return input;
	}
}
