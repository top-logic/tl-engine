/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;
import java.io.Writer;

/**
 * The NullWriter will swallow all output not doing anything.
 * 
 * Can be handy for testcases or in case (part) of some output
 * should be suppressed.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class NullWriter extends Writer {
    
    /** usually this one instance should doe as this is stateless */
    public static NullWriter INSTANCE = new NullWriter();

    /**
     * @see java.io.Writer#close()
     */
    @Override
	public void close() throws IOException {
        // nothing happens here
    }

    /**
     * @see java.io.Writer#flush()
     */
    @Override
	public void flush() throws IOException {
        // nothing happens here
    }

    /**
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
	public void write(char[] aCbuf, int aOff, int aLen) throws IOException {
        // nothing happens here
    }

}

