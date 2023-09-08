/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ClientAction} that sets a CSS class attribute on a client-side DOM element identified by
 * an ID.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractCssClassUpdate extends DOMAction {

	/**
	 * Client-side command key.
	 */
	public static final String CSS_CLASS_UPDATE_XSI_TYPE = "CssClassUpdate";

	/**
	 * Create a new {@link AbstractCssClassUpdate}
	 */
	public AbstractCssClassUpdate(String elementID) {
		super(elementID);
	}

	@Override
	protected String getXSIType() {
		return CSS_CLASS_UPDATE_XSI_TYPE;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
		super.writeChildrenAsXML(context, writer);

		writer.beginBeginTag(AJAXConstants.AJAX_PROPERTY);
		writer.beginCssClasses();

		int currentDepth = writer.getDepth();
		try {
			writeCssClassContent(context, writer);
			writer.endCssClasses();
			writer.endEmptyTag();
		} catch (Exception throwable) {
			writer.endAll(currentDepth);
			writer.endTag(AJAXConstants.AJAX_PROPERTY);
			Logger.error("Could not create css class update (Cause: " + throwable.getMessage(), throwable, this);
		}
	}

	/**
	 * Creates the content of the {@link HTMLConstants#CLASS_ATTR}.
	 * 
	 * <p>
	 * The given {@link TagWriter} is in CSS class mode so that multiple classes can be written
	 * using multiple {@link Appendable#append(CharSequence)} calls without caring about separators.
	 * </p>
	 */
	protected abstract void writeCssClassContent(DisplayContext context, Appendable out) throws IOException;
}
