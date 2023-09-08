/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import com.top_logic.basic.util.ResKey;

/**
 * Provide the importer messages in a string.
 * 
 * <p><b>Attention:</b> This logger holds all log messages in memory, so 
 * use it only on when working on import processes with a small amount of
 * messages.</p>
 * <p>This instance can be used to find an expected log message when the 
 * importer has been finished.</p>
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PlainMessageLogger extends AbstractLogger {

    private final StringBuffer buffer;
    private final String suffix;

    /** 
     * Creates a {@link BufferLogger}.
     */
    public PlainMessageLogger(String aSuffix) {
        this(new StringBuffer(), aSuffix);
    }

    /** 
     * Creates a {@link BufferLogger}.
     */
    public PlainMessageLogger(StringBuffer aBuffer, String aSuffix) {
		super();

		this.buffer = aBuffer;
        this.suffix = aSuffix;
	}

    @Override
    protected String getLogText(String type, ResKey message, Object caller) {
        return this.getRes().getString(message);
    }

	@Override
	protected void log(String stack) {
		this.buffer.append(stack).append(this.suffix);
	}

	/** 
	 * Provide the log messages in one string.
	 * 
	 * @return   The requested log messages.
	 */
	public String getString() {		
		return this.buffer.toString();
	}
}
