/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.Method;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod.Builder;

/**
 * Test for binding a TL-Script function to a Java method that shares its name with other methods.
 *
 * @see OverloadFunctions
 */
@SuppressWarnings("javadoc")
public class TestScriptFunctionOverloads extends AbstractSearchExpressionTest {

	public void testPrivateHelperIgnored() throws Exception {
		assertEquals("public:3", eval(OverloadFunctions.PREFIX + "Value('abc')"));
		assertEquals(OverloadFunctions.class.getMethod("value", String.class),
			SearchBuilder.getInstance().getBuilder(OverloadFunctions.PREFIX + "Value").getId());
	}

	public void testUnqualifiedNameWithOneScriptFunction() throws Exception {
		Builder builder = newBuilder(OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "value");
		assertEquals(OverloadFunctions.class.getMethod("value", String.class), builder.getId());
	}

	public void testPublicOverloadsWithDistinctNames() throws Exception {
		assertEquals("text:abc", eval(OverloadFunctions.PREFIX + "Text('abc')"));
		assertEquals("number:42", eval(OverloadFunctions.PREFIX + "Number(42)"));
	}

	public void testUnqualifiedOverloadRejected() throws ConfigurationException {
		String spec = OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "format";
		try {
			newBuilder(spec);
			fail("An ambiguous method name must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(spec));
			assertTrue(ex.getMessage(), ex.getMessage().contains("format(int)"));
			assertTrue(ex.getMessage(), ex.getMessage().contains("format(java.lang.String)"));
		}
	}

	public void testExplicitSignature() throws Exception {
		Method textFormat = OverloadFunctions.class.getMethod("format", String.class);
		Method numberFormat = OverloadFunctions.class.getMethod("format", int.class);

		assertEquals(textFormat, newBuilder(Builder.signature(textFormat)).getId());
		assertEquals(numberFormat, newBuilder(Builder.signature(numberFormat)).getId());
		assertEquals(numberFormat,
			newBuilder(OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "format( int )").getId());
	}

	public void testSignatureFormat() throws Exception {
		assertEquals(OverloadFunctions.class.getName() + "#format(java.lang.String)",
			Builder.signature(OverloadFunctions.class.getMethod("format", String.class)));
		assertEquals(OverloadFunctions.class.getName() + "#format(int)",
			Builder.signature(OverloadFunctions.class.getMethod("format", int.class)));
	}

	public void testNonMatchingSignatureRejected() throws ConfigurationException {
		String spec = OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "format(long)";
		assertRejected(spec);
	}

	public void testPrivateSignatureRejected() throws ConfigurationException {
		String spec = OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "value(int)";
		assertRejected(spec);
	}

	public void testInstanceSignatureRejected() throws ConfigurationException {
		String spec = OverloadFunctions.class.getName() + Builder.METHOD_SEPARATOR + "value()";
		assertRejected(spec);
	}

	private static void assertRejected(String spec) throws ConfigurationException {
		try {
			newBuilder(spec);
			fail("Method '" + spec + "' is no script function and must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(spec));
		}
	}

	private static Builder newBuilder(String method) throws ConfigurationException {
		TLScriptMethod.Builder.Config config = (TLScriptMethod.Builder.Config) TypedConfiguration
			.createConfigItemForImplementationClass(TLScriptMethod.Builder.class);
		config.setName("testOverloadBuilder");
		config.setMethod(method);
		return new TLScriptMethod.Builder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	public static Test suite() {
		return suite(TestScriptFunctionOverloads.class);
	}

}
