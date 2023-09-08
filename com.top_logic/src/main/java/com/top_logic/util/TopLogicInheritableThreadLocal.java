/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Hashtable;


/**
 * Used to inherit the TopLogicThreadInfo to child threads.
 * The different thread get their own copy to allow independent modification.
 *
 * @author   <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class TopLogicInheritableThreadLocal extends InheritableThreadLocal {

    /** Our value in fact is a hashMap */
    @Override
	protected  Object initialValue()  {
        return new Hashtable ();
    }
    
	/**
	 * Get the value to be used for the child thread.
	 *
     * This will be cloend so then changes by the Parent will not influence
     * the child
	 *
	 * @param	parentValue		the ThreadLocal content of the parent thread.
	 * @return	the cloned parentValue
	 */
	@Override
	protected Object childValue (Object parentValue) {
        if (parentValue != null)
		    return ((Hashtable) parentValue).clone ();
        else
            return null;
	}
	
}
