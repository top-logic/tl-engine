/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.SelectionFilterView.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.util.TLCollator;

/**
 * {@link FilterViewBuilder} of {@link SelectionFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectionFilterViewBuilder implements FilterViewBuilder<SelectionFilterConfiguration> {

	private static final ResourceView RESOURCES = RES_PREFIX;
	
	private int separateOptionDisplayThreshold;
	
	/**
	 * Creates a new {@link SelectionFilterViewBuilder}
	 */
	public SelectionFilterViewBuilder(int separateOptionDisplayThreshold) {
		this.separateOptionDisplayThreshold = separateOptionDisplayThreshold;
	}

	@Override
	public FilterViewControl<?> createFilterViewControl(DisplayContext context, SelectionFilterConfiguration config,
			FormGroup form, int filterControlId) {
		FormGroup formGroup = createFormGroup(context, config, filterControlId);
		form.addMember(formGroup);
		return new SelectionFilterView(config, formGroup, separateOptionDisplayThreshold);
	}

	private FormGroup createFormGroup(DisplayContext context, SelectionFilterConfiguration config,
			int filterControlId) {
		FormGroup filterContext = new FormGroup(FILTER_GROUP + filterControlId, RESOURCES);
		filterContext.addMember(createOptionsField(config));
		return filterContext;
	}

	private FormMember createOptionsField(SelectionFilterConfiguration filterConfiguration) {
		List<?> options = getOptions(filterConfiguration);
		SelectField optionsField = FormFactory.newSelectField(SelectionFilterView.CELL_VALUES_FIELD, options, true, false);
		Comparator<?> optionComparator;
		LabelProvider optionLabelProvider;
		if (filterConfiguration.isUseRawOptions()) {
			optionLabelProvider = filterConfiguration.getOptionLabelProvider();
			optionComparator = filterConfiguration.getOptionComparator();
		} else {
			optionLabelProvider = DefaultLabelProvider.INSTANCE;
			optionComparator = new TLCollator();
		}
		optionsField.setOptionComparator(optionComparator);
		optionsField.setOptionLabelProvider(optionLabelProvider);
		optionsField.setLabel(I18NConstants.FILTER_VALUES);
		return optionsField;
	}

	static List<?> getOptions(SelectionFilterConfiguration filterConfiguration) {
		return new ArrayList<>(filterConfiguration.getOptions().keySet());
	}
}
