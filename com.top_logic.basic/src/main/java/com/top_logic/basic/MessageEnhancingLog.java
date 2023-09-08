/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link ProtocolAdaptor} that rewrites messages.
 * 
 * @see #enhance(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MessageEnhancingLog extends LogAdaptor {

	@Override
	public void error(String message) {
		super.error(enhance(message));
	}

	@Override
	public void error(String message, Throwable ex) {
		super.error(enhance(message), ex);
	}

	@Override
	public void info(String message) {
		super.info(enhance(message));
	}

	@Override
	public void info(String message, int verbosityLevel) {
		super.info(enhance(message), verbosityLevel);
	}

	/**
	 * Rewrites the given message.
	 * 
	 * @param message
	 *        The original message.
	 * @return The message to log.
	 */
	protected abstract String enhance(String message);

}
