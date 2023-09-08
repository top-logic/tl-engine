/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.template;

import static test.com.top_logic.basic.config.template.TestTemplateExpression.*;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.basic.config.template.TestTemplateExpression.Var;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.config.template.Eval;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.parser.ParseException;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.template.TemplateWriter;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Test for {@link TemplateWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTemplateWriter extends AbstractLayoutTest {

	public void testExpand() throws ParseException, IOException {
		assertExpand("<div>Hello world</div>", "<div>{$m}</div>", var("m", "Hello world"));
	}

	public void testExpandAttribute() throws ParseException, IOException {
		assertExpand("<div data-tooltip=\"My tooltip\">Hello world</div>",
			"<div data-tooltip='{$t}'>{$m}</div>", var("m", "Hello world"), var("t", "My tooltip"));
	}

	public void testCollapseAttribute() throws ParseException, IOException {
		assertExpand("<div>Hello world</div>",
			"<div data-tooltip='{$t}'>{$m}</div>", var("m", "Hello world"), var("t", null));
	}

	public void testAttributeEmptyString() throws ParseException, IOException {
		assertExpand("<div>Hello world</div>",
			"<div data-tooltip='{$t}'>{$m}</div>", var("m", "Hello world"), var("t", ""));
	}

	public void testInnerXml() throws ParseException, IOException {
		assertExpand("<ol><li>a</li><li>b</li></ol>",
			"<ol>{foreach(x : $l, null, {<li>{$x}</li>})}</ol>", var("l", list("a", "b")));
	}

	private void assertExpand(String expected, String template, Var... vars) throws ParseException, IOException {
		TemplateExpression expression = parse(template);
		TagWriter buffer = new TagWriter();
		expression.visit(new TemplateWriter(displayContext(), buffer),
			new Eval.EvalContext(null, vars(vars)));
		assertEquals(expected, buffer.toString());
	}

	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestTemplateWriter.class,
			SecurityComponentCache.Module.INSTANCE));
	}
}
