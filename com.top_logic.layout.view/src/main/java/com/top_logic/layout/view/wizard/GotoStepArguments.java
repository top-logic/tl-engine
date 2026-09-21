/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactWizardControl#GOTO_STEP_COMMAND} command: which step to
 * display.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Go to wizard step '{stepId}'")
public interface GotoStepArguments extends ReactCommand {

	/** @see #getStepId() */
	String STEP_ID = "stepId";

	/**
	 * The client-side identifier of the step to display, as the wizard published it in its step
	 * list.
	 */
	@Name(STEP_ID)
	@Mandatory
	String getStepId();

}
