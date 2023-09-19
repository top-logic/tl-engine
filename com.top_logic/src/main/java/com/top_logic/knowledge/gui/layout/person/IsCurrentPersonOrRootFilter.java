/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class IsCurrentPersonOrRootFilter implements Filter {

	@Override
	public boolean accept(Object aObject) {
		
		if (ThreadContext.isAdmin()) {
			return true;
		}
		
		{
			UserInterface theSelected = Person.getUser(((Person) aObject));
			
			return Utils.equals(theSelected, TLContext.currentUser());
		}
	}

}
