/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Base class for {@link RuntimeException}s implementing {@link I18NFailure}.
 * 
 * @see I18NException
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NRuntimeException extends RuntimeException implements I18NFailure {

	private ErrorSeverity _severity;

	private final ResKey _errorTitle;

	private ResKey _details = null;

	/**
	 * Creates a {@link I18NRuntimeException}.
	 * 
	 * @param errorTitle
	 *        See {@link #getErrorKey()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public I18NRuntimeException(ResKey errorTitle, Throwable cause) {
		super(code(errorTitle), cause);
		_errorTitle = errorTitle;
	}

	/**
	 * Creates a {@link I18NRuntimeException}.
	 * 
	 * @param errorTitle
	 *        See {@link #getErrorKey()}.
	 */
	public I18NRuntimeException(ResKey errorTitle) {
		super(code(errorTitle));
		_errorTitle = errorTitle;
	}

	/**
	 * The severity of the problem used to provide different visual feedback to the user.
	 */
	public ErrorSeverity getSeverity() {
		return _severity == null ? defaultSeverity() : _severity;
	}

	/**
	 * The default {@link ErrorSeverity}, if no explicit value has been set.
	 * 
	 * @see #initSeverity(ErrorSeverity)
	 */
	protected ErrorSeverity defaultSeverity() {
		return ErrorSeverity.ERROR;
	}

	/**
	 * Initialized {@link #getSeverity()}.
	 * 
	 * <p>
	 * Note: Must only be called once.
	 * </p>
	 */
	public I18NRuntimeException initSeverity(ErrorSeverity severity) {
		if (_severity != null) {
			throw new IllegalStateException("The error severity has already been set.");
		}
		_severity = severity;
		return this;
	}

	/**
	 * A detail message explaining the problem.
	 */
	public ResKey getDetails() {
		return _details != null ? _details : ResKey.fallback(_errorTitle.tooltip(), ResKey.text(null));
	}

	/**
	 * Initializes {@link #getDetails()}.
	 * 
	 * <p>
	 * Note: Must only be called once.
	 * </p>
	 */
	public I18NRuntimeException initDetails(ResKey details) {
		if (_details != null) {
			throw new IllegalStateException("The error details have already been set.");
		}
		_details = details;
		return this;
	}

	@Override
	public final String getMessage() {
		try {
			return getMessageUnsafe();
		} catch (ThreadDeath exception) {
			throw exception; // Never ignore ThreadDeath.
		} catch (Throwable exception) {
			addSuppressed(exception);
			/* Resolving the message key failed: Print its toString() representation instead. If an
			 * exception is thrown here, the stack trace and the error message will contain no
			 * usable information. That has to be avoided at all costs. */
			return Objects.toString(getMessageKey());
		}
	}

	private String getMessageUnsafe() {
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
		return _errorTitle;
	}

	/**
	 * The internal {@link Throwable#getMessage()} for an {@link I18NFailure}.
	 */
	private static String code(ResKey errorKey) {
		return I18NException.code(errorKey);
	}

}
