/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import static test.com.top_logic.layout.table.TableModelTestScenario.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.layout.table.TableModelTestScenario;
import test.com.top_logic.layout.table.WrappingTableRowFilter;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.MappingBasedFilter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.AccessorMapping;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.util.Utils;

/**
 * The class {@link TestObjectTableModel} provides tests for {@link ObjectTableModel}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestObjectTableModel extends BasicTestCase {

	static class InverseComparableComparator<T extends Comparable<T>> implements Comparator<T> {

		@Override
		public int compare(T o1, T o2) {
			return o2.compareTo(o1);
		}

	}

	/**
	 * This method returns a list of list.
	 * 
	 * @param rowCount
	 *            the size of the list
	 * @param colCount
	 *            the size of each "entry list"
	 */
	protected final List<List<String>> createRowObjects(int rowCount, int colCount) {
		List<List<String>> rowObjects = new ArrayList<>(rowCount);
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			rowObjects.add(createRow(rowIndex, colCount));
		}
		return rowObjects;
	}

	private List<String> createRow(int rowIndex, int colCount) {
		List<String> row = new ArrayList<>(colCount);
		for (int colIndex = 0; colIndex < colCount; colIndex++) {
			row.add("[row " + rowIndex + ", column " + colIndex + "] added at " + System.currentTimeMillis());
		}
		return row;
	}

	public void testSetRowObjects() {
		ObjectTableModel testedModel = createTableModel(SimpleAccessor.INSTANCE, new String[] { "a", "b", "c" });
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();

		List<?> rowObjects = createRowObjects(100, 3);
		testedModel.setRowObjects(rowObjects);
		assertEquals(rowObjects, tester.getObservedRows());

		rowObjects = createRowObjects(42, 5);
		testedModel.setRowObjects(rowObjects);
		assertEquals(rowObjects, tester.getObservedRows());
	}

	public void testInsert() {
		List<?> rowObjects = createRowObjects(5, 1);
		ObjectTableModel testedModel = createTableModel(SimpleAccessor.INSTANCE, new String[] { "a" });
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();
		
		testedModel.insertRowObject(0, "rowObject");
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());

		testedModel.setRowObjects(rowObjects);
		testedModel.insertRowObject(0, (Object) createRow(5, 1));
		assertEquals(6, testedModel.getRowCount());
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());

		ListBuilder<?> listBuilder = new ListBuilder<>(2).add(createRow(5, 1)).add(createRow(6, 1));
		testedModel.setRowObjects(rowObjects);
		testedModel.insertRowObject(0, listBuilder.toList());
		assertEquals(7, testedModel.getRowCount());
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());
	}

	public void testAddRow() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.addRowObject(4);
		assertEquals(list(0, 1, 2, 3, 4), tableModel.getDisplayedRows());
	}

	public void testAddMultipleRows() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		tableModel.setOrder(Comparator.naturalOrder());
		List<TableModelEvent> events = new ArrayList<>();
		tableModel.addTableModelListener(event -> events.add(event));

		tableModel.addAllRowObjects(list(4, 5, 6));
		assertDisplayedAndAllRows(list(0, 1, 2, 3, 4, 5, 6), tableModel);
		assertEventChangeRange(events, 4, 6);

		tableModel.addAllRowObjects(list());
		assertDisplayedAndAllRows(list(0, 1, 2, 3, 4, 5, 6), tableModel);
		assertEquals(0, events.size());
	}

	public void testAddMultipleRowsSingleRows() {
		List<Integer> tableRows = list();
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		tableModel.setOrder(Comparator.naturalOrder());
		List<TableModelEvent> events = new ArrayList<>();
		tableModel.addTableModelListener(event -> events.add(event));

		tableModel.addAllRowObjects(list());
		assertDisplayedAndAllRows(list(), tableModel);
		assertEquals(0, events.size());

		tableModel.addAllRowObjects(list(1));
		assertDisplayedAndAllRows(list(1), tableModel);
		assertEventChangeRange(events, 0, 0);

		tableModel.addAllRowObjects(list(3));
		assertDisplayedAndAllRows(list(1, 3), tableModel);
		assertEventChangeRange(events, 1, 1);

		tableModel.addAllRowObjects(list(5));
		assertDisplayedAndAllRows(list(1, 3, 5), tableModel);
		assertEventChangeRange(events, 2, 2);

		tableModel.addAllRowObjects(list(0));
		assertDisplayedAndAllRows(list(0, 1, 3, 5), tableModel);
		assertEventChangeRange(events, 0, 0);

		tableModel.addAllRowObjects(list(2));
		assertDisplayedAndAllRows(list(0, 1, 2, 3, 5), tableModel);
		assertEventChangeRange(events, 2, 2);
	}

	public void testAddMultipleRowsTwoRows() {
		List<Integer> tableRows = list();
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		tableModel.setOrder(Comparator.naturalOrder());
		List<TableModelEvent> events = new ArrayList<>();
		tableModel.addTableModelListener(event -> events.add(event));

		tableModel.addAllRowObjects(list(1, 3));
		assertDisplayedAndAllRows(list(1, 3), tableModel);
		assertEventChangeRange(events, 0, 1);

		tableModel.addAllRowObjects(list(5, 7));
		assertDisplayedAndAllRows(list(1, 3, 5, 7), tableModel);
		assertEventChangeRange(events, 2, 3);

		tableModel.addAllRowObjects(list(0, 2));
		assertDisplayedAndAllRows(list(0, 1, 2, 3, 5, 7), tableModel);
		assertEventChangeRange(events, 0, 2);
	}

	public void testAddMultipleRowsThreeRows() {
		List<Integer> tableRows = list();
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		tableModel.setOrder(Comparator.naturalOrder());
		List<TableModelEvent> events = new ArrayList<>();
		tableModel.addTableModelListener(event -> events.add(event));

		tableModel.addAllRowObjects(list(1, 6, 11));
		assertDisplayedAndAllRows(list(1, 6, 11), tableModel);
		assertEventChangeRange(events, 0, 2);

		tableModel.addAllRowObjects(list(16, 21, 26));
		assertDisplayedAndAllRows(list(1, 6, 11, 16, 21, 26), tableModel);
		assertEventChangeRange(events, 3, 5);

		tableModel.addAllRowObjects(list(-2, -1, 0));
		assertDisplayedAndAllRows(list(-2, -1, 0, 1, 6, 11, 16, 21, 26), tableModel);
		assertEventChangeRange(events, 0, 2);

		tableModel.addAllRowObjects(list(2, 3, 4));
		assertDisplayedAndAllRows(list(-2, -1, 0, 1, 2, 3, 4, 6, 11, 16, 21, 26), tableModel);
		assertEventChangeRange(events, 4, 6);

		tableModel.addAllRowObjects(list(5, 7, 15, 20));
		assertDisplayedAndAllRows(list(-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 11, 15, 16, 20, 21, 26), tableModel);
		assertEventChangeRange(events, 7, 13);
	}

	public void testRemoveRowObject() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRowObject(1);
		assertEquals(list(0, 2, 3), tableModel.getDisplayedRows());

		setFilterZeroOrTwo(tableModel);
		assertEquals(list(0, 2), tableModel.getDisplayedRows());
		tableModel.removeRowObject(3);
		assertEquals("Removed object is not visible", list(0, 2), tableModel.getDisplayedRows());
		setTrueFilter(tableModel);
		assertEquals("Row object '" + 3 + "'was removed ", list(0, 2), tableModel.getDisplayedRows());

	}

	public void testRemoveAllRowObjectsSequential() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRowObject(0);
		assertEquals(list(1, 2, 3), tableModel.getDisplayedRows());
		tableModel.removeRowObject(1);
		assertEquals(list(2, 3), tableModel.getDisplayedRows());
		tableModel.removeRowObject(2);
		assertEquals(list(3), tableModel.getDisplayedRows());
		tableModel.removeRowObject(3);
		assertEquals(list(), tableModel.getDisplayedRows());
	}

	public void testRemoveAllRowsButHeadAndTail() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRowObject(1);
		assertEquals(list(0, 2, 3), tableModel.getDisplayedRows());
		tableModel.removeRowObject(2);
		assertEquals(list(0, 3), tableModel.getDisplayedRows());
	}

	public void testAddRemoveRowOnSortedTable() {
		List<Integer> tableRows = list(0, 1, 3, 4);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		assertEquals(tableRows, tableModel.getDisplayedRows());
		tableModel.setOrder(new InverseComparableComparator<>());
		assertEquals(list(4, 3, 1, 0), tableModel.getDisplayedRows());
		tableModel.addRowObject(2);
		assertEquals(list(4, 3, 2, 1, 0), tableModel.getDisplayedRows());
		tableModel.removeRowObject(0);
		assertEquals(list(4, 3, 2, 1), tableModel.getDisplayedRows());
	}

	public void testDoubleRemoveRowObject() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRowObject(1);
		assertEquals(list(0, 2, 3), tableModel.getDisplayedRows());

		tableModel.removeRowObject(1);
		assertEquals(list(0, 2, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveRowIndex() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRow(2);
		assertEquals(list(0, 1, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveInvalidRowIndex() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		try {
			tableModel.removeRow(5);
			fail("Removal of invalid row index should throw an IllegalArgumentException!");
		} catch (Exception ex) {
			assertInstanceof(ex, IllegalArgumentException.class);
			assertEquals("Row index, that shall be removed, is out of range! Range: [0, 3], RowIndex: 5",
				ex.getMessage());
		}
	}

	public void testRemoveMultipleRows() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.removeRows(1, 3);
		assertEquals(list(0, 3), tableModel.getDisplayedRows());
	}
	
	public void testRemoveMultipleInvalidRows() {
		try {
			List<Integer> tableRows = list(0, 1, 2, 3);
			ObjectTableModel tableModel = createTableModelSimple(tableRows);
			
			tableModel.removeRows(2, 7);
			fail("Removal of invalid row indices should throw an IllegalArgumentException!");
		} catch (Exception ex) {
			assertInstanceof(ex, IllegalArgumentException.class);
			assertEquals("Row indices, that shall be removed, are out of range! Range: [0, 3], RowIndices: [2, 7]", ex.getMessage());
		}
	}

	public void testRemoveRowObjectWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);
		tableModel.removeRowObject(2);
		assertEquals(list(0), tableModel.getDisplayedRows());

		setTrueFilter(tableModel);
		assertEquals(list(0, 1, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveRowIndexWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);
		tableModel.removeRow(0);
		assertEquals(list(2), tableModel.getDisplayedRows());

		setTrueFilter(tableModel);
		assertEquals(list(1, 2, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveLastRowIndexWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);
		assertEquals(list(0, 2), tableModel.getDisplayedRows());
		tableModel.removeRow(1);
		assertEquals(list(0), tableModel.getDisplayedRows());

		setTrueFilter(tableModel);
		assertEquals(list(0, 1, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveMultipleRowsWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);
		tableModel.removeRows(0, 2);
		assertEquals(Collections.emptyList(), tableModel.getDisplayedRows());

		setTrueFilter(tableModel);
		assertEquals(list(1, 3), tableModel.getDisplayedRows());
	}

	public void testRemoveMultipleRowsWithSortOrderFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3, 4);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		tableModel.setOrder(new InverseComparableComparator<>());
		assertEquals(list(4, 3, 2, 1, 0), tableModel.getDisplayedRows());
		tableModel.removeRows(0, 2);

	}

	public void testShowRow() {
		TableModelTestScenario scenario = new TableModelTestScenario(createPriorityTable());
		EditableRowTableModel testedModel = scenario.createTableModel();
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();

		// Scenario
		int colC2 = testedModel.getColumnNames().indexOf(C2);
		Filter<Object> filter = new MappingBasedFilter<>(
			new AccessorMapping<>(testedModel.getTableConfiguration().getCol(C0).getAccessor(), C0),
			new EqualsFilter(C0_A));
		Comparator<Object> order = new MappedComparator<>(
			new AccessorMapping<>(testedModel.getTableConfiguration().getCol(C2).getAccessor(), C2),
			new InverseComparator<>(ComparableComparator.INSTANCE));
		testedModel.setFilter(new WrappingTableRowFilter(filter), order);

		// Check scenario.
		assertEquals(3, testedModel.getRowCount());
		assertEquals(C2_C, testedModel.getValueAt(0, colC2));
		assertEquals(C2_A, testedModel.getValueAt(testedModel.getRowCount() - 1, colC2));
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());

		// Show C2_D row at the end.
		testedModel.showRowAt(scenario.getRows().get(1), testedModel.getRowCount());
		assertEquals(4, testedModel.getRowCount());
		assertEquals(C2_D, testedModel.getValueAt(testedModel.getRowCount() - 1, colC2));
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());

		// Show C2_A row at the end.
		testedModel.showRowAt(scenario.getRows().get(3), testedModel.getRowCount());
		assertEquals(5, testedModel.getRowCount());
		assertEquals(C2_E, testedModel.getValueAt(testedModel.getRowCount() - 1, colC2));
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());

		// Show C2_D row at the beginning.
		testedModel.showRowAt(scenario.getRows().get(5), 0);
		assertEquals(6, testedModel.getRowCount());
		assertEquals(C2_F, testedModel.getValueAt(0, colC2));
		assertEquals(testedModel.getDisplayedRows(), tester.getObservedRows());
	}

	public void testClearTwice() {
		ObjectTableModel testedModel =
			createTableModel(SimpleAccessor.INSTANCE, new String[] { "a", "b", "c" }, createRowObjects(100, 3));
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();

		testedModel.clear();
		testedModel.clear();

		assertEquals(Collections.EMPTY_LIST, tester.getObservedRows());
	}

	public void testBasics() {
		final String[] columnNames = new String[] { "a", "b", "c" };
		Accessor<?> listAccessor = new Accessor<List<Object>>() {
			@Override
			public Object getValue(List<Object> object, String property) {
				for (int index = 0, length = columnNames.length; index < length; index++) {
					if (Utils.equals(columnNames[index], property)) {
						return object.get(index);
					}
				}
				return null;
			}

			@Override
			public void setValue(List<Object> object, String property, Object value) {
				for (int index = 0, length = columnNames.length; index < length; index++) {
					if (Utils.equals(columnNames[index], property)) {
						object.set(index, value);
					}
				}
			}
		};
		List<List<String>> rowObjects = createRowObjects(100, 3);
		ObjectTableModel testedModel = createTableModel(listAccessor, columnNames);

		/* correct setting getting rowObjects */
		testedModel.setRowObjects(rowObjects);
		assertEquals(rowObjects, testedModel.getDisplayedRows());

		/* Correct accessor */
		assertEquals(listAccessor, testedModel.getTableConfiguration().getDefaultColumn().getAccessor());

		/* correct sizes */
		assertEquals(3 + TableConfiguration.defaultTable().getDeclaredColumns().size(), testedModel.getColumnCount());
		assertEquals(rowObjects.size(), testedModel.getRowCount());

		/* correct getValue */
		for (int rowIndex = 0; rowIndex < rowObjects.size(); rowIndex++) {
			List<String> row = rowObjects.get(rowIndex);
			for (int colIndex = 0, size = row.size(); colIndex < size; colIndex++) {
				int column = testedModel.getHeader().getColumn(columnNames[colIndex]).getIndex();
				assertEquals(row.get(colIndex), testedModel.getValueAt(rowIndex, column));
			}
		}

		/* model contains all elements */
		for (int rowIndex = 0; rowIndex < rowObjects.size(); rowIndex++) {
			assertTrue(testedModel.containsRowObject(rowObjects.get(rowIndex)));
		}
		for (int rowIndex = 0; rowIndex < rowObjects.size(); rowIndex++) {
			assertEquals(rowIndex, testedModel.getRowOfObject(rowObjects.get(rowIndex)));
		}

		/* correct column names */
		assertEquals(TestHeader.columnsWithDefaults(columnNames), list(getColumns(testedModel)));

		/* correct row removing and adding */
		int row = 4;
		Object rowObject = testedModel.getRowObject(row);
		testedModel.removeRowObject(rowObject);
		assertFalse(testedModel.containsRowObject(rowObject));
		testedModel.setRowObjects(rowObjects);

		testedModel.setRowObjects(rowObjects);
		testedModel.removeRow(row);
		assertEquals(rowObjects.get(row - 1), testedModel.getRowObject(row - 1));
		assertFalse(testedModel.containsRowObject(rowObjects.get(row)));
		assertEquals(rowObjects.get(row + 1), testedModel.getRowObject(row));

		Object newRowObject = new Object();
		testedModel.setRowObjects(rowObjects);
		testedModel.addRowObject(newRowObject);
		assertTrue(testedModel.containsRowObject(newRowObject));

		testedModel.setRowObjects(rowObjects);
		testedModel.insertRowObject(row, newRowObject);
		assertEquals(rowObjects.get(row - 1), testedModel.getRowObject(row - 1));
		assertEquals(newRowObject, testedModel.getRowObject(row));
		assertEquals(rowObjects.get(row), testedModel.getRowObject(row + 1));

		List<?> newRowObjects = createRowObjects(20, 3);
		testedModel.setRowObjects(rowObjects);
		testedModel.insertRowObject(row, newRowObjects);
		assertEquals(rowObjects.subList(0, row), testedModel.getDisplayedRows().subList(0, row));
		assertEquals(newRowObjects, testedModel.getDisplayedRows().subList(row, row + newRowObjects.size()));
		assertEquals(rowObjects.subList(row, rowObjects.size()),
			testedModel.getDisplayedRows().subList(row + newRowObjects.size(),
				testedModel.getDisplayedRows().size()));

		testedModel.setRowObjects(rowObjects);
		int column = 1;
		testedModel.setValueAt(newRowObject, row, column);
		assertEquals(newRowObject, testedModel.getValueAt(row, column));

	}

	public void testRowOrderPreservationAfterFilterDeactivation() {
		final List<Integer> allRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(allRows);

		setFilterZeroOrTwo(tableModel);

		assertEquals(list(0, 2), tableModel.getDisplayedRows());
		
		setTrueFilter(tableModel);
		
		assertEquals(list(0, 1, 2, 3), tableModel.getDisplayedRows());
	}

	/** Creates a table with a single column. */
	protected ObjectTableModel createTableModelSimple(List<Integer> rows) {
		return createTableModel(SimpleAccessor.INSTANCE, new String[] { "a" }, rows);
	}

	/** Sets the "true" filter. */
	protected void setTrueFilter(ObjectTableModel tableModel) {
		setFilter(FilterFactory.trueFilter(), Equality.INSTANCE, tableModel);
	}

	/** Sets the "0 or 2" filter. */
	protected void setFilterZeroOrTwo(ObjectTableModel tableModel) {
		setFilter(AcceptZeroOrTwoFilter.INSTANCE, Equality.INSTANCE, tableModel);
	}

	/**
	 * Creates the {@link ObjectTableModel} for test.
	 */
	protected ObjectTableModel createTableModel(Accessor<?> accessor, String[] columnNames, List<?> rowObjects) {
		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.getDefaultColumn().setAccessor(accessor);
		return new ObjectTableModel(columnNames, tableConfig, rowObjects, createPriorityTable());
	}

	/**
	 * Whether table should be priority tables.
	 */
	protected abstract boolean createPriorityTable();

	protected ObjectTableModel createTableModel(Accessor<?> accessor, String[] columnNames) {
		return createTableModel(accessor, columnNames, Collections.emptyList());
	}

	public String[] getColumns(TableModel tableModel) {
		List<String> columnNames = tableModel.getColumnNames();
		return columnNames.toArray(new String[columnNames.size()]);
	}

	protected void setFilter(Filter<?> wrappedFilter, Comparator<Object> defaultOrder, ObjectTableModel tableModel) {
		tableModel.setFilter(new WrappingTableRowFilter(wrappedFilter),
			defaultOrder);
	}

	/** Asserts that the table displays exactly the given rows in the given order. */
	protected void assertDisplayedRows(List<Integer> expectedRows, ObjectTableModel tableModel) {
		assertEquals(expectedRows, tableModel.getDisplayedRows());
	}

	/**
	 * Asserts that the table contains exactly the given rows in the given order, both as
	 * {@link TableModel#getAllRows()} and {@link TableModel#getDisplayedRows()}.
	 */
	@SuppressWarnings("unchecked")
	protected void assertDisplayedAndAllRows(List<Integer> expectedRows, ObjectTableModel tableModel) {
		assertEquals(expectedRows, tableModel.getDisplayedRows());
		/* It is not possible to check the order here, only the set of elements. The reason is, that
		 * TableModel.getAllRows() specified that there is no guaranteed order. */
		assertEquals(new HashSet<>(expectedRows), new HashSet<>(tableModel.getAllRows()));
	}

	private void assertEventChangeRange(List<TableModelEvent> events, int firstRow, int lastRow) {
		assertEquals(1, events.size());
		TableModelEvent addRowsEvent = events.remove(0);
		assertEquals(firstRow, addRowsEvent.getFirstRow());
		assertEquals(lastRow, addRowsEvent.getLastRow());
	}

	static final class AcceptZeroOrTwoFilter implements Filter<Integer> {

		/** The instance of the {@link AcceptZeroOrTwoFilter} class. */
		public static final AcceptZeroOrTwoFilter INSTANCE = new AcceptZeroOrTwoFilter();

		private int firstMatchingRow = 0;

		private int secondMatchingRow = 2;

		@Override
		public boolean accept(Integer value) {
			return value == firstMatchingRow || value == secondMatchingRow;
		}
	}

}
