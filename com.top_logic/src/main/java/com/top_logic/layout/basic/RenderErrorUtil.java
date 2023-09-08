/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentContentHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tag.JSPErrorUtil;

/**
 * Utility for handling errors, which occurred during rendering.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RenderErrorUtil {

	/**
	 * Attribute, that holds the {@link Throwable} caught during rendering.
	 */
	public static final String RENDERING_ERROR_ATTRIBUTE = "renderingError";

	/**
	 * Attribute, that holds an error message reported during rendering.
	 */
	public static final String RENDERING_ERROR_MESSAGE_ATTRIBUTE = "errorMessage";

	private static final String ERROR_JSP = "/jsp/display/error/JspErrorPage.jsp";

	/**
	 * Logs the given {@link Throwable}, presents the given {@link ResKey} as {@link InfoService}
	 * message and produces an error fragment, rendered as explanation, that something went wrong
	 * during rendering.
	 * 
	 * @see InfoService#logError(DisplayContext, ResKey, Throwable, Object)
	 * @see #writeErrorFragment(DisplayContext, TagWriter, String, Object)
	 * 
	 */
	public static void produceErrorOutput(DisplayContext context, TagWriter out, ResKey userMessage, String logMessage,
			Throwable throwable, Object caller) throws IOException {
		InfoService.logError(context, userMessage, logMessage, throwable, caller);
		writeErrorFragment(context, out, logMessage, caller);
	}

	/**
	 * Renders an error marker with some explanation as placeholder.
	 */
	public static final void writeErrorFragment(DisplayContext context, TagWriter out, String errorReason,
			Object caller) throws IOException {
		int currentDepth = out.getDepth();
		try {
			ErrorFragmentGenerator.writeErrorFragment(context, out, errorReason);
		} catch (Throwable exceptionError) {
			out.endAll(currentDepth);
			Logger.error(exceptionError.getMessage(), exceptionError, caller);
		}
	}

	/**
	 * Reports an error during component rendering.
	 * 
	 * @param context
	 *        The current rendering {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} that has potentially been written to.
	 * @param component
	 *        The context {@link LayoutComponent}.
	 * @param tagDepthBefore
	 *        The tag depth to restore after reporting the error.
	 * @param ex
	 *        The {@link Throwable} caught.
	 */
	public static void reportComponentRenderingError(DisplayContext context, TagWriter out, LayoutComponent component,
			int tagDepthBefore, Throwable ex) throws IOException {
		String errorMessage = ExceptionUtil.createErrorMessage(
			"Unable to render component " + JSPErrorUtil.getComponentInformation(component) + ".", ex);
		reportComponentRenderingError(context, out, tagDepthBefore, errorMessage, ex);
	}

	/**
	 * Reports an error during component rendering.
	 * 
	 * @param context
	 *        The current rendering {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} that has potentially been written to.
	 * @param tagDepthBefore
	 *        The tag depth to restore after reporting the error.
	 * @param errorMessage
	 *        The message to report.
	 * @param ex
	 *        An optional {@link Throwable} that was caught.
	 */
	public static void reportComponentRenderingError(DisplayContext context, TagWriter out, int tagDepthBefore,
			String errorMessage, Throwable ex) throws IOException {
		if (noOutputWritten(out)) {
			try {
				HttpServletRequest asRequest = context.asRequest();
				asRequest.setAttribute(RENDERING_ERROR_ATTRIBUTE, ex);
				asRequest.setAttribute(RENDERING_ERROR_MESSAGE_ATTRIBUTE, errorMessage);
				context.asServletContext().getRequestDispatcher(ERROR_JSP).include(asRequest, context.asResponse());
			} catch (ServletException servletException) {
				// Ignore
			}
		} else {
			out.endAll(tagDepthBefore);
			RenderErrorUtil.produceErrorOutput(context, out,
				com.top_logic.layout.form.component.I18NConstants.ERROR_VIEW_CREATION, errorMessage, ex,
				ComponentContentHandler.class);
		}
	}

	private static boolean noOutputWritten(TagWriter out) {
		return out.getDepth() == 0;
	}

}
