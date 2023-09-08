/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for covariant return type overriding in typed configuration properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationRefinement extends BasicTestCase {

	public interface B1 extends ConfigurationItem {
		String getFoo();
	}

	public interface B2 extends B1 {
		String getBar();
	}

	public interface A1 extends ConfigurationItem {
		B1 getB();

		void setB(B1 value);
	}

	public interface A2 extends A1 {
		@Override
		B2 getB();
	}

	public interface A3 extends A1 {
		String getOther();
	}

	public interface A4 extends A2, A3 {
		String getYetAnother();
	}

	/**
	 * Getter with more specific type than setter.
	 */
	public interface A5 extends ConfigurationItem {

		B2 getB();

		void setB(B1 value);

	}

	/**
	 * Setter with more specific type than getter.
	 */
	@NoImplementationClassGeneration
	public interface A6 extends ConfigurationItem {

		B1 getB();

		void setB(B2 value);

	}

	private B1 _b1;

	private B2 _b2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_b1 = TypedConfiguration.newConfigItem(B1.class);
		_b2 = TypedConfiguration.newConfigItem(B2.class);
	}

	@Override
	protected void tearDown() throws Exception {
		_b1 = null;
		_b2 = null;
		super.tearDown();
	}

	public void testA1() {
		A1 a1 = TypedConfiguration.newConfigItem(A1.class);
		assertAssignAllB(a1);
	}

	public void testA2() {
		A2 a2 = TypedConfiguration.newConfigItem(A2.class);
		assertOnlyAllowB2WithA1Interface(a2);
		assertOnlyAllowB2WithA2Interface(a2);
	}

	public void testA3() {
		A3 a3 = TypedConfiguration.newConfigItem(A3.class);
		assertAssignAllB(a3);
	}

	public void testA4() {
		A4 a4 = TypedConfiguration.newConfigItem(A4.class);
		assertOnlyAllowB2WithA1Interface(a4);
		assertOnlyAllowB2WithA2Interface(a4);
	}

	public void testA5() {
		A5 a5 = TypedConfiguration.newConfigItem(A5.class);
		assertOnlyAllowB2WithA5Interface(a5);
	}

	public void testA6() {
		try {
			TypedConfiguration.newConfigItem(A6.class);
			throw new AssertionFailedError("Must prevent creation of interface with more specific setter type.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private void assertAssignAllB(A1 a1) {
		a1.setB(_b2);
		a1.setB(_b1);
	}

	private void assertOnlyAllowB2WithA1Interface(A1 a2) {
		a2.setB(_b2);
		try {
			a2.setB(_b1);
			fail("Must not accept B1 as value of refined property.");
		} catch (RuntimeException ex) {
			// expected.
		}
	}

	private void assertOnlyAllowB2WithA2Interface(A2 a2) {
		a2.setB(_b2);
		try {
			a2.setB(_b1);
			fail("Must not accept B1 as value of refined property.");
		} catch (RuntimeException ex) {
			// expected.
		}
	}

	private void assertOnlyAllowB2WithA5Interface(A5 a5) {
		a5.setB(_b2);
		try {
			a5.setB(_b1);
			fail("Must not accept B1 as value of refined property.");
		} catch (RuntimeException ex) {
			// expected.
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTypedConfigurationRefinement.class));
	}

}
