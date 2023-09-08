/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.decoratedTabBar;

/**
 * Implement this Interface in a Component to decorate an associated Tabber.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface DecorationValueProvider {
    
    /**
     * Return an Array of String that will formatted in a Tabber Label.
     * 
     * A i18n for such a Lable might look like "Date for {0} at {1, date }
     * 
     * TODO TSA shoud better return Obect[] to allow mor flexibele formatting.
     * 
     * @return e.g. new String [] { model.getName() } null to indicate no
     */
    public String[] getTabBarDecorationValues();
    
    /**
     * Register the given {@link DecorationValueListener}. 
     * All registered listeners have to be notified if the decoration valies change.
     * 
     * @param aListener  the listener to be added
     */
    public void registerDecorationValueListener(DecorationValueListener aListener);

	/**
	 * Unregisters the given {@link DecorationValueListener}.
	 * 
	 * @param aListener
	 *        the listener to be added
	 * 
	 * @see #registerDecorationValueListener(DecorationValueListener)
	 */
	boolean unregisterDecorationValueListener(DecorationValueListener aListener);
    
}
