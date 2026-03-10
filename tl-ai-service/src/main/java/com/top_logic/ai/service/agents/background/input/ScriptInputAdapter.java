/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;

/**
 * {@link InputAdapter} that evaluates a TL-Script expression to produce input.
 *
 * <p>
 * The script is obtained from a {@link ScriptInput} configuration and evaluated at runtime.
 * </p>
 */
public class ScriptInputAdapter implements InputAdapter {

	private final String _script;

	/**
	 * Creates a new {@link ScriptInputAdapter} from the given configuration.
	 *
	 * @param config
	 *        The script input configuration.
	 */
	public ScriptInputAdapter(ScriptInput config) {
		_script = config.getScript();
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		// TODO: Implement TLScript evaluation against the context.
		return _script;
	}
}
