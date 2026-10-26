/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.util.WithEmptiness;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * Test for the {@link WithEmptiness} semantics of {@link I18NStructuredText}.
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class TestI18NStructuredText extends TestCase {

	public void testEmptyConstant() {
		assertTrue(I18NStructuredText.EMPTY.isEmpty());
	}

	public void testBlankEntriesAreEmpty() {
		Map<Locale, StructuredText> content = new HashMap<>();
		content.put(Locale.ENGLISH, new StructuredText(""));
		content.put(Locale.GERMAN, new StructuredText());

		assertTrue(new I18NStructuredText(content).isEmpty());
	}

	public void testNonBlankEntryIsNotEmpty() {
		Map<Locale, StructuredText> content = new HashMap<>();
		content.put(Locale.ENGLISH, new StructuredText(""));
		content.put(Locale.GERMAN, new StructuredText("<p>Hallo</p>"));

		assertFalse(new I18NStructuredText(content).isEmpty());
	}

}
