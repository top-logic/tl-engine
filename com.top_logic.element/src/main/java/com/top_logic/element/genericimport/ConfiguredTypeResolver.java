/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ConfiguredTypeResolver extends TypeResolverBase {

    public ConfiguredTypeResolver(Properties someProps) {
        super(someProps);
    }

    @Override
    protected String checkTypeConfiguration(String externalType, String internalType) {
        return internalType;
    }

}
