/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.util.BoundSecurityFilter;

/**
 * Security aware person model builder.
 * 
 * The builder will return all persons alive and adapt the {@link BoundSecurityFilter}
 * to that list to hide persons not allowed to be seen by the current user.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonSecurityModelBuilder extends PersonModelBuilder {

	/**
	 * Singleton {@link PersonSecurityModelBuilder} instance.
	 */
	public static final PersonSecurityModelBuilder INSTANCE = new PersonSecurityModelBuilder();

	private PersonSecurityModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        List   theList   = new ArrayList(super.getModel(businessModel, aComponent));
        Filter theFilter = new BoundSecurityFilter();

        for (Iterator theIt = theList.iterator(); theIt.hasNext();) {
            if (!theFilter.accept(theIt.next())) {
                theIt.remove();
            }
        }

        return theList;
    }
}

