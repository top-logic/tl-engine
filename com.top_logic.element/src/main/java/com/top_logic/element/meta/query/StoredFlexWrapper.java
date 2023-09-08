/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.LegacyFlexWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class StoredFlexWrapper extends LegacyFlexWrapper {

	public StoredFlexWrapper(KnowledgeObject ko) {
	    super(ko);
    }

    /**
     * Get an instance for the given KO.
     *
     * @param aKO   the KO
     * @return an instance for the given KO. <code>null</code> if it couldn't be found.
     */
    public static StoredFlexWrapper getStoredQuery(KnowledgeObject aKO) {
        return (StoredFlexWrapper) WrapperFactory.getWrapper(aKO);
    }


}
