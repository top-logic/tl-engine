/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;

/**
 * {@link InputAdapter} that returns a fixed custom message as input.
 *
 * <p>
 * The message is obtained from an {@link AutonomousInput} configuration and returned unchanged.
 * </p>
 */
public class AutonomousInputAdapter implements InputAdapter {

	private final String _message;

	/**
	 * Creates a new {@link AutonomousInputAdapter} from the given configuration.
	 *
	 * @param config
	 *        The autonomous input configuration.
	 */
	public AutonomousInputAdapter(AutonomousInput config) {
		_message = config.getCustomMessage();
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		return _message;
	}
}
