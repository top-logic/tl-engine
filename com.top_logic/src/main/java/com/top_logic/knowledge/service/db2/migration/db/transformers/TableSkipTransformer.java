/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * Skips all values for the given types.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableSkipTransformer extends AbstractRowTransformer<AbstractRowTransformer.Config<TableSkipTransformer>> {

	/**
	 * Creates a new {@link TableSkipTransformer}.
	 */
	public TableSkipTransformer(InstantiationContext context,
			AbstractRowTransformer.Config<TableSkipTransformer> config) {
		super(context, config);
	}

	@Override
	protected void internalTransform(RowValue row, RowWriter out) {
		// Skip type
	}

}

