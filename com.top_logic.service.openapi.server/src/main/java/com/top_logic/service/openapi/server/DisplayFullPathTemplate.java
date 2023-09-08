/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * {@link ControlProvider} creating a {@link Control} that renders the base <i>OpenAPI</i> URL.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayFullPathTemplate implements ControlProvider {

	private static final class LinkControl extends AbstractConstantControl {

		private final FormField _field;

		private LinkControl(FormField field) {
			_field = field;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			if (!_field.hasValue() || _field.hasError()) {
				out.beginBeginTag(SPAN);
				writeControlAttributes(context, out);
				out.endEmptyTag();
			} else {
				String link = createFullBaseURL(context, (String) _field.getValue());
				out.beginBeginTag(ANCHOR);
				writeControlAttributes(context, out);
				out.writeAttribute(HREF_ATTR, link);
				out.writeAttribute(TARGET_ATTR, HTMLConstants.BLANK_VALUE);
				out.endBeginTag();
				out.writeText(link);
				out.endTag(ANCHOR);
			}
		}
	}

	@Override
	public Control createControl(Object model, String style) {
		return new LinkControl((FormField) model);
	}

	/**
	 * Creates the full base <i>OpenAPI</i> URL.
	 * 
	 * @param context
	 *        Render context to determine host and context path of the link.
	 * @param relBasePath
	 *        Path relative to the application URL where the <i>OpenAPI</i> server can be reached.
	 */
	public static String createFullBaseURL(DisplayContext context, String relBasePath) {
		StringBuilder url = new StringBuilder();
		try {
			LayoutUtils.appendHostURL(context, url);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		url.append(context.getContextPath());
		url.append(relBasePath);
		return url.toString();
	}

}

