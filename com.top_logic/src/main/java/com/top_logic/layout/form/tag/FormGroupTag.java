/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellTag;
import com.top_logic.layout.form.model.FormGroup;

/**
 * The class {@link FormGroupTag} can be used to render the contents of a {@link FormGroup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormGroupTag extends GroupCellTag {

	/**
	 * The XML replacement for a piece of dynamic contents.
	 * 
	 * @see #writeControls(DisplayContext, TagWriter, String, List)
	 */
	public static final String PLACEHOLDER = "<placeHolder/>";

	private static final String FORM_GROUP_TAG = "form:group";

	@Override
	protected String getTagName() {
		return FORM_GROUP_TAG;
	}

	public void setCollapsible(boolean collapsible) {
		super.setPreventCollapse(!collapsible);
	}

	public static boolean assertWellformedXML(String content) {
		content = new StringBuffer("<root>").append(content).append("</root>").toString();
		try {
			SAXUtil.newSAXParser().parse(new InputSource(new StringReader(content)), new DefaultHandler());
			return true;
		} catch (ParserConfigurationException ex) {
			throw (AssertionError) new AssertionError("Unable to check wellformed of JSP contents.").initCause(ex);
		} catch (SAXException ex) {
			throw (AssertionError) new AssertionError("Non-wellformed JSP contents (" + ex.getMessage() + ") content: "
				+ content).initCause(ex);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Buffered operations must not fail.", ex);
		}
	}

	/**
	 * Renders a content pattern by replacing occurrences of {@link FormGroupTag#PLACEHOLDER} with
	 * the given dynamic content of the corresponding index.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The target writer.
	 * @param contentPattern
	 *        The pattern containing static content interleaved with
	 *        {@link FormGroupTag#PLACEHOLDER} references.
	 * @param dynamicFragments
	 *        The dynamic contents to render for each occurrence of {@link FormGroupTag#PLACEHOLDER}
	 *        in the content pattern.
	 */
	public static void writeControls(DisplayContext context, TagWriter out, String contentPattern,
			List<? extends HTMLFragment> dynamicFragments) throws IOException {
		int contentStart = 0;
		for (int viewIndex = 0, cnt = dynamicFragments.size(); viewIndex < cnt; viewIndex++) {
			int nextSeparatorIndex = contentPattern.indexOf(PLACEHOLDER, contentStart);
			if (nextSeparatorIndex >= 0) {
				out.writeContent(contentPattern.substring(contentStart, nextSeparatorIndex));
				contentStart = nextSeparatorIndex + PLACEHOLDER.length();
			} else {
				contentStart = contentPattern.length();
			}
			dynamicFragments.get(viewIndex).write(context, out);
		}
		out.writeContent(contentPattern.substring(contentStart));
	}
}
