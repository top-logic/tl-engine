/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider} implementation that forwards to
 * {@link HTMLFormatter#formatObject(Object)}.
 * 
 * @see DefaultLabelProvider for a {@link LabelProvider} that is backed by the
 *      value's {@link Object#toString()} method.
 * 
 * @see LocalizedDateLabelProvider and
 * @see LocalizedNumberLabelProvider for specialized versions for numbers and
 *      dates that do not require the extra dispatch in {@link HTMLFormatter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormattedLabelProvider implements LabelProvider {

	/**
	 * Singleton instance of this class.
	 */
	public static FormattedLabelProvider INSTANCE = 
		new FormattedLabelProvider();
	
	@Override
	public String getLabel(Object object) {
		return HTMLFormatter.formatObject(object);
	}

}
