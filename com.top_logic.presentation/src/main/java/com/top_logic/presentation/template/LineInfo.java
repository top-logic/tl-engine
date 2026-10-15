/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

/**
 * The outline of a shape in a presentation template.
 *
 * @param paint
 *        The fill of the line.
 * @param width
 *        The line width in points, <code>null</code> if not specified.
 * @param dash
 *        The preset dash style (e.g. <code>solid</code>, <code>dash</code>, <code>sysDot</code>),
 *        <code>null</code> if not specified.
 */
public record LineInfo(PaintInfo paint, Double width, String dash) {

	/**
	 * The data representation: the entries of the {@link #paint()} plus the keys
	 * {@link TemplateData#WIDTH} and {@link TemplateData#DASH}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = paint.toData();
		TemplateData.put(result, TemplateData.WIDTH, width == null ? null : TemplateData.number(width));
		TemplateData.put(result, TemplateData.DASH, dash);
		return result;
	}

}
