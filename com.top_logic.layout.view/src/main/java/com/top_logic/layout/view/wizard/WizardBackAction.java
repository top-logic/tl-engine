/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} that moves the enclosing {@link WizardScope wizard} to the preceding step.
 *
 * <p>
 * On the first step, nothing happens. Pair the action with a
 * {@link WizardHasBack &lt;wizard-has-back&gt;} rule to hide the button on that step instead of
 * offering one that does nothing.
 * </p>
 *
 * <p>
 * Passes the input through as output, so an action following it in the chain still sees the object
 * the chain works on - which is what makes "discard what was entered, then step back" one command.
 * </p>
 */
@InApp
public class WizardBackAction implements ViewAction {

	/**
	 * Configuration for {@link WizardBackAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<WizardBackAction> {

		/** Configuration tag of a {@link WizardBackAction}. */
		String TAG_NAME = "wizard-back";

		@Override
		@ClassDefault(WizardBackAction.class)
		Class<? extends WizardBackAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link WizardBackAction} from configuration.
	 */
	@CalledByReflection
	public WizardBackAction(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Creates a new {@link WizardBackAction}.
	 */
	public WizardBackAction() {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		WizardScope.lookup(context, Config.TAG_NAME).back();
		return input;
	}
}
