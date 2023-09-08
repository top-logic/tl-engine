/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Locale;

import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} for {@link Locale}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LocaleLabelProvider implements LabelProvider {

	/** Singleton {@link LocaleLabelProvider} instance. */
	public static final LocaleLabelProvider INSTANCE = new LocaleLabelProvider();

	private LocaleLabelProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return "";
		}
		return Resources.getDisplayLanguage((Locale) object);
	}

}

