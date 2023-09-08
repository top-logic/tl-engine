/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.text.DecimalFormat;
import java.text.Format;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * Render a {@link Number} by using a user I18Ned {@link DecimalFormat decimal} to a cell.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DefaultDecimalFormat implements LabelProvider {

	@Override
	public String getLabel(Object value) {
        if (value != null) {
			Format decimalFormat = HTMLFormatter.getInstance().getDoubleFormat();
			return decimalFormat.format(value);
		} else {
			return StringServices.EMPTY_STRING;
        }
    }
}

