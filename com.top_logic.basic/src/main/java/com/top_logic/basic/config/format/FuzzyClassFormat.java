/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.awt.Color;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * Extension of {@link ClassFormat} that supports "short names" for some classes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FuzzyClassFormat extends ClassFormat {

	/** Map mapping some simple class names to the corresponding class as shortcut */
	public static final Map<String, Class<?>> CLASSES_BY_NAME = new HashMap<>();
	static {
		addSimpleClass(CLASSES_BY_NAME, String.class);
		addSimpleClass(CLASSES_BY_NAME, Long.class);
		addSimpleClass(CLASSES_BY_NAME, long.class);
		addSimpleClass(CLASSES_BY_NAME, Integer.class);
		addSimpleClass(CLASSES_BY_NAME, int.class);
		addSimpleClass(CLASSES_BY_NAME, Short.class);
		addSimpleClass(CLASSES_BY_NAME, short.class);
		addSimpleClass(CLASSES_BY_NAME, Byte.class);
		addSimpleClass(CLASSES_BY_NAME, byte.class);
		addSimpleClass(CLASSES_BY_NAME, Double.class);
		addSimpleClass(CLASSES_BY_NAME, double.class);
		addSimpleClass(CLASSES_BY_NAME, Float.class);
		addSimpleClass(CLASSES_BY_NAME, float.class);
		addSimpleClass(CLASSES_BY_NAME, Character.class);
		addSimpleClass(CLASSES_BY_NAME, char.class);
		addSimpleClass(CLASSES_BY_NAME, Boolean.class);
		addSimpleClass(CLASSES_BY_NAME, boolean.class);
		addSimpleClass(CLASSES_BY_NAME, Class.class);
		addSimpleClass(CLASSES_BY_NAME, Date.class);

		addSimpleClass(CLASSES_BY_NAME, Map.class);
		addSimpleClass(CLASSES_BY_NAME, List.class);
		addSimpleClass(CLASSES_BY_NAME, Collection.class);
		addSimpleClass(CLASSES_BY_NAME, Set.class);

		addSimpleClass(CLASSES_BY_NAME, Color.class);
		addSimpleClass(CLASSES_BY_NAME, ResKey.class);

		addSimpleClass(CLASSES_BY_NAME, ConfigurationItem.class);
		addSimpleClass(CLASSES_BY_NAME, PolymorphicConfiguration.class);
	}

	private static void addSimpleClass(Map<String, Class<?>> cache, Class<?> entry) {
		cache.put(entry.getSimpleName(), entry);
	}

	/**
	 * Creates a new {@link FuzzyClassFormat}.
	 */
	public FuzzyClassFormat() {
		super();
	}

	@Override
	protected Class<?> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String valueAsString = propertyValue.toString();
		Class<?> classBySimpleName = CLASSES_BY_NAME.get(valueAsString);
		if (classBySimpleName != null) {
			return classBySimpleName;
		}
		return super.getValueNonEmpty(propertyName, valueAsString);
	}

}
