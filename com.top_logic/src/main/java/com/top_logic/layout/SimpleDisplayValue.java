/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;


/**
 * {@link DisplayValue} that first constructs its value before rendering.
 * 
 * <p>
 * Note: This base class should only be used, if implementing {@link #get(DisplayContext)} does not
 * require object allocation, e.g. string concatenation.
 * </p>
 * 
 * @see AbstractDisplayValue Efficiently implementing {@link DisplayValue} in cases, where string
 *      concatenation would be required.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SimpleDisplayValue implements DisplayValue {

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		out.append(get(context));
	}

}
