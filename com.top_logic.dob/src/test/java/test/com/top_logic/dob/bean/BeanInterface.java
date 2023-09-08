/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.bean;

/**
 * Helper interface to Test the {@link com.top_logic.dob.bean.BeanDataObject}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public interface BeanInterface {
  
    /** A simple accessor method to Test the BeanDO */
    public abstract String getString();
  
    /** A simple accessor method to Test the BeanDO */
    public abstract void setString(String s);
  
    /** This should result in a Security Exception */
    public abstract String getNothing();
}