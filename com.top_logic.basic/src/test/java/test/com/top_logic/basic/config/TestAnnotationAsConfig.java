/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Test case for using {@link Annotation} interfaces as {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAnnotationAsConfig extends AbstractTypedConfigurationTestCase {

	@TagName("annotation-with-default")
	public static @interface AnnotationWithDefault {

		String string() default "Hello World!";

		int integer() default 42;

		float floatingPoint() default 42.13f;

		double doublePrecision() default 123.456;

		Class<?> type() default Void.class;

		Override[] annotation() default {};

	}

	public static interface Base extends ConfigurationItem {

		String ANNOTATION = "annotation";

		@Name(ANNOTATION)
		Annotation getAnnotation();

		void setAnnotation(Annotation value);

		String ANNOTATIONS = "annotations";

		@Name(ANNOTATIONS)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		Map<Class<?>, Annotation> getAnnotations();

		void setAnnotations(Map<Class<?>, Annotation> value);

	}

	public void testAnnotationType() {
		Subtypes subtypes = TypedConfiguration.newAnnotationItem(Subtypes.class);

		assertEquals(Subtypes.class, subtypes.annotationType());
	}

	public void testAnnotationTypeGenericAccess() {
		Subtypes subtypes = TypedConfiguration.newAnnotationItem(Subtypes.class);

		ConfigurationItem item = (ConfigurationItem) subtypes;
		PropertyDescriptor annotationTypeProperty = item.descriptor().getProperty(ConfigurationItem.ANNOTATION_TYPE);
		assertNotNull(annotationTypeProperty);
		assertEquals(Subtypes.class, item.value(annotationTypeProperty));
	}

	public void testProperties() {
		Subtypes subtypes = TypedConfiguration.newAnnotationItem(Subtypes.class);

		ConfigurationItem item = item(subtypes);
		assertEquals(BasicTestCase.set(ConfigurationItem.CONFIGURATION_INTERFACE_NAME,
			ConfigurationItem.ANNOTATION_TYPE, "value", "adjust"), new HashSet<>(propertyNames(item)));
	}

	public void testArrayDefault() {
		Subtypes subtypes = TypedConfiguration.newAnnotationItem(Subtypes.class);

		assertTrue(
			Arrays.equals(new Subtypes.Subtype[0], (Object[]) get(subtypes, "value")));
		assertTrue(
			Arrays.equals(subtypes.value(), (Object[]) get(subtypes, "value")));
	}

	public void testDeclaredDefault() {
		AnnotationWithDefault item = TypedConfiguration.newAnnotationItem(AnnotationWithDefault.class);

		assertEquals("Hello World!", item.string());
		assertEquals(42, item.integer());
		assertEquals(42.13f, item.floatingPoint());
		assertEquals(123.456, item.doublePrecision());
		assertEquals(Void.class, item.type());
		assertNotNull(item.annotation());
		assertEquals(0, item.annotation().length);
		assertEquals(Override.class, item.annotation().getClass().getComponentType());
	}

	public void testSetGet() {
		Subtypes subtypes = TypedConfiguration.newAnnotationItem(Subtypes.class);
		Subtype subtype = TypedConfiguration.newAnnotationItem(Subtype.class);

		set(subtypes, "value", new Subtype[] { subtype });

		assertTrue(
			Arrays.equals(new Subtype[] { subtype }, (Object[]) get(subtypes, "value")));
	}

	public void testConfigItemProperties() {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(Base.class);
		PropertyDescriptor annotationProperty = descriptor.getProperty(Base.ANNOTATION);
		assertNotNull(annotationProperty);

		PropertyDescriptor typeProperty =
			annotationProperty.getValueDescriptor().getProperty(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
		assertNotNull(
			"Even if an annotation does not extends configuration item, the configuration must assume that values in this property are configuration items.",
			typeProperty);
	}

	public void testAnnotationTypeProperty() {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(Base.class);
		PropertyDescriptor annotationProperty = descriptor.getProperty(Base.ANNOTATION);
		assertNotNull(annotationProperty);

		PropertyDescriptor annotationTypeProperty =
			annotationProperty.getValueDescriptor().getProperty(ConfigurationItem.ANNOTATION_TYPE);
		assertNotNull(annotationTypeProperty);
		assertEquals(false, annotationTypeProperty.canHaveSetter());
	}

	public void testAnnotationByTagName() throws ConfigurationException {
		Base item1 = (Base) fromXML("<base><annotations /></base>");
		assertTrue(item1.getAnnotations().isEmpty());

		Base item2 =
			(Base) fromXML("<base><annotations><annotation-with-default integer=\"17\"/></annotations></base>");
		assertEquals(1, item2.getAnnotations().size());
		Annotation annotationWithDefault = item2.getAnnotations().get(AnnotationWithDefault.class);
		assertTrue(annotationWithDefault instanceof AnnotationWithDefault);
		assertEquals(17, ((AnnotationWithDefault) annotationWithDefault).integer());
	}

	public void testParse() throws ConfigurationException {
		Subtypes item0 = (Subtypes) fromXML("<subtypes></subtypes>");
		assertEquals(0, item0.value().length);

		Subtypes item1 = (Subtypes) fromXML(
			"<subtypes><value><subtype tag='foo' type='" + ConfigurationItem.class.getName()
				+ "'/></value></subtypes>");
		assertEquals(1, item1.value().length);
		assertEquals("foo", item1.value()[0].tag());
		assertEquals(ConfigurationItem.class, item1.value()[0].type());

		Subtypes item2 =
			(Subtypes) fromXML("<subtypes><value><subtype tag='foo' type='" + ConfigurationItem.class.getName()
				+ "'/><subtype tag='bar' type='" + PolymorphicConfiguration.class.getName() + "'/></value></subtypes>");
		assertEquals(2, item2.value().length);
		assertEquals("foo", item2.value()[0].tag());
		assertEquals(ConfigurationItem.class, item2.value()[0].type());
		assertEquals("bar", item2.value()[1].tag());
		assertEquals(PolymorphicConfiguration.class, item2.value()[1].type());
	}

	public void testMandatoryByDefault() {
		initFailureTest();
		try {
			fromXML(
				"<subtypes><value><subtype type='" + ConfigurationItem.class.getName() + "'/></value></subtypes>");
			fail("Annotation properties are always non-nullable.");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("mandatory", ex.getMessage());
		}

		initFailureTest();
		try {
			fromXML(
				"<subtypes><value><subtype tag='foo'/></value></subtypes>");
			fail("Annotation properties are always non-nullable.");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("mandatory", ex.getMessage());
		}
	}

	@Subtypes({ @Subtype(tag = "foo", type = ConfigurationItem.class) })
	public void testJavaAnnotationArrayAccess() throws NoSuchMethodException {
		Subtypes javaAnnotation =
			TestAnnotationAsConfig.class.getMethod("testJavaAnnotationArrayAccess").getAnnotation(Subtypes.class);

		doTestNoUpdate(javaAnnotation);
	}

	public void testConfigAnnotationArrayAccess() throws ConfigurationException {
		Subtypes javaAnnotation = (Subtypes) fromXML(
			"<subtypes><value><subtype tag='foo' type='" + ConfigurationItem.class.getName()
				+ "'/></value></subtypes>");

		doTestNoUpdate(javaAnnotation);
	}

	private void doTestNoUpdate(Subtypes javaAnnotation) {
		javaAnnotation.value()[0] = null;
		assertNotNull("Annotation value was updated.", javaAnnotation.value()[0]);
	}

	private static Object get(Annotation annotation, String propertyName) {
		ConfigurationItem item = item(annotation);
		return item.value(item.descriptor().getProperty(propertyName));
	}

	private static void set(Annotation annotation, String propertyName, Object value) {
		ConfigurationItem item = item(annotation);
		item.update(item.descriptor().getProperty(propertyName), value);
	}

	private static ConfigurationItem item(Annotation typedAnnotation) {
		ConfigurationItem annotationItem = (ConfigurationItem) typedAnnotation;
		return annotationItem;
	}

	private static Collection<String> propertyNames(ConfigurationItem item) {
		List<String> result = new ArrayList<>();
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			result.add(property.getPropertyName());
		}
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		HashMap<String, ConfigurationDescriptor> map = MapUtil.newMap(2);
		map.put("subtypes", TypedConfiguration.getConfigurationDescriptor(Subtypes.class));
		map.put("base", TypedConfiguration.getConfigurationDescriptor(Base.class));
		return map;
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestAnnotationAsConfig.class);
	}

}
