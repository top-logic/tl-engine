/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.TextFilterView.*;

import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.TLCollator;

/**
 * {@link FilterViewBuilder} of a {@link TextFilterView}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TextFilterViewBuilder implements FilterViewBuilder<TextFilterConfiguration> {

	/**
	 * Configuration for this view
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * true, if regular expressions shall be enabled, false otherwise
		 */
		@Name("use-regular-expressions")
		boolean isRegularExpressionUsed();
	}

	private final ResPrefix RESOURCES = ResPrefix.GLOBAL;

	private boolean _useRegularExpressions;
	private int _separateOptionDisplayThreshold;

	/**
	 * Create a new {@link TextFilterViewBuilder}
	 */
	public TextFilterViewBuilder(int separateOptionDisplayThreshold) {
		_useRegularExpressions = ApplicationConfig.getInstance().getConfig(Config.class).isRegularExpressionUsed();
		_separateOptionDisplayThreshold = separateOptionDisplayThreshold;
	}

	@Override
	public FilterViewControl<?> createFilterViewControl(DisplayContext context, TextFilterConfiguration config,
			FormGroup form, int filterControlId) {
		FormGroup filterFormGroup = createDialogFormGroup(config, filterControlId);
		form.addMember(filterFormGroup);

		return new TextFilterView(config, filterFormGroup, _separateOptionDisplayThreshold);
	}

	private FormGroup createDialogFormGroup(TextFilterConfiguration config, int filterControlId) {
		// Definition of the form group structure
		FormGroup filterContext = new FormGroup(FILTER_CONTEXT + filterControlId, RESOURCES);
		FormGroup filterSettings = new FormContext(FILTER_SETTINGS_FORM_GROUP, RESOURCES);
		FormGroup filterField = new FormContext(FILTER_FIELD_FORM_GROUP, RESOURCES);

		filterContext.addMember(filterSettings);
		filterContext.addMember(filterField);

		/* Definition of the filter settings */
		// "whole field" setting
		BooleanField wholeFieldCheckbox = createWholeFieldCheckbox(config.isWholeField());
		wholeFieldCheckbox.setLabel(Resources.getInstance().getString(WHOLE_FIELD_CHECKBOX));
		filterSettings.addMember(wholeFieldCheckbox);

		// "case sensitive" setting
		BooleanField caseSensitiveCheckbox = createCaseSensitiveCheckbox(config.isCaseSensitive());
		caseSensitiveCheckbox.setLabel(Resources.getInstance().getString(CASE_SENSITIVE_CHECKBOX));
		filterSettings.addMember(caseSensitiveCheckbox);

		// "filter string" setting
		StringField textField = createTextField(config.getTextPattern());
		filterField.addMember(textField);

		// "regular expression" setting
		if (_useRegularExpressions) {
			BooleanField regExpCheckbox = createRegExpCheckbox(config.isRegExp());
			regExpCheckbox.setLabel(Resources.getInstance().getString(REG_EXP_CHECKBOX));
			filterSettings.addMember(regExpCheckbox);
			ValidRegExpDependency validRegExpDependency = new ValidRegExpDependency(regExpCheckbox, textField);
			validRegExpDependency.attach();
		}

		SelectField selectableValuesField = createSelectableValuesField(config);
		filterContext.addMember(selectableValuesField);

		return filterContext;
	}

	private BooleanField createWholeFieldCheckbox(boolean value) {
		return FormFactory.newBooleanField(WHOLE_FIELD_FIELD, value, false);
	}

	private BooleanField createCaseSensitiveCheckbox(boolean value) {
		return FormFactory.newBooleanField(CASE_SENSITIVE_FIELD, value, false);
	}

	private BooleanField createRegExpCheckbox(boolean value) {
		return FormFactory.newBooleanField(REGEXP_FIELD, value, false);
	}

	private StringField createTextField(String text) {
		StringField field = FormFactory.newStringField(TEXT_FIELD, text, false);
		return field;
	}

	private SelectField createSelectableValuesField(TextFilterConfiguration filterConfiguration) {
		List<?> options = SelectionFilterViewBuilder.getOptions(filterConfiguration);
		SelectField optionsField =
			FormFactory.newSelectField(TextFilterView.SELECTABLE_VALUES_FIELD, options, true, false);
		optionsField.setOptionComparator(new TLCollator());
		optionsField.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		optionsField.setLabel(Resources.getInstance().getString(I18NConstants.FILTER_VALUES));
		return optionsField;
	}
}
