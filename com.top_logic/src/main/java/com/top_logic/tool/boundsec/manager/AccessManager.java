/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;

/**
 * The access manager is responsible to determine the roles for persons on objects.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@ServiceDependencies({
	ThreadContextManager.Module.class
})
@ServiceExtensionPoint(PersistencyLayer.Module.class)
public class AccessManager extends ConfiguredManagedClass<AccessManager.Config> implements Reloadable {

	/**
	 * {@link TypedConfiguration} interface for the {@link AccessManager}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AccessManager> {

		/** Property name of {@link #getStructures()}. */
		String STRUCTURES = "structures";

		@Name(STRUCTURES)
		@ListBinding(attribute = "name")
		List<String> getStructures();

	}

	private final Collection<String> _structureNames;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link AccessManager}.
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
	public AccessManager(InstantiationContext context, Config config) {
		super(context, config);
		_structureNames = config.getStructures();
        ReloadableManager.getInstance().addReloadable(this);
    }

    public static AccessManager getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

    /**
     * Get all roles the person has on the given object.
     * This implementation is based on the basic hasRoles association.
     *
     * @param aPerson
     *        the person
     * @param aBO
     *        a bound object
     * @return Set of BoundRoles, never <code>null</code>; empty in case aPerson or aBO is
     *         <code>null</code>
     */
    public Set<BoundRole> getRoles(Person aPerson, BoundObject aBO) {
        if (aBO == null || aPerson == null) {
            return new HashSet<>();
        }
        Collection<BoundRole> theResult = aBO.getLocalAndGlobalAndGroupRoles(aPerson);
        if (CollectionUtil.isEmptyOrNull(theResult)) {
            theResult = new HashSet<>();
        } else if (!(theResult instanceof Set<?>)) {
            theResult = new HashSet<>(theResult);
        }
        return (Set<BoundRole>)theResult;
    }

    /**
     * Check if the given person has one of the given roles at the given bound object.
     *
     * @param aPerson
     *            the person to check
     * @param aBO
     *            the bound object to check
     * @param someRoles
     *            a collection of BoundRoles
     * @return <code>true</code>, if the given person has one of the given roles at the
     *         given bound object, <code>false</code> otherwise
     */
    public boolean hasRole(Person aPerson, BoundObject aBO, Collection<BoundedRole> someRoles) {
        if (isSuperUser(aPerson)) return true;
        Collection theRoles = this.getRoles(aPerson, aBO);
        return CollectionUtil.containsAny(theRoles, someRoles);
    }

    /**
     * Check if the current person has one of the given roles at the given bound object.
     *
     * @param aBO
     *            the bound object to check
     * @param someRoles
     *            a collection of BoundRoles
     * @return <code>true</code>, if the given person has one of the given roles at the
     *         given bound object, <code>false</code> otherwise
     */
    public boolean hasRole(BoundObject aBO, Collection someRoles) {
        TLContext theContext = TLContext.getContext();
        return hasRole(theContext == null ? null : theContext.getCurrentPersonWrapper(), aBO, someRoles);
    }

    /**
     * Checks the given collection of BoundObjects and returns only these objects, on which
     * the given person has on of the given roles.
     *
     * @param aPerson
     *        the person to check
     * @param someRoles
     *        a collection of BoundRoles
     * @param someObjects
     *        the collection of BoundObjects to check
     * @return a collection containing only the allowed BoundObjects of the given collection
     *         someObjects; may be empty but not <code>null</code>
     */
	public <T extends BoundObject> Collection<T> getAllowedBusinessObjects(Person aPerson,
			Collection<BoundedRole> someRoles, Collection<T> someObjects) {
        if (isSuperUser(aPerson)) return someObjects;
		if (CollectionUtil.isEmptyOrNull(someRoles)) {
			return Collections.emptyList();
		}
		List<T> theResult = new ArrayList<>(someObjects.size());
		Iterator<T> it = someObjects.iterator();
        while (it.hasNext()) {
			T theBO = it.next();
			if (hasRole(aPerson, theBO, someRoles)) {
                theResult.add(theBO);
            }
        }
        return theResult;
    }

    /**
     * Checks whether the given Person is super user and should be allowed always.
     *
     * @param aPerson
     *        the person to check
     * @return <code>true</code>, if the given person is a super user, <code>false</code>
     *         otherwise
     */
    protected boolean isSuperUser(Person aPerson) {
		return aPerson != null && Person.isAdmin(aPerson);
    }

	public Collection<String> getStructureNames() {
		return _structureNames;
	}

	/**
	 * Initializes the security after application has been started.
	 */
	public void initSecurityAfterStartup() {
		// nothing to initialize
	}

    @Override
	public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
	public String getDescription() {
        return "Manages security access in the application";
    }

    @Override
	public boolean usesXMLProperties() {
        return true;
    }

    /**
     * This is for sub classes only.
     *
     * @see com.top_logic.basic.Reloadable#reload()
     */
    @Override
	public boolean reload() {
        return true;
    }


    // Subclass hooks

    /**
     * Hook for subclasses to update the access manager in case of a security change.
     */
	public void handleSecurityUpdate(KnowledgeBase kb, Map<TLID, Object> someChanged,
			Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler) {
        // Nothing to do here - hook for subclasses only
    }
    
	public static final class Module extends TypedRuntimeModule<AccessManager> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<AccessManager> getImplementation() {
			return AccessManager.class;
		}
	}

}
