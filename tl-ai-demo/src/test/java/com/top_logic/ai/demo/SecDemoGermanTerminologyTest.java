/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import junit.framework.Test;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryContent;

/**
 * The German user interface of this application says "Tool", never "Werkzeug".
 *
 * <p>
 * Both words are in use in German technical writing, and a user interface that mixes them reads as
 * if it were talking about two different things. "Tool" is the word, so a German text that says
 * "Werkzeug" fails the build - including one the build's machine translation produced for a key
 * somebody added without a German text.
 * </p>
 */
/*
 * The check reads the resource files rather than asking the running ResourcesModule, because a bundle
 * cannot enumerate its keys - and reading the files is also what states the rule where a developer
 * would look for it: every German resource of tl-ai, not only the ones a particular view happens to
 * render.
 */
public class SecDemoGermanTerminologyTest extends SecDemoPersonaTest {

	/** The word the German user interface uses instead. */
	private static final String REPLACEMENT = "Tool";

	/** The word no German text of this application may contain, in any compound. */
	private static final String FORBIDDEN = "Werkzeug";

	/** The classpath location of the resources the build's doclet writes. */
	private static final String CLASSPATH_BUNDLE = "META-INF/messages_de.properties";

	/** The key prefixes of the resources this project owns in a shared classpath bundle. */
	private static final List<String> OWN_KEY_PREFIXES =
		List.of("com.top_logic.tl.ai.", "com.top_logic.ai.");

	/** Where an application module keeps the resources it writes by hand. */
	private static final String RESOURCE_FOLDER = "/WEB-INF/conf/resources/";

	/** The hand-written German resource files of this project. */
	private static final Pattern OWN_RESOURCE_FILE =
		Pattern.compile(".*/((tl-ai|tl-chat|model\\.tl\\.ai)[^/]*)_de\\.properties");

	/**
	 * No German text of this application says "Werkzeug".
	 */
	public void testTheGermanUserInterfaceSaysTool() throws IOException {
		Map<String, String> offending = new TreeMap<>();
		collectOffending(classpathResources(), offending);
		collectOffending(applicationResources(), offending);

		assertEquals("The German user interface says \"" + REPLACEMENT + "\". These texts say \""
			+ FORBIDDEN + "\":\n  " + join(offending), Map.of(), offending);
	}

	/**
	 * Precondition: the files this test inspects were found.
	 *
	 * <p>
	 * Without it a moved bundle or a changed file name would turn the check above into one that
	 * inspects nothing and passes.
	 * </p>
	 */
	public void testTheResourcesAreThere() throws IOException {
		assertTrue("No German resource of this project found on the classpath. Either the bundle moved"
			+ " or the doclet did not run.", classpathResources().size() > 100);
		assertTrue("No hand-written German resource file of this project found under "
			+ RESOURCE_FOLDER + ".", applicationResources().size() > 20);
	}

	private static void collectOffending(Map<String, String> resources, Map<String, String> result) {
		for (Map.Entry<String, String> resource : resources.entrySet()) {
			if (resource.getValue().contains(FORBIDDEN)) {
				result.put(resource.getKey(), resource.getValue());
			}
		}
	}

	/** The German resources this project contributes to the classpath bundle. */
	private static Map<String, String> classpathResources() throws IOException {
		Map<String, String> result = new TreeMap<>();
		Enumeration<URL> bundles =
			SecDemoGermanTerminologyTest.class.getClassLoader().getResources(CLASSPATH_BUNDLE);
		while (bundles.hasMoreElements()) {
			Properties properties = read(bundles.nextElement());
			for (String key : properties.stringPropertyNames()) {
				if (isOwnKey(key)) {
					result.put(key, properties.getProperty(key));
				}
			}
		}
		return result;
	}

	/** The German resources this project writes by hand. */
	private static Map<String, String> applicationResources() throws IOException {
		Map<String, String> result = new TreeMap<>();
		for (String path : ownResourceFiles()) {
			Properties properties = read(FileManager.getInstance().getData(path));
			for (String key : properties.stringPropertyNames()) {
				result.put(key, properties.getProperty(key));
			}
		}
		return result;
	}

	private static Set<String> ownResourceFiles() {
		Set<String> result = new TreeSet<>();
		for (String path : FileManager.getInstance().getResourcePaths(RESOURCE_FOLDER)) {
			if (OWN_RESOURCE_FILE.matcher(path).matches()) {
				result.add(path);
			}
		}
		return result;
	}

	private static Properties read(URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			return read(in);
		}
	}

	private static Properties read(BinaryContent content) throws IOException {
		try (InputStream in = content.getStream()) {
			return read(in);
		}
	}

	private static Properties read(InputStream in) throws IOException {
		Properties properties = new Properties();
		try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static boolean isOwnKey(String key) {
		for (String prefix : OWN_KEY_PREFIXES) {
			if (key.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private static String join(Map<String, String> offending) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, String> entry : offending.entrySet()) {
			lines.add(entry.getKey() + " = " + entry.getValue());
		}
		return String.join("\n  ", lines);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoGermanTerminologyTest.class);
	}

}
