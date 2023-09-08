/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} displaying a {@link MetaObjectConfig} or an {@link AlternativeConfig}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaObjectConfigResourceProvider extends DefaultResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object instanceof MetaObjectName) {
			return ((MetaObjectName) object).getObjectName();
		}
		return super.getLabel(object);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof MetaObjectConfig) {
			if (((MetaObjectConfig) object).isAbstract()) {
				return Icons.TABLE_TEMPLATE;
			}
		}
		return super.getImage(object, flavor);
	}
}
