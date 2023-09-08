/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tag;

import java.io.IOException;

import javax.servlet.jsp.PageContext;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Utility for handling errors, which occurred during JSP rendering.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class JSPErrorUtil {

	/**
	 * Logs the given {@link Throwable} and produces an error fragment, rendered as explanation,
	 * that something went wrong during rendering.
	 */
	public static void produceErrorOutput(PageContext pageContext, String logMessage, Throwable cause,
			Object caller) throws IOException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
		TagWriter out = MainLayout.getTagWriter(pageContext);
		produceErrorOutput(displayContext, out, logMessage, cause, caller);
	}

	/**
	 * Logs the given {@link Throwable} and produces an error fragment, rendered as explanation,
	 * that something went wrong during rendering.
	 */
	public static void produceErrorOutput(DisplayContext displayContext, TagWriter out, String logMessage,
			Throwable cause, Object caller) throws IOException {
		RenderErrorUtil.produceErrorOutput(displayContext, out, I18NConstants.ERROR_VIEW_CREATION, logMessage, cause,
			caller);
	}

	/**
	 * information about component name, component class and reference in layout file.
	 */
	public static String getComponentInformation(LayoutComponent component) {
		String unknownComponent = "unknown";
		if (component != null) {
			try {
				String componentName = component.getName().qualifiedName();
				return "with name '" + componentName + "' (Class: " + component.getClass() + ", Location: "
					+ component.getLocation() + ")";
			} catch (Throwable throwable) {
				return unknownComponent;
			}
		} else {
			return unknownComponent;
		}
	}

	/**
	 * Writes error message as simple text to the given output.
	 */
	public static void produceFailSafeErrorOutput(TagWriter out, String logMessage, Throwable throwable,
			Class<?> caller) {
		String errorDetail = InfoService.errorDetail(logMessage, throwable);
		Logger.error(errorDetail, throwable, caller);

		out.beginBeginTag(HTMLConstants.PARAGRAPH);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "font-weight: bold");
		out.endBeginTag();
		out.writeText(errorDetail);
		out.endTag(HTMLConstants.PARAGRAPH);
	}

}
