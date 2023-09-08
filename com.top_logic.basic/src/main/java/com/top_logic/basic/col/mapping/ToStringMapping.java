/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.mapping;

import com.top_logic.basic.col.Mapping;

/**
 * {@link Mapping} calling {@link Object#toString()} on the given object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToStringMapping implements Mapping<Object, String> {

	/**
	 * Singleton {@link ToStringMapping} instance.
	 */
	public static final ToStringMapping INSTANCE = new ToStringMapping();

	private ToStringMapping() {
		// Singleton constructor.
	}

	@Override
	public String map(Object input) {
		if (input == null) {
			return "";
		}
		return input.toString();
	}

}
