/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * The StorageAccessManager stores roles in the security storage for fast access.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
@ServiceDependencies({
		ConnectionPoolRegistry.Module.class, // needed by SecurityStorage
		ClusterManager.Module.class// needed by SecurityStorage
})
public class StorageAccessManager extends ElementAccessManager {

	/**
	 * {@link TypedConfiguration} interface for the {@link StorageAccessManager}.
	 */
	public interface Config extends ElementAccessManager.Config {

		/** Property name of {@link #getRebuildStrategy()}. */
		String REBUILD_STRATEGY = "rebuild-strategy";

		/** Property name of {@link #getResetOnInit()}. */
		String RESET_ON_INIT = "reset-on-init";

		/** Property name of {@link #getRebuildAfterStartup()}. */
		String REBUILD_AFTER_STARTUP = "rebuild-after-startup";

		/** Property name of {@link #getSkipRebuildOnStartup()}. */
		String SKIP_REBUILD_ON_STARTUP = "skip-rebuild-on-startup";

		/** Property name of {@link #getUsePersonRoleCache()}. */
		String USE_PERSON_ROLE_CACHE = "use-person-role-cache";

		/** Property name of {@link #getStorage()}. */
		String STORAGE = "storage";

		/** Property name of {@link #getUpdateManager()}. */
		String UPDATE_MANAGER = "update-manager";

		/** The rebuild strategy to use. */
		@Name(REBUILD_STRATEGY)
		RebuildStrategy getRebuildStrategy();

		/**
		 * @see StorageAccessManager#resetOnInit
		 */
		@Name(RESET_ON_INIT)
		boolean getResetOnInit();

		/**
		 * @see StorageAccessManager#rebuildAfterStartup
		 */
		@Name(REBUILD_AFTER_STARTUP)
		boolean getRebuildAfterStartup();

		/**
		 * @see StorageAccessManager#skipRebuildOnStartup
		 */
		@Name(SKIP_REBUILD_ON_STARTUP)
		boolean getSkipRebuildOnStartup();

		@Name(USE_PERSON_ROLE_CACHE)
		boolean getUsePersonRoleCache();

		@Name(STORAGE)
		@ImplementationClassDefault(SecurityStorage.class)
		@ItemDefault
		PolymorphicConfiguration<SecurityStorage> getStorage();

		@Name(UPDATE_MANAGER)
		@ImplementationClassDefault(ElementSecurityUpdateManager.class)
		@ItemDefault
		PolymorphicConfiguration<ElementSecurityUpdateManager> getUpdateManager();

	}

	/**
	 * Key used to store {@link RoleComputation} in {@link TLContext}.
	 * 
	 * @see #getRoleComputation(Person)
	 */
	private static final Property<RoleComputation> PERSON_ROLE_CACHE =
		TypedAnnotatable.property(RoleComputation.class, "personRoleCache");

    /** Saves the security storage instance. */
    private final SecurityStorage securityStorage;

    private final ElementSecurityUpdateManager securityUpdateManager;

    /**
     * Flag indicating that the StorageAccessManager has already been started (the
     * {@link #reload()} method has been called).
     */
    protected boolean started = false;

    /**
     * Saves the objects on an update security process, on which rule based roles are
     * invalid and must be recalculated. 
     */
    protected Collection<BoundObject> invalidObjects;

    /**
     * Saves the objects on an update security process, on which rule based roles for a given role are
     * invalid and must be recalculated. 
     */
    protected Map<BoundRole, Set<BoundObject>> invalidObjectsByRole = new HashMap<>();

	/** @see Config#getRebuildStrategy() */
	private final RebuildStrategy _rebuildStrategy;

    /** Flag whether a potential security rebuilding will be done not until completed application startup. */
    protected final boolean rebuildAfterStartup;

    /** Flag indicating whether the storage should be rebuild on each application startup. */
    protected boolean resetOnInit;
    
    /**
     * Flag indicating whether to skip storage rebuild on application startup. This will
     * override the rebuildAfterStartup and resetOnInit flags.
     */
    protected boolean skipRebuildOnStartup;

	private final boolean usePersonRoleCache;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link StorageAccessManager}.
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
	public StorageAccessManager(InstantiationContext context, Config config) {
		super(context, config);
		_rebuildStrategy = config.getRebuildStrategy();
		resetOnInit = config.getResetOnInit();
		rebuildAfterStartup = config.getRebuildAfterStartup();
		skipRebuildOnStartup = config.getSkipRebuildOnStartup();
		usePersonRoleCache = config.getUsePersonRoleCache();
		securityStorage = createSecurityStorage(context, config);
		securityUpdateManager = createUpdateManager(context, config);
    }

	private SecurityStorage createSecurityStorage(InstantiationContext context, Config config) {
		SecurityStorage result = context.getInstance(config.getStorage());
		if (result == null) {
			context.error("SecurityStorage is not allowed to be null.");
		}
		return result;
	}

	private ElementSecurityUpdateManager createUpdateManager(InstantiationContext context, Config config) {
		ElementSecurityUpdateManager result = context.getInstance(config.getUpdateManager());
		if (result == null) {
			context.error("SecurityUpdateManager is not allowed to be null.");
		}
		return result;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    /**
	 * Returns the internal object which is used to update security.
	 */
    public final SecurityStorage getSecurityStorage() {
    	return securityStorage;
    }

    /**
     * Adds objects whose roles were invalidated while an update process. Call this method
     * at the begin of an update process, and call {@link #removeInvalidObjects}
     * with the same collection after the update process is finished.
     *
     * @param anInvalidObjectsList
     *        a collection of wrapper whose roles have become invalid
     */
    public void setInvalidObjects(Collection<BoundObject> anInvalidObjectsList, Map<BoundRole, Set<BoundObject>> someInvalidObjectsByRole) {
        invalidObjects       = anInvalidObjectsList;
        invalidObjectsByRole = someInvalidObjectsByRole;
    }

    /**
     * Removes objects whose roles were invalidated while an update process. Call this
     * method at the end of an update process, and call
     * {@link #setInvalidObjects(Collection, Map)} with the same collection at the beginning of
     * an update process.
     */
    public void removeInvalidObjects() {
        invalidObjects = null;
        invalidObjectsByRole = null;
        
    }



    /**
	 * Gets the security parents of an object.
	 *
	 * @param bo
	 *        The object to get the security parents from.
	 * @return List with the IDs of the security parents of the given object
	 */
	protected final List<TLID> getSecurityParentIDs(BoundObject bo) {
		return getSecurityParentIDs(Collections.singletonList(bo));
    }

    /**
	 * Gets the security parents of the given objects object.
	 *
	 * @param businessObjects
	 *        The objects to get the security parents from.
	 * 
	 * @return List with the IDs of the security parents of the given objects.
	 */
	protected List<TLID> getSecurityParentIDs(Collection<? extends BoundObject> businessObjects) {
		List<TLID> resultIdList = new ArrayList<>();
		for (BoundObject bo : businessObjects) {
			while (bo != null) {
				resultIdList.add(bo.getID());
				bo = bo.getSecurityParent();
    		}
    	}
		return resultIdList;
    }

    /**
	 * Gets the groups the given person is member of.
	 *
	 * @param aPerson
	 *        the person to get the groups from
	 * @return {@link List} with the {@link TLID}s of the groups the given person is member of.
	 */
    @SuppressWarnings("unchecked")
	protected List<TLID> getGroupIDs(Person aPerson) {
		@SuppressWarnings("rawtypes")
		List groupIds = getGroups(aPerson);
		WrapperFactory.toObjectNamesInline(groupIds);
		return groupIds;
    }



    @Override
    public Set<BoundRole> getRoles(Person aPerson, BoundObject aBO) {
        if (securityStorage.isRebuilding()) {
			switch (rebuildStrategy()) {
				case BLOCK:
					waitForRebuilding();
					break;
				case COMPUTE:
					return super.getRoles(aPerson, aBO);
				case DENY:
					return new HashSet<>();
				default:
					break;
			}
        }
		return getRoleComputation(aPerson).getRoles(aBO);
    }

    @Override
	public boolean hasRole(Person aPerson, BoundObject aBO, Collection<BoundedRole> someRoles) {
        if (isSuperUser(aPerson)) return true;
        if (securityStorage.isRebuilding()) {
			switch (rebuildStrategy()) {
				case BLOCK:
					waitForRebuilding();
					break;
				case COMPUTE:
					return super.hasRole(aPerson, aBO, someRoles);
				case DENY:
					return false;
				default:
					break;
			}
        }
		return getRoleComputation(aPerson).hasRole(aBO, someRoles);
    }

    @Override
	public <T extends BoundObject> Collection<T> getAllowedBusinessObjects(Person aPerson,
			Collection<BoundedRole> someRoles, Collection<T> someObjects) {
		if (isSuperUser(aPerson)) {
			return someObjects;
		}
		if (CollectionUtil.isEmptyOrNull(someRoles)) {
			return Collections.emptyList();
		}
        if (securityStorage.isRebuilding()) {
			switch (rebuildStrategy()) {
				case BLOCK:
					waitForRebuilding();
					break;
				case COMPUTE:
					return super.getAllowedBusinessObjects(aPerson, someRoles, someObjects);
				case DENY:
					return new ArrayList<>(0);
				default:
					break;
			}
        }
		return getRoleComputation(aPerson).getAllowedBusinessObjects(someRoles, someObjects);
    }

	/**
	 * The rebuild strategy which must be used, when the {@link #securityStorage} is currently
	 * rebuilding.
	 *
	 * @see SecurityStorage#isRebuilding()
	 */
	protected RebuildStrategy rebuildStrategy() {
		switch (_rebuildStrategy) {
			case BLOCK:
				if (SecurityStorage.isCurrentReloadingThread()) {
					/* Do not block when the current thread is the rebuilding thread to avoid
					 * dead-lock. */
					Logger.warn(
						"Can not block, because caller is the rebuilding thread. Blocking would cause dead-lock.",
						StorageAccessManager.class);
					return RebuildStrategy.COMPUTE;
				} else {
					return RebuildStrategy.BLOCK;
				}
			default:
				return _rebuildStrategy;
		}
	}

	@Override
	protected void startUp() {
		super.startUp();
		securityStorage.startUp(this);
		securityUpdateManager.startUp(this, securityStorage);
	}

	@Override
	protected void shutDown() {
		securityUpdateManager.shutDown();
		securityStorage.shutDown();
		super.shutDown();
	}

    @Override
    public boolean reload() {
        boolean theResult = super.reload();
		theResult &= securityStorage.reload();
		dirty = false;
		return theResult;
	}

	@Override
	public void initSecurityAfterStartup() {
		if (resetOnInit || dirty || securityStorage.isDirty()) {
			if (skipRebuildOnStartup) {
				Logger.warn("Skipping rebuilding of SecurityStorage as configured. Security could be inconsistent.",
					StorageAccessManager.class);
				return;
			}

			if (rebuildAfterStartup) {
				RebuildStorageAfterStartupThread.rebuildAfterStartup();
			} else {
				securityStorage.reload();
			}
			dirty = false;
		}
	}

    /**
     * Waits until rebuilding of security storage is complete.
     */
    private void waitForRebuilding() {
        while (securityStorage.isRebuilding()) {
            try {
            	Logger.info("Waiting for rebuilding security storage.", StorageAccessManager.class);
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                // ignore
            }
        }
    }


    @Override
	public void handleSecurityUpdate(KnowledgeBase kb, Map<TLID, Object> someChanged,
			Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler) {
        // Don't do anything if SecurityStorage is disabled
        if (!securityStorage.isAutoUpdate()) {
            return;
        }
        if (securityStorage.isRebuilding()) {
			if (_rebuildStrategy == RebuildStrategy.BLOCK && !ThreadContext.isAdmin()) {
                waitForRebuilding();
            }
            else {
                Logger.warn("A data change is about to be commited while security storage is rebuilding. Security could be inconsistent.", StorageAccessManager.class);
            }
        }
        super.handleSecurityUpdate(kb, someChanged, someNew, someRemoved, aHandler);
        doHandleSecurityUpdate(kb, someChanged, someNew, someRemoved, aHandler);
    }

    /**
     * Hook for subclasses to update the access manager in case of a security change.
     */
    protected void doHandleSecurityUpdate(KnowledgeBase kb, Map<TLID, Object> someChanged, Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler) {
		securityUpdateManager.handleSecurityUpdate(kb, someChanged, someNew, someRemoved, aHandler);
    }


    @Override
    public Collection<Group> getGroups(BoundObject aBO, BoundRole aRole) {
		if (securityStorage.isRebuilding() || !securityStorage.isAutoUpdate() || isInvalid(aBO, aRole)) {
            // Do not use rebuild strategy here!
            return super.getGroups(aBO, aRole);
        }
        try {
            return securityStorage.getGroups(aBO, aRole);
        }
        catch (StorageException ex) {
            Logger.error("Unable to get groups from security storage. Executing manually...", ex, this);
            return super.getGroups(aBO, aRole);
        }
    }
    
    private boolean isInvalid(BoundObject aBO, BoundRole aRole) {
    	if (invalidObjects != null && invalidObjects.contains(aBO)) return true;
    	if (invalidObjectsByRole == null) return false;
    	Set<BoundObject> theInvalidForRole = invalidObjectsByRole.get(aRole);
    	if (theInvalidForRole != null && theInvalidForRole.contains(aBO)) return true;
    	return false;
    }

	/**
	 * Return the person specific role computation (which is session local).
	 * 
	 * If the given person is not the user currently using this session, the method will return a
	 * new computation, which is not stored in the {@link TLContext}.
	 * 
	 * @param person
	 *        Person to get the computation for, must not be <code>null</code>.
	 * 
	 * @return The requested computation, never <code>null</code>.
	 */
	private RoleComputation getRoleComputation(Person person) {
		TLContext tlContext = TLContext.getContext();

		if (tlContext != null) {
			Person currentPerson = tlContext.getCurrentPersonWrapper();

			if (Utils.equals(currentPerson, person)) {
				RoleComputation roleComputation = tlContext.get(PERSON_ROLE_CACHE);
				if (roleComputation == null) {
					roleComputation = this.createRoleComputation(currentPerson, true);
					tlContext.set(PERSON_ROLE_CACHE, roleComputation);
				}

				return roleComputation;
			}
		}

		return this.createRoleComputation(person, false);
	}

	/**
	 * Creates a new {@link RoleComputation} for the given person.
	 * 
	 * @param person
	 *        The person to create the computation for, must not be <code>null</code>.
	 * @param forCache
	 *        whether the returned computation is cached and reused.
	 * 
	 * @return The new computation, never <code>null</code>.
	 * 
	 * @see #getRoleComputation(Person)
	 */
	protected RoleComputation createRoleComputation(Person person, boolean forCache) {
		if (forCache && usePersonRoleCache) {
			return new PersonRoleCache(person, this);
		} else {
			return new SimpleRoleComputation(person, this);
		}
	}

}
