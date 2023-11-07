/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * {@link Schema} defining an array as value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ArraySchema extends Schema {

	@Override
	@StringDefault(OpenAPISchemaConstants.SCHEMA_TYPE_ARRAY)
	String getType();

	/**
	 * The {@link Schema} that all entries must satisfy.
	 */
	@Constraint(NoArraySchema.class)
	@Mandatory
	Schema getItems();

	/**
	 * Setter for {@link #getItems()}.
	 */
	void setItems(Schema value);

	/**
	 * {@link ValueConstraint} that the {@link Schema} is <b>not</b> an {@link ArraySchema}.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class NoArraySchema extends ValueConstraint<Schema> {

		/**
		 * Creates a {@link NoArraySchema}.
		 */
		public NoArraySchema() {
			super(Schema.class);
		}

		@Override
		protected void checkValue(PropertyModel<Schema> propertyModel) {
			Schema value = propertyModel.getValue();
			if (value == null) {
				return;
			}
			if (value instanceof ArraySchema) {
				propertyModel.setProblemDescription(
					I18NConstants.ITEMS_MUST_NOT_BE_AN_ARRAY_SCHEMA__PROPERTY.fill(propertyModel.getLabel()));
			}
		}

	}

}
