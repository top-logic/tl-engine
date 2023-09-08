/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;

/**
 * {@link RowTransformer} that changes values in a specific column.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColumnTransformer extends AbstractColumnTransformer<ColumnTransformer.Config> {

	private String _newValue;

	private String _oldValue;

	/**
	 * Configuration of a {@link ColumnTransformer}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractColumnTransformer.Config<ColumnTransformer> {

		/**
		 * The old value to replace.
		 */
		String getOldValue();

		/**
		 * The new value to set.
		 */
		String getNewValue();
	}

	/**
	 * Creates a new {@link ColumnTransformer}.
	 */
	public ColumnTransformer(InstantiationContext context, Config config) {
		super(context, config);
		_oldValue = config.getOldValue();
		_newValue = config.getNewValue();
	}

	@Override
	protected void internalTransform(RowValue row, String column) {
		Object attributeValue = row.getValues().get(column);
		if (StringServices.equals(attributeValue, _oldValue)) {
			row.getValues().put(column, _newValue);
		}
	}

}

