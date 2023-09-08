/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

/**
 * Provide the importer logs in a string.
 * 
 * <p><b>Attention:</b> This logger holds all log messages in memory, so 
 * use it only on when working on import processes with a small amount of
 * messages.</p>
 * <p>This instance can be used to find an expected log message when the 
 * importer has been finished.</p>
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class BufferLogger extends AbstractLogger{

    private StringBuffer _buffer;

	/** 
	 * Creates a {@link BufferLogger}.
	 */
	public BufferLogger() {
		super();
		_buffer = new StringBuffer();
	}

	@Override
	protected void log(String stack) {
		_buffer.append(stack);
	}

	/** 
	 * Provide the log messages in one string.
	 * 
	 * @return   The requested log messages.
	 */
	public String getString() {		
		return _buffer.toString();
	}
}
