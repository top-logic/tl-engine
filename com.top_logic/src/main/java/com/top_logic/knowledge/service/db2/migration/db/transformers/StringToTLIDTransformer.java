/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Map;

import com.top_logic.basic.StringID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;

/**
 * {@link AbstractColumnTransformer} transforms a string value to a {@link StringID}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringToTLIDTransformer
		extends AbstractColumnTransformer<AbstractColumnTransformer.Config<StringToTLIDTransformer>> {

	/**
	 * Creates a new {@link StringToTLIDTransformer}.
	 */
	public StringToTLIDTransformer(InstantiationContext context,
			AbstractColumnTransformer.Config<StringToTLIDTransformer> config) {
		super(context, config);
	}

	@Override
	protected void internalTransform(RowValue row, String column) {
		Map<String, Object> values = row.getValues();
		Object columnValue = values.get(column);
		if (columnValue != null) {
			values.put(column, StringID.valueOf((String) columnValue));
		}
	}

}

