/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.resources;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.inspector.model.nodes.InspectorReflectionNode;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} for {@link Field}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaFieldResourceProvider extends DefaultResourceProvider {

	static final String PROTECTED_ICON = "/inspector/protected.png";

	@Override
	public String getLabel(Object object) {
		return field(object).getName();
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		Field field = field(object);
		boolean isPrivate = InspectorReflectionNode.isPrivate(field);

		return getTypeIcon(isPrivate, field.getType());
	}

	/**
	 * Icon based on visibility and type.
	 */
	public static ThemeImage getTypeIcon(boolean isPrivate, Class<?> type) {
		if (InspectorReflectionNode.isPrimitive(type)) {
			return isPrivate ? Icons.PRIVATE_PRIMITIVE
				: Icons.PUBLIC_PRIMITIVE;
		} else if (Map.class.isAssignableFrom(type)) {
			return isPrivate ? Icons.PRIVATE_MAP
				: Icons.PUBLIC_MAP;
		} else if (Collection.class.isAssignableFrom(type) || type.isArray()) {
			return isPrivate ? Icons.PRIVATE_LIST
				: Icons.PUBLIC_LIST;
		} else {
			return isPrivate ? Icons.PRIVATE
				: Icons.PUBLIC;
		}
	}

	private Field field(Object obj) {
		return (Field) obj;
	}

}
