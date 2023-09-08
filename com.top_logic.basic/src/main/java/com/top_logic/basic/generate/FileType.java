/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import java.io.PrintWriter;

/**
 * Algorithm to generate special lines in source files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FileType {

	/**
	 * Start a multi-lined comment.
	 */
	void commentStart(PrintWriter out);

	/**
	 * Write a comment between {@link #commentStart(PrintWriter)} and {@link #commentStop(PrintWriter)}.
	 */
	void commentLine(PrintWriter out, String comment);
	
    /**
     * End a multi-lined comment.
     */
    void commentStop(PrintWriter out);
    
    /**
     * Write a single one-line comment.
     */
    void comment(PrintWriter out, String comment);

	
}