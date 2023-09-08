/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A model builder creates a GUI model for a component. This interface separates 
 * the creation of the GUI model for a component from the component itself. The same 
 * component class may be used with different model builders to display different 
 * information in different contexts without subclassing the component.
 * 
 * For a concrete example, refer to 
 * <code>com.top_logic.element.layout.structured.StructureTreeModelBuilder</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelBuilder extends Serializable {

    /**
	 * Build up a GUI model for the given component.
	 * 
	 * @param businessModel
	 *        The business model of the component, i.e. value of {@link LayoutComponent#getModel()}.
	 * @param aComponent
	 *        The component to get the GUI model for, must not be <code>null</code>.
	 * 
	 * @return The requested GUI model, must not be <code>null</code>.
	 */
    public Object getModel(Object businessModel, LayoutComponent aComponent);

    /** 
     * Check, if this builder can process the given object in {@link #getModel(Object, LayoutComponent)}.
     * 
     * @param    aModel        The model to build a GUI model from, may be <code>null</code>.
     * @param    aComponent    The component calling this method, may be <code>null</code>.
     * @return   <code>true</code>, if {@link #getModel(Object, LayoutComponent)} can return a valid 
     *           GUI model for the given business model. 
     */
    public boolean supportsModel(Object aModel, LayoutComponent aComponent);
}
