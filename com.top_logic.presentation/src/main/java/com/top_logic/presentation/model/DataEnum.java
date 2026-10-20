/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration value with a name in the data representation of a presentation description.
 */
public interface DataEnum {

	/**
	 * The name of this value in the data representation.
	 */
	String id();

	/**
	 * The value of the given enumeration with the given {@link #id()}, <code>null</code> if there
	 * is none.
	 */
	static <E extends Enum<E> & DataEnum> E forId(Class<E> type, String id) {
		for (E value : type.getEnumConstants()) {
			if (value.id().equals(id)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * The {@link #id()}s of all values of the given enumeration.
	 */
	static <E extends Enum<E> & DataEnum> List<String> ids(Class<E> type) {
		List<String> result = new ArrayList<>();
		for (E value : type.getEnumConstants()) {
			result.add(value.id());
		}
		return result;
	}

}
