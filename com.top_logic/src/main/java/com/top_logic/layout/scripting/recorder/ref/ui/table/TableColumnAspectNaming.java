/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.table;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.TableColumnRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;

/**
 * An {@link UnrecordableNamingScheme} about an aspect of a table column.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TableColumnAspectNaming<M, N extends TableColumnAspectNaming.TableColumnAspectName>
		extends UnrecordableNamingScheme<M, N> {

	/**
	 * The {@link ModelName} of the {@link TableColumnAspectNaming}.
	 */
	@Abstract
	public interface TableColumnAspectName extends ModelName, TableAspectName, TableColumnRef {

		// Sum interface

	}

	/** Creates a {@link TableColumnAspectNaming}. */
	public TableColumnAspectNaming(Class<M> modelClass, Class<N> nameClass) {
		super(modelClass, nameClass);
	}

	@Override
	public M locateModel(ActionContext context, TableColumnAspectName name) {
		TableData tableData = (TableData) context.resolve(name.getTable());
		String columnName = ScriptTableUtil.getColumnName(name, tableData);
		return locateTableColumnAspect(tableData, columnName);
	}

	/** Locate the aspect of the given table column. */
	protected abstract M locateTableColumnAspect(TableData tableData, String columnName);

}
