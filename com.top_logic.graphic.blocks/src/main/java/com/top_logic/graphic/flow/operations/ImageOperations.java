/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.math.Mat;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Image;

/**
 * Embeds another image.
 */
public interface ImageOperations extends BoxOperations {

	@Override
	com.top_logic.graphic.flow.data.Image self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setWidth(self().getImgWidth());
		self().setHeight(self().getImgHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		Image self = self();

		out.beginGroup();

		// Apply translation in the outermost group since it conflicts with potential orientation
		// transformation.
		out.translate(self.getX(), self.getY());

		switch (self.getOrientation()) {
			case NORMAL:
				writeImage(out);
				break;
			case FLIP_H: {
				out.beginGroup();
				out.transform(Mat.translate(self.getWidth(), 0).apply(Mat.FLIP_X));
				writeImage(out);
				out.endGroup();
				break;
			}
			case FLIP_V: {
				out.beginGroup();
				out.transform(Mat.translate(0, self.getHeight()).apply(Mat.FLIP_Y));
				writeImage(out);
				out.endGroup();
				break;
			}
			case ROTATE_90: {
				out.beginGroup();
				out.transform(Mat.translate(self.getWidth(), 0).apply(Mat.ROT_90));
				writeImage(out);
				out.endGroup();
				break;
			}
			case ROTATE_180: {
				out.beginGroup();
				out.transform(Mat.translate(self.getWidth(), self.getHeight()).apply(Mat.ROT_180));
				writeImage(out);
				out.endGroup();
				break;
			}
			case FLIP_H_ROTATE_90: {
				out.beginGroup();
				out.transform(Mat.translate(self.getWidth(), self.getHeight()).apply(Mat.FLIP_X_ROT_90));
				writeImage(out);
				out.endGroup();
				break;
			}
			case FLIP_H_ROTATE_270: {
				out.beginGroup();
				out.transform(Mat.translate(0, 0).apply(Mat.FLIP_X_ROT_270));
				writeImage(out);
				out.endGroup();
				break;
			}
			case ROTATE_270: {
				out.beginGroup();
				out.transform(Mat.translate(0, self.getHeight()).apply(Mat.ROT_270));
				writeImage(out);
				out.endGroup();
				break;
			}
		}

		out.endGroup();
	}

	/** private */
	default void writeImage(SvgWriter out) {
		Image self = self();
		out.image(0, 0, self.getWidth(), self.getHeight(), self.getHref(),
			self.getAlign(), self.getScale());
	}

}
