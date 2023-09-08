/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * Test {@link ObjectTableModel} which has a priority sort order.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPriorityTableModel extends TestObjectTableModel {

	public void testRowOrderPreservationAfterRowMoveAndFilterDeactivation() {
		ObjectTableModel tableModel = createTableModelSimple(Collections.emptyList());

		tableModel.setRowObjects(list(0, 1, 2, 3));
		setFilterZeroOrTwo(tableModel);
		assertDisplayedRows(list(0, 2), tableModel);
		tableModel.moveRow(1, 0);
		assertDisplayedRows(list(2, 0), tableModel);
		setTrueFilter(tableModel);
		assertDisplayedRows(list(2, 0, 1, 3), tableModel);

		tableModel.setRowObjects(list(0, 1, 2, 3));
		setFilterZeroOrTwo(tableModel);
		assertDisplayedRows(list(0, 2), tableModel);
		tableModel.moveRow(0, 1);
		assertDisplayedRows(list(2, 0), tableModel);
		setTrueFilter(tableModel);
		assertDisplayedRows(list(1, 2, 0, 3), tableModel);
	}

	public void testAddRowWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);

		tableModel.addRowObject(4);
		assertDisplayedRows(list(0, 2, 4), tableModel);

		setTrueFilter(tableModel);
		assertDisplayedRows(list(0, 1, 2, 4, 3), tableModel);
	}

	public void testAddMultipleRowsWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);

		tableModel.addAllRowObjects(list(4, 5, 6));
		assertDisplayedRows(list(0, 2, 4, 5, 6), tableModel);

		setTrueFilter(tableModel);
		assertDisplayedRows(list(0, 1, 2, 3, 4, 5, 6), tableModel);
	}

	public void testMoveRow() {
		List<?> rowObjects = createRowObjects(100, 3);
		ObjectTableModel testedModel = createTableModel(SimpleAccessor.INSTANCE, new String[] { "a", "b", "c" });
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();

		int movedRow = 4;

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowDown(movedRow);
		assertEquals("row " + movedRow + " was not moved down", rowObjects.get(movedRow),
			tester.getObservedRows().get(movedRow + 1));

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowDown(rowObjects.size() - 1);
		assertEquals(rowObjects, tester.getObservedRows());

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowUp(movedRow);
		assertEquals("row " + movedRow + " was not moved up", rowObjects.get(movedRow),
			tester.getObservedRows().get(movedRow - 1));

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowUp(0);
		assertEquals(rowObjects, tester.getObservedRows());

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowToBottom(movedRow);
		for (int index = movedRow + 1, size = rowObjects.size(); index < size; index++) {
			assertEquals(rowObjects.get(index), tester.getObservedRows().get(index - 1));
		}
		assertEquals(rowObjects.get(movedRow), tester.getObservedRows().get(tester.getObservedRows().size() - 1));

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowToBottom(rowObjects.size() - 1);
		assertEquals(rowObjects, tester.getObservedRows());

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowToTop(movedRow);
		for (int index = 0, size = movedRow - 1; index < size; index++) {
			assertEquals(rowObjects.get(index), tester.getObservedRows().get(index + 1));
		}
		assertEquals(rowObjects.get(movedRow), tester.getObservedRows().get(0));

		testedModel.setRowObjects(rowObjects);
		testedModel.moveRowToTop(0);
		assertEquals(rowObjects, tester.getObservedRows());
	}

	public void testMoveRow2() {
		List<Integer> tableRows = list(0, 1, 2, 3, 4);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);
		setFilter(new SetFilter(set(0, 1, 2, 3)), ObjectTableModel.DEFAULT_ORDER, tableModel);

		assertDisplayedRows(list(0, 1, 2, 3), tableModel);
		tableModel.moveRow(3, 0);
		assertDisplayedRows(list(3, 0, 1, 2), tableModel);

		setFilter(FilterFactory.trueFilter(), ObjectTableModel.DEFAULT_ORDER, tableModel);
		assertDisplayedRows(list(3, 0, 1, 2, 4), tableModel);
		tableModel.moveRow(4, 3);
		assertDisplayedRows(list(3, 0, 1, 4, 2), tableModel);
		tableModel.moveRow(3, 3);
		assertEquals("No op move.", list(3, 0, 1, 4, 2), tableModel.getDisplayedRows());
		tableModel.moveRowDown(1);
		assertDisplayedRows(list(3, 1, 0, 4, 2), tableModel);
		tableModel.moveRowUp(2);
		assertDisplayedRows(list(3, 0, 1, 4, 2), tableModel);
		tableModel.moveRowToTop(2);
		assertDisplayedRows(list(1, 3, 0, 4, 2), tableModel);
		tableModel.moveRowToBottom(2);
		assertDisplayedRows(list(1, 3, 4, 2, 0), tableModel);
	}

	public void testModelWithNoRows() {
		ObjectTableModel testedModel = createTableModel(SimpleAccessor.INSTANCE, new String[] { "a", "b", "c" });
		ObjectTableModelTester tester = new ObjectTableModelTester(testedModel);
		tester.attach();

		testedModel.clear();
	}

	public void testClearComparatorsOfConfiguration() {
		String columnA = "a";
		String columnB = "b";
		TableConfiguration tableConfig = TableConfigurationFactory.table();
		tableConfig.getDefaultColumn().setComparator(ComparableComparator.INSTANCE);
		tableConfig.declareColumn(columnA).setComparator(ComparableComparator.INSTANCE);
		tableConfig.declareColumn(columnB);
		ObjectTableModel testedModel = new ObjectTableModel(new String[] { columnA, columnB }, tableConfig,
			Collections.emptyList(), createPriorityTable());

		TableConfiguration modifiedConfiguration = testedModel.getTableConfiguration();
		assertEmptyComparator(modifiedConfiguration.getDefaultColumn());
		assertEmptyComparator(modifiedConfiguration.getDeclaredColumn(columnA));
		assertEmptyComparator(modifiedConfiguration.getDeclaredColumn(columnB));
	}

	private void assertEmptyComparator(ColumnConfiguration defaultColumn) {
		assertNull("Comparator must be null!", defaultColumn.getComparator());
	}

	@Override
	protected boolean createPriorityTable() {
		return true;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestPriorityTableModel.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
