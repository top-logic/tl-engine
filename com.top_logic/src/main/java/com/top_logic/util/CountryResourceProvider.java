/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Presents a {@link Country} by its name in the current language, under its flag.
 *
 * @see FlagIcon
 */
public class CountryResourceProvider extends DefaultResourceProvider {

	/** Singleton {@link CountryResourceProvider} instance. */
	public static final CountryResourceProvider INSTANCE = new CountryResourceProvider();

	/**
	 * Creates a {@link CountryResourceProvider}.
	 *
	 * @see #INSTANCE
	 */
	protected CountryResourceProvider() {
		// Singleton instance.
	}

	@Override
	public String getLabel(Object object) {
		if (!(object instanceof Country)) {
			return super.getLabel(object);
		}
		return ((Country) object).getName(Resources.getCurrentLocale());
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (!(object instanceof Country)) {
			return super.getImage(object, flavor);
		}
		return FlagIcon.forCode(((Country) object).getCode());
	}

}
