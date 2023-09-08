/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.ComparableFilterView.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;
import com.top_logic.util.Resources;

/**
 * {@link FilterViewBuilder} of {@link ComparableFilterView}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ComparableFilterViewBuilder implements FilterViewBuilder<ComparableFilterConfiguration> {

	private static final String PRIMARY_FILTER_TEXT_FIELD = "text";
	private static final String SECONDARY_FILTER_TEXT_FIELD = "secondaryText";
	
	private FilterFieldProvider filterFieldProvider;
	private int _separateOptionDisplayThreshold;

	/**
	 * Create a new {@link ComparableFilterViewBuilder}.
	 */
	public ComparableFilterViewBuilder(FilterFieldProvider filterFieldProvider, int separateOptionDisplayThreshold) {
		this.filterFieldProvider = filterFieldProvider;
		_separateOptionDisplayThreshold = separateOptionDisplayThreshold;
	}

	@Override
	public FilterViewControl<?> createFilterViewControl(DisplayContext context, ComparableFilterConfiguration config,
			FormGroup form, int filterControlId) {
		ComparableFilterViewFormFields filterFormFields = new ComparableFilterViewFormFields();
		FormGroup filterFormGroup =
			createDialogFormGroup(config, filterControlId, filterFormFields);
		form.addMember(filterFormGroup);

		return new ComparableFilterView(config, filterFormGroup,
			filterFormFields,
			_separateOptionDisplayThreshold);
	}

	private FormGroup createDialogFormGroup(ComparableFilterConfiguration config,
			int filterControlId, ComparableFilterViewFormFields filterFormFields) {
		// Definition of the form group structure
		FormGroup filterContext = new FormGroup(FILTER_GROUP_NAME + filterControlId, RES_PREFIX);

		/* Definition of the filter settings */

		// operator setting
		SelectField operatorField = setupOperatorsField(config.getAvailableOperators(), config.getOperator());
		filterContext.addMember(operatorField);

		// "filter number" settings
		FormGroup patternGroup = new FormGroup(PATTERN_GROUP_NAME, RES_PREFIX);
		FormField primaryFilterPatternField =
			createFilterPatternField(PRIMARY_FILTER_TEXT_FIELD, config.getPrimaryFilterPattern());
		patternGroup.addMember(primaryFilterPatternField);

		FormField secondaryFilterPatternField =
			createFilterPatternField(SECONDARY_FILTER_TEXT_FIELD, config.getSecondaryFilterPattern());
		patternGroup.addMember(secondaryFilterPatternField);

		filterContext.addMember(patternGroup);

		SelectField selectableValuesField = createSelectableValuesField(config);
		filterContext.addMember(selectableValuesField);

		filterFormFields.setSelectableValuesField(selectableValuesField);
		filterFormFields.setOperatorField(operatorField);
		filterFormFields.setPrimaryFilterPatternField(primaryFilterPatternField);
		filterFormFields.setSecondaryFilterPatternField(secondaryFilterPatternField);

		if (config.getOperator() != Operators.BETWEEN) {
			setupSingleFieldDisplay(filterFormFields);
		} else {
			setupMultiFieldDisplay(filterFormFields);
		}

		return filterContext;
	}

	private void setupSingleFieldDisplay(ComparableFilterViewFormFields filterFormFields) {
		FormField secondaryFilterPatternField = filterFormFields.getSecondaryFilterPatternField();
		secondaryFilterPatternField.setVisible(false);
		secondaryFilterPatternField.setValue(null);
	}

	private void setupMultiFieldDisplay(ComparableFilterViewFormFields filterFormFields) {
		FormField secondaryFilterPatternField = filterFormFields.getSecondaryFilterPatternField();
		secondaryFilterPatternField.setVisible(true);
	}

	private SelectField setupOperatorsField(List<Operators> availableOperators, Operators selectedOperator) {
		// Creation and registration of the operators field
		SelectField operatorsField = createOperatorsField(availableOperators, selectedOperator);

		// Configuration of the operators field
		operatorsField.setMandatory(SelectField.MANDATORY);
		return operatorsField;
	}

	private SelectField createOperatorsField(List<Operators> availableOperators, Operators selectedOperator) {
		return FormFactory.newSelectField(OPERATOR_SELECT_FIELD, availableOperators, false,
			Collections.singletonList(selectedOperator), false);
	}

	private FormField createFilterPatternField(String patternFieldName, Comparable<?> value) {
		return filterFieldProvider.createField(patternFieldName, value);
	}

	private SelectField createSelectableValuesField(ComparableFilterConfiguration filterConfiguration) {
		List<?> options = SelectionFilterViewBuilder.getOptions(filterConfiguration);
		SelectField optionsField =
			FormFactory.newSelectField(ComparableFilterView.SELECTABLE_VALUES_FIELD, options, true, false);
		optionsField.setOptionComparator(filterConfiguration.getOptionComparator());
		optionsField.setOptionLabelProvider(filterConfiguration.getOptionLabelProvider());
		optionsField.setLabel(Resources.getInstance().getString(I18NConstants.FILTER_VALUES));
		return optionsField;
	}
}
