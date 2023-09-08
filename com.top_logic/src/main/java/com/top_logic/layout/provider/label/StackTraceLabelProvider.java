/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} producing stack trace of a {@link Throwable} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StackTraceLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link StackTraceLabelProvider} instance.
	 */
	public static final StackTraceLabelProvider INSTANCE = new StackTraceLabelProvider();

	private StackTraceLabelProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		Throwable ex = (Throwable) object;
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		ex.printStackTrace(out);
		out.flush();
		return buffer.toString();
	}

}
