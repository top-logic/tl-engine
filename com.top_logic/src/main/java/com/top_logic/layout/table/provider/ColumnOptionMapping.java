/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} for {@link ColumnOption options} providing the technical name.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ColumnOptionMapping implements OptionMapping {

	@Override
	public Object toSelection(Object option) {
		if (option instanceof ColumnOption) {
			return ((ColumnOption) option).getTechnicalName();
		}

		return null;
	}

}
