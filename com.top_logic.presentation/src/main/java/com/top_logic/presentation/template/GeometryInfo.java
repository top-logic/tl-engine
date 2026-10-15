/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

/**
 * The outline geometry of a shape in a presentation template.
 *
 * @param preset
 *        The name of the preset geometry (e.g. <code>rect</code>, <code>roundRect</code>,
 *        <code>line</code>), <code>null</code> for a custom geometry.
 * @param adjust
 *        The adjust values of the preset geometry by their guide names (e.g. <code>adj</code>), as
 *        given in the file (100000 for 100%).
 * @param cornerRadius
 *        The corner radius in points of a <code>roundRect</code>, <code>null</code> for other
 *        geometries.
 */
public record GeometryInfo(String preset, Map<String, Double> adjust, Double cornerRadius) {

	/**
	 * The data representation with the keys {@link TemplateData#PRESET},
	 * {@link TemplateData#ADJUST} and {@link TemplateData#CORNER_RADIUS}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		TemplateData.put(result, TemplateData.PRESET, preset);
		if (!adjust.isEmpty()) {
			Map<String, Object> values = TemplateData.map();
			values.putAll(adjust);
			result.put(TemplateData.ADJUST, values);
		}
		TemplateData.put(result, TemplateData.CORNER_RADIUS,
			cornerRadius == null ? null : TemplateData.number(cornerRadius));
		return result;
	}

}
