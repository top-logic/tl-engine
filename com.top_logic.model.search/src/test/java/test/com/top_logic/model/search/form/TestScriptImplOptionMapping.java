/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.form;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

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
import com.top_logic.basic.config.script.ScriptImplementations;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.ScriptFunction3;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.ScriptImplOptionMapping;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ScriptImplementationLabels;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.TLObject;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptFunction3Impl;
import com.top_logic.model.search.script.ScriptImplementation;
import com.top_logic.model.search.script.ScriptImplementationType;
import com.top_logic.model.search.script.TLScriptImplementationProvider;
import com.top_logic.model.search.form.WithExpression;
import com.top_logic.model.search.ui.model.options.NavigationOptions;
import com.top_logic.util.Resources;

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

	private static final String LABEL = "label";

	private static final String PROVIDER = "provider";

	private static final String CHOSEN = "chosen";

	private static final String CREATE_DEFAULT = "create-default";

	private static final String COMPUTED_IN_TRANSACTION = "is-computed-in-transaction";

	/**
	 * Sub interface of {@link PolymorphicConfiguration} that the TL-Script provider does not
	 * support.
	 */
	public interface CustomFilterConfig extends PolymorphicConfiguration<Filter<Object>> {
		// Pure sub interface.
	}

	public interface A extends ConfigurationItem {
		@Name(FILTER)
		PolymorphicConfiguration<? extends Filter<Object>> getFilter();

		@Name(FUNCTION)
		ScriptFunction1<Object, Object> getFunction();

		@Name(CUSTOM)
		CustomFilterConfig getCustom();

		@Name(LABEL)
		ScriptFunction3<Object, TLObject, ResKey, TLStructuredTypePart> getLabel();

		@Name(PROVIDER)
		PolymorphicConfiguration<? extends DefaultProvider> getProvider();

		@Name(CHOSEN)
		@com.top_logic.layout.form.values.edit.annotation.Options(fun = ChosenFilters.class,
			mapping = ImplOptionMapping.class)
		PolymorphicConfiguration<? extends Filter<Object>> getChosen();
	}

	/**
	 * Java implementation of a filter offered by {@link ChosenFilters}.
	 */
	public static class AcceptAll implements Filter<Object> {
		@Override
		public boolean accept(Object obj) {
			return true;
		}
	}

	/**
	 * {@link com.top_logic.layout.form.values.edit.annotation.Options} function offering a fixed choice of implementation classes.
	 */
	public static class ChosenFilters extends Function0<List<Class<?>>> {
		@Override
		public List<Class<?>> apply() {
			return List.of(AcceptAll.class);
		}
	}

	/**
	 * Java implementation fitting the type arguments of {@link A#getLabel()}.
	 */
	public static class FittingLabel extends Function3<Object, TLObject, ResKey, TLStructuredTypePart> {
		@Override
		public Object apply(TLObject arg1, ResKey arg2, TLStructuredTypePart arg3) {
			return arg2;
		}
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

	/**
	 * Only implementation classes whose type arguments fit the generic type of the property are
	 * offered.
	 */
	public void testOptionsFitTheTypeArguments() {
		A a = TypedConfiguration.newConfigItem(A.class);
		Options label = options(a, LABEL);
		assertTrue(label.options().contains(FittingLabel.class));
		assertTrue(label.options().contains(ScriptFunction3Impl.class));
		assertFalse("Function3<Object, Object, Object, Object> does not fit invariant type arguments.",
			label.options().contains(com.top_logic.basic.func.IfElse.class));
		assertFalse(label.options().contains(NavigationOptions.class));
	}

	/**
	 * The tooltip of a script function names the resolved signature of the implemented method.
	 */
	public void testScriptTooltipNamesTheSignature() {
		A a = TypedConfiguration.newConfigItem(A.class);
		ConfigurationItem labelScript =
			(ConfigurationItem) options(a, LABEL).mapping().toSelection(ScriptFunction3Impl.class);
		String tooltip = ScriptImplementationLabels.tooltip(property(LABEL), labelScript,
			labelScript.descriptor().getProperty(WithExpression.EXPRESSION));
		assertTrue(tooltip, tooltip.contains("<li><code>arg1</code>: <code>TLObject</code>"));
		assertTrue(tooltip, tooltip.contains("<li><code>arg2</code>: <code>ResKey</code>"));
		assertTrue(tooltip, tooltip.contains("<li><code>arg3</code>: <code>TLStructuredTypePart</code>"));
		assertTrue(tooltip, tooltip.contains(" <code>Object</code>"));
		assertEquals(Labels.propertyLabel(property(LABEL), false), ScriptImplementationLabels.label(property(LABEL),
			labelScript, labelScript.descriptor().getProperty(WithExpression.EXPRESSION)));
	}

	/**
	 * The method properties of a script implementation of a documented interface carry the
	 * documentation of the interface: the functional method the label and documentation of the
	 * owner followed by its own documentation and its arguments with names and documentation, a
	 * value property the label and documentation of its method.
	 */
	public void testDocumentedInterface() throws ConfigurationException {
		PropertyDescriptor owner = property(PROVIDER);
		ConfigurationItem script = ScriptImplementations.createScriptConfig(owner);
		PropertyDescriptor createDefault = script.descriptor().getProperty(CREATE_DEFAULT);
		PropertyDescriptor computedInTransaction = script.descriptor().getProperty(COMPUTED_IN_TRANSACTION);

		assertEquals("context -> attribute -> null",
			ExprFormat.INSTANCE.getSpecification((Expr) script.value(createDefault)));

		assertEquals(Labels.propertyLabel(owner, false), ScriptImplementationLabels.label(owner, script, createDefault));
		String tooltip = ScriptImplementationLabels.tooltip(owner, script, createDefault);
		assertTrue(tooltip, tooltip.contains(resource("createDefault.tooltip")));
		assertTrue(tooltip, tooltip.contains("<li><code>context</code>: <code>Object</code> \u2013 "
			+ resource("createDefault.param.context.tooltip")));
		assertTrue(tooltip, tooltip.contains("<li><code>attribute</code>: <code>TLStructuredTypePart</code> \u2013 "
			+ resource("createDefault.param.attribute.tooltip")));
		assertTrue(tooltip, tooltip.contains(
			"<code>Object</code> \u2013 " + resource("createDefault.return")));

		assertEquals(resource("isComputedInTransaction"),
			ScriptImplementationLabels.label(owner, script, computedInTransaction));
		String valueTooltip = ScriptImplementationLabels.tooltip(owner, script, computedInTransaction);
		assertTrue(valueTooltip, valueTooltip.startsWith("<div>" + resource("isComputedInTransaction.tooltip") + "</div>"));
		assertTrue(valueTooltip, valueTooltip.contains(resource("isComputedInTransaction.return")));
		assertFalse("No argument list for a value.", valueTooltip.contains("<ul>"));
	}

	/**
	 * Without exported documentation, a method property keeps its own label, and its tooltip
	 * still lists the signature.
	 */
	public void testUndocumentedInterface() throws ConfigurationException {
		PropertyDescriptor owner = property(FILTER);
		ConfigurationItem script = ScriptImplementations.createScriptConfig(owner);
		PropertyDescriptor test = script.descriptor().getProperty("test");
		assertNull("Default label.", ScriptImplementationLabels.label(owner, script, test));
		String tooltip = ScriptImplementationLabels.tooltip(owner, script, test);
		assertTrue(tooltip, tooltip.contains("<li><code>t</code>: <code>Object</code></li>"));
	}

	/**
	 * A parameter without name (a class compiled without parameter names) gets a generic name.
	 */
	public void testArgumentNameFallback() throws Exception {
		Method apply = BiFunction.class.getMethod("apply", Object.class, Object.class);
		assertFalse("Precondition: JDK compiled without parameter names.", apply.getParameters()[0].isNamePresent());
		assertEquals("arg1", ScriptImplementationLabels.argumentName(apply, 0));
		assertEquals("arg1 -> arg2 -> null",
			ExprFormat.INSTANCE.getSpecification(TLScriptImplementationProvider.skeleton(apply)));
	}

	/**
	 * The exported documentation of {@link DefaultProvider} with the given key suffix.
	 */
	private static String resource(String suffix) {
		String result =
			Resources.getInstance().getString(ResKey.forClass(DefaultProvider.class).suffix(suffix).optional());
		assertNotNull("Missing resource: " + suffix, result);
		return result;
	}

	/**
	 * Implementation classes computed by an
	 * {@link com.top_logic.layout.form.values.edit.annotation.Options} function are completed with the script
	 * option, as the generic implementation classes are.
	 */
	public void testScriptOptionForOptionsFunction() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);
		Options chosen = options(a, CHOSEN);
		assertTrue(chosen.mapping() instanceof ScriptImplOptionMapping);
		assertEquals(List.of(AcceptAll.class, ScriptImplementation.class), chosen.options());

		Object script = chosen.mapping().toSelection(ScriptImplementation.class);
		assertNotNull(ScriptImplementationType.forDescriptor(((ConfigurationItem) script).descriptor()));
		a.update(property(CHOSEN), script);
		assertSame(ScriptImplementation.class, chosen.mapping().asOption(chosen.options(), script));

		Object java = chosen.mapping().toSelection(AcceptAll.class);
		assertEquals(AcceptAll.class, ((PolymorphicConfiguration<?>) java).getImplementationClass());
		assertSame(AcceptAll.class, chosen.mapping().asOption(chosen.options(), java));
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
		assertEquals("Skeleton with the arguments of the method.", "obj -> null", ExprFormat.INSTANCE.getSpecification(
			(Expr) ((ConfigurationItem) filterScript).value(filterType.getProperty(filterType.getFunctionalMethod()))));
		assertNotSame("A fresh configuration for each selection.", filterScript,
			filter.mapping().toSelection(scriptOption(filter)));

		Options function = options(a, FUNCTION);
		Object functionScript = function.mapping().toSelection(scriptOption(function));
		assertTrue(functionScript instanceof ScriptFunction1Impl.Config<?, ?>);
		assertEquals("Skeleton with the arguments of the function.", "arg -> null",
			ExprFormat.INSTANCE.getSpecification(((WithExpression) functionScript).getExpression()));
		Object labelScript = options(a, LABEL).mapping().toSelection(ScriptFunction3Impl.class);
		assertEquals("arg1 -> arg2 -> arg3 -> null",
			ExprFormat.INSTANCE.getSpecification(((WithExpression) labelScript).getExpression()));

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
