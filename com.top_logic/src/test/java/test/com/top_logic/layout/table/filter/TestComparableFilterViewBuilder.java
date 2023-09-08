/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.text.NumberFormat;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.TableModelTestScenario;
import test.com.top_logic.layout.table.TestTableViewModel.TestingTableViewModel;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider.Config;
import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ComparableFilterView;
import com.top_logic.layout.table.filter.ComparableFilterViewBuilder;
import com.top_logic.layout.table.filter.NumberFieldProvider;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * Tests for {@link ComparableFilterView}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestComparableFilterViewBuilder extends AbstractLayoutTest {

	private TableModelTestScenario scenario;

	private EditableRowTableModel applicationModel;

	private TableViewModel viewModel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createTableModels();
	}

	private void createTableModels() {
		scenario = new TableModelTestScenario();
		applicationModel = scenario.createTableModel();
		viewModel = new TestingTableViewModel(applicationModel);
	}

	public void testFilterDialogCreationShouldNotChangePrecisionOfStandardNumberFormat() {
		NumberFormat standardFormat = getFormatCopy();
		Config filterConfiguration = ApplicationConfig.getInstance().getConfig(Config.class);
		FormGroup parent = new FormGroup("filter", ResPrefix.forTest("filter."));
		new ComparableFilterViewBuilder(NumberFieldProvider.INSTANCE,
			filterConfiguration.getSeparateOptionDisplayThreshold()).createFilterViewControl(
			DummyDisplayContext.newInstance(),
																		new ComparableFilterConfiguration(
					viewModel, scenario.getColumnNames()[0],
					AllOperatorsProvider.INSTANCE,
					com.top_logic.layout.table.filter.NumberComparator.getInstance(),
					false, true),
			parent, 1);

		assertStandardFormatSettings(standardFormat);
	}

	private NumberFormat getFormatCopy() {
		return (NumberFormat) HTMLFormatter.getInstance().getNumberFormat().clone();
	}

	private void assertStandardFormatSettings(NumberFormat standardFormat) {
		NumberFormat currentFormat = getFormatCopy();
		assertEquals("Minimum integer digits of standard has been changed!", standardFormat.getMinimumIntegerDigits(),
			currentFormat.getMinimumIntegerDigits());
		assertEquals("Maximum integer digits of standard has been changed!", standardFormat.getMaximumIntegerDigits(),
			currentFormat.getMaximumIntegerDigits());
		assertEquals("Minimum fraction digits of standard has been changed!",
			standardFormat.getMinimumFractionDigits(),
			currentFormat.getMinimumFractionDigits());
		assertEquals("Maximum fraction digits of standard has been changed!",
			standardFormat.getMaximumFractionDigits(),
			currentFormat.getMaximumFractionDigits());
	}

	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestComparableFilterViewBuilder.class,
			LabelProviderService.Module.INSTANCE, FormatterService.Module.INSTANCE));
	}
}
