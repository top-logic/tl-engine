/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.TableModelTestScenario;
import test.com.top_logic.layout.table.TestTableViewModel.TestingTableViewModel;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider.Config;
import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableFilter;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;
import com.top_logic.layout.table.filter.ComparableFilterView;
import com.top_logic.layout.table.filter.ComparableFilterViewBuilder;
import com.top_logic.layout.table.filter.ComparableFilterViewFormFields;
import com.top_logic.layout.table.filter.FilterComparator;
import com.top_logic.layout.table.filter.FilterViewControl;
import com.top_logic.layout.table.filter.NumberFieldProvider;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * Tests for {@link ComparableFilter}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestComparableFilter extends AbstractLayoutTest {

	private ComparableFilter filter;

	private ComparableFilterConfiguration filterConfiguration;

	private ComparableFilterViewBuilder viewBuilder;

	private ComparableFilterView view;

	private TableModelTestScenario scenario;

	private EditableRowTableModel applicationModel;

	private TableViewModel viewModel;

	private static final TestIntegerComparator COMPARATOR = new TestIntegerComparator();

	private static class TestIntegerComparator implements FilterComparator {

		@Override
		public int compare(Object o1, Object o2) {
			Integer firstInteger = (Integer) o1;
			Integer secondInteger = (Integer) o2;
			return firstInteger - secondInteger;
		}

		@Override
		public List<Class<?>> getSupportedObjectTypes() {
			return Collections.<Class<?>> singletonList(Integer.class);
		}

	}


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createTableModels();
		Config xmlFilterConfiguration = ApplicationConfig.getInstance().getConfig(Config.class);
		filterConfiguration =
			new ComparableFilterConfiguration(viewModel,
				scenario.getColumnNames()[0], AllOperatorsProvider.INSTANCE,
				COMPARATOR, false, true);
		viewBuilder = new ComparableFilterViewBuilder(NumberFieldProvider.INSTANCE,
			xmlFilterConfiguration.getSeparateOptionDisplayThreshold());
		filter = new ComparableFilter(filterConfiguration, viewBuilder,
			xmlFilterConfiguration.shouldShowNonMatchingOptions());
		view = (ComparableFilterView) viewBuilder.createFilterViewControl(DummyDisplayContext.newInstance(),
			filterConfiguration,
			new FormContext("filterView", ResPrefix.GLOBAL), 0);
		filterConfiguration.addValueListener((FilterViewControl) view);
	}

	private void createTableModels() {
		scenario = new TableModelTestScenario();
		applicationModel = scenario.createTableModel();
		viewModel = new TestingTableViewModel(applicationModel);
	}

	public void testFilterForGreaterValues() {
		filterConfiguration.setOperator(Operators.GREATER_THAN);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesNotAccepted(6, 9, 10);
		assertAllValuesAccepted(11, 14);
	}

	public void testFilterForGreaterAndEqualValues() {
		filterConfiguration.setOperator(Operators.GREATER_OR_EQUALS);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesNotAccepted(6, 9);
		assertAllValuesAccepted(10, 11, 14);
	}

	public void testFilterForEqualValues() {
		filterConfiguration.setOperator(Operators.EQUALS);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesNotAccepted(6, 9, 11, 14);
		assertAllValuesAccepted(10);
	}

	public void testFilterForSmallerOrEqualValues() {
		filterConfiguration.setOperator(Operators.LESS_OR_EQUALS);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesAccepted(6, 9, 10);
		assertAllValuesNotAccepted(11, 14);
	}

	public void testFilterForSmallerValues() {
		filterConfiguration.setOperator(Operators.LESS_THAN);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesAccepted(6, 9);
		assertAllValuesNotAccepted(10, 11, 14);
	}

	public void testFilterForNotEqualValues() {
		filterConfiguration.setOperator(Operators.NOT_EQUAL);
		filterConfiguration.setPrimaryFilterPattern(10);

		assertAllValuesAccepted(6, 9, 11, 14);
		assertAllValuesNotAccepted(10);
	}
	
	public void testFilterForValueRange() {
		filterConfiguration.setOperator(Operators.BETWEEN);
		filterConfiguration.setPrimaryFilterPattern(10);
		filterConfiguration.setSecondaryFilterPattern(12);

		assertAllValuesAccepted(10, 11, 12);
		assertAllValuesNotAccepted(6, 9, 13, 14);
	}

	private void assertAllValuesAccepted(int... values) {
		for (int value : values) {
			assertTrue(value + " has to be accepted, but is not!", filter.accept(value));
		}
	}

	private void assertAllValuesNotAccepted(int... values) {
		for (int value : values) {
			assertFalse(value + " has NOT to be accepted, but is!", filter.accept(value));
		}
	}

	public void testFilterInactiveForNullFilterPatternInConfig() {
		filterConfiguration.setPrimaryFilterPattern(null);
		filterConfiguration.setOperator(Operators.EQUALS);
		
		assertFalse("Filter must not be active in case filter pattern is null!", filter.isActive());
	}

	public void testResetFilterOperatorForNullFilterPatternInUpdate() {
		setupConfig(2, Operators.LESS_OR_EQUALS);
		setupView(null, Operators.GREATER_THAN);

		view.applyFilterSettings();

		assertResettedPatternAndOperator(filterConfiguration.getPrimaryFilterPattern(), filterConfiguration.getOperator());
	}

	private void assertResettedPatternAndOperator(Comparable<?> filterPattern, Operators operator) {
		assertEquals("Filter pattern must be set to null, in case filter has been resetted!", null,
			filterPattern);
		assertEquals(
			"Operator must be resetted to standard, in case filter pattern is set to null!",
			Operators.EQUALS, operator);
	}

	private void setupConfig(Comparable<?> filterPattern, Operators operator) {
		filterConfiguration.setPrimaryFilterPattern(filterPattern);
		filterConfiguration.setOperator(operator);
	}

	private void setupView(Comparable<?> filterPattern, Operators operator) {
		ComparableFilterViewFormFields filterFormFields = getFilterViewFormFields();
		filterFormFields.getPrimaryFilterPatternField().setValue(filterPattern);
		filterFormFields.getOperatorField().setAsSingleSelection(operator);
	}

	private ComparableFilterViewFormFields getFilterViewFormFields() {
		ComparableFilterViewFormFields filterFormFields =
			ReflectionUtils.getValue(view, "filterFormFields", ComparableFilterViewFormFields.class);
		return filterFormFields;
	}

	public void testResetFilterState() {
		setupView(2, Operators.LESS_OR_EQUALS);

		filter.clearFilterConfiguration();

		ComparableFilterViewFormFields dialogFormFields = getFilterViewFormFields();
		assertResettedPatternAndOperator((Comparable<?>) dialogFormFields.getPrimaryFilterPatternField().getValue(),
			(Operators) dialogFormFields.getOperatorField().getSingleSelection());
	}

	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestComparableFilter.class, LabelProviderService.Module.INSTANCE));
	}
}
