/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericCache {
    /** 
     * This method reinitialises the cache.
     */
    public void reload();
    public Object  get     (String aType, Object aKey);
    public boolean add     (String aType, Object aKey, Object aValue);
    public boolean contains(String aType, Object aKey);
    public void    merge   (GenericCache anotherCache);
}

