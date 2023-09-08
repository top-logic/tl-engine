/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.renderer;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.TableTestStructure;
import test.com.top_logic.layout.table.control.TestTableControl;

import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.renderer.PagePaneProvider;

/**
 * Tests for {@link PagePaneProvider}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestPagePaneProvider extends AbstractLayoutTest {

	private TableTestStructure tableTestStructure;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tableTestStructure = new TableTestStructure(TestTableControl.DEFAULT_TABLE_CONTROL_PROVIDER);
	}

	public void testUndefinedPaneForEmptyTableModel() {
		createNoRowTableModel();

		assertUndefinedPagePane();
	}

	public void testCompleteVisiblePaneRequestIfPaneFullyOnPage() {
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(5, 10));

		assertPaneRowRange(5, 10);
	}

	public void testUndefinedPaneIfPaneNotOnPage() {
		pagingSetup(20, 1);
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(5, 10));

		assertUndefinedPagePane();
	}

	public void testFirstPageRowIfPaneOverlapWithPreviousPage() {
		pagingSetup(20, 1);
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(18, 25));

		assertPaneRowRange(20, 25);
	}

	public void testLastPageRowIfPaneOverlapWithNextPage() {
		pagingSetup(20, 0);
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(18, 25));

		assertPaneRowRange(18, 19);
	}

	public void testDefinedForcedVisibleRowIfOnPage() {
		int forcedVisibleRow = 22;
		pagingSetup(20, 1);
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(18, 25, forcedVisibleRow));

		assertPaneRowRange(20, 25, forcedVisibleRow);
	}

	public void testFirstPanePageRowIfDefinedForcedVisibleNotOnPage() {
		int forcedVisibleRow = 22;
		pagingSetup(20, 0);
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(18, 25, forcedVisibleRow));

		assertPaneRowRange(18, 19, 18);
	}

	private void assertUndefinedPagePane() {
		assertPaneRowRange(-1, -1);
	}

	private void assertPaneRowRange(int firstRow, int lastRow) {
		assertPaneRowRange(firstRow, lastRow, firstRow);
	}

	private void assertPaneRowRange(int firstRow, int lastRow, int forcedVisibleRow) {
		IndexRange rowRange = PagePaneProvider.getPane(getViewModel()).getRowRange();

		assertEquals("First row of page pane is incorrect!", firstRow, rowRange.getFirstIndex());
		assertEquals("Last row of page pane is incorrect!", lastRow, rowRange.getLastIndex());
		assertEquals("Forced visible row of page pane is incorrect!", forcedVisibleRow,
			rowRange.getForcedVisibleIndexInRange());
	}

	private VisiblePaneRequest getVisiblePaneRequest() {
		return getViewModel().getClientDisplayData().getVisiblePaneRequest();
	}

	private TableViewModel getViewModel() {
		return tableTestStructure.getViewModel();
	}

	private void pagingSetup(int pageSize, int page) {
		getViewModel().getPagingModel().setPageSizeOptions(pageSize);
		getViewModel().getPagingModel().setPageSizeSpec(pageSize);
		getViewModel().getPagingModel().setPage(page);
		runValidation();
	}

	private void createNoRowTableModel() {
		int startRow = 0;
		int stopRow = tableTestStructure.applicationModel.getRowCount();
		tableTestStructure.applicationModel.removeRows(startRow, stopRow);

		runValidation();
	}

	public static Test suite() {
		return suite(TestPagePaneProvider.class);
	}

}
