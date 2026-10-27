/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.form;

import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.format.JavaTypeFormat;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.basic.func.IFunction2;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.mig.html.ElementUpdate;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptFunction2Impl;
import com.top_logic.model.search.form.ScriptImplementation;
import com.top_logic.model.search.form.ScriptImplementationType;
import com.top_logic.model.search.form.TLScriptImplementationProvider;
import com.top_logic.model.search.form.WithExpression;
import com.top_logic.model.search.form.WithImplementedType;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for configuration properties implemented by TL-Script through the
 * {@link TLScriptImplementationProvider} registered with the {@link java.util.ServiceLoader}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTLScriptImplementation extends AbstractSearchExpressionTest {

	private static final String ROOT_TAG = "a";

	public interface IntFun {
		int compute(Object x);
	}

	public interface BooleanFun {
		boolean check(Object x);
	}

	public interface ListFun {
		List<String> names(Object x);
	}

	public enum Color {
		RED, GREEN;
	}

	public interface EnumFun {
		Color color(Object x);
	}

	public interface WithDefault {
		String name(String x);

		default String twice(String x) {
			return name(x) + name(x);
		}
	}

	public interface VarArgsFun {
		Object call(Object... args);
	}

	public interface VoidFun {
		void run(Object x);
	}

	/**
	 * Interface with two abstract methods and a default method of different result types.
	 */
	public interface Multi {
		String name(Object x);

		int size(Object x);

		default boolean isEmpty(Object x) {
			return size(x) == 0;
		}
	}

	/**
	 * Interface with an overloaded abstract method.
	 */
	public interface Overloaded {
		Object call(Object x);

		Object call(Object x, Object y);
	}

	/**
	 * Java implementation of {@link Filter} configured by class name.
	 */
	public static class AcceptAll implements Filter<Object> {
		@Override
		public boolean accept(Object obj) {
			return true;
		}
	}

	/**
	 * Java implementation of {@link IntFun} configured by class name.
	 */
	public static class Seven implements IntFun {
		@Override
		public int compute(Object x) {
			return 7;
		}
	}

	public interface A extends ConfigurationItem {
		String FILTER = "filter";

		String MAPPING = "mapping";

		String INT_FUN = "int-fun";

		String BOOLEAN_FUN = "boolean-fun";

		String LIST_FUN = "list-fun";

		String ENUM_FUN = "enum-fun";

		String WITH_DEFAULT = "with-default";

		String VAR_ARGS = "var-args";

		String VOID_FUN = "void-fun";

		String FUN1 = "fun1";

		String FUN2 = "fun2";

		String LEGACY = "legacy";

		String MULTI = "multi";

		String BUILDER = "builder";

		String OVERLOADED = "overloaded";

		String COMPARATOR = "comparator";

		@Name(FILTER)
		PolymorphicConfiguration<Filter<Object>> getFilter();

		@Name(MAPPING)
		PolymorphicConfiguration<? extends Mapping<Object, String>> getMapping();

		@Name(INT_FUN)
		PolymorphicConfiguration<IntFun> getIntFun();

		@Name(BOOLEAN_FUN)
		PolymorphicConfiguration<BooleanFun> getBooleanFun();

		@Name(LIST_FUN)
		PolymorphicConfiguration<ListFun> getListFun();

		@Name(ENUM_FUN)
		PolymorphicConfiguration<EnumFun> getEnumFun();

		@Name(WITH_DEFAULT)
		PolymorphicConfiguration<WithDefault> getWithDefault();

		@Name(VAR_ARGS)
		PolymorphicConfiguration<VarArgsFun> getVarArgs();

		@Name(VOID_FUN)
		PolymorphicConfiguration<VoidFun> getVoidFun();

		@Name(FUN1)
		PolymorphicConfiguration<IFunction1<String, Object>> getFun1();

		@Name(FUN2)
		ScriptFunction2<String, Object, Object> getFun2();

		@Name(LEGACY)
		ScriptFunction1<Object, Object> getLegacy();

		@Name(MULTI)
		PolymorphicConfiguration<Multi> getMulti();

		@Name(BUILDER)
		PolymorphicConfiguration<ListModelBuilder> getBuilder();

		@Name(OVERLOADED)
		PolymorphicConfiguration<Overloaded> getOverloaded();

		@Name(COMPARATOR)
		PolymorphicConfiguration<Comparator<Object>> getComparator();
	}

	public void testRealProvider() {
		assertTrue(ScriptImplementations.getProvider() instanceof TLScriptImplementationProvider);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(A.class);
		for (String property : Arrays.asList(A.FILTER, A.MAPPING, A.INT_FUN, A.FUN1, A.FUN2, A.LEGACY)) {
			assertTrue(property, ScriptImplementations.acceptsScript(descriptor.getProperty(property)));
		}
	}

	public void testAttributeScripts() throws ConfigurationException {
		A a = read("<a"
			+ " filter=\"x -> $x == 'a'\""
			+ " mapping=\"x -> $x + 1\""
			+ " int-fun='x -> $x + 2'"
			+ " boolean-fun='x -> $x'"
			+ " list-fun=\"x -> list($x, 'b')\""
			+ " enum-fun=\"x -> 'GREEN'\""
			+ "/>");

		Filter<Object> filter = instance(a.getFilter());
		assertTrue(filter.accept("a"));
		assertFalse(filter.accept("b"));
		assertTrue("Default method of the implemented interface.", filter.test("a"));

		Mapping<Object, String> mapping = instance(a.getMapping());
		assertEquals("Result converted to String.", "3", mapping.map(2));

		IntFun intFun = instance(a.getIntFun());
		assertEquals(5, intFun.compute(3));

		BooleanFun booleanFun = instance(a.getBooleanFun());
		assertTrue(booleanFun.check("non-empty"));
		assertFalse(booleanFun.check(null));

		ListFun listFun = instance(a.getListFun());
		assertEquals(List.of("a", "b"), listFun.names("a"));
		assertEquals("Single value converted to list.", List.of("x"),
			((ListFun) instance(read("<a list-fun=\"x -> $x\"/>").getListFun())).names("x"));

		EnumFun enumFun = instance(a.getEnumFun());
		assertEquals(Color.GREEN, enumFun.color(null));
	}

	public void testImplementedType() throws Exception {
		A a = read("<a mapping='x -> $x' fun1='x -> $x' fun2='x -> y -> $x'/>");
		assertEquals("com.top_logic.basic.col.Mapping<java.lang.Object, java.lang.String>",
			JavaTypeUtil.toTypeName(implementedType(a.getMapping())));
		assertEquals("com.top_logic.basic.func.IFunction1<java.lang.String, java.lang.Object>",
			JavaTypeUtil.toTypeName(implementedType(a.getFun1())));
		assertEquals("com.top_logic.basic.func.IFunction2<java.lang.String, java.lang.Object, java.lang.Object>",
			JavaTypeUtil.toTypeName(implementedType(a.getFun2())));

		Type type = implementedType(a.getMapping());
		assertEquals(type, JavaTypeFormat.INSTANCE.getValue("test", JavaTypeFormat.INSTANCE.getSpecification(type)));
	}

	public void testElementText() throws ConfigurationException {
		A a = read("<a><filter><![CDATA[x -> $x < 2]]></filter><int-fun>x -> 7</int-fun></a>");
		Filter<Object> filter = instance(a.getFilter());
		assertTrue(filter.accept(1));
		assertFalse(filter.accept(3));
		assertEquals(7, ((IntFun) instance(a.getIntFun())).compute(null));
	}

	public void testClassNameAttribute() throws ConfigurationException {
		A a = read("<a filter='" + AcceptAll.class.getName() + "'/>");
		assertEquals(AcceptAll.class, a.getFilter().getImplementationClass());
		assertTrue(instance(a.getFilter()) instanceof AcceptAll);

		A element = read("<a><filter class='" + AcceptAll.class.getName() + "'/></a>");
		assertEquals(AcceptAll.class, element.getFilter().getImplementationClass());
	}

	public void testDefaultMethod() throws ConfigurationException {
		WithDefault fun = instance(read("<a with-default=\"x -> $x + '!'\"/>").getWithDefault());
		assertEquals("a!", fun.name("a"));
		assertEquals("a!a!", fun.twice("a"));
	}

	public void testVarArgs() throws ConfigurationException {
		VarArgsFun fun = instance(read("<a var-args='x -> y -> $x + $y'/>").getVarArgs());
		assertEquals(5L, ((Number) fun.call(2, 3)).longValue());
	}

	public void testVoidMethod() throws ConfigurationException {
		VoidFun fun = instance(read("<a void-fun='x -> $x'/>").getVoidFun());
		fun.run("ignored");
	}

	public void testScriptFunction() throws ConfigurationException {
		A a = read("<a fun2=\"x -> y -> $x + $y\" fun1=\"x -> 42\"/>");
		assertTrue(a.getFun2() instanceof ScriptFunction2Impl.Config<?, ?, ?>);
		IFunction2<String, Object, Object> fun2 = a.getFun2().impl();
		assertTrue(fun2 instanceof ScriptFunction2Impl);
		assertEquals("Result converted to the type argument of the property.", "ab", fun2.apply("a", "b"));

		IFunction1<String, Object> fun1 = instance(a.getFun1());
		assertTrue(Proxy.isProxyClass(fun1.getClass()));
		assertEquals("Result converted to the type argument of the property.", "42", fun1.apply(null));
	}

	public void testLegacyExpressionAttribute() throws ConfigurationException {
		A a = read("<a><legacy expression='x -> $x'/><fun2 expression='x -> y -> $y'/></a>");
		assertTrue(a.getLegacy() instanceof ScriptFunction1Impl.Config<?, ?>);
		assertEquals("v", a.getLegacy().impl().apply("v"));
		assertEquals("w", a.getFun2().impl().apply(null, "w"));

		A explicit = read("<a><legacy class='" + ScriptFunction1Impl.class.getName() + "' expression='x -> $x'/></a>");
		assertEquals("v", explicit.getLegacy().impl().apply("v"));
		assertNull(((WithImplementedType) explicit.getLegacy()).getImplementedType());
	}

	public void testXmlRoundTrip() throws Exception {
		A a = read("<a filter=\"x -> $x == 'a'\" mapping='x -> $x' fun2='x -> y -> $y'>"
			+ "<legacy expression='x -> $x'/>"
			+ "<int-fun class='" + Seven.class.getName() + "'/>"
			+ "</a>");
		String xml = toXML(a);
		assertFalse(xml, xml.contains(WithImplementedType.IMPLEMENTED_TYPE));
		assertFalse(xml, xml.contains(WithExpression.EXPRESSION));
		assertTrue(xml, xml.contains("legacy=\"x -&gt; $x\"") || xml.contains("legacy=\"x -> $x\""));
		assertTrue(xml, xml.contains("<int-fun class=\"" + Seven.class.getName() + "\""));

		A copy = read(xml);
		assertEquals(a, copy);
		Filter<Object> filter = instance(copy.getFilter());
		assertTrue(filter.accept("a"));
		assertEquals(7, ((IntFun) instance(copy.getIntFun())).compute(null));
	}

	public void testWrongResultType() throws ConfigurationException {
		IntFun fun = instance(read("<a int-fun=\"x -> 'abc'\"/>").getIntFun());
		try {
			fun.compute(null);
			fail("A text is no int.");
		} catch (TopLogicException ex) {
			ResKey errorKey = ex.getErrorKey();
			assertEquals(I18NConstants.ERROR_WRONG_RESULT__TYPE_METHOD_EXPECTED_VAL.getKey(), errorKey.getKey());
			assertEquals(IntFun.class.getName(), errorKey.arguments()[0]);
			assertEquals("compute", errorKey.arguments()[1]);
		}
	}

	public void testInvalidScript() {
		try {
			read("<a int-fun='x -> -> 1'/>");
			fail("Syntax error expected.");
		} catch (ConfigurationException | ConfigurationError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Syntax error"));
		}
	}

	public void testObjectMethods() throws ConfigurationException {
		A a = read("<a filter=\"x -> $x == 'a'\"/>");
		Filter<Object> filter = instance(a.getFilter());
		Filter<Object> other = instance(a.getFilter());
		assertEquals(filter, filter);
		assertFalse(filter.equals(other));
		assertEquals(System.identityHashCode(filter), filter.hashCode());
		assertTrue(filter.toString(), filter.toString().contains(Filter.class.getName()));
		assertTrue(filter.toString(), filter.toString().contains("x -> $x == 'a'"));
		assertSame(a.getFilter(), ((ConfiguredInstance<?>) filter).getConfig());
	}

	public void testNewScriptConfig() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(A.class);
		PolymorphicConfiguration<?> config =
			TLScriptImplementationProvider.newScriptConfig(descriptor.getProperty(A.INT_FUN));
		assertTrue(config instanceof ScriptImplementation.Config<?>);
		config.update(config.descriptor().getProperty("compute"), ExprFormat.INSTANCE.getValue("test", "x -> 1"));
		A a = TypedConfiguration.newConfigItem(A.class);
		a.update(descriptor.getProperty(A.INT_FUN), config);
		assertEquals(1, ((IntFun) instance(a.getIntFun())).compute(null));

		PolymorphicConfiguration<?> function =
			TLScriptImplementationProvider.newScriptConfig(descriptor.getProperty(A.LEGACY));
		assertTrue(function instanceof ScriptFunction1Impl.Config<?, ?>);
	}

	public void testMultiMethodElements() throws ConfigurationException {
		A a = read("<a><multi>"
			+ "<name>x -> 'n' + $x</name>"
			+ "<size><![CDATA[x -> $x * 1]]></size>"
			+ "</multi></a>");
		Multi multi = instance(a.getMulti());
		assertEquals("Result of the first method.", "n1", multi.name(1));
		assertEquals("Result of the second method converted to int.", 3, multi.size(3));
		assertFalse("Java default method based on the script.", multi.isEmpty(3));
		assertTrue(multi.isEmpty(0));
	}

	public void testMultiMethodAttributes() throws ConfigurationException {
		A a = read("<a><multi name=\"x -> 'a'\" size='x -> 0'/></a>");
		Multi multi = instance(a.getMulti());
		assertEquals("a", multi.name(null));
		assertTrue(multi.isEmpty(null));
	}

	public void testDefaultMethodOverride() throws ConfigurationException {
		A a = read("<a><multi name=\"x -> 'a'\" size='x -> 0' is-empty='x -> false'/></a>");
		Multi multi = instance(a.getMulti());
		assertEquals(0, multi.size(null));
		assertFalse("Script overrides the default method.", multi.isEmpty(null));
	}

	public void testMissingAbstractMethod() {
		try {
			read("<a><multi><name>x -> 'a'</name></multi></a>");
			fail("Abstract method size not implemented.");
		} catch (ConfigurationException | ConfigurationError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("size"));
		}
	}

	public void testOverloadedMethod() {
		try {
			read("<a><overloaded><call>x -> $x</call></overloaded></a>");
			fail("Overloaded abstract method cannot be implemented by TL-Script.");
		} catch (ConfigurationException | ConfigurationError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(Overloaded.class.getName()));
		}
	}

	public void testComparator() throws ConfigurationException {
		A a = read("<a comparator='x -> y -> $x - $y'/>");
		Comparator<Object> comparator = instance(a.getComparator());
		assertTrue(comparator.compare(3, 1) > 0);
		assertTrue("Java default method.", comparator.reversed().compare(3, 1) < 0);
	}

	public void testListModelBuilder() throws ConfigurationException {
		A a = read("<a><builder>"
			+ "<get-model>m -> c -> list($m, 'x')</get-model>"
			+ "<supports-model>m -> c -> $m != null</supports-model>"
			+ "<retrieve-model-from-list-element>c -> e -> 'model-' + $e</retrieve-model-from-list-element>"
			+ "</builder></a>");
		ListModelBuilder builder = instance(a.getBuilder());
		Collection<?> model = builder.getModel("m", null);
		assertEquals(List.of("m", "x"), List.copyOf(model));
		assertTrue(builder.supportsModel("m", null));
		assertFalse(builder.supportsModel(null, null));
		assertEquals("model-e", builder.retrieveModelFromListElement(null, "e"));
		assertEquals("Java default method.", ElementUpdate.NO_CHANGE, builder.supportsListElement(null, "e"));

		ModelBuilder asModelBuilder = builder;
		assertTrue("Call through the super interface.", asModelBuilder.getModel("m", null) instanceof Collection<?>);
	}

	public void testListModelBuilderDefaultOverride() throws ConfigurationException {
		A a = read("<a><builder>"
			+ "<get-model>m -> c -> list()</get-model>"
			+ "<supports-model>m -> c -> true</supports-model>"
			+ "<retrieve-model-from-list-element>c -> e -> null</retrieve-model-from-list-element>"
			+ "<supports-list-element>c -> e -> 'ADD'</supports-list-element>"
			+ "</builder></a>");
		ListModelBuilder builder = instance(a.getBuilder());
		assertEquals("Script result converted to the enum.", ElementUpdate.ADD,
			builder.supportsListElement(null, "e"));
	}

	public void testFunctionalElementForm() throws ConfigurationException {
		A element = read("<a><filter><accept>x -> $x == 'a'</accept></filter></a>");
		A text = read("<a filter=\"x -> $x == 'a'\"/>");
		assertEquals("Element and text form give the same configuration.", text, element);
		assertTrue(((Filter<Object>) instance(element.getFilter())).accept("a"));
	}

	public void testWriteClassLessElement() throws Exception {
		A a = read("<a><multi name=\"x -> 'a'\" size='x -> 0'/>"
			+ "<builder>"
			+ "<get-model>m -> c -> list()</get-model>"
			+ "<supports-model>m -> c -> true</supports-model>"
			+ "<retrieve-model-from-list-element><![CDATA[c -> e -> $e < 2]]></retrieve-model-from-list-element>"
			+ "</builder></a>");
		String xml = toXML(a);
		assertFalse("No implementation class: " + xml, xml.contains("class="));
		assertFalse("No configuration interface: " + xml, xml.contains("interface="));
		assertFalse("No implemented type: " + xml, xml.contains(WithImplementedType.IMPLEMENTED_TYPE));
		assertTrue(xml, xml.contains("<multi "));
		assertTrue(xml, xml.contains("size=\"x -&gt; 0\"") || xml.contains("size=\"x -> 0\""));
		assertTrue(xml, xml.contains("<retrieve-model-from-list-element>"));

		A copy = read(xml);
		assertEquals(a, copy);
		assertEquals("a", ((Multi) instance(copy.getMulti())).name(null));
	}

	public void testWriteFunctionalWithDefaultAsElement() throws Exception {
		A a = read("<a><filter accept=\"x -> $x == 'a'\" test='x -> true'/></a>");
		Filter<Object> filter = instance(a.getFilter());
		assertFalse(filter.accept("b"));
		assertTrue("Default method implemented by the script.", filter.test("b"));

		String xml = toXML(a);
		assertTrue("Not in text form, since a default method is implemented: " + xml, xml.contains("<filter "));
		assertFalse(xml, xml.contains("class="));
		A copy = read(xml);
		assertEquals(a, copy);
	}

	public void testJsonRoundTrip() throws Exception {
		A a = read("<a filter=\"x -> $x == 'a'\"><multi name=\"x -> 'a'\" size='x -> 0'/></a>");
		ConfigurationDescriptor staticType = TypedConfiguration.getConfigurationDescriptor(A.class);
		StringWriter buffer = new StringWriter();
		new JsonConfigurationWriter(buffer).write(staticType, a);
		String json = buffer.toString();
		assertFalse("No type annotation: " + json, json.contains(ScriptImplementation.class.getName()));
		assertFalse("No implemented type: " + json, json.contains(WithImplementedType.IMPLEMENTED_TYPE));

		A copy = (A) new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			staticType).setSource(CharacterContents.newContent(json)).read();
		assertEquals(a, copy);
		assertEquals("a", ((Multi) instance(copy.getMulti())).name(null));
	}

	public void testJsonClassLessObject() throws Exception {
		ConfigurationDescriptor staticType = TypedConfiguration.getConfigurationDescriptor(A.class);
		A a = (A) new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, staticType)
			.setSource(CharacterContents.newContent("{\"multi\": {\"name\": \"x -> 'j'\", \"size\": \"x -> 2\"}}"))
			.read();
		Multi multi = instance(a.getMulti());
		assertEquals("j", multi.name(null));
		assertEquals(2, multi.size(null));
	}

	public void testJavaImplementationUnchanged() throws Exception {
		A a = read("<a><int-fun class='" + Seven.class.getName() + "'/></a>");
		assertEquals(Seven.class, a.getIntFun().getImplementationClass());
		assertEquals(7, ((IntFun) instance(a.getIntFun())).compute(null));
		String xml = toXML(a);
		assertTrue(xml, xml.contains("<int-fun class=\"" + Seven.class.getName() + "\""));
	}

	public void testScriptTypePerImplementedType() throws Exception {
		A a = read("<a int-fun='x -> 1' boolean-fun='x -> 1' mapping='x -> $x'/>");
		assertNotSame(a.getIntFun().descriptor(), a.getBooleanFun().descriptor());
		assertFalse("Scripts of different interfaces differ.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a.getIntFun(), a.getBooleanFun()));
		assertEquals(a.getIntFun(), read("<a int-fun='x -> 1'/>").getIntFun());

		ScriptImplementationType type = ScriptImplementationType.forDescriptor(a.getMapping().descriptor());
		assertSame(type, ScriptImplementationType.of(type.getImplementedType()));
		assertEquals(ScriptImplementation.Config.class, type.getDescriptor().getConfigurationInterface());
	}

	public void testLabels() throws ConfigurationException {
		A a = read("<a><multi name=\"x -> 'a'\" size='x -> 0'/></a>");
		ConfigurationDescriptor descriptor = a.getMulti().descriptor();
		PropertyDescriptor property = descriptor.getProperty("is-empty");
		assertEquals("Property name is the label of a method without resource.", "is-empty",
			Resources.getInstance().getString(property.labelKey(null)));
		assertNull("No tooltip.", Resources.getInstance().getString(property.labelKey(null).tooltip(), null));
		assertEquals(Multi.class.getName().replace('$', '.') + ".is-empty",
			descriptor.getPropertyLabel("is-empty", null).getKey());
	}

	private static Type implementedType(PolymorphicConfiguration<?> config) {
		return ((WithImplementedType) config).getImplementedType();
	}

	@SuppressWarnings("unchecked")
	private static <T> T instance(PolymorphicConfiguration<?> config) {
		return (T) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static A read(String xml) throws ConfigurationException {
		return (A) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors())
			.setSource(CharacterContents.newContent(xml)).read();
	}

	private static String toXML(A a) throws Exception {
		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
			writer.write(ROOT_TAG, A.class, a);
		}
		return buffer.toString();
	}

	private static Map<String, ConfigurationDescriptor> descriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(A.class));
	}

	public static Test suite() {
		return suite(TestTLScriptImplementation.class);
	}

}
