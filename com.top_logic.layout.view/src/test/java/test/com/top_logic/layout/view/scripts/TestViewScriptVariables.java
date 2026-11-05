/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.scripts;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ViewScripts;
import com.top_logic.layout.view.table.ComputedColumn;

/**
 * Tests that instantiating a view rejects a script using a variable it does not see, see
 * {@link ViewScripts#checkVariables}.
 *
 * <p>
 * The services are those of {@link test.com.top_logic.layout.view.TestLoadViews}: the scripts are
 * not compiled against the application model, so the check must work on the parsed scripts.
 * </p>
 */
public class TestViewScriptVariables extends TestCase {

	private static final String UNDEFINED_VIEW = "undefined-variable.view.xml";

	private static final String DECLARED_VIEW = "declared-variable.view.xml";

	/**
	 * A computed column using a variable it does not declare fails the view with a message naming
	 * the view and the line of the column, the script property, the column, and the variable.
	 */
	public void testUndefinedVariable() throws Exception {
		String error = instantiationError(UNDEFINED_VIEW);
		assertNotNull("An undefined variable must be rejected.", error);
		assertTrue(error, error.contains(UNDEFINED_VIEW + " line 8"));
		assertTrue(error, error.contains("\"" + ComputedColumn.Config.VALUE + "\" of \"undefinedColumn\""));
		assertTrue(error, error.contains("$undefined"));
	}

	/**
	 * The same variable declared as input of the column, a variable bound by a function in the
	 * script, and the row the column supplies are no errors.
	 */
	public void testDefinedVariables() throws Exception {
		assertNull(instantiationError(DECLARED_VIEW));
	}

	private static String instantiationError(String view) throws Exception {
		ViewElement.Config config = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestViewScriptVariables.class, view)));
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		ThreadContext.inSystemContext(TestViewScriptVariables.class, () -> context.getInstance(config));
		return log.hasErrors() ? log.getError() : null;
	}

	/**
	 * The test suite, with the services {@link test.com.top_logic.layout.view.TestLoadViews}
	 * instantiates views with.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestViewScriptVariables.class,
			TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
