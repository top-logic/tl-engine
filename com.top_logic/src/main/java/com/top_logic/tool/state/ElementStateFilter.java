/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import com.top_logic.basic.col.Filter;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ElementStateFilter implements Filter {

    private ElementState state;

    /** 
     * Create a new instance of this class.
     */
    public ElementStateFilter(String aState) {
        this(StateManager.getManager().getState(aState));
    }

    /** 
     * Create a new instance of this class.
     */
    public ElementStateFilter(ElementState aState) {
        if (aState == null) {
            throw new IllegalArgumentException("Given state is null.");
        }

        this.state = aState;
    }

    /** 
     * accept either ElementStates or String mathich the states key. 
     */
    @Override
	public boolean accept(Object anObject) {
        if (anObject instanceof ElementState) {
            return (this.state.equals(anObject));
        }
        else if (anObject instanceof String) {
            return (this.state.getKey().equals(anObject));
        }
        else {
            return false;
        }
    }

}
