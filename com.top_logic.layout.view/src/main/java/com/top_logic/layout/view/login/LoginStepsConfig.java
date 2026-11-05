/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * The {@link LoginStep}s of the React login, in the order they run.
 *
 * <p>
 * Every login of the React UI - with the password, or without it with a passkey - runs these steps
 * in a {@link LoginFlow}; a step that does not apply to the account is skipped.
 * </p>
 *
 * @see #configured()
 */
public interface LoginStepsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getSteps()}. */
	String STEPS = "steps";

	/**
	 * The steps, in the order they run.
	 *
	 * @implNote Keyed by {@link NamedConfiguration#NAME_ATTRIBUTE} so that the fragments of several
	 *           modules merge, and a module places its step with <code>config:position</code>
	 *           before or after a step it names.
	 */
	@Key(NamedConfiguration.NAME_ATTRIBUTE)
	@Name(STEPS)
	@DefaultContainer
	List<LoginStep.Config<?>> getSteps();

	/**
	 * The {@link #getSteps() steps} of the application configuration, instantiated in their order.
	 *
	 * <p>
	 * A step that cannot be instantiated fails the call instead of being left out: a login must not
	 * start without a step the application configured, the second factor least of all.
	 * </p>
	 */
	static List<LoginStep> configured() {
		LoginStepsConfig config = ApplicationConfig.getInstance().getConfig(LoginStepsConfig.class);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		List<LoginStep> result = new ArrayList<>(config.getSteps().size());
		for (LoginStep.Config<?> stepConfig : config.getSteps()) {
			LoginStep step = context.getInstance(stepConfig);
			if (step == null) {
				throw new IllegalStateException("Login step '" + stepConfig.getName() + "' could not be created.");
			}
			result.add(step);
		}
		return result;
	}

}
