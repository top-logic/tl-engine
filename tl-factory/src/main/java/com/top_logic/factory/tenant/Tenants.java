/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The tenants of the control application, their members and the projects they own.
 *
 * <p>
 * A {@link FactoryModel#TENANT tenant} holds its members in one reference per role (see
 * {@link FactoryModel#TENANT_MEMBERS}). A user is a member of one tenant at most, possibly in
 * several of its roles, see {@link #checkMembers(TLObject, Object)}. Which roles a member holds on
 * the tenant and its projects, and what these roles may do, is the access configuration of the
 * application (role rules of the <code>AccessManager</code>, grants of the
 * <code>SecurityConfigurationService</code>).
 * </p>
 */
public class Tenants {

	private Tenants() {
		// Static accessors only.
	}

	/**
	 * The tenants the given account is a member of, in any role.
	 *
	 * @param person
	 *        The account, {@code null} for none.
	 * @return The tenants; more than one only if the {@link #checkMembers(TLObject, Object) check
	 *         of the members} was bypassed.
	 */
	public static Set<TLObject> tenantsOf(Person person) {
		Set<TLObject> result = new LinkedHashSet<>();
		if (person == null) {
			return result;
		}
		TLClass type = FactoryModel.tenantType();
		for (String members : FactoryModel.TENANT_MEMBERS) {
			result.addAll(person.tReferers((TLReference) type.getPartOrFail(members)));
		}
		return result;
	}

	/**
	 * The tenant the given account is a member of.
	 *
	 * @param person
	 *        The account, {@code null} for none.
	 * @return The tenant, {@code null} if the account is a member of no tenant.
	 */
	public static TLObject tenantOf(Person person) {
		Set<TLObject> tenants = tenantsOf(person);
		return tenants.isEmpty() ? null : tenants.iterator().next();
	}

	/**
	 * The projects owned by the given tenant.
	 */
	public static List<TLObject> projectsOf(TLObject tenant) {
		TLReference reference = (TLReference) FactoryModel.projectType().getPartOrFail(FactoryModel.PROJECT_TENANT);
		return new ArrayList<>(tenant.tReferers(reference));
	}

	/**
	 * The projects the given account works with: all projects for an account that may read every
	 * project (an operator), the projects of its tenant for a member of a tenant, none otherwise.
	 *
	 * @see #mayReadAll(Person, TLClass)
	 */
	public static List<TLObject> projects(Person person) {
		TLClass projectType = FactoryModel.projectType();
		if (mayReadAll(person, projectType)) {
			return new ArrayList<>(MetaElementUtil.getAllInstancesOf(projectType, TLObject.class));
		}
		TLObject tenant = tenantOf(person);
		if (tenant == null) {
			return new ArrayList<>();
		}
		return projectsOf(tenant);
	}

	/**
	 * Whether the given account may read every object of the given type, independent of the
	 * object: an administrator, or an account holding a role on the security root that is granted
	 * reading the type.
	 *
	 * @see ModelAccessRights#getAccessibleTypes(Person, com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	public static boolean mayReadAll(Person person, TLClass type) {
		if (person == null) {
			return false;
		}
		return ModelAccessRights.getInstance().getAccessibleTypes(person, SimpleBoundCommandGroup.READ)
			.contains(type);
	}

	/**
	 * Checks that the given members of a tenant are members of no other tenant.
	 *
	 * @param tenant
	 *        The tenant, or the form object editing or creating one.
	 * @param members
	 *        The accounts to become members of the tenant in one of its roles: an account or a
	 *        collection of accounts.
	 * @return The message naming the first account that is a member of another tenant, and that
	 *         tenant; {@code null} if there is none.
	 */
	public static ResKey checkMembers(TLObject tenant, Object members) {
		TLObject self = tenant instanceof TLFormObjectBase form ? form.getEditedObject() : tenant;
		Collection<?> accounts = members instanceof Collection<?> collection ? collection
			: members == null ? List.of() : List.of(members);
		for (Object account : accounts) {
			if (!(account instanceof Person person)) {
				continue;
			}
			for (TLObject other : tenantsOf(person)) {
				if (!other.equals(self)) {
					return I18NConstants.ERROR_MEMBER_OF_OTHER_TENANT__USER_TENANT.fill(person, other);
				}
			}
		}
		return null;
	}

}
