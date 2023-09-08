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

	public Protocol getProtocol() {
		return _protocol;
	}

	public void setProtocol(Protocol protocol) {
		this._protocol = protocol;
	}

	public Application getApplication() {
		return _application;
	}

	public void setApplication(Application application) {
		_application = application;
	}

	protected void checkErrors() {
		_protocol.checkErrors();
	}

	protected boolean hasErrors() {
		return _protocol.hasErrors();
	}

	protected void info(String message) {
		_protocol.info(message);
	}

	protected void info(String message, int verbosityLevel) {
		_protocol.info(message, verbosityLevel);
	}

	protected RuntimeException fatal(String message) {
		return _protocol.fatal(message);
	}

	protected RuntimeException fatal(String message, Exception ex) {
		return _protocol.fatal(message, ex);
	}

	protected void warn(String message) {
		handleMessage("WARN", null, message, null);
	}

	protected void warn(BinaryData resource, String message) {
		handleMessage("WARN", resource, message, null);
	}

	protected void error(String message) {
		handleMessage("ERROR", null, message, null);
	}

	protected void error(String message, Exception ex) {
		handleMessage("ERROR", null, message, ex);
	}

	protected void error(BinaryData resource, String message) {
		handleMessage("ERROR", resource, message, null);
	}

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

	protected BinaryData getResource() {
		return null;
	}

}
