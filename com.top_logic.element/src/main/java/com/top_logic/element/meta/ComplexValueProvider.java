/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;


/**
 * Implementations of this interface are used by {@link TLStructuredTypePart}s of custom data type to
 * evaluate the data and gui representation of a meta-attribute.
 * 
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public interface ComplexValueProvider<T> extends StorageMapping<T> {
    
    /**
     * Delivers the right OptionProvider for the ComplexValue.
     * 
     * @return an {@link OptionProvider}.
     */
    public OptionProvider getOptionProvider();
    
}
