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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.BinaryContent;

/**
 * The German user interface of this application says "Tool", never "Werkzeug".
 *
 * <p>
 * Both words are in use in German technical writing, and a user interface that mixes them reads as
 * if it were talking about two different things. "Tool" is the word, so a German text that says
 * "Werkzeug" fails the build - in any compound, "Werkzeugaufruf" as well as "Einzelwerkzeuge", and
 * including one the build's machine translation produced for a key somebody added without a German
 * text.
 * </p>
 *
 * <p>
 * The rule holds for both halves of the product: the resources of the Java side and the strings of
 * the React chat, which keeps its own German texts in <code>ui/src/i18n.ts</code> (#633).
 * </p>
 */
/*
 * The check reads the resource files rather than asking the running ResourcesModule, because a bundle
 * cannot enumerate its keys - and reading the files is also what states the rule where a developer
 * would look for it: every German resource of tl-ai, not only the ones a particular view happens to
 * render. The generated bundles and the chat's strings are read from the sources of the reactor, so
 * that a module the application does not ship (tl-ai-eval) is held to the same rule.
 */
public class SecDemoGermanTerminologyTest extends BasicTestCase {

	/** The word the German user interface uses instead. */
	private static final String REPLACEMENT = "Tool";

	/** The word no German text of this application may contain, in any compound. */
	private static final String FORBIDDEN = "Werkzeug";

	/**
	 * Identifies the POM at the root of the tl-ai reactor.
	 *
	 * <p>
	 * The artifact name alone does not: every module names the same artifact as its parent. Only the
	 * root POM names it and is itself an aggregator.
	 * </p>
	 */
	private static final List<String> REACTOR_POM_MARKERS =
		List.of("<artifactId>tl-ai-parent</artifactId>", "<packaging>pom</packaging>");

	/** Where the build's doclet writes the German resources of a module. */
	private static final String MODULE_BUNDLE = "src/main/java/META-INF/messages_de.properties";

	/** Where a module with a React user interface keeps that interface's strings. */
	private static final String UI_STRINGS = "ui/src/i18n.ts";

	/** The line that opens the German half of {@link #UI_STRINGS}. */
	private static final String UI_GERMAN_START = "const DE: Bundle = {";

	/** The line that closes it. */
	private static final String UI_GERMAN_END = "}";

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
		collectOffending(moduleResources(), offending);
		collectOffending(applicationResources(), offending);
		collectOffending(uiStrings(), offending);

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
		assertTrue("No German resource bundle of this project found under " + reactorRoot() + "/*/"
			+ MODULE_BUNDLE + ". Either the bundles moved or the doclet did not run.",
			moduleResources().size() > 100);
		assertTrue("No hand-written German resource file of this project found under "
			+ RESOURCE_FOLDER + ".", applicationResources().size() > 20);
		assertTrue("No German string of the React chat found in " + reactorRoot() + "/*/" + UI_STRINGS
			+ " between \"" + UI_GERMAN_START + "\" and \"" + UI_GERMAN_END + "\". Either the file moved"
			+ " or its German half is no longer declared that way.", uiStrings().size() > 100);
	}

	private static void collectOffending(Map<String, String> resources, Map<String, String> result) {
		String forbidden = FORBIDDEN.toLowerCase(Locale.GERMAN);
		for (Map.Entry<String, String> resource : resources.entrySet()) {
			if (resource.getValue().toLowerCase(Locale.GERMAN).contains(forbidden)) {
				result.put(resource.getKey(), resource.getValue());
			}
		}
	}

	/** The German resources the doclet writes, of every module of the reactor. */
	private static Map<String, String> moduleResources() throws IOException {
		Map<String, String> result = new TreeMap<>();
		for (Path bundle : reactorFiles(MODULE_BUNDLE)) {
			Properties properties = read(bundle);
			for (String key : properties.stringPropertyNames()) {
				result.put(key, properties.getProperty(key));
			}
		}
		return result;
	}

	/**
	 * The German strings of the React user interfaces, one per line, keyed by file and line number.
	 */
	private static Map<String, String> uiStrings() throws IOException {
		Path root = reactorRoot();
		Map<String, String> result = new TreeMap<>();
		for (Path strings : reactorFiles(UI_STRINGS)) {
			List<String> lines = Files.readAllLines(strings, StandardCharsets.UTF_8);
			int start = lines.indexOf(UI_GERMAN_START);
			if (start < 0) {
				continue;
			}
			for (int n = start + 1; n < lines.size() && !lines.get(n).equals(UI_GERMAN_END); n++) {
				result.put(root.relativize(strings) + ":" + (n + 1), lines.get(n).strip());
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

	private static Properties read(Path file) throws IOException {
		try (InputStream in = Files.newInputStream(file)) {
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

	/** The file at the given path below every module of the reactor that has one. */
	private static List<Path> reactorFiles(String path) throws IOException {
		try (Stream<Path> modules = Files.list(reactorRoot())) {
			return modules.map(module -> module.resolve(path)).filter(Files::isRegularFile).sorted().toList();
		}
	}

	/**
	 * The directory holding the POM of the reactor this test runs in.
	 *
	 * <p>
	 * Surefire runs a test with the directory of its own module as the working directory, and the
	 * modules of this project are directories next to the reactor POM - so walking upwards finds the
	 * root, from a worktree as well as from the main checkout.
	 * </p>
	 */
	private static Path reactorRoot() throws IOException {
		for (Path candidate = Path.of("").toAbsolutePath(); candidate != null; candidate =
			candidate.getParent()) {
			Path pom = candidate.resolve("pom.xml");
			if (Files.isRegularFile(pom) && isReactorPom(Files.readString(pom, StandardCharsets.UTF_8))) {
				return candidate;
			}
		}
		throw new IOException("No POM containing " + REACTOR_POM_MARKERS + " found at or above "
			+ Path.of("").toAbsolutePath() + ". This test needs the sources of the reactor it belongs to.");
	}

	private static boolean isReactorPom(String pom) {
		for (String marker : REACTOR_POM_MARKERS) {
			if (!pom.contains(marker)) {
				return false;
			}
		}
		return true;
	}

	private static String join(Map<String, String> offending) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, String> entry : offending.entrySet()) {
			lines.add(entry.getKey() + " = " + entry.getValue());
		}
		return String.join("\n  ", lines);
	}

	/**
	 * All this test needs is the {@link FileManager} over the web application.
	 *
	 * <p>
	 * No knowledge base, no accounts, no tool access policy: the check reads resource files. Booting
	 * the security test bed for it would cost a full application start-up and prove nothing more.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(SecDemoGermanTerminologyTest.class);
	}

}
