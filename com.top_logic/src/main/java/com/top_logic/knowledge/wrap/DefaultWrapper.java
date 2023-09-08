/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The default wrapper wraps a knowledge object type in case that no
 * specific wrapper is registered for it.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DefaultWrapper extends AbstractWrapper {

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *  
     * @param    ko    The KnowledgeObject, must never be <code>null</code>.
     * 
     * @throws   NullPointerException  If the KO is <code>null</code>.
     */
    public DefaultWrapper(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * Get the single wrapper for the given knowledge object.
     */
    public static Wrapper getInstance(KnowledgeObject aKO) {
        return WrapperFactory.getWrapper(aKO);
    }
}
