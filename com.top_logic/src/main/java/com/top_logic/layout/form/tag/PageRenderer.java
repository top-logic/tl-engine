/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import static com.top_logic.mig.html.HTMLConstants.*;
import static com.top_logic.mig.html.HTMLUtil.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.SaveScrollPosition;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Rendering algorithm for writing a page with header/title and body area.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PageRenderer {

	/**
	 * CSS class of the outermost div.
	 */
	protected static final String PAGE_CSS_CLASS = "fptPage";

	/**
	 * CSS class of the header div.
	 */
	protected static final String HEADER_CSS_CLASS = "fptHeader";

	/**
	 * CSS class of the title div.
	 */
	protected static final String TITLE_CSS_CLASS = "fptTitle";

	/**
	 * CSS class of the title content div.
	 */
	protected static final String TITLE_CONTENT_CSS_CLASS = "fptTitleContent";

	/**
	 * CSS class of the subtitle div.
	 */
	protected static final String SUBTITLE_CSS_CLASS = "fptSubtitle";

	/**
	 * CSS class of the subtitle content div.
	 */
	protected static final String SUBTITLE_CONTENT_CSS_CLASS = "fptSubtitleContent";

	/**
	 * CSS class of the icon bar div.
	 */
	protected static final String ICONBAR_CSS_CLASS = "fptIconBar";

	/**
	 * CSS class of the body div.
	 */
	protected static final String BODY_CSS_CLASS = "fptBody";

	/**
	 * CSS class of the body content div.
	 */
	protected static final String BODY_CONTENT_CSS_CLASS = "fptBodyContent";

	/**
	 * Writes the complete page consisting of header and body.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeHeader(DisplayContext, TagWriter, PageControl)
	 * @see #writeBody(DisplayContext, TagWriter, PageControl)
	 */
	public final void writePage(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginPage(out);
		{
			writeHeader(context, out, pageControl);
			writeBody(context, out, pageControl);
		}
		endPage(out);
	}

	/**
	 * Writes the complete header consisting of title, subtitle and icon bar.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeTitle(DisplayContext, TagWriter, PageControl)
	 * @see #writeSubTitle(DisplayContext, TagWriter, PageControl)
	 * @see #writeIconBar(DisplayContext, TagWriter, PageControl)
	 */
	public final void writeHeader(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginHeader(out);
		{
			writeTitle(context, out, pageControl);
			writeSubTitle(context, out, pageControl);
			writeIconBar(context, out, pageControl);
		}
		endHeader(out);
	}

	/**
	 * Writes the complete page body.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeBodyContent(DisplayContext, TagWriter, PageControl)
	 */
	public final void writeBody(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginBody(out, pageControl.getID(), false);
		{
			writeBodyContent(context, out, pageControl);
		}
		endBody(out);
	}

	/**
	 * Writes the complete page title.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeTitleContent(DisplayContext, TagWriter, PageControl)
	 */
	public final void writeTitle(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginTitle(out);
		{
			writeTitleContent(context, out, pageControl);
		}
		endTitle(out);
	}

	/**
	 * Writes the complete page subtitle.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeSubtitleContent(DisplayContext, TagWriter, PageControl)
	 */
	public final void writeSubTitle(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginSubTitle(out);
		{
			writeSubtitleContent(context, out, pageControl);
		}
		endSubTitle(out);
	}

	/**
	 * Writes the complete icon bar.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeIconBarContent(DisplayContext, TagWriter, PageControl)
	 */
	public final void writeIconBar(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		beginIconBar(out);
		{
			writeIconBarContent(context, out, pageControl);
		}
		endIconBar(out);
	}

	/**
	 * Hook for writing the contents of the title area.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeTitle(DisplayContext, TagWriter, PageControl)
	 */
	public void writeTitleContent(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		pageControl.getTitleContent().write(context, out);
	}

	/**
	 * Hook for writing the contents of the subtitle area.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeSubTitle(DisplayContext, TagWriter, PageControl)
	 */
	public void writeSubtitleContent(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		pageControl.getSubtitleContent().write(context, out);
	}

	/**
	 * Hook for writing the contents of the icon bar area.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeIconBar(DisplayContext, TagWriter, PageControl)
	 */
	public void writeIconBarContent(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		pageControl.getIconBarContent().write(context, out);
	}

	/**
	 * Hook for writing the contents of the body area.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param pageControl
	 *        The {@link PageControl} being written.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeBody(DisplayContext, TagWriter, PageControl)
	 */
	public void writeBodyContent(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		pageControl.getBodyContent().write(context, out);
	}

	/**
	 * Starts writing the complete page.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeHeader(DisplayContext, TagWriter, PageControl)
	 * @see #writeBody(DisplayContext, TagWriter, PageControl)
	 * @see #endPage(TagWriter)
	 */
	public void beginPage(TagWriter out) throws IOException {
		beginDiv(out, PAGE_CSS_CLASS);
	}

	/**
	 * Starts writing the page header.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * 
	 * @see #writeTitle(DisplayContext, TagWriter, PageControl)
	 * @see #writeSubTitle(DisplayContext, TagWriter, PageControl)
	 * @see #writeIconBar(DisplayContext, TagWriter, PageControl)
	 * @see #endHeader(TagWriter)
	 */
	public void beginHeader(TagWriter out) throws IOException {
		beginDiv(out, HEADER_CSS_CLASS);
	}

	/**
	 * Starts writing the title area of the page header.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * 
	 * @see #writeHeader(DisplayContext, TagWriter, PageControl)
	 * @see #endTitle(TagWriter)
	 */
	public void beginTitle(TagWriter out) throws IOException {
		beginDiv(out, TITLE_CSS_CLASS);
		beginDiv(out, TITLE_CONTENT_CSS_CLASS);
	}

	/**
	 * @see #beginTitle(TagWriter)
	 */
	public void endTitle(TagWriter out) throws IOException {
		endDiv(out);
		endDiv(out);
	}

	/**
	 * Starts writing the subtitle area of the page header.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * 
	 * @see #writeHeader(DisplayContext, TagWriter, PageControl)
	 * @see #endSubTitle(TagWriter)
	 */
	public void beginSubTitle(TagWriter out) throws IOException {
		beginDiv(out, SUBTITLE_CSS_CLASS);
		beginDiv(out, SUBTITLE_CONTENT_CSS_CLASS);
	}

	/**
	 * @see #beginSubTitle(TagWriter)
	 */
	public void endSubTitle(TagWriter out) throws IOException {
		endDiv(out);
		endDiv(out);
	}

	/**
	 * Starts writing the icon bar area of the page header.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * 
	 * @see #writeHeader(DisplayContext, TagWriter, PageControl)
	 * @see #endIconBar(TagWriter)
	 */
	public void beginIconBar(TagWriter out) throws IOException {
		beginDiv(out, ICONBAR_CSS_CLASS);
	}

	/**
	 * @see #beginIconBar(TagWriter)
	 */
	public void endIconBar(TagWriter out) throws IOException {
		endDiv(out);
	}

	/**
	 * @see #beginHeader(TagWriter)
	 */
	public void endHeader(TagWriter out) throws IOException {
		endDiv(out);
	}

	/**
	 * Starts writing the page body area.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * @param resetScollPosition
	 *        Whether the scroll position shall be reset or restored.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writePage(DisplayContext, TagWriter, PageControl)
	 * @see #endBody(TagWriter)
	 */
	public void beginBody(TagWriter out, String containerId, boolean resetScollPosition) throws IOException {
		beginDiv(out, BODY_CSS_CLASS);

		out.beginBeginTag(DIV);
		writeBodyContentAttributes(out, containerId);
		out.endBeginTag();
		HTMLUtil.beginScriptAfterRendering(out);
		if (resetScollPosition) {
			SaveScrollPosition.writeResetScrollPositionScript(out, getScrollContainerId(containerId));
		} else {
			out.append(SaveScrollPosition.getPositionViewportCommand(getScrollContainerId(containerId)));
		}
		HTMLUtil.endScriptAfterRendering(out);
	}

	/**
	 * Writes the attributes of the {@link #BODY_CONTENT_CSS_CLASS} div.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 */
	protected void writeBodyContentAttributes(TagWriter out, String containerId) throws IOException {
		String scrollContainerId = getScrollContainerId(containerId);
		out.writeAttribute(CLASS_ATTR, BODY_CONTENT_CSS_CLASS + " " + FormConstants.OVERFLOW_AUTO_CLASS);
		out.writeAttribute(ID_ATTR, scrollContainerId);
		out.beginAttribute(ONSCROLL_ATTR);
		SaveScrollPosition.writePushScrollPositionScript(out, scrollContainerId);
		out.endAttribute();
	}

	/** id of scroll container element */
	protected String getScrollContainerId(String containerId) {
		return containerId + "-" + SaveScrollPosition.SCROLL_CONTAINER_ID;
	}

	/**
	 * @see #beginBody(TagWriter, String, boolean)
	 */
	public void endBody(TagWriter out) throws IOException {
		endDiv(out);
		writeAfterBodyContent(out);
		endDiv(out);
	}

	/**
	 * Hook that is called after closing the {@link #BODY_CONTENT_CSS_CLASS} div.
	 * 
	 * @param out
	 *        the {@link TagWriter} to write to.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writeAfterBodyContent(TagWriter out) throws IOException {
		// Hook for subclasses.
	}

	/**
	 * @see #beginPage(TagWriter)
	 */
	public void endPage(TagWriter out) throws IOException {
		endDiv(out);
	}

	/**
	 * The {@link PageRenderer} for the current theme.
	 */
	public static PageRenderer getThemeInstance() {
		return getThemeInstance(ThemeFactory.getTheme());
	}

	/**
	 * The {@link PageRenderer} for the given theme.
	 */
	public static PageRenderer getThemeInstance(Theme theme) {
		return theme.getValue(Icons.PAGE_RENDERER);
	}

}
