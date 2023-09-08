/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Decorate the TabBar with the Current Persons full name.
 */
public class StandardTitleDecorator implements DecorationValueProvider {

    /**
     * Return the FullName of current Person at [0].
     *
     * Will return some String inidcation the Problem otherwise
     */
    @Override
	public String[] getTabBarDecorationValues() {
		TLContext theContext = TLContext.getContext();
        Person    thePerson  = null;
        String[]  theResult  = new String[3];

        theResult[0]="[No Current Person]" ;

        Resources theRes = Resources.getInstance();
		theResult[1] = Version.getApplicationName();
		theResult[2] = Version.getApplicationVersion().getVersionString();

        if (theContext != null) {
            thePerson = theContext.getCurrentPersonWrapper();
        }
        if (thePerson != null) {
			theResult[0] = thePerson.getFullName();
        }
        return (theResult);
    }

    /**
     * @see com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider#registerDecorationValueListener(com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener)
     */
    @Override
	public void registerDecorationValueListener(DecorationValueListener aListener) {
        // TODO MGA this is empty because ...
    }

	@Override
	public boolean unregisterDecorationValueListener(DecorationValueListener aListener) {
		// TODO MGA this is empty because ...
		return false;
	}

}