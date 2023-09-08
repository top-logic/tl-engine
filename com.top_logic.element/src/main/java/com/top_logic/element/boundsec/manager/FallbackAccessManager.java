/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The FallbackAccessManager 
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 * 
 * @deprecated Must be replaced by the default security storage, because roles 
 *             are no longer be cached at all objects. See Ticket #733. 
 */
@Deprecated
public class FallbackAccessManager extends StorageAccessManager {

	/**
	 * {@link TypedConfiguration} interface for the {@link FallbackAccessManager}.
	 */
	public interface Config extends StorageAccessManager.Config {

		/** Property name of {@link #getFallback()}. */
		String FALLBACK = "fallback";

		/** Property name of {@link #getLogRoles()}. */
		String LOG_ROLES = "log-roles";

		/**
		 * @see FallbackAccessManager#fallback
		 */
		@Name(FALLBACK)
		@InstanceFormat
		AccessManager getFallback();

		/**
		 * @see FallbackAccessManager#doLog
		 */
		@Name(LOG_ROLES)
		boolean getLogRoles();

	}

    private static final Comparator           COMPARATOR = new WrapperComparator("name");

    /**
     * log found roles, use for debugging
     */
    private boolean       doLog;
    
    /**
     * internal mangager uses as fallback
     */
    private AccessManager fallback;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link FallbackAccessManager}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public FallbackAccessManager(InstantiationContext context, Config config) {
    	super(context, config);
		fallback = config.getFallback();
		doLog = config.getLogRoles();
    }

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    @Override
	public Set<BoundRole> getRoles(Person aPerson, BoundObject aBO) {
        Set theNewRoles = super.getRoles(aPerson, aBO); // ask the new rule based security
        
        if (this.doLog) { // just for debugging
            
            Set theOldRoles = this.fallback.getRoles(aPerson, aBO); // the old way, ask getLocalAndGlobalAndGroupRoles()

            Set theOld = new TreeSet(COMPARATOR);
            Set theNew = new TreeSet(COMPARATOR);
            if (theNewRoles != null) theNew.addAll(theNewRoles);
            if (theOldRoles != null) theOld.addAll(theOldRoles);
            

            if (true) {
                Set theSame = new TreeSet(COMPARATOR);
                theSame.addAll(theNew);
                theSame.retainAll(theOld);
    
                theOld.removeAll(theSame); 
                theNew.removeAll(theSame);
    
                boolean doLog = theSame.isEmpty() || ! theOld.isEmpty() || ! theNew.isEmpty();
    
                if (doLog) {
                    Logger.info("Roles for: " + aPerson.getName() + "/" + aBO.getID(), this.getClass());
                    Logger.info("Found old roles:   " + printRoles(theOld), this.getClass());
                    Logger.info("Found new roles: " + printRoles(theNew), this.getClass());
                }
            }
        }
        
        return theNewRoles;
    }
    
    @Override
	public boolean hasRole(Person aPerson, BoundObject aBO, Collection<BoundedRole> aSomeRoles) {
        if (doLog) {
            getRoles(aPerson, aBO); // just for debugging
        }
        if (this.fallback.hasRole(aPerson, aBO, aSomeRoles)) {
            return true;
        }
        return super.hasRole(aPerson, aBO, aSomeRoles);
    }
    
    private String printRoles(Collection someRoles) {
        StringBuffer theString = new StringBuffer(64);
        Iterator theIter = someRoles.iterator();
        while (theIter.hasNext()) {
            theString.append(((BoundedRole) theIter.next()).getName());
            theString.append(',');
        }
        return theString.toString();
    }
}

