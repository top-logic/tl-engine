/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOError;
import java.io.IOException;

/**
 * Base class for {@link DisplayValue} implementations that construct their value incrementally.
 * 
 * @see SimpleDisplayValue For trivial cases that do not require string concatenation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDisplayValue implements DisplayValue {

	@Override
	public String get(DisplayContext context) {
		StringBuilder out = new StringBuilder();
		try {
			append(context, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return out.toString();
	}

}
