/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import com.top_logic.dob.bean.BeanMORepository;
import com.top_logic.dob.bean.BeanMetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Set a Value in a MergeTreeNode using BeanInfos.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SetBeanTask extends MergeMessage {

    /** Name of the attribute to use when setting */
    protected String attName;

    /** The value to be set */
    protected Object value;

    /** 
     * Create a new SetBeanTask with INFO level and a default meesage,
     * 
     * @param isApproveable The user can choose if he/she wishes to perform this message.
     */
    public SetBeanTask(String anAttribute, Object aValue, boolean isApproveable) {
		super(INFO, I18NConstants.SET_VALUE_TASK.key(anAttribute), isApproveable);
        attName = anAttribute;
        value   = aValue;
    }

    /**
     * Call method to set {@link #attName} to {@link #value} on {@link #getBean(MergeTreeNode)}.
     * 
     * @throws    Exception if executing the merge operation fails.
     */
    @Override
	public void perform(MergeTreeNode owner) throws Exception {
        if (!approved)
            return;
        Object dest = getBean(owner);
        if (dest != null) {
            BeanMetaObject meta = BeanMORepository.getInstance()
                .getMetaObject(dest.getClass());
            PropertyDescriptor desc = meta.getDescriptor(attName);
            Method writer = desc.getWriteMethod();
            if (writer == null) {
                throw new NoSuchAttributeException("Cannot write Attribute '" +
                        attName + "'");
            }
            writer.invoke(dest, new Object[] { value });
        }
    }
    
    /** 
     * By default the Baan we try to set is the destination.
     * 
     * Override this to set the values somewhere eles (e.G. in the source)
     */
    protected Object getBean(MergeTreeNode owner) {
       return owner.getDest();
    }

}
