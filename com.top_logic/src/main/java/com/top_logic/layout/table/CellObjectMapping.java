/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;

/**
 * This {@link Mapping} gets the value of a {@link CellObject}.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class CellObjectMapping implements Mapping {

	/**
	 * Singleton {@link CellObjectMapping} instance.
	 */
	public static final CellObjectMapping INSTANCE = new CellObjectMapping();

	private CellObjectMapping() {
		// Singleton constructor.
	}

	@Override
	public Object map(Object input) {
		if (input instanceof CellObject) {
			return ((CellObject) input).getValue();
		}
		return input;
	}

}
