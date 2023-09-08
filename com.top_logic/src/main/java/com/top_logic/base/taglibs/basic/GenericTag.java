/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Tag} to render a single HTML tag on a JSP.
 * 
 * <p>
 * This tag should be used, instead of literally writing the HTML on the JSP page, if the page
 * cannot be formulated in well-formed XML otherwise:
 * </p>
 * 
 * <p>
 * The following JSP code cannot be checked to be well-formed XML, because the boolean attribute
 * <code>checked</code> must be omitted to represent the <code>false</code> value:
 * </p>
 * 
 * <pre>
 * &lt;input name="my-name" <%=someCondition ? "checked=\"checked\"" : ""%>/>
 * </pre>
 * 
 * <p>
 * Formulating JSP contents this way, the XML/HTML part of the JSP (considering all JSP code as
 * plain text) represents no well-formed XML. This violates constraints implied by the
 * <code>normalize_jsps</code> task.
 * </p>
 * 
 * <p>
 * Instead, the code can be formulated with JSP tags without violating constraints:
 * </p>
 * 
 * <pre>
 * &lt;form:tag name="input">
 *   &lt;form:attribute name="name" value="my-name">
 *   &lt;form:attribute name="checked" value="<%=someCondition ? "checked" : null%>">
 * &lt;/form:tag>
 * </pre>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericTag extends AbstractTagBase implements BodyTag {

	private String _name;

	private BodyContent _bodyContent;

	/**
	 * The tag name of the generated HTML tag.
	 */
	public void setName(String name) {
		_name = name;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		// In case the tag is nested in some other generic tag.
		if (getOut().getState() == State.START_TAG) {
			getOut().endBeginTag();
		}

		getOut().beginBeginTag(_name);
		return EVAL_BODY_BUFFERED;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		if (HTMLConstants.VOID_ELEMENTS.contains(_name)) {
			getOut().endEmptyTag();
		} else {
			if (getOut().getState() == State.START_TAG) {
				getOut().endBeginTag();
			}
			getOut().endTag(_name);
		}
		return EVAL_PAGE;
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		_bodyContent = bodyContent;
	}

	@Override
	public void doInitBody() throws JspException {
		// Ignore.
	}

	@Override
	public int doAfterBody() throws JspException {
		if (_bodyContent != null) {
			String content = _bodyContent.getString().trim();
			try {
				getOut().writeContent(content);
			} catch (IOException ex) {
				throw new JspException(ex);
			}
		}
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		_name = null;
		_bodyContent = null;
		super.teardown();
	}

}
