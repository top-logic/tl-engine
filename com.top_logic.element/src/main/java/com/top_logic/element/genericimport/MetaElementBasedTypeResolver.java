/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.model.TLClass;

/**
 * The {@link MetaElementBasedTypeResolver} maps external types to {@link TLClass}s.
 * 
 * The mapping is configured and passed to the constructor.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedTypeResolver extends TypeResolverBase {

    /** 
     * Creates a {@link MetaElementBasedTypeResolver}.
     */
    public MetaElementBasedTypeResolver(Properties someProps) {
        super(someProps);
    }

    @Override
    protected String checkTypeConfiguration(String externalType, String internalType) {
        TLClass meta = MetaElementBasedImportBase.getUniqueMetaElement(internalType);
        if (meta != null) {
            return meta.getName();
        }
        return null;
    }
    
}

