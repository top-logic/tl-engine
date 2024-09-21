/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.basic.col.SetBuilder;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * {@link ColumnConfigurator} to suppress the export of the column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SuppressExportConfigurator implements ColumnConfigurator {

	/** Singleton {@link SuppressExportConfigurator} instance. */
	public static final SuppressExportConfigurator INSTANCE = new SuppressExportConfigurator();

	/**
	 * Creates a new {@link SuppressExportConfigurator}.
	 */
	protected SuppressExportConfigurator() {
		// singleton instance
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setClassifiers(
			new SetBuilder<String>()
				.addAll(column.getClassifiers())
				.add(ColumnConfig.CLASSIFIER_NO_EXPORT)
				.toSet());
	}

}
