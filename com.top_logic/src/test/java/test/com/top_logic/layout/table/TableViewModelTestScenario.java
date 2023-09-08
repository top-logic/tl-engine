/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import static test.com.top_logic.layout.table.TableModelTestScenario.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import test.com.top_logic.layout.table.TestTableViewModel.TestingTableViewModel;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Icons;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.TableFilterProviderUtil;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Provider of {@link TableViewModel} and table filter parts.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableViewModelTestScenario {

	private EditableRowTableModel applicationModel;

	private TableViewModel viewModel;

	private StaticFilterWrapper globalStaticFilter;

	private StaticFilterWrapper c0StaticFilter1;

	private StaticFilterWrapper c0StaticFilter2;

	private StaticFilterWrapper c1StaticFilter1;

	private StaticFilterWrapper c1StaticFilter2;

	private StaticFilterWrapper c2StaticFilter1;

	private StaticFilterWrapper c3StaticFilter1;

	/**
	 * Create a new {@link TableViewModelTestScenario}.
	 */
	public TableViewModelTestScenario(EditableRowTableModel applicationModel) {
		this.applicationModel = applicationModel;
		viewModel = createTableViewModel(applicationModel);
	}

	public EditableRowTableModel getApplicationModel() {
		return applicationModel;
	}

	public TableViewModel getViewModel() {
		return viewModel;
	}

	public StaticFilterWrapper getGlobalStaticFilter() {
		return globalStaticFilter;
	}

	public StaticFilterWrapper getC0StaticFilter1() {
		return c0StaticFilter1;
	}

	public StaticFilterWrapper getC0StaticFilter2() {
		return c0StaticFilter2;
	}

	public StaticFilterWrapper getC1StaticFilter1() {
		return c1StaticFilter1;
	}

	public StaticFilterWrapper getC1StaticFilter2() {
		return c1StaticFilter2;
	}

	public StaticFilterWrapper getC2StaticFilter1() {
		return c2StaticFilter1;
	}

	/**
	 * first filter of column c3
	 */
	public StaticFilterWrapper getC3StaticFilter1() {
		return c3StaticFilter1;
	}

	private TableViewModel createTableViewModel(TableModel applicationModel) {
		TableViewModel viewModel = new TestingTableViewModel(applicationModel);

		createColumnComparators(applicationModel, viewModel);
		createTableFilters(viewModel);

		return viewModel;
	}

	private void createColumnComparators(TableModel applicationModel, TableViewModel model) {
		for (int i = 0; i < applicationModel.getColumnCount(); i++) {
			model.setColumnComparator(i, ComparableComparator.INSTANCE);
		}
	}

	private void createTableFilters(TableViewModel model) {
		createGlobalFilter(model);
		createFilterForFirstColumn(model);
		createFilterForSecondColumn(model);
		createFilterForThirdColumn(model);
		createFilterForFourthColumn(model);
	}

	private void createGlobalFilter(TableViewModel model) {
		globalStaticFilter = new StaticFilterWrapper(new Filter<Object>() {

			@Override
			public boolean accept(Object rowObject) {
				TableConfiguration tableConfiguration = applicationModel.getTableConfiguration();
				ColumnConfiguration column1 = tableConfiguration.getDeclaredColumn(C1);
				ColumnConfiguration column3 = tableConfiguration.getDeclaredColumn(C3);
				return column1.getAccessor().getValue(rowObject, C1).equals(C1_A) &&
					column3.getAccessor().getValue(rowObject, C3).equals(C3_A);
			}
		}, new ConstantDisplayValue("GLOBAL_FILTER"));

		FilterDialogBuilder manager = getFilterDialogManager();
		TableFilter controlFilter = new TableFilter(manager, Collections.singletonList(globalStaticFilter), true);

		model.setGlobalFilter(controlFilter);
	}

	private void createFilterForFirstColumn(TableViewModel model) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);
		c0StaticFilter1 = new StaticFilterWrapper(new EqualsFilter(C0_A),
			new ConstantDisplayValue(C0_A));
		c0StaticFilter2 = new StaticFilterWrapper(new EqualsFilter(C0_B),
			new ConstantDisplayValue(C0_B));
		filters.add(c0StaticFilter1);
		filters.add(c0StaticFilter2);

		createFilterForColumn(TableModelTestScenario.C0, model, filters);
	}

	private void createFilterForSecondColumn(TableViewModel model) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);
		c1StaticFilter1 = new StaticFilterWrapper(new EqualsFilter(C1_A),
			new ConstantDisplayValue(C1_A));
		c1StaticFilter2 = new StaticFilterWrapper(new EqualsFilter("nomatch"),
			new ConstantDisplayValue("nomatch"));
		filters.add(c1StaticFilter1);
		filters.add(c1StaticFilter2);

		createFilterForColumn(TableModelTestScenario.C1, model, filters);
	}

	private void createFilterForThirdColumn(TableViewModel model) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);
		c2StaticFilter1 = new StaticFilterWrapper(new EqualsFilter(C2_A),
			new ConstantDisplayValue(C2_A));
		filters.add(c2StaticFilter1);

		createFilterForColumn(TableModelTestScenario.C2, model, filters);
	}

	private void createFilterForFourthColumn(TableViewModel model) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);
		c3StaticFilter1 = TableFilterProviderUtil.createNoValueFilter();
		filters.add(c3StaticFilter1);

		createFilterForColumn(TableModelTestScenario.C3, model, filters);
	}

	private void createFilterForColumn(String columnName, TableViewModel model, List<ConfiguredFilter> filters) {
		// Creation of table filter infrastructure based on static filters
		FilterDialogBuilder manager = getFilterDialogManager();
		TableFilter controlFilter = new TableFilter(manager, filters, true);

		model.setFilter(model.getHeader().getColumn(columnName).getIndex(), controlFilter);
	}

	private PopupFilterDialogBuilder getFilterDialogManager() {
		PopupDialogModel dialogModel =
			new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
				new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL, 100, 70, DisplayUnit.PIXEL, 100, Scrolling.NO),
				1);
		return new PopupFilterDialogBuilder(dialogModel);
	}
}
