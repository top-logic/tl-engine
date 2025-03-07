/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.param.ImageAlign;
import com.top_logic.graphic.flow.param.ImageScale;

/**
 * Embeds another image.
 */
public class Image extends AbstractDrawElement {

	private double _imgWidth;

	private double _imgHeight;

	private String _href;

	private ImageAlign _align = ImageAlign.xMidYMid;

	private ImageScale _scale = ImageScale.meet;

	/**
	 * Creates a {@link Image}.
	 *
	 */
	public Image(double width, double height, String href) {
		_imgWidth = width;
		_imgHeight = height;
		_href = href;
	}

	/**
	 * The URL to load the image from.
	 */
	public String getHref() {
		return _href;
	}

	/**
	 * The alignment of the image within its available space.
	 */
	public ImageAlign getAlign() {
		return _align;
	}

	/**
	 * @see #getAlign()
	 */
	public Image setAlign(ImageAlign align) {
		_align = align;
		return this;
	}

	/**
	 * The scaling strategy, if image size and available space does not match.
	 */
	public ImageScale getScale() {
		return _scale;
	}

	/**
	 * @see #getScale()
	 */
	public ImageScale setScale(ImageScale scale) {
		_scale = scale;
		return getScale();
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setWidth(_imgWidth);
		setHeight(_imgHeight);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void draw(SvgWriter out) {
		out.image(getX(), getY(), getWidth(), getHeight(), getHref(), getAlign(), getScale());
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
