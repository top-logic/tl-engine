/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.view.ViewLoader;

/**
 * Loads every view a module ships - parsing its configuration and instantiating it - and fails on
 * a configuration error.
 *
 * <p>
 * Instantiating a view compiles its scripts, so a view whose script is rejected at load time - for
 * instance a script declaring a parameter where it sees its values as variables - fails here
 * instead of when it is first displayed. The scripts are checked, but not compiled against the
 * application model, which the test does not start; a reference to an unknown model part is
 * therefore not reported.
 * </p>
 *
 * <p>
 * Instantiating needs whatever services the elements of the views need when they are created; for
 * modules whose elements need the application model, {@link TestViewScripts} checks the scripts
 * without instantiating the views.
 * </p>
 *
 * <p>
 * The views are the files below {@value #VIEWS_DIR} of the module the test runs in. A module with
 * views of its own runs this test from its test suite with a class whose {@code suite()} method
 * returns {@link #suite(Class)} for itself.
 * </p>
 */
public class TestLoadViews extends TestCase {

	/**
	 * The directory of the views of the module, relative to the module directory the tests run in.
	 */
	public static final String VIEWS_DIR = "src/main/webapp" + ViewLoader.VIEW_BASE_PATH;

	private static final String VIEW_SUFFIX = ".view.xml";

	private final String _viewPath;

	/**
	 * Creates a {@link TestLoadViews} for one view.
	 *
	 * @param viewPath
	 *        The full path of the view, as {@link ViewLoader#fullPath(String)} produces it.
	 */
	public TestLoadViews(String viewPath) {
		super(viewPath);
		_viewPath = viewPath;
	}

	@Override
	protected void runTest() throws Throwable {
		// An element may read session settings (e.g. the time zone) when it is instantiated.
		ThreadContext.inSystemContext(TestLoadViews.class, () -> ViewLoader.loadView(_viewPath));
	}

	/**
	 * The test loading the views of this module.
	 */
	public static Test suite() {
		return suite(TestLoadViews.class);
	}

	/**
	 * The test loading the views of the module the test runs in.
	 *
	 * @param owner
	 *        The test class in the module, naming the suite.
	 */
	public static Test suite(Class<?> owner) {
		return viewSuite(owner, TestLoadViews::new);
	}

	/**
	 * A suite with one test per view of the module the test runs in.
	 *
	 * @param owner
	 *        The test class in the module, naming the suite.
	 * @param testForView
	 *        Creates the test of a view from its full path, as {@link ViewLoader#fullPath(String)}
	 *        produces it.
	 * @return The suite, set up with the services parsing and instantiating a view needs.
	 */
	public static Test viewSuite(Class<?> owner, Function<String, Test> testForView) {
		TestSuite suite = new TestSuite(owner.getName());
		File viewsDir = new File(VIEWS_DIR);
		for (String viewRef : viewRefs(viewsDir)) {
			suite.addTest(testForView.apply(ViewLoader.fullPath(viewRef)));
		}
		if (suite.testCount() == 0) {
			suite.addTest(TestSuite.warning("No views found in '" + viewsDir.getAbsolutePath() + "'."));
		}
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(suite,
			TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

	private static List<String> viewRefs(File viewsDir) {
		List<String> result = new ArrayList<>();
		collect(viewsDir, "", result);
		Collections.sort(result);
		return result;
	}

	private static void collect(File dir, String prefix, List<String> result) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				collect(file, prefix + file.getName() + "/", result);
			} else if (file.getName().endsWith(VIEW_SUFFIX)) {
				result.add(prefix + file.getName());
			}
		}
	}
}
