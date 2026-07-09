/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.persistency.attribute.macro.ui;

import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.macro.ui.MacroDisplayControl;
import com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping;
import com.top_logic.util.model.ModelService;

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

	public void testMacroWithModelAccess() {
		TLModel applicationModel = ModelService.getApplicationModel();
		Collection<TLModule> modules = applicationModel.getModules();
		assertTrue(!modules.isEmpty());

		SearchExpression macro = createMacro("<b>name: {$model.get(`tl.model:TLModule#name`)}</b>");

		for (TLModule module : modules) {
			MacroDisplayControl control = createControl(module, macro);

			String output = writeControl(control);
			assertContains("<b>name: " + module.getName() + "</b>", output);
		}

	}

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
		return createControl(null, createMacro(macroSource));
	}

	private SearchExpression createMacro(String macroSource) {
		return TemplateStorageMapping.INSTANCE.getBusinessObject(macroSource);
	}

	private MacroDisplayControl createControl(TLObject self, SearchExpression macro) {
		HiddenField field = FormFactory.newHiddenField("macro", macro);
		return new MacroDisplayControl(self, field);
	}

	public static Test suite() {
		return TestControl.suite(TestMacroDisplayControl.class);
	}

}
