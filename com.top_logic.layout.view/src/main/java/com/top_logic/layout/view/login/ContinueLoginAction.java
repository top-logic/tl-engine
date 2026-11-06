/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} ending the dialog of a {@link DialogStep} of the login: finishes the step and
 * runs the next one, or logs the account in after the last step.
 *
 * <p>
 * Finishes only a {@link DialogStep}: when another step of the login is due - a password change,
 * a second factor - the action refuses and cancels the login, so that no dialog skips a step that
 * asks for a proof.
 * </p>
 *
 * @see LoginFlow#finish(ReactContext, Class, com.top_logic.knowledge.wrap.person.Person)
 */
@InApp
public class ContinueLoginAction implements ViewAction {

	/**
	 * Configuration of a {@link ContinueLoginAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ContinueLoginAction> {

		/** Configuration tag of a {@link ContinueLoginAction}. */
		String TAG_NAME = "continue-login";

		@Override
		@ClassDefault(ContinueLoginAction.class)
		Class<? extends ContinueLoginAction> getImplementationClass();
	}

	/**
	 * Creates a {@link ContinueLoginAction} from configuration.
	 */
	@CalledByReflection
	public ContinueLoginAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		LoginFlow.finish(context, DialogStep.class, null);
		return input;
	}

	/**
	 * Opens the dialog of the next step or logs the account in.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
