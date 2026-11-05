/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.dob.DataObjectException;

/**
 * The {@link GenericConverter} converts a {@link GenericValueMap} into another
 * {@link GenericValueMap}.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericConverter {
    /**
     * Converts the given imported data.
     *
     * @param aDO
     *        The imported data.
     * @param aCache
     *        The cache of already imported objects.
     * @return The converted data.
     * @throws DataObjectException
     *         If accessing the data fails.
     */
    public GenericValueMap convert(GenericValueMap aDO, GenericCache aCache) throws DataObjectException;
}

