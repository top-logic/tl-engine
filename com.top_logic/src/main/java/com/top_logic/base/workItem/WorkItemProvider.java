/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.Collection;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Work item provider are resposible to determin work items for a given person.
 * Each implementation provides work items from a certain domain. 
 * The {@link WorkItemManager} collets the wprovieded work items from
 * the {@link WorkItemProvider}s configured.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface WorkItemProvider {

    public Collection getWorkItems(Person aPerson);
}
