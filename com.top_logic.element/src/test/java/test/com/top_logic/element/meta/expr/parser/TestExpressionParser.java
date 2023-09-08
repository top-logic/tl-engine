/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.expr.parser;


import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementTestSetup;

import com.top_logic.element.config.ConfigTypeResolver;
import com.top_logic.element.meta.expr.internal.AssociationDestinations;
import com.top_logic.element.meta.expr.internal.AssociationSources;
import com.top_logic.element.meta.expr.internal.Chain;
import com.top_logic.element.meta.expr.internal.GetValue;
import com.top_logic.element.meta.expr.internal.MethodCall;
import com.top_logic.element.meta.expr.internal.NavigateBackwards;
import com.top_logic.element.meta.expr.internal.TypeOf;
import com.top_logic.element.meta.expr.parser.ExpressionParser;
import com.top_logic.element.meta.expr.parser.ParseException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.element.meta.kbbased.filtergen.CustomAttributeValueLocator;

/**
 * Test case for {@link ExpressionParser}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestExpressionParser extends BasicTestCase {

	public void testAssociationDestinations() throws ParseException {
		assertInstanceOf(AssociationDestinations.class,
			parse(">foo"));
	}

	public void testAssociationSources() throws ParseException {
		assertInstanceOf(AssociationSources.class,
			parse("<bar"));
	}

	public void testGetData() throws ParseException {
		assertInstanceOf(GetValue.class,
			parse("@attr"));
	}

	public void testMethodCall() throws ParseException {
		assertInstanceOf(MethodCall.class,
			parse("java.lang.Object#toString()"));
	}

	public void testNavigateBackwards() throws ParseException {
		assertInstanceOf(NavigateBackwards.class,
			parse("-module:Type#others"));
	}

	public void testTypeOf() throws ParseException {
		assertInstanceOf(TypeOf.class,
			parse("(module:Type)"));
	}

	public void testQualifiedAttribute() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("module:Type#attr"));
	}

	public void testChain1() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("asData()/>foo/<bar/asObject()"));
	}

	public void testChain2() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("asData()/@attr"));
	}

	public void testChain3() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("attr1/attr2"));
	}

	public void testIndexedAccess() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("attr[0]"));
	}

	public void testQualifiedIndexedAccess() throws ParseException {
		assertInstanceOf(Chain.class,
			parse("module:Type#attr[0]"));
	}

	public void testSubConfigurationQuoting() throws ParseException {
		assertEquals("someone's \\value\\",
			parse("SubConfiguredLocator('someone\\'s \\\\value\\\\')").locateAttributeValue(null));
	}

	private static AttributeValueLocator parse(String expressionSource) throws ParseException {
		return ConfigTypeResolver.parseLocator(expressionSource);
	}

	private void assertInstanceOf(Class<?> type, Object value) {
		assertTrue("Not an instance of '" + type.getName() + "': " + value, type.isInstance(value));
	}

	public static class SubConfiguredLocator extends CustomAttributeValueLocator {

		private final String _config;

		public SubConfiguredLocator(String config) {
			_config = config;
		}

		@Override
		public Object locateAttributeValue(Object anObject) {
			return _config;
		}

	}

	public static Test suite() {
		return ElementTestSetup.createElementTestSetup(ServiceTestSetup.createSetup(
			TestExpressionParser.class, AttributeValueLocatorFactory.Module.INSTANCE));
	}

}
