/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.List;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.util.Country;

/**
 * {@link OptionProvider} that based on a flat list of options.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ListOptionProvider extends OptionProvider {

	@Override
	default ListOptionModel<?> getOptions(EditContext editContext) {
		return new DefaultListOptionModel<>(getOptionsList(editContext));
	}

	/**
	 * Deliver the list values for the {@link OptionModel}. For example the
	 * {@link ListOptionProvider} for countries would deliver a {@link List} containing all
	 * {@link Country}s.
	 * 
	 * @param editContext
	 *        The current {@link AttributeUpdate} for which the provider must deliver an option
	 *        list.
	 * 
	 * @return The list of all possible values.
	 */
	List<?> getOptionsList(EditContext editContext);

}

