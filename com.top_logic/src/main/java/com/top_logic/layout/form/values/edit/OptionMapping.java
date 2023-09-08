/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.SelectField;

/**
 * Conversion from select options to {@link PropertyDescriptor} values and vice-versa.
 * 
 * <p>
 * An {@link OptionMapping} is typically used to converts an selected option of a
 * {@link SelectField} to a value of a {@link PropertyDescriptor} using {@link #toSelection(Object)}
 * and determine the selection of a {@link SelectField} from a {@link PropertyDescriptor} value
 * using {@link #asOption(Iterable, Object)}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface OptionMapping {

	/**
	 * Converts an option into a valid member of the selection.
	 * 
	 * @param option
	 *        A member of the options.
	 * @return A corresponding member of the target selection.
	 */
	Object toSelection(Object option);

	/**
	 * Converts a selection back to an option.
	 * 
	 * @param allOptions
	 *        All options in the context selection to choose from.
	 * @param selection
	 *        A selected value.
	 * 
	 * @return A value displayed as option.
	 */
	default Object asOption(Iterable<?> allOptions, Object selection) {
		for (Object option : allOptions) {
			if (toSelection(option).equals(selection)) {
				return option;
			}
		}
		return null;
	}

}
