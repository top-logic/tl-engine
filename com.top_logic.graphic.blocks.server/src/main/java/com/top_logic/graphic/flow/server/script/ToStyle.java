/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.awt.Color;

import com.top_logic.graphic.svg.SvgUtil;
import com.top_logic.model.search.expr.ToString;

/**
 * Converts {@link Color} values to HTML color strings.
 */
public class ToStyle implements ValueConverter {

	@Override
	public Object fromScript(Object javaValue) {
		if (javaValue instanceof Color color) {
			return SvgUtil.html(color);
		}
		return ToString.toString(javaValue);
	}

}
