/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.ImmediateFailureProtocol;

/**
 * {@link ImmediateFailureProtocol} that throws {@link ConfigurationError}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigurationErrorProtocol extends ImmediateFailureProtocol {

	/**
	 * Singleton {@link ConfigurationErrorProtocol} instance.
	 */
	public static final ConfigurationErrorProtocol INSTANCE = new ConfigurationErrorProtocol();

	private ConfigurationErrorProtocol() {
		// Singleton constructor.
	}

	@Override
	protected RuntimeException createFailure(String message, Throwable ex) {
		return new ConfigurationError(message, ex);
	}

}