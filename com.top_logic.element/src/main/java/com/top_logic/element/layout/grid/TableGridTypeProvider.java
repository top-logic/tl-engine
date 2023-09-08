/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.form.FormMember;

/**
 * {@link AbstractGridTypeProvider} for a table grid.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableGridTypeProvider extends AbstractGridTypeProvider {

	/**
	 * Singleton {@link TableGridTypeProvider} instance.
	 */
	public static final TableGridTypeProvider INSTANCE = new TableGridTypeProvider();

	private TableGridTypeProvider() {
		// Singleton constructor.
	}
	
	@Override
	protected Object toModel(Object row) {
		return asModel((FormMember) row);
	}

	private Object asModel(FormMember row) {
		return row.get(GridComponent.PROP_ATTRIBUTED);
	}


}
