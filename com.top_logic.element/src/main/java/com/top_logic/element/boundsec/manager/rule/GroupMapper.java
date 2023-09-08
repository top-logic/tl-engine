/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Maps Objects to groups
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class GroupMapper implements Mapping {

    @Override
	public Object map(Object anObject) {
        if (anObject == null) {
            return null;
        }
        if (anObject instanceof Group) {
            return anObject;
        }
        if (anObject instanceof Person) {
            return ((Person) anObject).getRepresentativeGroup();
        }
        return null;
    }

}
