/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * A sample slide of a presentation template.
 *
 * @param index
 *        The position of the slide in the presentation, starting with 0.
 * @param layoutIndex
 *        The {@link LayoutInfo#index()} of the layout the slide is based on.
 * @param layoutName
 *        The {@link LayoutInfo#name()} of the layout the slide is based on.
 * @param background
 *        The background defined by the slide itself, <code>null</code> if the slide uses the
 *        background of its layout.
 * @param shapes
 *        The shapes on the slide.
 */
public record SlideInfo(int index, int layoutIndex, String layoutName, PaintInfo background,
		List<ShapeInfo> shapes) {

	/**
	 * The data representation with the keys {@link PresentationData#INDEX}, {@link PresentationData#LAYOUT}
	 * (with {@link PresentationData#INDEX} and {@link PresentationData#NAME}),
	 * {@link PresentationData#BACKGROUND} and {@link PresentationData#SHAPES}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.INDEX, Double.valueOf(index));
		Map<String, Object> layout = PresentationData.map();
		layout.put(PresentationData.INDEX, Double.valueOf(layoutIndex));
		PresentationData.put(layout, PresentationData.NAME, layoutName);
		result.put(PresentationData.LAYOUT, layout);
		PresentationData.put(result, PresentationData.BACKGROUND, background == null ? null : background.toData());
		result.put(PresentationData.SHAPES, PresentationData.list(shapes, ShapeInfo::toData));
		return result;
	}

}
