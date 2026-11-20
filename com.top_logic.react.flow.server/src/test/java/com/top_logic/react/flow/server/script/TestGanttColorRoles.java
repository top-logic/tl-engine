/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.script;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for the color role parameters of the Gantt functions of {@link FlowFactory} called from
 * TL-Script.
 */
@SuppressWarnings("javadoc")
public class TestGanttColorRoles extends AbstractSearchExpressionTest {

	public void testLineDecoWithoutColorAndRole() throws Exception {
		GanttDecoration deco = (GanttDecoration) execute(search("reactFlowGanttLineDeco(model: 'm', at: 1)"));
		assertEquals(FlowFactory.DEFAULT_LINE_DECORATION_COLOR, deco.getColor());
	}

	public void testLineDecoWithRole() throws Exception {
		GanttDecoration deco =
			(GanttDecoration) execute(search("reactFlowGanttLineDeco(model: 'm', at: 1, colorRole: 'error')"));
		assertFalse(deco.hasColor());
		assertEquals(ValueColor.ERROR.getExternalName(), deco.getColorRole());
	}

	public void testLineDecoColorTakesPrecedence() throws Exception {
		GanttDecoration deco = (GanttDecoration) execute(
			search("reactFlowGanttLineDeco(model: 'm', at: 1, color: '#00ff00', colorRole: 'error')"));
		assertEquals("#00ff00", deco.getColor());
	}

	public void testRangeDecoRole() throws Exception {
		GanttDecoration plain =
			(GanttDecoration) execute(search("reactFlowGanttRangeDeco(model: 'm', from: 1, to: 2)"));
		assertEquals(FlowFactory.DEFAULT_RANGE_DECORATION_COLOR, plain.getColor());

		GanttDecoration withRole = (GanttDecoration) execute(
			search("reactFlowGanttRangeDeco(model: 'm', from: 1, to: 2, colorRole: 'category-3')"));
		assertFalse(withRole.hasColor());
		assertEquals(ValueColor.CATEGORY_3.getExternalName(), withRole.getColorRole());
	}

	public void testEdgeRoles() throws Exception {
		String edge = "reactFlowGanttEdge(model: 'e', sourceModel: 'a', sourceEndpoint: 'END', "
			+ "targetModel: 'b', targetEndpoint: 'START'";
		GanttEdge plain = (GanttEdge) execute(search(edge + ")"));
		GanttEdge defaults = GanttEdge.create();
		assertEquals(defaults.getStrokeRole(), plain.getStrokeRole());
		assertEquals(defaults.getViolatedStrokeRole(), plain.getViolatedStrokeRole());

		GanttEdge withRoles =
			(GanttEdge) execute(search(edge + ", strokeRole: 'success', violatedStrokeRole: 'warning')"));
		assertEquals(ValueColor.SUCCESS.getExternalName(), withRoles.getStrokeRole());
		assertEquals(ValueColor.WARNING.getExternalName(), withRoles.getViolatedStrokeRole());
	}

	public void testInvalidRole() throws Exception {
		try {
			execute(search("reactFlowGanttLineDeco(model: 'm', at: 1, colorRole: 'no-such-role')"));
			fail("An unknown color role must be rejected.");
		} catch (TopLogicException ex) {
			ResKey error = ex.getErrorKey();
			assertEquals(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL.fill("", "", "", "").plain().getKey(),
				error.plain().getKey());
			String expected = (String) error.arguments()[2];
			for (ValueColor role : ValueColor.values()) {
				assertTrue("Valid role '" + role.name() + "' listed: " + expected, expected.contains(role.name()));
			}
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The tests evaluate scripts that touch no persistent objects, so a single knowledge
	 *           base suffices.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestGanttColorRoles.class,
			ServiceTestSetup.createStarterFactoryForModules(getModules()));
	}

}
