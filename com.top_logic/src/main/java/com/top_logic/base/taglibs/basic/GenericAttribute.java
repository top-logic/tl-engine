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
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.StringServices;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Tag} rendering a single attribute.
 * 
 * <p>
 * Must solely be used in combination with / as child of the {@link GenericTag} tag.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericAttribute extends AbstractTagBase implements BodyTag {

	private String _name;

	private String _value;

	private boolean _hasValue;

	private BodyContent _bodyContent;

	/**
	 * The name of the attribute.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Optional value of the attribute.
	 * 
	 * <p>
	 * If not given, the contents of the tag is used as attribute value.
	 * </p>
	 */
	public void setValue(String value) {
		_hasValue = true;
		_value = value;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		if (_hasValue) {
			if (!HTMLConstants.BOOLEAN_ATTRIBUTES.contains(_name) || !StringServices.isEmpty(_value)) {
				getOut().writeAttribute(_name, _value);
			}
			return SKIP_BODY;
		} else {
			return EVAL_BODY_BUFFERED;
		}
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() throws JspException {
		// Nothing to do.
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		_bodyContent = bodyContent;
	}

	@Override
	public int doAfterBody() throws JspException {
		if (_bodyContent != null) {
			getOut().writeAttribute(_name, _bodyContent.getString());
		}
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		_name = null;
		_value = null;
		_hasValue = false;
		_bodyContent = null;
		super.teardown();
	}

}
