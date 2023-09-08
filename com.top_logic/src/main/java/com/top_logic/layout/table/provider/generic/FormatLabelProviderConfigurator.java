/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.layout.table.filter.GenericFormatProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * {@link ColumnConfigurator} that creates a {@link LabelProvider} from the annotated format of the
 * attribute a column was built for.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormatLabelProviderConfigurator implements ColumnConfigurator {

	/**
	 * Singleton {@link FormatLabelProviderConfigurator} instance.
	 */
	public static final FormatLabelProviderConfigurator INSTANCE = new FormatLabelProviderConfigurator();

	private FormatLabelProviderConfigurator() {
		// Singleton constructor.
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setLabelProvider(new FormatLabelProvider(GenericFormatProvider.INSTANCE.getFormat(column)));
	}

}
