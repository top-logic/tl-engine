/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} that resolves {@link SelectField} options using only their labels.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OptionByLabelNaming extends ModelNamingScheme<SelectField, Object, OptionByLabelNaming.Name> {

	/**
	 * {@link ModelName} pointing to a {@link SelectField} option by its label.
	 */
	public interface Name extends ContextDependent {

		/**
		 * The option label.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String value);

	}

	private static final Object INFINITE_OPTIONS = new NamedConstant("infinite");

	private static final Object NOT_FOUND = new NamedConstant("notFound");

	private static final Object AMBIGUOUS = new NamedConstant("ambiguous");

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<SelectField> getContextClass() {
		return SelectField.class;
	}

	@Override
	public Maybe<Name> buildName(SelectField valueContext, Object model) {
		String label = valueContext.getOptionLabelProvider().getLabel(model);
		if (label == null) {
			return Maybe.none();
		}
		String trimmedLabel = label.trim();
		if (trimmedLabel.isEmpty()) {
			return Maybe.none();
		}

		Object resolvedOption = resolve(valueContext, trimmedLabel);
		if (resolvedOption != model) {
			// Ambiguous.
			return Maybe.none();
		}

		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setLabel(trimmedLabel);
		return Maybe.some(name);
	}

	@Override
	public Object locateModel(ActionContext context, SelectField valueContext, Name name) {
		String expectedLabel = name.getLabel();

		Object option = resolve(valueContext, expectedLabel);
		if (option == NOT_FOUND) {
			throw ApplicationAssertions.fail(name, "Cannot resolve option labeled '" + expectedLabel
				+ "' in context of '"
				+ valueContext + "', available options are " + allOptions(valueContext) + ".");
		}
		if (option == AMBIGUOUS) {
			throw ApplicationAssertions.fail(name, "Cannot resolve option labeled '" + expectedLabel
				+ "' in context of '" + valueContext + "', label is ambiguous.");
		}
		if (option == INFINITE_OPTIONS) {
			throw ApplicationAssertions.fail(name, "Cannot resolve option labeled '" + expectedLabel
				+ "' in context of '" + valueContext + "', infinite many options.");
		}
		return option;
	}

	private List<String> allOptions(SelectField valueContext) {
		Iterator<?> it = valueContext.getOptionModel().iterator();
		LabelProvider labelProvider = valueContext.getOptionLabelProvider();
		List<String> result = new ArrayList<>();
		while (it.hasNext()) {
			Object option = it.next();
			String actualLabel = label(labelProvider, option);
			if (actualLabel != null) {
				result.add(actualLabel.trim());
			}
		}
		return result;
	}

	private Object resolve(SelectField valueContext, String expectedLabel) {
		OptionModel<?> optionModel = valueContext.getOptionModel();
		if (optionModel.getOptionCount() == OptionModel.INFINITE) {
			return INFINITE_OPTIONS;
		}
		Iterator<?> it = optionModel.iterator();
		LabelProvider labelProvider = valueContext.getOptionLabelProvider();
		Object uniqueOption = NOT_FOUND;
		while (it.hasNext()) {
			Object option = it.next();
			if (hasLabel(labelProvider, option, expectedLabel)) {
				if (uniqueOption != NOT_FOUND) {
					return AMBIGUOUS;
				}
				uniqueOption = option;
			}
		}

		return uniqueOption;
	}

	private boolean hasLabel(LabelProvider labelProvider, Object option, String expectedLabel) {
		String actualLabel = label(labelProvider, option);
		if (actualLabel == null) {
			return false;
		}

		return actualLabel.trim().equals(expectedLabel);
	}

	private String label(LabelProvider labelProvider, Object option) {
		return labelProvider.getLabel(option);
	}

}
