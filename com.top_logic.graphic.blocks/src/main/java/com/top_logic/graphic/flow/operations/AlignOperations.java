/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * Alignment of the contents.
 */
public interface AlignOperations extends DecorationOperations {

	@Override
	com.top_logic.graphic.flow.data.Align self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		DecorationOperations.super.computeIntrinsicSize(context, offsetX, offsetY);

		BoxOperations content = self().getContent();
		self().setWidth(content.self().getWidth());
		self().setHeight(content.self().getHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		double additionalWidth = width - self().getWidth();
		double additionalHeight = height - self().getHeight();

		double top, left, bottom, right;
		switch (self().getXAlign()) {
			case START: {
				left = 0;
				right = additionalWidth;
				break;
			}
			case MIDDLE: {
				left = additionalWidth / 2;
				right = additionalWidth / 2;
				break;
			}
			case STOP: {
				left = additionalWidth;
				right = 0;
				break;
			}
			case STRECH: {
				left = 0;
				right = 0;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + self().getXAlign());
		}
		switch (self().getYAlign()) {
			case START: {
				top = 0;
				bottom = additionalHeight;
				break;
			}
			case MIDDLE: {
				top = additionalHeight / 2;
				bottom = additionalHeight / 2;
				break;
			}
			case STOP: {
				top = additionalHeight;
				bottom = 0;
				break;
			}
			case STRECH: {
				top = 0;
				bottom = 0;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + self().getYAlign());
		}

		self().getContent().distributeSize(context, offsetX + left, offsetY + top, width - left - right,
			height - top - bottom);

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

}
