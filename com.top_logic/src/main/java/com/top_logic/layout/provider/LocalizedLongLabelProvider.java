/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * Renderer which returns the long representation of the given number.
 *
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class LocalizedLongLabelProvider implements LabelProvider {

    public static final LocalizedLongLabelProvider INSTANCE = new LocalizedLongLabelProvider();

    /**
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object aNumber) {
		return HTMLFormatter.getInstance().formatLong(((Number) aNumber).longValue());
	}
}
