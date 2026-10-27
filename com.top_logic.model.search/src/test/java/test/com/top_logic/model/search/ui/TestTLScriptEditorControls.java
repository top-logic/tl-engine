/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.layout.form.values.edit.TestEditorFactoryControls;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.ScriptFunction0;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.layout.ScriptFunction3;
import com.top_logic.layout.ScriptFunction4;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptImplementation;
import com.top_logic.model.search.form.ScriptImplementationType;
import com.top_logic.model.search.form.WithExpression;
import com.top_logic.model.search.ui.TLScriptCodeEditorControl;
import com.top_logic.util.Resources;

/**
 * Test that the configuration of this module edits TL-Script valued configuration properties in a
 * declarative form with the {@link TLScriptCodeEditorControl}, chosen by
 * {@link EditorFactory.Config#getTypeControls()}, and that properties implemented by TL-Script
 * offer TL-Script as implementation choice.
 *
 * @see TestEditorFactoryControls
 */
@SuppressWarnings("javadoc")
public class TestTLScriptEditorControls extends TestCase {

	private static final String ROOT_TAG = "a";

	/**
	 * Name of the field selecting the implementation of an item property.
	 */
	private static final String OPTIONS_FIELD = "options";

	/**
	 * Name of the {@link WithExpression#EXPRESSION} field of a script function.
	 */
	private static final String EXPRESSION_FIELD = WithExpression.EXPRESSION;

	/**
	 * Name of the property of the method {@link Filter#accept(Object)} of a script implementation.
	 */
	private static final String ACCEPT_FIELD = "accept";

	/**
	 * Interface with a method implemented by a value and a method implemented by a script.
	 */
	public interface Switch {
		boolean isOn();

		Object compute(Object x);
	}

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

		@Name("filter")
		PolymorphicConfiguration<Filter<Object>> getFilter();

		@Name("switch")
		PolymorphicConfiguration<Switch> getSwitch();
	}

	/**
	 * An {@link Expr} property gets the TL-Script code editor with its error display, although its
	 * field is created by the editor of a
	 * {@link com.top_logic.layout.form.values.edit.annotation.PropertyEditor} annotation.
	 */
	public void testTLScriptCodeEditor() {
		FormGroup group = build(TypedConfiguration.newConfigItem(ScriptProperties.class));
		assertCodeEditor(group.getField("expr"));
	}

	/**
	 * A script function property is edited as polymorphic item.
	 */
	public void testScriptFunctionsAreItems() {
		FormGroup group = build(TypedConfiguration.newConfigItem(ScriptProperties.class));
		for (String name : new String[] { "function0", "function1", "function2", "function3", "function4" }) {
			assertTrue("Item editor for '" + name + "'.", group.getMember(name) instanceof FormGroup);
		}
	}

	/**
	 * A script value read from the configuration opens with TL-Script selected and the expression
	 * in the TL-Script code editor.
	 */
	public void testScriptValue() throws ConfigurationException {
		ScriptProperties model = read("<a function1=\"x -> $x\" filter=\"x -> true\"/>");
		FormGroup group = build(model);

		FormContainer function = (FormContainer) group.getMember("function1");
		assertEquals(List.of(ScriptFunction1Impl.class), find(function, OPTIONS_FIELD).getValue());
		FormField expression = find(function, EXPRESSION_FIELD);
		assertCodeEditor(expression);
		assertTrue(expression.getValue() instanceof Expr);

		FormContainer filter = (FormContainer) group.getMember("filter");
		assertEquals(List.of(ScriptImplementation.class), find(filter, OPTIONS_FIELD).getValue());
		assertCodeEditor(find(filter, ACCEPT_FIELD));
	}

	/**
	 * Selecting the TL-Script option creates an empty script, edited in the TL-Script code
	 * editor.
	 */
	public void testSelectScript() {
		ScriptProperties model = TypedConfiguration.newConfigItem(ScriptProperties.class);
		FormGroup group = build(model);

		FormContainer filter = (FormContainer) group.getMember("filter");
		SelectField options = (SelectField) find(filter, OPTIONS_FIELD);
		assertTrue(options.getOptions().contains(ScriptImplementation.class));
		options.setValue(List.of(ScriptImplementation.class));

		assertNotNull(model.getFilter());
		assertNotNull(ScriptImplementationType.forDescriptor(model.getFilter().descriptor()));
		FormField accept = find(filter, ACCEPT_FIELD);
		assertCodeEditor(accept);
		assertEquals("Skeleton with the arguments of the method.", "obj -> null",
			ExprFormat.INSTANCE.getSpecification((Expr) accept.getValue()));
	}

	/**
	 * The only script of a script implementation is labelled and documented like the property it
	 * implements, and its tooltip names the signature of the implemented method.
	 */
	public void testSingleScriptCarriesTheOwnersLabelAndSignature() throws ConfigurationException {
		ScriptProperties model = read("<a function1=\"x -> $x\" filter=\"x -> true\"/>");
		FormGroup group = build(model);

		PropertyDescriptor function1 = model.descriptor().getProperty("function1");
		FormField expression = find((FormContainer) group.getMember("function1"), EXPRESSION_FIELD);
		assertEquals(Labels.propertyLabel(function1, false), expression.getLabel());
		assertSignature(expression.getTooltip(), "Object", "arg", "Object");

		PropertyDescriptor filter = model.descriptor().getProperty("filter");
		FormField accept = find((FormContainer) group.getMember("filter"), ACCEPT_FIELD);
		assertEquals(Labels.propertyLabel(filter, false), accept.getLabel());
		assertSignature(accept.getTooltip(), "boolean", "obj", "Object");
	}

	/**
	 * A method without parameters returning a plain value is edited with the editor of its value
	 * type, a <code>boolean</code> with a checkbox, while the other methods get code editors.
	 */
	public void testValueMethodEditor() throws ConfigurationException {
		ScriptProperties model = read("<a><switch is-on='true' compute='x -> $x'/></a>");
		FormGroup group = build(model);

		FormContainer container = (FormContainer) group.getMember("switch");
		FormField on = find(container, "is-on");
		assertTrue("Checkbox for a boolean value: " + on.getClass(), on instanceof BooleanField);
		assertEquals(Boolean.TRUE, on.getValue());
		assertCodeEditor(find(container, "compute"));
	}

	/**
	 * A new script configuration leaves a value method empty instead of filling it with a script
	 * skeleton, the editor shows it with its value editor.
	 */
	public void testNewScriptWithValueMethod() throws ConfigurationException {
		ScriptProperties model = TypedConfiguration.newConfigItem(ScriptProperties.class);
		PropertyDescriptor property = model.descriptor().getProperty("switch");
		PolymorphicConfiguration<?> config = ScriptImplementations.getProvider().createScriptConfig(property);
		assertFalse(config.valueSet(config.descriptor().getProperty("is-on")));
		assertTrue("Skeleton for the script method.", config.valueSet(config.descriptor().getProperty("compute")));

		model.update(property, config);
		FormGroup group = build(model);
		assertTrue(find((FormContainer) group.getMember("switch"), "is-on") instanceof BooleanField);
	}

	/**
	 * Asserts that the given tooltip lists the given arguments (name and type, alternating) and
	 * the given result type.
	 */
	private static void assertSignature(String tooltip, String result, String... arguments) {
		assertNotNull(tooltip);
		for (int n = 0; n < arguments.length; n += 2) {
			String argument = "<li><code>" + arguments[n] + "</code>: <code>" + arguments[n + 1] + "</code>";
			assertTrue(tooltip, tooltip.contains(argument));
		}
		String resultLabel = Resources.getInstance()
			.getString(com.top_logic.layout.form.values.edit.I18NConstants.SCRIPT_RESULT);
		assertTrue(tooltip, tooltip.contains("<b>" + resultLabel + ":</b> <code>" + result + "</code>"));
	}

	private static void assertCodeEditor(FormField field) {
		ControlProvider controlProvider = field.getControlProvider();
		assertNotNull("No control for '" + field.getName() + "'.", controlProvider);
		assertEquals("Control for '" + field.getName() + "'.", TLScriptCodeEditorControl.CP.class,
			controlProvider.getClass());
	}

	/**
	 * The field with the given name in the given container or its descendants.
	 */
	private static FormField find(FormContainer container, String name) {
		FormField result = findOrNull(container, name);
		assertNotNull("No field '" + name + "' in '" + container.getQualifiedName() + "'.", result);
		return result;
	}

	private static FormField findOrNull(FormContainer container, String name) {
		for (Iterator<? extends FormMember> it = container.getMembers(); it.hasNext();) {
			FormMember member = it.next();
			if (member instanceof FormField field && name.equals(field.getName())) {
				return field;
			}
			if (member instanceof FormContainer inner) {
				FormField result = findOrNull(inner, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private static FormGroup build(ConfigurationItem model) {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		FormGroup group = new FormGroup("group", null);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);
		return group;
	}

	private static ScriptProperties read(String xml) throws ConfigurationException {
		return (ScriptProperties) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(ScriptProperties.class)))
				.setSource(CharacterContents.newContent(xml)).read();
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestTLScriptEditorControls.class);
	}

}
