/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Set a Value in a MergeTreeNode assuming it is a Wrapper.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SetWrapperTask extends MergeMessage {

    /** Name of the attribute to use when setting */
    protected String attName;

    /** The value to be set */
    protected Object value;

    /**
	 * Create a new SetWrapperTask with INFO level and a default message,
	 * 
	 * @param isApproveable
	 *        The user can choose if he/she wishes to perform this message.
	 */
    public SetWrapperTask(String anAttribute, Object aValue, boolean isApproveable) {
		super(INFO, I18NConstants.SET_VALUE_TASK.key(anAttribute), isApproveable);
        attName = anAttribute;
        value   = aValue;
    }

    /**
     * Call setValue({@link #attName}, {@link #value}) on {@link #getWrapper(MergeTreeNode)}.
     * 
     * @throws    Exception if executing the merge operation fails.
     */
    @Override
	public void perform(MergeTreeNode owner) throws Exception {
        if (!approved)
            return;
        Wrapper dest = getWrapper(owner);
        if (dest != null && dest.tValid()) {
            dest.setValue(attName, value);
        }
    }
    
    /** 
     * By default the Wrapper we try to set is the destination.
     * 
     * Override this to set the values somewhere eles (e.G. in the source)
     */
    protected Wrapper getWrapper(MergeTreeNode owner) {
       return (Wrapper) owner.getDest();
    }

}
