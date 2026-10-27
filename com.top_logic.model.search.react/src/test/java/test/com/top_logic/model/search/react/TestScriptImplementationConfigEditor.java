/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.PolymorphicItemControl;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.ScriptImplOptionMapping;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptImplementation;
import com.top_logic.model.search.form.ScriptImplementationType;
import com.top_logic.model.search.form.WithExpression;
import com.top_logic.model.search.react.TLScriptEditorReactControl;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Test of the React configuration editor ({@link ConfigEditorControl}) for properties that are
 * implemented either by a Java class or by TL-Script.
 *
 * <p>
 * Such a property is a polymorphic item: a {@link PolymorphicItemControl} offers the Java
 * implementations and TL-Script as choice, and a TL-Script implementation is edited in a nested
 * form with one TL-Script editor per method.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestScriptImplementationConfigEditor extends BasicTestCase {

	private static final String ROOT_TAG = "a";

	/**
	 * Interface implemented in Java or in TL-Script.
	 */
	public interface Greeter {

		/** Property name of the TL-Script implementation of {@link #greet(Object)}. */
		String GREET = "greet";

		/**
		 * The greeting for the given object.
		 */
		String greet(Object x);
	}

	/**
	 * Java implementation of {@link Greeter}.
	 */
	public static class FixedGreeter implements Greeter {

		/**
		 * Configuration of a {@link FixedGreeter}.
		 */
		public interface Config<I extends FixedGreeter> extends PolymorphicConfiguration<I> {

			/** Property name of {@link #getText()}. */
			String TEXT = "text";

			@Name(TEXT)
			String getText();

			void setText(String value);
		}

		private final String _text;

		@CalledByReflection
		public FixedGreeter(InstantiationContext context, Config<?> config) {
			_text = config.getText();
		}

		@Override
		public String greet(Object x) {
			return _text;
		}
	}

	public interface ScriptProperties extends ConfigurationItem {

		/** Property name of {@link #getGreeter()}. */
		String GREETER = "greeter";

		/** Property name of {@link #getFunction()}. */
		String FUNCTION = "function";

		@Name(GREETER)
		@Mandatory
		PolymorphicConfiguration<Greeter> getGreeter();

		@Name(FUNCTION)
		@Mandatory
		ScriptFunction1<Object, Object> getFunction();
	}

	public void testOptionsIncludeTLScript() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);

		List<Object> greeterOptions = PolymorphicOptions.compute(config, greeter(config)).options();
		assertTrue(greeterOptions.contains(ScriptImplementation.class));
		assertTrue(greeterOptions.contains(FixedGreeter.class));

		List<Object> functionOptions = PolymorphicOptions.compute(config, function(config)).options();
		assertTrue(functionOptions.contains(ScriptFunction1Impl.class));
	}

	public void testScriptOptionIsLabelledWithoutClassName() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		for (PropertyDescriptor property : List.of(greeter(config), function(config))) {
			PolymorphicOptions.Choices choices = PolymorphicOptions.compute(config, property);
			Object scriptOption = ((ScriptImplOptionMapping) choices.mapping()).getScriptOption();
			LabelProvider labels = PolymorphicOptions.keyLabelProvider(choices.options(), choices.mapping());
			assertEquals("TL-Script", labels.getLabel(PolymorphicOptions.keyFor(scriptOption)));
		}

		PolymorphicOptions.Choices greeterChoices = PolymorphicOptions.compute(config, greeter(config));
		LabelProvider labels =
			PolymorphicOptions.keyLabelProvider(greeterChoices.options(), greeterChoices.mapping());
		assertEquals("A Java implementation keeps its label.", PolymorphicOptions.labelFor(FixedGreeter.class),
			labels.getLabel(PolymorphicOptions.keyFor(FixedGreeter.class)));
	}

	public void testMandatoryNullShowsSelector() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		ConfigEditorControl editor = editor(config, new ConfigFieldIndex());

		for (PropertyDescriptor property : List.of(greeter(config), function(config))) {
			PolymorphicItemControl poly = polymorphic(editor, property);
			assertNotNull("Type selector for '" + property.getPropertyName() + "'.", typeSelector(poly));
			assertNull(poly.getNestedEditor());
			assertNull(poly.getTypeModel().getValue());
		}
	}

	public void testScriptOnlyOptionShowsSelector() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		PolymorphicOptions.Choices computed = PolymorphicOptions.compute(config, greeter(config));
		PolymorphicOptions.Choices scriptOnly =
			new PolymorphicOptions.Choices(List.of(ScriptImplementation.class), computed.mapping());
		assertTrue("TL-Script as only and already chosen option is shown.",
			PolymorphicItemControl.showsSelector(scriptOnly, new Object(), false));
	}

	public void testSelectScriptYieldsCodeEditors() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigEditorControl editor = editor(config, index);

		PolymorphicItemControl greeter = polymorphic(editor, greeter(config));
		greeter.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptImplementation.class));

		PolymorphicConfiguration<Greeter> script = config.getGreeter();
		assertNotNull(script);
		assertNotNull("Script configuration.", ScriptImplementationType.forDescriptor(script.descriptor()));
		assertEquals(List.of(Greeter.GREET), propertyNames(index, script));
		List<TLScriptEditorReactControl> greetEditors = scriptEditors(greeter.getNestedEditor());
		assertEquals(1, greetEditors.size());
		assertEquals("Skeleton with the arguments of the method.", "arg1 -> null", value(greetEditors.get(0)));

		PolymorphicItemControl function = polymorphic(editor, function(config));
		function.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptFunction1Impl.class));
		assertTrue(config.getFunction() instanceof ScriptFunction1Impl.Config<?, ?>);
		assertEquals(List.of(WithExpression.EXPRESSION), propertyNames(index, config.getFunction()));
		assertEquals(1, scriptEditors(function.getNestedEditor()).size());
	}

	public void testScriptValueMapsToScriptOption() throws ConfigurationException {
		ScriptProperties config = read("<a greeter=\"x -> 'hello'\" function=\"x -> $x\"/>");
		ConfigEditorControl editor = editor(config, new ConfigFieldIndex());

		PolymorphicItemControl greeter = polymorphic(editor, greeter(config));
		assertNotNull(typeSelector(greeter));
		assertEquals(PolymorphicOptions.keyFor(ScriptImplementation.class), greeter.getTypeModel().getValue());
		List<TLScriptEditorReactControl> greetEditors = scriptEditors(greeter.getNestedEditor());
		assertEquals(1, greetEditors.size());
		assertEquals("x -> 'hello'", value(greetEditors.get(0)));

		PolymorphicItemControl function = polymorphic(editor, function(config));
		assertNotNull(typeSelector(function));
		assertEquals(PolymorphicOptions.keyFor(ScriptFunction1Impl.class), function.getTypeModel().getValue());
		List<TLScriptEditorReactControl> expressionEditors = scriptEditors(function.getNestedEditor());
		assertEquals(1, expressionEditors.size());
		assertEquals("x -> $x", value(expressionEditors.get(0)));
	}

	public void testEditedScriptIsStored() throws Exception {
		ScriptProperties config = read("<a greeter=\"x -> 'hello'\" function=\"x -> $x\"/>");
		ConfigEditorControl editor = editor(config, new ConfigFieldIndex());

		TLScriptEditorReactControl greet = scriptEditors(polymorphic(editor, greeter(config)).getNestedEditor()).get(0);
		greet.executeCommand("valueChanged", Map.of("value", "x -> 'bye'"));

		String written = write(config);
		assertTrue("Script written as text: " + written, written.contains("greeter=\"x -&gt; 'bye'\""));
	}

	public void testJavaValueUnchanged() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		@SuppressWarnings("unchecked")
		FixedGreeter.Config<FixedGreeter> java = TypedConfiguration.newConfigItem(FixedGreeter.Config.class);
		java.setImplementationClass(FixedGreeter.class);
		java.setText("hi");
		config.update(greeter(config), java);
		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigEditorControl editor = editor(config, index);

		PolymorphicItemControl greeter = polymorphic(editor, greeter(config));
		assertEquals(PolymorphicOptions.keyFor(FixedGreeter.class), greeter.getTypeModel().getValue());
		assertEquals(List.of(FixedGreeter.Config.TEXT), propertyNames(index, java));
		assertEquals(0, scriptEditors(greeter.getNestedEditor()).size());
	}

	public void testSwitchBetweenScriptAndJava() throws ConfigurationException {
		ScriptProperties config = read("<a greeter=\"x -> 'hello'\" function=\"x -> $x\"/>");
		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigEditorControl editor = editor(config, index);
		PolymorphicItemControl greeter = polymorphic(editor, greeter(config));

		greeter.getTypeModel().setValue(PolymorphicOptions.keyFor(FixedGreeter.class));
		assertEquals(FixedGreeter.class, config.getGreeter().getImplementationClass());
		assertEquals(List.of(FixedGreeter.Config.TEXT), propertyNames(index, config.getGreeter()));

		greeter.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptImplementation.class));
		assertNotNull(ScriptImplementationType.forDescriptor(config.getGreeter().descriptor()));
		assertEquals(1, scriptEditors(greeter.getNestedEditor()).size());
	}

	/**
	 * Interface with two methods, implemented by a script with one script per method.
	 */
	public interface Converter {

		/** Property name of the TL-Script implementation of {@link #first(Object)}. */
		String FIRST = "first";

		/** Property name of the TL-Script implementation of {@link #second(String, List)}. */
		String SECOND = "second";

		/**
		 * The first conversion.
		 */
		String first(Object x);

		/**
		 * The second conversion.
		 */
		Integer second(String a, List<String> b);
	}

	public interface ConverterProperties extends ConfigurationItem {

		/** Property name of {@link #getConverter()}. */
		String CONVERTER = "converter";

		@Name(CONVERTER)
		PolymorphicConfiguration<Converter> getConverter();
	}

	public void testSingleScriptCarriesTheOwnersLabelAndSignature() {
		ScriptProperties config = TypedConfiguration.newConfigItem(ScriptProperties.class);
		ConfigEditorControl editor = editor(config, new ConfigFieldIndex());

		PolymorphicItemControl greeter = polymorphic(editor, greeter(config));
		greeter.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptImplementation.class));
		ReactFormFieldChromeControl greet = chromeOf(greeter, scriptEditors(greeter.getNestedEditor()).get(0));
		assertEquals(Labels.propertyLabel(greeter(config), false), label(greet));
		assertTrue(tooltip(greet).contains(signature("1. Object", "String")));

		PolymorphicItemControl function = polymorphic(editor, function(config));
		function.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptFunction1Impl.class));
		ReactFormFieldChromeControl expression = chromeOf(function, scriptEditors(function.getNestedEditor()).get(0));
		assertEquals(Labels.propertyLabel(function(config), false), label(expression));
		assertTrue(tooltip(expression).contains(signature("1. Object", "Object")));
	}

	public void testSeveralScriptsKeepTheirMethodLabels() {
		ConverterProperties config = TypedConfiguration.newConfigItem(ConverterProperties.class);
		PropertyDescriptor converter = config.descriptor().getProperty(ConverterProperties.CONVERTER);
		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigEditorControl editor = new ConfigEditorControl(createContext(), config, Set.of(), false, index);

		PolymorphicItemControl poly = find(editor, PolymorphicItemControl.class).get(0);
		poly.getTypeModel().setValue(PolymorphicOptions.keyFor(ScriptImplementation.class));
		ConfigurationItem script = config.getConverter();

		List<TLScriptEditorReactControl> scripts = scriptEditors(poly.getNestedEditor());
		assertEquals(2, scripts.size());
		for (TLScriptEditorReactControl control : scripts) {
			ReactFormFieldChromeControl chrome = chromeOf(poly, control);
			String label = label(chrome);
			if (label.equals(Labels.propertyLabel(script.descriptor().getProperty(Converter.FIRST), false))) {
				assertTrue(tooltip(chrome).contains(signature("1. Object", "String")));
				assertEquals("arg1 -> null", value(control));
			} else {
				assertEquals(Labels.propertyLabel(script.descriptor().getProperty(Converter.SECOND), false), label);
				assertTrue(tooltip(chrome),
					tooltip(chrome).contains(signature("1. String, 2. List&lt;String&gt;", "Integer")));
				assertEquals("arg1 -> arg2 -> null", value(control));
			}
		}
	}

	/**
	 * The signature text for the given arguments and result, in the language of the test.
	 */
	private static String signature(String arguments, String result) {
		return Resources.getInstance().getString(
			com.top_logic.layout.form.values.edit.I18NConstants.SCRIPT_SIGNATURE__ARGUMENTS_RESULT.fill(arguments,
				result));
	}

	/**
	 * The chrome below the given root that displays the given field.
	 */
	private static ReactFormFieldChromeControl chromeOf(ReactControl root, ReactControl field) {
		for (ReactFormFieldChromeControl chrome : find(root, ReactFormFieldChromeControl.class)) {
			if (find(chrome, field.getClass()).contains(field)) {
				return chrome;
			}
		}
		fail("No chrome around: " + field);
		return null;
	}

	private static String label(ReactFormFieldChromeControl chrome) {
		return (String) chrome.scriptingScalarState().get("label");
	}

	private static String tooltip(ReactFormFieldChromeControl chrome) {
		TooltipContent content = chrome.getTooltipContent("tooltip");
		assertNotNull("No tooltip.", content);
		return content.getHtml();
	}

	private static PropertyDescriptor greeter(ConfigurationItem config) {
		return config.descriptor().getProperty(ScriptProperties.GREETER);
	}

	private static PropertyDescriptor function(ConfigurationItem config) {
		return config.descriptor().getProperty(ScriptProperties.FUNCTION);
	}

	private static ConfigEditorControl editor(ConfigurationItem config, ConfigFieldIndex index) {
		return new ConfigEditorControl(createContext(), config, Set.of(), false, index);
	}

	/**
	 * The names of the properties of the given item that the editor built a field for.
	 */
	private static List<String> propertyNames(ConfigFieldIndex index, ConfigurationItem item) {
		List<String> result = new ArrayList<>();
		for (ConfigFieldModel model : index.fieldsOf(item)) {
			result.add(model.getProperty().getPropertyName());
		}
		return result;
	}

	/**
	 * The {@link PolymorphicItemControl} the given editor built for the given property.
	 */
	private static PolymorphicItemControl polymorphic(ConfigEditorControl editor, PropertyDescriptor property) {
		for (PolymorphicItemControl control : find(editor, PolymorphicItemControl.class)) {
			if (control.getProperty() == property) {
				return control;
			}
		}
		fail("No type selector for '" + property.getPropertyName() + "'.");
		return null;
	}

	private static ReactControl typeSelector(PolymorphicItemControl control) {
		for (ReactControl child : control.scriptingChildren()) {
			if (PolymorphicItemControl.TYPE_SELECTOR_NAME.equals(child.scriptingName())) {
				return child;
			}
		}
		return null;
	}

	private static List<TLScriptEditorReactControl> scriptEditors(ReactControl root) {
		return find(root, TLScriptEditorReactControl.class);
	}

	private static <T extends ReactControl> List<T> find(ReactControl root, Class<T> type) {
		List<T> result = new ArrayList<>();
		collect(root, type, result);
		return result;
	}

	private static <T extends ReactControl> void collect(ReactControl control, Class<T> type, List<T> result) {
		if (control == null) {
			return;
		}
		if (type.isInstance(control)) {
			result.add(type.cast(control));
		}
		for (ReactControl child : control.scriptingChildren()) {
			collect(child, type, result);
		}
	}

	private static Object value(TLScriptEditorReactControl editor) {
		return editor.scriptingScalarState().get("value");
	}

	private static ScriptProperties read(String xml) throws ConfigurationException {
		return (ScriptProperties) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(ScriptProperties.class)))
				.setSource(CharacterContents.newContent(xml)).read();
	}

	private static String write(ConfigurationItem config) throws Exception {
		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
			writer.write(ROOT_TAG, config.descriptor(), config);
		}
		return buffer.toString();
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestScriptImplementationConfigEditor.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}

}
