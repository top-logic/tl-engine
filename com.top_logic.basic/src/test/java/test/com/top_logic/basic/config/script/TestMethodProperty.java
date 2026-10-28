/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.script;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptorBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.script.I18NConstants;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.script.MethodProperty;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.util.ResKey;

/**
 * Test case for {@link MethodProperty}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMethodProperty extends TestCase {

	private static final Set<String> RESERVED = Set.of("class");

	public interface Multi {
		String getName(Object x);

		default boolean supportsModel(Object x) {
			return size(x) > 0;
		}

		int size(Object x);

		static Multi none() {
			return null;
		}
	}

	public interface SubMulti extends Multi {
		@Override
		default boolean supportsModel(Object x) {
			return true;
		}

		Object first();
	}

	public interface Base {
		Object getModel(Object x);
	}

	public interface Covariant extends Base {
		@Override
		Collection<?> getModel(Object x);
	}

	public interface BoundFunction extends Function<String, List<String>> {
		// Re-declaration through type arguments only.
	}

	public interface OverloadedAbstract {
		Object call(Object x);

		Object call(Object x, Object y);
	}

	public interface OverloadedWithDefault {
		Object call(Object x);

		default Object call(Object x, Object y) {
			return call(x);
		}
	}

	public interface OverloadedDefaults {
		Object call(Object x);

		default Object other(Object x) {
			return x;
		}

		default Object other(Object x, Object y) {
			return y;
		}
	}

	public interface Renamed {
		@Name("label")
		Object getName();

		@Name("unary")
		Object call(Object x);

		@Name("binary")
		Object call(Object x, Object y);
	}

	public interface RenamedRedeclaration extends Renamed {
		@Override
		@Name("title")
		String getName();
	}

	public interface RenamedClash {
		Object getName();

		@Name("get-name")
		Object name();
	}

	public interface GenericAbstract {
		<T> T call(Object x);
	}

	public interface GenericDefault {
		Object call(Object x);

		default <T> T cast(Object x) {
			return null;
		}
	}

	public interface NameClash {
		Object getUrl();

		Object getURL();
	}

	public interface Reserved {
		Object getName();

		default Object clazz() {
			return null;
		}

		default Class<?> class_() {
			return null;
		}
	}

	public interface ReservedClash {
		Object call();

		@SuppressWarnings("unused")
		default Object getClass(Object x) {
			return null;
		}
	}

	public interface WithObjectMethods {
		Object call();

		@Override
		boolean equals(Object obj);

		@Override
		String toString();
	}

	public enum Color {
		RED, GREEN;
	}

	public interface Values<T> {
		boolean isActive();

		default String getTitle() {
			return "java";
		}

		Object compute(Object x);

		Object getAnything();

		default Color getColor() {
			return Color.RED;
		}

		T getTyped();

		default ResKey getLabel() {
			return null;
		}

		default List<String> getNames() {
			return List.of();
		}

		default Class<? extends Number> getNumberType() {
			return Integer.class;
		}

		default Runnable getAction() {
			return null;
		}
	}

	public void testValueTypes() throws ConfigurationException {
		Map<String, MethodProperty> properties = byName(MethodProperty.getMethodProperties(Values.class, RESERVED));
		assertEquals("Abstract value method.", boolean.class, properties.get("is-active").getValueType(null));
		assertEquals("Default value method.", String.class, properties.get("get-title").getValueType(null));
		assertNull("Method with parameters.", properties.get("compute").getValueType(null));
		assertNull("Method returning Object.", properties.get("get-anything").getValueType(null));
		assertEquals("Enum.", Color.class, properties.get("get-color").getValueType(null));
		assertNull("Unresolved type variable.", properties.get("get-typed").getValueType(null));
		assertEquals("Type with own format.", ResKey.class, properties.get("get-label").getValueType(null));
		assertNull("Collection.", properties.get("get-names").getValueType(null));
		assertEquals("Parameterized class type.", Class.class, properties.get("get-number-type").getValueType(null));
		assertNull("Interface.", properties.get("get-action").getValueType(null));

		Type implementedType = JavaTypeUtil.newParameterizedType(Values.class, String.class);
		assertEquals("Type variable resolved in the implemented type.", String.class,
			properties.get("get-typed").getValueType(implementedType));
		assertNull("Type variable resolved to a non-value type.",
			properties.get("get-typed").getValueType(JavaTypeUtil.newParameterizedType(Values.class, Object.class)));
	}

	public void testIsValueType() {
		assertTrue(MethodProperty.isValueType(int.class));
		assertTrue(MethodProperty.isValueType(Integer.class));
		assertTrue(MethodProperty.isValueType(String.class));
		assertTrue(MethodProperty.isValueType(Color.class));
		assertTrue(MethodProperty.isValueType(Class.class));
		assertTrue(MethodProperty.isValueType(ResKey.class));
		assertFalse(MethodProperty.isValueType(void.class));
		assertFalse(MethodProperty.isValueType(Object.class));
		assertFalse(MethodProperty.isValueType(Runnable.class));
		assertFalse(MethodProperty.isValueType(List.class));
		assertFalse(MethodProperty.isValueType(com.top_logic.basic.config.ConfigurationItem.class));
	}

	private static Map<String, MethodProperty> byName(List<MethodProperty> properties) {
		Map<String, MethodProperty> result = new LinkedHashMap<>();
		for (MethodProperty property : properties) {
			result.put(property.getPropertyName(), property);
		}
		return result;
	}

	public void testDefaultConfigurationName() {
		assertEquals("supports-model", ConfigurationDescriptorBuilder.getDefaultConfigurationName("supportsModel"));
		assertEquals("get-model", ConfigurationDescriptorBuilder.getDefaultConfigurationName("getModel"));
		assertEquals("retrieve-model-from-list-element",
			ConfigurationDescriptorBuilder.getDefaultConfigurationName("retrieveModelFromListElement"));
		assertEquals("get-url-prefix", ConfigurationDescriptorBuilder.getDefaultConfigurationName("getURLPrefix"));
	}

	public void testAbstractAndDefaultMethods() throws ConfigurationException {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(Multi.class, RESERVED);
		assertEquals(List.of("get-name", "size", "supports-model"), names(properties));
		assertTrue(properties.get(0).isAbstract());
		assertTrue(properties.get(1).isAbstract());
		assertFalse("Default method.", properties.get(2).isAbstract());
		assertEquals(String.class, properties.get(0).getMethod().getReturnType());
		assertEquals(int.class, properties.get(1).getMethod().getReturnType());
	}

	public void testInheritedMethods() throws ConfigurationException {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(SubMulti.class, RESERVED);
		assertEquals("Abstract methods first, the sub interface before its super interface.",
			List.of("first", "get-name", "size", "supports-model"), names(properties));
		MethodProperty supportsModel = properties.get(3);
		assertFalse(supportsModel.isAbstract());
		assertEquals("The overriding default method is the most specific declaration.", SubMulti.class,
			supportsModel.getMethod().getDeclaringClass());
	}

	public void testCovariantRedeclaration() throws Exception {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(Covariant.class, RESERVED);
		assertEquals(List.of("get-model"), names(properties));
		assertEquals(Collection.class, properties.get(0).getMethod().getReturnType());

		Method baseMethod = Base.class.getMethod("getModel", Object.class);
		assertEquals("Both declarations have the same signature.", properties.get(0).getSignature(),
			JavaTypeUtil.getResolvedSignature(Covariant.class, baseMethod));

		List<Method> abstractMethods = JavaTypeUtil.getAbstractMethods(Covariant.class);
		assertEquals(1, abstractMethods.size());
		assertEquals("Most specific declaration selected.", Collection.class,
			abstractMethods.get(0).getReturnType());
	}

	public void testIsMoreSpecific() throws Exception {
		Method base = Base.class.getMethod("getModel", Object.class);
		Method covariant = Covariant.class.getMethod("getModel", Object.class);
		assertTrue(JavaTypeUtil.isMoreSpecific(covariant, base));
		assertFalse(JavaTypeUtil.isMoreSpecific(base, covariant));
		assertFalse(JavaTypeUtil.isMoreSpecific(base, base));

		Method multi = Multi.class.getMethod("supportsModel", Object.class);
		Method sub = SubMulti.class.getMethod("supportsModel", Object.class);
		assertTrue("Same return type, declared in a sub interface.", JavaTypeUtil.isMoreSpecific(sub, multi));
		assertFalse(JavaTypeUtil.isMoreSpecific(multi, sub));
	}

	public void testBoundTypeArguments() throws ConfigurationException {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(BoundFunction.class, RESERVED);
		assertEquals("Generic default methods are not offered.", List.of("apply"), names(properties));
		assertTrue(properties.get(0).isAbstract());
	}

	public void testComparator() throws ConfigurationException {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(Comparator.class, RESERVED);
		assertEquals("compare", properties.get(0).getPropertyName());
		assertTrue(properties.get(0).isAbstract());
		assertEquals("Only one abstract method.", 1, properties.stream().filter(MethodProperty::isAbstract).count());
		List<String> names = names(properties);
		assertFalse("Overloaded generic default method not offered.", names.contains("then-comparing"));
		assertTrue("Plain default method offered.", names.contains("reversed"));
		assertFalse("Object method is no property.", names.contains("equals"));
	}

	public void testObjectMethods() throws ConfigurationException {
		assertEquals(List.of("call"), names(MethodProperty.getMethodProperties(WithObjectMethods.class, RESERVED)));
	}

	public void testStaticMethodsIgnored() throws ConfigurationException {
		assertFalse(names(MethodProperty.getMethodProperties(Multi.class, RESERVED)).contains("none"));
	}

	public void testOverloadedAbstract() {
		assertError(OverloadedAbstract.class, I18NConstants.ERROR_OVERLOADED_SCRIPT_METHOD__INTERFACE_METHOD.getKey());
		assertError(OverloadedWithDefault.class, I18NConstants.ERROR_OVERLOADED_SCRIPT_METHOD__INTERFACE_METHOD.getKey());
	}

	public void testOverloadedDefaults() throws ConfigurationException {
		assertEquals("Overloaded default methods keep their Java implementation.", List.of("call"),
			names(MethodProperty.getMethodProperties(OverloadedDefaults.class, RESERVED)));
	}

	public void testAnnotatedNames() throws ConfigurationException {
		Map<String, MethodProperty> properties = byName(MethodProperty.getMethodProperties(Renamed.class, RESERVED));
		assertEquals("Overloaded methods with different annotated names are implementable.",
			Set.of("label", "unary", "binary"), properties.keySet());
		assertEquals("getName", properties.get("label").getMethod().getName());
		assertEquals(1, properties.get("unary").getMethod().getParameterCount());
		assertEquals(2, properties.get("binary").getMethod().getParameterCount());
	}

	public void testAnnotatedNameOfRedeclaration() throws ConfigurationException {
		List<MethodProperty> properties = MethodProperty.getMethodProperties(RenamedRedeclaration.class, RESERVED);
		Map<String, MethodProperty> byName = byName(properties);
		assertEquals("The name of the most specific declaration applies.", Set.of("title", "unary", "binary"),
			byName.keySet());
		assertEquals(String.class, byName.get("title").getMethod().getReturnType());
	}

	public void testAnnotatedNameClash() {
		assertError(RenamedClash.class, I18NConstants.ERROR_METHOD_PROPERTY_CLASH__INTERFACE_PROPERTY_METHODS.getKey());
	}

	public void testGenericAbstract() {
		assertError(GenericAbstract.class, I18NConstants.ERROR_GENERIC_SCRIPT_METHOD__INTERFACE_METHOD.getKey());
	}

	public void testGenericDefault() throws ConfigurationException {
		assertEquals("Generic default methods keep their Java implementation.", List.of("call"),
			names(MethodProperty.getMethodProperties(GenericDefault.class, RESERVED)));
	}

	public void testNameClash() {
		assertError(NameClash.class, I18NConstants.ERROR_METHOD_PROPERTY_CLASH__INTERFACE_PROPERTY_METHODS.getKey());
	}

	public void testReservedName() throws ConfigurationException {
		assertEquals(List.of("get-name", "clazz", "class_"),
			names(MethodProperty.getMethodProperties(Reserved.class, RESERVED)));
		assertError(ReservedClash.class, Set.of("get-class"),
			I18NConstants.ERROR_RESERVED_METHOD_PROPERTY__INTERFACE_PROPERTY_METHOD.getKey());
	}

	public void testNoInterface() throws ConfigurationException {
		try {
			MethodProperty.getMethodProperties(Object.class, RESERVED);
			fail("Only interfaces are implemented through method properties.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private static void assertError(Class<?> type, String expectedKey) {
		assertError(type, RESERVED, expectedKey);
	}

	private static void assertError(Class<?> type, Set<String> reserved, String expectedKey) {
		try {
			MethodProperty.getMethodProperties(type, reserved);
			fail("Interface cannot be implemented through properties: " + type.getName());
		} catch (ConfigurationException ex) {
			assertEquals(expectedKey, ex.getErrorKey().getKey());
			assertEquals(type.getName(), ex.getErrorKey().arguments()[0]);
		}
	}

	private static List<String> names(List<MethodProperty> properties) {
		return properties.stream().map(MethodProperty::getPropertyName).collect(Collectors.toList());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestMethodProperty.class));
	}

}
