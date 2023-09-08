/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.component.TableComponent;

/**
 * The DefaultColumnBuilder uses only the columns configured in the table component layout
 * description.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DefaultColumnBuilder implements ColumnBuilder {
    
    public static final DefaultColumnBuilder INSTANCE = new DefaultColumnBuilder();

    @Override
	public String[] getColumnNames(TableComponent aComponent, String[] defaultColumns) {
        return defaultColumns;
    }

}
