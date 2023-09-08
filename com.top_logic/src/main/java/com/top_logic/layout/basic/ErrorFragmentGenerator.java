/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * Creates an UI error fragment, which can be used for indication of faulty UI fragments.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ErrorFragmentGenerator {

	/** CSS class for error fragment */
	public static final String ERROR_FRAGMENT_CONTAINER_CLASS = "errorFragmentContainer";

	/** CSS class for error fragment image */
	public static final String ERROR_FRAGMENT_IMAGE_CLASS = "errorFragmentImage";

	/**
	 * Writes an error fragment, whereby errorReason will be displayed as tooltip.
	 */
	public static void writeErrorFragment(DisplayContext context, TagWriter out, String errorReason)
			throws IOException {
		// Make sure that this can be called in any context.
		returnToElementContent(out);
		ResKey tooltip = I18NConstants.RENDERING_ERROR_TOOLTIP.fill(errorReason != null ? errorReason : "");
		writeErrorFragment(context, out, SPAN, I18NConstants.RENDERING_ERROR, tooltip);
	}

	/**
	 * Writes an error fragment: an error image followed by an error message.
	 * 
	 * @param tagName
	 *        Name of the root tag of the written fragment.
	 * @param message
	 *        The actual error message to write.
	 * @param tooltip
	 *        An optional tooltip to write. May be <code>null</code>.
	 */
	public static void writeErrorFragment(DisplayContext context, TagWriter out, String tagName, ResKey message,
			ResKey tooltip) throws IOException {
		out.beginBeginTag(tagName);
		out.writeAttribute(CLASS_ATTR, ERROR_FRAGMENT_CONTAINER_CLASS);
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out,
				Resources.getInstance().getString(tooltip));
		}
		out.endBeginTag();
		writeErrorImage(context, out);
		out.writeText(Resources.getInstance().getString(message));
		out.endTag(tagName);
	}

	/**
	 * Encapsulates writing an error fragment into an {@link HTMLFragment}.
	 * 
	 * @see #writeErrorFragment(DisplayContext, TagWriter, String, ResKey, ResKey)
	 */
	public static HTMLFragment errorFragment(String tagName, ResKey message, ResKey tooltip) {
		return new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeErrorFragment(context, out, tagName, message, tooltip);
			}
		};
	}

	private static void returnToElementContent(TagWriter out) throws IOException {
		while (true) {
			switch (out.getState()) {
				case ELEMENT_CONTENT:
					return;
				case ATTRIBUTE_START:
				case ATTRIBUTE: {
					out.endAttribute();
					break;
				}
				case CDATA_CONTENT: {
					out.endCData();
					break;
				}
				case CLASS_ATTRIBUTE_START:
				case CLASS_ATTRIBUTE: {
					out.endCssClasses();
					break;
				}
				case COMMENT_CONTENT: {
					out.endComment();
					break;
				}
				case JS_SCRIPT_STRING:
				case JS_ELEMENT_STRING:
				case JS_ATTRIBUTE_STRING:
				case JS_CDATA_STRING: {
					out.endJsString();
					break;
				}
				case SCRIPT: {
					out.endScript();
					break;
				}
				case START_TAG: {
					out.endBeginTag();
					break;
				}
			}
		}
	}

	/**
	 * Writes an error image (e.g. used by
	 * {@link #writeErrorFragment(DisplayContext, TagWriter, String)}).
	 */
	public static void writeErrorImage(DisplayContext context, TagWriter out) throws IOException {
		Icons.RENDERING_ERROR_NORMAL.writeWithCss(context, out, ERROR_FRAGMENT_IMAGE_CLASS);
	}

}
