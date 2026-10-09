/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.codeedit;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.codeedit.CodeEditorControlProvider;
import com.top_logic.layout.react.field.FieldSpec;

/**
 * Tests how a {@link CodeEditorControlProvider} field is displayed where it has no room.
 */
public class TestCodeEditorPreview extends TestCase {

	/**
	 * A code editor always needs more room than a single line.
	 */
	public void testIsLarge() {
		assertTrue(provider().isLarge(FieldSpec.of(String.class, "Source")));
	}

	/**
	 * The first line of the source holding more than white space stands for it.
	 */
	public void testPreview() {
		CodeEditorControlProvider provider = provider();
		FieldSpec field = FieldSpec.of(String.class, "Source");

		assertEquals("", provider.previewText(field, null));
		assertEquals("", provider.previewText(field, "\n  \n"));
		assertEquals("function f() {", provider.previewText(field, "\n\n  function f() {\n  return 1;\n}"));
	}

	@SuppressWarnings("unchecked")
	private static CodeEditorControlProvider provider() {
		CodeEditorControlProvider.Config<CodeEditorControlProvider> config =
			TypedConfiguration.newConfigItem(CodeEditorControlProvider.Config.class);
		return new CodeEditorControlProvider(null, config);
	}

}
