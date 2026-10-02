/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the command dismissing a message that is shown under a generation of its own,
 * the message of a {@link ReactSnackbarControl} or of a
 * {@link com.top_logic.layout.react.control.common.ReactAlertControl}: the generation of the
 * message being dismissed.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Dismiss message {generation}")
public interface DismissArguments extends ReactCommand {

	/** @see #getGeneration() */
	String GENERATION = "generation";

	/**
	 * The generation of the message being dismissed; a stale generation is ignored. Absent when the
	 * client does not carry a generation.
	 */
	@Name(GENERATION)
	Integer getGeneration();

}
