/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.form.FormContainer;

/**
 * This interface provides a methods to enrich a given form container
 * with additioal members.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface FormMemberProvider {
    
    /**
     * Adds additional form members to the given container.
     *
     * @param aModel
     *        The model the members are created for.
     * @param aFormContainer
     *        The container to add the members to.
     */
    public void addFormMembers(Object aModel, FormContainer aFormContainer);

}

