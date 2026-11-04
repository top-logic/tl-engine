/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

/**
 * Cache of the objects known to an import, indexed by internal type and key.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericCache {
    /** 
     * This method reinitialises the cache.
     */
    public void reload();

    /**
     * The cached object of the given type with the given key.
     *
     * @param aType
     *        The internal type name.
     * @param aKey
     *        The key of the object.
     * @return The cached object, or {@code null} if there is none.
     */
    public Object  get     (String aType, Object aKey);

    /**
     * Adds an object to this cache.
     *
     * @param aType
     *        The internal type name.
     * @param aKey
     *        The key of the object.
     * @param aValue
     *        The object to cache.
     * @return Whether the object was added.
     */
    public boolean add     (String aType, Object aKey, Object aValue);

    /**
     * Whether this cache contains an object of the given type with the given key.
     *
     * @param aType
     *        The internal type name.
     * @param aKey
     *        The key of the object.
     */
    public boolean contains(String aType, Object aKey);

    /**
     * Adds the contents of the given cache to this cache.
     *
     * @param anotherCache
     *        The cache to merge into this one.
     */
    public void    merge   (GenericCache anotherCache);
}

