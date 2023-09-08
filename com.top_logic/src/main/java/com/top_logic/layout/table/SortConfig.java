/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.model.AllColumnsForConfiguredTypes;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;

/**
 * Sort configuration of a single column.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	SortConfig.COLUMN_PROPERTY,
	SortConfig.ASCENDING_PROPERTY
})
public interface SortConfig extends ConfigurationItem {

	/**
	 * Name of the {@link #getColumnName()} property.
	 */
	String COLUMN_PROPERTY = "column";

	/**
	 * Name of the {@link #getAscending()} property.
	 */
	String ASCENDING_PROPERTY = "ascending";


	/**
	 * {@link Mapping} to a JSON representation of a {@link SortConfig}.
	 */
	Mapping<Map, SortConfig> PARSER = new Mapping<>() {
		@Override
		public SortConfig map(Map input) {
			return SortConfigFactory.sortConfig(getColumnName(input), getAscending(input));
		}

		private boolean getAscending(Map<?, ?> sortConfig) {
			return ((Boolean) sortConfig.get(ASCENDING_PROPERTY)).booleanValue();
		}

		private String getColumnName(Map<?, ?> sortConfig) {
			return (String) sortConfig.get(COLUMN_PROPERTY);
		}

	};

	/**
	 * {@link Mapping} from a JSON representation of a {@link SortConfig}.
	 */
	Mapping<SortConfig, Map<String, Object>> SERIALIZER = new Mapping<>() {
		@Override
		public Map<String, Object> map(SortConfig input) {
			return toMap(input);
		}

		private Map<String, Object> toMap(SortConfig sortConfig) {
			HashMap<String, Object> result = MapUtil.newMap(2);
			result.put(COLUMN_PROPERTY, sortConfig.getColumnName());
			result.put(ASCENDING_PROPERTY, Boolean.valueOf(sortConfig.getAscending()));
			return result;
		}
	};

	/**
	 * {@link Mapping} to the {@link #getColumnName()}.
	 */
	Mapping<SortConfig, String> TO_NAME = new Mapping<>() {
		@Override
		public String map(SortConfig input) {
			return input.getColumnName();
		}
	};

	/**
	 * The sorted column name.
	 */
	@Name(COLUMN_PROPERTY)
	@Options(fun = AllColumnsForConfiguredTypes.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	String getColumnName();

	/**
	 * Whether the {@link #getColumnName()} is sorted in ascending order.
	 * 
	 * <p>
	 * <code>false</code> if the column is sorted in descending order.
	 * </p>
	 */
	@Name(ASCENDING_PROPERTY)
	@BooleanDefault(true)
	boolean getAscending();

}