/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.Collection;

import com.top_logic.basic.Named;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.state.State;

/**
 * A work item is an abstraction to things "to be done". 
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface WorkItem extends Named {

    public Object     getSubject();

	public String getWorkItemType();

    public Collection getAssignees();

    public State      getState();
    
    public Person     getResponsible();
}
