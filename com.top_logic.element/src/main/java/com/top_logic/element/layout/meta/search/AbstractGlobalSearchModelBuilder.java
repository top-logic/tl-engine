/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.model.TLClass;

/**
 * Abstract model builder for {@link AttributedSearchComponent} when the used meta element
 * is defined as a global meta element.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractGlobalSearchModelBuilder implements SearchModelBuilder {

    /** 
     * Creates a {@link AbstractGlobalSearchModelBuilder}.
     * 
     */
    public AbstractGlobalSearchModelBuilder() {
    }

    /**
     * @see com.top_logic.element.layout.meta.search.SearchModelBuilder#getMetaElement(java.lang.String)
     */
    @Override
	public TLClass getMetaElement(String aME) throws IllegalArgumentException {
		return MetaElementFactory.getInstance().getGlobalMetaElement(aME);
    }
}

