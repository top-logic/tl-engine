/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewFunctions;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the TL-Script functions of {@link ViewFunctions}.
 */
public class TestViewFunctions extends BasicTestCase {

	/**
	 * <code>viewParse()</code> reads the XML text of a view into the configuration an attribute of
	 * the datatype <code>tl.view:ViewDefinition</code> stores.
	 */
	public void testParse() throws Exception {
		QueryExecutor parse = compile("xml -> viewParse($xml)");

		Object result = parse.execute(
			"<view><channels><channel name=\"token\"/><channel name=\"process\"/></channels>"
				+ "<text input=\"process\"/></view>");

		assertTrue(result instanceof ViewElement.Config);
		ViewElement.Config view = (ViewElement.Config) result;
		assertEquals(List.of("token", "process"),
			view.getChannels().stream().map(ChannelConfig::getName).toList());
		assertNotNull(view.getContent());
	}

	/**
	 * A text that is no view configuration is reported as an error the user can read.
	 */
	public void testParseInvalid() throws Exception {
		QueryExecutor parse = compile("xml -> viewParse($xml)");

		try {
			parse.execute("<view><no-such-element/></view>");
			fail("A view with an unknown element is no view configuration.");
		} catch (TopLogicException ex) {
			assertNotNull(ex.getErrorKey());
		}
	}

	private static QueryExecutor compile(String script) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
	}

	/**
	 * The suite, with the TL-Script functions.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestViewFunctions.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}

}
