/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import test.com.top_logic.layout.TestControl;
import test.com.top_logic.layout.table.TableTestStructure;
import test.com.top_logic.layout.table.TableTestStructure.DeferredLoading;
import test.com.top_logic.layout.table.TableTestStructure.TableControlProvider;
import test.com.top_logic.layout.table.TestingConfigKey;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.DefaultTableData.NoTableDataOwner;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.StaticFilterWrapperConfiguration;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterEvent.FilterEventTypes;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Test case for {@link TableControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTableControl extends TestControl {

	public static final TableControlProvider DEFAULT_TABLE_CONTROL_PROVIDER = new TableControlProvider() {

		@Override
		public TableControl createTableControl(TableData model) {
			DefaultTableRenderer renderer = DefaultTableRenderer.newInstance();
			TableControl tableControl =
				new TableControl(model, renderer);
			return tableControl;
		}

	};

	protected TableTestStructure tableTestStructure;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.tableTestStructure = new TableTestStructure(getTableControlProvider());
	}

	@Override
	protected void tearDown() throws Exception {
		this.tableTestStructure = null;
		super.tearDown();
	}


	/**
	 * Creates the {@link TableControlProvider} for the {@link TableTestStructure}
	 */
	protected TableControlProvider getTableControlProvider() {
		return TestTableControl.DEFAULT_TABLE_CONTROL_PROVIDER;
	}

	interface PersonalConfigModification {
		void modify(TableViewModel viewModel);
	}

	/**
	 * Tests {@link TableControl#addVisibilityListenerFor(FormMember)}.
	 */
	public void testVisibilityInheritance() {
		FormGroup group = new FormGroup("testGroup", ResPrefix.forTest(TestTableControl.class.getSimpleName()));
		FormMember innerField = FormFactory.newBooleanField("innerField");
		group.addMember(innerField);
		TableControl table = tableTestStructure.tableControl;
		table.addVisibilityListenerFor(group);

		writeControl(table);
		assertTrue("Precondition for following tests.", group.isVisible());
		assertTrue("Precondition for following tests.", table.isVisible());

		// Check control inherits visibility
		group.setVisible(false);
		assertFalse("Control becomes invisible if member becomes invisible.", table.isVisible());
		group.setVisible(true);
		assertTrue("Control becomes visible if member becomes invisible.", table.isVisible());
		group.setVisible(false);
		assertFalse("Control becomes invisible if member becomes invisible.", table.isVisible());

		// Check control does not react if detached
		table.detach();
		assertFalse("Precondition for following tests.", table.isVisible());
		writeControl(table);
		assertFalse("Control remains invisible without any change.", table.isVisible());

		// Check control updates state during attachment
		table.detach();
		assertFalse("Precondition for following tests.", table.isVisible());
		group.setVisible(true);
		assertFalse("Control is not attached. No reaction on events.", table.isVisible());
		writeControl(table);
		assertTrue("Control brings state in sync during rendering.", table.isVisible());

		// Check control does not react on inner members
		assertTrue(innerField.isVisible());
		innerField.setVisible(false);
		assertTrue("Control must not react on inner members.", table.isVisible());
	}

	public void testWriteNoModel() {
		writeControl(tableTestStructure.tableControl);
		TableData tableData = tableTestStructure.getTableData();
		TableViewModel oldViewModel = tableData.getViewModel();

		tableData.setTableModel(null);

		boolean before = enableUpdates(false);
		writeControl(tableTestStructure.tableControl);
		disableUpdates(before);

		tableData.setTableModel(oldViewModel);
		writeControl(tableTestStructure.tableControl);
	}

	public void testValidAfterWrite() {
		writeControl(tableTestStructure.tableControl);

		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				// NO special modification
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWritePageSize() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				PagingModel pagingModel = viewModel.getPagingModel();
				int[] pageSizeOptions = pagingModel.getPageSizeOptions();
				pagingModel.changePageSizeSpec(pageSizeOptions[pageSizeOptions.length - 1]);
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWriteColumnWidth() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				viewModel.saveColumnWidth(0, 123);
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWriteFixedColumns() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				viewModel.setPersonalFixedColumns(1);
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWriteTableFilter() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				assertSame("createTableFilters(int) uses viewModel of tableTestStructure",
					tableTestStructure.getViewModel(), viewModel);
				List<ConfiguredFilter> filters = TestTableControl.this.createTableFilters(TableTestStructure.COL_A);
				StaticFilterWrapper filter = (StaticFilterWrapper) filters.get(0);

				TestTableControl.this.activateFilter(filter);
				// PersonalConfiguration is stored automatically when filters are activated
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWriteSortOrder() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				int columnCount = viewModel.getColumnCount();
				assertTrue("No columns in application model", columnCount > 0);
				String columnName = viewModel.getColumnName(0);
				List<SortConfig> newSortOrder =
						Collections.singletonList(SortConfigFactory.sortConfig(columnName, true));
				assertFalse("Sort order will not be changed when setting constructued order",
					newSortOrder.equals(viewModel.getSortOrder()));
				viewModel.setSortOrder(newSortOrder);
				viewModel.saveSortOrder();
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	public void testValidAfterWriteColumnOrder() {
		PersonalConfigModification modificator = new PersonalConfigModification() {
			@Override
			public void modify(TableViewModel viewModel) {
				List<String> revertedcolumnNames = new ArrayList<>(viewModel.getColumnNames());
				Collections.reverse(revertedcolumnNames);
				assertFalse("Column order will not be changed when setting constructued order",
					revertedcolumnNames.equals(viewModel.getColumnNames()));
				try {
					viewModel.setColumns(revertedcolumnNames);
				} catch (VetoException ex) {
					fail("Veto received.", ex);
				}
				viewModel.saveColumnOrder();
			}
		};
		assertValidControlAfterPersonalConfigModification(modificator);
	}

	private void assertValidControlAfterPersonalConfigModification(PersonalConfigModification modificator) {
		modificator.modify(tableTestStructure.getViewModel());
		/* create new tableData to force TableControl to build TableViewModel and load configuration
		 * during rendering */
		ObjectTableModel applicationModel = tableTestStructure.applicationModel;
		TestingConfigKey configKey = tableTestStructure.configKey;
		TableData tableData =
			DefaultTableData.createTableData(NoTableDataOwner.INSTANCE, applicationModel, configKey);
		TableControl tableControl = getTableControlProvider().createTableControl(tableData);
		writeControl(tableControl);
	}

	public void testPaging() {
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
		
		this.executeControlCommand(tableTestStructure.tableControl, TableControl.NEXT_PAGE_COMMAND,
			Collections.<String, Object> emptyMap());
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 1 * TableTestStructure.PAGE_SIZE);
		
		this.executeControlCommand(tableTestStructure.tableControl, TableControl.NEXT_PAGE_COMMAND,
			Collections.<String, Object> emptyMap());
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 2 * TableTestStructure.PAGE_SIZE);
		
		this.executeControlCommand(tableTestStructure.tableControl, TableControl.PREVIOUS_PAGE_COMMAND,
			Collections.<String, Object> emptyMap());
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 1 * TableTestStructure.PAGE_SIZE);
		
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(tableTestStructure.LAST_PAGE);
		assertRowDisplay(tableTestStructure.LAST_PAGE_ROWS, tableTestStructure.LAST_PAGE_FIRST_ROW);
	}
	
	public void testActiveContents() {
		String pathToCContent = td(TableTestStructure.COL_C) + "/div/span/text()";

		// Test first page.
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0, pathToCContent, TableTestStructure.COL_C);
		
		int page4 = 4;
		int firstRowPage4 = page4 * TableTestStructure.PAGE_SIZE;

		int page5 = 5;
		int firstRowPage5 = page5 * TableTestStructure.PAGE_SIZE;
		
		int page6 = 6;
		int firstRowPage6 = page6 * TableTestStructure.PAGE_SIZE;
		
		// Test page 5.
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(page5);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5, pathToCContent, TableTestStructure.COL_C);
		
		// Send row deletion event.
		tableTestStructure.applicationModel.removeRow(0);

		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5, pathToCContent, TableTestStructure.COL_C);
		
		int updatedInvisibleRow = firstRowPage5 - 1;
		tableTestStructure.rowObjects.set(updatedInvisibleRow, Integer.valueOf(42));
		tableTestStructure.applicationModel.updateRows(updatedInvisibleRow, updatedInvisibleRow);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5, pathToCContent, TableTestStructure.COL_C);
		
		int updatedVisibleRow = firstRowPage5 + 1;
		tableTestStructure.rowObjects.set(updatedVisibleRow, Integer.valueOf(13));
		tableTestStructure.applicationModel.updateRows(updatedVisibleRow, updatedVisibleRow);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage5, pathToCContent, TableTestStructure.COL_C);

		// Test display of previously updated page 4.
		tableTestStructure.tableControl.getViewModel().getPagingModel().showPreviousPage();
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage4);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage4, pathToCContent, TableTestStructure.COL_C);
		
		tableTestStructure.rowObjects.set(firstRowPage6, Integer.valueOf(99));
		tableTestStructure.applicationModel.updateRows(firstRowPage6, firstRowPage6);
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(page6);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage6);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage6, pathToCContent, TableTestStructure.COL_C);
	}

	private String td(String column) {
		int index = tableTestStructure.getViewModel().getHeader().getColumn(column).getIndex();
		return "td[" + (index + 1) + "]";
	}

	public void testVisibleUpdate() {
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
		
		tableTestStructure.rowObjects.set(10, Integer.valueOf(42));
		tableTestStructure.applicationModel.updateRows(10, 10);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
	}

	public void testInvisibleUpdate() {
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
		
		int page3 = 3;
		int firstRowPage3 = page3 * TableTestStructure.PAGE_SIZE;
		
		tableTestStructure.rowObjects.set(firstRowPage3, Integer.valueOf(42));
		tableTestStructure.applicationModel.updateRows(firstRowPage3, firstRowPage3);
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(page3);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, firstRowPage3);
	}
	
	public void testDeleteLastRow() {
		doDeleteAllButLastRow();

		tableTestStructure.applicationModel.removeRow(0);
		assertRowDisplay(0, 0);
	}

	public void testDeleteLastSelectedRow() {
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(tableTestStructure.LAST_PAGE);
		TableUtil.selectRow(tableTestStructure.getTableData(), TableTestStructure.ROW_COUNT - 1);
		assertRowDisplay(tableTestStructure.LAST_PAGE_ROWS, tableTestStructure.LAST_PAGE_FIRST_ROW);
		assertSelection(TableTestStructure.ROW_COUNT - 1);

		tableTestStructure.applicationModel.removeRow(TableTestStructure.ROW_COUNT - 1);
		assertRowDisplay(tableTestStructure.LAST_PAGE_ROWS - 1, tableTestStructure.LAST_PAGE_FIRST_ROW);
		
		TableUtil.selectRow(tableTestStructure.getTableData(), tableTestStructure.LAST_PAGE_FIRST_ROW);
		assertRowDisplay("Ticket #999: New selection after deletion last selected row faild.",
			tableTestStructure.LAST_PAGE_ROWS - 1, tableTestStructure.LAST_PAGE_FIRST_ROW);
		assertSelection(tableTestStructure.LAST_PAGE_FIRST_ROW);
	}

	private void doDeleteAllButLastRow() {
		for (int n = TableTestStructure.ROW_COUNT - 1; n > 0; n--) {
			tableTestStructure.applicationModel.removeRow(n);
		}
		assertRowDisplay(1, 0);
	}
	
	public void testDeleteSelectedRowAndSelectNew() {
		int lastRow = TableTestStructure.ROW_COUNT - 1;
		int selectedRowBefore = lastRow;
		tableTestStructure.tableControl.getViewModel().getPagingModel().setPage(tableTestStructure.LAST_PAGE);
		TableUtil.selectRow(tableTestStructure.getTableData(), selectedRowBefore);
		assertRowDisplay(tableTestStructure.LAST_PAGE_ROWS, tableTestStructure.LAST_PAGE_FIRST_ROW);
		assertSelection(selectedRowBefore);
		
		int selectedRowAfter = tableTestStructure.LAST_PAGE_FIRST_ROW;
		// Remove selected row and select another row in one step.
		tableTestStructure.applicationModel.removeRow(lastRow);
		TableUtil.selectRow(tableTestStructure.getTableData(), selectedRowAfter);
		assertRowDisplay(tableTestStructure.LAST_PAGE_ROWS - 1, tableTestStructure.LAST_PAGE_FIRST_ROW);
		assertSelection(selectedRowAfter);
	}
	
	public void testFilter() {
		List<ConfiguredFilter> filters = createTableFilters(TableTestStructure.COL_A);
		StaticFilterWrapper filter = (StaticFilterWrapper) filters.get(0);

		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
		
		activateFilter(filter);
		assertRowDisplay(12, 0);
		
		deactivateFilter(filter);
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
	}

	private void removeAllFilters() {
		TableViewModel viewModel = tableTestStructure.getViewModel();
		viewModel.removeAllFilters();
		viewModel.handleTableFilterEvent(
			new TableFilterEvent(viewModel.getFilter(0),
				FilterEventTypes.DEACTIVATION));
		runValidation();
	}

	private void deactivateFilter(StaticFilterWrapper filter) {
		((StaticFilterWrapperConfiguration) filter.getFilterConfiguration()).setActive(false);
		TableViewModel viewModel = tableTestStructure.getViewModel();
		viewModel.handleTableFilterEvent(
			new TableFilterEvent(viewModel.getFilter(0),
				FilterEventTypes.DEACTIVATION));
	}

	void activateFilter(StaticFilterWrapper filter) {
		((StaticFilterWrapperConfiguration) filter.getFilterConfiguration()).setActive(true);
		TableViewModel viewModel = tableTestStructure.getViewModel();
		viewModel.handleTableFilterEvent(
				new TableFilterEvent(viewModel.getFilter(0),
					FilterEventTypes.CONFIGURATION_UPDATE));
		runValidation();
	}

	public void testRemoveAllFilters() {
		List<ConfiguredFilter> filters = createTableFilters(TableTestStructure.COL_A);
		
		StaticFilterWrapper filter = (StaticFilterWrapper)filters.get(0);
		activateFilter(filter);
		assertRowDisplay(12, 0);

		removeAllFilters();
		assertRowDisplay(TableTestStructure.PAGE_SIZE, 0);
	}
	
	List<ConfiguredFilter> createTableFilters(String column) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);
		StaticFilterWrapper staticFilter1 = new StaticFilterWrapper(DeferredLoading.FILTER_1,
																	new ConstantDisplayValue("Filter1"));
		StaticFilterWrapper staticFilter2 = new StaticFilterWrapper(DeferredLoading.FILTER_2,
																	new ConstantDisplayValue("Filter2"));
		filters.add(staticFilter1);
		filters.add(staticFilter2);
		
		// Creation of table filter infrastructure based on static filters
		PopupDialogModel dialogModel = new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
			new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH),
				DisplayUnit.PIXEL, 100, 70, DisplayUnit.PIXEL, 100,
				Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);
		TableFilter controlFilter = new TableFilter(manager, filters, true);
		
		TableViewModel viewModel = tableTestStructure.getViewModel();
		viewModel.setFilter(viewModel.getHeader().getColumn(column).getIndex(), controlFilter);
		
		return filters;
	}

	
	protected void assertRowDisplay(int expectedRows, int firstExpectedRow) {
		assertRowDisplay(null, expectedRows, firstExpectedRow);
	}
	
	protected void assertRowDisplay(String message, int expectedRows, int firstExpectedRow) {
		assertRowDisplay(message, expectedRows, firstExpectedRow, td(TableTestStructure.COL_B) + "/div/text()",
			TableTestStructure.COL_B);
	}

	private void assertSelection(int expectedRow) {
		Document controlDisplay = getControlDocument();
		
		NodeList selectedRows = selectNodeList(controlDisplay, 
			"//table[" + getClassContainsExpression("tl-table") + "]/tbody/tr["
				+ getClassContainsExpression(DefaultRowClassProvider.SELECTED_CSS_CLASS) + "]/"
				+ td(TableTestStructure.COL_B)
				+ "/div/text()");
		
		if (expectedRow >= 0) {
			assertEquals("Mismatch number of selected rows.", 1, selectedRows.getLength());
			assertEquals(
				value(tableTestStructure.tableControl.getViewModel(), expectedRow, TableTestStructure.COL_B),
				selectedRows.item(0)
				.getNodeValue());
		} else {
			assertEquals("Unexpected selection.", 0, selectedRows.getLength());
		}
	}

	private Object value(TableViewModel model, int row, String columnName) {
		return model.getValueAt(row, model.getHeader().getColumn(columnName).getIndex());
	}

	protected void assertRowDisplay(int expectedRows, int firstExpectedRow, String colSelectXPath, String column) {
		assertRowDisplay(null, expectedRows, firstExpectedRow, colSelectXPath, column);
	}
	
	private String getClassContainsExpression(String clazz) {
		return "contains(concat(' ', @class, ' '), ' " + clazz+ " ')";
	}
	
	protected void assertRowDisplay(String message, int expectedRows, int firstExpectedRow, String colSelectXPath,
			String column) {
		runValidation();
		Document controlDisplay = getControlDocument();
		
		NodeList colBValues = 
			selectNodeList(controlDisplay, 
				"//table[" + getClassContainsExpression("tl-table") + "]/tbody/tr["
					+ getClassContainsExpression(DefaultTableRenderer.TABLE_ROW_CSS_CLASS) + "]/" + colSelectXPath);

		assertEquals(message, expectedRows, colBValues.getLength());
		for (int n = 0, cnt = colBValues.getLength(); n < cnt; n++) {
			String textValue = colBValues.item(n).getNodeValue();
			// Do not use first column for test, because this column
			// additionally contains the selection marker image.
			assertEquals(message, value(tableTestStructure.tableControl.getViewModel(), firstExpectedRow + n, column),
				textValue);
		}
	}

	private NodeList selectNodeList(Document document, String xpath) {
		try {
			return DOMUtil.selectNodeList(document, xpath);
		} 
		
		catch (XPathExpressionException e) {
			throw new AssertionError(e);
		}
	}

	private Document getControlDocument() {
		return getControlDocument(tableTestStructure.tableControl);
	}

	public static Test suite() {
		return TestTableControl.suite(TestTableControl.class);
	}

}
