/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Empty {@link InputStream}.
 * 
 * @author aro
 */
public class EmptyInputStream extends InputStream {

    public static final InputStream INSTANCE = new EmptyInputStream ();

    /** You can use the Shared INSTANCE.
     *  No need to construct this more than once.
     */
    private EmptyInputStream() { /* empty, what did you expect ? */}

	/**
	 * returns always -1 because it is an empty input stream.
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return -1;
	}

}

