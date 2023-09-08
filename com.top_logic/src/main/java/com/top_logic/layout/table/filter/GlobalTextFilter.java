/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.LabelFilterProvider.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ResettableIterator;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link Filter} for specified text in any column of a table row.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GlobalTextFilter extends ColumnGlobalFilterAdapter<TextFilter> {

	private ResettableIterator<FilterableColumn> filterableColumnsIterator;
	private ResettableIterator<FilterableColumn> defaultFilterableColumnsIterator;

	/**
	 * Creates a {@link GlobalTextFilter}
	 * 
	 * @param tableViewModel
	 *        See {@link #getTableViewModel()}
	 */
	@SuppressWarnings("synthetic-access")
	public GlobalTextFilter(TableViewModel tableViewModel, boolean showNonMatchingOptions) {
		super(tableViewModel, createTextFilter(tableViewModel, showNonMatchingOptions));
		defaultFilterableColumnsIterator = filterableColumnsIterator = new SearchIterator(tableViewModel);
	}

	private static TextFilter createTextFilter(TableViewModel tableViewModel, boolean showNonMatchingOptions) {
		List<Class<?>> supportedTypes = Collections.<Class<?>> singletonList(Object.class);
		TextFilter filter =
			TextFilter.createTextFilter(tableViewModel, TableViewModel.GLOBAL_TABLE_FILTER_ID,
				DefaultLabelProvider.INSTANCE, supportedTypes, 0, showNonMatchingOptions, false, false);
		filter.setSupportsBlockedValues(true);
		return filter;
	}

	@SuppressWarnings("synthetic-access")
	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		boolean doCount = false;
		super.startFilterRevalidation(doCount);
		filterableColumnsIterator = new PrederminedColumnsIterator(getTableViewModel());
	}

	@Override
	public boolean accept(Object rowObject) {
		boolean result = false;
		if (rowObject != null) {
			while (filterableColumnsIterator.hasNext()) {
				FilterableColumn filterableColumn = filterableColumnsIterator.next();
				if (accept(rowObject, filterableColumn)) {
					result = true;
					break;
				}
			}
		}
		filterableColumnsIterator.reset();
		return result;
	}

	private boolean accept(Object rowObject, FilterableColumn filterableColumn) {
		Mapping<Object, Object> valueMapping = filterableColumn.getValueMapping();
		LabelProvider fullTextProvider = filterableColumn.getFullTextProvider();
		getColumnFilter().setLabelProvider(fullTextProvider);

		return acceptedByColumnFilter(valueMapping.map(rowObject));
	}

	@Override
	public void stopFilterRevalidation() {
		super.stopFilterRevalidation();
		filterableColumnsIterator = defaultFilterableColumnsIterator;
	}

	private static class SearchIterator implements ResettableIterator<FilterableColumn> {

		private TableViewModel tableModel;
		private int currentElementIndex;
		private int nextElementIndex;

		private SearchIterator(TableViewModel tableModel) {
			this.tableModel = tableModel;
			currentElementIndex = 0;
			nextElementIndex = 0;
		}

		@Override
		public boolean hasNext() {
			updateNextIndex();
			return nextElementIndex < tableModel.getColumnCount();
		}

		private void updateNextIndex() {
			if (currentElementIndex == nextElementIndex) {
				calculateNextElementIndex();
			}
		}

		@SuppressWarnings({ "synthetic-access", "unchecked" })
		@Override
		public FilterableColumn next() {
			updateNextIndex();
			currentElementIndex = nextElementIndex;
			String columnName = tableModel.getColumnName(currentElementIndex);
			Mapping<Object, Object> valueMapping = tableModel.getRowMapping(columnName);
			LabelProvider fullTextProvider = tableModel.getColumnDescription(columnName).getFullTextProvider();
			return new FilterableColumn(columnName, valueMapping, fullTextProvider);
		}

		private void calculateNextElementIndex() {
			int columnCount = tableModel.getColumnCount();
			for (nextElementIndex = currentElementIndex + 1; nextElementIndex < columnCount; nextElementIndex++) {
				String currentColumnName = tableModel.getColumnName(nextElementIndex);
				ColumnConfiguration columnConfiguration = tableModel.getColumnDescription(currentColumnName);
				if (hasFullTextProvider(columnConfiguration)) {
					break;
				}
			}
		}

		static boolean hasFullTextProvider(ColumnConfiguration columnConfiguration) {
			LabelProvider fullTextProvider = columnConfiguration.getFullTextProvider();
			return isValidLabelProvider(fullTextProvider);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			currentElementIndex = nextElementIndex = 0;
		}
	}
	
	private static class PrederminedColumnsIterator implements ResettableIterator<FilterableColumn> {

		private List<FilterableColumn> filterableColumns;
		private int currentElementIndex;

		private PrederminedColumnsIterator(TableViewModel tableModel) {
			filterableColumns = createFilterableColumns(tableModel);
			currentElementIndex = 0;
		}

		@SuppressWarnings({ "hiding", "unchecked", "synthetic-access" })
		private List<FilterableColumn> createFilterableColumns(TableViewModel tableModel) {
			List<FilterableColumn> filterableColumns = new ArrayList<>();
			for (int i = 0; i < tableModel.getColumnCount(); i++) {
				String currentColumnName = tableModel.getColumnName(i);
				ColumnConfiguration columnConfiguration = tableModel.getColumnDescription(currentColumnName);
				if (SearchIterator.hasFullTextProvider(columnConfiguration)) {
					Mapping<Object, Object> valueMapping = tableModel.getRowMapping(currentColumnName);
					LabelProvider fullTextProvider = columnConfiguration.getFullTextProvider();
					filterableColumns.add(new FilterableColumn(currentColumnName,
						valueMapping, fullTextProvider));
				}
			}
			return filterableColumns;
		}

		@Override
		public boolean hasNext() {
			return currentElementIndex < filterableColumns.size();
		}


		@Override
		public FilterableColumn next() {
			return filterableColumns.get(currentElementIndex++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			currentElementIndex = 0;
		}
	}
	
	private static class FilterableColumn {
		private String name;
		private Mapping<Object, Object> valueMapping;
		private LabelProvider fullTextProvider;
		
		private FilterableColumn(String name, Mapping<Object, Object> valueMapping, LabelProvider fullTextProvider) {
			this.name = name;
			this.valueMapping = valueMapping;
			this.fullTextProvider = fullTextProvider;
		}
		
		public String getName() {
			return name;
		}
		
		public Mapping<Object, Object> getValueMapping() {
			return valueMapping;
		}
		
		public LabelProvider getFullTextProvider() {
			return fullTextProvider;
		}
	}
}