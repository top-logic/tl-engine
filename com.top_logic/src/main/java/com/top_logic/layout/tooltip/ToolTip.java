/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.io.character.NonClosingProxyWriter;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The content of a tool-tip attribute.
 * 
 * <p>
 * A tool-tip must be rendered either within a tag using
 * {@link #writeAttribute(DisplayContext, TagWriter)}, or within a
 * {@link HTMLConstants#TL_TOOLTIP_ATTR} attribute using {@link #write(DisplayContext, TagWriter)}.
 * </p>
 * 
 * @see #writeAttribute(DisplayContext, TagWriter)
 */
public interface ToolTip extends WithProperties, HTMLFragment {

	@Override
	default void renderProperty(DisplayContext context, TagWriter out, String propertyName)
			throws IOException {
		switch (propertyName) {
			case "caption":
				writeCaption(context, out);
				return;
			case "content":
				writeContent(context, out);
				return;
			default:
				break;
		}
		WithProperties.super.renderProperty(context, out, propertyName);
	}

	/**
	 * Writes the contents of the tool-tip.
	 */
	void writeContent(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Whether a caption should be generated.
	 * 
	 * @return <code>true</code> to call {@link #writeCaption(DisplayContext, TagWriter)},
	 *         <code>false</code> otherwise.
	 */
	default boolean hasCaption() {
		return false;
	}

	/**
	 * Writes the tool-tip caption.
	 */
	default void writeCaption(DisplayContext context, TagWriter out) throws IOException {
		// None by default.
	}

	/**
	 * Writes the content of a tool-tip data attribute.
	 * 
	 * @see HTMLConstants#TL_TOOLTIP_ATTR
	 */
	@Override
	default void write(DisplayContext context, TagWriter out) throws IOException {
		// Write HTML quoted into the attribute.
		try (TagWriter inner = new TagWriter(new NonClosingProxyWriter(out))) {
			OverlibTooltipFragmentGenerator.template(hasCaption()).write(context, inner, this);
		}
	}

	/**
	 * Writes a HTML attribute containing the tool-tip data.
	 */
	default void writeAttribute(DisplayContext context, TagWriter out) throws IOException {
		out.beginAttribute(HTMLConstants.TL_TOOLTIP_ATTR);
		write(context, out);
		out.endAttribute();
	}
}
