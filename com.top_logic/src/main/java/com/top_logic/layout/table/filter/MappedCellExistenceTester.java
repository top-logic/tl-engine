/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Objects;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.func.Identity;

/**
 * {@link CellExistenceTesterProxy} which maps the row object and the column name using configured
 * {@link Mapping}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MappedCellExistenceTester extends CellExistenceTesterProxy {

	/**
	 * Configuration of a {@link MappedCellExistenceTester}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends CellExistenceTesterProxy.Config {

		/** Configuration name of the value {@link #getObjectMapping()}. */
		String OBJECT_MAPPING = "object-mapping";

		/** Configuration name of the value {@link #getColumnMapping()}. */
		String COLUMN_MAPPING = "column-mapping";

		/**
		 * {@link Mapping} for the row objects.
		 */
		@Name(OBJECT_MAPPING)
		@ItemDefault(Identity.class)
		PolymorphicConfiguration<? extends Mapping<Object, ?>> getObjectMapping();

		/**
		 * Setter for {@link #getObjectMapping()}.
		 */
		void setObjectMapping(PolymorphicConfiguration<? extends Mapping<Object, ?>> mapping);

		/**
		 * {@link Mapping} for the column names.
		 */
		@Name(COLUMN_MAPPING)
		@ItemDefault(Identity.class)
		PolymorphicConfiguration<? extends Mapping<String, String>> getColumnMapping();

		/**
		 * Setter for {@link #getColumnMapping()}.
		 */
		void setColumnMapping(PolymorphicConfiguration<? extends Mapping<String, String>> mapping);
	}

	private final Mapping<Object, ?> _objectMapping;

	private final Mapping<String, String> _columnMapping;

	/**
	 * Creates a new {@link MappedCellExistenceTester} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MappedCellExistenceTester}.
	 */
	public MappedCellExistenceTester(InstantiationContext context, Config config) {
		super(context, config);
		_objectMapping = context.getInstance(config.getObjectMapping());
		_columnMapping = context.getInstance(config.getColumnMapping());
	}

	/**
	 * Creates a {@link MappedCellExistenceTester}.
	 */
	public MappedCellExistenceTester(CellExistenceTester tester, Mapping<Object, ?> objectMapping,
			Mapping<String, String> columnMapping) {
		super(tester);
		_objectMapping = Objects.requireNonNull(objectMapping);
		_columnMapping = Objects.requireNonNull(columnMapping);
	}

	@Override
	public boolean isCellExistent(Object rowObject, String columnName) {
		return super.isCellExistent(_objectMapping.map(rowObject), _columnMapping.map(columnName));
	}

}

