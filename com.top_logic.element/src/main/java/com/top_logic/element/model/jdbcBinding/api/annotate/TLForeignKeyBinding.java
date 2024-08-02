/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.model.jdbcBinding.api.KeyColumnNormalizer;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} for a {@link TLReference} that identifies the target object by the
 * values in the given columns.
 * 
 * <p>
 * The target object is found by comparing the values in the given columns with the values of the
 * {@link TLTableBinding#getPrimaryKey() primary key} columns of the target table.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@TagName("foreign-key-binding")
@DisplayOrder({
	TLForeignKeyBinding.COLUMNS,
	TLForeignKeyBinding.COLUMN_CONFIGS,
})
public interface TLForeignKeyBinding extends TLAttributeAnnotation {

	/** The configuration for a non-trivial foreign key column. */
	@DisplayOrder({
		KeyColumnConfig.SOURCE_COLUMN,
		KeyColumnConfig.TARGET_COLUMN,
		KeyColumnConfig.NORMALIZER,
	})
	public interface KeyColumnConfig extends ConfigurationItem {

		/** Property name of {@link #getSourceColumn()}. */
		String SOURCE_COLUMN = "source-column";

		/** Property name of {@link #getTargetColumn()}. */
		String TARGET_COLUMN = "target-column";

		/** Property name of {@link #getNormalizer()}. */
		String NORMALIZER = "normalizer";

		/** The name of the column in this table. */
		@Mandatory
		@Name(SOURCE_COLUMN)
		String getSourceColumn();

		/** The column in the foreign table whose values are used as keys. */
		@Name(TARGET_COLUMN)
		String getTargetColumn();

		/** The {@link KeyColumnNormalizer} for this column. */
		@Name(NORMALIZER)
		PolymorphicConfiguration<KeyColumnNormalizer> getNormalizer();

	}

	/** Property name of {@link #getColumns()}. */
	String COLUMNS = "columns";

	/** Property name of {@link #getColumnConfigs()}. */
	String COLUMN_CONFIGS = "column-configs";

	/**
	 * The columns whose values are use to identify the target object of the annotated
	 * {@link TLReference}.
	 */
	@Name(COLUMNS)
	@Mandatory
	@Format(CommaSeparatedStrings.class)
	List<String> getColumns();

	/**
	 * @see #getColumns()
	 */
	void setColumns(List<String> value);

	/** The configuration for non-trivial foreign key columns. */
	@Name(COLUMN_CONFIGS)
	@Key(KeyColumnConfig.SOURCE_COLUMN)
	Map<String, KeyColumnConfig> getColumnConfigs();

}
