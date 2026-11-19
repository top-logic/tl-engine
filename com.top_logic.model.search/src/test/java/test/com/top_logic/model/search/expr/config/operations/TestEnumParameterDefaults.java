/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;
import test.com.top_logic.model.search.expr.config.operations.EnumDefaultFunctions.Shade;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod.Builder;

/**
 * Test for the default values of optional enum parameters of TL-Script functions declared by
 * {@link FormattedDefault} and {@link NullDefault}.
 *
 * @see EnumDefaultFunctions
 */
@SuppressWarnings("javadoc")
public class TestEnumParameterDefaults extends AbstractSearchExpressionTest {

	private static final String PLAIN = EnumDefaultFunctions.PREFIX + "Plain";

	private static final String BY_CONSTANT_NAME = EnumDefaultFunctions.PREFIX + "ByConstantName";

	private static final String BY_EXTERNAL_NAME = EnumDefaultFunctions.PREFIX + "ByExternalName";

	private static final String UNSET = EnumDefaultFunctions.PREFIX + "Unset";

	/**
	 * Script functions that are not registered, because their declaring class is no
	 * {@link TLScriptFunctions}.
	 */
	public static class UnregisteredFunctions {

		/**
		 * Function with an enum parameter whose default names no constant.
		 */
		public static String invalidDefault(@FormattedDefault("no-such-shade") Shade shade) {
			return shade.name();
		}
	}

	public void testUnannotatedDefaultsToFirstConstant() throws Exception {
		assertEquals(Shade.LIGHT_GRAY.name(), eval(PLAIN + "()"));
	}

	public void testDefaultByConstantName() throws Exception {
		assertEquals(Shade.MEDIUM_GRAY.name(), eval(BY_CONSTANT_NAME + "()"));
	}

	public void testDefaultByExternalName() throws Exception {
		assertEquals(Shade.DARK_GRAY.name(), eval(BY_EXTERNAL_NAME + "()"));
	}

	public void testNullDefault() throws Exception {
		assertEquals(EnumDefaultFunctions.NONE, eval(UNSET + "()"));
	}

	public void testExplicitNull() throws Exception {
		assertEquals(EnumDefaultFunctions.NONE, eval(PLAIN + "(null)"));
		assertEquals(EnumDefaultFunctions.NONE, eval(BY_CONSTANT_NAME + "(null)"));
		assertEquals(EnumDefaultFunctions.NONE, eval(BY_EXTERNAL_NAME + "(null)"));
		assertEquals(EnumDefaultFunctions.NONE, eval(UNSET + "(null)"));
	}

	public void testGivenArgument() throws Exception {
		assertEquals(Shade.DARK_GRAY.name(), eval(PLAIN + "('dark-gray')"));
		assertEquals(Shade.LIGHT_GRAY.name(), eval(BY_CONSTANT_NAME + "('light-gray')"));
		assertEquals(Shade.MEDIUM_GRAY.name(), eval(BY_EXTERNAL_NAME + "('MEDIUM_GRAY')"));
		assertEquals(Shade.LIGHT_GRAY.name(), eval(UNSET + "('light-gray')"));
	}

	public void testInvalidDefaultRejected() throws Exception {
		String spec = UnregisteredFunctions.class.getName() + Builder.METHOD_SEPARATOR + "invalidDefault";
		try {
			newBuilder(spec);
			fail("Invalid enum default of '" + spec + "' must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("no-such-shade"));
			assertInstanceof(ex.getCause(), ConfigurationException.class);
		}
	}

	private static Builder newBuilder(String method) throws ConfigurationException {
		TLScriptMethod.Builder.Config config = (TLScriptMethod.Builder.Config) TypedConfiguration
			.createConfigItemForImplementationClass(TLScriptMethod.Builder.class);
		config.setName("testInvalidEnumDefault");
		config.setMethod(method);
		return new TLScriptMethod.Builder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	public static Test suite() {
		return suite(TestEnumParameterDefaults.class);
	}

}
