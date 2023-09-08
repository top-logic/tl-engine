/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.basic.TLID;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * The GenericValueMap 
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericValueMap {
    public String getType();
    public TLID getIdentifier();
    public String[] getAttributeNames();
    public void    setAttributeValue(String anAttribute, Object aValue);
    public Object  getAttributeValue(String anAttribute) throws NoSuchAttributeException;
    public boolean hasAttribute(String anAttribute);
}

