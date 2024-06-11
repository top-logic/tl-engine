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
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.ExternalRoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
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

    /**
	 * Updates the database entries based on rules by reevaluating affected rules for affected
	 * business objects.
	 *
	 * This is a optimized version. It only commits changes to the security storage, i.e. removes
	 * only entries which will not be added again and adds only entries that are not already in the
	 * storage.
	 *
	 * @param aChangesInformation
	 *        a Map of {@link RoleRule} -> {@link Collection} of {@link Wrapper} containing all
	 *        rules and business objects affected by a change
	 * @throws StorageException
	 *         if some error occurs while requesting the database
	 */
    @Override
    public void internalUpdateSecurity(Object aChangesInformation) throws StorageException {
        if (!(aChangesInformation instanceof Map)) {
            throw new IllegalArgumentException("aChangesInformation must be instanceof Map.");
        }
        if (!(getAccessManager() instanceof ElementAccessManager)) {
            throw new StorageException("Cannot get instance of ElementAccessManager. Configured AccessManager must be instanceof ElementAccessManager.");
        }

        Map<RoleProvider,Collection<BoundObject>> theChangedMap = (Map<RoleProvider,Collection<BoundObject>>) aChangesInformation;
        ElementAccessManager                      theAM         = (ElementAccessManager)getAccessManager();

        // vector for insert parameters - group, object, role, rule
		Object[] theVector = new Object[] { null, null, null, null };

        // Reevaluate rules in second step:
        int            iCount       = 0;
		List<Object[]> batchRemoves = new ArrayList<>();
		List<Object[]> batchInserts = new ArrayList<>();
		Map<Object, Object> sharedIds = new HashMap<>();

        try {
            // Cache mode has no real effect when using incremental updates
            theAM.beginCacheMode();

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

            Logger.debug("ElementSecurityStorage: Reevaluate rules in second step with " + iCount + " inserts : " + DebugHelper.getTime(System.currentTimeMillis() - startTime), ElementSecurityStorage.class);
        }
        finally {
            theAM.endCacheMode();
        }
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
        AccessManager theAM = getAccessManager();
        if (!(theAM instanceof ElementAccessManager)) {
            throw new StorageException("Cannot get instance of ElementAccessManager");
        }
        super.doComputeRoles();

        ((ElementAccessManager)theAM).beginCacheMode();
        try {
            Logger.info("Computing rules...", ElementSecurityStorage.class);
			StopWatch sw = StopWatch.createStartedWatch();
            computeMERuleBasedRoles();
            computeMORuleBasedRoles();
            computeExternalRulesBasedRoles();
			Logger.info("Computing of rules completed in " + sw + ".", ElementSecurityStorage.class);
        }
        finally {
            ((ElementAccessManager)theAM).endCacheMode();
        }
    }



    /**
     * Compute all roles based on the configured {@link ExternalRoleProvider}s.
     */
    public void computeExternalRulesBasedRoles() {
        Logger.info("Computing external rules...", ElementSecurityStorage.class);
		StopWatch sw = StopWatch.createStartedWatch();
        ElementAccessManager theAM = (ElementAccessManager)getAccessManager();
        Map<String, ExternalRoleProvider> externalRules = theAM.getExternalRules();
        int counterRules = externalRules.size();
        for(Iterator<Map.Entry<String, ExternalRoleProvider>> theIt = externalRules.entrySet().iterator(); theIt.hasNext(); ) {
            Entry<String, ExternalRoleProvider> theNext = theIt.next();
            ExternalRoleProvider theRoleRuleFactory = theNext.getValue();
            theRoleRuleFactory.computeRoles(this.executor);
        }
		Logger.info("Done in " + sw + ". Executed " + counterRules + " external rules.", ElementSecurityStorage.class);
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
        int counterBOs = 0, counterRules = 0, counterExecs = 0, counterEntries = 0;
        ElementAccessManager theAM = (ElementAccessManager)getAccessManager();

		List<Object[]> batchInserts = new ArrayList<>();
		Map<Object, Object> sharedIds = new HashMap<>();

        // Vector for insert parameters - group, object, role, rule
		Object[] theVector = new Object[] { null, null, null, null };

		Map<TLClass, Collection<RoleProvider>> theMetaElements = theAM.getResolvedMERules();

		Iterator<TLClass> itME = theMetaElements.keySet().iterator();
        while (itME.hasNext()) {
			TLClass theME = itME.next();
			Collection<RoleProvider> theRules = theMetaElements.get(theME);
            counterRules += theRules.size();

			try (CloseableIterator<Wrapper> theObjectIterator =
				MetaElementUtil.iterateDirectInstances(theME, Wrapper.class)) {
                while (theObjectIterator.hasNext()) {
					Wrapper theBusinessObject = theObjectIterator.next();
					theVector[1] = objectId(theBusinessObject);
					counterBOs++;

					Iterator<RoleProvider> itRule = theRules.iterator();
					while (itRule.hasNext()) {
						RoleProvider theRule = itRule.next();
					    theVector[2] = roleId(theRule);
					    theVector[3] = theAM.getPersitancyId(theRule);
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
		Logger.info(
			"Done in " + sw + ". Executed " + counterExecs + " ME based rules (business objects: " + counterBOs
				+ ", rules: " + counterRules + "). Computed entries: " + counterEntries + ".",
			ElementSecurityStorage.class);
	}

    /**
     * Creates the database entries based on MetaObject rules.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    protected void computeMORuleBasedRoles() throws StorageException {
        Logger.info("Computing MO rules...", ElementSecurityStorage.class);
		StopWatch sw = StopWatch.createStartedWatch();
        int counterBOs = 0, counterRules = 0, counterExecs = 0, counterInserts = 0;
        KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
        ElementAccessManager theAM = (ElementAccessManager)getAccessManager();

		List<Object[]> batchInserts = new ArrayList<>();
		Map<Object, Object> sharedIds = new HashMap<>();

        // Vector for insert parameters - group, object, role, rule
		Object[] theVector = new Object[] { null, null, null, null };

        // Map < MetaObject, Collection < Rule > >
		Map<MetaObject, Collection<RoleProvider>> theMetaObjects = theAM.getResolvedMORules();

        // Iterator <MetaObject>
		Iterator<MetaObject> itMO = theMetaObjects.keySet().iterator();
        while (itMO.hasNext()) {
			MetaObject theMO = itMO.next();
			Collection<RoleProvider> theRules = theMetaObjects.get(theMO);
            counterRules += theRules.size();

            // Iterator <Business Object>
			Iterator<TLObject> theObjectIterator = WrapperFactory.getWrappersByType(theMO.getName(), theKB).iterator();
			while (theObjectIterator.hasNext()) {
				TLObject theBusinessObject = theObjectIterator.next();
			    theVector[1] = objectId(theBusinessObject);
			    counterBOs++;

			    // Iterator <RoleRule>
				Iterator<RoleProvider> itRule = theRules.iterator();
			    while (itRule.hasNext()) {
					RoleProvider theRule = itRule.next();
			        theVector[2] = roleId(theRule);
			        theVector[3] = theAM.getPersitancyId(theRule);
					Collection<Group> theGroups = theRule.getGroups((BoundObject) theBusinessObject);
			        counterExecs++;

			        // Iterator <Group>
					Iterator<Group> itGroup = theGroups.iterator();
			        while (itGroup.hasNext()) {
						Group theGroup = itGroup.next();
						theVector[0] = securityId(theGroup);
			            try {
							batchInsert(theVector, batchInserts, sharedIds);
			                counterInserts++;
			            }
			            catch (IllegalArgumentException e) {
			                Logger.warn("An element is null - something is wrong in this world.", e, ElementSecurityStorage.class);
			            }
			        }
			    }
			}
        }
        multiInsert(batchInserts);
		Logger.info(
			"Done in " + sw + ". Executed " + counterExecs + " MO based rules (business objects: " + counterBOs
				+ ", rules: " + counterRules + "). Computed entries: " + counterInserts + ".",
			ElementSecurityStorage.class);
    }

	private Object roleId(RoleProvider theRule) {
		return securityId(theRule.getRole());
	}

}
