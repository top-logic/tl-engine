/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.TypeSafeLabelProvider;

/**
 * {@link LabelProvider} for {@link ColumnOption}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ColumnOptionLabelProvider extends TypeSafeLabelProvider<ColumnOption> {

	@Override
	protected Class<ColumnOption> getObjectType() {
		return ColumnOption.class;
	}

	@Override
	protected String getNonNullLabel(ColumnOption option) {
		return option.getLabel();
	}

}
