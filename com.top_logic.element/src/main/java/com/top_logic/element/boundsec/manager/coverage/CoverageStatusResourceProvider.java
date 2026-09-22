/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.EnumResourceProvider;

/**
 * Resources of a {@link CoverageStatus}: its label and tooltip from the resources of the enum,
 * plus an icon telling a complete definition from an incomplete one at a glance.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CoverageStatusResourceProvider extends EnumResourceProvider {

	/** Icon of {@link CoverageStatus#COVERED}. */
	public static final ThemeImage COVERED_ICON = ThemeImage.cssIcon("bi bi-check-circle");

	/** Icon of {@link CoverageStatus#INCOMPLETE}. */
	public static final ThemeImage INCOMPLETE_ICON = ThemeImage.cssIcon("bi bi-exclamation-triangle");

	/** Icon of {@link CoverageStatus#EXEMPT}. */
	public static final ThemeImage EXEMPT_ICON = ThemeImage.cssIcon("bi bi-dash-circle");

	/** Singleton {@link CoverageStatusResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final CoverageStatusResourceProvider INSTANCE = new CoverageStatusResourceProvider();

	/**
	 * Creates a {@link CoverageStatusResourceProvider}.
	 */
	protected CoverageStatusResourceProvider() {
		super();
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof CoverageStatus status) {
			return switch (status) {
				case COVERED -> COVERED_ICON;
				case EXEMPT -> EXEMPT_ICON;
				case INCOMPLETE -> INCOMPLETE_ICON;
			};
		}
		return super.getImage(object, flavor);
	}

}
