/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.persistency.attribute.macro.ui;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.macro.ui.MacroDisplayControl;
import com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping;

/**
 * Test case for {@link MacroDisplayControl}.
 *
 * <p>
 * A macro is a rendering search expression producing HTML output as evaluation side-effect. When
 * called as a function value (as {@link MacroDisplayControl} does), the evaluation context looses its
 * output writer and the macro therefore returns an {@link com.top_logic.base.services.simpleajax.HTMLFragment}
 * instead of writing directly. {@link MacroDisplayControl} must render this returned fragment,
 * otherwise no output is produced at all.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMacroDisplayControl extends TestControl {

	public void testMacroOutput() {
		MacroDisplayControl control = createControl("<b>value: {1 + 2}</b>");

		String output = writeControl(control);

		assertContains("<b>value: 3</b>", output);
	}

	public void testEmptyMacro() {
		HiddenField field = FormFactory.newHiddenField("macro");
		MacroDisplayControl control = new MacroDisplayControl(null, field);

		// A macro without value must not fail and must produce the surrounding element only.
		String output = writeControl(control);

		assertContains("cMacro", output);
	}

	private MacroDisplayControl createControl(String macroSource) {
		SearchExpression macro = TemplateStorageMapping.INSTANCE.getBusinessObject(macroSource);
		HiddenField field = FormFactory.newHiddenField("macro", macro);
		return new MacroDisplayControl(null, field);
	}

	public static Test suite() {
		return TestControl.suite(TestMacroDisplayControl.class);
	}

}
