/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.react.theme.AbstractStylesheetTokenTest;

/**
 * Audits the bundled stylesheet of the structured text editor.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestWysiwygStylesheetTokens extends AbstractStylesheetTokenTest {

	/** The stylesheets audited, under the resource paths they are served from. */
	private static final List<String> STYLESHEETS = List.of("/script/tl-react-wysiwyg.css");

	/**
	 * The token sheet of the design system package: the rules of the editor's sheet read its tokens
	 * (the focus ring of a selected image, the resize handle of a table column).
	 */
	private static final List<String> DESIGN_SYSTEM_TOKENS = List.of("/style/tl-design-system/tokens.css");

	@Override
	protected List<String> stylesheets() {
		return STYLESHEETS;
	}

	@Override
	protected List<String> tokenStylesheets() {
		return DESIGN_SYSTEM_TOKENS;
	}

	/**
	 * @see AbstractStylesheetTokenTest#suite(Class)
	 */
	public static Test suite() {
		return AbstractStylesheetTokenTest.suite(TestWysiwygStylesheetTokens.class);
	}

}
