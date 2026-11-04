/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Utilities for constant interfaces describing the elements of an XML namespace.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NamespaceUtil {

	private static final String ID_CONSTANT_SUFFIX      = "_ID";

	private static final String ELEMENT_CONSTANT_SUFFIX = "_ELEMENT";
	
	private static final int    CONSTANT_MODIFIERS      = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

	/**
	 * Creates a map from element names to element IDs.
	 *
	 * <p>
	 * For each constant {@code X_ELEMENT} of the given class that has a corresponding integer
	 * constant {@code X_ELEMENT_ID}, the value of the first is mapped to the value of the second. A
	 * lookup of an unknown element name results in {@code -1}.
	 * </p>
	 *
	 * @param namespaceConstants
	 *        The class defining the element constants.
	 * @return The map from element names to their IDs.
	 */
	public static Map elementMap(Class namespaceConstants) {
		HashMap result = new HashMap() {
			@Override
			public Object get(Object key) {
				Object value = super.get(key);
				if (value == null) {
					return Integer.valueOf((-1));
				} 
				return value;
			}
		};
		
		Field[] constants = namespaceConstants.getDeclaredFields();
		for (int n = 0, cnt = constants.length; n < cnt; n++) {
			Field field = constants[n];
			if ((field.getModifiers() & CONSTANT_MODIFIERS) != CONSTANT_MODIFIERS) {
			    continue;
			}
			
			String fieldName = field.getName();
			if (fieldName.endsWith(ELEMENT_CONSTANT_SUFFIX)) {
				String name;
				try {
					name = (String) field.get(null);
				} catch (IllegalArgumentException e) {
					throw new UnreachableAssertion(e);
				} catch (IllegalAccessException e) {
					throw new UnreachableAssertion(e);
				}
				Integer id;
				try {
					String idFieldName = fieldName + ID_CONSTANT_SUFFIX;
					id = (Integer) namespaceConstants.getField(idFieldName).get(null);
					if (id.intValue() < 0) {
						throw new IllegalArgumentException(
							"ID constants must not be smaller than zero (see '" + namespaceConstants.getName() + "." + idFieldName + "').");
					}
				} catch (IllegalArgumentException e) {
					throw new UnreachableAssertion(e);
				} catch (SecurityException e) {
					throw new UnreachableAssertion(e);
				} catch (IllegalAccessException e) {
					throw new UnreachableAssertion(e);
				} catch (NoSuchFieldException e) {
					throw (IllegalArgumentException) new IllegalArgumentException(
						"Missing ID constant for '" + namespaceConstants.getName() + "." + fieldName + "'").initCause(e);
				}
				result.put(name, id);
			}
		}
		
		return result;
	}

}
