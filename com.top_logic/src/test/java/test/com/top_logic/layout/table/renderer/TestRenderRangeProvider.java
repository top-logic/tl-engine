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
import com.top_logic.layout.table.display.ClientDisplayData;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.renderer.RenderRange;
import com.top_logic.layout.table.renderer.RenderRangeProvider;

/**
 * Tests for {@link RenderRangeProvider}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestRenderRangeProvider extends AbstractLayoutTest {

	private TableTestStructure tableTestStructure;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tableTestStructure = new TableTestStructure(TestTableControl.DEFAULT_TABLE_CONTROL_PROVIDER);
	}

	public void testEmptyRenderRangeForNoRowsModel() {
		createNoRowTableModel();

		assertRange(-1, -1);
	}

	public void testFirstPageRowForUndefinedRangeIfMaxInitRowCountLessThanPageSize() {
		int firstExpectedRow = 0;
		int lastExpectedRow = getDefaultRowCount() - 1;

		assertRangeAndInitRowCount(firstExpectedRow, lastExpectedRow);
	}

	public void testFirstPageRowForUndefinedRangeIfMaxInitRowCountGreaterThanPageSize() {
		pagingSetup(getDefaultRowCount() - 1, 0);
		int firstExpectedRow = 0;
		int lastExpectedRow = getViewModel().getPagingModel().getLastRowOnCurrentPage();

		assertRange(firstExpectedRow, lastExpectedRow);
	}

	public void testFirstPaneRowIfPaneEqualsToMaxInitRowCount() {
		pagingSetup(30, 0);

		int firstExpectedRow = 10;
		int lastExpectedRow = firstExpectedRow + getDefaultRowCount() - 1;

		setPaneRows(firstExpectedRow, lastExpectedRow);

		assertRangeAndInitRowCount(firstExpectedRow, lastExpectedRow);
	}

	public void testEvenRowPaneInMiddleIfLessThanMaxInitRowCount() {
		pagingSetup(30, 0);

		int firstPaneRow = 10;
		int lastPaneRow = firstPaneRow + getEvenRowCount() - 1;

		setPaneRows(firstPaneRow, lastPaneRow);

		assertPaneMiddlePosition(firstPaneRow, lastPaneRow);
	}

	public void testUnevenRowPaneInMiddleIfLessThanMaxInitRowCount() {
		pagingSetup(30, 0);

		int firstPaneRow = 9;
		int lastPaneRow = firstPaneRow + getEvenRowCount();

		setPaneRows(firstPaneRow, lastPaneRow);

		assertPaneMiddlePosition(firstPaneRow, lastPaneRow);
	}

	public void testDisplayPageTopIfPaneOverlapsWithPreviousPageAndHasLessThanMaxInitRowCount() {
		pagingSetup(30, 1);
		int firstPaneRow = 28;
		int lastPaneRow = firstPaneRow + getEvenRowCount();

		setPaneRows(firstPaneRow, lastPaneRow);

		int firstRenderedRow = getViewModel().getPagingModel().getFirstRowOnCurrentPage();
		int lastRenderedRow = firstRenderedRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	public void testDisplayPageTopIfPaneOverlapsWithNextPageAndHasLessThanMaxInitRowCount() {
		pagingSetup(30, 0);
		int firstPaneRow = 28;
		int lastPaneRow = firstPaneRow + getEvenRowCount();

		setPaneRows(firstPaneRow, lastPaneRow);

		int firstRenderedRow =
			getViewModel().getPagingModel().getLastRowOnCurrentPage() - getDefaultRowCount() + 1;
		int lastRenderedRow = getViewModel().getPagingModel().getLastRowOnCurrentPage();
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}
	
	public void testForcedVisibleRowMostlyMiddleIfPaneGreaterThanInitialRowCount() {
		pagingSetup(50, 0);
		int firstPaneRow = 10;
		int lastPaneRow = firstPaneRow + getDefaultRowCount() + 10;
		int forcedVisibleRowNearMiddle = firstPaneRow + ((lastPaneRow - firstPaneRow) / 2);

		setPaneRows(firstPaneRow, lastPaneRow, forcedVisibleRowNearMiddle);

		assertForcedVisibleRowPosition(forcedVisibleRowNearMiddle);
	}

	public void testForcedVisibleRowNearUpperBoundIfPaneGreaterThanInitialRowCount() {
		pagingSetup(50, 0);
		int firstPaneRow = 10;
		int lastPaneRow = firstPaneRow + getDefaultRowCount() + 10;
		int forcedVisibleRowNearUpperBound = firstPaneRow + 1;

		setPaneRows(firstPaneRow, lastPaneRow, forcedVisibleRowNearUpperBound);

		assertForcedVisibleRowPosition(forcedVisibleRowNearUpperBound);
	}

	public void testForcedVisibleRowNearLowerBoundIfPaneGreaterThanInitialRowCount() {
		pagingSetup(50, 0);
		int firstPaneRow = 10;
		int lastPaneRow = firstPaneRow + getDefaultRowCount() + 10;
		int forcedVisibleRowNearLowerBound = lastPaneRow - 1;

		setPaneRows(firstPaneRow, lastPaneRow, forcedVisibleRowNearLowerBound);

		assertForcedVisibleRowPosition(forcedVisibleRowNearLowerBound);
	}

	public void testFirstPageRowForForcedVisibleRowBeforeCurrentPageAndIfPaneGreaterThanInitialRowCount() {
		pagingSetup(50, 1);
		int firstPaneRow = 48;
		int lastPaneRow = firstPaneRow + getDefaultRowCount() + 15;
		int forcedVisibleRow = 48;

		setPaneRows(firstPaneRow, lastPaneRow, forcedVisibleRow);
		
		int firstRenderedRow = getViewModel().getPagingModel().getFirstRowOnCurrentPage();
		int lastRenderedRow = firstRenderedRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	public void testForcedVisibleRowAfterCurrentPageAndIfPaneGreaterThanInitialRowCount() {
		pagingSetup(50, 0);
		int firstPaneRow = 15;
		int lastPaneRow = firstPaneRow + 50;
		int forcedVisibleRow = 55;

		setPaneRows(firstPaneRow, lastPaneRow, forcedVisibleRow);

		int firstRenderedRow = firstPaneRow;
		int lastRenderedRow = firstPaneRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	public void testRenderScrollPositionIfVisiblePaneUndefined() {
		pagingSetup(50, 0);

		setPaneRows(-1, -1, -1);
		setRowScrollPosition(15);

		int firstRenderedRow = 15;
		int lastRenderedRow = firstRenderedRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	public void testNotRenderScrollPositionIfVisiblePaneDefined() {
		pagingSetup(50, 0);
		int firstExpectedRow = 10;
		int lastExpectedRow = firstExpectedRow + getDefaultRowCount() - 1;

		setPaneRows(firstExpectedRow, lastExpectedRow);
		setRowScrollPosition(44);

		assertRangeAndInitRowCount(firstExpectedRow, lastExpectedRow);
	}

	private int getDefaultRowCount() {
		return TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT;
	}

	private int getPaneRowCount(int firstPaneRow, int lastPaneRow) {
		return lastPaneRow - firstPaneRow + 1;
	}

	private void createNoRowTableModel() {
		int startRow = 0;
		int stopRow = tableTestStructure.applicationModel.getRowCount();
		tableTestStructure.applicationModel.removeRows(startRow, stopRow);

		runValidation();
	}

	private int getEvenRowCount() {
		return 4;
	}

	private void setPaneRows(int firstRow, int lastRow) {
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(firstRow, lastRow));
	}

	private void setPaneRows(int firstRow, int lastRow, int forcedVisibleRow) {
		getVisiblePaneRequest().setRowRange(IndexRange.multiIndex(firstRow, lastRow, forcedVisibleRow));
	}

	private VisiblePaneRequest getVisiblePaneRequest() {
		return getClientDisplayData().getVisiblePaneRequest();
	}

	private ClientDisplayData getClientDisplayData() {
		return getViewModel().getClientDisplayData();
	}

	private void setRowScrollPosition(int visibleRow) {
		getViewportState().setRowAnchor(RowIndexAnchor.create(visibleRow));
	}

	private ViewportState getViewportState() {
		return getClientDisplayData().getViewportState();
	}

	private int getRowCountRequestBeforePane(int paneRowCount) {
		int initialRowsLeft = getDefaultRowCount() - paneRowCount;
		return (int) Math.floor((double) initialRowsLeft / 2);
	}

	private void assertPaneMiddlePosition(int firstPaneRow, int lastPaneRow) {
		int paneRowCount = getPaneRowCount(firstPaneRow, lastPaneRow);
		int renderedRowCountBeforePane = getRowCountRequestBeforePane(paneRowCount);
		int firstRenderedRow = firstPaneRow - renderedRowCountBeforePane;
		int lastRenderedRow = firstRenderedRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	private void assertForcedVisibleRowPosition(int forcedVisibleRowNearMiddle) {
		int renderedRowsBeforePane = (int) Math.floor(((double) getDefaultRowCount() - 1) / 2);
		int firstRenderedRow =
			Math.max(forcedVisibleRowNearMiddle - renderedRowsBeforePane,
					getVisiblePaneRequest().getRowRange().getFirstIndex());
		int lastRenderedRow = firstRenderedRow + getDefaultRowCount() - 1;
		assertRangeAndInitRowCount(firstRenderedRow, lastRenderedRow);
	}

	private void assertRangeAndInitRowCount(int firstExpectedRow, int lastExpectedRow) {
		RenderRange renderRange = RenderRangeProvider.createRange(getViewModel());
		assertEquals("The initial row count must be written!", getDefaultRowCount(),
			renderRange.getLastRow() - renderRange.getFirstRow() + 1);
		assertRange(firstExpectedRow, lastExpectedRow);
	}

	private void assertRange(int firstExpectedRow, int lastExpectedRow) {
		RenderRange renderRange = RenderRangeProvider.createRange(getViewModel());
		assertEquals("First row of render range is incorrect!", firstExpectedRow, renderRange.getFirstRow());
		assertEquals("Last row of render range is incorrect!", lastExpectedRow, renderRange.getLastRow());
	}

	private void pagingSetup(int pageSize, int page) {
		getViewModel().getPagingModel().setPageSizeOptions(pageSize);
		getViewModel().getPagingModel().setPageSizeSpec(pageSize);
		getViewModel().getPagingModel().setPage(page);
		runValidation();
	}

	private TableViewModel getViewModel() {
		return tableTestStructure.getViewModel();
	}

	public static Test suite() {
		return suite(TestRenderRangeProvider.class);
	}
}
