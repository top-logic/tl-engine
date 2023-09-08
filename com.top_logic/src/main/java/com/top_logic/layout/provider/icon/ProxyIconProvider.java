/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.icon;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link IconProvider} dispatching to other {@link IconProvider}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProxyIconProvider implements IconProvider {

	private IconProvider _impl;

	private IconProvider _fallback;

	/**
	 * Creates a {@link ProxyIconProvider}.
	 * 
	 * @param first
	 *        The first {@link IconProvider} to ask.
	 * @param fallback
	 *        The second {@link IconProvider} to ask, if the first one did not retrieve an icon.
	 */
	public ProxyIconProvider(IconProvider first, IconProvider fallback) {
		_impl = first;
		_fallback = fallback;
	}

	@Override
	public ThemeImage getIcon(Object object, Flavor flavor) {
		ThemeImage result = _impl.getIcon(object, flavor);
		if (result != null) {
			return result;
		}
		return _fallback.getIcon(object, flavor);
	}

}
