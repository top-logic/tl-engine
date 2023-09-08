/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.icon;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.annotate.InstancePresentation;

/**
 * {@link IconProvider} providing the same icons for all objects.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticIconProvider implements IconProvider {

	/**
	 * Creates an {@link IconProvider} for an {@link InstancePresentation} type annotation.
	 */
	public static IconProvider getInstance(InstancePresentation presentation) {
		if (presentation == null) {
			return IconProvider.NONE;
		}

		ThemeImage regular = nonNull(presentation.getIcon(), ThemeImage.none());
		ThemeImage large = nonNull(presentation.getLargeIcon(), regular);

		return new StaticIconProvider(regular, large);
	}

	private static ThemeImage nonNull(ThemeImage icon, ThemeImage defaultIcon) {
		return icon == null ? defaultIcon : icon;
	}

	private ThemeImage _large;

	private ThemeImage _regular;

	/**
	 * Creates a {@link StaticIconProvider}.
	 */
	public StaticIconProvider(ThemeImage regular, ThemeImage large) {
		_regular = regular;
		_large = large;
	}

	@Override
	public ThemeImage getIcon(Object object, Flavor flavor) {
		if (StaticIconProvider.isEnlarged(flavor)) {
			return _large;
		}
		return _regular;
	}

	/**
	 * Whether the given {@link Flavor} is non-null and {@link Flavor#implies(Flavor) implies}
	 * {@link Flavor#ENLARGED}.
	 */
	public static boolean isEnlarged(Flavor flavor) {
		return (flavor != null) && flavor.implies(Flavor.ENLARGED);
	}

}
