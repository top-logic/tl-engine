/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * Test case for {@link Header}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestHeader extends TestCase {

	/**
	 * Tests the problem that the columns have not a valid index when they are fetched. Reason is,
	 * that computation of indexes does not occur when column is fetched.
	 */
	public void testColumnByName() {
		TableConfiguration config = TableConfigurationFactory.table();

		Header header = new Header(config, list("col-a", "col-b"), true);

		Column colA = header.getColumn("col-a");
		Column colB = header.getColumn("col-b");
		assertSame("Ticket #17624: ", colA.getIndex(), header.getColumns().indexOf(colA));
		assertSame("Ticket #17624: ", colB.getIndex(), header.getColumns().indexOf(colB));
	}

	public void testColumnVisibility() {
		testColumnVisibility(DisplayMode.mandatory, false);
		testColumnVisibility(DisplayMode.visible, false);
		testColumnVisibility(DisplayMode.hidden, false);
		testColumnVisibility(DisplayMode.excluded, true);
	}

	private void testColumnVisibility(DisplayMode displayMode, boolean excluded) {
		TableConfiguration config = TableConfigurationFactory.table();
		String columnName = "col";
		ColumnConfiguration mandatoryColumnConfig = config.declareColumn(columnName);
		mandatoryColumnConfig.setVisibility(displayMode);
		Header header = new Header(config, list(columnName), false);

		boolean visible = displayMode.isDisplayed();

		Column column = header.getColumn(columnName);
		assertEquals(visible, column.isVisible());
		assertEquals(excluded, column.isExcluded());

		column.setVisible(!visible);
		assertEquals(!visible & !column.isExcluded(), column.isVisible());
		column.setVisible(visible);
		assertEquals(visible & !column.isExcluded(), column.isVisible());

		column.setExcluded(!excluded);
		assertEquals(!excluded, column.isExcluded());
		assertEquals(visible & !column.isExcluded(), column.isVisible());
		column.setExcluded(excluded);
		assertEquals(excluded, column.isExcluded());
		assertEquals(visible & !column.isExcluded(), column.isVisible());
	}

	public void testUndeclaredColumns() {
		TableConfiguration config = TableConfigurationFactory.table();
		config.getDefaultColumn().setVisible(false);
		ColumnConfiguration colA = config.declareColumn("col-a");
		colA.setVisible(true);

		Header header = new Header(config, list("col-a", "col-b"), false);

		assertEquals(columnsWithDefaults("col-a", "col-b"), names(header.getAllElementaryColumns()));
		assertColumnExistance(header, "col-a", "col-b");
		assertVisibleColumns(header, "col-a");

		header.setVisibleColumns(list("col-a", "col-b"));
		assertVisibleColumns(header, "col-a", "col-b");
	}

	public void testUndeclaredColumnsAllVisible() {
		TableConfiguration config = TableConfigurationFactory.table();
		config.getDefaultColumn().setVisible(false);
		ColumnConfiguration colA = config.declareColumn("col-a");
		colA.setVisible(true);

		Header header = new Header(config, list("col-a", "col-b"), true);

		assertEquals(columnsWithDefaults("col-a", "col-b"), names(header.getAllElementaryColumns()));
		assertColumnExistance(header, "col-a", "col-b");
		assertVisibleColumnsWithDefaults(header, "col-a", "col-b");

		header.setVisibleColumns(list("col-b"));
		assertVisibleColumns(header, "col-b");
	}

	public void testMergedColumnsAllVisible() {
		TableConfiguration config = TableConfigurationFactory.table();
		config.getDefaultColumn().setVisible(false);
		config.declareColumn("col-c");

		Header header = new Header(config, list("col-a", "col-b"), true);

		assertEquals(columnsWithDefaults("col-a", "col-b", "col-c"), names(header.getAllElementaryColumns()));
		assertColumnExistance(header, "col-a", "col-b", "col-c");
		assertVisibleColumnsWithDefaults(header, "col-a", "col-b", "col-c");
	}

	public void testMergeDefaultTableColumns() {
		String selectColumnName = "_select";
		DisplayMode hiddenMode = DisplayMode.hidden;
		TableConfiguration config = TableConfigurationFactory.table();
		ColumnConfiguration incrementalSelectColumnConfiguration = config.declareColumn(selectColumnName);
		incrementalSelectColumnConfiguration.setVisibility(hiddenMode);

		Header header = new Header(config, list("col-a", "col-b"), false);

		Column selectColumn = header.getColumn(selectColumnName);
		ColumnConfiguration selectColumnConfig = selectColumn.getConfig();
		assertFalse(selectColumn.isVisible());
		assertFalse(selectColumnConfig.isShowHeader());
		assertFalse(selectColumnConfig.isSortable());
		assertNull(selectColumnConfig.getFilterProvider());
	}

	private void assertVisibleColumns(Header header, String... columnNames) {
		assertEquals(list(columnNames), names(header.getColumns()));
		assertEquals(list(columnNames), header.getColumnNames());
	}

	private void assertVisibleColumnsWithDefaults(Header header, String... columnNames) {
		assertEquals(columnsWithDefaults(columnNames), names(header.getColumns()));
		assertEquals(columnsWithDefaults(columnNames), header.getColumnNames());
	}

	public static List<String> columnsWithDefaults(String... columnNames) {
		ArrayList<String> result =
			new ArrayList<>(columnNames.length + TableConfiguration.defaultTable().getDeclaredColumns().size());
		for (ColumnConfiguration name : TableConfiguration.defaultTable().getDeclaredColumns()) {
			result.add(name.getName());
		}
		result.addAll(Arrays.asList(columnNames));
		return result;
	}

	private void assertColumnExistance(Header header, String... columnNames) {
		for (String columnName : columnNames) {
			assertEquals(columnName, header.getColumn(columnName).getName());
		}
	}

	private static List<String> names(List<Column> columns) {
		ArrayList<String> result = new ArrayList<>(columns.size());
		for (Column column : columns) {
			result.add(column.getName());
		}
		return result;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestHeader.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
