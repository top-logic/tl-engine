/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.StringServices;

/**
 * Tool for easier {@link Object#toString()} implementations.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NameBuilder {
	
	private final StringBuilder nameBuffer = new StringBuilder();
	
	private boolean nothingAdded = true;

	/**
	 * Creates a new {@link NameBuilder} for the given object.
	 * 
	 * @param objectToName
	 *        The object this name builder should name. Must not be <code>null</code>.
	 */
	public NameBuilder(Object objectToName) {
		nameBuffer.append(objectToName.getClass().getSimpleName());
		if (objectToName instanceof Enum<?>) {
			Enum<?> enumToName = (Enum<?>) objectToName;
			nameBuffer.append(".").append(enumToName.name());
			return;
		}
		nameBuffer.append("(");
	}

	/**
	 * Finishes building the name by returning the full name.
	 */
	public String build() {
		if (!nothingAdded) {
			nameBuffer.delete(nameBuffer.length() - 2, nameBuffer.length());
		}
		return nameBuffer.append(")").toString();
	}

	/**
	 * Same as {@link #buildName()}.
	 * <p>
	 * Kept for compatibility with existing code.
	 * </p>
	 */
	public String buildName() {
		return build();
	}

	/**
	 * Add the given class under the given (field) name to the full object name.
	 * <p>
	 * Used {@link Class#getSimpleName()} for the class name.
	 * </p>
	 */
	public NameBuilder add(String name, Class<?> theClass) {
		nothingAdded = false;
		if (theClass == null) {
			return add(name, "null");
		}
		return add(name, "Class(" + theClass.getSimpleName() + ")");
	}

	/**
	 * Add the given {@link Collection} under the given (field) name to the full object name.
	 * <p>
	 * The entries are joined with ", ".
	 * </p>
	 */
	public NameBuilder add(String name, Collection<?> collection) {
		nothingAdded = false;
		nameBuffer.append(name).append(" = ");
		if (collection == null) {
			nameBuffer.append("null");
		} else {
			nameBuffer.append(collection.getClass().getSimpleName()).append("(");
			for (Object entry : collection) {
				nameBuffer.append(entry).append(", ");
			}
			if (!collection.isEmpty()) {
				nameBuffer.delete(nameBuffer.length() - 2, nameBuffer.length());
			}
			nameBuffer.append(")");
		}
		nameBuffer.append(", ");
		return this;
	}

	/**
	 * Add the given {@link Map} under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, Map<?, ?> map) {
		nothingAdded = false;
		nameBuffer.append(name).append(" = ");
		if (map == null) {
			nameBuffer.append("null");
		} else {
			nameBuffer.append(map.getClass().getSimpleName()).append("(");
			for (Entry<?, ?> entry : map.entrySet()) {
				nameBuffer.append(entry.getKey()).append(" -> ").append(entry.getValue()).append(", ");
			}
			if (!map.isEmpty()) {
				nameBuffer.delete(nameBuffer.length() - 2, nameBuffer.length());
			}
			nameBuffer.append(")");
		}
		nameBuffer.append(", ");
		return this;
	}

	/**
	 * Add the given enum under the given (field) name to the full object name.
	 * <p>
	 * Uses {@link Enum#name()} as representation for the enum value.
	 * </p>
	 */
	public NameBuilder add(String name, Enum<?> value) {
		String stringValue;
		if (value == null) {
			stringValue = "null";
		} else {
			stringValue = value.name();
		}
		return add(name, stringValue);
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 * <p>
	 * Uses {@link StringServices#debug(Object)} as string representation of the value.
	 * </p>
	 */
	public NameBuilder add(String name, Object value) {
		return add(name, StringServices.debug(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, boolean value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, byte value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, short value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, int value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, long value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, char value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, float value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, double value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given object under the given (field) name to the full object name.
	 * <p>
	 * Converts the array to a {@link String}.
	 * </p>
	 */
	public NameBuilder add(String name, char[] value) {
		return add(name, String.valueOf(value));
	}

	/**
	 * Add the given value under the given (field) name to the full object name.
	 */
	public NameBuilder add(String name, String value) {
		nothingAdded = false;
		nameBuffer.append(name).append(" = ").append(value).append(", ");
		return this;
	}

	/**
	 * Add the given unnamed value to the full object name.
	 * 
	 * @param value
	 *        Is allowed to be null.
	 * @return This {@link NameBuilder} for convenient method chaining. Never null.
	 */
	public NameBuilder addUnnamed(Object value) {
		return addUnnamed(StringServices.debug(value));
	}

	/**
	 * Add the given unnamed value to the full object name.
	 * 
	 * @param value
	 *        Is allowed to be null.
	 * @return This {@link NameBuilder} for convenient method chaining. Never null.
	 */
	public NameBuilder addUnnamed(String value) {
		nothingAdded = false;
		nameBuffer.append(value).append(", ");
		return this;
	}

	/**
	 * Builds a name for an object which has only a single value which therefore does not need a
	 * name.
	 * <p>
	 * Example result: <code>MyProxy(innerValue)</code>
	 * </p>
	 * 
	 * @param objectToName
	 *        Is not allowed to be null.
	 * @param onlyAttribute
	 *        Is allowed to be null.
	 * @return Never null.
	 */
	public static String buildName(Object objectToName, String onlyAttribute) {
		return new NameBuilder(objectToName).addUnnamed(onlyAttribute).buildName();
	}

}
