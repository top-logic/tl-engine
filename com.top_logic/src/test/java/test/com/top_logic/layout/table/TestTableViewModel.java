/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import static com.top_logic.layout.DisplayUnit.*;
import static com.top_logic.layout.table.SortConfigFactory.*;
import static test.com.top_logic.layout.table.TableModelTestScenario.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Icons;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.treetable.component.SilentVetoException;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.ColumnChangeEvt;
import com.top_logic.layout.table.ColumnChangeVetoListener;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.control.TableControl.Slice;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.StaticFilterWrapperConfiguration;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterEvent.FilterEventTypes;
import com.top_logic.layout.table.filter.TableFilterListener;
import com.top_logic.layout.table.model.ArrayTableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;

/**
 * Test case for {@link TableViewModel}.
 */
public class TestTableViewModel extends AbstractLayoutTest {

	private static final String PERSONAL_CONFIG_KEY_FOR_TEST_MODEL = "testTableViewModel";

	private static final String MAPPED_VALUES_SUFFIX = "-Mapped";

	private static final TestingConfigKey MODEL_CONFIG_KEY = new TestingConfigKey("testing-key");

	private TableModelTestScenario scenario;

	protected EditableRowTableModel applicationModel;

	protected TableViewModel model;
	
	private StaticFilterWrapper globalStaticFilter;
	private StaticFilterWrapper staticFilter1;
	private StaticFilterWrapper staticFilter2;

	private FailingTableFilter failingTableFilter;


	public TestTableViewModel(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.scenario = new TableModelTestScenario();
		this.applicationModel = createApplicationModel(scenario);
		this.model = createTableViewModel(applicationModel);
	}

	@Override
	protected void tearDown() throws Exception {
		deactivateFilters();
		this.applicationModel = null;
		this.model = null;

		super.tearDown();
	}

	private void deactivateFilters() {
		deactivateFirstFilterOfGlobalFilter();
		deactivateFirstFilterOfFirstColumn();
		deactivateSecondFilterOfFirstColumn();
		deactivateErrorProducingFilter();
	}

	private void restoreDefaultColumns() {
		try {
			model.setColumns(Arrays.asList(C0, C1, C2, C3));
		} catch (VetoException ex) {
			// Ignore
		}
	}

	public void testVetoListener() {
		final ArrayList<String> reversed = new ArrayList<>(Arrays.asList(scenario.getColumnNames()));
		Collections.reverse(reversed);
		class VetoListener implements ColumnChangeVetoListener {
			boolean veto = true;
			@Override
			public void checkVeto(ColumnChangeEvt evt) throws VetoException {
				assertEquals(reversed, evt.newValue());
				assertEquals(model, evt.source());
				if (veto) {
					throw new SilentVetoException();
				}
			}
		}
		VetoListener vetoListener = new VetoListener();
		model.setColumnChangeVetoListener(vetoListener);

		class Listener implements TableModelListener {

			TableModelEvent evt = null;

			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				if (event.getType() == TableModelEvent.COLUMNS_UPDATE) {
					evt = event;
				}
			}
		}

		Listener listener = new Listener();
		model.addTableModelListener(listener);

		try {
			model.setColumns(reversed);
			fail("Veto expected.");
		} catch (VetoException ex) {
			// Ignore.
		}
		assertNull(listener.evt);

		vetoListener.veto = false;
		try {
			model.setColumns(reversed);
		} catch (VetoException ex) {
			fail("Veto received.", ex);
		}
		assertNotNull(listener.evt);
	}

	public void testGlobalTableProperties() {
		assertEquals(20, model.getPagingModel().getPageSizeSpec());

		int[] expectedPageSizeOptions = { 20, 40, 0 };
		assertTrue(ArrayUtil.equals(expectedPageSizeOptions, model.getPagingModel().getPageSizeOptions()));
	}

	public void testSetValidPageSize() {
		model.getPagingModel().setPageSizeSpec(40);
		assertEquals(40, model.getPagingModel().getPageSizeSpec());
	}

	public void testSetInvalidPageSize() {
		model.getPagingModel().setPageSizeSpec(100);
		assertEquals(20, model.getPagingModel().getPageSizeSpec());
	}

	public void testLoadInvalidPageSize() {
		createInvalidPageSizeInPersonalConfiguration();

		model.getPagingModel().loadPageSize();
		assertEquals(20, model.getPagingModel().getPageSizeSpec());

		removeInvalidPageSizeFromPersonalConfiguration();
	}

	private void createInvalidPageSizeInPersonalConfiguration() {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		config.setValue(PERSONAL_CONFIG_KEY_FOR_TEST_MODEL + "pageSize", 100);
	}

	private void removeInvalidPageSizeFromPersonalConfiguration() {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		config.setValue(PERSONAL_CONFIG_KEY_FOR_TEST_MODEL + "pageSize", null);
	}

	public void testChangeCausingPageSizeOptions() {
		int[] testOptions = { 20, 80, 150 };
		model.getPagingModel().setPageSizeOptions(testOptions);
		assertEquals(20, model.getPagingModel().getPageSizeSpec());
	}

	public void testColumnMoveIdentity1() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(2, true);
		model.moveColumn(2, 2);

		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		assertEquals(2, model.getSortedApplicationModelColumn());
	}
	
	public void testColumnMoveIdentity2() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(2, true);
		model.moveColumn(1, 2);
		
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		assertEquals(2, model.getSortedApplicationModelColumn());
	}
	
	public void testColumnMoveLeft() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(1, true);
		model.moveColumn(2, 1);
		
		assertRowValues(0, new Object[] {C0_A, C2_A, C1_A, C3_A});
		assertEquals(1, model.getSortedApplicationModelColumn());
		assertEquals(2, model.getSortedColumn());
	}

	public void testColumnMoveRight() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(3, true);
		model.moveColumn(1, 3);
		
		assertRowValues(0, new Object[] { C0_B, C2_E, C1_B, null });
		assertEquals(3, model.getSortedApplicationModelColumn());
		assertEquals(3, model.getSortedColumn());
	}
	
	public void testColumnMoveToFirst() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(1, true);
		model.moveColumn(1, 0);
		
		assertRowValues(0, new Object[] {C1_A, C0_A, C2_A, C3_A});
		assertEquals(1, model.getSortedApplicationModelColumn());
		assertEquals(0, model.getSortedColumn());
	}
	
	public void testColumnMoveToLast() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(2, true);
		model.moveColumn(1, 4);

		assertRowValues(0, new Object[] {C0_A, C2_A, C3_A, C1_A});
		assertEquals(2, model.getSortedApplicationModelColumn());
		assertEquals(1, model.getSortedColumn());
	}
	
	public void testColumnMoveFirstToLast() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(3, true);
		model.moveColumn(0, 4);

		assertRowValues(0, new Object[] { C1_B, C2_E, null, C0_B });
		assertEquals(3, model.getSortedApplicationModelColumn());
		assertEquals(2, model.getSortedColumn());
	}
	
	public void testColumnMoveLastToFirst() throws VetoException {
		assertRowValues(0, new Object[] {C0_A, C1_A, C2_A, C3_A});
		
		model.setSortedApplicationModelColumn(0, true);
		model.moveColumn(3, 0);
		
		assertRowValues(0, new Object[] {C3_A, C0_A, C1_A, C2_A});
		assertEquals(0, model.getSortedApplicationModelColumn());
		assertEquals(1, model.getSortedColumn());
	}
	
	public void testTwoColumnMoveRight() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(0, 1, 3);
		assertRowValues(0, new Object[] { C2_A, C0_A, C1_A, C3_A });
	}

	public void testThreeColumnMoveRight() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(0, 2, 4);
		assertRowValues(0, new Object[] { C3_A, C0_A, C1_A, C2_A });
	}

	public void testMultiColumnMoveLeft() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(2, 3, 1);
		assertRowValues(0, new Object[] { C0_A, C2_A, C3_A, C1_A });
	}

	public void testMultiColumnMoveToFirst() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(1, 2, 0);
		assertRowValues(0, new Object[] { C1_A, C2_A, C0_A, C3_A });
	}

	public void testMultiColumnMoveToLast() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(1, 2, 4);
		assertRowValues(0, new Object[] { C0_A, C3_A, C1_A, C2_A });
	}

	public void testMultiColumnMoveFirstToLast() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(0, 1, 4);
		assertRowValues(0, new Object[] { C2_A, C3_A, C0_A, C1_A });
	}

	public void testMultiColumnMoveLastToFirst() throws VetoException {
		assertRowValues(0, new Object[] { C0_A, C1_A, C2_A, C3_A });
		model.moveColumns(2, 3, 0);
		assertRowValues(0, new Object[] { C2_A, C3_A, C0_A, C1_A });
	}

	public void testColumnFilter() {
		doColumnFilterTest(C0_A);
	}

	public void testSaveAndLoadColumnFilter() {
		int countBeforeFiltering = 7;
		int countAfterFiltering = 3;
		assertEquals(countBeforeFiltering, model.getRowCount());
		activateFirstFilterOfFirstColumn();
		assertEquals(countAfterFiltering, model.getRowCount());

		setFilter(FilterFactory.trueFilter(), Equality.INSTANCE, applicationModel);
		assertEquals(countBeforeFiltering, model.getRowCount());

		model = createTableViewModel(applicationModel);
		assertEquals(countAfterFiltering, model.getRowCount());
	}

	public void testGlobalFilter() {
		int countBeforeFiltering = 7;
		assertEquals(countBeforeFiltering, model.getRowCount());

		activateFirstFilterOfGlobalFilter();

		int countAfterFiltering = 2;
		assertEquals(countAfterFiltering, model.getRowCount());

		for (int i = 0; i < countAfterFiltering; i++) {
			String column1Value = (String) model.getValueAt(i, 1);
			assertEquals(C1_A, column1Value);
			String column3Value = (String) model.getValueAt(i, 3);
			assertEquals(C3_A, column3Value);
		}
	}

	public void testSaveAndLoadGlobalFilter() {
		int countBeforeFiltering = 7;
		int countAfterFiltering = 2;
		assertEquals(countBeforeFiltering, model.getRowCount());
		activateFirstFilterOfGlobalFilter();
		assertEquals(countAfterFiltering, model.getRowCount());

		setFilter(FilterFactory.trueFilter(), Equality.INSTANCE, applicationModel);
		assertEquals(countBeforeFiltering, model.getRowCount());

		model = createTableViewModel(applicationModel);
		assertEquals(countAfterFiltering, model.getRowCount());
	}

	private void activateFirstFilterOfGlobalFilter() {
		((StaticFilterWrapperConfiguration) globalStaticFilter.getFilterConfiguration()).setActive(true);
		fireGlobalFilterUpdate();
	}

	private void deactivateFirstFilterOfGlobalFilter() {
		((StaticFilterWrapperConfiguration) globalStaticFilter.getFilterConfiguration()).setActive(false);
		fireGlobalFilterUpdate();
	}

	private void activateFirstFilterOfFirstColumn() {
		activateSubFilterOfFirstColumn((StaticFilterWrapperConfiguration) staticFilter1.getFilterConfiguration());
	}

	private void deactivateFirstFilterOfFirstColumn() {
		deactivateSubFilterOfFirstColumn((StaticFilterWrapperConfiguration) staticFilter1.getFilterConfiguration());
	}

	private void activateSubFilterOfFirstColumn(StaticFilterWrapperConfiguration filterConfig) {
		filterConfig.setActive(true);
		fireFilterUpdateForColumn(0);
	}

	private void deactivateSubFilterOfFirstColumn(StaticFilterWrapperConfiguration filterConfig) {
		filterConfig.setActive(false);
		fireFilterUpdateForColumn(0);
	}

	private void fireFilterUpdateForColumn(int filterApplicationColumn) {
		model.handleTableFilterEvent(new TableFilterEvent(model.getFilter(filterApplicationColumn),
			FilterEventTypes.CONFIGURATION_UPDATE));
		runValidation();
	}

	private void fireGlobalFilterUpdate() {
		model.handleTableFilterEvent(new TableFilterEvent(model.getGlobalFilter(),
			FilterEventTypes.CONFIGURATION_UPDATE));
		runValidation();
	}

	public void testColumnFilterWithMappedValues() {
		setUpMappedValuesModel();

		doColumnFilterTest(C0_A + MAPPED_VALUES_SUFFIX);
	}

	private void doColumnFilterTest(String expectedCellValue) {
		activateFirstFilterOfFirstColumn();
		int count = model.getRowCount();
		assertEquals(3, count);
		for (int i = 0; i < count; i++) {
			String theString = (String) model.getValueAt(i, 0);
			assertEquals(expectedCellValue, theString);
		}
	}

	private void setUpMappedValuesModel() {
		applicationModel = createMappedValuesTableModel();
		model = createMappedValuesTableViewModel(applicationModel);
	}

	/**
	 * Test for {@link TableViewModel#adjustFiltersForRow(Object)}.
	 */
	public void testAdjustFiltersFor() {
		doFilterAdjustTests(C0_A, C0_B);
	}

	public void testAdjustFiltersForWithMappedValues() {
		setUpMappedValuesModel();
		doFilterAdjustTests(C0_A + MAPPED_VALUES_SUFFIX, C0_B + MAPPED_VALUES_SUFFIX);
	}

	private void doFilterAdjustTests(String visibleCellValue, String invisibleCellValue) {
		activateFirstFilterOfFirstColumn();
		model.adjustFiltersForRow(scenario.getRows().get(0)); // No need to adjust anything here
		assertEquals(3, applicationModel.getRowCount());
		assertEquals(3, model.getRowCount());
		for (int n = 0, cnt = model.getRowCount(); n < cnt; n++) {
			String theString = (String) model.getValueAt(n, 0);
			assertEquals(visibleCellValue, theString);
		}
		model.adjustFiltersForRow(scenario.getRows().get(6));
		assertEquals(7, applicationModel.getRowCount());
		assertEquals(7, model.getRowCount());
	}

	public void testSort() {
		doSortTests("");
	}

	public void testSortWithMappedValues() {
		setUpMappedValuesModel();
		doSortTests(MAPPED_VALUES_SUFFIX);
	}

	private void doSortTests(String cellValueSuffix) {
		sort(sortConfig(C2, false));
		assertColumnValues(2, new Object[] { C2_G + cellValueSuffix,
												C2_F + cellValueSuffix,
												C2_E + cellValueSuffix,
												C2_D + cellValueSuffix,
												C2_C + cellValueSuffix,
												C2_B + cellValueSuffix,
												C2_A + cellValueSuffix });

		sort(sortConfig(C0, false), sortConfig(C1, true));
		assertColumnValues(2, new Object[] { C2_D + cellValueSuffix,
												C2_E + cellValueSuffix,
												C2_F + cellValueSuffix,
												C2_G + cellValueSuffix,
												C2_A + cellValueSuffix,
												C2_B + cellValueSuffix,
												C2_C + cellValueSuffix });

		sort(sortConfig(C0, false), sortConfig(C1, false));
		assertColumnValues(2, new Object[] { C2_G + cellValueSuffix,
												C2_F + cellValueSuffix,
												C2_E + cellValueSuffix,
												C2_D + cellValueSuffix,
												C2_C + cellValueSuffix,
												C2_B + cellValueSuffix,
												C2_A + cellValueSuffix });

		sort(sortConfig(C2, true));
		assertColumnValues(2, new Object[] { C2_A + cellValueSuffix,
												C2_B + cellValueSuffix,
												C2_C + cellValueSuffix,
												C2_D + cellValueSuffix,
												C2_E + cellValueSuffix,
												C2_F + cellValueSuffix,
												C2_G + cellValueSuffix });

		sort(sortConfig(C0, true), sortConfig(C3, true), sortConfig(C1, false));
		assertColumnValues(2, new Object[] { C2_C + cellValueSuffix,
												C2_A + cellValueSuffix,
												C2_B + cellValueSuffix,
												C2_E + cellValueSuffix,
												C2_F + cellValueSuffix,
												C2_D + cellValueSuffix,
												C2_G + cellValueSuffix });
	}

	public void testSetEmptySortOrder() {
		sort(sortConfig(C2, false));
		sort(sortConfig(null, false));
		assertEmpty("Empty sort order was requested but not set!", true, model.getSortOrder());
	}

	public void testSingleSortEventCountNoFormerSort() {
		TableEventDetector eventDetector = new TableEventDetector(model);

		markCommandThreadBegin();
		sort(sortConfig(C2, false));
		markCommandThreadEnd();

		assertSortEventCount(eventDetector);
	}

	public void testSingleSortEventCountWithFormerSort() {
		sort(sortConfig(C0, false));
		TableEventDetector eventDetector = new TableEventDetector(model);

		markCommandThreadBegin();
		sort(sortConfig(C2, false));
		markCommandThreadEnd();

		assertSortEventCount(eventDetector);
	}

	public void testMultiSortEventCount() {
		TableEventDetector eventDetector = new TableEventDetector(model);

		markCommandThreadBegin();
		sort(sortConfig(C0, true), sortConfig(C2, false));
		markCommandThreadEnd();

		assertSortEventCount(eventDetector);
	}

	private void assertSortEventCount(TableEventDetector eventDetector) {
		assertEquals("Count of detected sort events does not meet expected value.", 1,
			eventDetector.getSortEventCount());
		assertEquals("Count of detected invalidate events does not meet expected value.", 1,
			eventDetector.getInvalidateEventCount());
		assertEquals("Count of overall detected events does not meet expected value.", 2,
			eventDetector.getOverallEventCount());
	}

	private void sort(SortConfig... columns) {
		model.setSortOrder(Arrays.asList(columns));
	}

	public void testStoreAndLoadColumnPermutation() throws VetoException {
		TableViewModel tempViewModelReference = createTableViewModel(applicationModel);
		
		// Exchange columns to following order ----> C3, C1, C0, C2
		tempViewModelReference.moveColumn(0, 2);
		tempViewModelReference.moveColumn(3, 0);
		assertRowValues(0, new Object[] {C3_A, C1_A, C0_A, C2_A}, tempViewModelReference);
		
		// Store column permutation
		tempViewModelReference.saveColumnOrder();
		
		// Test if column permutation will be fully restored, if size and structure of application
		// columns have not been changed
		TableViewModel tempViewModel = createTableViewModel(applicationModel);
		assertRowValues(0, new Object[] {C3_A, C1_A, C0_A, C2_A}, tempViewModel);
		
		// Test if column permutation will be restored in case the current application model
		// contains fewer columns than the previous one used by this table.
		// That means, that the current set of application columns is a subset of former
		// set of application columns.
		// Previous columns were: C0, C1, C2, C3 ---> former permutation: C3, C1, C0, C2
		// Current columns are: C0, C2, C3 ---> expected permutation: C3, C0, C2
		TableModel tempApplicationModel = createReducedTableModel();
		tempViewModel = createTableViewModel(tempApplicationModel);
		assertRowValues(0, new Object[] {C3_A, C0_A, C2_A}, tempViewModel);
		
		// Test if column permutation will be restored in case the current application model
		// contains more columns than the previous one used by this table.
		// Previous columns were: C0, C1, C2, C3 ---> former permutation: C3, C1, C0, C2
		// Current columns are: C0, C2, CX, C3, C4 ---> expected permutation: C3, C0, C2, CX, C4
		tempViewModelReference.saveColumnOrder();
		tempApplicationModel = createExtendedTableModel();
		tempViewModel = createTableViewModel(tempApplicationModel);
		assertRowValues(0, new Object[] { C3_A, C0_A, C2_A, CX_A, C4_A }, tempViewModel);
	}
	
	private void deactivateSecondFilterOfFirstColumn() {
		deactivateSubFilterOfFirstColumn((StaticFilterWrapperConfiguration) staticFilter2.getFilterConfiguration());
	}

	public void testErrorLoggingInCaseTableFilterErrorOccured() {
		CollectingLogListener logListener = new CollectingLogListener(Collections.singleton(Level.WARN), true);
		Logger.getListenerRegistry().register(logListener);

		activateErrorProducingFilter();
		
		assertWarningInLog(logListener);
	}

	public void testFilterUpdateEventByLoadingFilterSettings() {
		activateFirstFilterOfFirstColumn();
		TableEventDetector detector = new TableEventDetector(model);
		model.loadConfiguration();

		assertEquals(
			"View model fires event in case persistent filter settings had been restored! This also happens if loading is actually a noop.",
						1, detector.getFilterEventCount());
	}

	public void testSortedSingleRowInsert() {
		int colC2 = model.getColumnNames().indexOf(C2);
		model.sort(colC2, true);
		runValidation();

		assertEquals(C2_A, model.getValueAt(0, colC2));
		assertEquals(C2_G, model.getValueAt(model.getRowCount() - 1, colC2));

		applicationModel.insertRowObject(0, scenario.row("X", "X", "X", "X"));
		assertEquals("X", model.getValueAt(0, colC2));

		int lastRow = applicationModel.getRowCount();
		applicationModel.insertRowObject(lastRow, scenario.row("A", "A", "A", "A"));
		assertEquals("A", model.getValueAt(lastRow, colC2));
	}

	public void testApplyConfiguredSortOrderForEmptyConfigKey() throws Exception {
		TableConfiguration tableConfig = TableConfiguration.table();
		String[] columnNames = scenario.getColumnNames();
		scenario.createColumnConfigsAndInitAccessors(tableConfig, columnNames);
		List<SortConfig> expectedSortConfig = Collections.singletonList(sortConfig(columnNames[0], true));
		tableConfig.setDefaultSortOrder(expectedSortConfig);
		TableModel applicationModel = scenario.createTableModel(tableConfig);
		TableViewModel viewModel = new TestingTableViewModel(applicationModel, ConfigKey.none());

		List<SortConfig> actualSortConfig = viewModel.getSortOrder();
		SortConfig sortConfig = expectedSortConfig.get(0);
		assertEquals(
			"Expected list of sort configurations has not the same size as actual list of sort configurations",
			expectedSortConfig.size(), actualSortConfig.size());
		assertEquals("Wrong column is sorted!", sortConfig.getColumnName(), actualSortConfig.get(0).getColumnName());
		assertEquals("Column has a wrong sort direction!", sortConfig.getAscending(), actualSortConfig.get(0)
			.getAscending());
	}

	public void testVisibleRangeIsEqualToPageRangeForNoRowsTableModel() throws Exception {
		applicationModel.clear();
		TableViewModel tableViewModel = new TestingTableViewModel(applicationModel);

		assertPageRange(tableViewModel);
	}

	public void testVisibleRangeIsEqualToSliceRange() throws Exception {
		int firstVisibleRow = 10;
		int lastVisibleRow = 50;
		model.addSlice(new Slice(firstVisibleRow, 20));
		model.addSlice(new Slice(21, lastVisibleRow));

		assertRange(firstVisibleRow, lastVisibleRow);
	}

	public void testFirstVisibleRowIsDeterminedBySliceRequest() throws Exception {
		int firstVisibleRowBefore = 10;
		int firstVisibleRowAfter = 0;
		int lastVisibleRow = 50;
		model.addSlice(new Slice(firstVisibleRowBefore, lastVisibleRow));
		model.pushSliceRequest(new Slice(firstVisibleRowAfter, firstVisibleRowBefore - 1));

		assertRange(firstVisibleRowAfter, lastVisibleRow);
	}

	public void testLastVisibleRowIsDeterminedBySliceRequest() throws Exception {
		int firstVisibleRow = 10;
		int lastVisibleRowBefore = 50;
		int lastVisibleRowAfter = 70;
		model.addSlice(new Slice(firstVisibleRow, lastVisibleRowBefore));
		model.pushSliceRequest(new Slice(lastVisibleRowBefore + 1, lastVisibleRowAfter));

		assertRange(firstVisibleRow, lastVisibleRowAfter);
	}

	public void testVisibleRangeIsEqualToSliceRequest() throws Exception {
		int firstRow = 10;
		int lastRow = 50;
		model.pushSliceRequest(new Slice(firstRow, lastRow));
		assertRange(firstRow, lastRow);
	}

	public void testVisibleRangeIsEqualToFirstRowSliceRequest() throws Exception {
		int firstRow = 0;
		int lastRow = 0;
		model.pushSliceRequest(new Slice(firstRow, lastRow));
		assertRange(firstRow, lastRow);
	}

	public void testFixedColumnsSmallerThanColumnAmount() throws Exception {
		int fixedColumns = 0;
		model.setPersonalFixedColumns(fixedColumns);
		assertEquals(fixedColumns, model.getFixedColumnCount());
		assertEquals(fixedColumns, model.getPersonalFixedColumns());
	}

	public void testFixedColumnsEqualToColumnAmount() throws Exception {
		int fixedColumns = model.getColumnCount();
		model.setPersonalFixedColumns(fixedColumns);
		assertEquals(fixedColumns, model.getFixedColumnCount());
		assertEquals(fixedColumns, model.getPersonalFixedColumns());
	}

	public void testFixedColumnsGreaterThanColumnAmount() throws Exception {
		int fixedColumns = model.getColumnCount() + 1;
		model.setPersonalFixedColumns(fixedColumns);
		assertEquals(model.getColumnCount(), model.getFixedColumnCount());
		assertEquals(fixedColumns, model.getPersonalFixedColumns());
	}

	public void testFullRestoreColumnOrder() throws Exception {
		model.setColumns(Arrays.asList(C1, C2));
		assertCurrentColumnNames(C1, C2);

		applicationModel = createApplicationModel(scenario);
		model = createTableViewModel(applicationModel);
		assertCurrentColumnNames(C1, C2);
		restoreDefaultColumns();
	}

	public void testRestoreColumnOrderWithoutExcludedColumns() throws Exception {
		model.setColumns(Arrays.asList(C1, C2));
		assertCurrentColumnNames(C1, C2);

		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager();
		tableConfiguration.declareColumn(C1).setVisibility(DisplayMode.excluded);
		applicationModel = scenario.createTableModel(tableConfiguration);
		model = createTableViewModel(applicationModel);
		assertCurrentColumnNames(C2);
		restoreDefaultColumns();
	}

	public void testRestoreColumnOrderWithMandatoryColumns() throws Exception {
		model.setColumns(Arrays.asList(C1, C2));
		assertCurrentColumnNames(C1, C2);

		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager();
		tableConfiguration.declareColumn(C0).setVisibility(DisplayMode.mandatory);
		applicationModel = scenario.createTableModel(tableConfiguration);
		model = createTableViewModel(applicationModel);
		assertCurrentColumnNames(C1, C2, C0);
		restoreDefaultColumns();
	}

	public void testResetTableSettings() throws Exception {
		int adjustedColumn = 0;
		int defaultPageSize = 5;

		model.setColumns(Arrays.asList(C0, C2));
		model.saveColumnWidth(adjustedColumn, 2000);
		model.setPersonalFixedColumns(10);
		activateFirstFilterOfFirstColumn();
		model.setSortOrder(Collections.singletonList(SortConfigFactory.sortConfig(C2, false)));
		model.getPagingModel().setPageSizeOptions(defaultPageSize, 10);
		model.getPagingModel().setPageSizeSpec(10);

		model.resetToConfiguration();

		assertEquals(Arrays.asList(C0, C1, C2, C3), model.getColumnNames());
		assertEquals(TableViewModel.NO_COLUMN_WIDTH_PERSONALIZATION, model.getProgrammaticColumnWidth(adjustedColumn));
		assertEquals(TableViewModel.NO_FIXED_COLUMN_PERSONALIZATION, model.getPersonalFixedColumns());
		assertFalse(model.hasActiveFilters());
		assertEquals(0, model.getSortColumnCount());
		assertEquals(defaultPageSize, model.getPagingModel().getCurrentPageSize());
	}

	public void testResetFilter() {
		int allRows = 7;
		int c0aRows = 3;

		assertEquals(allRows, model.getRowCount());

		activateFirstFilterOfFirstColumn();
		assertEquals("Setting filter was not successful!", c0aRows, model.getRowCount());

		model.resetFilter(0);
		assertEquals("Resetting filter was not successful!", allRows, model.getRowCount());
	}

	private void assertCurrentColumnNames(String... expectedColumn) {
		assertColumnNames(model.getColumnNames(), expectedColumn);
	}

	private void assertColumnNames(List<String> actualColumns, String... expectedColumn) {
		String[] expectedColumns = expectedColumn;
		assertEquals(expectedColumns.length, actualColumns.size());
		for (int i = 0; i < expectedColumn.length; i++) {
			String expected = expectedColumns[i];
			String actual = actualColumns.get(i);
			assertEquals(expected, actual);
		}
	}

	private void assertPageRange(TableViewModel tableViewModel) {
		PagingModel pagingModel = tableViewModel.getPagingModel();
		assertRange(tableViewModel, pagingModel.getFirstRowOnCurrentPage(), pagingModel.getLastRowOnCurrentPage());
	}

	private void assertRange(int firstRow, int lastRow) {
		assertRange(model, firstRow, lastRow);
	}

	private void assertRange(TableViewModel tableViewModel, int expectedFirstRow, int expectedLastRow) {
		assertEquals("First displayed row does not match expectation!", expectedFirstRow,
			tableViewModel.getFirstDisplayedRow());
		assertEquals("Last displayed row does not match expectation!", expectedLastRow,
			tableViewModel.getLastDisplayedRow());
	}

	private void assertWarningInLog(CollectingLogListener logListener) {
		String expectedLogMessage =
			"An error occured processing table filter '" + C1 + "' of table model '"
				+ model.getConfigKey().get() + "'.";
		for (LogEntry logEntry : logListener.getLogEntries()) {
			if (logEntry.getMessage().equals(expectedLogMessage)) {
				return;
			}
		}
		
		fail("Log message of failing table filter was not detected");
	}

	private void activateErrorProducingFilter() {
		failingTableFilter.setActive();
		fireFilterUpdateForColumn(1);
	}

	private void deactivateErrorProducingFilter() {
		failingTableFilter.setInactive();
		fireFilterUpdateForColumn(1);
	}

	private void assertRowValues(int row, Object[] objects) {
		assertRowValues(row, objects, model);
	}
	
	private void assertRowValues(int row, Object[] objects, TableViewModel model) {
		runValidation();
		for (int column = 0, cnt = model.getColumnCount(); column < cnt; column++) {
			assertEquals("Mismatch at row " + row + ", column " + column + ".", 
					objects[column], model.getValueAt(row, column));
		}
	}

	private void assertColumnValues(int col, Object[] objects) {
		runValidation();
		for (int row = 0, cnt = model.getRowCount(); row < cnt; row++) {
			assertEquals("Mismatch at row " + row + ", column " + col + ".", 
					objects[row], model.getValueAt(row, col));
		}
	}

	public static void main(String[] args) {
		// SHOW_TIME = true; // for debugging
		KBSetup.setCreateTables(false); // for debugging

		Logger.configureStdout();

		TestRunner.run(suite());
	}

	private void markCommandThreadBegin() {
		changeUpdateState(true);
	}

	private void changeUpdateState(boolean enableUpdate) {
		((SubsessionHandler) displayContext().getLayoutContext()).enableUpdate(enableUpdate);
	}

	private void markCommandThreadEnd() {
		changeUpdateState(false);
	}

	private EditableRowTableModel createApplicationModel(TableModelTestScenario scenario) {
		TableConfiguration tableConfiguration = scenario.createColumnDescriptionManager();
		createColumnComparators(tableConfiguration);
		createTableFilters(tableConfiguration);
		return scenario.createTableModel(tableConfiguration);
	}

	private TableViewModel createTableViewModel(TableModel applicationModel) {
		TableViewModel viewModel = new TestingTableViewModel(applicationModel);
		return viewModel;
	}
	
	private void createColumnComparators(TableConfiguration tableConfiguration) {
		for (ColumnConfiguration columnConfiguration : tableConfiguration.getDeclaredColumns()) {
			columnConfiguration.setComparator(ComparableComparator.INSTANCE);
		}
	}

	private void createTableFilters(TableConfiguration tableConfiguration) {
		createGlobalFilter(tableConfiguration);
		createFilterForFirstColumn(tableConfiguration);
		createFilterForSecondColumn(tableConfiguration);
	}

	private void createGlobalFilter(TableConfiguration tableConfiguration) {
		tableConfiguration.setFilterProvider(new TableFilterProvider() {

			@Override
			public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {
				// TODO STS Automatically created
				globalStaticFilter = new StaticFilterWrapper(new Filter<Object>() {

					@Override
					public boolean accept(Object rowObject) {
						TableConfiguration tableConfiguration = applicationModel.getTableConfiguration();
						ColumnConfiguration column1 = tableConfiguration.getDeclaredColumn(C1);
						ColumnConfiguration column3 = tableConfiguration.getDeclaredColumn(C3);
						return column1.getAccessor().getValue(rowObject, C1).equals(C1_A) &&
							column3.getAccessor().getValue(rowObject, C3).equals(C3_A);
					}
				}, new ConstantDisplayValue("GLOBAL_FILTER"));

				FilterDialogBuilder manager = getFilterDialogManager();
				TableFilter controlFilter =
					new TableFilter(manager, Collections.singletonList(globalStaticFilter), true);
				return controlFilter;
			}
		});
	}

	private void createFilterForFirstColumn(TableConfiguration tableConfiguration) {
		tableConfiguration.getDeclaredColumn(C0).setFilterProvider(new TableFilterProvider() {

			@Override
			public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {
				List<ConfiguredFilter> filters = new ArrayList<>(2);
				staticFilter1 = new StaticFilterWrapper(new EqualsFilter(C0_A),
					new ConstantDisplayValue(C0_A));
				staticFilter2 = new StaticFilterWrapper(new EqualsFilter(C0_B),
					new ConstantDisplayValue(C0_B));
				filters.add(staticFilter1);
				filters.add(staticFilter2);

				// Creation of table filter infrastructure based on static filters
				FilterDialogBuilder manager = getFilterDialogManager();
				TableFilter controlFilter = new TableFilter(manager, filters, true);
				return controlFilter;
			}
		});
	}

	private void createFilterForSecondColumn(TableConfiguration tableConfiguration) {
		tableConfiguration.getDeclaredColumn(C1).setFilterProvider(new TableFilterProvider() {

			@Override
			public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {
				failingTableFilter = new FailingTableFilter(getFilterDialogManager());
				return failingTableFilter;
			}
		});
	}

	private PopupFilterDialogBuilder getFilterDialogManager() {
		PopupDialogModel dialogModel =
			new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
				new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH), PIXEL,
					100, 70, PIXEL, 100, Scrolling.NO),
				1);
		return new PopupFilterDialogBuilder(dialogModel);
	}


	protected TableModel createReducedTableModel() {
		String[] columnNames = new String[] { C0, C2, C3 };
		
		List rows = Arrays.asList(new Object[][] { 
				{ C0_A, C2_A, C3_A },
				{ C0_B, C2_D, C3_A },
				{ C0_A, C2_B, C3_C },
				{ C0_B, C2_E, C3_A },
				{ C0_A, C2_C, C3_A },
				{ C0_B, C2_F, C3_A },
				{ C0_B, C2_G, C3_B }
		});
		
		return new ArrayTableModel(columnNames, rows);
	}
	
	protected TableModel createExtendedTableModel() {
		String[] columnNames = new String[] { C0, C2, "CX", C3, C4 };
		
		List rows = Arrays.asList(new Object[][] { 
			{ C0_A, C2_A, CX_A, C3_A, C4_A },
			{ C0_B, C2_D, CX_C, C3_A, C4_A },
			{ C0_A, C2_B, CX_B, C3_C, C4_C },
			{ C0_B, C2_E, CX_B, C3_A, C4_B },
			{ C0_A, C2_C, CX_A, C3_A, C4_C },
			{ C0_B, C2_F, CX_A, C3_A, C4_C },
			{ C0_B, C2_G, CX_C, C3_B, C4_B }
		});
		
		return new ArrayTableModel(columnNames, rows);
	}

	protected EditableRowTableModel createMappedValuesTableModel() {
		String[] columnNames = scenario.getColumnNames();
		List rows = createMappedValuesRows();
		TableConfiguration configuration = createMappedValuesTableConfiguration(columnNames);
		createColumnComparators(configuration);
		createTableFilters(configuration);
		return new ObjectTableModel(columnNames, configuration, rows);
	}

	private List createMappedValuesRows() {
		List rows = scenario.createRows();
		for (int i = 0; i < rows.size(); i++) {
			Object[] row = (Object[]) rows.get(i);
			for (int j = 0; j < row.length; j++) {
				String defaultValue = (String) row[j];
				if (defaultValue != null) {
					row[j] = defaultValue + MAPPED_VALUES_SUFFIX;
				}
			}
		}
		return rows;
	}

	protected TableViewModel createMappedValuesTableViewModel(TableModel applicationModel) {
		TableViewModel model = createTableViewModel(applicationModel);
		return model;
	}

	private TableConfiguration createMappedValuesTableConfiguration(String[] columnNames) {
		TableConfiguration configuration = TableConfiguration.table();
		configuration.getDefaultColumn().setSortKeyProvider(TestSortKeyProvider.INSTANCE);

		scenario.createColumnConfigsAndInitAccessors(configuration, columnNames);
		return configuration;
	}

	private void setFilter(Filter<?> wrappedFilter, Comparator<?> order, TableModel tableModel) {
		tableModel.setFilter(new WrappingTableRowFilter(wrappedFilter), order);
	}

	private static class TestSortKeyProvider implements Mapping<String, String> {

		/* package protected */static TestSortKeyProvider INSTANCE = new TestSortKeyProvider();

		@Override
		public String map(String input) {
			if (input != null) {
				return input.split("-")[0];
			} else {
				return null;
			}
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			noSelectColumnSetup(
				ServiceTestSetup.createSetup(
					new TestSuite(TestTableViewModel.class),
					RequestLockFactory.Module.INSTANCE, TableConfigurationFactory.Module.INSTANCE)));
	}

	public static CustomPropertiesSetup noSelectColumnSetup(Test test) {
		return new CustomPropertiesSetup(test,
			CustomPropertiesDecorator.createFileName(TestTableViewModel.class, "noSelectColumn.xml"),
			true);
	}

	/**
	 * {@link TableViewModel} with accessible constructor
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class TestingTableViewModel extends TableViewModel {
		public TestingTableViewModel(TableModel applicationModel) {
			super(applicationModel, MODEL_CONFIG_KEY);
		}

		protected TestingTableViewModel(TableModel applicationModel, ConfigKey key) {
			super(applicationModel, key);
		}
	}

	private static class FailingTableFilter extends TableFilter {
		TableFilterListener viewModel;

		private boolean isActive;

		public FailingTableFilter(FilterDialogBuilder manager) {
			super(manager);
			viewModel = null;
			isActive = false;
		}

		@Override
		public FormContext openFilterDialog(DisplayContext context, PopupHandler handler, Optional<BlockControl> sortControl) {
			throw new UnsupportedOperationException("Cannot open filter dialog in test filter");
		}

		@Override
		public void addTableFilterListener(TableFilterListener listener) {
			this.viewModel = listener;
		}

		@Override
		public boolean accept(Object obj) {
			viewModel.handleTableFilterEvent(new TableFilterEvent(this, FilterEventTypes.ERROR));
			return super.accept(obj);
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		void setActive() {
			isActive = true;
		}

		void setInactive() {
			isActive = false;
		}

	}
}