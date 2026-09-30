/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.confirm;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.I18NConstants;

/**
 * Shows a generic confirmation message for a delete command.
 * 
 * @implNote This class is used as annotated default and therefore must not have any configurable
 *           options. Changing values of default items is not supported by the layout editor.
 */
@InApp
public class DefaultDeleteConfirmation implements CommandConfirmation {

	/**
	 * Singleton {@link DefaultDeleteConfirmation} instance.
	 */
	public static final DefaultDeleteConfirmation INSTANCE = new DefaultDeleteConfirmation();

	private DefaultDeleteConfirmation() {
		// Singleton constructor.
	}

	@Override
	public ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model,
			Map<String, Object> arguments) {
		if (model instanceof Collection<?>) {
			Collection<?> elements = (Collection<?>) model;
			if (elements.size() == 1) {
				return I18NConstants.CONFIRM_DELETE_ONE_ELEMENT__ELEMENT.fill(elements.iterator().next());
			}
			String arg = MetaLabelProvider.INSTANCE.getLabel(elements);
			return I18NConstants.CONFIRM_DELETE_MORE_ELEMENTS__ELEMENTS.fill(arg);
		}
		return I18NConstants.CONFIRM_DELETE_ONE_ELEMENT__ELEMENT.fill(model);
	}
}
