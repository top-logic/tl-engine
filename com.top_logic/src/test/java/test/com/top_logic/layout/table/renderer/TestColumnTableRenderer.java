/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.renderer;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import test.com.top_logic.layout.TestControl;
import test.com.top_logic.layout.table.TableTestStructure;
import test.com.top_logic.layout.table.control.TestTableControl;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableRenderer.RenderState;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl.Slice;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Test case for {@link DefaultTableRenderer}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class TestColumnTableRenderer extends TestControl {

	private TableRenderer _tableRenderer;
	private DefaultUpdateQueue updateQueue;

	private TableTestStructure tableTestStructure;

	private Slice firstSlice;
	private Slice secondSlice;
	private Slice thirdSlice;

	private RenderState state;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initFragmentUpdateQueue();
		initTableControlWithFrozenRenderer();
		createFirstOpenSlice();
	}

	private void initFragmentUpdateQueue() {
		this.updateQueue = new DefaultUpdateQueue();
	}

	private void initTableControlWithFrozenRenderer() {
		tableTestStructure = new TableTestStructure(TestTableControl.DEFAULT_TABLE_CONTROL_PROVIDER);
		tableTestStructure.tableControl.getTableData().getViewModel().getTableConfiguration()
			.setFixedColumnCount(getFrozenColumnCount());
		_tableRenderer = DefaultTableRenderer.newInstance();
		tableTestStructure.tableControl.setRenderer(_tableRenderer);
		state = new DefaultTableRenderer.DefaultRenderState(_tableRenderer, tableTestStructure.tableControl);
	}

	protected abstract int getFrozenColumnCount();

	private void createFirstOpenSlice() {
		// First slice must be created by control write
		writeControl(tableTestStructure.tableControl);
		firstSlice = getViewModel().getOpenSlice();
	}

	private void createSecondOpenSlice() {
		secondSlice = new Slice(20, 39);
		registerSliceAsVisible(secondSlice);
	}

	private void createThirdOpenSlice() {
		thirdSlice = new Slice(40, 49);
		registerSliceAsVisible(thirdSlice);
	}

	private void markAsNewClientRequest(Slice slice) {
		getViewModel().pushSliceRequest(slice);
	}

	private void registerSliceAsVisible(Slice slice) {
		getViewModel().addSlice(slice);
		getViewModel().checkDisplayedRows(0, slice.getLastRow());
	}

	private boolean hasFixedAndFlexTableParts() {
		return (getFrozenColumnCount() > 0) && (getFrozenColumnCount() < getViewModel().getColumnCount());
	}

	public void testSingleLineUpdateInFirstSlice() {
		createSecondOpenSlice();
		_tableRenderer.addUpdateActions(updateQueue, state, 0, 0);

		assertHTMLFragmentUpdateCount(1);
	}

	public void testSingleLineUpdateInSecondSlice() {
		createSecondOpenSlice();
		_tableRenderer.addUpdateActions(updateQueue, state, 20, 20);

		assertHTMLFragmentUpdateCount(1);
	}

	public void testCrossTwoSlicesLineUpdates() {
		createSecondOpenSlice();
		_tableRenderer.addUpdateActions(updateQueue, state, 10, 25);

		assertHTMLFragmentUpdateCount(1);
	}

	public void testSliceRequest() {
		createSecondOpenSlice();
		createThirdOpenSlice();
		markAsNewClientRequest(thirdSlice);

		_tableRenderer.addUpdateActions(updateQueue, state,
													thirdSlice.getFirstRow(), thirdSlice.getLastRow());

		assertHTMLFragmentUpdateCount(1);
	}

	private void assertHTMLFragmentUpdateCount(int expectedFragmentCount) {
		assertEquals(expectedFragmentCount, getActualFragmentCount());
	}

	private int getActualFragmentCount() {
		return updateQueue.getStorage().size();
	}

	protected TableTestStructure getTableStructure() {
		return tableTestStructure;
	}

	public void testFirstPageRowIsFirstRowInInitialWriteIfNoRowIsSelected() {
		assertRowDisplay("First rendered row is not first page row!", TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT,
			getViewModel()
			.getPagingModel().getFirstRowOnCurrentPage());
	}

	public void testSelectedRowIsInMiddleInInitialWrite() {
		int selectedRow = 410;
		tableTestStructure.tableControl.setSelectedRow(selectedRow);

		double renderedRowCountLeft = TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT - 1;
		int renderedRowsBefore = (int) Math.ceil(renderedRowCountLeft / 2);
		int firstRenderedRow = selectedRow - renderedRowsBefore;
		assertRowDisplay("Selected row is not in the middle of the rendered slice!", TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT,
			firstRenderedRow);
	}

	public void testSliceContainsInitialRowCountIfSelectedRowIsNearPageEnd() {
		int lastPageRow = 19;

		tableTestStructure.tableControl.setSelectedRow(lastPageRow);

		int firstRenderedRow = lastPageRow - TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT + 1;
		assertRowDisplay("Initial Slice should contain initial row count!",
			TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT,
			firstRenderedRow);
	}

	public void testSliceShouldBeEmptyIfTableHasNoRows() {
		tableTestStructure.applicationModel.clear();
		assertRowDisplay("Slice should be empty, if table has no rows!", 0,
			0);
	}

	private TableViewModel getViewModel() {
		return tableTestStructure.getViewModel();
	}

	private void assertRowDisplay(String message, int expectedRows, int firstExpectedRow) {
		assertRowDisplay(message, expectedRows, firstExpectedRow, "/div/text()", TableTestStructure.COL_B);
	}

	private void assertRowDisplay(String message, int expectedRows, int firstExpectedRow, String tableCellValueXPath,
			String column) {
		runValidation();
		Document controlDisplay = getControlDocument(tableTestStructure.tableControl);

		NodeList colBValues =
			selectNodeList(controlDisplay,
				getPathToSecondColumn() + tableCellValueXPath);

		assertEquals(message, expectedRows, colBValues.getLength());
		for (int n = 0, cnt = colBValues.getLength(); n < cnt; n++) {
			String textValue = colBValues.item(n).getNodeValue();
			// Do not use first column for test, because this column
			// additionally contains the selection marker image.
			assertEquals(message, tableTestStructure.applicationModel.getValueAt(firstExpectedRow + n, column),
				textValue);
		}
	}

	private String getPathToSecondColumn() {
		return "//table/tbody/tr/" + td(TableTestStructure.COL_B);
	}

	private String td(String column) {
		TableViewModel viewModel = getTableStructure().getViewModel();

		int index = viewModel.getColumnIndex(column);

		if (index >= viewModel.getFixedColumnCount()) {
			// if the given column is flexible add one for the column that separates the fixed
			// and flexible area.
			index++;
		}

		return "td[" + (1 + index) + "]";
	}

	private NodeList selectNodeList(Document document, String xpath) {
		try {
			return DOMUtil.selectNodeList(document, xpath);
		}

		catch (XPathExpressionException e) {
			throw new AssertionError(e);
		}
	}
}
