/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;
import com.top_logic.basic.func.Function1;

/**
 * Test case for {@link Abstract} {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationAbstract extends BasicTestCase {

	@Abstract
	public interface A extends ConfigurationItem {

		@Abstract
		String getFoo();

		void setFoo(String value);
	}

	public interface A1 extends A {
		@Override
		public String getFoo();
	}

	public interface A2 extends A {
		String X = "x";

		@Name(X)
		int getX();

		void setX(int value);

		@Override
		@Derived(fun = IntegerToString.class, args = @Ref(X))
		public String getFoo();

		class IntegerToString extends Function1<String, Integer> {
			@Override
			public String apply(Integer arg) {
				return Integer.toString(arg);
			}
		}

	}

	public void testAFailInstantiate() {
		try {
			TypedConfiguration.newConfigItem(A.class);
			fail("Must not allow instantiating abstract class.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testA1() {
		A1 a1 = TypedConfiguration.newConfigItem(A1.class);
		a1.setFoo("Hello");

		checkAsA("Hello", a1);
	}

	public void testA2() {
		A2 a2 = TypedConfiguration.newConfigItem(A2.class);
		a2.setX(42);

		checkAsA("42", a2);
	}

	private void checkAsA(String expected, A a) {
		assertEquals(expected, a.getFoo());
	}

	public void testA2FailUpdate() {
		A2 a2 = TypedConfiguration.newConfigItem(A2.class);

		A a = a2;
		try {
			a.setFoo("Hello");
			fail("Must not allow setting a derived property.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	/**
	 * Non-abstract interface with abstract method.
	 */
	@NoImplementationClassGeneration
	public interface FailB extends ConfigurationItem {
		@Abstract
		String getBar();
	}

	public void testFailB() {
		try {
			TypedConfiguration.newConfigItem(FailB.class);
			fail("Invalid configuration interface, must be declared abstract.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			assertContains("must be declared abstract", ex.getMessage());
		}
	}

	/**
	 * Abstract interface without abstract methods.
	 */
	@Abstract
	public interface C extends ConfigurationItem {
		String getBar();
	}

	public void testC() {
		try {
			TypedConfiguration.newConfigItem(C.class);
			fail("Invalid configuration interface, must be declared abstract.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			assertContains("cannot be instantiated", ex.getMessage());
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTypedConfigurationAbstract.class));
	}

}
