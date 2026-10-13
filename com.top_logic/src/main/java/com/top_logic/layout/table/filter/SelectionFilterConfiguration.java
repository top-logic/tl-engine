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
import com.top_logic.util.TLCollator;

/**
 * {@link FilterConfiguration}, that is uses by filters, that support separate selectable options.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectionFilterConfiguration extends FilterConfiguration {

	private List<?> filterPattern = Collections.emptyList();
	private JSONTransformer jsonTransformer;
	
	private Map<Object, MutableInteger> options;
	private Map<Object, Object> _optionValues = Collections.emptyMap();
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

	/**
	 * {@link Comparator} for the {@link #getOptions() options} of this filter, if they are labels
	 * of the column values (not {@link #isUseRawOptions()}).
	 * 
	 * <p>
	 * The options are sorted in the same order as the column values they represent, i.e. the
	 * {@link #getOptionComparator() comparator of the column} is applied to the
	 * {@link #setOptionValues(Map) values} the labels were created from. Options with equal values
	 * and options without known value are sorted by their label.
	 * </p>
	 */
	public Comparator<Object> getLabelOptionComparator() {
		return new LabelOptionComparator(getOptionComparator());
	}

	/**
	 * Sets for each option of this filter some column value it was created from.
	 * 
	 * @see #getLabelOptionComparator()
	 */
	void setOptionValues(Map<Object, Object> optionValues) {
		_optionValues = optionValues;
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

	private class LabelOptionComparator implements Comparator<Object> {

		private final Comparator<Object> _valueComparator;

		private final TLCollator _labelComparator = new TLCollator();

		@SuppressWarnings("unchecked")
		LabelOptionComparator(Comparator<?> valueComparator) {
			_valueComparator = (Comparator<Object>) valueComparator;
		}

		@Override
		public int compare(Object option1, Object option2) {
			if (_valueComparator != null) {
				int valueResult = compareValues(option1, option2);
				if (valueResult != 0) {
					return valueResult;
				}
			}
			return _labelComparator.compare(String.valueOf(option1), String.valueOf(option2));
		}

		private int compareValues(Object option1, Object option2) {
			boolean hasValue1 = _optionValues.containsKey(option1);
			boolean hasValue2 = _optionValues.containsKey(option2);
			if (!hasValue1) {
				// Options without value are sorted after options with value.
				return hasValue2 ? 1 : 0;
			}
			if (!hasValue2) {
				return -1;
			}
			return _valueComparator.compare(columnValue(option1), columnValue(option2));
		}

		private Object columnValue(Object option) {
			Object value = _optionValues.get(option);
			if (hasCollectionValues) {
				// The options are created from the elements of the collection values, but the
				// column comparator compares whole collections.
				return Collections.singletonList(value);
			}
			return value;
		}
	}
}