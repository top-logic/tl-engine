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
 * The first question this test asks is the one that has to hold for every text in the module, in
 * both languages: does the whitelist accept it? The security configuration is then held to two
 * further demands of its own - a length cap, and no preformatted XML examples - because its editors
 * put these texts in front of an administrator who is deciding what an agent may do.
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

	/** The resource keys of the security configuration - the policy, its service and its mode. */
	private static final String SECURITY_PREFIX = MODULE_PREFIX + "security.";

	/** The languages the module ships. */
	private static final List<String> LANGUAGES = List.of("en", "de");

	/**
	 * The length at which a tooltip stops being a sentence and starts being an essay.
	 *
	 * <p>
	 * The English texts sit around 300 characters; their German translations run noticeably longer, so
	 * the limit leaves room for that without admitting a page of prose. For scale: the texts this test
	 * was written to keep out were between 700 and 1900 characters.
	 * </p>
	 */
	private static final int MAX_TOOLTIP_LENGTH = 450;

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

	/** A tooltip of the security configuration is a sentence or three, not the design document. */
	public void testTooltipsStayShort() throws IOException {
		List<String> tooLong = new ArrayList<>();
		for (String language : LANGUAGES) {
			for (Map.Entry<String, String> resource : securityResources(language).entrySet()) {
				if (resource.getValue().length() > MAX_TOOLTIP_LENGTH) {
					tooLong.add(language + ": " + resource.getKey()
						+ " (" + resource.getValue().length() + " characters)");
				}
			}
		}

		assertEquals("Longer than " + MAX_TOOLTIP_LENGTH + " characters, which means the JavaDoc is"
			+ " explaining the implementation to an administrator. Move that part into a comment the"
			+ " doclet does not export:\n  " + String.join("\n  ", tooLong), List.of(), tooLong);
	}

	/**
	 * No tooltip of the security configuration carries a preformatted block.
	 *
	 * <p>
	 * {@code <pre>} is on the whitelist, so the safe-HTML check cannot object to it - but in one of
	 * these tooltips it only ever means an XML example, which is a thing an administrator reads in the
	 * documentation and not in a hover text.
	 * </p>
	 */
	public void testNoTooltipCarriesAnXmlExample() throws IOException {
		List<String> withExample = new ArrayList<>();
		for (String language : LANGUAGES) {
			for (Map.Entry<String, String> resource : securityResources(language).entrySet()) {
				if (resource.getValue().contains("<pre>")) {
					withExample.add(language + ": " + resource.getKey());
				}
			}
		}

		assertEquals("An XML example belongs in doc/security/tool-access-configuration.md, not in a"
			+ " tooltip:\n  " + String.join("\n  ", withExample), List.of(), withExample);
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

	/**
	 * The labels and tooltips of the security configuration, as the application reads them.
	 *
	 * @param language
	 *        The language suffix of the resource bundle.
	 * @return The resources whose key names the security package.
	 */
	private static Map<String, String> securityResources(String language) throws IOException {
		Map<String, String> result = new TreeMap<>();
		for (Map.Entry<String, String> resource : moduleResources(language).entrySet()) {
			if (resource.getKey().startsWith(SECURITY_PREFIX)) {
				result.put(resource.getKey(), resource.getValue());
			}
		}
		return result;
	}
}
