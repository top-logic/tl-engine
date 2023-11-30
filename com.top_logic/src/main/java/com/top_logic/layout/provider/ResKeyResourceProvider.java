/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link ResKey} providing label and tooltip.
 * 
 * @see ResKey#tooltip()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton {@link ResKeyResourceProvider} instance.
	 */
	public static final ResKeyResourceProvider INSTANCE = new ResKeyResourceProvider();

	private ResKeyResourceProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return resources().getString(key(object));
	}

	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return null;
		}
		return resources().getString(key(object).tooltipOptional());
	}

	private Resources resources() {
		return Resources.getInstance();
	}

	private ResKey key(Object object) {
		return (ResKey) object;
	}

}
