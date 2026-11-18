/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import com.top_logic.layout.view.table.RowRoleConfig;
import com.top_logic.model.annotate.ui.ValueColor;

/**
 * Checks that no view of the engine marks a state with a status class
 * <code>tl-status-&lt;role&gt;</code> for one of the state roles {@link ValueColor#ERROR},
 * {@link ValueColor#WARNING}, {@link ValueColor#SUCCESS} and {@link ValueColor#INFO}.
 *
 * <p>
 * The client tints a row or cell only for a role, computed by
 * {@link RowRoleConfig#getRowRole()} and the other role properties; a class script is a styling
 * hook for the application's own stylesheet. A view delivering a status class in a class script
 * therefore loads without an error but shows no mark.
 * </p>
 *
 * <p>
 * The views checked are the files below {@value TestLoadViews#VIEWS_DIR} of every module of the
 * repository the test module is part of, that is of every sibling directory of the module
 * directory the test runs in. Only view files are searched, so CSS variables such as
 * <code>--tl-status-warning</code> in stylesheets are not reported; within a view, a name
 * following a hyphen is not taken for a status class either.
 * </p>
 */
public class TestNoStatusClasses extends TestCase {

	/**
	 * Prefix of a status class naming a state role.
	 */
	private static final String STATUS_CLASS_PREFIX = "tl-status-";

	private static final String VIEW_SUFFIX = ".view.xml";

	/**
	 * The roles for which a status class existed.
	 */
	private static final List<ValueColor> STATE_ROLES =
		List.of(ValueColor.ERROR, ValueColor.WARNING, ValueColor.SUCCESS, ValueColor.INFO);

	/**
	 * Matches a status class of a state role that is not part of a longer name (such as the CSS
	 * variable <code>--tl-status-warning</code>).
	 */
	private static final Pattern STATUS_CLASS = Pattern.compile(
		"(?<![-\\w])" + Pattern.quote(STATUS_CLASS_PREFIX) + "("
			+ STATE_ROLES.stream().map(role -> Pattern.quote(role.getExternalName()))
				.collect(Collectors.joining("|"))
			+ ")(?![-\\w])");

	/**
	 * Searches the views of all modules for status classes of state roles.
	 */
	public void testNoStatusClassInViews() throws IOException {
		File moduleDir = new File("").getAbsoluteFile();
		File ownViews = new File(moduleDir, TestLoadViews.VIEWS_DIR);
		assertTrue("The test does not run in a module with views: " + moduleDir, ownViews.isDirectory());

		File repositoryDir = moduleDir.getParentFile();
		File[] modules = repositoryDir.listFiles(File::isDirectory);
		assertNotNull("Cannot list the modules in: " + repositoryDir, modules);

		List<String> findings = new ArrayList<>();
		int viewCount = 0;
		for (File module : modules) {
			File viewsDir = new File(module, TestLoadViews.VIEWS_DIR);
			if (!viewsDir.isDirectory()) {
				continue;
			}
			List<Path> views;
			try (Stream<Path> files = Files.walk(viewsDir.toPath())) {
				views = files.filter(file -> file.getFileName().toString().endsWith(VIEW_SUFFIX)).sorted().toList();
			}
			for (Path view : views) {
				viewCount++;
				search(repositoryDir.toPath(), view, findings);
			}
		}
		assertTrue("No views found in the modules of: " + repositoryDir, viewCount > 0);

		if (!findings.isEmpty()) {
			fail("Views mark a state with a status class instead of a role (e.g. row-class '"
				+ STATUS_CLASS_PREFIX + ValueColor.WARNING.getExternalName() + "' -> "
				+ RowRoleConfig.ROW_ROLE + " '" + ValueColor.WARNING.getExternalName() + "'):\n"
				+ String.join("\n", findings));
		}
	}

	private static void search(Path repositoryDir, Path view, List<String> findings) throws IOException {
		List<String> lines = Files.readAllLines(view, StandardCharsets.UTF_8);
		for (int n = 0; n < lines.size(); n++) {
			Matcher matcher = STATUS_CLASS.matcher(lines.get(n));
			while (matcher.find()) {
				findings.add(repositoryDir.relativize(view) + ":" + (n + 1) + ": " + matcher.group());
			}
		}
	}

}
