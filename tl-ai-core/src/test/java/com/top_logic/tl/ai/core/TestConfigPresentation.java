/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.HTMLChecker;
import com.top_logic.basic.html.SafeHTML;

/**
 * Guards that every label and tooltip this module generates can actually be rendered.
 *
 * <p>
 * The JavaDoc of a configuration property <em>is</em> the administrator documentation: the build's
 * doclet exports it to {@code META-INF/messages_*.properties} and the editor renders it as the
 * tooltip of the corresponding field. An XML element name written into that JavaDoc reaches the
 * resource unescaped, and the editor's safe-HTML renderer refuses the unknown tag - the
 * administrator then reads an error message where the documentation should have been.
 * </p>
 *
 * <p>
 * {@link com.top_logic.tl.ai.core.security.TestSecurityConfigPresentation} makes further demands on
 * the security configuration alone (a length cap, no preformatted XML examples). This test asks the
 * one question that has to hold for every text in the module, in both languages: does the whitelist
 * accept it?
 * </p>
 *
 * <p>
 * What is checked is the generated resource file rather than the Java source, because that file is
 * what the running application reads.
 * </p>
 */
public class TestConfigPresentation extends BasicTestCase {

	/** The classpath location the doclet writes and the application reads. */
	private static final String BUNDLE = "META-INF/messages_";

	/** The resource keys this module owns. */
	private static final String MODULE_PREFIX = "com.top_logic.tl.ai.core.";

	/** The languages the module ships. */
	private static final List<String> LANGUAGES = List.of("en", "de");

	/**
	 * The safe-HTML check needs the service that owns the tag whitelist.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigPresentation.class, SafeHTML.Module.INSTANCE));
	}

	/**
	 * The positive control: the whitelist is really being applied, and it really does reject an XML
	 * element name.
	 *
	 * <p>
	 * Without this the other cases would pass vacuously whenever the service is not running.
	 * </p>
	 */
	public void testTheWhitelistIsActive() {
		try {
			SafeHTML.getInstance().check("Provide the names as an <options> generator.");
			fail("An unknown tag must be rejected; otherwise this test proves nothing.");
		} catch (I18NException expected) {
			// The editor turns exactly this into the "invalid tag name" text it renders instead of the
			// tooltip.
		}
	}

	/** No label or tooltip of this module may be refused by the whitelist. */
	public void testEveryTextSurvivesTheWhitelist() throws IOException {
		HTMLChecker checker = SafeHTML.getInstance();
		List<String> rejected = new ArrayList<>();
		for (String language : LANGUAGES) {
			for (Map.Entry<String, String> resource : moduleResources(language).entrySet()) {
				try {
					checker.check(resource.getValue());
				} catch (I18NException ex) {
					rejected.add(language + ": " + resource.getKey() + " -> " + ex.getMessage());
				}
			}
		}

		assertEquals("The editor renders these as an error message instead of as documentation."
			+ " An XML element name in the JavaDoc has to be escaped (&lt;options&gt;) or left out:\n  "
			+ String.join("\n  ", rejected), List.of(), rejected);
	}

	/** Precondition: the resources this test inspects exist. */
	public void testTheResourcesAreThere() throws IOException {
		for (String language : LANGUAGES) {
			int found = moduleResources(language).size();

			assertTrue("Only " + found + " resources found for '" + language + "'. Either the bundle moved"
				+ " or the doclet did not run; this test would silently check almost nothing.",
				found > 500);
		}
	}

	/**
	 * The labels and tooltips this module generates, as the application reads them.
	 *
	 * @param language
	 *        The language suffix of the resource bundle.
	 * @return The resources whose key names this module, from every bundle on the classpath.
	 */
	private static Map<String, String> moduleResources(String language) throws IOException {
		Map<String, String> result = new TreeMap<>();
		Enumeration<URL> bundles =
			TestConfigPresentation.class.getClassLoader().getResources(BUNDLE + language + ".properties");
		while (bundles.hasMoreElements()) {
			Properties properties = new Properties();
			try (InputStream in = bundles.nextElement().openStream();
					Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			for (String key : properties.stringPropertyNames()) {
				if (key.startsWith(MODULE_PREFIX)) {
					result.put(key, properties.getProperty(key));
				}
			}
		}
		return result;
	}
}
