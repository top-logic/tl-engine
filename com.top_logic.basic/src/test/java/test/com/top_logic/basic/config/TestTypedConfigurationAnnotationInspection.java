/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test case for finding annotations on configuration interface properties filterd by meta
 * annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationAnnotationInspection extends TestCase {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Annotation1 {
		int value();
	}

	public interface A extends ConfigurationItem {

		String FOO_PROPERTY = "foo";

		@Name(FOO_PROPERTY)
		String getFoo();

	}

	public interface B extends A {

		public static final int B_VALUE_1 = 1;

		@Annotation1(B_VALUE_1)
		@Override
		String getFoo();

	}

	public interface C1 extends B {

		public static final int C1_VALUE_1 = 10;

		@Annotation1(C1_VALUE_1)
		@Override
		String getFoo();

	}

	public interface C2 extends B {

		public static final int C2_VALUE_1 = 20;

		@Annotation1(C2_VALUE_1)
		@Override
		String getFoo();

	}

	public interface D extends C1, C2 {
		// Pure sum interface.
	}

	public void testGetLocalAnnotations() {
		assertEquals(list(), getAnnotations(A.class, A.FOO_PROPERTY));
		assertEquals(list(B.B_VALUE_1), getAnnotations(B.class, A.FOO_PROPERTY));
		assertEquals(list(C1.C1_VALUE_1), getAnnotations(C1.class, A.FOO_PROPERTY));
		assertEquals(list(C2.C2_VALUE_1), getAnnotations(C2.class, A.FOO_PROPERTY));
		assertEquals(list(), getAnnotations(D.class, A.FOO_PROPERTY));
	}

	private ArrayList<Integer> getAnnotations(Class<? extends ConfigurationItem> configurationInterface,
			String propertyName) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configurationInterface);
		PropertyDescriptor property = descriptor.getProperty(propertyName);

		ArrayList<Integer> _result = new ArrayList<>();

		for (Annotation annotation : property.getLocalAnnotations()) {
			int value = value(annotation);
			if (value > 0) {
				_result.add(value);
			}
		}

		return _result;
	}

	private int value(Annotation annotation) {
		if (annotation instanceof Annotation1) {
			return ((Annotation1) annotation).value();
		}
		return -1;
	}

}
