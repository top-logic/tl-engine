/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test case for custom annotations on configuration interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationAnnotation extends TestCase {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnnotation {
		int value();
	}

	public interface A extends ConfigurationItem {

		String FOO_PROPERTY = "foo";

		@Name(FOO_PROPERTY)
		String getFoo();

	}

	public interface A1 extends A {

		@Override
		String getFoo();

	}

	public interface B extends A {

		public static final int B_VALUE = 13;

		@MyAnnotation(B_VALUE)
		@Override
		String getFoo();

	}

	public interface BS extends B {
		@Override
		public String getFoo();
	}

	public interface BSS extends BS {
		@Override
		public String getFoo();
	}

	public interface C extends A1 {

		public static final int C_VALUE = 42;

		@MyAnnotation(C_VALUE)
		@Override
		String getFoo();

	}

	public interface D extends B {

		String getBar();

	}

	public interface E extends C {

		String getFooBar();

	}

	public interface DE extends D, E {

		@Override
		String getFooBar();

	}

	public interface ED extends E, D {

		@Override
		String getFooBar();

	}

	public void testAnnotation() {
		assertNull(getMyAnnotation(A.class, A.FOO_PROPERTY));
		assertNull(getMyAnnotation(A1.class, A.FOO_PROPERTY));
		assertEquals(B.B_VALUE, getMyAnnotation(B.class, A.FOO_PROPERTY).value());
		assertEquals(B.B_VALUE, getMyAnnotation(BS.class, A.FOO_PROPERTY).value());
		assertEquals(B.B_VALUE, getMyAnnotation(BSS.class, A.FOO_PROPERTY).value());
		assertEquals(C.C_VALUE, getMyAnnotation(C.class, A.FOO_PROPERTY).value());
		assertEquals(B.B_VALUE, getMyAnnotation(D.class, A.FOO_PROPERTY).value());
		assertEquals(C.C_VALUE, getMyAnnotation(E.class, A.FOO_PROPERTY).value());
		assertEquals(B.B_VALUE, getMyAnnotation(DE.class, A.FOO_PROPERTY).value());
		assertEquals(C.C_VALUE, getMyAnnotation(ED.class, A.FOO_PROPERTY).value());
	}

	private MyAnnotation getMyAnnotation(Class<? extends ConfigurationItem> configurationInterface, String propertyName) {
		return TypedConfiguration.getConfigurationDescriptor(configurationInterface).getProperty(propertyName)
			.getAnnotation(MyAnnotation.class);
	}

}
