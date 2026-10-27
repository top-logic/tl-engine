/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.ScriptImplOptionMapping;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptImplementation;
import com.top_logic.model.search.form.ScriptImplementationType;
import com.top_logic.model.search.form.TLScriptImplementationProvider;
import com.top_logic.model.search.form.WithExpression;

/**
 * Test of the TL-Script option that the configuration editors offer through
 * {@link Fields#optionProvider(com.top_logic.layout.form.values.DeclarativeFormOptions)} for
 * properties that {@link ScriptImplementations#acceptsScript(PropertyDescriptor) accept scripts}.
 *
 * @see ScriptImplOptionMapping
 */
@SuppressWarnings("javadoc")
public class TestScriptImplOptionMapping extends TestCase {

	private static final String ROOT_TAG = "a";

	private static final String FILTER = "filter";

	private static final String FUNCTION = "function";

	private static final String CUSTOM = "custom";

	/**
	 * Sub interface of {@link PolymorphicConfiguration} that the TL-Script provider does not
	 * support.
	 */
	public interface CustomFilterConfig extends PolymorphicConfiguration<Filter<Object>> {
		// Pure sub interface.
	}

	public interface A extends ConfigurationItem {
		@Name(FILTER)
		PolymorphicConfiguration<Filter<Object>> getFilter();

		@Name(FUNCTION)
		ScriptFunction1<Object, Object> getFunction();

		@Name(CUSTOM)
		CustomFilterConfig getCustom();
	}

	public void testScriptOptionForScriptProperties() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		Options filter = options(a, FILTER);
		assertTrue(filter.mapping() instanceof ScriptImplOptionMapping);
		assertEquals(ScriptImplementation.class, scriptOption(filter));
		assertEquals(1, Collections.frequency(filter.options(), ScriptImplementation.class));

		Options function = options(a, FUNCTION);
		assertTrue(function.mapping() instanceof ScriptImplOptionMapping);
		assertEquals(ScriptFunction1Impl.class, scriptOption(function));
		assertEquals("The script implementation class is no Java option.",
			1, Collections.frequency(function.options(), ScriptFunction1Impl.class));
	}

	public void testNoScriptOptionForOtherProperties() {
		A a = TypedConfiguration.newConfigItem(A.class);
		PropertyDescriptor custom = property(CUSTOM);
		assertFalse(ScriptImplementations.acceptsScript(custom));
		assertNull(ScriptImplOptionMapping.forProperty(custom));

		Options options = options(a, CUSTOM);
		assertSame(ImplOptionMapping.INSTANCE, options.mapping());
		assertFalse(options.options().contains(ScriptImplementation.class));
	}

	public void testSelectScriptOption() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		Options filter = options(a, FILTER);
		Object filterScript = filter.mapping().toSelection(scriptOption(filter));
		ScriptImplementationType filterType =
			ScriptImplementationType.of(TLScriptImplementationProvider.implementedType(property(FILTER)));
		assertSame(filterType.getDescriptor(), ((ConfigurationItem) filterScript).descriptor());
		assertNull("Empty script.", ((ConfigurationItem) filterScript)
			.value(filterType.getProperty(filterType.getFunctionalMethod())));
		assertNotSame("A fresh configuration for each selection.", filterScript,
			filter.mapping().toSelection(scriptOption(filter)));

		Options function = options(a, FUNCTION);
		Object functionScript = function.mapping().toSelection(scriptOption(function));
		assertTrue(functionScript instanceof ScriptFunction1Impl.Config<?, ?>);
		assertNull("Empty script.", ((WithExpression) functionScript).getExpression());

		// The selected value is a valid value of the property.
		a.update(property(FILTER), filterScript);
		a.update(property(FUNCTION), functionScript);
	}

	public void testScriptValueMapsToScriptOption() throws ConfigurationException {
		A a = read("<a filter=\"x -> true\" function=\"x -> $x\"/>");

		Options filter = options(a, FILTER);
		assertSame(ScriptImplementation.class, filter.mapping().asOption(filter.options(), a.getFilter()));

		Options function = options(a, FUNCTION);
		assertSame(ScriptFunction1Impl.class, function.mapping().asOption(function.options(), a.getFunction()));
		assertEquals("x -> $x",
			ExprFormat.INSTANCE.getSpecification(((WithExpression) a.getFunction()).getExpression()));
	}

	public void testJavaOptionUnchanged() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		Options filter = options(a, FILTER);
		Class<?> javaFilter = javaOption(filter, Filter.class);
		PolymorphicConfiguration<?> filterConfig = (PolymorphicConfiguration<?>) filter.mapping().toSelection(javaFilter);
		assertEquals(javaFilter, filterConfig.getImplementationClass());
		assertSame(((ConfigurationItem) ImplOptionMapping.INSTANCE.toSelection(javaFilter)).descriptor(),
			filterConfig.descriptor());
		assertSame(javaFilter, filter.mapping().asOption(filter.options(), filterConfig));
		a.update(property(FILTER), filterConfig);

		Options function = options(a, FUNCTION);
		Class<?> javaFunction = javaOption(function, IFunction1.class);
		PolymorphicConfiguration<?> functionConfig =
			(PolymorphicConfiguration<?>) function.mapping().toSelection(javaFunction);
		assertEquals(javaFunction, functionConfig.getImplementationClass());
		assertTrue("Java implementation configured with the type of the property.",
			functionConfig instanceof ScriptFunction1<?, ?>);
		assertSame(javaFunction, function.mapping().asOption(function.options(), functionConfig));
		a.update(property(FUNCTION), functionConfig);
	}

	private static Class<?> scriptOption(Options options) {
		Class<?> result = ((ScriptImplOptionMapping) options.mapping()).getScriptOption();
		assertTrue("Script option offered.", options.options().contains(result));
		return result;
	}

	/**
	 * A Java implementation class of the given interface among the options, whose configuration
	 * interface is the plain {@link PolymorphicConfiguration}.
	 */
	private static Class<?> javaOption(Options options, Class<?> implementedInterface) throws ConfigurationException {
		ScriptImplOptionMapping mapping = (ScriptImplOptionMapping) options.mapping();
		for (Object option : options.options()) {
			if (mapping.isScriptOption(option)) {
				continue;
			}
			Class<?> result = (Class<?>) option;
			if (implementedInterface.isAssignableFrom(result) && DefaultConfigConstructorScheme.getFactory(result)
				.getConfigurationInterface() == PolymorphicConfiguration.class) {
				return result;
			}
		}
		fail("No Java implementation offered: " + options.options());
		return null;
	}

	private record Options(List<Object> options, OptionMapping mapping) {
		// Options of a property and their mapping.
	}

	private static Options options(ConfigurationItem model, String propertyName) {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		var provider = Fields.optionProvider(editorFactory.formOptions(property(propertyName)));
		assertNotNull("No options for '" + propertyName + "'.", provider);
		List<Object> options = new ArrayList<>();
		for (Object option : provider.get(model)) {
			options.add(option);
		}
		return new Options(options, Fields.optionMapping(provider));
	}

	private static PropertyDescriptor property(String name) {
		return TypedConfiguration.getConfigurationDescriptor(A.class).getProperty(name);
	}

	private static A read(String xml) throws ConfigurationException {
		return (A) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(A.class)))
				.setSource(CharacterContents.newContent(xml)).read();
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestScriptImplOptionMapping.class);
	}

}
