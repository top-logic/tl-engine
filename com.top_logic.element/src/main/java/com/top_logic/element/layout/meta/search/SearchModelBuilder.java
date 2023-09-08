/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.TLClass;

/**
 * Extend the normal model builder to provide the meta element out of a given name.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface SearchModelBuilder extends ModelBuilder {

    /**
	 * Return the meta element defined by the given name.
	 * 
	 * @param aMetaElement
	 *        The name of the meta element to look up, must not be <code>null</code>.
	 * @return The requested meta element, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If requesting the meta element fails for a reason.
	 */
    public TLClass getMetaElement(String aMetaElement) throws IllegalArgumentException;
}

