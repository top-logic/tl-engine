/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;
import com.top_logic.model.search.expr.config.operations.UsesSecurity;
import com.top_logic.model.search.expr.interpreter.UpdateSecurityVisitor;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.TLScriptDocumentation;

/**
 * Test for {@link UsesSecurity} parameters of TL-Script functions.
 *
 * @see SecurityFlagFunctions
 */
@SuppressWarnings("javadoc")
public class TestUsesSecurity extends AbstractSearchExpressionTest {

	private static final String FUNCTION = SecurityFlagFunctions.PREFIX + "Combine";

	private static final String CALL = FUNCTION + "('a', 3)";

	public void testSecurityEnabledByDefault() throws Exception {
		assertEquals("a:true:3", eval(CALL));
	}

	public void testChainedCall() throws Exception {
		assertEquals("a:true:3", eval("'a'." + FUNCTION + "(3)"));
	}

	public void testNamedArguments() throws Exception {
		assertEquals("a:true:3", eval(FUNCTION + "(count: 3, prefix: 'a')"));
	}

	public void testSecurityDisabledInExpression() throws Exception {
		SearchExpression expr = search(CALL);
		UpdateSecurityVisitor.disableSecurity(expr);
		assertEquals("a:false:3", executeCompiled(expr));
	}

	public void testSecurityDisabledInExecutor() throws Exception {
		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(CALL));
		executor.disableSecurity();
		assertEquals("a:false:3", execute((DisplayContext) null, (TagWriter) null, executor));
	}

	public void testSecurityDisabledInFunction() throws Exception {
		SearchExpression expr = search("x -> " + FUNCTION + "($x, 3)");
		UpdateSecurityVisitor.disableSecurity(expr);
		assertEquals("b:false:3", executeCompiled(expr, "b"));
	}

	public void testFlagIsNoScriptArgument() {
		try {
			search(FUNCTION + "('a', true, 3)");
			fail("The security flag must not be passable as script argument.");
		} catch (Exception ex) {
			// Expected: too many arguments.
		}
		try {
			search(FUNCTION + "(prefix: 'a', " + SecurityFlagFunctions.SECURITY_PARAM + ": true, count: 3)");
			fail("The security flag must not be passable as named script argument.");
		} catch (Exception ex) {
			// Expected: unknown argument.
		}
	}

	public void testDescriptorOmitsFlag() {
		MethodBuilder<?> builder = SearchBuilder.getInstance().getBuilder(FUNCTION);
		assertNotNull("Function '" + FUNCTION + "' not registered.", builder);
		ArgumentDescriptor descriptor = builder.descriptor();
		assertEquals(List.of("prefix", "count"), List.copyOf(descriptor.getArgumentNames()));
		assertEquals(2, descriptor.getMaxArgCnt());
	}

	public void testDocumentationOmitsFlag() {
		String doc = TLScriptDocumentation.documentation(DummyDisplayContext.newInstance(), FUNCTION).orElse(null);
		assertNotNull("No documentation for '" + FUNCTION + "'.", doc);
		assertTrue(doc, doc.contains("prefix"));
		assertTrue(doc, doc.contains("count"));
		assertFalse(doc, doc.contains(SecurityFlagFunctions.SECURITY_PARAM));
	}

	public void testNoCompileTimeEvaluation() throws Exception {
		TLScriptMethod.Builder builder = (TLScriptMethod.Builder) SearchBuilder.getInstance().getBuilder(FUNCTION);
		TLScriptMethod call = builder.build(null, new SearchExpression[] {
			SearchExpressionFactory.literal("a"),
			SearchExpressionFactory.literal(3.0) });
		assertTrue(call.usesSecurity());
		assertFalse(call.canEvaluateAtCompileTime(new Object[] { "a", 3.0 }));
		assertTrue("Copy must keep the security flag.",
			((TLScriptMethod) call.copy(call.getArguments())).usesSecurity());
		call.setUsesSecurity(false);
		assertFalse("Copy must keep the security flag.",
			((TLScriptMethod) call.copy(call.getArguments())).usesSecurity());
	}

	public void testNonBooleanFlagRejected() throws Exception {
		TLScriptMethod.Builder.Config config = (TLScriptMethod.Builder.Config) TypedConfiguration
			.createConfigItemForImplementationClass(TLScriptMethod.Builder.class);
		config.setName("testSecurityBroken");
		config.setMethod(BrokenFunctions.class.getName() + TLScriptMethod.Builder.METHOD_SEPARATOR + "broken");
		try {
			new TLScriptMethod.Builder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
			fail("A non-boolean security parameter must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(UsesSecurity.class.getSimpleName()));
		}
	}

	/**
	 * Functions with an invalid {@link UsesSecurity} parameter, not registered as TL-Script
	 * functions.
	 */
	public static class BrokenFunctions {

		/**
		 * Function with a {@link UsesSecurity} parameter that is not a <code>boolean</code>.
		 */
		public static String broken(@UsesSecurity String usesSecurity) {
			return usesSecurity;
		}
	}

	public static Test suite() {
		return suite(TestUsesSecurity.class);
	}

}
