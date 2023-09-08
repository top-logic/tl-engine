/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link RoleComputation} caching the groups of the person and the roles.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersonRoleCache extends RoleComputation {

	private final Person _person;

	private List<TLID> _groupIDs;

	private final ConcurrentHashMap<TLID, Set<BoundRole>> cache = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Set<BoundRole>, Set<BoundRole>> roleCache =
		new ConcurrentHashMap<>();

	/**
	 * Creates a new {@link PersonRoleCache}.
	 */
	public PersonRoleCache(Person person, StorageAccessManager accessManager) {
		super(accessManager);
		_person = person;
		_groupIDs = getGroupIDs(_person);
	}

	@Override
	public boolean hasRole(BoundObject boundObject, Collection<BoundedRole> someRoles) {
		if (CollectionUtil.isEmptyOrNull(someRoles)) {
			return false;
		}
		return CollectionUtil.containsAny(someRoles, getRoles(boundObject));
	}

	@Override
	public Set<BoundRole> getRoles(BoundObject boundObject) {
    	TLID id = boundObject.getID();
		Set<BoundRole> cachedRoles = this.cache.get(id);
		if (cachedRoles == null) {
			Set<BoundRole> roles;
			try {
				roles = Collections.unmodifiableSet(new HashSet<>(findRolesInStorage(boundObject, _groupIDs)));
			} catch (StorageException ex) {
				Logger.error("Unable to determine roles for person '" + _person.getName()
					+ "'. No roles will be given to the person.", ex, PersonRoleCache.class);
				roles = Collections.emptySet();
			}
			Set<BoundRole> theRoles = roleCache.get(roles);
			if (theRoles == null) {
				// Cache the roles too.
				theRoles = MapUtil.putIfAbsent(roleCache, roles, roles);
			}
			cachedRoles = MapUtil.putIfAbsent(cache, id, theRoles);
		}
		return cachedRoles;
	}

	@Override
	public <T extends BoundObject> Collection<T> getAllowedBusinessObjects(Collection<BoundedRole> someRoles,
			Collection<T> someObjects) {
		try {
			return findAllowedBusinessObjects(someRoles, _groupIDs, someObjects);
		} catch (StorageException ex) {
			Logger.error("Unable to determine allowed for person '" + _person.getName() + "'.", ex,
				PersonRoleCache.class);
			return Collections.emptyList();
		}
	}

}

