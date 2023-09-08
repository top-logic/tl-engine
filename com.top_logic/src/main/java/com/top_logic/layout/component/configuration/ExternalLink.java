/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImageView;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * {@link HTMLFragment} rendering a link to an external system (e.g. http://www.top-logic.com) with
 * a content view, e.g. an image ({@link ThemeImageView}) or text ({@link ResourceText}).
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ExternalLink implements HTMLFragment, Link {

	private static final ResKey DEFAULT_TOOLTIP = ResKey.NONE;

	private final String _link;

	private ThemeImageView _image;

	private ResKey _label;

	private ResKey _tooltip = DEFAULT_TOOLTIP;

	private String _cssClass;

	/**
	 * Creates a new {@link ExternalLink}.
	 * 
	 * @param link
	 *        The link to open when activating link.
	 */
	public ExternalLink(String link) {
		_link = link;
	}

	/**
	 * The target link.
	 */
	public String getLink() {
		return _link;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.ANCHOR);
		out.writeAttribute(HTMLConstants.HREF_ATTR, _link);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, _cssClass);
		out.writeAttribute(HTMLConstants.TARGET_ATTR, HTMLConstants.BLANK_VALUE);
		if (_image != null) {
			if (_tooltip != null) {
				if (_tooltip != DEFAULT_TOOLTIP) {
					tooltip(context, out, _tooltip);
				} else if (_label != null) {
					// Use label as tooltip for image link
					tooltip(context, out, _label);
				} else {
					// Use link as tooltip when no lablel is given
					tooltip(context, out, ResKey.text(_link));
				}
			}
			out.endBeginTag();
			_image.write(context, out);
		} else if (_label != null) {
			if (_tooltip != null) {
				if (_tooltip != DEFAULT_TOOLTIP) {
					tooltip(context, out, _tooltip);
				} else {
					// Use link as tooltip for labeled link
					tooltip(context, out, ResKey.text(_link));
				}
			}
			out.endBeginTag();
			out.writeText(eval(context, _label));
		} else {
			out.endBeginTag();
			out.writeText(_link);
		}
		out.endTag(HTMLConstants.ANCHOR);
	}

	private void tooltip(DisplayContext context, TagWriter out, ResKey tooltip) throws IOException {
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, eval(context, tooltip));
	}

	private String eval(DisplayContext context, ResKey resKey) {
		return context.getResources().getString(resKey);
	}

	/**
	 * The image to render for the link, or <code>null</code> if no image was set.
	 */
	@Override
	public ThemeImage getImage() {
		return _image != null ? _image.getImage() : null;
	}

	/**
	 * Sets the image to render for the link.
	 * 
	 * @param image
	 *        The displayed image. May be <code>null</code>.
	 */
	public void setImage(ThemeImage image) {
		if (image == null) {
			_image = null;
		} else {
			_image = new ThemeImageView(image, _label);
		}
	}

	/**
	 * The label of the link.
	 */
	@Override
	public String getLabel() {
		return Resources.getInstance().getString(_label);
	}

	/**
	 * Sets the label for the link.
	 * 
	 * @param key
	 *        The key for the label text. May be <code>null</code>.
	 */
	public void setLabel(ResKey key) {
		_label = key;
		if (_image != null) {
			_image = new ThemeImageView(_image.getImage(), _label);
		}
	}
	
	/**
	 * The tooltip to display, when {@link #setImage(ThemeImage) image} or
	 * {@link #setLabel(ResKey) label} is set.
	 */
	@Override
	public String getTooltip() {
		return Resources.getInstance().getString(_tooltip);
	}

	/**
	 * Sets the tooltip to display, when {@link #setImage(ThemeImage) image} or
	 * {@link #setLabel(ResKey) label} is set.
	 * 
	 * @param tooltip
	 *        The key for the tooltip text. May be <code>null</code>.
	 */
	public void setTooltip(ResKey tooltip) {
		_tooltip = tooltip;
	}

	/**
	 * The CSS class for the link.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * Sets the CSS class for the link.
	 * 
	 * @param cssClass
	 *        The CSS class for the link. May be <code>null</code>.
	 */
	public void setCSSClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public String getAltText() {
		return getLabel();
	}

	@Override
	public String getTooltipCaption() {
		return null;
	}

	@Override
	public void writeCssClassesContent(Appendable out) throws IOException {
		out.append(getCssClass());
	}

	@Override
	public String getOnclick() {
		StringBuilder out = new StringBuilder();
		// Note: As stated in the documentation of this hook: No element-specific attributes e.g.
		// href must be created, since the context decides in which element the button is rendered.
		out.append("window.open(");
		TagUtil.writeJsString(out, getLink());
		out.append(", ");
		TagUtil.writeJsString(out,
			DefaultDisplayContext.getDisplayContext().getExecutionScope().getFrameScope().createNewID());
		out.append("); return false;");
		return out.toString();
	}

}
