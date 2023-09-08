/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableRowFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.filter.FilterViewControl;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.StaticFilterWrapperConfiguration;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterEvent.FilterEventTypes;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * {@link TestCase} of {@link TableRowFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTableRowFilter extends AbstractLayoutTest {

	private TableViewModelTestScenario testScenario;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TableModelTestScenario scenario = new TableModelTestScenario();
		EditableRowTableModel applicationModel = scenario.createTableModel();
		testScenario = new TableViewModelTestScenario(applicationModel);
	}

	public void testOnePartFilterIsActiveOnly() throws Exception {
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter1().getFilterConfiguration());

		assertMatchCount(testScenario.getC0StaticFilter1(), 3);
		assertMatchCount(testScenario.getC0StaticFilter2(), 4);
	}

	public void testOneColumnFilterIsAllActive() throws Exception {
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter1().getFilterConfiguration());
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter2().getFilterConfiguration());

		assertMatchCount(testScenario.getC0StaticFilter1(), 3);
		assertMatchCount(testScenario.getC0StaticFilter2(), 4);
	}

	public void testGlobalFilterIsActiveOnly() throws Exception {
		activateGlobalFilter();
		assertMatchCount(testScenario.getGlobalStaticFilter(), 2);
	}

	public void testTwoColumnFiltersAreAllActive() throws Exception {
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter1().getFilterConfiguration());
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter2().getFilterConfiguration());
		activateSubFilterOfSecondColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC1StaticFilter1().getFilterConfiguration());
		activateSubFilterOfSecondColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC1StaticFilter2().getFilterConfiguration());

		assertMatchCount(testScenario.getC0StaticFilter1(), 1);
		assertMatchCount(testScenario.getC0StaticFilter2(), 1);
		assertMatchCount(testScenario.getC1StaticFilter1(), 2);
		assertMatchCount(testScenario.getC1StaticFilter2(), 0);
	}

	public void testOneColumnFiltersIsVisibleOnly() throws Exception {
		makeFirstColumnFilterVisible();

		assertMatchCount(testScenario.getC0StaticFilter1(), 3);
		assertMatchCount(testScenario.getC0StaticFilter2(), 4);
	}

	public void testNoValueFilterVisibleOnly() throws Exception {
		makeFourthColumnFilterVisible();

		assertMatchCount(testScenario.getC3StaticFilter1(), 2);
	}

	public void testNoValueFilterIsActive() throws Exception {
		makeFourthColumnFilterVisible();
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC3StaticFilter1().getFilterConfiguration());

		assertMatchCount(testScenario.getC3StaticFilter1(), 2);
	}

	public void testOneColumnFiltersIsActiveAndOneColumnFilterVisibleOnly() throws Exception {
		makeSecondColumnFilterVisible();
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter1().getFilterConfiguration());
		activateSubFilterOfFirstColumn(
			(StaticFilterWrapperConfiguration) testScenario.getC0StaticFilter2().getFilterConfiguration());

		assertMatchCount(testScenario.getC0StaticFilter1(), 3);
		assertMatchCount(testScenario.getC0StaticFilter2(), 4);
		assertMatchCount(testScenario.getC1StaticFilter1(), 2);
		assertMatchCount(testScenario.getC1StaticFilter2(), 0);
	}

	private void makeFirstColumnFilterVisible() {
		testScenario.getC0StaticFilter1().getFilterConfiguration()
			.addValueListener(new FilterViewControl(null, new FormContext("dummyForm", ResPrefix.GLOBAL)) {
				@Override
				protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
					throw new UnsupportedOperationException();

				}

				@Override
				public void resetFilterSettings() {
					throw new UnsupportedOperationException();
				}

				@Override
				protected void internalValueChanged() {
					// Do nothing
				}

				@Override
				protected boolean internalApplyFilterSettings() {
					throw new UnsupportedOperationException();
				}
			});
		fireFilterUpdateForColumn(TableModelTestScenario.C0);
	}

	private void makeSecondColumnFilterVisible() {
		testScenario.getC1StaticFilter1().getFilterConfiguration()
			.addValueListener(new FilterViewControl(null, new FormContext("dummyForm", ResPrefix.GLOBAL)) {
			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				throw new UnsupportedOperationException();

			}

			@Override
			public void resetFilterSettings() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected void internalValueChanged() {
				// Do nothing
			}

			@Override
			protected boolean internalApplyFilterSettings() {
				throw new UnsupportedOperationException();
			}
		});
		fireFilterUpdateForColumn(TableModelTestScenario.C1);
	}

	private void makeFourthColumnFilterVisible() {
		testScenario.getC3StaticFilter1().getFilterConfiguration()
			.addValueListener(new FilterViewControl(null, new FormContext("dummyForm", ResPrefix.GLOBAL)) {
				@Override
				protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
					throw new UnsupportedOperationException();

				}

				@Override
				public void resetFilterSettings() {
					throw new UnsupportedOperationException();
				}

				@Override
				protected void internalValueChanged() {
					// Do nothing
				}

				@Override
				protected boolean internalApplyFilterSettings() {
					throw new UnsupportedOperationException();
				}
			});
		fireFilterUpdateForColumn(TableModelTestScenario.C3);
	}

	private void assertMatchCount(StaticFilterWrapper staticFilter, int expectedMatchCount) {
		StaticFilterWrapperConfiguration configuration = (StaticFilterWrapperConfiguration)staticFilter.getFilterConfiguration();
		assertEquals(expectedMatchCount, configuration.getMatchCount());
	}

	private void activateSubFilterOfFirstColumn(StaticFilterWrapperConfiguration filterConfig) {
		filterConfig.setActive(true);
		fireFilterUpdateForColumn(TableModelTestScenario.C0);
	}

	private void activateSubFilterOfSecondColumn(StaticFilterWrapperConfiguration filterConfig) {
		filterConfig.setActive(true);
		fireFilterUpdateForColumn(TableModelTestScenario.C1);
	}

	private void activateGlobalFilter() {
		((StaticFilterWrapperConfiguration) testScenario.getGlobalStaticFilter().getFilterConfiguration())
			.setActive(true);
		fireFilterUpdate(testScenario.getViewModel().getGlobalFilter());
	}

	private void fireFilterUpdateForColumn(String columnName) {
		TableViewModel viewModel = testScenario.getViewModel();
		TableFilter filter =
			viewModel.getFilter(viewModel.getApplicationModel().getHeader().getColumn(columnName).getIndex());
		fireFilterUpdate(filter);
	}

	private void fireFilterUpdate(TableFilter filter) {
		testScenario.getViewModel().handleTableFilterEvent(new TableFilterEvent(filter,
				FilterEventTypes.CONFIGURATION_UPDATE));
		runValidation();
	}

	public static Test suite() {
		return suite(TestTableRowFilter.class);
	}
}
