/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Test;

import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.layout.AdminToolDescriptions;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService.ToolFactoryConfig;

/**
 * Every tool the framework ships is described for an administrator, in both languages.
 *
 * <p>
 * The catalog's description column is what tells an administrator what they are granting. A tool the
 * framework ships and nobody described would show the text the language model is given instead - in
 * English, written for a caller getting arguments right - so a newly registered framework tool fails
 * the build until somebody writes the one sentence.
 * </p>
 *
 * <p>
 * Tools that arrive while the application runs are the deliberate exception: a TL-Script tool
 * authored in the application and a tool of a connected server have nobody who could have written a
 * sentence for them, and the catalog marks their fallback per row.
 * </p>
 *
 * @see AdminToolDescriptions
 */
public class SecDemoToolDescriptionTest extends SecDemoPersonaTest {

	/**
	 * What a German description must not contain.
	 *
	 * <p>
	 * "Werkzeug" and "Whitelist" are what the build's machine translation produces; the German user
	 * interface says "Tool" and "Zulassungsliste". The last two are the dashes this repository does
	 * not use.
	 * </p>
	 */
	private static final List<String> FORBIDDEN_IN_GERMAN =
		List.of("Werkzeug", "Whitelist", "\u2014", "\u2013");

	/** The languages the application ships resources for. */
	private static final List<Locale> LANGUAGES = List.of(Locale.GERMAN, Locale.ENGLISH);

	/**
	 * Every registered Java tool has a description in every shipped language.
	 */
	public void testEveryFrameworkToolIsDescribed() {
		Map<String, List<String>> missing = new TreeMap<>();
		for (String qualifiedName : describableTools()) {
			for (Locale language : LANGUAGES) {
				if (lookup(language, AdminToolDescriptions.keyString(qualifiedName)) == null) {
					missing.computeIfAbsent(qualifiedName, name -> new ArrayList<>())
						.add(language.getLanguage());
				}
			}
		}

		assertEquals("Undescribed tools. Add one sentence per language to"
			+ " tl-ai-core/src/main/webapp/WEB-INF/conf/resources/tl-ai-core.app-messages_*.properties"
			+ " under the key '" + AdminToolDescriptions.PREFIX + ".{factory}.{tool}':\n  "
			+ missing, Map.of(), missing);
	}

	/**
	 * The two languages say different things, which is the check that catches a copied file.
	 */
	public void testTheDescriptionsAreTranslated() {
		List<String> untranslated = new ArrayList<>();
		for (String qualifiedName : describableTools()) {
			String key = AdminToolDescriptions.keyString(qualifiedName);
			String german = lookup(Locale.GERMAN, key);
			String english = lookup(Locale.ENGLISH, key);
			if (german != null && german.equals(english)) {
				untranslated.add(qualifiedName);
			}
		}

		assertEquals("Identical in German and English, which means one of the two was never written.",
			List.of(), untranslated);
	}

	/**
	 * No German description uses the words the German user interface does not use.
	 *
	 * <p>
	 * The build machine-translates a key that is missing from the German bundle, and its output says
	 * "Werkzeug" and "Whitelist" where the German user interface says "Tool" and "Zulassungsliste". A
	 * description that slipped through untranslated is what this catches.
	 * </p>
	 */
	public void testTheGermanDescriptionsUseTheDocumentedWords() {
		Map<String, String> offending = new TreeMap<>();
		for (String qualifiedName : describableTools()) {
			String german = lookup(Locale.GERMAN, AdminToolDescriptions.keyString(qualifiedName));
			if (german == null) {
				continue;
			}
			for (String word : FORBIDDEN_IN_GERMAN) {
				if (german.contains(word)) {
					offending.put(qualifiedName, word + " in: " + german);
				}
			}
		}

		assertEquals("German descriptions have to say Tool rather than Werkzeug, Zulassungsliste"
			+ " rather than Whitelist, and never an em or en dash:\n  " + offending, Map.of(), offending);
	}

	/**
	 * The fallback really is the exception: the fixture's own TL-Script tools have no description, and
	 * the catalog is what marks them.
	 */
	public void testApplicationToolsFallBack() {
		assertFalse("A TL-Script tool of the application has nobody to write a sentence for it.",
			AdminToolDescriptions.isDescribed(CLASSIFIED_READ_SCRIPT_TOOL));
		assertTrue("A tool the framework ships has one.",
			AdminToolDescriptions.isDescribed(MODEL_READ_TOOL));
	}

	/**
	 * The tools that must be described: every registered one except the ones a factory produces from
	 * configuration written in the running application.
	 */
	private static List<String> describableTools() {
		TreeSet<String> scriptFactories = new TreeSet<>();
		for (Map.Entry<String, ToolFactoryConfig> factory :
				ToolRegistryService.getInstance().getConfig().getToolFactories().entrySet()) {
			Class<?> implementation = factory.getValue().getImplementation().getImplementationClass();
			if (implementation != null && ScriptToolFactory.class.isAssignableFrom(implementation)) {
				scriptFactories.add(factory.getKey());
			}
		}

		List<String> result = new ArrayList<>();
		for (Tool tool : ToolRegistryService.getInstance().getTools()) {
			String qualifiedName = AiToolAccessService.identities().identify(tool).displayName();
			int dot = qualifiedName.indexOf('.');
			String factory = dot < 0 ? "" : qualifiedName.substring(0, dot);
			if (!scriptFactories.contains(factory)) {
				result.add(qualifiedName);
			}
		}
		assertTrue("The registry has to hold the framework's tools; without them this test would"
			+ " check nothing.", result.size() > 50);
		return result;
	}

	/**
	 * The text of one resource in one language, without the fallback to another language.
	 *
	 * @return The text, or <code>null</code> if that language does not have it - which is exactly what
	 *         the fallback would hide.
	 */
	private static String lookup(Locale language, String key) {
		I18NBundleSPI bundle = (I18NBundleSPI) ResourcesModule.getInstance().getBundle(language);
		return bundle.lookup(key, false);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoToolDescriptionTest.class);
	}

}
