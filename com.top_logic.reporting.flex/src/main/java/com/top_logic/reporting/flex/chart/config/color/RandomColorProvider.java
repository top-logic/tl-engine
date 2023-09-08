/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link ColorProvider} that provides random colors.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class RandomColorProvider implements ColorProvider {

	@Override
	public ColorContext createColorContext() {
		return new ColorContext() {
			Map<Object, Color> _colors = new HashMap<>();

			@Override
			public Paint getColor(Object group) {
				Color res = _colors.get(group);
				if (res == null) {
					res = getRandomColor();
					_colors.put(group, res);
				}
				return res;
			}

		};
	}

	/**
	 * a new random color with alpha 1.
	 */
	public static Color getRandomColor() {
		return new Color((int) (Math.random() * 0x1000000));
	}

}
