/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.config.TypedConfiguration;
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
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider.Config;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableFilter;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ComparableFilterViewBuilder;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.DateFieldProvider;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.NumberComparator;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.StaticFilterWrapper;

/**
 * A filter provider for the {@link TableComponent}, applying filters to table columns 'givenName',
 * 'tooltip_surname', 'Responsibility' and 'gender'
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class DemoTableFilterProvider implements TableFilterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableFilter createTableFilter(TableViewModel aTableModel, String filterPosition) {

		TableFilter filter;
		if (filterPosition.equals("givenName")) {
			filter = createGivenNameFilter(aTableModel, filterPosition);
		} else if (filterPosition.equals("responsability")) {
			filter = createResponsabilityFilter(aTableModel, filterPosition);
		} else if (filterPosition.equals("gender")) {
			filter = createGenderFilter(aTableModel, filterPosition);
		} else {
			filter = LabelFilterProvider.INSTANCE.createTableFilter(aTableModel, filterPosition);
		}

		return filter;
	}
	
	/**
	 * This method creates the {@link TableFilter} for the column 'givenName'
	 * @param model - the table view model
	 * @param columnID - the column id
	 */
	private TableFilter createGivenNameFilter(TableViewModel model, String columnID) {

		Config filterConfiguration = TypedConfiguration.newConfigItem(Config.class);
		/*
		 *  Create combination of all filter types
		 */
		
		// Create text filter
		TableFilter filter = LabelFilterProvider.INSTANCE.createTableFilter(model, columnID);
		
		// Creation of a comparable filter
		ConfiguredFilter comparableFilter = new ComparableFilter(new ComparableFilterConfiguration(
			model, columnID, AllOperatorsProvider.INSTANCE, NumberComparator.getInstance(), false, true),
			filterConfiguration.getSeparateOptionDisplayThreshold(),
			filterConfiguration.shouldShowNonMatchingOptions());
		filter.addSubFilter(comparableFilter, true);
		
		// Creation of a date filter
		ConfiguredFilter dateFilter = new ComparableFilter(new ComparableFilterConfiguration(
			model, columnID, AllOperatorsProvider.INSTANCE, NumberComparator.getInstance(), false, true),
			new ComparableFilterViewBuilder(DateFieldProvider.INSTANCE,
				filterConfiguration.getSeparateOptionDisplayThreshold()),
			filterConfiguration.shouldShowNonMatchingOptions());
		filter.addSubFilter(dateFilter, true);
		
		// Creation of static filters
		List<ConfiguredFilter> staticFilters = new ArrayList<>(2);
		ConfiguredFilter firstFilter = new StaticFilterWrapper(new EqualsFilter("Thomas"), new ConstantDisplayValue("Thomas"));
		ConfiguredFilter secondFilter = new StaticFilterWrapper(new EqualsFilter("Holger"), new ConstantDisplayValue("Holger"));
		staticFilters.add(firstFilter);
		staticFilters.add(secondFilter);
		filter.addSubFilterGroup(staticFilters);

		return filter;
	}
	
	/**
	 * This method creates the {@link TableFilter} for the column 'responsability'
	 * @param model - the table view model
	 * @param columnID - the column id
	 */
	private TableFilter createResponsabilityFilter(TableViewModel model, String columnID) {
		List<ConfiguredFilter> staticFilters = new ArrayList<>(2);
		ConfiguredFilter firstFilter = new StaticFilterWrapper(new EqualsFilter("EWE"), new ConstantDisplayValue("EWE Projekt"));
		ConfiguredFilter secondFilter = new StaticFilterWrapper(new EqualsFilter("xxx"), new ConstantDisplayValue("impossible Projekt"));
		staticFilters.add(firstFilter);
		staticFilters.add(secondFilter);
		
		// Creation of table filter infrastructure based on static filters
		PopupDialogModel dialogModel = new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
			new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH),
				DisplayUnit.PIXEL, 100, 0, DisplayUnit.PIXEL, 100,
				Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);
		TableFilter controlFilter = new TableFilter(manager, staticFilters, true);
		
		return controlFilter;
	}
	
	/**
	 * This method creates the {@link TableFilter} for the column 'gender'
	 * @param model - the table view model
	 * @param columnID - the column id
	 */
	private TableFilter createGenderFilter(TableViewModel model, String columnID) {
		List<ConfiguredFilter> staticFilters = new ArrayList<>(2);
		ConfiguredFilter firstFilter = new StaticFilterWrapper(new EqualsFilter("male"), new ConstantDisplayValue("male"));
		ConfiguredFilter secondFilter = new StaticFilterWrapper(new EqualsFilter("female"), new ConstantDisplayValue("female"));
		staticFilters.add(firstFilter);
		staticFilters.add(secondFilter);
		
		// Creation of table filter infrastructure based on static filters
		PopupDialogModel dialogModel = new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
			new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH),
				DisplayUnit.PIXEL, 100, 0, DisplayUnit.PIXEL, 100,
				Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);
		TableFilter controlFilter = new TableFilter(manager, staticFilters, true);
		
		return controlFilter;
	}
}
