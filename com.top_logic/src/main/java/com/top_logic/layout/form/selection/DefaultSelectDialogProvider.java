/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.SelectField;

/**
 * Configurable {@link SelectDialogProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSelectDialogProvider extends SelectDialogProvider {
	
	/**
	 * New {@link DefaultSelectDialogProvider} with the given configuration.
	 */
	public DefaultSelectDialogProvider(InstantiationContext context, SelectDialogConfig config) {
		super(context, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractSelectDialog createSelectDialog(SelectField selectField) {
		if (selectField.isMultiple() && selectField.hasCustomOrder()) {
			OptionRenderer dialogListRenderer = new OptionRenderer(true);
			Filter<Object> fixedOptions = selectField.getFixedOptionsNonNull();
			dialogListRenderer.addListItemAdorner(new FixedOptionAdorner(fixedOptions));
			dialogListRenderer.setFixedOptionMarker(new FilterBasedMarker(fixedOptions));
			getConfig().setDialogListRenderer(dialogListRenderer);
		}
		if (selectField.isOptionsTree()) {
			// Option tree
			if (selectField.isMultiple()) {
				return new TreeMultiSelectDialog(selectField, getConfig());
			} else {
				return new TreeSingleSelectDialog(selectField, getConfig());
			}
		} else {
			// Option list
			if (selectField.isMultiple()) {
				return new MultiSelectDialog(selectField, getConfig());
			} else {
				return new SingleSelectDialog(selectField, getConfig());
			}
		}
	}

}
