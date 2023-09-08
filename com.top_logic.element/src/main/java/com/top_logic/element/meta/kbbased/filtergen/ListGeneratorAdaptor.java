/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.List;

import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;

/**
 * {@link Generator} Adaptor for {@link ListOptionModel}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class ListGeneratorAdaptor implements Generator {

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return new DefaultListOptionModel<>(generateLazyList(editContext));
	}

	@SuppressWarnings("rawtypes")
	private List<?> generateLazyList(EditContext editContext) {
		return new LazyListUnmodifyable() {

			@Override
			protected List<?> initInstance() {
				return generateList(editContext);
			}

		};
	}

	/**
	 * Generates the flat list options.
	 */
	public abstract List<?> generateList(EditContext editContext);

}
