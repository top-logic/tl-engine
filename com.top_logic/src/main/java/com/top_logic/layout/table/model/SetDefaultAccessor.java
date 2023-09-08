/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.Accessor;

/**
 * {@link DefaultColumnAdaption} that sets a given accessor to the default column.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetDefaultAccessor extends DefaultColumnAdaption {

	private final Accessor _defaultAccessor;

	/**
	 * Creates a new {@link SetDefaultAccessor}.
	 * 
	 * @param defaultAccessor
	 *        New {@link Accessor} for default column.
	 */
	public SetDefaultAccessor(Accessor defaultAccessor) {
		_defaultAccessor = defaultAccessor;
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		defaultColumn.setAccessor(_defaultAccessor);
	}
}
