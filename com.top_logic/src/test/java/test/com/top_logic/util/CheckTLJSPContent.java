/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.File;
import java.util.Collection;

import test.com.top_logic.basic.jsp.JSPContentChecker;
import test.com.top_logic.basic.jsp.TestJSPContent;

import com.top_logic.layout.tools.JSPRewrite;

/**
 * Plugin for {@link TestJSPContent} with normalization test.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CheckTLJSPContent implements JSPContentChecker {

	/** Singleton {@link CheckTLJSPContent} instance. */
	public static final CheckTLJSPContent INSTANCE = new CheckTLJSPContent();

	/**
	 * Creates a new {@link CheckTLJSPContent}.
	 */
	protected CheckTLJSPContent() {
		// singleton instance
	}

	@Override
	public void checkContent(Collection<String> errors, File jsp, String contents) {
		String normalizedContents;
		try {
			normalizedContents = new JSPRewrite().processContents(contents);
		} catch (Exception ex) {
			errors.add("Contents not well-formed: " + ex.getMessage());
			return;
		}

		if (!stripWhiteSpace(contents).equals(stripWhiteSpace(normalizedContents))) {
			errors.add("Contents not normalized (run JSPRewrite)");
		}
	}

	private String stripWhiteSpace(String contents) {
		return contents.replaceAll("\\s+", "");
	}

}