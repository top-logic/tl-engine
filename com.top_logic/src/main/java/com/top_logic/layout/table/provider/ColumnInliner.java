/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Set;

import com.top_logic.basic.func.Identity;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.AccessorMapping;
import com.top_logic.layout.accessors.MappingAccessor;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.filter.MappedCellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.model.TLClass;

/**
 * {@link GenericTableConfigurationProvider} creating columns for a reference reachable from the
 * original row object.
 * 
 * <p>
 * If a table is displayed that has a reference column <code>ref</code> of type <code>T</code>, then
 * the {@link ColumnInliner} can add columns to the original table that display the properties of
 * the object referenced by <code>ref</code>. The type <code>T</code> of the reference determines
 * the columns to be added to the table.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ColumnInliner extends GenericTableConfigurationProvider {
	private AccessorMapping<Object> _accessorMapping;

	private Identity<String> _columnMapping;

	/**
	 * Creates a {@link ColumnInliner}.
	 * 
	 * @param inlinedColumn
	 *        The column displaying the object to inline.
	 * @param contentTypes
	 *        The potential content types of the inlined column.
	 */
	public ColumnInliner(String inlinedColumn, Set<? extends TLClass> contentTypes) {
		super(contentTypes);

		_accessorMapping = new AccessorMapping<>(WrapperAccessor.INSTANCE, inlinedColumn);
		_columnMapping = Identity.getInstance();
	}

	@Override
	protected void configureColumn(TableConfiguration table, String columnName, ColumnInfo info,
			boolean isInitialDeclaration) {
		ColumnConfiguration existingColumn = table.getDeclaredColumn(columnName);
		if (existingColumn != null) {
			// Do not override columns that have been created by the original table
			// configuration.
			return;
		}

		super.configureColumn(table, columnName, info, false);

		ColumnConfiguration newColumn = table.getDeclaredColumn(columnName);
		if (newColumn != null) {
			wrapColumn(newColumn);
		}
	}

	private void wrapColumn(ColumnConfiguration column) {
		@SuppressWarnings("unchecked")
		Accessor<Object> accessor = column.getAccessor();
		column.setAccessor(wrapAccessor(accessor));
		column.setCellExistenceTester(wrapCellExistenceTester(column.getCellExistenceTester()));
	}

	private Accessor<Object> wrapAccessor(Accessor<Object> accessor) {
		return new MappingAccessor(accessor, _accessorMapping);
	}

	private CellExistenceTester wrapCellExistenceTester(CellExistenceTester cellExistenceTester) {
		return new MappedCellExistenceTester(cellExistenceTester, _accessorMapping, _columnMapping);
	}
}