/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.react.theme.AbstractStylesheetTokenTest;

/**
 * Audits the stylesheet of the TL-Script editor control.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestScriptEditorStylesheetTokens extends AbstractStylesheetTokenTest {

	/** The stylesheets audited, under the resource paths they are served from. */
	private static final List<String> STYLESHEETS = List.of("/style/tlScriptEditor.css");

	@Override
	protected List<String> stylesheets() {
		return STYLESHEETS;
	}

	/**
	 * @see AbstractStylesheetTokenTest#suite(Class)
	 */
	public static Test suite() {
		return AbstractStylesheetTokenTest.suite(TestScriptEditorStylesheetTokens.class);
	}

}
