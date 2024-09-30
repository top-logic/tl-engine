/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * {@link Tag} for writing image elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImageTag extends AbstractTagBase {

	/** @see #getIcon() */
	private ThemeImage _icon;

	/** @see #getWidth() */
	private String _width;

	/** @see #getHeight() */
	private String _height;

	/** @see #getAlt() */
	private String _alt;

	/** @see #setAltKey(Object) */
	private ResKey _altKey;

	/** @see #getTooltipCaption() */
	private ResKey _tooltipCaption;

	/** @see #setTooltipKey(Object) */
	private ResKey _tooltip;

	/** @see #getBorder() */
	private String _border;

	/** @see #getCssClass() */
	private String _cssClass;

	/**
	 * Creates a {@link ImageTag}.
	 */
	public ImageTag() {
		super();
	}

	/**
	 * The source of the image relative to the application's <code>images</code> directory.
	 * 
	 * <p>
	 * Only one of {@link #setSrc(String)} or {@link #setSrcKey(String)} may be set.
	 * </p>
	 */
	public ThemeImage getIcon() {
		return _icon;
	}

	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 * 
	 * @see #setSrcKey(String)
	 */
	@Deprecated
	public void setSrc(String src) {
		setSrcKey("/images" + src);
	}

	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 * 
	 * @see #setSrc(String)
	 */
	@CalledFromJSP
	@Deprecated
	public void setSrcKey(String srcKey) {
		setIcon(ThemeImage.icon(srcKey));
	}

	/**
	 * Sets the image to render.
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_icon = icon;
	}

	/**
	 * The width of the image (dimension including unit).
	 */
	public String getWidth() {
		return _width;
	}

	/**
	 * @see #getWidth()
	 */
	public void setWidth(String width) {
		_width = width;
	}

	/**
	 * The resource key (given as {@link ResKey} or string) for the {@link HTMLConstants#ALT_ATTR}.
	 * 
	 * <p>
	 * Only one of {@link #setAlt(String)} or {@link #setAltKey(Object)} may be set.
	 * </p>
	 * 
	 * @see #setAlt(String)
	 */
	public void setAltKey(Object altKey) {
		ResKey key;
		if (altKey instanceof String) {
			key = ResKey.internalJsp((String) altKey);
		} else {
			key = (ResKey) altKey;
		}
		internalSetAltKey(key);
	}

	private void internalSetAltKey(ResKey altKey) {
		_altKey = altKey;
	}

	/**
	 * The alternate text displayed on restricted devices or if the image is not available.
	 * 
	 * <p>
	 * Only one of {@link #getAlt()} or {@link #setAltKey(Object)} may be set.
	 * </p>
	 * 
	 * @see #setAlt(String)
	 */
	public String getAlt() {
		return (_alt == null) ? "" : _alt;
	}

	/**
	 * @see #getAlt()
	 */
	public void setAlt(String alt) {
		_alt = alt;
	}

	/**
	 * Tooltip contents to be displayed on the image.
	 * 
	 * <p>
	 * Only one of {@link #setTooltip(ResKey)} or {@link #setTooltipKey(Object)} must be specified.
	 * </p>
	 * 
	 * @see #setTooltipKey(Object)
	 */
	public ResKey getTooltip() {
		return _tooltip;
	}

	/**
	 * @see #setTooltip(ResKey)
	 */
	public void setTooltip(ResKey tooltip) {
		_tooltip = tooltip;
	}

	/**
	 * The resource key to generate the image tooltip from.
	 * 
	 * <p>
	 * The tooltip title is generated from an optional derived resource with the <code>.title</code>
	 * suffix.
	 * </p>
	 * 
	 * <p>
	 * Only one of {@link #setTooltip(ResKey)} or {@link #setTooltipKey(Object)} must be specified.
	 * </p>
	 * 
	 * @see #setTooltip(ResKey)
	 */
	public void setTooltipKey(Object tooltipKey) {
		ResKey key;
		if (tooltipKey instanceof String) {
			key = ResKey.internalJsp((String) tooltipKey);
		} else {
			key = (ResKey) tooltipKey;
		}
		internalSetTooltipKey(key);
	}

	private void internalSetTooltipKey(ResKey tooltipKey) {
		_tooltip = tooltipKey;
	}

	/**
	 * The {@link #getTooltip()} caption text.
	 * 
	 * <p>
	 * Must only be specified, if {@link #getTooltip()} is given.
	 * </p>
	 */
	public ResKey getTooltipCaption() {
		return _tooltipCaption;
	}

	/**
	 * @see #getTooltipCaption()
	 */
	public void setTooltipCaption(ResKey tooltipCaption) {
		_tooltipCaption = tooltipCaption;
	}

	/**
	 * The height of the image (dimension including unit).
	 */
	public String getHeight() {
		return _height;
	}

	/**
	 * @see #getHeight()
	 */
	public void setHeight(String height) {
		_height = height;
	}

	/**
	 * The size of the border of the image.
	 */
	public String getBorder() {
		return _border;
	}

	/**
	 * @see #getBorder()
	 */
	public void setBorder(String border) {
		_border = border;
	}

	/**
	 * CSS class string for the image tag.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		DisplayContext context = getDisplayContext();
		TagWriter out = getOut();

		XMLTag icon = getIcon().toIcon();
		icon.beginBeginTag(context, out);

		ResKey altKey = _altKey;
		if (altKey == null) {
			out.writeAttribute(ALT_ATTR, getAlt());
		} else {
			out.writeAttribute(ALT_ATTR, context.getResources().getString(altKey));
		}

		String cssClass = getCssClass();
		if (!StringServices.isEmpty(cssClass)) {
			out.writeAttribute(CLASS_ATTR, cssClass);
		}

		String border = getBorder();
		if (!StringServices.isEmpty(border)) {
			out.writeAttribute(BORDER_ATTR, border);
		}

		String width = getWidth();
		if (!StringServices.isEmpty(width)) {
			out.writeAttribute(WIDTH_ATTR, width);
		}

		String height = getHeight();
		if (!StringServices.isEmpty(height)) {
			out.writeAttribute(HEIGHT_ATTR, height);
		}

		writeTooltipAttribute(context, out);

		icon.endEmptyTag(context, out);

		return SKIP_BODY;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}

	private void writeTooltipAttribute(DisplayContext context, TagWriter out) throws IOException {
		ResKey tooltip;
		ResKey tooltipCaption;
		ResKey tooltipKey = _tooltip;
		if (tooltipKey != null) {
			tooltip = tooltipKey;
			tooltipCaption = Resources.derivedKey(tooltipKey, ".title").optional();
		} else {
			tooltip = getTooltip();
			tooltipCaption = getTooltipCaption();
		}
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, tooltipCaption);
		}
	}

	@Override
	protected void teardown() {
		super.teardown();

		_icon = null;
		_width = null;
		_height = null;
		_alt = null;
		_altKey = null;
		_tooltip = null;
		_tooltipCaption = null;
		_tooltip = null;
		_border = null;
		_cssClass = null;
	}

}
