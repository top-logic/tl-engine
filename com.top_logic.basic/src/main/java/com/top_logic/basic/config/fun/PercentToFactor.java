/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.fun;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.GenericFunction;

/**
 * {@link GenericFunction} transforming a percentage value to a factor.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PercentToFactor extends Function1<Float, Number> {

	@Override
	public Float apply(Number arg) {
		if (arg == null) {
			return null;
		}
		return Float.valueOf(arg.floatValue() * 0.01f);
	}

}
