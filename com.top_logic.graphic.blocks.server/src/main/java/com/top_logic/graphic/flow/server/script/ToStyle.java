/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import com.top_logic.graphic.blocks.svg.SVGColor;
import com.top_logic.graphic.blocks.svg.SvgUtil;
import com.top_logic.model.search.expr.ToString;

/**
 * Converts {@link SVGColor} values to HTML color strings.
 */
public class ToStyle implements ValueConverter {

	@Override
	public Object fromScript(Object javaValue) {
		if (javaValue instanceof SVGColor color) {
			return SvgUtil.html(color);
		}
		return ToString.toString(javaValue);
	}

}
