/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;

/**
 * The GenericConverter converts a {@link DataObject} into another {@link DataObject}
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericConverter {
    public GenericValueMap convert(GenericValueMap aDO, GenericCache aCache) throws DataObjectException;
}

