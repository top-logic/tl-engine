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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} that displays a named step of the enclosing {@link WizardScope wizard}.
 *
 * <p>
 * The step is the one {@link Config#getStep()} names, or - where the configuration names none - the
 * value the chain carries, so that a jump target computed by an earlier action is followed. A key no
 * step of the wizard carries leaves the wizard where it is.
 * </p>
 *
 * <p>
 * Passes the input through as output, so an action following it in the chain still sees the object
 * the chain works on.
 * </p>
 */
@InApp
public class WizardGotoAction implements ViewAction {

	/**
	 * Configuration for {@link WizardGotoAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<WizardGotoAction> {

		/** Configuration tag of a {@link WizardGotoAction}. */
		String TAG_NAME = "wizard-goto";

		/** Configuration name for {@link #getStep()}. */
		String STEP = "step";

		@Override
		@ClassDefault(WizardGotoAction.class)
		Class<? extends WizardGotoAction> getImplementationClass();

		/**
		 * The {@code id} of the step to display.
		 *
		 * <p>
		 * Without one, the value the chain carries names the step, which is how a step computed
		 * beforehand is jumped to.
		 * </p>
		 */
		@Name(STEP)
		@Nullable
		String getStep();
	}

	private final String _step;

	/**
	 * Creates a new {@link WizardGotoAction} from configuration.
	 */
	@CalledByReflection
	public WizardGotoAction(InstantiationContext context, Config config) {
		this(config.getStep());
	}

	/**
	 * Creates a new {@link WizardGotoAction}.
	 *
	 * @param step
	 *        The {@code id} of the step to display, or {@code null} to take the step from the value
	 *        the chain carries.
	 */
	public WizardGotoAction(String step) {
		_step = step;
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		WizardScope.lookup(context, Config.TAG_NAME).goTo(_step != null ? _step : input);
		return input;
	}
}
