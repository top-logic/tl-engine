/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} that displays {@link Boolean}s with checkbox images.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BooleanResourceProviderNullAsFalse extends DefaultResourceProvider {

	/**
	 * Singleton {@link BooleanResourceProviderNullAsFalse} instance.
	 */
	public static final BooleanResourceProviderNullAsFalse INSTANCE = new BooleanResourceProviderNullAsFalse();

	private BooleanResourceProviderNullAsFalse() {
		// Singleton constructor.
	}

    @Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
        if (anObject instanceof Boolean) {
			return ((Boolean) anObject).booleanValue() ? com.top_logic.layout.table.renderer.Icons.TRUE_DISABLED
				: Icons.FALSE_DISABLED;
        }
		if (anObject == null) {
			return Icons.FALSE_DISABLED;
        }
		return null;
    }

	@Override
	public String getCssClass(Object anObject) {
		if (anObject instanceof Boolean || anObject == null) {
			return BooleanResourceProvider.BOOLEAN_CSS_CLASS;
		}
		return super.getCssClass(anObject);
	}

    @Override
	public String getLabel(Object booleanValue) {
		return "";
    }

}
