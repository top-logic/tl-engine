/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Base class for layout processing operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Operation {

	private Protocol _protocol;

	private Application _application;

	public Operation(Protocol protocol, Application application) {
		_protocol = protocol;
		_application = application;
	}

	/** The {@link Protocol} to report messages to. */
	public Protocol getProtocol() {
		return _protocol;
	}

	/** Setter for {@link #getProtocol()}. */
	public void setProtocol(Protocol protocol) {
		this._protocol = protocol;
	}

	/** The {@link Application} this operation works on, may be <code>null</code>. */
	public Application getApplication() {
		return _application;
	}

	/** Setter for {@link #getApplication()}. */
	public void setApplication(Application application) {
		_application = application;
	}

	/** Aborts the operation, if errors have been reported to the {@link #getProtocol()}. */
	protected void checkErrors() {
		_protocol.checkErrors();
	}

	/** Whether errors have been reported to the {@link #getProtocol()}. */
	protected boolean hasErrors() {
		return _protocol.hasErrors();
	}

	/** Reports an informational message. */
	protected void info(String message) {
		_protocol.info(message);
	}

	/** Reports an informational message with the given verbosity level. */
	protected void info(String message, int verbosityLevel) {
		_protocol.info(message, verbosityLevel);
	}

	/**
	 * Creates an exception reporting a fatal error.
	 *
	 * @return The exception to throw.
	 */
	protected RuntimeException fatal(String message) {
		return _protocol.fatal(message);
	}

	/**
	 * Creates an exception reporting a fatal error caused by the given exception.
	 *
	 * @return The exception to throw.
	 */
	protected RuntimeException fatal(String message, Exception ex) {
		return _protocol.fatal(message, ex);
	}

	/** Reports a warning for the current {@link #getResource()}. */
	protected void warn(String message) {
		handleMessage("WARN", null, message, null);
	}

	/** Reports a warning for the given resource. */
	protected void warn(BinaryData resource, String message) {
		handleMessage("WARN", resource, message, null);
	}

	/** Reports an error for the current {@link #getResource()}. */
	protected void error(String message) {
		handleMessage("ERROR", null, message, null);
	}

	/** Reports an error caused by the given exception for the current {@link #getResource()}. */
	protected void error(String message, Exception ex) {
		handleMessage("ERROR", null, message, ex);
	}

	/** Reports an error for the given resource. */
	protected void error(BinaryData resource, String message) {
		handleMessage("ERROR", resource, message, null);
	}

	/** Reports an error caused by the given exception for the given resource. */
	protected void error(BinaryData resource, String message, Exception ex) {
		handleMessage("ERROR", resource, message, ex);
	}

	private void handleMessage(String level, BinaryData resource, String message, Exception ex) {
		if (resource == null) {
			resource = getResource();
		}
		if (resource != null) {
			message = "In '" + resource + "': " + message;
		}

		if (this._application != null) {
			String applicationPrefix = _application.getName();
			message = "Application '" + applicationPrefix + "': " + message;
		}

		_protocol.error(level + ": " + message, ex);
	}

	/**
	 * The resource currently processed, which is mentioned in reported messages, or
	 * <code>null</code> if there is none.
	 */
	protected BinaryData getResource() {
		return null;
	}

}
