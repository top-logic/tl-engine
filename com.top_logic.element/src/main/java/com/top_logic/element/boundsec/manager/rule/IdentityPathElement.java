/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * The IdentityPathElement is used for rules with no path elements to map some roles to
 * other roles.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class IdentityPathElement extends PathElement {

    public IdentityPathElement() {
        super((String)null, false);
    }

    @Override
	public Collection getSources(Wrapper aDestination) {
        return CollectionUtil.intoList(aDestination);
    }

    @Override
	public Collection getValues(Wrapper aBase) {
        return CollectionUtil.intoList(aBase);
    }

}
