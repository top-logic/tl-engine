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
import java.util.Objects;
import java.util.Set;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.RuleHistory;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.knowledge.security.SecurityStorage.StorageEntry;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.InteractionRevisionMemo;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.cs.TLObjectChangeSet;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * The {@link StorageAccessManager} stores roles in the security storage for fast access.
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

		/**
		 * Whether the roles of a person are computed once for all objects and cached for the
		 * person.
		 * 
		 * <p>
		 * If not set, the roles of a person are looked up in the security storage for each access
		 * check.
		 * </p>
		 */
		@Name(USE_PERSON_ROLE_CACHE)
		boolean getUsePersonRoleCache();

		/**
		 * The storage holding the precomputed roles of all users on all objects.
		 */
		@Name(STORAGE)
		@ImplementationClassDefault(SecurityStorage.class)
		@ItemDefault
		PolymorphicConfiguration<? extends SecurityStorage> getStorage();

		/**
		 * The component updating the security storage, when objects or role assignments change.
		 */
		@Name(UPDATE_MANAGER)
		@ImplementationClassDefault(ElementSecurityUpdateManager.class)
		@ItemDefault
		PolymorphicConfiguration<? extends ElementSecurityUpdateManager> getUpdateManager();

	}

	/**
	 * Key used to store {@link RoleComputation} in {@link TLContext}.
	 * 
	 * @see #getRoleComputation(Person)
	 */
	private static final Property<RoleComputation> PERSON_ROLE_CACHE =
		TypedAnnotatable.property(RoleComputation.class, "personRoleCache");

	/**
	 * Answers of {@link #internalHasRoleOnAnyObject(Person, Collection)} in the current
	 * interaction.
	 * <p>
	 * The groups of a person are part of the committed state the memo is valid for, so the answers
	 * are kept by person.
	 * </p>
	 */
	private static final InteractionRevisionMemo<Map<RolesOfPerson, Boolean>> ROLE_ON_ANY_OBJECT_MEMO =
		new InteractionRevisionMemo<>("roleOnAnyObject", kb -> new HashMap<>());

	/**
	 * For the identifier of an object, the groups by the identifier of the role they hold on each
	 * version of the object according to the storage.
	 *
	 * @see #storedRolesOfIdentity(TLID)
	 */
	private static final InteractionRevisionMemo<Map<TLID, Map<TLID, Set<Group>>>> VERSION_ROLES_MEMO =
		new InteractionRevisionMemo<>("storedRolesOfVersions", kb -> new HashMap<>());

    /** Saves the security storage instance. */
    private final SecurityStorage securityStorage;

    private final ElementSecurityUpdateManager securityUpdateManager;

    /**
     * Flag indicating that the {@link StorageAccessManager} has already been started (the
     * {@link #reload()} method has been called).
     */
    protected boolean started = false;

    /**
     * Saves the objects on an update security process, on which rule based roles are
     * invalid and must be recalculated. 
     */
	protected Collection<BoundObject> _invalidObjects;

    /**
     * Saves the objects on an update security process, on which rule based roles for a given role are
     * invalid and must be recalculated. 
     */
	protected Map<BoundRole, Set<BoundObject>> _invalidObjectsByRole = new HashMap<>();

	/**
	 * Saves the rules on an update security process which are invalid and must be recalculated.
	 */
	protected Set<RoleProvider> _invalidRules;

	/** @see Config#getRebuildStrategy() */
	private final RebuildStrategy _rebuildStrategy;

    /** Flag whether a potential security rebuilding will be done not until completed application startup. */
    protected final boolean rebuildAfterStartup;

    /** Flag indicating whether the storage should be rebuild on each application startup. */
    protected boolean resetOnInit;
    
    /**
     * Flag indicating whether to skip storage rebuild on application startup. This will
     * override the {@link #rebuildAfterStartup} and {@link #resetOnInit} flags.
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
	 * Adds objects whose roles were invalidated while an update process. Call this method at the
	 * begin of an update process, and call {@link #removeInvalidObjects} after the update process
	 * is finished.
	 *
	 * @param anInvalidObjectsList
	 *        The stored value for all roles for objects in the collection is invalid.
	 * @param someInvalidObjectsByRole
	 *        For each role the stored value for each object have become invalid.
	 * @param invalidRules
	 *        Each entry for elements in that match one of the rules is invalid.
	 * 
	 * @see #removeInvalidObjects()
	 */
	public void setInvalidObjects(Collection<BoundObject> anInvalidObjectsList,
			Map<BoundRole, Set<BoundObject>> someInvalidObjectsByRole, Set<RoleProvider> invalidRules) {
		if (_invalidObjects != null) {
			throw failUpdateInProgress(anInvalidObjectsList, someInvalidObjectsByRole, invalidRules);
		}
		_invalidObjects = Objects.requireNonNull(anInvalidObjectsList);
		_invalidObjectsByRole = Objects.requireNonNull(someInvalidObjectsByRole);
		_invalidRules = Objects.requireNonNull(invalidRules);
    }

	private IllegalStateException failUpdateInProgress(Collection<BoundObject> anInvalidObjectsList,
			Map<BoundRole, Set<BoundObject>> someInvalidObjectsByRole, Set<RoleProvider> invalidRules) {
		StringBuilder error = new StringBuilder();
		error.append("Concurrent update in StorageAccessManager ");
		error.append(this);
		error.append(". Current values: ");
		error.append("InvalidObjects: ").append(_invalidObjects);
		error.append("InvalidObjectsByRole: ").append(_invalidObjectsByRole);
		error.append("InvalidRules: ").append(_invalidRules);
		error.append(". New values: ");
		error.append("InvalidObjects: ").append(anInvalidObjectsList);
		error.append("InvalidObjectsByRole: ").append(someInvalidObjectsByRole);
		error.append("InvalidRules: ").append(invalidRules);
		throw new IllegalStateException(error.toString());
	}

    /**
	 * Removes objects whose roles were invalidated while an update process. Call this method at the
	 * end of an update process.
	 * 
	 * @see #setInvalidObjects(Collection, Map, Set)
	 */
    public void removeInvalidObjects() {
		_invalidObjects = null;
		_invalidObjectsByRole = null;
		_invalidRules = null;
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
			resultIdList.add(bo.getID());
			BoundHelper.collectAllSecurityParents(bo, parent -> resultIdList.add(parent.getID()));
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
	public Set<? extends BoundRole> getRoles(Person aPerson, BoundObject aBO) {
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
		if (aBO != null && aPerson != null && needsEvaluation(aBO, null)) {
			return evaluateRoles(aPerson, aBO);
		}
		return getRoleComputation(aPerson).getRoles(aBO);
    }

    @Override
	protected boolean internalHasRole(Person aPerson, BoundObject aBO, Collection<? extends BoundRole> accessRoles) {
        if (securityStorage.isRebuilding()) {
			switch (rebuildStrategy()) {
				case BLOCK:
					waitForRebuilding();
					break;
				case COMPUTE:
					return super.hasRole(aPerson, aBO, accessRoles);
				case DENY:
					return false;
				default:
					break;
			}
        }
		if (aBO != null && needsEvaluation(aBO, accessRoles)) {
			return evaluateHasRole(aPerson, aBO, accessRoles);
		}
		return getRoleComputation(aPerson).hasRole(aBO, accessRoles);
    }

	/**
	 * Whether the roles on the given object cannot be answered by a lookup of the identities of the
	 * object and its role parents in the storage.
	 *
	 * <p>
	 * This is the case, if the object or one of its role parents is a historic version, or if a rule
	 * that is not stored may grant a role there.
	 * </p>
	 *
	 * @param roles
	 *        The roles asked for, <code>null</code> for all roles.
	 */
	final boolean needsEvaluation(BoundObject object, Collection<? extends BoundRole> roles) {
		RuleAnalysis analysis = getRuleAnalysis();
		if (needsEvaluation(analysis, object, roles)) {
			return true;
		}
		if (analysis.isStorageSufficientForCurrentObjects()) {
			return false;
		}
		boolean[] result = { false };
		BoundHelper.visitAllSecurityParents(object, parent -> {
			if (needsEvaluation(analysis, parent, roles)) {
				result[0] = true;
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		});
		return result[0];
	}

	private static boolean needsEvaluation(RuleAnalysis analysis, BoundObject object,
			Collection<? extends BoundRole> roles) {
		if (!(object instanceof TLObject item) || !item.tValid()) {
			// Nothing can be evaluated on a deleted object.
			return false;
		}
		if (RuleHistory.isHistoric(item)) {
			return true;
		}
		if (item.tType() instanceof TLClass type) {
			return roles == null ? analysis.hasNotStoredRule(type) : analysis.hasNotStoredRule(type, roles);
		}
		return false;
	}

	private boolean evaluateHasRole(Person person, BoundObject object, Collection<? extends BoundRole> roles) {
		Collection<Group> groups = getGroups(person);
		for (BoundRole role : roles) {
			if (CollectionUtil.containsAny(groups, getEffectiveGroups(object, role))) {
				return true;
			}
		}
		return false;
	}

	private Set<BoundRole> evaluateRoles(Person person, BoundObject object) {
		Collection<Group> groups = getGroups(person);
		Set<BoundRole> result = new HashSet<>();
		for (BoundedRole role : BoundedRole.getAll()) {
			if (CollectionUtil.containsAny(groups, getEffectiveGroups(object, role))) {
				result.add(role);
			}
		}
		return result;
	}

	/**
	 * Answers from the {@link SecurityStorage}, which holds an entry for each object a group has a
	 * role on, inherited roles included through the entry on the role parent.
	 * <p>
	 * The answer is <code>true</code> while the storage is rebuilding or cannot be read. Answers are
	 * kept in an {@link InteractionRevisionMemo}.
	 * </p>
	 */
	@Override
	protected boolean internalHasRoleOnAnyObject(Person person, Collection<? extends BoundRole> roles) {
		if (securityStorage.isRebuilding()) {
			return true;
		}
		RuleAnalysis analysis = getRuleAnalysis();
		for (BoundRole role : roles) {
			if (analysis.isBeyondStorage(role)) {
				// The role may be held on a historic version of an object or through a rule that is
				// not stored, which the storage does not know.
				return true;
			}
		}
		Set<TLID> roleIDs = roleIDs(roles);
		if (roleIDs.isEmpty()) {
			return false;
		}
		Map<RolesOfPerson, Boolean> memo = ROLE_ON_ANY_OBJECT_MEMO.current(PersistencyLayer.getKnowledgeBase());
		if (memo == null) {
			return lookupRoleOnAnyObject(person, roleIDs);
		}
		RolesOfPerson key = new RolesOfPerson(person.tId(), roleIDs);
		Boolean result = memo.get(key);
		if (result == null) {
			result = Boolean.valueOf(lookupRoleOnAnyObject(person, roleIDs));
			if (!securityStorage.isRebuilding()) {
				memo.put(key, result);
			}
		}
		return result.booleanValue();
	}

	private boolean lookupRoleOnAnyObject(Person person, Collection<TLID> roleIDs) {
		try {
			return securityStorage.hasRoleOnAnyObject(getGroupIDs(person), roleIDs);
		} catch (StorageException ex) {
			Logger.error("Unable to determine whether person '" + person.getName()
				+ "' has a role on any object, assuming it has.", ex, StorageAccessManager.class);
			return true;
		}
	}

	private static Set<TLID> roleIDs(Collection<? extends BoundRole> roles) {
		Set<TLID> result = new HashSet<>();
		for (BoundRole role : roles) {
			if (role instanceof BoundedRole withId) {
				result.add(withId.tIdLocal());
			}
		}
		return result;
	}

    @Override
	protected <T extends BoundObject> Collection<T> internalAllowedBusinessObjects(Person user,
			Collection<? extends BoundRole> someRoles, Collection<T> objects) {
        if (securityStorage.isRebuilding()) {
			switch (rebuildStrategy()) {
				case BLOCK:
					waitForRebuilding();
					break;
				case COMPUTE:
					return super.getAllowedBusinessObjects(user, someRoles, objects);
				case DENY:
					return new ArrayList<>(0);
				default:
					break;
			}
        }
		PreloadContext preload = new PreloadContext();
		try {
			return filterAllowed(preload, user, someRoles, objects);
		} finally {
			preload.close();
		}
	}

	private <T extends BoundObject> Collection<T> filterAllowed(PreloadContext preload, Person user,
			Collection<? extends BoundRole> someRoles, Collection<T> objects) {
		if (objects.size() > 1 && !getRuleAnalysis().isStorageSufficientForCurrentObjects()) {
			// The role parents of each object are visited to find out, whether the storage answers.
			preloadRuleNavigation(preload, objects, true);
		}
		List<T> simple = new ArrayList<>(objects.size());
		List<T> toEvaluate = new ArrayList<>();
		for (T object : objects) {
			if (needsEvaluation(object, someRoles)) {
				toEvaluate.add(object);
			} else {
				simple.add(object);
			}
		}
		prefetchVersions(preload, toEvaluate);
		List<T> evaluated = new ArrayList<>();
		for (T object : toEvaluate) {
			if (evaluateHasRole(user, object, someRoles)) {
				evaluated.add(object);
			}
		}
		if (evaluated.isEmpty() && simple.size() == objects.size()) {
			return getRoleComputation(user).getAllowedBusinessObjects(someRoles, objects);
		}
		if (simple.isEmpty()) {
			return evaluated;
		}
		Set<T> allowed = new HashSet<>(getRoleComputation(user).getAllowedBusinessObjects(someRoles, simple));
		allowed.addAll(evaluated);
		List<T> result = new ArrayList<>(allowed.size());
		for (T object : objects) {
			if (allowed.contains(object)) {
				result.add(object);
			}
		}
		return result;
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
	public void handleSecurityUpdate(TLObjectChangeSet change, CommitHandler aHandler) {
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
        super.handleSecurityUpdate(change, aHandler);
		doHandleSecurityUpdate(change, aHandler);
    }

    /**
     * Hook for subclasses to update the access manager in case of a security change.
     */
	protected void doHandleSecurityUpdate(TLObjectChangeSet change, CommitHandler aHandler) {
		securityUpdateManager.handleSecurityUpdate(change, aHandler);
    }


	@Override
	protected boolean useRoleMemo() {
		return super.useRoleMemo() && !securityStorage.isRebuilding();
	}

	@Override
	protected Set<Group> computeGroups(Wrapper wrapper, BoundRole role) {
		BoundObject object = (BoundObject) wrapper;
		if (!wrapper.tValid()) {
			// A deleted object: no rule applies, the storage is asked as for every current object.
			try {
				return new HashSet<>(securityStorage.getGroups(object, role));
			} catch (StorageException ex) {
				Logger.error("Unable to get groups from security storage.", ex, this);
				return new HashSet<>();
			}
		}
		boolean historic = RuleHistory.isHistoric(wrapper);
		Wrapper current = historic ? WrapperHistoryUtils.getCurrent(wrapper) : wrapper;
		if (securityStorage.isRebuilding() || !securityStorage.isAutoUpdate()
			|| (current != null && isInvalid((BoundObject) current, role))) {
			// Do not use rebuild strategy here!
			return super.computeGroups(wrapper, role);
		}
		RuleAnalysis analysis = getRuleAnalysis();
		try {
			if (!historic) {
				Set<Group> result = new HashSet<>(securityStorage.getGroups(object, role));
				if (wrapper.tType() instanceof TLClass type && analysis.hasNotStoredRule(type)) {
					addRuleGroups(result, wrapper, role, rule -> !analysis.isStored(rule));
				}
				return result;
			}
			// A historic version: The storage holds the roles of the current object. Of these, only
			// the entries of direct assignments and of rules evaluated on the current data hold for
			// the version, too.
			Set<Group> result = new HashSet<>();
			if (current != null) {
				addStoredGroupsOfIdentity(result, (BoundObject) current, role);
			}
			addRuleGroups(result, wrapper, role, rule -> !isStoredForEachVersion(rule));
			return result;
		} catch (StorageException ex) {
			Logger.error("Unable to get groups from security storage. Executing manually...", ex, this);
			return super.computeGroups(wrapper, role);
		}
	}

	/**
	 * Whether the entries the given rule produces in the storage for a current object hold for each
	 * version of the object.
	 */
	private boolean isStoredForEachVersion(RoleRule rule) {
		return rule.getHistory().getMode() == HistoryMode.CURRENT && getRuleAnalysis().isStored(rule);
	}

	private void addStoredGroupsOfIdentity(Set<Group> result, BoundObject current, BoundRole role)
			throws StorageException {
		if (!(role instanceof BoundedRole storedRole)) {
			return;
		}
		Set<Group> groups = storedRolesOfIdentity(current.getID()).get(storedRole.tIdLocal());
		if (groups != null) {
			result.addAll(groups);
		}
	}

	/**
	 * The groups by the identifier of the role they hold on each version of the object with the
	 * given identifier according to the storage: through a direct assignment, or through a rule
	 * that is evaluated on the current data.
	 *
	 * <p>
	 * All roles of an object are requested at once and kept for the current interaction.
	 * </p>
	 */
	private Map<TLID, Set<Group>> storedRolesOfIdentity(TLID id) throws StorageException {
		Map<TLID, Map<TLID, Set<Group>>> memo = versionRolesMemo();
		if (memo == null) {
			return loadStoredRoles(Collections.singletonList(id)).get(id);
		}
		Map<TLID, Set<Group>> roles = memo.get(id);
		if (roles == null) {
			roles = loadStoredRoles(Collections.singletonList(id)).get(id);
			memo.put(id, roles);
		}
		return roles;
	}

	private Map<TLID, Map<TLID, Set<Group>>> versionRolesMemo() {
		return useRoleMemo() ? VERSION_ROLES_MEMO.current(PersistencyLayer.getKnowledgeBase()) : null;
	}

	/**
	 * Requests {@link #storedRolesOfIdentity(TLID)} for all given identifiers at once.
	 *
	 * @return An entry for each of the given identifiers.
	 */
	private Map<TLID, Map<TLID, Set<Group>>> loadStoredRoles(Collection<TLID> ids) throws StorageException {
		Map<TLID, Map<TLID, Set<Group>>> result = new HashMap<>();
		for (TLID id : ids) {
			result.put(id, new HashMap<>());
		}
		for (StorageEntry entry : securityStorage.getEntries(null, ids, null)) {
			if (!entry.isDirectAssignment()) {
				RoleProvider reason = getRulefromPersitancyId(Integer.valueOf(entry.reason()));
				if (!(reason instanceof RoleRule rule) || !isStoredForEachVersion(rule)) {
					continue;
				}
			}
			Group group = Group.getInstance(entry.group());
			Map<TLID, Set<Group>> roles = result.get(entry.businessObject());
			if (group != null && roles != null) {
				roles.computeIfAbsent(entry.role(), role -> new HashSet<>()).add(group);
			}
		}
		return result;
	}

	/**
	 * Loads what is needed to check the historic versions among the given objects, before the
	 * objects are checked one by one: the values the rules navigate first, and the stored roles.
	 * The same is done for the historic versions among their role parents.
	 *
	 * <p>
	 * The number of requests to the database then depends on the depth of the chains of role
	 * parents, not on the number of objects.
	 * </p>
	 */
	private void prefetchVersions(PreloadContext preload, Collection<? extends BoundObject> objects) {
		if (objects.size() < 2) {
			return;
		}
		Map<TLID, Map<TLID, Set<Group>>> memo = securityStorage.isAutoUpdate() ? versionRolesMemo() : null;
		Set<BoundObject> seen = new HashSet<>();
		Collection<? extends BoundObject> level = objects;
		while (!level.isEmpty()) {
			List<BoundObject> versions = new ArrayList<>();
			for (BoundObject object : level) {
				if (seen.add(object) && object.tValid() && RuleHistory.isHistoric(object)) {
					versions.add(object);
				}
			}
			if (versions.isEmpty()) {
				break;
			}
			preloadRuleNavigation(preload, versions, false);
			if (memo != null) {
				Set<TLID> ids = new HashSet<>();
				for (BoundObject version : versions) {
					if (!memo.containsKey(version.getID())) {
						ids.add(version.getID());
					}
				}
				try {
					if (!ids.isEmpty()) {
						memo.putAll(loadStoredRoles(ids));
					}
				} catch (StorageException ex) {
					// The roles are requested again when the objects are checked.
					Logger.warn("Unable to get roles of historic versions from security storage.", ex, this);
					memo = null;
				}
			}
			List<BoundObject> parents = new ArrayList<>();
			for (BoundObject version : versions) {
				parents.addAll(version.getSecurityParents());
			}
			level = parents;
		}
	}

    private boolean isInvalid(BoundObject aBO, BoundRole aRole) {
		if (_invalidObjects == null) {
			// Check whether the update process has started.
			return false;
		}
		if (_invalidObjects.contains(aBO)) {
			return true;
		}
		if (_invalidObjectsByRole.getOrDefault(aRole, Collections.emptySet()).contains(aBO)) {
			return true;
		}
		if (!_invalidRules.isEmpty()) {
			if (_invalidRules.stream().anyMatch(rule -> rule.matches(aBO))) {
				return true;
			}
		}
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
				// The cache is stored per interaction under a person-independent property. If the
				// current person of the interaction has changed (e.g. the person was switched on the
				// same context), a cache built for the previous person must not be reused, otherwise
				// it would report that person's roles.
				if (roleComputation == null || !Utils.equals(roleComputation.getPerson(), currentPerson)) {
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

	/**
	 * Key of an answer of {@link StorageAccessManager#internalHasRoleOnAnyObject(Person, Collection)}.
	 *
	 * @param person
	 *        The identifier of the person asked for.
	 * @param roleIDs
	 *        The identifiers of the roles asked for.
	 */
	private record RolesOfPerson(ObjectKey person, Set<TLID> roleIDs) {
		// Pure value.
	}

}
