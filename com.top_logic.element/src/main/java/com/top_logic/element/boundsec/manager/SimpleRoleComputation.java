/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link RoleComputation} computing values on the fly.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleRoleComputation extends RoleComputation {

	private final Person _person;

	/**
	 * Creates a new {@link SimpleRoleComputation}.
	 */
	public SimpleRoleComputation(Person person, StorageAccessManager accessManager) {
		super(accessManager);
		_person = person;
	}

	@Override
	public boolean hasRole(BoundObject boundObject, Collection<BoundedRole> someRoles) {
		try {
			return hasRole(boundObject, someRoles, getGroupIDs(_person));
		} catch (StorageException ex) {
			Logger.error("Unable to determine roles for person '" + _person.getName() + "'.", ex,
				SimpleRoleComputation.class);
			return false;
		}
	}

	@Override
	public Set<BoundRole> getRoles(BoundObject boundObject) {
		try {
			return new HashSet<>(findRolesInStorage(boundObject, getGroupIDs(_person)));
		} catch (StorageException ex) {
			Logger.error("Unable to determine roles for person '" + _person.getName()
				+ "'. No roles will be given to the person.", ex, SimpleRoleComputation.class);
			return Collections.emptySet();
		}
	}

	@Override
	public <T extends BoundObject> Collection<T> getAllowedBusinessObjects(Collection<BoundedRole> someRoles,
			Collection<T> someObjects) {
		try {
			return findAllowedBusinessObjects(someRoles, getGroupIDs(_person), someObjects);
		} catch (StorageException ex) {
			Logger.error("Unable to determine allowed for person '" + _person.getName() + "'.", ex,
				SimpleRoleComputation.class);
			return Collections.emptyList();
		}
	}

}

