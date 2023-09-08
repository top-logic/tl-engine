/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.AbstractCssClassUpdate;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.CssClassUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link TestCase} for {@link AbstractCssClassUpdate}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestCssClassUpdate extends ActionTestcase {

	private static final String ID = "id";

	public void testSeparatedClassUpdate() throws Exception {
		AbstractCssClassUpdate update = new AbstractCssClassUpdate(ID) {
			@Override
			protected void writeCssClassContent(DisplayContext context, Appendable out) throws IOException {
				out.append("foo");
				out.append("bar");
			}
		};
		assertContains(update, "foo bar");
	}

	public void testEmptyClassUpdate() throws Exception {
		AbstractCssClassUpdate update = new CssClassUpdate(ID, "", null);
		assertContains(update, "");
	}

	private void assertContains(AbstractCssClassUpdate update, String updateValue) throws Exception {
		assertXMLOutput(update, createCssUpdateStructure(updateValue));
	}

	private String createCssUpdateStructure(String updateValue) throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag(AJAXConstants.AJAX_ACTION_ELEMENT);
		out.writeAttribute(AJAXConstants.AJAX_CONTEXT_ATTRIBUTE, null);
		out.writeContent(ClientAction.XSI_NAMESPACE_DECL);
		out.writeAttribute(AJAXConstants.XSI_TYPE_ATTRIBUTE, AbstractCssClassUpdate.CSS_CLASS_UPDATE_XSI_TYPE);
		out.endBeginTag();

		out.beginTag(AJAXConstants.AJAX_ID_ELEMENT);
		out.writeText(ID);
		out.endTag(AJAXConstants.AJAX_ID_ELEMENT);
		out.beginBeginTag(AJAXConstants.AJAX_PROPERTY);

		if (!StringServices.isEmpty(updateValue)) {
			out.writeAttribute(HTMLConstants.CLASS_ATTR, updateValue);
		}
		out.endEmptyTag();

		out.endTag(AJAXConstants.AJAX_ACTION_ELEMENT);
		out.flush();
		out.close();
		return out.toString();
	}

	public static Test suite() {
		return suite(TestCssClassUpdate.class);
	}

}
