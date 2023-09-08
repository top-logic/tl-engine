/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericValueMapTypeResolver extends TypeResolverBase {

    public GenericValueMapTypeResolver(Properties aSomeProps) {
        super(aSomeProps);
    }

    @Override
    protected String checkTypeConfiguration(String externalType, String internalType) {
        return internalType;
    }
    
    @Override
    public String resolveType(String aType) {
        return aType;
    }
}

