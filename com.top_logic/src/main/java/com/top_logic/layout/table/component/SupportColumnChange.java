/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

/**
 * Interface for flexible defining of columns displayed by an implementing component.
 *
 * This interface will be used by the AttributedSearchFormComponent.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface SupportColumnChange {

    /**
     * Set the columns to be displayed.
     *
     * @param    someColumns    The array of columns, must not be <code>null</code> or empty.
     */
    public void setColumns(String[] someColumns);

    /**
     * Return the columns displayed in the table.
     *
     * @return    The requested columns, never <code>null</code> or empty.
     */
    public String[] getColumns();
}
