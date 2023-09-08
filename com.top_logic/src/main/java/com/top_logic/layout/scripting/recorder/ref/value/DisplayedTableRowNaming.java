/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;

/**
 * {@link ModelNamingScheme} that is able to identity any object in the context of a
 * {@link TableModel} by its displayed row index.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DisplayedTableRowNaming extends ModelNamingScheme<TableData, Object, DisplayedTableRowNaming.Name> {

	/**
	 * {@link ModelName} created by {@link DisplayedTableRowNaming}.
	 */
	public interface Name extends ModelName {
		/**
		 * The row index.
		 */
		int getDisplayedRowIndex();

		/**
		 * @see #getDisplayedRowIndex()
		 */
		void setDisplayedRowIndex(int value);
	}

	/**
	 * Creates a {@link DisplayedTableRowNaming}.
	 *
	 */
	public DisplayedTableRowNaming() {
		super(Object.class, Name.class, TableData.class);
	}

	@Override
	protected Maybe<Name> buildName(TableData valueContext, Object model) {
		int index = valueContext.getTableModel().getDisplayedRows().indexOf(model);
		if (index < 0) {
			return Maybe.none();
		}
		Name name = createName();
		name.setDisplayedRowIndex(index);
		return Maybe.some(name);
	}

	@Override
	public Object locateModel(ActionContext context, TableData valueContext, Name name) {
		int index = name.getDisplayedRowIndex();
		List<?> rows = valueContext.getTableModel().getDisplayedRows();
		if (index >= rows.size()) {
			throw ApplicationAssertions.fail(name,
				"No such row index " + index + " in table with " + rows.size() + " rows.");
		}
		return rows.get(index);
	}

}
