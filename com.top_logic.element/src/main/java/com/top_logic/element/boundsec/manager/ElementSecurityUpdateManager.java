/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import com.top_logic.element.boundsec.manager.rule.BaseObjects;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cs.TLObjectChangeSet;
import com.top_logic.model.cs.TLObjectCreation;
import com.top_logic.model.cs.TLObjectDeletion;
import com.top_logic.model.cs.TLObjectUpdate;
import com.top_logic.model.util.TLModelUtil;
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

	public synchronized void handleSecurityUpdate(TLObjectChangeSet change, CommitHandler aHandler) {

		KnowledgeBase kb = change.kb();
		List<TLObjectCreation> creations = change.creations();
		List<TLObjectDeletion> deletions = change.deletions();
		List<TLObjectUpdate> updates = change.updates();
		if (creations.isEmpty() && deletions.isEmpty() && updates.isEmpty()) {
			return;
		}
		
		Map<TLID, KnowledgeItem> someRemoved = deletions.stream().map(TLObjectDeletion::object)
			.collect(Collectors.toMap(object -> object.tIdLocal(), object -> object.tHandle()));

		// Reset temporary
		Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap = new HashMap<>();
		Map<RoleProvider, Set<BoundObject>> rulesToDeletedObjectsMap = new HashMap<>();

		/* Map holding new role assignments. The value for a {@link BoundedRole} are the objects for
		 * which a new {@link BoundedRole#ROLE_ASSIGNMENT_TYPE role assignment} is created. */
		Map<BoundRole, Set<Object>> newRoleAssignments = new HashMap<>();
		/* Map holding deleted role assignments. The value for a {@link BoundedRole} are the objects
		 * for which a {@link BoundedRole#ROLE_ASSIGNMENT_TYPE role assignment} was deleted. */
		Map<BoundRole, Set<Object>> deletedRoleAssignments = new HashMap<>();
		/* Collection of all objects for which a {@link BoundedRole#ROLE_ASSIGNMENT_TYPE role
		 * assignment} was created or deleted. */
		Set<BoundObject> invalidObjects = new HashSet<>();
		/* All {@link RoleProvider} for which no invalid objects could be determined. The security
		 * storage for these rules must be rebuild. */
		Set<RoleProvider> invalidRules = new HashSet<>();

		securityStorage.begin(aHandler);
		
		Map<TLObject, Map<TLStructuredTypePart, Supplier<?>>> added = new HashMap<>();
		Set<TLObject> addedDirectRoles = new HashSet<>();
		Map<TLObject, Map<TLStructuredTypePart, Supplier<?>>> removed = new HashMap<>();
		Set<TLObject> removedDirectRoles = new HashSet<>();
		if (!creations.isEmpty()) {
			for (TLObjectCreation creation : creations) {
				TLObject createdObject = creation.object();
				TLStructuredType objType = createdObject.tType();
				if (BoundedRole.ROLE_ASSIGNMENT_TYPE.equals(TLModelUtil.qualifiedName(objType))) {
					addedDirectRoles.add(createdObject);
				} else {
					objType.getAllParts().stream()
						.forEach(part -> {
							put(added, createdObject, part,	() -> createdObject.tValue(part));
						});
				}
			}
		}
		if (!deletions.isEmpty()) {
			kb.withoutModifications(() -> {
				for (TLObjectDeletion deletion : deletions) {
					TLObject deletedObject = deletion.object();

					TLStructuredType objType = deletedObject.tType();
					if (BoundedRole.ROLE_ASSIGNMENT_TYPE.equals(TLModelUtil.qualifiedName(objType))) {
						removedDirectRoles.add(deletedObject);
					} else {
						objType.getAllParts().stream()
							.forEach(part -> {
								put(removed, deletedObject, part, () -> deletedObject.tValue(part));
							});
					}
				}
				return null;
			});
		}
		if (!updates.isEmpty()) {
			/* The oldValues and newValues of an TLObjectUpdate may not contain the same parts. */
			HashSet<TLStructuredTypePart> processedParts = new HashSet<>();

			for (TLObjectUpdate update : updates) {
				TLObject updatedObject = update.object();
				TLStructuredType objType = updatedObject.tType();
				if (BoundedRole.ROLE_ASSIGNMENT_TYPE.equals(TLModelUtil.qualifiedName(objType))) {
					/* For simplicity: Role assignment is added to remove and to add. */
					removedDirectRoles.add(updatedObject);
					addedDirectRoles.add(updatedObject);
				} else {
					processedParts.clear();
					Map<TLStructuredTypePart, Object> newValues = update.newValues();
					for (Entry<TLStructuredTypePart, Object> oldPartValue : update.oldValues().entrySet()) {
						TLStructuredTypePart part = oldPartValue.getKey();
						Object oldValue = oldPartValue.getValue();
						Object newValue = newValues.get(part);
						if (part instanceof TLReference reference) {
							/* Check whether the change is actually an collection add or collection
							 * remove. */
							if (newValue == null) {
								if (!newValues.containsKey(reference)) {
									// no new value. Maybe part deleted?
									put(removed, updatedObject, reference, () -> oldValue);
								} else {
									put(removed, updatedObject, reference, () -> oldValue);
								}
							} else {
								if (oldValue == null) {
									put(added, updatedObject, reference, () -> newValue);
								} else if (oldValue instanceof Collection oldColValue) {
									if (newValue instanceof Collection newColValue) {
										HashSet<?> addedValues = new HashSet<>(newColValue);
										addedValues.removeAll(oldColValue);
										if (!addedValues.isEmpty()) {
											put(added, updatedObject, reference, () -> addedValues);
										}
										HashSet<?> removedValues = new HashSet<>(oldColValue);
										removedValues.removeAll(newColValue);
										if (!removedValues.isEmpty()) {
											put(removed, updatedObject, reference, () -> removedValues);
										}
									} else {
										/* Strange situation: multiplicity of attribute changed in
										 * this commit. */
										put(removed, updatedObject, reference, () -> oldValue);
										put(added, updatedObject, reference, () -> newValue);
									}
								} else {
									put(removed, updatedObject, reference, () -> oldValue);
									put(added, updatedObject, reference, () -> newValue);
								}
							}
						} else {
							put(removed, updatedObject, part, () -> oldValue);
							put(added, updatedObject, part, () -> newValue);
						}
						processedParts.add(part);
					}
					for (Entry<TLStructuredTypePart, Object> partValue : newValues.entrySet()) {
						TLStructuredTypePart part = partValue.getKey();
						if (processedParts.contains(part)) {
							// new value for part already processed.
							continue;
						}
						// register as change
						Object newValue = partValue.getValue();
						put(added, updatedObject, part, () -> newValue);
					}
				}
			}

		}
		for (TLObject addedDirectRole : addedDirectRoles) {
			handleRoleAssignment(addedDirectRole, newRoleAssignments, invalidObjects, true);
		}
		for (Entry<TLObject, Map<TLStructuredTypePart, Supplier<?>>> e : added.entrySet()) {
			TLObject object = e.getKey();
			e.getValue().entrySet()
				.forEach(entry -> handleReference(object, entry.getKey(), entry.getValue(),
					rulesToObjectsMap, invalidRules, true));
		}
		kb.withoutModifications(() -> {
			for (TLObject removedDirectRole : removedDirectRoles) {
				handleRoleAssignment(removedDirectRole, deletedRoleAssignments, invalidObjects, false);
			}
			for (Entry<TLObject, Map<TLStructuredTypePart, Supplier<?>>> e : removed.entrySet()) {
				TLObject object = e.getKey();
				e.getValue().entrySet()
					.forEach(entry -> handleReference(object, entry.getKey(), entry.getValue(),
						rulesToDeletedObjectsMap, invalidRules, false));
			}
			return null;
		});

		handleInheritance(kb, someRemoved.values(), newRoleAssignments, deletedRoleAssignments, rulesToObjectsMap,
			rulesToDeletedObjectsMap, invalidRules);
		handleNewObjects(creations, rulesToObjectsMap, invalidRules);

		getLogHandler().logSecurityUpdate(change, rulesToObjectsMap, invalidObjects);

		Map<BoundRole, Set<BoundObject>> invalidObjectsByRole = mergeValuesByRole(rulesToObjectsMap);
		updateSecurityStorage(someRemoved, invalidObjects, invalidObjectsByRole, rulesToObjectsMap, invalidRules);
	}

		private static void put(Map<TLObject, Map<TLStructuredTypePart, Supplier<?>>> map, TLObject object, TLStructuredTypePart ref,
			Supplier<?> value) {
		map.computeIfAbsent(object, unused -> new HashMap<>()).put(ref, value);
	}

	/**
	 * Consider all rules for the concrete type: It is not enough to consider only the rules for all
	 * attributes of the type, because there may be inheritance rules which inherit a role without
	 * attribute; e.g. inherit a role from a root object to all instances of a certain type.
	 */
	private void handleNewObjects(List<TLObjectCreation> creations,
			Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap, Set<RoleProvider> invalidRules) {
		Map<TLClass, Collection<RoleProvider>> meRules = accessManager.getResolvedMERules();

		for (TLObjectCreation creation : creations) {
			TLObject createdObject = creation.object();
			TLStructuredType objType = createdObject.tType();
			if (BoundedRole.ROLE_ASSIGNMENT_TYPE.equals(TLModelUtil.qualifiedName(objType))) {
				continue;
			} else {
				Collection<RoleProvider> rulesForType = meRules.getOrDefault(objType, Collections.emptyList());
				for (RoleProvider rule : rulesForType) {
					if (invalidRules.contains(rule)) {
						// Rule is completely rebuild.
						continue;
					}
					add(rulesToObjectsMap, rule, (BoundObject) createdObject);
				}
			}
		}
	}

	private void updateSecurityStorage(Map<TLID, KnowledgeItem> someRemoved,
			Collection<BoundObject> invalidObjects, Map<BoundRole, Set<BoundObject>> invalidObjectsByRole,
			Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap, Set<RoleProvider> invalidRules) {
		long start = System.currentTimeMillis();
		try {
			// Set invalid objects
			if (accessManager instanceof StorageAccessManager sam) {
				sam.setInvalidObjects(invalidObjects, invalidObjectsByRole, invalidRules);
			}
			try {
				try { // remove security entries of removed objects
					securityStorage.removeObjects(someRemoved);
				} catch (StorageException ex) {
					Logger.error("Failed to remove security entries of removed objects.", ex, this);
				}

				try { // update security entries of changed objects
					securityStorage.updateSecurity(rulesToObjectsMap, invalidRules);
				} catch (StorageException ex) {
					Logger.error("Failed to update security entries of changed objects.", ex, this);
				}
			} finally {
				if (accessManager instanceof StorageAccessManager sam) {
					sam.removeInvalidObjects();
				}
			}
		} finally {
			long delta = System.currentTimeMillis() - start;
			if (delta > 3000) {
				Logger.warn("Incremental security update needed " + DebugHelper.getTime(delta),
					ElementSecurityUpdateManager.class);
			}
		}
	}

	private void handleReference(TLObject baseObject, TLStructuredTypePart part,
			Supplier<?> partValue, Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap,
			Set<RoleProvider> invalidRules, boolean isAdded) {
		try {
			for (Iterator<RoleProvider> theIt = accessManager.getRules(part).iterator(); theIt.hasNext();) {
				RoleProvider theRule = theIt.next();
				if (invalidRules.contains(theRule)) {
					/* Rule must be completely recomputed. Remove potentially previously added
					 * elements. */
					rulesToObjectsMap.remove(theRule);
					continue;
				}
				if (!(theRule instanceof RoleRule)) {
					continue;
				}

				BaseObjects<Set<BoundObject>> baseObjects =
					ElementAccessHelper.navigateRoleRuleBackwards((RoleRule) theRule, baseObject, part, partValue);
				if (baseObjects.isAll()) {
					invalidRules.add(theRule);
				} else {
					Set<BoundObject> theBaseObjects = baseObjects.get();
					if (!CollectionUtil.isEmptyOrNull(theBaseObjects)) {
						addAll(rulesToObjectsMap, theRule, theBaseObjects);
					}
				}
			}
		} catch (Exception ex) {
			Logger.error("Failed to handle " + (isAdded ? "new" : "removed") + " reference value change.",
				ex, ElementSecurityUpdateManager.class);
		}

	}

	private void handleRoleAssignment(TLObject roleAssignment, Map<BoundRole, Set<Object>> roleAssignments,
			Set<BoundObject> invalidObjects, boolean isAdded) {
		BoundRole role =
			((KnowledgeItem) roleAssignment.tValueByName(BoundedRole.ATTRIBUTE_ROLE)).getWrapper();
		BoundObject businessObject =
			((KnowledgeItem) roleAssignment.tValueByName(BoundedRole.ATTRIBUTE_OBJECT))
				.getWrapper();

		add(roleAssignments, role, businessObject);
		invalidObjects.add(businessObject);

		// update security storage with direct haseRole associations
		try {
			if (isAdded) {
				securityStorage.insert(roleAssignment.tHandle());
			} else {
				securityStorage.remove(roleAssignment.tHandle());
			}
		} catch (StorageException ex) {
			Logger.warn("Could not update role assignment in security storage.", ex,
				ElementSecurityUpdateManager.class);
		}

	}

    /**
	 * This method handles inheritance.
	 */
	private void handleInheritance(KnowledgeBase kb, final Collection<KnowledgeItem> removed,
			Map<BoundRole, Set<Object>> newRoleAssignments,
			Map<BoundRole, Set<Object>> deletedRoleAssignments,
			Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap,
			Map<RoleProvider, Set<BoundObject>> rulesToDeletedObjectsMap, Set<RoleProvider> invalidRules) {

		/* For each role we have a set of affected objects */
		for (Entry<BoundRole, Set<Object>> entry : newRoleAssignments.entrySet()) {
			addInheritedRules(rulesToObjectsMap, entry.getKey(), entry.getValue(), Collections.emptySet(),
				invalidRules);
		}

		kb.withoutModifications(new Computation<Void>() {

			@Override
			public Void run() {
				for (Entry<BoundRole, Set<Object>> entry : deletedRoleAssignments.entrySet()) {
					addInheritedRules(rulesToObjectsMap, entry.getKey(), entry.getValue(), removed, invalidRules);
				}
				for (Entry<RoleProvider, Set<BoundObject>> entry : rulesToDeletedObjectsMap.entrySet()) {
					addInheritedRules(rulesToObjectsMap, entry.getKey().getRole(), entry.getValue(), removed,
						invalidRules);
				}
				return null;
			}
		});

		Map<RoleProvider, Set<BoundObject>> theRulesToObjectsToBeConsidered = rulesToObjectsMap;

        int i = 0;
        // repeat until no more rules are to be considered
        while ( (! theRulesToObjectsToBeConsidered.isEmpty()) && (i < 50) ) {

            i++;

            // initially there are no more rules to be considered
			Map<RoleProvider, Set<BoundObject>> someMoreRulesToObjects = new HashMap<>();

			for (Entry<RoleProvider, Set<BoundObject>> entry : theRulesToObjectsToBeConsidered.entrySet()) {
				addInheritedRules(someMoreRulesToObjects, entry.getKey().getRole(), entry.getValue(),
					Collections.emptySet(), invalidRules);
			}
            // finally we add all the new entries to the original "rules to be considered" map
            // in order to return the enriched map to the calling method
			mergeMaps(someMoreRulesToObjects, rulesToObjectsMap);
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
		mergeMaps(rulesToDeletedObjectsMap, rulesToObjectsMap);

    }

	/**
	 * Finds for a {@link BoundedRole} the inheritance rules with that role as source rule, and
	 * computes the objects that must be evaluated on the inheritance rule when the objects inherits
	 * the rule from one of the given target objects.
	 * 
	 * @param out
	 *        Map to store {@link RoleProvider} together with the base objects. The rules must be
	 *        reevaluated on the base objects.
	 * @param role
	 *        The role which is assigned to <code>objects</code>.
	 * @param objects
	 *        The objects to which <code>role</code> is assigned.
	 * @param removed
	 *        All removed items. Only elements that are not contained in this collection must be
	 *        stored in <code>out</code>.
	 * @param invalidRules
	 *        Set of {@link RoleProvider} for which no base objects could be determined. A complete
	 *        rebuild for these rules must be performed.
	 */
	private void addInheritedRules(Map<RoleProvider, Set<BoundObject>> out, BoundRole role,
			Collection<?> objects, Collection<KnowledgeItem> removed, Set<RoleProvider> invalidRules) {

		/* We take all inheritance rules that declare the given role as source role. */
		for (RoleProvider inheritRule : accessManager.getRulesWithSourceRole(role, Type.inheritance)) {
			/* For each inheritance rule we determine the affected base objects starting from
			 * the objects affected by the given role assignment. All these objects are added to
			 * the new "rules to be considered" map. */
			if (invalidRules.contains(inheritRule)) {
				continue;
			}
			for (Object object : objects) {
				BaseObjects<Set<BoundObject>> baseObjectsObj = inheritRule.getBaseObjects(object);
				if (baseObjectsObj.isAll()) {
					invalidRules.add(inheritRule);
					// inherited rule is completely invalid
					break;
				}
				Set<BoundObject> baseObjects = baseObjectsObj.get();

				if (CollectionUtil.isEmptyOrNull(baseObjects)) {
					continue;
				}
				if (removed.isEmpty()) {
					addAll(out, inheritRule, baseObjects);
				} else {
					Iterator<BoundObject> iterator = baseObjects.iterator();
					BoundObject notDeleted = null;
					while (iterator.hasNext()) {
						BoundObject affectedObject = iterator.next();
						if (removed.contains(affectedObject.tHandle())) {
							// Skip elements that are deleted
							continue;
						}
						notDeleted = affectedObject;
						break;
					}
					if (notDeleted == null) {
						// all affected are also deleted
						continue;
					}
					Collection<BoundObject> inheritedRuleObjects = getOrCreateSet(out, inheritRule);
					inheritedRuleObjects.add(notDeleted);
					while (iterator.hasNext()) {
						BoundObject affectedObject = iterator.next();
						if (removed.contains(affectedObject.tHandle())) {
							// Skip elements that are deleted
							continue;
						}
						inheritedRuleObjects.add(affectedObject);
					}
				}
		    }
		}
		/* For rebuild roles no objects are stored in "out". These rules are handled explicit. */
		out.keySet().removeAll(invalidRules);
	}

	private void mergeMaps(
			Map<RoleProvider, Set<BoundObject>> source,
			Map<RoleProvider, Set<BoundObject>> into) {
		for (Map.Entry<RoleProvider, Set<BoundObject>> entry : source.entrySet()) {
			RoleProvider rule = entry.getKey();
			Set<BoundObject> objects = entry.getValue();
			if (!CollectionUtil.isEmptyOrNull(objects)) {
				addAll(into, rule, objects);
		    }
		}
	}

	private Map<BoundRole, Set<BoundObject>> mergeValuesByRole(Map<RoleProvider, Set<BoundObject>> rulesToObjectsMap) {
		Map<BoundRole, Set<BoundObject>> invalidObjectsByRole = new HashMap<>();

		for (Map.Entry<RoleProvider, Set<BoundObject>> entry : rulesToObjectsMap.entrySet()) {
			BoundRole theRole = entry.getKey().getRole();
			addAll(invalidObjectsByRole, theRole, entry.getValue());
		}

		return invalidObjectsByRole;
	}

	private static <U, V> void add(Map<U, Set<V>> map, U key, V value) {
		getOrCreateSet(map, key).add(value);
	}

	private static <U, V> void addAll(Map<U, Set<V>> map, U key, Collection<? extends V> values) {
		getOrCreateSet(map, key).addAll(values);
	}

	private static <U, V> Set<V> getOrCreateSet(Map<U, Set<V>> aMap, U aKey) {
		Set<V> theResult = aMap.get(aKey);
        if (theResult == null) {
            theResult = new HashSet<>();
            aMap.put(aKey, theResult);
        }
        return theResult;
    }

}
