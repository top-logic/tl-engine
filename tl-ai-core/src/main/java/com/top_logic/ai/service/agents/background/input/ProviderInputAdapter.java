/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.basic.config.SimpleInstantiationContext;

/**
 * {@link InputAdapter} that delegates to a pluggable {@link InputProvider}.
 *
 * <p>
 * The provider is instantiated from a {@link ProviderInput} configuration.
 * </p>
 */
public class ProviderInputAdapter implements InputAdapter {

	private final InputProvider _provider;

	/**
	 * Creates a new {@link ProviderInputAdapter} from the given configuration.
	 *
	 * @param config
	 *        The provider input configuration.
	 */
	public ProviderInputAdapter(ProviderInput config) {
		_provider = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config.getProvider());
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		return _provider.provide(context);
	}
}
