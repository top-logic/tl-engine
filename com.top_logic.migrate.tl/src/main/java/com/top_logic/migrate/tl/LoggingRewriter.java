/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;

/**
 * {@link RewritingEventVisitor} which additionally has a log protocol.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LoggingRewriter extends RewritingEventVisitor {

	/** {@link Protocol} to write messages to. */
	protected final Protocol _log;

	/**
	 * Creates a new {@link LoggingRewriter}.
	 * 
	 * @param log
	 *        {@link Protocol} to write messages to.
	 */
	public LoggingRewriter(Protocol log) {
		if (log == null) {
			throw new NullPointerException("'log' must not be 'null'.");
		}
		_log = log;
	}
}

