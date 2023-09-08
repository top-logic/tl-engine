/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.tree;

import com.top_logic.basic.col.Filter;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface FilterModelBuilder extends ModelBuilder {

    /** 
     * Build up a GUI model for the given component.
     * 
     * @param    aComponent    The component to get the GUI model for, must not be <code>null</code>.
     * @param    useFilter     Indicates that the internal filter is to be applied
     * @return  The requested GUI model, must not be <code>null</code>.
     */
    public Object getModel(LayoutComponent aComponent, boolean useFilter);
    
    /**
     * Get the filter used internally
     */
    public Filter getFilter(LayoutComponent aComponent);
    
}

