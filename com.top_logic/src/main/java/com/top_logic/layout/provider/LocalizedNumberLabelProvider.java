/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider} for {@link Number}s that uses {@link Formatter#formatNumber(Number)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocalizedNumberLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		return HTMLFormatter.getInstance().formatNumber((Number) object);
	}

}
