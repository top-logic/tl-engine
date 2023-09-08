/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.dob.DataObjectException;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ConfiguredConverter extends AbstractGenericConverter {

    public ConfiguredConverter(Properties someProps) {
        super(someProps);
    }

    @Override
    protected void postConvert(GenericValueMap aSource, GenericValueMap aResult, GenericCache aCache) throws DataObjectException {
    }

}
