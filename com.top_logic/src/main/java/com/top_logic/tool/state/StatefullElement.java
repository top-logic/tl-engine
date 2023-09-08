/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

/**
 * Interface to mark objects which have a state
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface StatefullElement {
    
    /**
     * the State of this StatefullElement
     */
    public ElementState getState();
    
    /**
     * sets the state of the element to the given State
     * 
     * @param aState the new State of the element
     */
    public void setState(ElementState aState);
    
}
