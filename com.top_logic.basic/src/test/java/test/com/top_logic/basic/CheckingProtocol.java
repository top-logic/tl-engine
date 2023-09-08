/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.AssertionFailedError;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;

/**
 * {@link Protocol} that reports failures comfomant to JUnit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckingProtocol extends BufferingProtocol {

	private final String _description;

	/**
	 * Creates a {@link CheckingProtocol} without global test description.
	 */
	public CheckingProtocol() {
		this(null);
	}

	/**
	 * Creates a {@link CheckingProtocol}.
	 * 
	 * @param description
	 *        A global test description that is prepended to failure messages.
	 */
	public CheckingProtocol(String description) {
		this._description = description;
	}
	
	@Override
	protected void reportError(String message, Throwable ex) {
		Logger.error(message, ex, CheckingProtocol.class);
		super.reportError(message, ex);
	}
	
	@Override
	protected RuntimeException reportFatal(String message, Throwable ex) {
		Logger.fatal(message, ex, CheckingProtocol.class);
		return super.reportFatal(message, ex);
	}
	
	@Override
	protected RuntimeException createAbort(String message, Throwable cause) {
		throw (AssertionFailedError) new AssertionFailedError(getFailureMessage(message)).initCause(cause);
	}

	private String getFailureMessage(String message) {
		if (_description != null) {
			return _description + ": " + message;
		} else {
			return message;
		}
	}
	
}
