/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Classes implementing this interface provide some sort of a template
 * providing data usefull for creation, initialization, and processing
 * of objects of the class.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface TemplateAware extends Wrapper {

    /**
     * the template to be used for this object, may be null.
     */
    public Wrapper getTemplate();
}
