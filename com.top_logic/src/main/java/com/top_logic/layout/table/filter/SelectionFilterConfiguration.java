/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;

/**
 * {@link FilterConfiguration}, that is uses by filters, that support separate selectable options.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectionFilterConfiguration extends FilterConfiguration {

	private List<?> filterPattern = Collections.emptyList();
	private JSONTransformer jsonTransformer;
	
	private Map<Object, MutableInteger> options;
	private TableViewModel tableModel;
	private String columnName;
	private List<Class<?>> supportedObjectTypes;
	private boolean hasCollectionValues;
	private boolean useRawOptions;
	private boolean showOptionEntries;

	/**
	 * Create a new {@link SelectionFilterConfiguration}.
	 */
	public SelectionFilterConfiguration(TableViewModel tableModel, String columnName, JSONTransformer jsonTransformer,
			boolean hasCollectionValues, boolean showOptionEntries) {
		this.jsonTransformer = jsonTransformer;
		this.tableModel = tableModel;
		this.columnName = columnName;
		options = new HashMap<>();
		this.hasCollectionValues = hasCollectionValues;
		this.showOptionEntries = showOptionEntries;
		supportedObjectTypes = Collections.<Class<?>> singletonList(Object.class);
		setUseRawOptions(false);
	}
	
	/**
	 * true, if option matches shall be counted, false otherwise.
	 * 
	 * @see TableModel#isFilterCountingEnabled()
	 */
	public boolean isOptionMatchCountable() {
		return tableModel.isFilterCountingEnabled();
	}

	/**
	 * true, if the filter shall handle collection values, false otherwise.
	 */
	public boolean hasCollectionValues() {
		return hasCollectionValues;
	}

	/**
	 * true, if the filter options shall be available as selectable entries, false
	 *         otherwise.
	 */
	public boolean showOptionEntries() {
		return showOptionEntries;
	}

	/**
	 * true, if the given value is part of the filter pattern, false otherwise.
	 */
	public boolean matchesFilterPattern(Object value) {
		return filterPattern.contains(value);
	}

	/**
	 * the object types, which can be handled by this {@link FilterConfiguration}.
	 */
	public List<Class<?>> getSupportedObjectTypes() {
		return supportedObjectTypes;
	}

	/**
	 * @see #getSupportedObjectTypes()
	 */
	public void setSupportedObjectTypes(List<Class<?>> supportedObjectTypes) {
		this.supportedObjectTypes = supportedObjectTypes;
	}

	/** comparator used for sorting options of the column this filter belongs to. */
	public Comparator<?> getOptionComparator() {
		if (!columnName.equals(TableViewModel.GLOBAL_TABLE_FILTER_ID)) {
			return tableModel.getColumnDescription(columnName).getComparator();
		} else {
			return tableModel.getTableConfiguration().getDefaultColumn().getComparator();
		}
	}

	/** label provider used for displaying options of the column this filter belongs to. */
	public LabelProvider getOptionLabelProvider() {
		if (!columnName.equals(TableViewModel.GLOBAL_TABLE_FILTER_ID)) {
			return tableModel.getColumnDescription(columnName).getFullTextProvider();
		} else {
			return tableModel.getTableConfiguration().getDefaultColumn().getFullTextProvider();
		}
	}

	/**
	 * attributes of the wrapper, after which shall be filtered
	 */
	public List<?> getFilterPattern() {
		return filterPattern;
	}

	/**
	 * @see #getFilterPattern()
	 */
	public void setFilterPattern(List<?> filterPattern) {
		this.filterPattern = filterPattern;
		notifyValueChanged();
	}

	@Override
	public List<Object> getSerializedState() {
		return jsonTransformer.transformToJSON(filterPattern);
	}

	@Override
	public void setSerializedState(List<Object> state) {
		filterPattern = jsonTransformer.transformFromJSON(state);
	}

	@Override
	public FilterConfigurationType getConfigurationType() {
		return new FilterConfigurationType(this.getClass(), jsonTransformer.getClass());
	}

	/**
	 * possible options to select from, and their match count in the table column.
	 */
	public Map<Object, MutableInteger> getOptions() {
		return options;
	}

	/**
	 * @see SelectionFilterConfiguration#getOptions()
	 */
	public void setOptions(Map<Object, MutableInteger> options) {
		this.options = options;
		notifyValueChanged();
	}

	@Override
	protected void clearConfiguration() {
		setFilterPattern(Collections.emptyList());
	}

	boolean isUseRawOptions() {
		return useRawOptions;
	}

	/**
	 * @param useRawOptions
	 *        - whether available column options shall be collected as is, or converted to string
	 *        representations, using {@link #getOptionLabelProvider()}. Set this to false only, if
	 *        the option type can be serialized by TL JSON.
	 */
	public void setUseRawOptions(boolean useRawOptions) {
		this.useRawOptions = useRawOptions;
	}
}