/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.ui;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.graphic.flow.server.ui.ChartHandlerVariables;
import com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder;
import com.top_logic.graphic.flow.server.ui.handler.ScriptedClickHandler;

/**
 * Test for {@link ChartHandlerVariables}.
 */
public class TestChartHandlerVariables extends BasicTestCase {

	/**
	 * The provider returns the keys of the
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config#getHandlers()
	 * handlers} map, which are the names bound as implicit variables around the
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config#getCreateChart()
	 * create chart script} (see the constructor's
	 * <code>lambda(handler.getKey(), createChart)</code> wrapping and the matching
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config.SyntaxCheck}).
	 */
	public void testReturnsHandlerNames() {
		ScriptFlowChartBuilder.Config<?> config = TypedConfiguration.newConfigItem(ScriptFlowChartBuilder.Config.class);
		config.getHandlers().put("h1", handler("h1"));
		config.getHandlers().put("h2", handler("h2"));

		List<String> variables = new ChartHandlerVariables().getVariablesFromModel(config);

		assertEquals(2, variables.size());
		assertTrue(variables.contains("h1"));
		assertTrue(variables.contains("h2"));
	}

	/**
	 * A model that is not a
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config} yields no
	 * variables, guarding against a {@link ClassCastException} when the annotated property is
	 * inspected in an unrelated context.
	 */
	public void testEmptyForUnrelatedModel() {
		ScriptedClickHandler.Config<?> other = TypedConfiguration.newConfigItem(ScriptedClickHandler.Config.class);

		List<String> variables = new ChartHandlerVariables().getVariablesFromModel(other);

		assertTrue(variables.isEmpty());
	}

	/**
	 * A minimal, instantiable
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config.HandlerDefinition}
	 * with the given name, suitable as a value in the
	 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config#getHandlers()} map.
	 */
	private static ScriptedClickHandler.Config<?> handler(String name) {
		ScriptedClickHandler.Config<?> handler = TypedConfiguration.newConfigItem(ScriptedClickHandler.Config.class);
		handler.setName(name);
		return handler;
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestChartHandlerVariables.class));
	}
}
