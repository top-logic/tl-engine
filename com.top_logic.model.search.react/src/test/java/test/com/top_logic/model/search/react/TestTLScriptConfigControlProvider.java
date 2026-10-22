/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.react.TLScriptConfigControlProvider;
import com.top_logic.model.search.react.TLScriptEditorReactControl;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link TLScriptConfigControlProvider}, editing a TL-Script valued configuration property
 * in the TL-Script editor.
 */
@SuppressWarnings("javadoc")
public class TestTLScriptConfigControlProvider extends BasicTestCase {

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

	/**
	 * Configuration declaring the names of the variables its {@link #getScript() script} may use.
	 */
	public interface VariablesConfig extends ConfigurationItem {

		/** Property name of {@link #getNames()}. */
		String NAMES = "names";

		/** Property name of {@link #getScript()}. */
		String SCRIPT = "script";

		@Name(NAMES)
		List<String> getNames();

		void setNames(List<String> value);

		@Name(SCRIPT)
		ContextScriptConfig getScript();

		void setScript(ContextScriptConfig value);
	}

	/**
	 * Part of a {@link VariablesConfig} with a script using the variables declared there.
	 */
	public interface ContextScriptConfig extends ConfigPart {

		/** Property name of {@link #getExpr()}. */
		String EXPR = "expr";

		@Name(EXPR)
		@ScriptContextVariables(DeclaredNames.class)
		Expr getExpr();

		void setExpr(Expr value);
	}

	/**
	 * The {@link VariablesConfig#getNames() names} of the configuration containing the edited
	 * script.
	 */
	public static class DeclaredNames implements ScriptContextVariablesProvider {
		@Override
		public List<String> getVariables(ValueModel valueModel) {
			ConfigPart script = (ConfigPart) valueModel.getModel();
			return ((VariablesConfig) script.container()).getNames();
		}
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

	public void testDeclaredVariableIsAccepted() throws Exception {
		ContextScriptConfig script = contextScript(List.of("x"));
		ConfigFieldModel model = contextModel(script);
		ReactControl control = createControl(model);
		enter(control, "$x.container()");
		leave(control);

		assertFalse("A variable declared by the context is known: " + model.getError(), model.hasError());
		assertEquals("$x.container()", ExprFormat.INSTANCE.getSpecification(script.getExpr()));
	}

	public void testUndeclaredVariableIsRejected() throws Exception {
		ContextScriptConfig script = contextScript(List.of("x"));
		ConfigFieldModel model = contextModel(script);
		ReactControl control = createControl(model);
		enter(control, "$y");

		assertFalse("An unknown variable is not reported while the user is still typing.", model.hasError());
		leave(control);

		assertTrue("A variable the context does not declare is reported once the field is left.",
			model.hasError());
		assertEquals("The stored script is left untouched.", "$x",
			ExprFormat.INSTANCE.getSpecification(script.getExpr()));
	}

	public void testCompletionOffersDeclaredVariables() throws Exception {
		ContextScriptConfig script = contextScript(List.of("x", "other"));
		ReactControl control = createControl(contextModel(script));

		assertEquals(Set.of("$x", "$other"), completeVariables(control));
	}

	public void testVariablesFollowTheDeclaringProperty() throws Exception {
		ContextScriptConfig script = contextScript(List.of("x"));
		ConfigFieldModel model = contextModel(script);
		ReactControl control = createControl(model);

		VariablesConfig variables = (VariablesConfig) script.container();
		List<String> names = new ArrayList<>(variables.getNames());
		names.add("added");
		variables.setNames(names);

		assertEquals(Set.of("$x", "$added"), completeVariables(control));

		enter(control, "$added");
		leave(control);
		assertFalse("A variable declared after the editor was built is known: " + model.getError(),
			model.hasError());
		assertEquals("$added", ExprFormat.INSTANCE.getSpecification(script.getExpr()));
	}

	/**
	 * A script part with the expression <code>$x</code> in a configuration declaring the given
	 * variable names.
	 */
	private static ContextScriptConfig contextScript(List<String> names) throws Exception {
		VariablesConfig variables = TypedConfiguration.newConfigItem(VariablesConfig.class);
		variables.setNames(names);
		ContextScriptConfig script = TypedConfiguration.newConfigItem(ContextScriptConfig.class);
		script.setExpr(ExprFormat.INSTANCE.getValue(ContextScriptConfig.EXPR, "$x"));
		variables.setScript(script);
		return script;
	}

	private static ConfigFieldModel contextModel(ContextScriptConfig script) {
		return new ConfigFieldModel(script, script.descriptor().getProperty(ContextScriptConfig.EXPR));
	}

	/**
	 * The names the editor offers to complete a <code>$</code> typed at the beginning of the
	 * script.
	 */
	@SuppressWarnings("unchecked")
	private static Set<Object> completeVariables(ReactControl control) {
		control.executeCommand("complete",
			Map.of("line", "$", "prefix", "$", "textToCursor", "$", "requestId", "1"));
		Map<String, Object> response = (Map<String, Object>) control.scriptingScalarState().get("completionResponse");
		assertEquals("1", response.get("requestId"));
		Set<Object> names = new HashSet<>();
		for (Map<String, Object> completion : (List<Map<String, Object>>) response.get("completions")) {
			names.add(completion.get("name"));
		}
		return names;
	}

	private ReactControl createControl() {
		return createControl(_model);
	}

	private static ReactControl createControl(ConfigFieldModel model) {
		return new TLScriptConfigControlProvider().createControl(createContext(), model);
	}

	private static void enter(ReactControl control, String text) {
		control.executeCommand("valueChanged", Map.of("value", text));
	}

	private static void leave(ReactControl control) {
		control.executeCommand("blur", Map.of());
	}

	public static Test suite() {
		// The reference check of an entered script compiles it against the model of a knowledge base.
		return KBSetup.getSingleKBTest(TestTLScriptConfigControlProvider.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

}
