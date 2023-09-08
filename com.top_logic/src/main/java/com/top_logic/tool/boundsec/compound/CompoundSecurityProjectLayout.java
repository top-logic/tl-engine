/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * This Layout delegates the allow methods to a primary checker,
 * which in turn should delegate its allow methods to the secondary checker.
 * 
 * This mechanism is used in the context of layouts with a tree and a element view.
 * The tree must be visible only if the element view is visible for at least 
 * one of the trees nodes (elements). 
 * In addition nodes which can not be viewd in the element view 
 * must not be visible in the tree.
 * 
 * In such a scenario the tree is the primaryChecker, and the element view the secondary.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class CompoundSecurityProjectLayout extends CompoundSecurityLayout {

    public interface Config extends CompoundSecurityLayout.Config {
		@Name("primaryChecker")
		@Mandatory
		ComponentName getPrimaryChecker();

		@Name("secondaryChecker")
		@Mandatory
		ComponentName getSecondaryChecker();
	}

	/** The name of the component to which the Layout delegates its allow check. */
	protected ComponentName primaryCheckerName;
    
    /** The name of the Component to which the primary checker delegates its allow check. */
	protected ComponentName secondaryCheckerName;

    /** The component to which the Layout delegates its allow check. */
    protected transient CompoundSecurityBoundChecker primaryChecker;
    
    /** The Component to which the primary checker delegates its allow check. */
    protected transient BoundChecker secondaryChecker;

    public CompoundSecurityProjectLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        primaryCheckerName   = atts.getPrimaryChecker();
        secondaryCheckerName = atts.getSecondaryChecker();
    }

    /**
     * Get the primary checker.
     * 
     * @return the primary checker.
     */
    public CompoundSecurityBoundChecker getPrimaryChecker() {
        // initialize if not present yet
        if (primaryChecker == null) {
            // try my Children first
            LayoutComponent checker = this.getComponentByName(this.primaryCheckerName);
            if (checker == null) {
                checker =  getMainLayout().getComponentByName(this.primaryCheckerName);
            }
            if (checker == null) {
                throw new NullPointerException("No Primary Checker named '" 
                        + this.primaryCheckerName + "'");
            }
            primaryChecker = (CompoundSecurityBoundChecker) checker;
        }
        return primaryChecker;
    }
    
    /**
     * Get the primary checker.
     * 
     * @return the primary checker.
     */
    public BoundChecker getSecondaryChecker() {
        // initialize if not present yet
        if (secondaryChecker == null) {
            LayoutComponent checker = this.getComponentByName(this.secondaryCheckerName);
            if (checker == null) {
                checker =  getMainLayout().getComponentByName(this.secondaryCheckerName);
            }
            if (checker == null) {
                throw new NullPointerException("No Secondary Checker named '" 
					+ this.secondaryCheckerName + "' in " + getLocation());
            }
            secondaryChecker = (BoundChecker) checker;
        }
        return secondaryChecker;
    }
    
    /** 
     * Delegate to primary checker
     *
     * @see com.top_logic.tool.boundsec.BoundLayout#hideReason(Object)
     */
    @Override
	public ResKey hideReason(Object potentialModel) {
		return getPrimaryChecker().hideReason(potentialModel);
    }
    
    /** 
     * Delegate to primary checker
     *
     * @see com.top_logic.tool.boundsec.BoundLayout#allow(com.top_logic.tool.boundsec.BoundCommandGroup, com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
        return getPrimaryChecker().allow(aGroup, anObject);
    }
    
    /** 
     * Delegate to primary checker
     *
     * @see com.top_logic.tool.boundsec.BoundLayout#allow(com.top_logic.tool.boundsec.BoundCommandGroup, com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundObject anObject) {
        return getPrimaryChecker().allow(anObject);
    }
    
    /** 
     * Set the secondary checker as deledation destination in the primary checker.
     *
     * @see com.top_logic.tool.boundsec.compound.CompoundSecurityLayout#componentsResolved(InstantiationContext)
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
        super.componentsResolved(context);
        this.getPrimaryChecker().setDelegationDestination(this.getSecondaryChecker());
    }
    
    /** 
     * Get the selected object from the primary checker. This must be a BoundObject.
     * 
     * @return the selected object in the primary checker
     */
    @Override
	public BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
        Object theSelected = this.getPrimaryChecker().getSelected();

        return (theSelected instanceof BoundObject) ? (BoundObject) theSelected : null;
    }
    
    /**
     * Overriden to foreward anObject to primary checker.
     */
    @Override
	public void setCurrentObject(BoundObject anObject){
        CompoundSecurityBoundChecker primaryChecker = this.getPrimaryChecker();
        
        // If the primary checker is a layout component (e.g. a tree) then the component
        // must have a change to initialize the model. Maybe it is better to make this
        // in setSelected but this would be changed the current behavior. 
        // The code here hasn't so strong consequences.
        if (primaryChecker instanceof LayoutComponent) {
			LayoutComponent layoutComponent = (LayoutComponent) primaryChecker;
			layoutComponent.getModel();
		}
        
		primaryChecker.setSelected(anObject);
        super.setCurrentObject(anObject);
    }
    
}
