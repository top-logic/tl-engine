/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Base class for declared {@link Exception}s implementing {@link I18NFailure}.
 * 
 * @see I18NRuntimeException
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class I18NException extends Exception implements I18NFailure {

	private final ResKey _errorKey;

	/**
	 * Creates a {@link I18NException}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public I18NException(ResKey errorKey, Throwable cause) {
		super(code(errorKey), cause);
		_errorKey = errorKey;
	}

	/**
	 * Creates a {@link I18NException}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 */
	public I18NException(ResKey errorKey) {
		super(code(errorKey));
		_errorKey = errorKey;
	}

	@Override
	public final String getMessage() {
		// System messages are reported consistently in English.
		ResourcesModule.Module module = ResourcesModule.Module.INSTANCE;
		if (module.isActive()) {
			return module.getImplementationInstance().getBundle(ResourcesModule.getLogLocale()).getString(getMessageKey());
		} else {
			return getMessageKey().unknown(DummyBundle.INSTANCE);
		}
	}

	/**
	 * The {@link ResKey} to use for creating e.g. a stack trace.
	 * 
	 * <p>
	 * Defaults to {@link #getErrorKey()} but may be customized by sub-classes.
	 * </p>
	 */
	protected ResKey getMessageKey() {
		return getErrorKey();
	}

	/**
	 * The technical message for e.g. a stack trace or the log, if internationalization service is
	 * available.
	 * 
	 * <p>
	 * Defaults to the {@link ResKey#toString()} of the {@link #getErrorKey()} but may be customized
	 * by sub-classes.
	 * </p>
	 */
	protected String getDefaultMessage() {
		return super.getMessage();
	}

	@Override
	public final ResKey getErrorKey() {
		return _errorKey;
	}

	/**
	 * The internal {@link Throwable#getMessage()} for an {@link I18NFailure}.
	 */
	static String code(ResKey errorKey) {
		// Note: Do not use errorKey.toString() here unconditionally. If the key contains arguments,
		// printing these arguments may fail. This value is only used for the super class
		// constructor to have any technical error key.
		return errorKey.hasKey() ? errorKey.getKey() : errorKey.toString();
	}

}
