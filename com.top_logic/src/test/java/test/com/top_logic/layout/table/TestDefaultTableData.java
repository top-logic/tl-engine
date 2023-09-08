/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import static test.com.top_logic.layout.table.TableModelTestScenario.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.collections4.CollectionUtils;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DelegatingCommandModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataListener;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.renderer.TableButtons.ConfigureColumnsCommand;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link TestCase} for {@link DefaultTableData}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultTableData extends AbstractLayoutTest {

	final class TestTableDataListener implements TableDataListener {

		TableViewModel eventOldViewModel;

		@Override
		public void notifyTableViewModelChanged(TableData source, TableViewModel oldValue, TableViewModel newValue) {
			eventOldViewModel = oldValue;
		}

		@Override
		public void notifySelectionModelChanged(TableData source, SelectionModel oldValue, SelectionModel newValue) {
			throw new UnsupportedOperationException();
		}

		public TableViewModel getEventViewModel() {
			return eventOldViewModel;
		}
	}

	/**
	 * Listener that records all selection changes.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	private class SelectionRecorder implements SelectionListener {

		private int _calledCount;

		private List<Pair<Set<?>, Set<?>>> _selectionChanges;

		SelectionRecorder() {
			_calledCount = 0;
			_selectionChanges = new ArrayList<>();
		}

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			_calledCount++;
			_selectionChanges.add(new Pair<>(oldSelection, newSelection));
		}

		/**
		 * Returns the number of times this listener was called.
		 */
		public int getCalledCount() {
			return _calledCount;
		}

		/**
		 * Returns all selection changes.
		 */
		public List<Pair<Set<?>, Set<?>>> getSelectionChanges() {
			return _selectionChanges;
		}
	}

	public void testUseExistingOldViewModelForEvent() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TestTableDataListener eventListener = new TestTableDataListener();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		tableData.addListener(TableData.VIEW_MODEL_PROPERTY, eventListener);

		TableViewModel firstViewModel = tableData.getViewModel();

		tableData.setTableModel(scenario.createTableModel());

		assertEquals("DefaultTableData do not provide existent old TableViewModel!", firstViewModel,
			eventListener.getEventViewModel());
	}

	public void testShowColumnConfigurationDialog() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		assertContainsColumnConfigCommand(tableData);
	}

	public void testSelectionListener() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		Object firstRow = CollectionUtil.getFirst(tableData.getViewModel().getAllRows());

		SelectionRecorder selectionRecorder = new SelectionRecorder();

		SelectionModel selectionModel = tableData.getSelectionModel();
		selectionModel.addSelectionListener(selectionRecorder);
		Set<?> oldSelection = selectionModel.getSelection();
		Set<?> newSelection = Collections.singleton(firstRow);

		selectionModel.setSelection(newSelection);

		assertEquals(1, selectionRecorder.getCalledCount());
		List<Pair<Set<?>, Set<?>>> selectionChanges = selectionRecorder.getSelectionChanges();
		Pair<Set<?>, Set<?>> selectionChange = CollectionUtils.extractSingleton(selectionChanges);
		assertEquals(oldSelection, selectionChange.getFirst());
		assertEquals(newSelection, selectionChange.getSecond());

		selectionModel.removeSelectionListener(selectionRecorder);
	}

	public void testUndefinedVisiblePaneRequestAfterDeselection() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		Object firstRow = CollectionUtil.getFirst(tableData.getViewModel().getAllRows());

		SelectionModel selectionModel = tableData.getSelectionModel();
		selectionModel.setSelection(Collections.singleton(firstRow));
		selectionModel.clear();

		IndexRange actualRange = tableData.getViewModel().getClientDisplayData().getVisiblePaneRequest().getRowRange();
		assertEquals("Visible pane must be undefined after row deselection!", IndexRange.undefined(), actualRange);
	}

	public void testRowIsInVisiblePaneRequestAfterSelection() throws IOException {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		TableViewModel viewModel = tableData.getViewModel();
		Object firstRow = CollectionUtil.getFirst(viewModel.getAllRows());

		SelectionModel selectionModel = tableData.getSelectionModel();
		selectionModel.setSelection(Collections.singleton(firstRow));

		int selectedRow = viewModel.getRowOfObject(firstRow);
		TableControl tableControl = new TableControl(tableData, null);
		tableControl.write(displayContext(), new TagWriter());
		runValidation();

		IndexRange range = viewModel.getClientDisplayData().getVisiblePaneRequest().getRowRange();
		assertEquals("Selected row must be part of the visible pane!", selectedRow, range.getFirstIndex());
		assertEquals("Selected row must be part of the visible pane!", selectedRow, range.getLastIndex());

		tableControl.detach();
	}

	public void testEmptyTableModelRemovesSelection() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		TableViewModel viewModel = tableData.getViewModel();
		Object firstRow = CollectionUtil.getFirst(viewModel.getAllRows());

		SelectionModel selectionModel = tableData.getSelectionModel();
		selectionModel.setSelection(Collections.singleton(firstRow));

		((EditableRowTableModel) viewModel.getApplicationModel()).setRowObjects(Collections.emptyList());
		assertEquals(Collections.emptySet(), selectionModel.getSelection());
	}

	public void testSelectionOfNotDisplayedRowShouldChangeDisplayedPage() throws IOException {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableData tableData = DefaultTableData.createAnonymousTableData(scenario.createTableModel());
		TableViewModel viewModel = tableData.getViewModel();

		SelectionModel selectionModel = tableData.getSelectionModel();

		Object row1 = viewModel.getRowObject(0);
		Object row3 = viewModel.getRowObject(2);

		viewModel.getPagingModel().setPageSizeOptions(2);

		selectionModel.setSelection(Collections.singleton(row1));
		int startPage = viewModel.getPagingModel().getPage();

		selectionModel.setSelection(Collections.singleton(row3));
		TableControl tableControl = new TableControl(tableData, null);
		tableControl.write(displayContext(), new TagWriter());

		int newPage = viewModel.getPagingModel().getPage();

		assertNotEquals("Selection of row, which is not part of the current page, should change displayed page!",
			startPage, newPage);

		tableControl.detach();
	}

	public void testHideColumnConfigurationDialogSingleColumnOnly() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager(new String[] { C0 });
		TableData tableData =
			DefaultTableData.createAnonymousTableData(
				scenario.createTableModel(tableConfiguration, new String[] { C0 }));
		assertNoColumnConfigCommand(tableData);
	}

	public void testHideColumnConfigurationDialogSingleSelectableColumnOnly() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager(new String[] { C0, C1, C2 });
		tableConfiguration.getDeclaredColumn(C1).setVisibility(DisplayMode.excluded);
		tableConfiguration.getDeclaredColumn(C2).setVisibility(DisplayMode.mandatory);
		TableData tableData =
			DefaultTableData.createAnonymousTableData(
				scenario.createTableModel(tableConfiguration, new String[] { C0, C1, C2 }));
		// Column config dialog is still required, since the non-mandatory column C0 could be
		// de-selected.
		assertColumnConfigCommand(tableData);
	}

	public void testHideColumnConfigurationDialogAllColumnsMandatory() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager(new String[] { C1, C2 });
		tableConfiguration.getDeclaredColumn(C1).setVisibility(DisplayMode.mandatory);
		tableConfiguration.getDeclaredColumn(C2).setVisibility(DisplayMode.mandatory);
		TableData tableData =
			DefaultTableData.createAnonymousTableData(
				scenario.createTableModel(tableConfiguration, new String[] { C1, C2 }));
		// Column config dialog is still required, since the order of the two mandatory columns C1
		// and C2 could be changed.
		assertColumnConfigCommand(tableData);
	}

	public void testHideColumnConfigurationDialogAllColumnsExcluded() {
		TableModelTestScenario scenario = new TableModelTestScenario();
		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager(new String[] { C0, C1, C2 });
		tableConfiguration.getDeclaredColumn(C1).setVisibility(DisplayMode.excluded);
		tableConfiguration.getDeclaredColumn(C2).setVisibility(DisplayMode.excluded);
		TableData tableData =
			DefaultTableData.createAnonymousTableData(
				scenario.createTableModel(tableConfiguration, new String[] { C0, C1, C2 }));
		assertNoColumnConfigCommand(tableData);
	}

	private void assertContainsColumnConfigCommand(TableData tableData) {
		assertTrue(hasColumnConfigCommand(tableData));
	}

	private boolean hasColumnConfigCommand(TableData tableData) {
		initToolbarButtons(tableData);
		ToolBarGroup columnConfigGroup = tableData.getToolBar().getGroup(CommandHandlerFactory.TABLE_CONFIG_GROUP);
		if (columnConfigGroup != null) {
			for (HTMLFragment fragment : columnConfigGroup.getViews()) {
				if (fragment instanceof ButtonControl) {
					CommandModel model = ((ButtonControl) fragment).getModel();
					if (model instanceof DelegatingCommandModel) {
						if (((DelegatingCommandModel) model).unwrap() instanceof ConfigureColumnsCommand) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private void assertNoColumnConfigCommand(TableData tableData) {
		assertFalse(hasColumnConfigCommand(tableData));
	}

	private void assertColumnConfigCommand(TableData tableData) {
		assertTrue(hasColumnConfigCommand(tableData));
	}

	private void initToolbarButtons(TableData tableData) {
		tableData.getViewModel();
	}

	public static Test suite() {
		return suite(TestDefaultTableData.class);
	}
}
