/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.Serializable;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;


/**
 * This is a proxy Object for one or more Components.
 * 
 * The idea is that you do not really care how may components
 * (even none) you want to refer to.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class ComponentProxy implements Serializable {

    /** The Component, may be null when a no Components can not be found. */
    protected LayoutComponent[] components;
    
    /** 
     * Return the Components we proxy.
     * 
     * @return null when no components couls be found.
     */
     public LayoutComponent[] getComponents() {
        return components;
    }
    
     /**
      * Invalidate all my Components.
      */
     public void invalidate() {
         LayoutComponent comps[] = components;
         if (comps != null)
           for (int i = 0; i < comps.length; i++) {
               comps[i].invalidate();
         }
     }

     // abstract methods

    /**
     * Will be called by myOwner in {@link LayoutComponent#componentsResolved(InstantiationContext)}.
     */
    protected abstract void componentsResolved(LayoutComponent myOwner);
    
    /**
     * Return the name(s) of the referenced component(s).
     */
    protected abstract List<String> getComponentNames();

}
