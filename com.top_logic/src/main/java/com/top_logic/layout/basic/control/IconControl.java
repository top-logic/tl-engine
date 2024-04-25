/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AbstractXMLTag;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link Control} rendering a simple image element with dynamically adjustable properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconControl extends AbstractControlBase {

	private XMLTag _src;

	private String _altText = "";

	private String _cssWidth;

	private String _cssHeight;

	private String _cssClass;

	private String _tooltipCaption;

	private String _tooltip;

	/**
	 * Creates a {@link IconControl}.
	 */
	public IconControl() {
		super();
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Contents of the {@link HTMLConstants#SRC_ATTR} attribute.
	 * 
	 * <p>
	 * <code>null</code> for an invisible icon.
	 * </p>
	 */
	public XMLTag getSrc() {
		return _src;
	}

	/**
	 * @see #getSrc()
	 */
	public void setSrc(XMLTag src) {
		_src = src;
		requestRepaint();
	}

	/**
	 * Convenience method for creating an icon with an application local URL (not from a
	 * {@link Theme}).
	 */
	public void setSrcPath(String fullImagePath) {
		setSrc(new LocalURL(fullImagePath));
	}

	/**
	 * Convenience method for creating an icon from a {@link Theme} image key.
	 */
	public void setSrcKey(ThemeImage src) {
		if (src == null) {
			setSrc(null);
		} else {
			setSrc(src.toIcon());
		}
	}

	/**
	 * The CSS class of the icon, <code>null</code> for no CSS class.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
		requestRepaint();
	}

	/**
	 * The value of the {@link HTMLConstants#WIDTH_ATTR} attribute of the icon, <code>null</code>
	 * for no explicit width.
	 */
	public String getCssWidth() {
		return _cssWidth;
	}

	/**
	 * @see #getCssWidth()
	 */
	public void setWidth(String cssWidth) {
		_cssWidth = cssWidth;
		requestRepaint();
	}

	/**
	 * The value of the {@link HTMLConstants#HEIGHT_ATTR} attribute of the icon, <code>null</code>
	 * for no explicit height.
	 */
	public String getCssHeight() {
		return _cssHeight;
	}

	/**
	 * @see #getCssHeight()
	 */
	public void setHeight(String cssHeight) {
		_cssHeight = cssHeight;
		requestRepaint();
	}

	/**
	 * The alternative text of the icon when displayed on a limited device.
	 */
	public String getAltText() {
		return _altText;
	}

	/**
	 * @see #getAltText()
	 */
	public void setAltText(String altText) {
		_altText = StringServices.nonNull(altText);
		requestRepaint();
	}

	/**
	 * The tool-tip to display over the icon.
	 * 
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributes(DisplayContext, TagWriter,
	 *      String, String)
	 */
	public String getTooltip() {
		return _tooltip;
	}

	/**
	 * @see #getTooltip()
	 */
	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
		requestRepaint();
	}

	/**
	 * The tool-tip caption of the tool-tip to display over the icon.
	 * 
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributes(DisplayContext, TagWriter,
	 *      String, String)
	 */
	public String getTooltipCaption() {
		return _tooltipCaption;
	}

	/**
	 * @see #getTooltipCaption()
	 */
	public void setTooltipCaption(String tooltipCaption) {
		_tooltipCaption = tooltipCaption;
		requestRepaint();
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Noting to incrementally revalidate.
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (_src == null) {
			// Just write an anchor that allows dynamically inserting an image.
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			out.endTag(SPAN);
		} else {
			_src.beginBeginTag(context, out);
			writeControlAttributes(context, out);
			out.writeAttribute(WIDTH_ATTR, _cssWidth);
			out.writeAttribute(HEIGHT_ATTR, _cssHeight);
			out.writeAttribute(ALT_ATTR, _altText);
			out.writeAttribute(TITLE_ATTR, "");

			if (_tooltip != null) {
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, _tooltip,
					_tooltipCaption);
			}
			_src.endBeginTag(context, out);
			_src.endTag(context, out);
		}
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, _cssClass);
	}

	/**
	 * Create an {@link IconControl} displaying theme icon with the given theme key.
	 */
	public static IconControl iconTheme(ThemeImage src) {
		return icon(src);
	}

	/**
	 * Create an {@link IconControl} displaying the given URL.
	 * 
	 * @param src
	 *        The algorithm constructing the URL, typically an instance of {@link ThemeImage}.
	 */
	public static IconControl icon(ThemeImage src) {
		IconControl result = new IconControl();
		result.setSrc(src.toIcon());
		return result;
	}
	
	private class LocalURL extends AbstractXMLTag {

		private final String _path;

		public LocalURL(String path) {
			_path = path;
		}

		@Override
		public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(IMG);
			out.writeAttribute(SRC_ATTR, _path);
		}

		@Override
		public void endBeginTag(DisplayContext context, TagWriter out) {
			// Ignore, empty tag.
		}

		@Override
		public void endTag(DisplayContext context, TagWriter out) {
			out.endEmptyTag();
		}

	}

}
