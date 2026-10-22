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

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ViewScripts;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;

/**
 * Checks the scripts of every view a module ships, without instantiating the views: no script that
 * sees its values as variables declares a parameter.
 *
 * <p>
 * A script property sees its values as variables when its getter is annotated with
 * {@link ScriptContextVariables}. Each such script in the parsed configuration of a view is checked
 * with {@link ViewScripts#checkNoParameter(Expr, List)}, with the variables the script editor
 * offers. A property is covered as soon as it carries the annotation.
 * </p>
 *
 * <p>
 * The check needs neither the application model nor a knowledge base, so it covers the views of
 * modules whose elements cannot be instantiated in a test, unlike {@link TestLoadViews}. The views
 * are the files below {@link TestLoadViews#VIEWS_DIR} of the module the test runs in. A module with
 * views runs this test from its test suite with a class whose {@code suite()} method returns
 * {@link #suite(Class)} for itself.
 * </p>
 */
public class TestViewScripts extends TestCase {

	private final String _viewPath;

	/**
	 * Creates a {@link TestViewScripts} for one view.
	 *
	 * @param viewPath
	 *        The full path of the view, as {@link ViewLoader#fullPath(String)} produces it.
	 */
	public TestViewScripts(String viewPath) {
		super(viewPath);
		_viewPath = viewPath;
	}

	@Override
	protected void runTest() throws Throwable {
		// Loading a class of the view may read session settings (e.g. the time zone).
		ViewElement.Config config =
			ThreadContext.inSystemContext(TestViewScripts.class, () -> ViewLoader.loadConfig(_viewPath));
		List<String> problems = new ArrayList<>();
		check(config, problems);
		if (!problems.isEmpty()) {
			fail("Scripts declaring a parameter in '" + _viewPath + "':\n" + String.join("\n", problems));
		}
	}

	/**
	 * Checks the scripts in the given configuration tree.
	 *
	 * @param item
	 *        The root of the configuration tree.
	 * @param problems
	 *        The list to add a description of each script declaring a parameter to.
	 */
	public static void check(ConfigurationItem item, List<String> problems) {
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isDerived() || property.hasContainerAnnotation()) {
				continue;
			}
			Object value = item.value(property);
			if (value instanceof Expr script) {
				checkScript(item, property, script, problems);
				continue;
			}
			switch (property.kind()) {
				case ITEM:
					descend(value, problems);
					break;
				case LIST:
				case ARRAY:
					if (value instanceof Collection<?> entries) {
						for (Object entry : entries) {
							descend(entry, problems);
						}
					} else if (value instanceof Object[] entries) {
						for (Object entry : entries) {
							descend(entry, problems);
						}
					}
					break;
				case MAP:
					if (value instanceof Map<?, ?> entries) {
						for (Object entry : entries.values()) {
							descend(entry, problems);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	private static void descend(Object value, List<String> problems) {
		// A script is checked by the property holding it; its own syntax tree holds no scripts.
		if (value instanceof ConfigurationItem child && !(value instanceof Expr)) {
			check(child, problems);
		}
	}

	private static void checkScript(ConfigurationItem item, PropertyDescriptor property, Expr script,
			List<String> problems) {
		if (property.getAnnotation(ScriptContextVariables.class) == null) {
			return;
		}
		List<String> variables =
			ScriptContextVariablesResolver.resolve(new DefaultValueModel(item, property));
		try {
			ViewScripts.checkNoParameter(script, variables);
		} catch (ConfigurationError ex) {
			problems.add("  " + property.getPropertyName() + " at " + item.location() + ": " + ex.getMessage());
		}
	}

	/**
	 * The test checking the views of this module.
	 */
	public static Test suite() {
		return suite(TestViewScripts.class);
	}

	/**
	 * The test checking the views of the module the test runs in.
	 *
	 * @param owner
	 *        The test class in the module, naming the suite.
	 */
	public static Test suite(Class<?> owner) {
		return TestLoadViews.viewSuite(owner, TestViewScripts::new);
	}
}
