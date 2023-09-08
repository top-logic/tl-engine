/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Comparator;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Compares two {@link Wrapper}s. They are sorted first by creator (current user comes first) and
 * then by name.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class FlexWrapperAdminComparator implements Comparator {

	public static final FlexWrapperAdminComparator INSTANCE = new FlexWrapperAdminComparator();

	@Override
	public int compare(Object o1, Object o2) {

	    Person currentUser = TLContext.getContext().getCurrentPersonWrapper();
		Wrapper sq1 = (Wrapper) o1;
		Wrapper sq2 = (Wrapper) o2;
		
		{
			Person creator1 = sq1.getCreator();
			Person creator2 = sq2.getCreator();
			
			if (currentUser == creator1 && currentUser == creator2) {
				return sq1.compareTo(sq2);
			}
			else if (creator1 == currentUser) {
				return -1;
			}
			else if (creator2 == currentUser) {
				return 1;
			}
			else {
				String n1 = creator1.wasAlive() ? creator1.getFullName() : creator1.getID().toString();
				String n2 = creator2.wasAlive() ? creator2.getFullName() : creator2.getID().toString();
			    
		        int res = n1.compareTo(n2);
		        if (res == 0) {
		            return sq1.compareTo(sq2);
		        }
		        return res;
			}
		}
	}
}
