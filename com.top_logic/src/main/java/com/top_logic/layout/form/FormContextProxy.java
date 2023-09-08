/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.form.model.FormContext;

/**
 * Able to get a FormContext
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public interface FormContextProxy {

    /**
     * The top-level form context of this form member. 
     */
    public FormContext getFormContext();
}
