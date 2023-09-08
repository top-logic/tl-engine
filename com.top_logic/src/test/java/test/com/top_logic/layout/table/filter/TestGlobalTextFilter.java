/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.TableModelTestScenario;
import test.com.top_logic.layout.table.TestTableViewModel.TestingTableViewModel;

import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.filter.GlobalTextFilter;
import com.top_logic.layout.table.filter.TextFilterConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * Test case for {@link GlobalTextFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestGlobalTextFilter extends AbstractLayoutTest {

	private TableModelTestScenario scenario;
	private EditableRowTableModel applicationModel;
	private TableViewModel viewModel;

	private GlobalTextFilter globalTextFilter;

	private TextFilterConfiguration filterConfiguration;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		createTableModels();
		createFirstColumnLabelProvider();
		createTestFilter();
	}

	private void createTableModels() {
		scenario = new TableModelTestScenario();
		applicationModel = scenario.createTableModel();
		viewModel = new TestingTableViewModel(applicationModel);
	}

	private void createFirstColumnLabelProvider() {
		ColumnConfiguration firstColumnConfiguration =
			applicationModel.getTableConfiguration().getDeclaredColumn(TableModelTestScenario.C0);
		firstColumnConfiguration.setFullTextProvider(DefaultLabelProvider.INSTANCE);
	}

	public void createTestFilter() {
		globalTextFilter = new GlobalTextFilter(viewModel, false);
		filterConfiguration = (TextFilterConfiguration) globalTextFilter.getFilterConfiguration();
	}

	public void testRowAcceptedByFirstColumnTextFilter() {
		filterConfiguration.setTextPattern(TableModelTestScenario.C0_A);

		assertTrue("Filter did not accept value of first column!",
			filterAccepts(applicationModel.getRowObject(0)));
	}

	public void testRowDroppedIfNoFilterMatches() {
		filterConfiguration.setTextPattern("No match");

		assertFalse("Filter should not accept row, if no column matches search string!",
			filterAccepts(applicationModel.getRowObject(0)));
	}

	public void testDropNullRow() {
		assertFalse("Filter should not accept null rows!",
			filterAccepts(null));
	}

	private boolean filterAccepts(Object rowObject) {
		globalTextFilter.startFilterRevalidation(false);
		boolean acceptWithSetup = globalTextFilter.accept(rowObject);
		globalTextFilter.stopFilterRevalidation();
		boolean acceptWithoutSetup = globalTextFilter.accept(rowObject);
		return acceptWithSetup && acceptWithoutSetup;
	}

	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestGlobalTextFilter.class, LabelProviderService.Module.INSTANCE));
	}
}
