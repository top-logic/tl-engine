/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.template.model;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.layout.TestControl;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLCompare;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.template.model.internal.TemplateControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Test case for form {@link Templates}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTemplates extends TestControl {

	public void testHTMLFragment() throws IOException {
		assertRender("<div class=\"foo\"><span style=\"color: red;\">hello</span></div>",
			htmlTemplate(new HTMLFragment() {

				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(HTMLConstants.DIV);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "foo");
					out.endBeginTag();
					out.beginBeginTag(HTMLConstants.SPAN);
					out.writeAttribute(HTMLConstants.STYLE_ATTR, "color: red;");
					out.endBeginTag();
					out.write("hello");
					out.endTag(HTMLConstants.SPAN);
					out.endTag(HTMLConstants.DIV);
				}
			}));
	}

	public void testHTMLSource() throws IOException {
		assertRender("<div class=\"foo\"><span style=\"color: red;\">hello</span></div>",
			htmlSource("<div class=\"foo\"><span style=\"color: red;\">hello</span></div>"));
	}

	public void testLiterals() throws IOException {
		assertRender("<div class=\"foo\"><span style=\"color: red;\">hello</span></div>",
			div(css("foo"), span(style("color: red;"), text("hello"))));
	}

	private void assertRender(String expected, HTMLTemplateFragment template) throws IOException {
		// Render through a control, since this is the only public API.
		TemplateControlProvider cp =
			new TemplateControlProvider(div(template),
				DefaultFormFieldControlProvider.INSTANCE);
		Control control = cp.createControl(new FormGroup("noname", ResPrefix.forTest("noresource")));
		String result = tryWritingControl(control);

		// Skip the control element in comparision, since this contains an ID attribute that is not
		// predictable.
		XMLCompare compare = new XMLCompare(new AssertProtocol(), true, TrueFilter.INSTANCE);
		compare.assertEqualsNode(
			DOMUtil.parse(expected).getDocumentElement(),
			DOMUtil.parse(result).getDocumentElement().getFirstChild());
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return TestControl.suite(TestTemplates.class);
	}

}
