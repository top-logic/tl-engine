/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.component.TableComponent;

/**
 * The ColumnBuilder builds the list of column names for a table.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public interface ColumnBuilder {

    /**
     * Builds the list of column names for the given table component.
     *
     * @param aComponent
     *        the table component to build the column names list for.
     * @param defaultColumns
     *        the default columns as configured in the configuration section of the table
     *        component
     * @return the column names list for the given table component; must not be
     *         <code>null</code>
     */
    public String[] getColumnNames(TableComponent aComponent, String[] defaultColumns);

}
