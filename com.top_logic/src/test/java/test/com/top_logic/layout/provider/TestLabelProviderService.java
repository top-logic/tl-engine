/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * Test of {@link LabelProviderService}.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
public class TestLabelProviderService extends BasicTestCase {

	private static final String INTERFACE = "interface";

	private static final String CLASS = "class";

	/**
	 * Creates a new {@link TestLabelProviderService}.
	 */
	public TestLabelProviderService(String aName) {
		super(aName);
	}

	/**
	 * Tests that a class, for which a {@link LabelProvider} was configured, obtains a label.
	 */
	public void testClassLabelProvider() {
		testLabelProvider(new TestClassWithProvider(), CLASS);
	}

	/**
	 * Tests that a class, for whose implemented interface a {@link LabelProvider} was configured,
	 * obtains a label.
	 */
	public void testInterfaceLabelProvider() {
		testLabelProvider(new TestClassWithoutProvider(), INTERFACE);
	}

	private void testLabelProvider(Object object, final String expectedlabel) {
		LabelProviderService instance = LabelProviderService.getInstance();
		LabelProvider labelProvider = instance.getLabelProvider(object);
		assertTrue(expectedlabel.equals(labelProvider.getLabel(object)));
	}

	/**
	 * Return the suite of tests to execute.
	 */
	public static Test suite() {
		Test setup = ServiceTestSetup.createSetup(TestLabelProviderService.class, LabelProviderService.Module.INSTANCE);
		return BasicTestSetup.createBasicTestSetup(setup);
	}

	/**
	 * main function for direct testing.
	 */
	public static void main(String[] args) {
		Logger.configureStdout(); // "INFO"
		TestRunner.run(suite());
	}

	/** Empty interface. */
	public interface TestInterface {
		// Empty interface.
	}

	/** Empty class. */
	public class TestClassWithoutProvider implements TestInterface {
		/** Constructor. */
		public TestClassWithoutProvider() {
		}
	}
	
	/** Empty class. */
	public class TestClassWithProvider implements TestInterface {
		/** Constructor. */
		public TestClassWithProvider() {
		}
	}

	/**
	 * A {@link LabelProvider} for {@link TestInterface}.
	 */
	public static class TestInterfaceLabelProvider implements LabelProvider {
		/** Singleton instance. */
		public static final TestInterfaceLabelProvider INSTANCE = new TestInterfaceLabelProvider();

		private TestInterfaceLabelProvider() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			return INTERFACE;
		}

	}

	/**
	 * A {@link LabelProvider} for {@link TestClassWithProvider}.
	 */
	public static class TestClassLabelProvider implements LabelProvider {
		/** Singleton instance. */
		public static final TestClassLabelProvider INSTANCE = new TestClassLabelProvider();

		private TestClassLabelProvider() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			return CLASS;
		}

	}
}
