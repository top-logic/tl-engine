/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.resources;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} for {@link PropertyDescriptor}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertyDescriptorResourceProvider extends DefaultResourceProvider {

	@Override
	public String getLabel(Object object) {
		return property(object).getPropertyName();
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		PropertyDescriptor property = property(anObject);
		Hidden hidden = property.getAnnotation(Hidden.class);
		boolean isPrivate = hidden != null && hidden.value();

		switch (property.kind()) {
			case ITEM:
				return isPrivate ? Icons.PRIVATE : Icons.PUBLIC;

			case ARRAY:
			case LIST:
				return isPrivate ? Icons.PRIVATE_LIST
					: Icons.PUBLIC_LIST;
			case MAP:
				return isPrivate ? Icons.PRIVATE_MAP
					: Icons.PUBLIC_MAP;

			case DERIVED:
			case PLAIN:
			case REF:
			case COMPLEX:
				return isPrivate ? Icons.PRIVATE_PRIMITIVE
					: Icons.PUBLIC_PRIMITIVE;
		}
		return super.getImage(anObject, aFlavor);
	}

	private PropertyDescriptor property(Object object) {
		return (PropertyDescriptor) object;
	}

}
