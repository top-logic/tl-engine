/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.StaticFilterWrapperView.*;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;

/**
 * {@link FilterViewBuilder} of {@link StaticFilterWrapper}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class StaticFilterWrapperViewBuilder implements FilterViewBuilder<StaticFilterWrapperConfiguration> {

	private final ResPrefix RESOURCES = RES_PREFIX;

	@Override
	public FilterViewControl<?> createFilterViewControl(DisplayContext context, StaticFilterWrapperConfiguration config,
			FormGroup form, int filterControlId) {
		FormGroup filterFormGroup =
			createDialogFormGroup(config, context, filterControlId);
		form.addMember(filterFormGroup);

		return new StaticFilterWrapperView(config, filterFormGroup);
	}

	private FormGroup createDialogFormGroup(StaticFilterWrapperConfiguration config, DisplayContext context,
			int filterControlId) {
		// Definition of the form group structure
		FormGroup filterContext = new FormGroup(FILTER_GROUP + filterControlId, RESOURCES);
		FormGroup filterSettings = new FormContext(FILTER_SETTINGS_FORM_GROUP, RESOURCES);

		filterContext.addMember(filterSettings);

		/* Definition of the filter settings */
		// "active field" setting
		BooleanField activeFieldCheckbox = createActiveField(config.isActive());
		activeFieldCheckbox.setLabel(ResKey.text(config.getFilterDescription().get(context)));
		filterSettings.addMember(activeFieldCheckbox);

		StringField matchCountField = createMatchCountField(config.getMatchCount());
		filterSettings.addMember(matchCountField);

		return filterContext;
	}

	private BooleanField createActiveField(boolean value) {
		return FormFactory.newBooleanField(ACTIVE_FIELD, value, false);
	}

	private StringField createMatchCountField(int matchCount) {
		String output = getFormattedTextOutput(matchCount);
		return FormFactory.newStringField(MATCH_COUNT_FIELD, output, true);
	}

	public static String getFormattedTextOutput(int matchCount) {
		if (matchCount != SingleEmptyValueMatchCounter.EMPTY_VALUE) {
			return "(" + matchCount + ")";
		} else {
			return "";
		}
	}
}
