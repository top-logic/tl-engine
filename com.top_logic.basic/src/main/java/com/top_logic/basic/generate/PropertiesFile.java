/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import java.io.PrintWriter;

/**
 * Java properties file handling.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertiesFile implements FileType {

	public static PropertiesFile INSTANCE = new PropertiesFile();
	
	public PropertiesFile() {
		// Singleton constructor.
	}
	
	@Override
	public void comment(PrintWriter out, String comment) {
		out.print("# ");
		out.println(comment);
	}

	@Override
	public void commentStart(PrintWriter out) {
		out.println('#');
	}
	
	@Override
	public void commentLine(PrintWriter out, String comment) {
		out.print("# ");
		out.println(comment);
	}

	@Override
	public void commentStop(PrintWriter out) {
		out.println('#');
	}

}
