/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.ReactContext;

/**
 * A step of the React login between the authentication of an account and the start of its session.
 *
 * <p>
 * The steps of an application are listed in the {@link LoginStepsConfig}, in the order they run. A
 * {@link LoginFlow} asks each step in turn whether it {@link #appliesTo(LoginFlow) applies} to the
 * account logging in, and {@link #start(ReactContext, LoginFlow) starts} the first one that does.
 * The step opens a dialog of its own (see
 * {@link LoginFlow#replaceDialog(ReactContext, String, java.util.Map)}); the dialog ends the step
 * through {@link LoginFlow#finish(ReactContext, Class, com.top_logic.knowledge.wrap.person.Person)},
 * which starts the next applying step or, after the last one, logs the account in. Cancelling the
 * dialog cancels the login.
 * </p>
 *
 * <p>
 * A step is finished only as a step of its own kind: an action that proves something - a new
 * password, a second factor - finishes the step of its kind and no other. The general
 * {@link ContinueLoginAction} finishes only a {@link DialogStep}, a step that asks for nothing but
 * confirmation.
 * </p>
 */
public interface LoginStep {

	/**
	 * Configuration of a {@link LoginStep}.
	 *
	 * <p>
	 * The {@link #getName() name} identifies the step in the {@link LoginStepsConfig}, so that a
	 * module places its step before or after another one.
	 * </p>
	 */
	interface Config<I extends LoginStep> extends PolymorphicConfiguration<I>, NamedConfigMandatory {
		// Pure marker.
	}

	/**
	 * Whether this step applies to the login described by the given flow.
	 *
	 * @param flow
	 *        The login in progress, carrying the account and what is known of its authentication.
	 */
	boolean appliesTo(LoginFlow flow);

	/**
	 * Starts this step by opening its dialog.
	 *
	 * <p>
	 * Called only when the step {@link #appliesTo(LoginFlow) applies}.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param flow
	 *        The login in progress.
	 */
	void start(ReactContext context, LoginFlow flow);

}
