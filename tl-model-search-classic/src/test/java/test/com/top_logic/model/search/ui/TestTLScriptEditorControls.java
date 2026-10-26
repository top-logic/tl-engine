/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.layout.form.values.edit.TestEditorFactoryControls;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ScriptFunction0;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.layout.ScriptFunction3;
import com.top_logic.layout.ScriptFunction4;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.TLScriptCodeEditorControl;

/**
 * Test that the configuration of this module edits TL-Script valued configuration properties in a
 * declarative form with the {@link TLScriptCodeEditorControl}, chosen by
 * {@link EditorFactory.Config#getTypeControls()}.
 *
 * @see TestEditorFactoryControls
 */
@SuppressWarnings("javadoc")
public class TestTLScriptEditorControls extends TestCase {

	public interface ScriptProperties extends ConfigurationItem {
		@Name("expr")
		Expr getExpr();

		@Name("function0")
		ScriptFunction0<?> getFunction0();

		@Name("function1")
		ScriptFunction1<?, ?> getFunction1();

		@Name("function2")
		ScriptFunction2<?, ?, ?> getFunction2();

		@Name("function3")
		ScriptFunction3<?, ?, ?, ?> getFunction3();

		@Name("function4")
		ScriptFunction4<?, ?, ?, ?, ?> getFunction4();
	}

	/**
	 * Every TL-Script valued property gets the TL-Script code editor with its error display,
	 * although its field is created by the editor of a {@link PropertyEditor} annotation.
	 */
	public void testTLScriptCodeEditor() {
		FormGroup group = build(ScriptProperties.class);

		for (String field : new String[] { "expr", "function0", "function1", "function2", "function3", "function4" }) {
			ControlProvider controlProvider = group.getField(field).getControlProvider();
			assertNotNull("No control for '" + field + "'.", controlProvider);
			assertEquals("Control for '" + field + "'.", TLScriptCodeEditorControl.CP.class,
				controlProvider.getClass());
		}
	}

	private static FormGroup build(Class<? extends ConfigurationItem> type) {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		FormGroup group = new FormGroup("group", null);
		ConfigurationItem model = TypedConfiguration.newConfigItem(type);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);
		return group;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestTLScriptEditorControls.class);
	}

}
