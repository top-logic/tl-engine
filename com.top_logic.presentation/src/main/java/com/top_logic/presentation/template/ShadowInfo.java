/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

/**
 * The outer shadow of a shape in a presentation template.
 *
 * @param color
 *        The shadow color, its transparency given by {@link ColorInfo#ALPHA}.
 * @param blur
 *        The blur radius in points.
 * @param distance
 *        The distance of the shadow from the shape in points.
 * @param direction
 *        The direction of the shadow in degrees, clockwise with 0 pointing to the right (90 casts
 *        the shadow downwards).
 */
public record ShadowInfo(ColorInfo color, double blur, double distance, double direction) {

	/**
	 * The data representation with the keys {@link TemplateData#COLOR}, {@link TemplateData#BLUR},
	 * {@link TemplateData#DISTANCE} and {@link TemplateData#DIRECTION}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		TemplateData.put(result, TemplateData.COLOR, color == null ? null : color.toData());
		result.put(TemplateData.BLUR, TemplateData.number(blur));
		result.put(TemplateData.DISTANCE, TemplateData.number(distance));
		result.put(TemplateData.DIRECTION, TemplateData.number(direction));
		return result;
	}

}
