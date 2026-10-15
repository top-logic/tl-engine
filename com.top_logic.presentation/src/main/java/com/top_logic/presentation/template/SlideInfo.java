/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

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
	 * The data representation with the keys {@link TemplateData#INDEX}, {@link TemplateData#LAYOUT}
	 * (with {@link TemplateData#INDEX} and {@link TemplateData#NAME}),
	 * {@link TemplateData#BACKGROUND} and {@link TemplateData#SHAPES}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		result.put(TemplateData.INDEX, Double.valueOf(index));
		Map<String, Object> layout = TemplateData.map();
		layout.put(TemplateData.INDEX, Double.valueOf(layoutIndex));
		TemplateData.put(layout, TemplateData.NAME, layoutName);
		result.put(TemplateData.LAYOUT, layout);
		TemplateData.put(result, TemplateData.BACKGROUND, background == null ? null : background.toData());
		result.put(TemplateData.SHAPES, TemplateData.list(shapes, ShapeInfo::toData));
		return result;
	}

}
