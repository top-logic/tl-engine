/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.wrap;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;


/**
 * Flexible implementation of a wrapper for {@link com.top_logic.element.structured.StructuredElement TL elements}.
 *
 * This class will take care for its children, but not for the parent.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class WrapperTLElement extends AbstractBoundWrapper implements StructuredElement {

    /** Flag, if caching is enabled. */
    protected static Boolean USE_CACHE;

    /**
     * @param    ko         The KO to be wrapped.
     */
    public WrapperTLElement(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * @see com.top_logic.knowledge.wrap.Wrapper#getName()
     */
    @Override
	public String getName() {
		return (this.getString(NAME_ATTRIBUTE));
    }

}
