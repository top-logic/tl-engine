/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link Enum} constants based on system resources.
 */
public class EnumResourceProvider extends EnumLabelProvider implements ResourceProvider {

	/**
	 * Singleton {@link EnumLabelProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final EnumResourceProvider INSTANCE = new EnumResourceProvider();

	/**
	 * Creates a {@link EnumResourceProvider}.
	 */
	protected EnumResourceProvider() {
		super();
	}

	@Override
	public String getType(Object object) {
		return null;
	}

	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return null;
		}

		Enum<?> enumInstance = (Enum<?>) object;
		return Resources.getInstance().getString(ResKey.forEnum(enumInstance).tooltip(), null);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		return null;
	}

	@Override
	public String getLink(DisplayContext context, Object object) {
		return null;
	}

	@Override
	public String getCssClass(Object object) {
		return null;
	}

}
