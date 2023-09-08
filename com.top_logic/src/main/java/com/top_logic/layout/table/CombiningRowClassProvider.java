/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import static com.top_logic.basic.StringServices.*;

import java.util.List;

import com.top_logic.layout.table.control.TableControl;

/**
 * {@link RowClassProvider}, that is a container of other {@link RowClassProvider}. The CSS classes
 * it will produce, is a concatenation of CSS classes, provided by the contained
 * {@link RowClassProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CombiningRowClassProvider implements RowClassProvider {

	private List<RowClassProvider> _providers;

	/**
	 * Create a new {@link CombiningRowClassProvider}.
	 */
	public CombiningRowClassProvider(List<RowClassProvider> providers) {
		_providers = providers;
	}

	@Override
	public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
		StringBuilder cssClasses = new StringBuilder();
		for (RowClassProvider rowClassProvider : _providers) {
			appendCssClass(cssClasses, rowClassProvider.getTRClass(view, rowOptions, displayedRow, row));
		}

		if (cssClasses.length() > 0) {
			return cssClasses.toString();
		} else {
			return null;
		}
	}

	private void appendCssClass(StringBuilder cssClasses, String additionalCssClass) {
		if (additionalCssClass != null) {
			if (cssClasses.length() > 0) {
				cssClasses.append(BLANK_CHAR);
			}
			cssClasses.append(additionalCssClass);
		}
	}

}
