/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.TableModelTestScenario;
import test.com.top_logic.layout.table.TestTableViewModel.TestingTableViewModel;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.filter.TextFilter;
import com.top_logic.layout.table.filter.TextFilterConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * Test case for the order of the options of a {@link TextFilter}.
 *
 * @see TextFilterConfiguration#getLabelOptionComparator()
 */
public class TestTextFilterOptionOrder extends AbstractLayoutTest {

	/** Labels whose alphabetical order differs from the numeric order of the values. */
	private static final LabelProvider NUMBER_LABELS = value -> "Value " + value;

	private EditableRowTableModel _applicationModel;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		_applicationModel = new TableModelTestScenario().createTableModel();
	}

	/**
	 * The options are sorted by the column comparator applied to the values they were created
	 * from, not alphabetically by their label (which would put "Value 10" before "Value 2").
	 */
	public void testOptionsSortedByColumnComparator() {
		setColumnComparator(ComparableComparator.INSTANCE);
		TextFilter filter = createFilter(false);

		collectOptions(filter, 10, 2, 9, 1);

		assertEquals(Arrays.asList("Value 1", "Value 2", "Value 9", "Value 10"), sortedOptions(filter));
	}

	/**
	 * A custom comparator set for the column (here: descending) determines the option order.
	 */
	public void testOptionsSortedByCustomColumnComparator() {
		setColumnComparator(Collections.reverseOrder());
		TextFilter filter = createFilter(false);

		collectOptions(filter, 10, 2, 9, 1);

		assertEquals(Arrays.asList("Value 10", "Value 9", "Value 2", "Value 1"), sortedOptions(filter));
	}

	/**
	 * For collection-valued cells, each option is created from a single element. The column
	 * comparator, which compares whole collections, is applied to the element wrapped into a
	 * singleton list.
	 */
	public void testOptionsOfCollectionValuesSortedByColumnComparator() {
		setColumnComparator(ComparableComparator.INSTANCE);
		TextFilter filter = createFilter(true);

		collectOptions(filter, Arrays.asList(10, 2), Arrays.asList(9), Arrays.asList(1, 10));

		assertEquals(Arrays.asList("Value 1", "Value 2", "Value 9", "Value 10"), sortedOptions(filter));
	}

	/**
	 * Options whose values are equal according to the column comparator are sorted
	 * alphabetically by their label.
	 */
	public void testOptionsWithEqualValuesSortedByLabel() {
		setColumnComparator((v1, v2) -> 0);
		TextFilter filter = createFilter(false);

		collectOptions(filter, 10, 2, 9, 1);

		assertEquals(Arrays.asList("Value 1", "Value 10", "Value 2", "Value 9"), sortedOptions(filter));
	}

	/**
	 * Options for which no value was collected (e.g. a selected option not present in the current
	 * table rows) are sorted after all options with value, and alphabetically among each other.
	 */
	public void testOptionsWithoutValueSortedLast() {
		setColumnComparator(ComparableComparator.INSTANCE);
		TextFilter filter = createFilter(false);

		collectOptions(filter, 10, 2);

		List<Object> options = new ArrayList<>(Arrays.asList("Unknown B", "Value 10", "Unknown A", "Value 2"));
		options.sort(config(filter).getLabelOptionComparator());
		assertEquals(Arrays.asList("Value 2", "Value 10", "Unknown A", "Unknown B"), options);
	}

	/**
	 * Sets the comparator and the label provider of the filtered column.
	 */
	private void setColumnComparator(Comparator<?> comparator) {
		ColumnConfiguration column =
			_applicationModel.getTableConfiguration().getDeclaredColumn(TableModelTestScenario.C0);
		column.setComparator(comparator);
		column.setFullTextProvider(NUMBER_LABELS);
	}

	/**
	 * Creates a {@link TextFilter} for the column {@link TableModelTestScenario#C0}.
	 * 
	 * @param collectionValues
	 *        Whether the cells of the column contain collections of values.
	 */
	private TextFilter createFilter(boolean collectionValues) {
		TableViewModel viewModel = new TestingTableViewModel(_applicationModel);
		return TextFilter.createTextFilter(viewModel, TableModelTestScenario.C0, NUMBER_LABELS,
			Collections.<Class<?>> singletonList(Object.class), 0, true, true, collectionValues);
	}

	/**
	 * Simulates a filter revalidation of the table, in which the given cell values are passed to
	 * the filter. This creates the options of the filter.
	 */
	private static void collectOptions(TextFilter filter, Object... cellValues) {
		filter.startFilterRevalidation(true);
		for (Object value : cellValues) {
			filter.accept(value);
		}
		filter.stopFilterRevalidation();
	}

	/**
	 * The options of the given filter, sorted as displayed in the filter dialog.
	 */
	private static List<Object> sortedOptions(TextFilter filter) {
		TextFilterConfiguration config = config(filter);
		List<Object> options = new ArrayList<>(config.getOptions().keySet());
		options.sort(config.getLabelOptionComparator());
		return options;
	}

	private static TextFilterConfiguration config(TextFilter filter) {
		return (TextFilterConfiguration) filter.getFilterConfiguration();
	}

	/**
	 * Suite of tests in this class.
	 */
	public static Test suite() {
		return suite(
			ServiceTestSetup.createSetup(TestTextFilterOptionOrder.class, LabelProviderService.Module.INSTANCE));
	}
}
