/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.base.security.SecurityConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.TLContext;

/**
 * A CompoundSecurityLayout provides a logical representation of a technical view.
 *
 * The structure the the parts of the layouts into logic groups as experienced
 * by the user. The BoundSecurityLayout is too fine-grained (has too many nesting levels).
 * CompoundSecurityLayout allows to define the security for a group of components.
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public class CompoundSecurityLayout extends BoundLayout {

	/**
	 * Configuration options for {@link CompoundSecurityLayout}.
	 */
	@TagName(Config.TAG_NAME)
    public interface Config extends BoundLayout.Config {

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "securityLayout";

		@Override
		@ClassDefault(CompoundSecurityLayout.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Name(ATT_USE_DEFAULT_CHECKER)
		@BooleanDefault(false)
		boolean getUseDefaultChecker();

		@Name(ATT_SECURITY_DOMAIN)
		@Nullable
		String getSecurityDomain();

		@Override
		@Mandatory
		ComponentName getName();
	}

	private static final String ATT_SECURITY_DOMAIN = "securityDomain";

	private static final String ATT_USE_DEFAULT_CHECKER = "useDefaultChecker";

    /** Flag if BoundHelper.getInstance().getDefaultObject() is used as current object(). */
    boolean useDefaultChecker;

    /** The Sum of the command groups of all children */
	private Collection<BoundCommandGroup> commandGroups;

    private String securityDomain;

    /**
     * Create a CompoundSecurityLayout from XML which is the default usage.
     */
    public CompoundSecurityLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        this.useDefaultChecker = atts.getUseDefaultChecker();
		this.securityDomain = atts.getSecurityDomain();
    }

    /**
     * Collect the CommandGroups of the children
     *
     * @see com.top_logic.tool.boundsec.BoundLayout#getCommandGroups()
     */
    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
        if (this.commandGroups == null || this.commandGroups.isEmpty()) {
            return Collections.singletonList(this.getDefaultCommandGroup());
        } else {
            return this.commandGroups;
        }
    }

    public String getSecurityDomain() {
        return this.securityDomain;
    }

    /**
     * This method looks up a command group by its id.
     * Only command groups registered at this component are taken into account.
     *
     * @return the command group with the given id, <code>null</code> otherwise.
     */
    public BoundCommandGroup getCommandGroupById(String aCommandGroupId) {
		Collection<BoundCommandGroup> theCommandGroups = getCommandGroups();
        if (theCommandGroups == null)
            return null;
		Iterator<BoundCommandGroup> theIt = theCommandGroups.iterator();
        while (theIt.hasNext()) {
			BoundCommandGroup theCG = theIt.next();
            if (theCG.getID().equals(aCommandGroupId))
                return theCG;
        }
        return null;
    }

    /**
     * Initialize the command groups with all the command groups of your children.
     *
     * @see com.top_logic.mig.html.layout.LayoutContainer#componentsResolved(InstantiationContext)
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
        super.componentsResolved(context);

        try {
            CompoundSecurityLayoutCommandGroupCollector theVisitor = createCommandgroupCollector();
            this.acceptVisitorRecursively(theVisitor);
            this.commandGroups = theVisitor.getCommandGroups();

            if (!useDefaultChecker) {
                LayoutComponent theMaster = this.getMaster();
                if (!(theMaster instanceof BoundComponent)) {
                    CompoundSecurityLayout thePL = CompoundSecurityLayout.getNearestCompoundLayout(this);
                    if (thePL != null) {
                        useDefaultChecker = thePL.useDefaultChecker;
                    }
                }

            }
        }
        catch (Exception e) {
            Logger.error("...", e, this);
        }

        if (Logger.isDebugEnabled(this)) {
            Logger.debug(this.getName() + ": " + this.commandGroups, this);
        }
    }

    /**
     * Create a Visitor as configured by "security.layout.collector.name"
     */
    protected static synchronized CompoundSecurityLayoutCommandGroupCollector createCommandgroupCollector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		SecurityConfiguration securityConfiguration =
			ApplicationConfig.getInstance().getConfig(SecurityConfiguration.class);
		PolymorphicConfiguration<? extends CompoundSecurityLayoutCommandGroupCollector> commandGroupCollector =
			securityConfiguration.getLayout().getCommandGroupCollector();
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(commandGroupCollector);
    }

    /**
     * Create a Visitor as configured by "security.layout.collector.name"
     */
    public static com.top_logic.basic.col.Mapping getSecurityDomainMapper() {
		return ApplicationConfig.getInstance().getConfig(SecurityConfiguration.class).getLayout().getDomainMapper();
    }

    /**
     * Remove access to the Component using a CommandGroup for a given BoundRole.
     *
     * Keep in mind to visit this layout with a
     * {@link CompoundSecurityLayoutCommandGroupDistributor}
     * after invoking this method to restore a consistent security.
     *
     * @param aGroup    the BoundCommandGroup. If <code>null</code> remove any access for the role
     * @param aRole     the BoundedRole
     * @return true if something was actually removed.
     */
    public boolean removeAccess (BoundCommandGroup aGroup, BoundedRole aRole) {
		PersBoundComp persBoundComp = getPersBoundComp();
		if (persBoundComp == null) {
			return false;
		}
		return persBoundComp.removeAccess(aGroup, aRole);
    }

    /**
     * Remove all access on this component.
     *
     * Keep in mind to visit this layout with a
     * {@link CompoundSecurityLayoutCommandGroupDistributor}
     * after invoking this method to restore a consistent security.
     *
     * @return true when something was actually removed.
     */
    public boolean removeAllAccess() {
		PersBoundComp persBoundComp = getPersBoundComp();
		if (persBoundComp == null) {
			return false;
		}
		return persBoundComp.removeAllAccess();
    }

    /**
     * Add access to the Component using a CommandGroup for a given BoundRole.
     *
     * @param aGroup    the BoundCommandGroup
     * @param aRole     the BoundedRole
     */
    public void addAccess (BoundCommandGroup aGroup, BoundedRole aRole) {
        PersBoundComp persBoundComp = getPersBoundComp();
 		if (persBoundComp == null) {
			return;
 		}
		persBoundComp.addAccess(aGroup, aRole);
    }

    /**
     * The Roles for CommandGroups are configured using the PersBoundComp.
     */
    @Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
		PersBoundComp persBoundComp = getPersBoundComp();
		if (persBoundComp == null) {
			return Collections.emptySet();
		}
		return persBoundComp.rolesForCommandGroup(aCommand);
	}

    /**
	 * Return the current Security object to derive the security from.
	 *
	 * When useDefaultChecker is set the getDefaultObject from {@link BoundHelper} will be used.
	 *
	 * Otherwise first {@link LayoutComponent#getMaster()} is checked and when it is a
	 * BoundComponent its {@link BoundComponent#getCurrentObject(BoundCommandGroup aBCG, Object potentialModel)} is used.
	 *
	 * As last resort we use our next parent CompoundSecurityLayout.
	 *
	 * When all this fails we return null.
	 */
    @Override
	public BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
        if (useDefaultChecker) {
            return BoundHelper.getInstance().getDefaultObject();
        }

        LayoutComponent theMaster = this.getMaster();
        if (theMaster instanceof BoundComponent) {
			return ((BoundComponent) theMaster).getCurrentObject(aBCG, potentialModel);
        }

        CompoundSecurityLayout thePL = CompoundSecurityLayout.getNearestCompoundLayout(this);
        if (thePL != null) {
			return thePL.getCurrentObject(aBCG, potentialModel);
        }
        return null;
    }



    /**
     * Set the current BoundObject in the nearest CompoundSecurityLayout.
     *
     * @param anObject the BoundObject
     */
    public void setCurrentObject(BoundObject anObject){
        CompoundSecurityLayout thePL = CompoundSecurityLayout.getNearestCompoundLayout(this);
        if (thePL != null) {
            thePL.setCurrentObject(anObject);
        }
    }

    /**
     * If there is a model for this view, only show view if current user has default command group.
     *
     * @see com.top_logic.tool.boundsec.BoundLayout#hideReason(Object)
     */
    @Override
	public ResKey hideReason(Object potentialModel) {
		ResKey masterReason = hideReasonMaster();
		if (masterReason != null) {
			return masterReason;
        }
		return super.hideReason(potentialModel);

// TODO TSA: this code does not work properly with inner components using default security
//             Test the new behavior in top level!!!!
//             Changes became necessary in the context of COMS
//        if (theBO != null) {
//            TLContext         theContext = TLContext.getContext();
//            BoundCommandGroup theBC      = this.getDefaultCommandGroup();
//
//            if (!this.checkAccess(theContext, theBO, theBC)) {
//                return false;
//            }
//        }

    }


    @Override
	public boolean allow(BoundObject anObject) {

        if (!this.allowBySecurityMaster(anObject, this.useDefaultChecker)) {
            return (false);
        }

        if (anObject != null) {
            TLContext         theContext = TLContext.getContext();
            BoundCommandGroup theBC      = this.getDefaultCommandGroup();

            if (!this.checkAccess(theContext, anObject, theBC)) {
                return false;
            }
        }
        return super.allow(anObject);
    }

    /**
     * Check Access based on BoundObject .
     * TODO MGA introduce cache if necessary
     *
     * @param aContext  The context to check (i.e. to get the current Person)
     * @param aModel     The model of this component.
     * @param aCmdGroup The command group to check against.
     *
     * @return true when the intersection of roles for the commandGroups
     *              and roles of the current Person on the BoundObject
     *              is not empty.
     */
    protected boolean checkAccess(TLContext aContext, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		if (ThreadContext.isAdmin()) {
            return true;    // bypass bound security for all for SuperUsers
        }

		Person currentPerson = PersonManager.getManager().getCurrentPerson();
        if (currentPerson == null)
            return false;   // Don't know how to check this

        return AccessManager.getInstance().hasRole(currentPerson, aModel, getRolesForCommandGroup(aCmdGroup));
    }

    /**
     * Find the nearest {@link CompoundSecurityLayout} above the given component.
     *
     * @param aComponent  the component to start the search from
     * @return the next CompoundSecurityLayout in the parent hierarchy.
     *         <code>null</code> if no CompoundSecurityLayout is found
     *                        or if the input was <code>null</code>.
     */
    public static CompoundSecurityLayout getNearestCompoundLayout(LayoutComponent aComponent) {
        LayoutComponent theComponent = aComponent;
        while (theComponent != null) {
            theComponent = theComponent.getParent();

            if (theComponent instanceof CompoundSecurityLayout) {
                return (CompoundSecurityLayout) theComponent;
            }
        }
        if (aComponent.openedAsDialog()) {
            return getNearestCompoundLayout(aComponent.getDialogParent());
        }
        return null;
    }

    /**
     * Returns the next visible CompoundSecurityLayout below aComponent.
     *
     * The search is recursively, depth first.
     *
     * If aComponent is a CompoundSecurityLayout, the next inner CompoundSecurityLayout is returned.
     * This implies that aComponent must be LayoutContainer as only those can have children.
     *
     * @param aComponent the component to examine. If aComponent is not visible, null is returned.
     * @return a visible CompoundSecurityLayout which is a visible, recursive child of aComponent
     *         or null, if no such CompoundSecurityLayout exists.
     */
    public static CompoundSecurityLayout getNextVisibleChildCompoundLayout(LayoutComponent aComponent) {
        if (  aComponent == null || !aComponent.isVisible() ||
            !(aComponent instanceof LayoutContainer)) {
            return null;
        }

        LayoutContainer theLayout = (LayoutContainer)aComponent;
		for (LayoutComponent theChild : theLayout.getChildList()) {
            if(theChild.isVisible()){
                if(theChild instanceof CompoundSecurityLayout){
                    return (CompoundSecurityLayout) theChild;
                }
                CompoundSecurityLayout theResult = getNextVisibleChildCompoundLayout(theChild);
                if(theResult != null) {
                    return theResult;
                }
            }
        }
        return null;
    }

}
