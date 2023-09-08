/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping;

/**
 * Test case for TLScript expressions that perform HTML rendering.
 */
@SuppressWarnings("javadoc")
public class TestHtmlMacroExpansion extends AbstractSearchExpressionTest {

	public void testHtmlTextQuoting() throws IOException {
		doTest("testHtmlTextQuoting", null);
	}

	public void testDynamicHtml() throws IOException {
		doTest("testDynamicHtml", null);
	}

	public void testEmbeddedScriptTag() throws IOException {
		doTest("testEmbeddedScriptTag", null);
	}

	private void doTest(String testName, TLObject model) throws IOException {
		SearchExpression expr =
			TemplateStorageMapping.INSTANCE.getBusinessObject(readScriptHtml(testName));
		HTMLFragment renderer = (HTMLFragment) execute(expr, model);
		TagWriter out = new TagWriter();
		renderer.write(new DefaultDisplayContext(null, null, null), out);

		String expectation = readExpectation(testName);
		assertEquals(expectation, out.toString());
	}

	private String readScriptHtml(String testName) throws IOException {
		return readResource(TestHtmlMacroExpansion.class.getSimpleName() + "-" + testName + ".script.html");
	}

	private String readExpectation(String testName) throws IOException {
		return readResource(TestHtmlMacroExpansion.class.getSimpleName() + "-" + testName + ".expected.html");
	}

	private String readResource(String resourceName) throws IOException, UnsupportedEncodingException {
		try (InputStream in = getClass().getResourceAsStream(resourceName)) {
			return StreamUtilities.readAllFromStream(in, "utf-8");
		}
	}

	public static Test suite() {
		return suite(TestHtmlMacroExpansion.class, SafeHTML.Module.INSTANCE);
	}

}
