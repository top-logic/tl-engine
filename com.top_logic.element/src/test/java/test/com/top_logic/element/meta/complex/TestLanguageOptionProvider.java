/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.complex;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.top_logic.element.meta.complex.LanguageOptionProvider;
import com.top_logic.element.meta.form.EditContext;

/**
 * {@link TestCase} for {@link LanguageOptionProvider}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestLanguageOptionProvider extends TestCase {

	/**
	 * Test whether {@link LanguageOptionProvider#getOptions(EditContext)} returns the correct set of
	 * languages.
	 */
	public void testReturnsAllIsoLanguages() {
		Locale[] displayLanguages = { Locale.getDefault(), Locale.ENGLISH, Locale.GERMAN };
		String[] isoLanguages = Locale.getISOLanguages();

		for (Locale displayLanguage : displayLanguages) {
			List<Locale> languageOptions = LanguageOptionProvider.INSTANCE.getOptionsList(displayLanguage);
			assertEquals("Not all or too much languages returned! ", isoLanguages.length, languageOptions.size());
			for (String isoLanguage : isoLanguages) {
				assertTrue("Missing language: '" + isoLanguage + "'", languageOptions.contains(new Locale(isoLanguage)));
			}
		}
	}

}
