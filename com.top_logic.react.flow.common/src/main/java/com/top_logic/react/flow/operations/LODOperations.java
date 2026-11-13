/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.List;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.LOD;
import com.top_logic.react.flow.data.LODVariant;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for the {@link LOD} level-of-detail container.
 *
 * <p>
 * Probes its variants in declared order (richest first) and selects the first one whose
 * intrinsic size fits the available space and that satisfies its optional gates
 * ({@link LODVariant#getMinZoom()}, {@link LODVariant#getMinWidth()},
 * {@link LODVariant#getMinHeight()}). If no variant fits, the last one is used as a fallback.
 * </p>
 *
 * <p>
 * In the measuring mode {@link RenderContext#isWidestVariants()}, no variant is chosen: the
 * intrinsic size is the maximum width and the maximum height over all variants.
 * </p>
 */
public interface LODOperations extends BoxOperations {

	@Override
	LOD self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight) {
		LOD self = self();
		if (context.isWidestVariants()) {
			computeWidestSize(context, offsetX, offsetY, availableWidth, availableHeight);
			return;
		}

		List<LODVariant> variants = self.getVariants();
		double zoom = context.getZoom();

		int chosenIdx = -1;
		Box chosenContent = null;
		for (int i = 0; i < variants.size(); i++) {
			LODVariant v = variants.get(i);
			if (zoom < v.getMinZoom()) {
				continue;
			}
			if (availableWidth < v.getMinWidth()) {
				continue;
			}
			if (availableHeight < v.getMinHeight()) {
				continue;
			}

			Box content = v.getContent();
			if (content == null) {
				continue;
			}
			content.computeIntrinsicSize(context, offsetX, offsetY, availableWidth, availableHeight);
			if (content.getWidth() <= availableWidth && content.getHeight() <= availableHeight) {
				chosenIdx = i;
				chosenContent = content;
				break;
			}
		}

		if (chosenIdx < 0) {
			// Fallback: take the last variant unconditionally.
			int last = variants.size() - 1;
			if (last >= 0) {
				LODVariant fallback = variants.get(last);
				Box content = fallback.getContent();
				if (content != null) {
					content.computeIntrinsicSize(context, offsetX, offsetY,
						availableWidth, availableHeight);
					chosenIdx = last;
					chosenContent = content;
				}
			}
		}

		self.setX(offsetX);
		self.setY(offsetY);
		self.setChosenIndex(chosenIdx);

		double w = chosenContent == null ? 0 : chosenContent.getWidth();
		double h = chosenContent == null ? 0 : chosenContent.getHeight();
		setIntrinsicSize(w, h);
	}

	/**
	 * Measures all variants and reports the maximum width and height over them.
	 *
	 * @see RenderContext#isWidestVariants()
	 */
	private void computeWidestSize(RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight) {
		LOD self = self();
		double w = 0;
		double h = 0;
		for (LODVariant v : self.getVariants()) {
			Box content = v.getContent();
			if (content == null) {
				continue;
			}
			content.computeIntrinsicSize(context, offsetX, offsetY, availableWidth, availableHeight);
			w = Math.max(w, content.getWidth());
			h = Math.max(h, content.getHeight());
		}
		self.setX(offsetX);
		self.setY(offsetY);
		setIntrinsicSize(w, h);
	}

	/**
	 * Sets the size of this box from the given content size, respecting
	 * {@link LOD#getFixedHeight()}.
	 */
	private void setIntrinsicSize(double contentWidth, double contentHeight) {
		LOD self = self();
		self.setWidth(contentWidth);
		if (self.getFixedHeight() > 0) {
			self.setHeight(self.getFixedHeight());
		} else {
			self.setHeight(contentHeight);
		}
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY,
			double width, double height) {
		LOD self = self();
		self.setX(offsetX);
		self.setY(offsetY);
		self.setWidth(width);
		self.setHeight(height);

		Box chosen = chosenContent();
		if (chosen != null) {
			double contentH = self.getFixedHeight() > 0 ? chosen.getHeight() : height;
			chosen.distributeSize(context, offsetX, offsetY, width, contentH);
		}
	}

	@Override
	default void draw(SvgWriter out) {
		Box chosen = chosenContent();
		if (chosen == null) {
			return;
		}
		out.beginGroup();
		out.attachModel(self());
		out.write(chosen);
		out.endGroup();
	}

	/** The content of the currently chosen variant, or {@code null} if none. */
	private Box chosenContent() {
		LOD self = self();
		int idx = self.getChosenIndex();
		List<LODVariant> variants = self.getVariants();
		if (idx < 0 || idx >= variants.size()) {
			return null;
		}
		return variants.get(idx).getContent();
	}

}
