/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import java.io.IOError;
import java.io.IOException;

import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The {@link OverlibTooltipFragmentGenerator} using the 'onmouseout' and 'onmouseover' attribute to
 * generate tool tips for image map area tags and other ui relevant places.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OverlibTooltipFragmentGenerator implements ToolTipTagFragmentGenerator, HTMLConstants {

	/**
	 * Value to use in
	 * {@link #writeTooltipAttributes(DisplayContext, TagWriter, HTMLFragment, HTMLFragment)} for
	 * suppressing a tool-tip caption.
	 */
	public static final String NO_CAPTION = null;

    /** The single instance of this class. */
    public static final OverlibTooltipFragmentGenerator INSTANCE = new OverlibTooltipFragmentGenerator();

	/**
	 * Creates a {@link OverlibTooltipFragmentGenerator}.
	 * 
	 */
	private OverlibTooltipFragmentGenerator() {
	}

    /**
	 * @see org.jfree.chart.imagemap.ToolTipTagFragmentGenerator#generateToolTipFragment(java.lang.String)
	 * 
	 * @deprecated Result cannot safely be written to {@link TagWriter}, use
	 *             {@link #writeTooltipAttributes(DisplayContext, TagWriter, String)} instead.
	 */
	@Override
	@Deprecated
	public String generateToolTipFragment(String aToolTipText) {
		if (!StringServices.isEmpty(aToolTipText)) {
			DisplayContext context = DefaultDisplayContext.getDisplayContext();
			TagWriter out = new TagWriter();
			out.setState(TagWriter.State.START_TAG);
			try {
				new HtmlToolTip(aToolTipText, NO_CAPTION).writeAttribute(context, out);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			return out.toString();
		} else {
			return "";
		}
	}

    /**
	 * Write the attributes for the tool tip to the given writer.
	 * 
	 * This method expects that the tag is already opened.
	 * 
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The writer to be used for writing the attributes to.
	 * @param tooltipHtml
	 *        The HTML source code to display as tool-tip text.
	 */
	public void writeTooltipAttributes(DisplayContext context, TagWriter out, String tooltipHtml) throws IOException {
		if (!StringServices.isEmpty(tooltipHtml)) {
			new HtmlToolTip(tooltipHtml, NO_CAPTION).writeAttribute(context, out);
		}
    }
    
	/**
	 * Adds a text tool-tip to the currently open tag.
	 * 
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The writer to be used for writing the attributes to.
	 * @param tooltipText
	 *        The plain text to display as tool-tip text.
	 */
	public void writeTooltipAttributesPlain(DisplayContext context, TagWriter out, String tooltipText)
			throws IOException {
		if (!StringServices.isEmpty(tooltipText)) {
			new PlainToolTip(tooltipText, NO_CAPTION).writeAttribute(context, out);
		}
	}
	
    /**
	 * Adds a HTML tool-tip to the currently open tag.
	 * 
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The writer to be used for writing the attributes to.
	 * @param tooltipHtml
	 *        The HTML source code to display as tool-tip text.
	 * @param captionHtml
	 *        The HTML source code to display as tool-tip caption.
	 */
	public void writeTooltipAttributes(DisplayContext context, TagWriter out, String tooltipHtml, String captionHtml)
			throws IOException {
		if (!StringServices.isEmpty(tooltipHtml)) {
			new HtmlToolTip(tooltipHtml, captionHtml).writeAttribute(context, out);
		}
    }

	/**
	 * Adds a text tool-tip to the currently open tag.
	 * 
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The writer to be used for writing the attributes to.
	 * @param tooltipText
	 *        The plain text to display as tool-tip text.
	 * @param captionText
	 *        The plain text to display as tool-tip caption.
	 */
	public void writeTooltipAttributesPlain(DisplayContext context, TagWriter out, String tooltipText,
			String captionText) throws IOException {
		if (!StringServices.isEmpty(tooltipText)) {
			new PlainToolTip(tooltipText, captionText).writeAttribute(context, out);
		}
	}

	/**
	 * Writes a tool-tip attribute.
	 * 
	 * <p>
	 * The tool-tip displays the given content optionally prefixed with the given caption.
	 * </p>
	 *
	 * @param context
	 *        The current {@link DisplayContext} for rendering.
	 * @param out
	 *        The current {@link TagWriter} output to create an attribute in.
	 * @param content
	 *        The tool-tip content. If it is <code>null</code>, no tool-tip is produced.
	 * 
	 * @see #writeTooltipAttributes(DisplayContext, TagWriter, HTMLFragment, HTMLFragment)
	 */
	public void writeTooltipAttributes(DisplayContext context, TagWriter out, HTMLFragment content) throws IOException {
		writeTooltipAttributes(context, out, content, null);
	}

	/**
	 * Writes a tool-tip attribute.
	 * 
	 * <p>
	 * The tool-tip displays the given content optionally prefixed with the given caption.
	 * </p>
	 *
	 * @param context
	 *        The current {@link DisplayContext} for rendering.
	 * @param out
	 *        The current {@link TagWriter} output to create an attribute in.
	 * @param content
	 *        The tool-tip content. If it is <code>null</code>, no tool-tip is produced.
	 * @param caption
	 *        The optional tool-tip caption. If it is <code>null</code>, only the content is
	 *        displayed.
	 */
	public void writeTooltipAttributes(DisplayContext context, TagWriter out, HTMLFragment content,
			HTMLFragment caption) throws IOException {
		if (content != null) {
			new DefaultToolTip(content, caption).writeAttribute(context, out);
		}
	}

	/**
	 * Writes tooltip for a {@link HTMLTemplateFragment}, but without the attribute's name, just the
	 * content of the tooltip.
	 * 
	 * @param context
	 *        In current use cases it's a default one
	 *        {@link DefaultDisplayContext#getDisplayContext()}.
	 * @param out
	 *        The writer to be used for writing the tooltip.
	 * @param tooltipHtml
	 *        The HTML source code to display as tool-tip text.
	 * @param captionHtml
	 *        The HTML source code to display as tool-tip caption.
	 */
	public void writeTooltip(DisplayContext context, TagWriter out, String tooltipHtml, String captionHtml)
			throws IOException {
		if (!StringServices.isEmpty(tooltipHtml)) {
			new HtmlToolTip(tooltipHtml, captionHtml).write(context, out);
		}
	}

	/**
	 * Chooses the template to use for the tool-tip contents.
	 */
	public static HTMLTemplateFragment template(boolean hasCaption) {
		return (hasCaption ? Icons.TOOLTIP_WITH_CAPTION : Icons.TOOLTIP_WITHOUT_CAPTION).get();
	}

}
