/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.layout.tree.renderer.TableDeclaration;

/**
 * Instances of implementing classes are responsible for creating and initializing a
 * {@link TableDeclaration} instance to be used for rendering tables.
 *
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public interface TableDeclarationProvider {

    /**
     * Returns a fully initialized {@link TableDeclaration} instance defining
     * the table columns along with all other necessary attributes.
     *
     * @return a fully initialized {@link TableDeclaration} instance, never
     *         <code>null</code>
     */
    public TableDeclaration getTableDeclaration();

}
