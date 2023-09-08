/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link LabelProvider} for {@link AttributeConfig} declarations resolving the
 * {@link AttributeConfig#getAttributeName()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeConfigResourceProvider extends DefaultResourceProvider {

	/**
	 * Singleton {@link AttributeConfigResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final AttributeConfigResourceProvider INSTANCE = new AttributeConfigResourceProvider();

	private AttributeConfigResourceProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof AttributeConfig) {
			return ((AttributeConfig) object).getAttributeName();
		}
		return null;
	}
}
