/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The SecurityStorage stores the security permissions respecting rules.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class ElementSecurityStorage extends SecurityStorage {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ElementSecurityStorage}.
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
	public ElementSecurityStorage(InstantiationContext context, Config config) {
		super(context, config);
    }

	@Override
	public void startUp(AccessManager accessManager) {
		if (!(accessManager instanceof ElementAccessManager)) {
			throw new IllegalArgumentException(
				"Cannot get instance of ElementAccessManager. Configured AccessManager must be instanceof ElementAccessManager.");
		}
		super.startUp(accessManager);
	}

	@Override
	protected ElementAccessManager getAccessManager() {
		return (ElementAccessManager) super.getAccessManager();
	}

    /**
	 * Updates the database entries based on rules by reevaluating affected rules for affected
	 * business objects.
	 *
	 * This is a optimized version. It only commits changes to the security storage, i.e. removes
	 * only entries which will not be added again and adds only entries that are not already in the
	 * storage.
	 *
	 * @param aChangesInformation
	 *        A {@link Map} of {@link RoleProvider} -> {@link Collection} of {@link Wrapper}
	 *        containing all rules and business objects affected by a change
	 * @param invalidRules
	 *        A {@link Set} of {@link RoleProvider} containing invalid rules.
	 * @throws StorageException
	 *         if some error occurs while requesting the database
	 */
    @Override
	public void internalUpdateSecurity(Object aChangesInformation, Object invalidRules) throws StorageException {
        if (!(aChangesInformation instanceof Map)) {
            throw new IllegalArgumentException("aChangesInformation must be instanceof Map.");
        }
		if (!(invalidRules instanceof Set)) {
			throw new IllegalArgumentException("invalidRules must be instanceof Set.");
		}
        Map<RoleProvider,Collection<BoundObject>> theChangedMap = (Map<RoleProvider,Collection<BoundObject>>) aChangesInformation;
		ElementAccessManager theAM = getAccessManager();
        // vector for insert parameters - group, object, role, rule
		Object[] theVector = new Object[] { null, null, null, null };

        // Reevaluate rules in second step:
        int            iCount       = 0;
		List<Object[]> batchRemoves = new ArrayList<>();
		List<Object[]> batchInserts = new ArrayList<>();
		Map<Object, Object> sharedIds = new HashMap<>();

		// Cache mode has no real effect when using incremental updates
		theAM.beginCacheMode();
        try {
            // Iterator <RoleRule>
            long startTime = System.currentTimeMillis();
            for (RoleProvider theRule : theChangedMap.keySet()) {
            	long startRule = System.currentTimeMillis();
            	long existingTime = 0;
            	long newTime      = 0;
                Collection<BoundObject> theBusinessObjects = theChangedMap.get(theRule);
				Integer theRuleId = theAM.getPersitancyId(theRule);
				assert theRuleId != null : "Rule " + theRule + " has no ID in the persistency layer.";

                theVector[0] = null;
                theVector[1] = null;
                theVector[2] = roleId(theRule);
                theVector[3] = theRuleId;
                
                Map<BoundObject, Collection<Wrapper>> someToAdd    = new HashMap<>();
                Map<BoundObject, Collection<Wrapper>> someToRemove = new HashMap<>();
                
                long compEA = System.currentTimeMillis();
				Map<TLID, List<Group>> someExisting;
                try {
                	someExisting = this.executor.getGroupsByObject(theVector, CollectionUtil.toList(theBusinessObjects));
                }
                catch (SQLException e) {
                    throw new StorageException("Error while requesting the database.", e);
                }
                long compEB = System.currentTimeMillis();
                existingTime = compEB - compEA;
                // Iterator <Business Object>
                for (BoundObject theBusinessObject : theBusinessObjects) {
					TLID theBOId = theBusinessObject.getID();
					theVector[1] = theBOId.toStorageValue();

                    if (theVector[1] == null) {
                        // the object has no id, so security can be cleaned.
                        // this is not a security problem because the referenced object is no longer valid,
                        // so no false roles will be available.
                        //
                        // This may result in garbage in the security storage.
                        continue;
                    }

                    theVector[0] = null;

					Collection<Group> theExistingGroups = CollectionUtil.toCollection(someExisting.get(theBOId));
                    Collection<Group> theNewGroups      = theRule.getGroups(theBusinessObject);

                    Collection<Wrapper> theGroupsToAdd    = new HashSet<>(theNewGroups);
                    Collection<Wrapper> theGroupsToRemove = new HashSet<>(theExistingGroups);

                    theGroupsToAdd   .removeAll(theExistingGroups);
                    theGroupsToRemove.removeAll(theNewGroups);
                    
                    someToAdd   .put(theBusinessObject, theGroupsToAdd);
                    someToRemove.put(theBusinessObject, theGroupsToRemove);

                }
                for (BoundObject theBusinessObject : theBusinessObjects) {
					theVector[1] = securityId(theBusinessObject);

                    if (theVector[1] == null) {
                        // the object has no id, so security can be cleaned.
                        // this is not a security problem because the referenced object is no longer valid,
                        // so no false roles will be available.
                        //
                        // This may result in garbage in the security storage.
                        continue;
                    }

                    theVector[0] = null;
                    
                    for (Wrapper theGroup : someToRemove.get(theBusinessObject)) {
						theVector[0] = securityId(((BoundObject) theGroup));
                        try {
							batchInsert(theVector, batchRemoves, sharedIds);
                            iCount++;
                        }
                        catch (IllegalArgumentException e) {
                            Logger.warn("An element is null - something is wrong in this world.", e, ElementSecurityStorage.class);
                        }
                    }
                    

                	for (Wrapper theGroup : someToAdd.get(theBusinessObject)) {
						theVector[0] = securityId(((BoundObject) theGroup));
                        try {
							batchInsert(theVector, batchInserts, sharedIds);
                            iCount++;
                        }
                        catch (IllegalArgumentException e) {
                            Logger.warn("An element is null - something is wrong in this world.", e, ElementSecurityStorage.class);
                        }
                    }
                }
				flushDeleteAndInsert(batchRemoves, batchInserts, sharedIds);
            }

            multiDeleteAndInsert(batchRemoves, batchInserts);
			sharedIds = null; // release memory

			fullRebuildRules(theAM, (Set<RoleProvider>) invalidRules);

			int numberBatches = iCount;
			Logger
				.debug(
					() -> "ElementSecurityStorage: Reevaluate rules in second step with " + numberBatches
							+ " inserts : " + DebugHelper.getTime(System.currentTimeMillis() - startTime),
					ElementSecurityStorage.class);
        }
        finally {
            theAM.endCacheMode();
        }
	}

	/**
	 * Rebuilds the security storage for the given {@link RoleProvider rules}.
	 */
	protected void fullRebuildRules(ElementAccessManager accessManager, Set<RoleProvider> invalidRules)
			throws StorageException {
		if (invalidRules.isEmpty()) {
			return;
		}
		try {
			int removedEntries = executor.removeReasons(invalidRules.stream().map(accessManager::getPersitancyId).iterator());
			Logger.debug(() -> "Removed " + removedEntries + " for invalid rules.", ElementSecurityStorage.class);
		} catch (SQLException e) {
			throw new StorageException("Error while requesting the database.", e);
		}
		computeRoles(invalidRules, true);
	}

	protected void flushDeleteAndInsert(List<Object[]> batchRemoves, List<Object[]> batchInserts, Map<?, ?> sharedIds) throws StorageException {
        multiDeleteAndInsert(batchRemoves, batchInserts);
        batchRemoves.clear();
        batchInserts.clear();
		sharedIds.clear();
    }

    /**
	 * Prepares an entry to get inserted to the database. Instead of inserting it directly, it is
	 * added only to the given batchInserts list. Call {@link #multiInsert(List)} to insert all
	 * entries which were prepared by this method before. See {@link #insert(Object[])} for
	 * parameter details.
	 * 
	 * @param aVector
	 *        the parameter array, must be of length 4.
	 * @param batchInserts
	 *        result list for entries to insert after computation is finished
	 * @param sharedIds
	 *        map to share stings for batch inserts to reduce memory usage
	 */
	protected void batchInsert(Object[] aVector, List<Object[]> batchInserts, Map<Object, Object> sharedIds) {
        if (aVector == null || aVector.length != 4 || aVector[0] == null || aVector[1] == null || aVector[2] == null || aVector[3] == null) {
            throw new IllegalArgumentException("Illegal Arguments: aVector must be of length 4 and no parameter must be null.");
        }
        // cloning is required because the given vector is reused by calling methods
		Object[] clone = new Object[aVector.length];
        for (int i = 0; i < aVector.length; i++) {
			clone[i] = MapUtil.shareObject(sharedIds, aVector[i]);
        }
        batchInserts.add(clone);
    }



    /**
     * Creates the database entries based on rules.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    @Override
    protected void doComputeRoles() throws StorageException {
		ElementAccessManager theAM = getAccessManager();
        super.doComputeRoles();

		theAM.beginCacheMode();
        try {
            Logger.info("Computing rules...", ElementSecurityStorage.class);
			StopWatch sw = StopWatch.createStartedWatch();
            computeMERuleBasedRoles();
			Logger.info("Computing of rules completed in " + sw + ".", ElementSecurityStorage.class);
        }
        finally {
			theAM.endCacheMode();
        }
    }



    /**
	 * Creates the database entries based on {@link TLClass} rules.
	 *
	 * @throws StorageException
	 *         if some error occurs while requesting the database
	 */
    protected void computeMERuleBasedRoles() throws StorageException {
		Logger.info("Computing ME rules...", ElementSecurityStorage.class);
		StopWatch sw = StopWatch.createStartedWatch();

		RoleComputationResult counter =
			internalComputeMERuleBasedRoles(getAccessManager().getResolvedMERules(), false);

		Logger.info("Done in " + sw + ". Executed " + counter.executions() + " ME based rules (business objects: "
				+ counter.objects() + ", rules: " + counter.rules() + "). Computed entries: " + counter.entries() + ".",
			ElementSecurityStorage.class);
	}

	/**
	 * Creates the database entries for the given rules.
	 *
	 * @throws StorageException
	 *         if some error occurs while requesting the database
	 */
	protected void computeRoles(Collection<? extends RoleProvider> rules, boolean inTransaction)
			throws StorageException {
		Map<TLClass, Collection<RoleProvider>> allRules = getAccessManager().getResolvedMERules();
		Map<TLClass, Collection<RoleProvider>> filtered = new HashMap<>();
		for (Entry<TLClass, Collection<RoleProvider>> e : allRules.entrySet()) {
			HashSet<RoleProvider> rulesForType = new HashSet<>(e.getValue());
			rulesForType.retainAll(rules);
			if (!rulesForType.isEmpty()) {
				filtered.put(e.getKey(), rulesForType);
			}
		}

		internalComputeMERuleBasedRoles(filtered, inTransaction);
	}

	/**
	 * Creates the database entries for the given types.
	 * 
	 * @param rulesByType
	 *        Mapping from the type to the rules for which roles must be computed.
	 * @param inTransaction
	 *        Whether the uncommitted changes has to be considered.
	 */
	protected RoleComputationResult internalComputeMERuleBasedRoles(Map<TLClass, Collection<RoleProvider>> rulesByType,
			boolean inTransaction) throws StorageException {
        int counterBOs = 0, counterRules = 0, counterExecs = 0, counterEntries = 0;

		List<Object[]> batchInserts = new ArrayList<>();
		Map<Object, Object> sharedIds = new HashMap<>();

        // Vector for insert parameters - group, object, role, rule
		Object[] theVector = new Object[] { null, null, null, null };
		ElementAccessManager am = getAccessManager();

		Iterator<TLClass> itME = rulesByType.keySet().iterator();
        while (itME.hasNext()) {
			TLClass theME = itME.next();
			Collection<RoleProvider> theRules = rulesByType.get(theME);
            counterRules += theRules.size();

			try (CloseableIterator<TLObject> objects =
				AttributeOperations.allDirectInstances(theME, inTransaction, TLObject.class)) {
				while (objects.hasNext()) {
					TLObject theBusinessObject = objects.next();
					theVector[1] = objectId(theBusinessObject);
					counterBOs++;

					Iterator<RoleProvider> itRule = theRules.iterator();
					while (itRule.hasNext()) {
						RoleProvider theRule = itRule.next();
					    theVector[2] = roleId(theRule);
						theVector[3] = am.getPersitancyId(theRule);
						Collection<Group> theGroups = theRule.getGroups((BoundObject) theBusinessObject);
					    counterExecs++;

						Iterator<Group> itGroup = theGroups.iterator();
					    while (itGroup.hasNext()) {
							Group theGroup = itGroup.next();
							theVector[0] = securityId(theGroup);
					        try {
								batchInsert(theVector, batchInserts, sharedIds);
					            counterEntries++;
					        }
					        catch (IllegalArgumentException e) {
					            Logger.warn("An element is null - something is wrong in this world.", e, ElementSecurityStorage.class);
					        }
					    }
					}
                }
            }
        }
		multiInsert(batchInserts);

		return new RoleComputationResult(counterBOs, counterRules, counterExecs, counterEntries);
	}

	private static record RoleComputationResult(int objects, int rules, int executions, int entries) {
	}

	private Object roleId(RoleProvider theRule) {
		return securityId(theRule.getRole());
	}

}
