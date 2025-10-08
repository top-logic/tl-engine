/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Session local computation for roles assigned to a single person.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class RoleComputation {

	private final SecurityStorage _storage;

	private final StorageAccessManager _accessManager;

	/**
	 * Creates a new {@link RoleComputation}.
	 * 
	 * @param accessManager
	 *        The {@link StorageAccessManager} creating this computation.
	 */
	public RoleComputation(StorageAccessManager accessManager) {
		_accessManager = accessManager;
		_storage = accessManager.getSecurityStorage();
	}

	/**
	 * Check, if the current user has at least one of the given roles on the given business object.
	 * 
	 * @param boundObject
	 *        The business object to check the roles for, must not be <code>null</code>.
	 * @param someRoles
	 *        The roles to be checked, may be <code>null</code>.
	 * 
	 * @return <code>true</code> when the used has at least one of the roles.
	 */
	public abstract boolean hasRole(BoundObject boundObject, Collection<? extends BoundRole> someRoles);

	/**
	 * Checks whether one of the given groups has one of the given roles on one of the given
	 * business objects.
	 */
	protected boolean hasRole(BoundObject boundObject, Collection<? extends BoundRole> someRoles,
			Collection<TLID> groupIDs)
			throws StorageException {
		if (CollectionUtil.isEmptyOrNull(someRoles)) {
			return false;
		}
		Collection<TLID> roleIDs = idSet(someRoles);
		return _storage.hasRole(groupIDs, getSecurityParentIDs(boundObject), roleIDs);
	}

	/**
	 * Return the roles, the user has on the given business object.
	 * 
	 * @param boundObject
	 *        The business object to check the roles for, must not be <code>null</code>.
	 * 
	 * @return The roles, the user has on the given business object, never <code>null</code>.
	 */
	public abstract Set<BoundRole> getRoles(BoundObject boundObject);

	/**
	 * Find the roles the given groups on the given business object.
	 */
	protected List<? extends BoundRole> findRolesInStorage(BoundObject boundObject, List<TLID> groupIDs)
			throws StorageException {
		return _storage.getRoles(groupIDs, getSecurityParentIDs(boundObject));
	}

	/**
	 * Checks the given collection and returns only these objects with the given roles.
	 * 
	 * @param someRoles
	 *        A collection of BoundRoles.
	 * @param someObjects
	 *        The collection of BoundObjects to check.
	 * @return A collection containing only the allowed BoundObjects of the given collection
	 *         someObjects, may be empty but not <code>null</code>.
	 */
	public abstract <T extends BoundObject> Collection<T> getAllowedBusinessObjects(
			Collection<? extends BoundRole> someRoles,
			Collection<T> someObjects);

	/**
	 * Checks the given collection and returns only these objects, on which the given groups has one
	 * of the given roles.
	 */
	protected <T extends BoundObject> Collection<T> findAllowedBusinessObjects(
			Collection<? extends BoundRole> someRoles,
			List<TLID> groupIDs, Collection<T> someObjects) throws StorageException {
		List<T> theResult = new ArrayList<>(someObjects.size());
		Collection<TLID> theRoleIDs = idSet(someRoles);
		Collection<TLID> allowedObjectIDs;
		if (dontUseInStatementQuery(someRoles, someObjects)) {
			allowedObjectIDs = _storage.getBusinessObjectIds(groupIDs, theRoleIDs);
		} else {
			List<TLID> businessObjectIDs = _accessManager.getSecurityParentIDs(someObjects);
			allowedObjectIDs = _storage.getBusinessObjectIds(groupIDs, theRoleIDs, businessObjectIDs);
		}
		Collection<?> checkIDs = CollectionUtil.toContainsChecker(someObjects.size(), allowedObjectIDs);
		for (T bo : someObjects) {
			BoundObject potentiallyAllowedObject = bo;
			while (potentiallyAllowedObject != null) {
				if (checkIDs.contains(potentiallyAllowedObject.getID())) {
					theResult.add(bo);
					break;
				}
				potentiallyAllowedObject = potentiallyAllowedObject.getSecurityParent();
			}
		}
		return theResult;
	}

	private static Collection<TLID> idSet(Collection<? extends BoundRole> someRoles) {
		ArrayList<TLID> result = new ArrayList<>();
		for (BoundRole role : someRoles) {
			if (role instanceof BoundedRole withId) {
				result.add(withId.tIdLocal());
			}
		}
		return result;
	}

	/**
	 * Normally the {@link #findAllowedBusinessObjects(Collection, List, Collection)} method uses
	 * the in-statement-query of the security storage. In some cases this query could be slow (e.g.
	 * if someObject has many elements). If this method returns <code>true</code>, another query
	 * without in-statement will be used instead.
	 * 
	 * @return <code>true</code>, if no in-statement-query should be used, <code>false</code>
	 *         otherwise.
	 */
	protected boolean dontUseInStatementQuery(Collection<? extends BoundRole> someRoles,
			Collection<? extends BoundObject> someObjects) {
		return someObjects.size() > 256; // This number was selected arbitrary...
	}

	/**
	 * Gets the security parents of an object.
	 * 
	 * @param boundObject
	 *        The object to get the security parents from.
	 * 
	 * @return IDs of the security parents of the given object.
	 */
	protected List<TLID> getSecurityParentIDs(BoundObject boundObject) {
		return _accessManager.getSecurityParentIDs(boundObject);
    }

	/**
	 * Gets the groups the given person is member of.
	 * 
	 * @param person
	 *        The person to get the groups from.
	 * 
	 * @return IDs of the groups the given person is member of.
	 */
	protected List<TLID> getGroupIDs(Person person) {
		return _accessManager.getGroupIDs(person);
	}

}
