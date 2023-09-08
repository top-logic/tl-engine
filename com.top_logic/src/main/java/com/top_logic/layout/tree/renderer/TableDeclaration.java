/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.util.List;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;

/**
 * Declaration for configuring the table rendered by a {@link TreeTableRenderer}.
 * 
 * @see TreeTableRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableDeclaration {

	/**
	 * The {@link Accessor} to access column values of node objects.
	 */
	Accessor getAccessor();

	/**
	 * The list of column names to render.
	 */
	List getColumnNames();

	/**
	 * Whether to render a table header. 
	 */
	boolean hasHeader();

	/**
	 * The {@link ResourceProvider} to use for displaying node object, when
	 * writing a {@link ColumnDeclaration#DEFAULT_COLUMN}.
	 */
	ResourceProvider getResourceProvider();

	/**
	 * The {@link ColumnDeclaration} for the given {@link #getColumnNames() column name}.
	 */
	ColumnDeclaration getColumnDeclaration(String columnName);

	/**
	 * The resource prefix for all internationalized texts displayed in the
	 * table.
	 */
	ResPrefix getResourcePrefix();

}
