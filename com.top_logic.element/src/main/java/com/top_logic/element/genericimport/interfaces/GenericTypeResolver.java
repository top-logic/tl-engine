/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;


/**
 * The {@link GenericTypeResolver} maps the types of external objects (i.e. GenericValueMap)
 * to internal object types that can be understand by <i>TopLogic</i>.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericTypeResolver {
    /**
     * Return the internal type of the {@link GenericValueMap#getType()}
     */
    public String resolveType(GenericValueMap aDO);
    
    /**
     * Return the internal type of the given external type
     */
    public String resolveType(String anExternalType);
}

