/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.table;

import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;

/**
 * {@link ModelNamingScheme} for the columns labels of a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableColumnsNaming extends UnrecordableNamingScheme<List<String>, TableColumnsNaming.TableColumnsName> {

	/**
	 * The {@link ModelName} of the {@link TableColumnsNaming}.
	 */
	public interface TableColumnsName extends ModelName, TableAspectName {

		/**
		 * Whether the name represents all columns or just the displayed columns.
		 */
		boolean getAllColumns();

		/**
		 * Setter for {@link #getAllColumns()}.
		 */
		void setAllColumns(boolean value);

	}

	/**
	 * Creates a new {@link TableColumnsNaming}.
	 */
	public TableColumnsNaming() {
		super(listOfStringType(), TableColumnsName.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	private static Class<List<String>> listOfStringType() {
		return (Class<List<String>>) (Class) List.class;
	}

	@Override
	public List<String> locateModel(ActionContext context, TableColumnsName name) {
		TableData tableData = (TableData) context.resolve(name.getTable());
		return ScriptTableUtil.getColumnLabels(tableData, name.getAllColumns());
	}

}

