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
 * Typed arguments of the {@link ReactWizardControl#ADVANCE_STEP_COMMAND} command: the step whose own
 * time is up.
 *
 * <p>
 * The step is named rather than assumed, because the user may have moved on themselves while the
 * timer ran: the wizard moves only while the step the timer belongs to is still the one displayed.
 * </p>
 */
@Label("Wizard step '{stepId}' ran out of time")
public interface AdvanceStepArguments extends ReactCommand {

	/** @see #getStepId() */
	String STEP_ID = "stepId";

	/**
	 * The client-side identifier of the step the timer was started for, as the wizard published it
	 * in its step list.
	 */
	@Name(STEP_ID)
	@Mandatory
	String getStepId();

}
