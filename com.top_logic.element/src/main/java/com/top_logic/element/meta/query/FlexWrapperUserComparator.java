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
 * Compares two {@link Wrapper}s. If both queries have the same creator they are sorted by name,
 * otherwise the wrappers owned by the current user are first.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class FlexWrapperUserComparator implements Comparator {
	
	public static final FlexWrapperUserComparator INSTANCE = new FlexWrapperUserComparator();

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 instanceof Wrapper && o2 instanceof Wrapper) {
			Person user = TLContext.getContext().getCurrentPersonWrapper();
			
			Wrapper sq1 = (Wrapper) o1;
			Wrapper sq2 = (Wrapper) o2;
			{
				Person c1 = sq1.getCreator();
				Person c2 = sq2.getCreator();

				if (user == c1 && user == c2) {
					return sq1.compareTo(sq2);
				}
				else if(c1 == user) {
					return -1;
				}
				else if(c2 == user) {
					return 1;
				}
				else {
					return sq1.compareTo(sq2);
				}
			}
		}
		else {
			throw new ClassCastException("objects '" + o1 + "' and '" + o2 + "' are not comparable");
		}
	}

}
