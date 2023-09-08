/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.Listener;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * A {@link Listener} that interprets the {@link Value} it listens on as options for the given
 * {@link ValueModel} and updates the latter.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ValueUpdateOnOptionsChange implements Listener {

	private final ValueModel _valueModel;

	private final OptionMapping _optionMapping;

	private final Comparator<Object> _optionComparator;

	ValueUpdateOnOptionsChange(ValueModel valueModel, OptionMapping optionMapping,
			Comparator<Object> optionComparator) {
		_optionComparator = optionComparator;
		_valueModel = Utils.requireNonNull(valueModel);
		_optionMapping = optionMapping;
	}

	@Override
	public void handleChange(Value<?> sender) {
		Iterable<?> newOptions = (Iterable<?>) sender.get();
		Object currentValue = _valueModel.getValue();
		if ((currentValue != null) && contains(newOptions, _optionMapping.asOption(newOptions, currentValue))) {
			return;
		}

		PropertyDescriptor property = _valueModel.getProperty();
		ImplementationClassDefault annotation = property.getAnnotation(ImplementationClassDefault.class);
		if (annotation != null) {
			Class<?> defaultImplClass = annotation.value();

			Object defaultOption = findDefaultOption(defaultImplClass, newOptions);
			if (defaultOption != null) {
				_valueModel.setValue(_optionMapping.toSelection(defaultOption));
				return;
			}
		}

		Object firstOption = defaultOption(newOptions);
		if (firstOption != null) {
			_valueModel.setValue(_optionMapping.toSelection(firstOption));
		}
	}

	private Object findDefaultOption(Class<?> defaultImplClass, Iterable<?> newOptions) {
		for (Object option : newOptions) {
			if (option instanceof Class<?>) {
				if (option == defaultImplClass) {
					return option;
				}
			} else if (option instanceof PolymorphicConfiguration<?>) {
				if (((PolymorphicConfiguration<?>) option).getImplementationClass() == defaultImplClass) {
					return option;
				}
			}
			else if (option != null && option.getClass() == defaultImplClass) {
				return option;
			}
		}
		return null;
	}

	private boolean contains(Iterable<?> optionModel, Object option) {
		if (optionModel instanceof ListOptionModel) {
			optionModel = ((ListOptionModel<?>) optionModel).getBaseModel();
		}
		if (optionModel instanceof Collection<?>) {
			return ((Collection<?>) optionModel).contains(option);
		}
		for (Object element : optionModel) {
			if (option.equals(element)) {
				return true;
			}
		}
		return false;
	}

	private Object defaultOption(Iterable<?> optionModel) {
		// The default as defined by the option model.
		if (optionModel instanceof OptionModel) {
			Object defaultValue = ((OptionModel<?>) optionModel).getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}
		}

		if (optionModel instanceof ListOptionModel) {
			optionModel = ((ListOptionModel<?>) optionModel).getBaseModel();
		}

		// The first option in compare order.
		if (optionModel instanceof Collection<?>) {
			Object[] options = ((Collection<?>) optionModel).toArray();
			if (options.length > 0) {
				Arrays.sort(options, _optionComparator);
				return options[0];
			} else {
				return null;
			}
		}

		// The first option found.
		for (Object element : optionModel) {
			return element;
		}
		return null;
	}

}
