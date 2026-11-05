/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Checks that every function the scripts of a module's views call is known to TL-Script.
 *
 * <p>
 * A misspelled or missing function - e.g. one of a module the views' module does not depend on -
 * is otherwise reported only when the script is compiled, when its view is first displayed. Like
 * {@link TestViewScripts}, the check parses the views without instantiating them; it looks up the
 * name of every function call in the {@link SearchBuilder}, which knows the functions of all
 * modules on the class path. The views are the files below {@link TestLoadViews#VIEWS_DIR} of the
 * module the test runs in. A module runs this test from its test suite with a class whose
 * {@code suite()} method returns {@link #suite(Class)} for itself.
 * </p>
 */
public class TestViewScriptFunctions extends TestCase {

	private final String _viewPath;

	/**
	 * Creates a {@link TestViewScriptFunctions} for one view.
	 *
	 * @param viewPath
	 *        The full path of the view, as {@link ViewLoader#fullPath(String)} produces it.
	 */
	public TestViewScriptFunctions(String viewPath) {
		super(viewPath);
		_viewPath = viewPath;
	}

	@Override
	protected void runTest() throws Throwable {
		// Loading a class of the view may read session settings (e.g. the time zone).
		ViewElement.Config config =
			ThreadContext.inSystemContext(TestViewScriptFunctions.class, () -> ViewLoader.loadConfig(_viewPath));
		Set<String> functions = new TreeSet<>();
		collectFunctions(config, functions);

		SearchBuilder<?> builder = SearchBuilder.getInstance();
		List<String> unknown = new ArrayList<>();
		for (String function : functions) {
			if (builder.getBuilder(function) == null) {
				unknown.add(function);
			}
		}
		if (!unknown.isEmpty()) {
			fail("Unknown TL-Script functions in '" + _viewPath + "': " + unknown);
		}
	}

	/**
	 * Adds the names of the functions called by the scripts in the given configuration tree.
	 *
	 * @param item
	 *        The root of the configuration tree.
	 * @param functions
	 *        The set to add the names to.
	 */
	public static void collectFunctions(ConfigurationItem item, Set<String> functions) {
		if (item instanceof Expr.Method call) {
			functions.add(call.getName());
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isDerived() || property.hasContainerAnnotation()) {
				continue;
			}
			descend(item.value(property), functions);
		}
	}

	private static void descend(Object value, Set<String> functions) {
		if (value instanceof ConfigurationItem child) {
			collectFunctions(child, functions);
		} else if (value instanceof Collection<?> entries) {
			for (Object entry : entries) {
				descend(entry, functions);
			}
		} else if (value instanceof Object[] entries) {
			for (Object entry : entries) {
				descend(entry, functions);
			}
		} else if (value instanceof Map<?, ?> entries) {
			for (Object entry : entries.values()) {
				descend(entry, functions);
			}
		}
	}

	/**
	 * The test checking the views of this module.
	 */
	public static Test suite() {
		return suite(TestViewScriptFunctions.class);
	}

	/**
	 * The test checking the views of the module the test runs in.
	 *
	 * @param owner
	 *        The test class in the module, naming the suite.
	 */
	public static Test suite(Class<?> owner) {
		return TestLoadViews.viewSuite(owner, TestViewScriptFunctions::new, SearchBuilder.Module.INSTANCE);
	}
}
