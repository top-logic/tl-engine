/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.properties.DBPropertiesSchema;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * {@link RowTransformer} adapting the application model stored in {@link DBPropertiesSchema#TABLE_NAME}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ApplicationModelTransformer implements RowTransformer {

	/**
	 * Property in {@link DBProperties} that stores the factory defaults of the application model.
	 * 
	 * @see "com.top_logic.element.model.DynamicModelService.APPLICATION_MODEL_PROPERTY"
	 */
	private static final String APPLICATION_MODEL_PROPERTY = "applicationModel";

	@Override
	public void transform(RowValue row, RowWriter out) {
		if (isApplicationModelRow(row)) {
			Map<String, Object> rowValues = row.getValues();
			String applicationModel = (String) rowValues.get(DBPropertiesSchema.PROP_VALUE_COLUMN_NAME);
			try {
				String modifiedModel = updateApplicationModel(applicationModel);
				rowValues.put(DBPropertiesSchema.PROP_VALUE_COLUMN_NAME, modifiedModel);
				log().info("Updated application model.");
			} catch (RuntimeException ex) {
				log().error("Unable to update application model: " + applicationModel, ex);
			}
		}
		out.write(row);
	}

	/**
	 * The log to write messages to.
	 */
	protected abstract Log log();

	/**
	 * Adapts the given application model.
	 * 
	 * @param serializedApplicationModel
	 *        Serialized version of the application model.
	 * @return The adapted model.
	 */
	protected abstract String updateApplicationModel(String serializedApplicationModel);

	boolean isApplicationModelRow(RowValue row) {
		if (row.getTable().getName().equals(DBPropertiesSchema.TABLE_NAME)) {
			Map<String, Object> values = row.getValues();
			if (DBProperties.GLOBAL_PROPERTY.equals(values.get(DBPropertiesSchema.NODE_COLUMN_NAME))) {
				if (APPLICATION_MODEL_PROPERTY.equals(values.get(DBPropertiesSchema.PROP_KEY_COLUMN_NAME))) {
					return true;
				}
			}
		}
		return false;
	}

}

