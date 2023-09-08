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
 * {@link ResourceProvider} that displays {@link Boolean}s with tristate checkbox images.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BooleanResourceProvider extends DefaultResourceProvider {

	/**
	 * CSS class to use for displaying boolean values.
	 */
	public static final String BOOLEAN_CSS_CLASS = "tBoolean";

	/**
	 * CSS class to use for <code>null</code> values with static type boolean.
	 */
	public static final String BOOLEAN_NULL_CSS_CLASS = BOOLEAN_CSS_CLASS + " " + "tBooleanNull";

	/**
	 * Singleton {@link BooleanResourceProviderNullAsFalse} instance.
	 */
	public static final BooleanResourceProvider INSTANCE = new BooleanResourceProvider();

	private BooleanResourceProvider() {
		// Singleton constructor.
	}

    @Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
        if (anObject instanceof Boolean) {
			return ((Boolean) anObject).booleanValue() ? com.top_logic.layout.table.renderer.Icons.TRUE_DISABLED
				: Icons.FALSE_DISABLED;
        }
		if (anObject == null) {
			return Icons.NULL_DISABLED;
        }
		return null;
    }

	@Override
	public String getCssClass(Object anObject) {
		if (anObject instanceof Boolean) {
			return BOOLEAN_CSS_CLASS;
		}
		if (anObject == null) {
			return BOOLEAN_NULL_CSS_CLASS;
		}
		return super.getCssClass(anObject);
	}

    @Override
	public String getLabel(Object booleanValue) {
		return "";
    }

}
