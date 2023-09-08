/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.util;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;

/**
 * Filter that will accept Wrapper with a given name.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class WrapperNameFilter implements Filter {

    /** The name we check for */
    protected String name;
    
    /**
     * Cerate a Filter for given name.
     */
    public WrapperNameFilter(String aName) {
        name = aName;
    }

    /** 
     * Accept Wrappers with given name.
     */
    @Override
	public boolean accept(Object anObject) {
        if (anObject instanceof Wrapper) try {
            String theName = ((Wrapper)anObject).getName();

            return (theName == null) ? name == null : theName.equals(name);
        }
        catch (WrapperRuntimeException wx) {
            Logger.info("Failed to accept", wx, this);
        }

        return false;
    }
}
