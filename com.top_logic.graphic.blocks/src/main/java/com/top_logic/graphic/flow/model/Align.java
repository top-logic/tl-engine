/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.param.HAlign;
import com.top_logic.graphic.flow.param.VAlign;

/**
 * Alignment of the contents.
 */
public class Align extends Decoration {

	private HAlign _hAlign = HAlign.CENTER;

	private VAlign _vAlign = VAlign.CENTER;

	/**
	 * The horizontal alignment.
	 */
	public HAlign getHAlign() {
		return _hAlign;
	}

	/**
	 * @see #getHAlign()
	 */
	public Align setHAlign(HAlign hAlign) {
		_hAlign = hAlign;
		return this;
	}

	/**
	 * The vertical alignment.
	 */
	public VAlign getVAlign() {
		return _vAlign;
	}

	/**
	 * @see #getVAlign()
	 */
	public Align setVAlign(VAlign vAlign) {
		_vAlign = vAlign;
		return this;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setX(offsetX);
		setY(offsetY);

		super.computeIntrinsicSize(context, offsetX, offsetY);

		DrawElement content = getContent();
		setWidth(content.getWidth());
		setHeight(content.getHeight());
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		double additionalWidth = width - getWidth();
		double additionalHeight = height - getHeight();

		double top, left, bottom, right;
		switch (_hAlign) {
			case LEFT: {
				left = 0;
				right = additionalWidth;
				break;
			}
			case CENTER: {
				left = additionalWidth / 2;
				right = additionalWidth / 2;
				break;
			}
			case RIGHT: {
				left = additionalWidth;
				right = 0;
				break;
			}
			case STRETCH: {
				left = 0;
				right = 0;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + _hAlign);
		}
		switch (_vAlign) {
			case TOP: {
				top = 0;
				bottom = additionalHeight;
				break;
			}
			case CENTER: {
				top = additionalHeight / 2;
				bottom = additionalHeight / 2;
				break;
			}
			case BOTTOM: {
				top = additionalHeight;
				bottom = 0;
				break;
			}
			case STRETCH: {
				top = 0;
				bottom = 0;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + _hAlign);
		}

		getContent().distributeSize(context, offsetX + left, offsetY + top, width - left - right,
			height - top - bottom);

		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
