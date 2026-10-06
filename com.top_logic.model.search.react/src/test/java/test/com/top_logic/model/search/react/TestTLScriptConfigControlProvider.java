/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.react.TLScriptConfigControlProvider;
import com.top_logic.model.search.react.TLScriptEditorReactControl;

/**
 * Test for {@link TLScriptConfigControlProvider}, editing a TL-Script valued configuration property
 * in the TL-Script editor.
 */
@SuppressWarnings("javadoc")
public class TestTLScriptConfigControlProvider extends TestCase {

	/**
	 * Configuration with a TL-Script valued property.
	 */
	public interface ScriptConfig extends ConfigurationItem {

		/** Property name of {@link #getExpr()}. */
		String EXPR = "expr";

		@Name(EXPR)
		Expr getExpr();

		void setExpr(Expr value);
	}

	private ScriptConfig _config;

	/**
	 * The model a {@link com.top_logic.layout.configedit.ConfigControlService} builds for a property
	 * whose value type is mapped to a control: it holds the typed value, the expression.
	 */
	private ConfigFieldModel _model;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(ScriptConfig.class);
		_config.setExpr(ExprFormat.INSTANCE.getValue(ScriptConfig.EXPR, "x -> $x"));
		_model = new ConfigFieldModel(_config, _config.descriptor().getProperty(ScriptConfig.EXPR));
	}

	public void testEditedInTheScriptEditor() {
		ReactControl control = createControl();
		assertTrue(control instanceof TLScriptEditorReactControl);
		assertEquals("x -> $x", control.scriptingScalarState().get("value"));
	}

	public void testEnteredScriptIsStored() {
		ReactControl control = createControl();
		enter(control, "x -> $x.container()");

		assertEquals("x -> $x.container()", ExprFormat.INSTANCE.getSpecification(_config.getExpr()));
		assertFalse(_model.hasError());
	}

	public void testInvalidScriptIsNotReportedWhileTyping() {
		ReactControl control = createControl();
		enter(control, "x -> ");

		assertFalse("An incomplete script is not reported while the user is still typing.", _model.hasError());
		assertEquals("The stored script is left untouched.", "x -> $x",
			ExprFormat.INSTANCE.getSpecification(_config.getExpr()));
	}

	public void testInvalidScriptIsReportedOnLeavingTheField() {
		ReactControl control = createControl();
		enter(control, "x -> ");
		leave(control);

		assertTrue("A script that does not parse is reported once the field is left.", _model.hasError());
		assertEquals("The stored script is left untouched.", "x -> $x",
			ExprFormat.INSTANCE.getSpecification(_config.getExpr()));
	}

	public void testValidScriptClearsTheReportedError() {
		ReactControl control = createControl();
		enter(control, "x -> ");
		leave(control);
		enter(control, "x -> $x.container()");

		assertFalse(_model.hasError());
		assertEquals("x -> $x.container()", ExprFormat.INSTANCE.getSpecification(_config.getExpr()));
	}

	public void testFunctionYieldingNullIsAccepted() {
		ReactControl control = createControl();
		enter(control, "o -> null");

		assertFalse("A well-formed script is accepted: " + _model.getError(), _model.hasError());
		assertEquals("o -> null", ExprFormat.INSTANCE.getSpecification(_config.getExpr()));
	}

	public void testEditorFindsNoSyntaxErrorInFunctionYieldingNull() {
		ReactControl control = createControl();
		control.executeCommand("validate", Map.of("text", "o -> null"));
		assertEquals(java.util.List.of(), control.scriptingScalarState().get("diagnostics"));
	}

	public void testLeavingWithAValidScriptReportsNothing() {
		ReactControl control = createControl();
		enter(control, "x -> $x.container()");
		leave(control);

		assertFalse(_model.hasError());
	}

	public void testChangeOfTheConfigurationReachesTheEditor() throws Exception {
		ReactControl control = createControl();
		_model.setValue(ExprFormat.INSTANCE.getValue(ScriptConfig.EXPR, "y -> $y"));
		assertEquals("y -> $y", control.scriptingScalarState().get("value"));
	}

	public void testNotEditableFieldIsReadOnly() {
		_model.setEditable(false);
		ReactControl control = createControl();
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get("readOnly"));
	}

	public void testEditabilityReachesTheEditor() {
		ReactControl control = createControl();
		_model.setEditable(false);
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get("readOnly"));
	}

	private ReactControl createControl() {
		return new TLScriptConfigControlProvider().createControl(createContext(), _model);
	}

	private static void enter(ReactControl control, String text) {
		control.executeCommand("valueChanged", Map.of("value", text));
	}

	private static void leave(ReactControl control) {
		control.executeCommand("blur", Map.of());
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTLScriptConfigControlProvider.class, TypeIndex.Module.INSTANCE));
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

}
