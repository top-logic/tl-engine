/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BooleanLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link BooleanLabelProvider} instance.
	 */
	public static final BooleanLabelProvider INSTANCE = new BooleanLabelProvider();

	private BooleanLabelProvider() {
		// Singleton constructor.
	}

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof Boolean) {
			return Resources.getInstance().getString(
				((Boolean) anObject).booleanValue() ? I18NConstants.TRUE_LABEL : I18NConstants.FALSE_LABEL);
        }
		if (anObject == null) {
			return Resources.getInstance().getString(I18NConstants.NONE_LABEL);
        }
		return "?";
    }
}

