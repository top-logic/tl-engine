/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.Computation;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.rule.ExternalRoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItemUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Algorithm updating the {@link SecurityStorage} when business objects change.
 * 
 * @see #startUp(ElementAccessManager, SecurityStorage) Must be called with the {@link AccessManager}
 *      and {@link SecurityStorage} before the {@link ElementSecurityUpdateManager} is used.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementSecurityUpdateManager implements ConfiguredInstance<ElementSecurityUpdateManager.Config> {

	/**
	 * {@link TypedConfiguration} interface for the {@link ElementSecurityUpdateManager}.
	 */
	public interface Config extends PolymorphicConfiguration<ElementSecurityUpdateManager> {

		/** Property name of {@link #getLogHandler()}. */
		String LOG_HANDLER = "log-handler";

		/**
		 * The {@link LogHandler} is used to log security updates.
		 * 
		 * @see ElementSecurityUpdateManager#getLogHandler()
		 */
		@Name(LOG_HANDLER)
		@InstanceFormat
		@InstanceDefault(NullLogHandler.class)
		LogHandler getLogHandler();

	}

	/** Saves the security storage instance. */
	protected SecurityStorage securityStorage;

    /** Saves the element access manager instance. */
	protected ElementAccessManager accessManager;

	private final LogHandler _logHandler;

	private final Config _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ElementSecurityUpdateManager}.
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
	public ElementSecurityUpdateManager(InstantiationContext context, Config config) {
		_config = config;
		_logHandler = getLogHandler(context, config);
	}

	private LogHandler getLogHandler(InstantiationContext context, Config config) {
		LogHandler logHandler = config.getLogHandler();
		if (logHandler == null) {
			String message = "LogHandler is not allowed to be null. Use " + NullLogHandler.class.getSimpleName()
				+ " to disable logging.";
			context.error(message, new NullPointerException(message));
		}
		return logHandler;
	}

	/**
	 * Has to be called before this class is used.
	 * <p>
	 * Every caller has to call {@link #shutDown()}, too.
	 * </p>
	 * 
	 * @param accessManager
	 *        The {@link ElementAccessManager} which uses this {@link ElementSecurityUpdateManager}
	 * @param securityStorage
	 *        The {@link SecurityStorage} which is used together with this
	 *        {@link ElementSecurityUpdateManager}
	 */
	public void startUp(ElementAccessManager accessManager, SecurityStorage securityStorage) {
		this.accessManager = accessManager;
		this.securityStorage = securityStorage;
	}

	/**
	 * Has to be called when this instance is no longer used.
	 * 
	 * @see #startUp(ElementAccessManager, SecurityStorage)
	 */
	public void shutDown() {
		// Hook for shutdown code, for potential subclasses and to keep the symmetry with "startUp".
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * @see Config#getLogHandler()
	 * 
	 * @return Never null.
	 */
	public LogHandler getLogHandler() {
		return _logHandler;
	}

	public synchronized void handleSecurityUpdate(KnowledgeBase kb, Map<TLID, Object> someChanged,
			Map<TLID, Object> someNew, final Map<TLID, Object> someRemoved, CommitHandler aHandler) {

        // This map holds the affected rules mapped to the set of affected objects
		final Map<RoleProvider, Collection<BoundObject>> rulesToObjectsMap =
			new HashMap<>();
		final Map<RoleProvider, Collection<BoundObject>> rulesToDeletedObjectsMap =
			new HashMap<>();
		Map<BoundRole, Collection<Object>> newHasRoleChanged = new HashMap<>();
		final Map<BoundRole, Collection<Object>> deletedHasRoleChanged = new HashMap<>();

        // Contains the business objects whose roles became invalid for inheritance rules
        // and rules must reevaluate this roles manually in storage access manager.
		final Set<BoundObject> invalidObjects = new HashSet<>();
        
        if (!someNew.isEmpty() || !someRemoved.isEmpty()) {
            securityStorage.begin(aHandler);
            handleAssociations(someNew, rulesToObjectsMap, newHasRoleChanged, invalidObjects, true);
			kb.withoutModifications(new Computation<Void>() {

				@Override
				public Void run() {
					handleAssociations(someRemoved, rulesToDeletedObjectsMap, deletedHasRoleChanged, invalidObjects,
						false);
					return null;
				}
			});
			handleInheritance(kb, rulesToObjectsMap, rulesToDeletedObjectsMap, newHasRoleChanged,
				deletedHasRoleChanged, someRemoved.values());
            handleNewObjects(someNew, rulesToObjectsMap);

            Map<BoundRole, Set<BoundObject>> invalidObjectsByRole = mergeValuesByRole(rulesToObjectsMap);

			getLogHandler().logSecurityUpdate(someNew, someRemoved, rulesToObjectsMap, invalidObjects);

            long start = System.currentTimeMillis();
            try {
                // Set invalid objects
                if (accessManager instanceof StorageAccessManager) {
                    ((StorageAccessManager)accessManager).setInvalidObjects(invalidObjects, invalidObjectsByRole);
                }

                try { // remove security entries of removed objects
                    securityStorage.removeObjects(someRemoved);
                }
                catch (StorageException ex) {
                    Logger.error("Failed to remove security entries of removed objects.", ex, this);
                }

                try { // update security entries of changed objects
                    securityStorage.updateSecurity(rulesToObjectsMap);
                }
                catch (StorageException ex) {
                    Logger.error("Failed to update security entries of changed objects.", ex, this);
                }
            }
            finally {
                if (accessManager instanceof StorageAccessManager) {
                    ((StorageAccessManager)accessManager).removeInvalidObjects();
                }
                long delta = System.currentTimeMillis() - start;
                if (delta > 3000) {
                    Logger.warn("Incremental security update needed " + DebugHelper.getTime(delta), ElementSecurityUpdateManager.class);
                }
            }

        }
    }





    /**
     * This method handles new created objects.
     */
	private void handleNewObjects(Map<TLID, Object> someNew,
			Map<RoleProvider, Collection<BoundObject>> rulesToObjectsMap) {
        Map<TLClass, Collection<RoleProvider>> meRules = accessManager.getResolvedMERules();
        Map<MetaObject, Collection<RoleProvider>> moRules = accessManager.getResolvedMORules();

        for (Object newObject : someNew.values()) {
            if (newObject instanceof KnowledgeObject) {
				newObject = WrapperFactory.getWrapper((KnowledgeObject) newObject);
            }
            if (!(newObject instanceof BoundObject)) continue;

            if (newObject instanceof Wrapper) {
				TLStructuredType me = ((Wrapper) newObject).tType();
                Collection<RoleProvider> rules = meRules.get(me);
                if (rules != null)
                for (RoleProvider rule : rules) {
                    getOrCreateSet(rulesToObjectsMap, rule).add((BoundObject)newObject);
                }
            }
            if (newObject instanceof Wrapper) {
                MetaObject mo = ((Wrapper)newObject).tTable();
                Collection<RoleProvider> rules = moRules.get(mo);
                if (rules != null)
                for (RoleProvider rule : rules) {
                    getOrCreateSet(rulesToObjectsMap, rule).add((BoundObject)newObject);
                }
                Collection<ExternalRoleProvider> extRules = accessManager.getAffectedRoleRuleFactories(mo.getName());
                if (extRules != null)
                for (RoleProvider rule : extRules) {
                    getOrCreateSet(rulesToObjectsMap, rule).add((BoundObject)newObject);
                }
            }
        }
    }



    /**
	 * This method handles inheritance.
	 * 
     * @param aRulesToObjectsMap
	 *        Map < RoleRule , Collection < BoundObject > >
	 */
	private void handleInheritance(KnowledgeBase kb,
			final Map<RoleProvider, Collection<BoundObject>> aRulesToObjectsMap,
			final Map<RoleProvider, Collection<BoundObject>> aRulesToDeletedObjectsMap,
			Map<BoundRole, Collection<Object>> someNewHasRoles,
			final Map<BoundRole, Collection<Object>> someDeletedHasRoles, final Collection<Object> removed) {

        // for each rule we have a set of affected objects
        for (Map.Entry<BoundRole,Collection<Object>>  theEntry : someNewHasRoles.entrySet()) {
            BoundRole          theRole    = theEntry.getKey();
            Collection<Object> theObjects = theEntry.getValue();

            // we take all inheritance rules that declare the given rule's role as source role
            for (Iterator theRulesIt = accessManager.getRulesWithSourceRole(theRole, Type.inheritance).iterator(); theRulesIt.hasNext(); ) {
                RoleProvider   theInheritRule = (RoleProvider) theRulesIt.next();

                // for each inheritance rule we determine the affected base objects starting form the objects affected by the given role rule
                // All these objects are added to the new "rules to be considered" map
                for (Iterator<Object> theObIt = theObjects.iterator(); theObIt.hasNext();) {
                    Object           theObject      = theObIt.next();
                    Set<BoundObject> theBaseObjects = theInheritRule.getBaseObjects(theObject);
                    if (!CollectionUtil.isEmptyOrNull(theBaseObjects)) {
                        Collection<BoundObject> theRuleObjects = this.getOrCreateSet(aRulesToObjectsMap, theInheritRule);
                        theRuleObjects.addAll(theBaseObjects);
                    }
                }
            }
        }

		kb.withoutModifications(new Computation<Void>() {

			@Override
			public Void run() {
				handleDeletedRoles(someDeletedHasRoles, aRulesToObjectsMap);

				for (Map.Entry<RoleProvider, Collection<BoundObject>> theEntry : aRulesToDeletedObjectsMap.entrySet()) {
					RoleProvider theRule = theEntry.getKey();
					Collection<BoundObject> theObjects = theEntry.getValue();
					BoundRole theRole = theRule.getRole();

					// we take all inheritance rules that declare the given rule's role as source
					// role
					for (Iterator theRulesIt =
						accessManager.getRulesWithSourceRole(theRole, Type.inheritance).iterator(); theRulesIt
						.hasNext();) {
						RoleProvider theInheritRule = (RoleProvider) theRulesIt.next();

						// for each inheritance rule we determine the affected base objects starting
						// form
						// the objects affected by the given role rule
						// All these objects are added to the new "rules to be considered" map
						for (Iterator theObIt = theObjects.iterator(); theObIt.hasNext();) {
							BoundObject theObject = (BoundObject) theObIt.next();
							Set<BoundObject> theBaseObjects = theInheritRule.getBaseObjects(theObject);
							Iterator<BoundObject> iterator = theBaseObjects.iterator();
							BoundObject notDeleted = null;
							while (iterator.hasNext()) {
								BoundObject affectedObject = iterator.next();
								if (!removed.contains(affectedObject.tHandle())) {
									notDeleted = affectedObject;
									break;
								}
							}
							if (notDeleted == null) {
								// all affected are also deleted
								continue;
							}
							Collection<BoundObject> theRuleObjects = getOrCreateSet(aRulesToObjectsMap, theInheritRule);
							theRuleObjects.add(notDeleted);
							while (iterator.hasNext()) {
								BoundObject affectedObject = iterator.next();
								if (!removed.contains(affectedObject.tHandle())) {
									theRuleObjects.add(affectedObject);
								}
							}
						}
					}
				}
				return null;
			}
		});

        Map<RoleProvider, Collection <BoundObject>> theRulesToObjectsToBeConsidered = aRulesToObjectsMap;

        int i = 0;
        // repeat until no more rules are to be considered
        while ( (! theRulesToObjectsToBeConsidered.isEmpty()) && (i < 50) ) {

            i++;
            // initially there are no more rules to be considered
            Map<RoleProvider, Collection<BoundObject>> someMoreRulesToObjects = new HashMap<>();
            // for each rule we have a set of affected objects
            for (Iterator<Map.Entry<RoleProvider, Collection <BoundObject>>> theIt = theRulesToObjectsToBeConsidered.entrySet().iterator(); theIt.hasNext();) {
                Map.Entry<RoleProvider, Collection <BoundObject>> theEntry   = theIt.next();
                RoleProvider                                      theRule    = theEntry.getKey();
                Collection<BoundObject>                           theObjects = theEntry.getValue();
                BoundRole  theRole    = theRule.getRole();

                // we take all inheritance rules that declare the given rule's role as source role
                for (Iterator theRulesIt = accessManager.getRulesWithSourceRole(theRole, Type.inheritance).iterator(); theRulesIt.hasNext(); ) {
                    RoleProvider   theInheritRule = (RoleProvider) theRulesIt.next();

                    // for each inheritance rule we determine the affected base objects starting form the objects affected by the given role rule
                    // All these objects are added to the new "rules to be considered" map
                    for (Iterator theObIt = theObjects.iterator(); theObIt.hasNext();) {
                        BoundObject      theObject      = (BoundObject) theObIt.next();
                        Set<BoundObject> theBaseObjects = theInheritRule.getBaseObjects(theObject);
                        if (!CollectionUtil.isEmptyOrNull(theBaseObjects)) {
                            Collection<BoundObject> theRuleObjects = this.getOrCreateSet(someMoreRulesToObjects, theInheritRule);
                            theRuleObjects.addAll(theBaseObjects);
                        }
                    }
                }
            }
            // finally we add all the new entries to the original "rules to be considered" map
            // in order to return the enriched map to the calling method
            mergeMaps(someMoreRulesToObjects, aRulesToObjectsMap);
            theRulesToObjectsToBeConsidered = someMoreRulesToObjects;
        }
        if (i > 40) {
            Logger.warn("Inheritance rules to deep: " + i + " RoleProviders: " + Mappings.map(new Mapping<RoleProvider, String>() {
                   @Override
				public String map(RoleProvider input) {
                       return input.getId();
                   }
            }, theRulesToObjectsToBeConsidered.keySet()), this);
        }

		// Enhance output map with deleted objects.
		mergeMaps(aRulesToDeletedObjectsMap, aRulesToObjectsMap);

    }

	void handleDeletedRoles(Map<BoundRole, Collection<Object>> someDeletedHasRoles,
			Map<RoleProvider, Collection<BoundObject>> aRulesToObjectsMap) {
		// for each rule we have a set of affected objects
		for (Entry<BoundRole, Collection<Object>> entry : someDeletedHasRoles.entrySet()) {
			// we take all inheritance rules that declare the given rule's role as source role
			BoundRole deletedRole = entry.getKey();
			Collection<Object> objectsWithDeletedRoles = entry.getValue();
			for (Object roleProvider : accessManager.getRulesWithSourceRole(deletedRole, Type.inheritance)) {
				RoleProvider inheritRule = (RoleProvider) roleProvider;

				/* for each inheritance rule we determine the affected base objects starting form
				 * the objects affected by the given role rule */
				/* All these objects are added to the new "rules to be considered" map */
				for (Object source : objectsWithDeletedRoles) {
					Set<BoundObject> affectedObjects = inheritRule.getBaseObjects(source);
					if (CollectionUtil.isEmptyOrNull(affectedObjects)) {
						continue;
                    }
					getOrCreateSet(aRulesToObjectsMap, inheritRule).addAll(affectedObjects);
                }
            }
        }
	}

	private void mergeMaps(Map<RoleProvider, Collection<BoundObject>> source,
			final Map<RoleProvider, Collection<BoundObject>> into) {
		for (Iterator<Map.Entry<RoleProvider, Collection<BoundObject>>> theIt = source.entrySet().iterator(); theIt.hasNext();) {
		    Map.Entry<RoleProvider, Collection<BoundObject>> theEntry   = theIt.next();
		    RoleProvider                                     theRule    = theEntry.getKey();
		    Collection<BoundObject>                          theObjects = theEntry.getValue();
		    if (!CollectionUtil.isEmptyOrNull(theObjects)) {
		        Collection<BoundObject> theRuleObjects = this.getOrCreateSet(into, theRule);
		        theRuleObjects.addAll(theObjects);
		    }
		}
	}



	void handleAssociations(Map someAssociations, Map<RoleProvider, Collection<BoundObject>> rulesToObjectsMap,
			Map<BoundRole, Collection<Object>> hasRoleChanged, Set<BoundObject> invalidObjects, boolean isAdded) {
        for (Iterator theIt = someAssociations.values().iterator(); theIt.hasNext();) {
            Object theObject = theIt.next();
            if (KnowledgeItemUtil.instanceOfKnowledgeAssociation(theObject)) {
                KnowledgeAssociation theDO = (KnowledgeAssociation) theObject;
                MetaObject theMO     = theDO.tTable();
                String     theMOType = theMO.getName();
				if (WrapperMetaAttributeUtil.isAttributeReferenceAssociation(theDO)) {
                    this.handleAttributedAssociationChange(theDO, rulesToObjectsMap, isAdded);
                    this.handleAssociationChange(theDO, rulesToObjectsMap, isAdded);
                } else if (BoundedRole.HAS_ROLE_ASSOCIATION.equals(theMOType)) {
                    try {
						BoundRole theRole = (BoundRole) WrapperFactory.getWrapper(theDO.getDestinationObject());
						Collection<Object> theRoleObjects = this.getOrCreateSet(hasRoleChanged, theRole);
						BoundObject theBusinessObject =
							(BoundObject) WrapperFactory.getWrapper(theDO.getSourceObject());
						invalidObjects.add(theBusinessObject);
						theRoleObjects.add(theBusinessObject);
                        // update security storage with direct haseRole associations
                        try {
                            if (isAdded) {
                                securityStorage.insert(theDO);
                            }
                            else {
                                securityStorage.remove(theDO);
                            }
                        }
                        catch (StorageException ex) {
                            Logger.warn("Could not update hasRole association in security storage.", ex, ElementSecurityUpdateManager.class);
                        }
                    }
                    catch (InvalidLinkException ex) {
                        // is ok, source data object was removed too, so security update will be triggered by removed source object
                        //Logger.error("Failed to handle "+(isAdded?"new":"removed")+" hasRole knowledge association: "+theDO.toString(), ex, ElementSecurityUpdateManager.class);
                    }
                } else {
                    this.handleAssociationChange(theDO, rulesToObjectsMap, isAdded);
                }
            }
        }
    }

    private void handleAssociationChange(KnowledgeAssociation aKA, Map<RoleProvider, Collection<BoundObject>> aRulesToObjectsMap, boolean isAdded) {
        try {
            String theKATypeName = aKA.tTable().getName();
            for (Iterator theIt = accessManager.getRules(theKATypeName).iterator(); theIt.hasNext();) {
                RoleRule         theRule        = (RoleRule) theIt.next();
                Set<BoundObject> theBaseObjects = ElementAccessHelper.traversRoleRuleBackwards(theRule, aKA);
                if (!CollectionUtil.isEmptyOrNull(theBaseObjects)) {
                    Collection<BoundObject> theRuleObjects = this.getOrCreateSet(aRulesToObjectsMap, theRule);
                    theRuleObjects.addAll(theBaseObjects);
                }
            }
            for(ExternalRoleProvider theFactory : accessManager.getAffectedRoleRuleFactories(theKATypeName)) {
                Set<BoundObject> theAffected = theFactory.getAffectedObjects(aKA);
                if (!CollectionUtil.isEmptyOrNull(theAffected)) {
                    Collection<BoundObject> theRuleObjects = this.getOrCreateSet(aRulesToObjectsMap, theFactory);
                    theRuleObjects.addAll(theAffected);
                }
            }
        }
        catch (Exception ex) {
            Logger.error("Failed to handle "+(isAdded?"new":"removed")+" meta attribute knowledge association: "+aKA.toString(), ex, ElementSecurityUpdateManager.class);
        }
    }

    private void handleAttributedAssociationChange(KnowledgeAssociation aKA, Map<RoleProvider, Collection<BoundObject>> aRulesToObjectsMap, boolean isAdded) {
        try {
            TLStructuredTypePart theMA = WrapperMetaAttributeUtil.getMetaAttribute(aKA);

            for (Iterator<RoleProvider> theIt = accessManager.getRules(theMA).iterator(); theIt.hasNext();) {
                RoleProvider theRule = theIt.next();
                if (theRule instanceof RoleRule) {
                    Set<BoundObject> theBaseObjects = ElementAccessHelper.traversRoleRuleBackwards((RoleRule)theRule, theMA, aKA);
                    if (!CollectionUtil.isEmptyOrNull(theBaseObjects)) {
                        Collection<BoundObject> theRuleObjects = this.getOrCreateSet(aRulesToObjectsMap, theRule);
                        theRuleObjects.addAll(theBaseObjects);
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.error("Failed to handle "+(isAdded?"new":"removed")+" meta attribute knowledge association: "+aKA.toString(), ex, this);
        }
    }

    private <U,V> Collection<V> getOrCreateSet(Map<U,Collection<V>> aMap, U aKey) {
        Collection<V> theResult = aMap.get(aKey);
        if (theResult == null) {
            theResult = new HashSet<>();
            aMap.put(aKey, theResult);
        }
        return theResult;
    }

    private Map<BoundRole, Set<BoundObject>> mergeValuesByRole(Map<RoleProvider, Collection<BoundObject>> aCollectionMap) {
    	Map<BoundRole, Set<BoundObject>> invalidObjectsByRole = new HashMap<>();

        Iterator<Map.Entry<RoleProvider, Collection<BoundObject>>> it = aCollectionMap.entrySet().iterator();
        for (Iterator<Map.Entry<RoleProvider, Collection<BoundObject>>> theIt = aCollectionMap.entrySet().iterator(); theIt.hasNext();) {
        	Map.Entry<RoleProvider, Collection<BoundObject>> theEntry = theIt.next();
        	BoundRole        theRole   = theEntry.getKey().getRole();
			Set<BoundObject> theValues = invalidObjectsByRole.get(theRole);
			if (theValues == null) {
				theValues = new HashSet<>();
				invalidObjectsByRole.put(theRole, theValues);
			}
			theValues.addAll(theEntry.getValue());
		}
        
        return invalidObjectsByRole;
    }

}
