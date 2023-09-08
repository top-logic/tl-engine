/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Generic error to abort processing in case there is some misconfiguration. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationError extends I18NRuntimeException {

	@Deprecated
	private final String _technicalMessage;

	/**
	 * Upgrades a {@link ConfigurationException} to a {@link ConfigurationError}.
	 */
	public ConfigurationError(ConfigurationException ex) {
		this(ex.getErrorKey(), ex);
	}

	/**
	 * Creates a {@link ConfigurationError}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 */
	public ConfigurationError(ResKey errorKey) {
		super(errorKey);
		_technicalMessage = null;
	}

	/**
	 * Creates a {@link ConfigurationError}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public ConfigurationError(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
		_technicalMessage = null;
	}

	/**
	 * Creates a {@link ConfigurationError}.
	 * 
	 * @deprecated Use either {@link #ConfigurationError(ConfigurationException)}, or
	 *             {@link #ConfigurationError(ResKey, Throwable)}
	 */
	@Deprecated
	public ConfigurationError(String message, ConfigurationException cause) {
		super(cause.getErrorKey(), cause);
		_technicalMessage = message;
	}

	/**
	 * Creates a {@link ConfigurationError}.
	 * 
	 * @deprecated Use either {@link #ConfigurationError(ConfigurationException)}, or
	 *             {@link #ConfigurationError(ResKey, Throwable)}
	 */
	@Deprecated
	public ConfigurationError(String message, Throwable cause) {
		super(key(message, cause), cause);
		_technicalMessage = message;
	}

	private static ResKey key(String message, Throwable cause) {
		if (cause instanceof I18NFailure) {
			return ((I18NFailure) cause).getErrorKey();
		} else {
			return ResKey.text(message);
		}
	}

	/**
	 * Creates a {@link ConfigurationError}.
	 * 
	 * @deprecated Use {@link #ConfigurationError(ResKey)}
	 */
	@Deprecated
	public ConfigurationError(String message) {
		super(ResKey.text(message));
		_technicalMessage = message;
	}

	@Override
	protected String getDefaultMessage() {
		return _technicalMessage == null ? super.getDefaultMessage() : _technicalMessage;
	}

}
