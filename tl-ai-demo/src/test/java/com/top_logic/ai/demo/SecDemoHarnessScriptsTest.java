/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Every TL-Script file of {@code doc/security/harness} still compiles.
 *
 * <p>
 * The harness scripts are documentation a human pastes into the Script console, so nothing else in
 * the build looks at them. Without this test they rot silently: a renamed model part, a renamed tool
 * or a removed script function turns them into a wall of red the first time somebody tries to use the
 * test bed, which is exactly the moment they were supposed to save time.
 * </p>
 *
 * <p>
 * What is checked is that each file <em>parses and compiles</em> against the application model and the
 * registered script functions - the same {@link QueryExecutor#compile} path the console uses. Not that
 * it produces the right answer: {@code fire.tlscript} starts real agent runs and
 * {@code reset.tlscript} deletes objects, so executing them here would be a different test with much
 * larger side effects. Their behaviour is covered by the other {@code SecDemo*} tests and by the
 * scenarios in {@code doc/security/testbed.md}.
 * </p>
 *
 * <p>
 * Compilation is the check that catches the failures these scripts actually suffer, because TL-Script
 * resolves model literals and function names at compile time: a `secdemo:Note#title` that no longer
 * exists, or an {@code aiCallTool} that was renamed, fails here rather than in front of a tester.
 * </p>
 */
public class SecDemoHarnessScriptsTest extends SecDemoPersonaTest {

	/** Relative to the repository root. */
	private static final String HARNESS_DIRECTORY = "doc/security/harness";

	/** Every script a human is told to paste into the console. */
	public void testEveryHarnessScriptCompiles() throws IOException {
		List<Path> scripts = harnessScripts();
		assertFalse("No harness script found in '" + HARNESS_DIRECTORY + "'.", scripts.isEmpty());

		List<String> failures = new ArrayList<>();
		for (Path script : scripts) {
			String source = Files.readString(script, StandardCharsets.UTF_8);
			try {
				QueryExecutor.compile(parse(source));
			} catch (RuntimeException ex) {
				failures.add(script.getFileName() + ": " + ex.getMessage());
			}
		}

		assertTrue("Harness scripts that no longer compile:\n  " + String.join("\n  ", failures),
			failures.isEmpty());
	}

	/**
	 * The set of scripts is the set the documentation names, so a new one is covered automatically and
	 * a deleted one is noticed.
	 */
	public void testTheDocumentedScriptsAreThere() throws IOException {
		List<String> names = new ArrayList<>();
		for (Path script : harnessScripts()) {
			names.add(script.getFileName().toString());
		}

		for (String expected : List.of("probe.tlscript", "fire.tlscript", "collect.tlscript",
			"reset.tlscript")) {
			assertTrue("doc/security/testbed.md names '" + expected + "', which is missing. Update"
				+ " both, or neither: a scenario table pointing at a file that is gone is worse than"
				+ " no scenario table. Found: " + names, names.contains(expected));
		}
	}

	private static List<Path> harnessScripts() throws IOException {
		Path directory = harnessDirectory();
		try (var entries = Files.list(directory)) {
			return entries
				.filter(path -> path.getFileName().toString().endsWith(".tlscript"))
				.sorted()
				.toList();
		}
	}

	/**
	 * Locates {@code doc/security/harness} by walking up from the working directory.
	 *
	 * <p>
	 * The working directory of a surefire run is the module, and the scripts live at the repository
	 * root, so the relative path depends on how the build was invoked. Searching upwards is what makes
	 * the test work from the module and from the reactor root alike.
	 * </p>
	 */
	private static Path harnessDirectory() {
		Path candidate = Paths.get("").toAbsolutePath();
		while (candidate != null) {
			Path harness = candidate.resolve(HARNESS_DIRECTORY);
			if (Files.isDirectory(harness)) {
				return harness;
			}
			candidate = candidate.getParent();
		}
		throw new AssertionError("Could not find '" + HARNESS_DIRECTORY + "' above the working"
			+ " directory " + Paths.get("").toAbsolutePath() + ".");
	}

	private static Expr parse(String script) {
		try {
			return ExprFormat.INSTANCE.getValue("script", script);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to parse: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoHarnessScriptsTest.class);
	}

}
