/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.decoratedTabBar;

/**
 * A decoration value listener registers itself at a 
 * {@link com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider}
 * in order to be informed in case the decoration value changes.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface DecorationValueListener {
    
    /**
     * Called by a decoration value provider in order to indicate 
     * that the decoration value changed.
     * 
     * @param aProvider  the decoration value provider, never null
     */
    public void receiveDecorationValueChanged(DecorationValueProvider aProvider);

}
