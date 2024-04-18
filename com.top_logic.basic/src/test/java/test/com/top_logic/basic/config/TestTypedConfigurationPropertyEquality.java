/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.StringServices.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for equality of {@link PropertyDescriptor}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationPropertyEquality extends TestCase {

	@Retention(RUNTIME)
	public @interface ExampleAnnotation {
		public int value();
	}

	private static final String FOO = "foo";

	public interface A extends ConfigurationItem {

		@ExampleAnnotation(1)
		String getFoo();

	}

	public interface B extends ConfigurationItem {

		@ExampleAnnotation(1)
		String getFoo();

	}

	public interface C extends A {
		// Empty
	}

	public interface D extends A {

		@Override
		String getFoo();

	}

	public interface E extends A {

		@ExampleAnnotation(2)
		@Override
		String getFoo();

	}

	public void testDifferentTypes() {
		assertNotEquals(
			getProperty(A.class, FOO),
			getProperty(B.class, FOO));
	}

	public void testIdentifierDifferentTypes() {
		assertNotEquals(
			getProperty(A.class, FOO).identifier(),
			getProperty(B.class, FOO).identifier());
	}

	public void testTypeAndEmptySubtype() {
		assertNotEquals(
			getProperty(A.class, FOO),
			getProperty(C.class, FOO));
	}

	public void testIdentifierTypeAndEmptySubtype() {
		assertEquals(
			getProperty(A.class, FOO).identifier(),
			getProperty(C.class, FOO).identifier());
	}

	public void testTypeAndSubtypeWithSimpleOverride() {
		assertNotEquals(
			getProperty(A.class, FOO),
			getProperty(D.class, FOO));
	}

	public void testIdentifierTypeAndSubtypeWithSimpleOverride() {
		assertEquals(
			getProperty(A.class, FOO).identifier(),
			getProperty(D.class, FOO).identifier());
	}

	public void testOverrideAnnotationValue() {
		assertNotEquals(
			getProperty(A.class, FOO),
			getProperty(E.class, FOO));
	}

	public void testIdentifierOverrideAnnotationValue() {
		assertEquals(
			getProperty(A.class, FOO).identifier(),
			getProperty(E.class, FOO).identifier());
	}

	private void assertNotEquals(Object left, Object right) {
		if (left.equals(right)) {
			fail("Expected objects to be not equal,  but one is " + getObjectDescription(left) + " and the other is "
				+ getObjectDescription(right) + ".");
		}
	}

	private PropertyDescriptor getProperty(Class<? extends ConfigurationItem> type, String propertyName) {
		return TypedConfiguration.getConfigurationDescriptor(type).getProperty(propertyName);
	}

}
