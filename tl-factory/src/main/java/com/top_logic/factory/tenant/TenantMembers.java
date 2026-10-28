/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.tenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.project.FactoryAccess;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * The management of the tenants and their members: creating a tenant with its first
 * administrator, creating the account of a member, changing the role of a member and removing a
 * member with the account.
 *
 * <p>
 * The accounts of the members belong to their tenant: a member is created with a new account,
 * and removing a member deletes the account. Whoever may write a tenant (an administrator of the
 * tenant or an operator) manages its members, whoever may create a tenant (an operator) creates
 * tenants. These rights are checked for the current user; the creation and deletion of the
 * accounts and the change of the memberships then run without access checks, since they need
 * rights on the accounts and the security root that the administrator of a tenant does not hold.
 * The current user stays the author of the changes.
 * </p>
 *
 * <p>
 * Every operation commits its changes in a transaction of its own.
 * </p>
 *
 * @see Tenants
 */
public class TenantMembers {

	private TenantMembers() {
		// Static operations only.
	}

	/**
	 * The login data and the name of an account to create.
	 *
	 * @param login
	 *        The login name.
	 * @param firstName
	 *        The first name, {@code null} for none.
	 * @param lastName
	 *        The last name, {@code null} for none.
	 * @param email
	 *        The e-mail address, {@code null} for none.
	 * @param password
	 *        The initial password; ignored if the authentication device sets no passwords.
	 */
	public record NewAccount(String login, String firstName, String lastName, String email, String password) {
		// Pure data.
	}

	/**
	 * Creates a tenant with its first administrator.
	 *
	 * @param name
	 *        The name of the tenant, unique among the tenants.
	 * @param administrator
	 *        The account of the first administrator of the tenant.
	 * @return The new tenant.
	 * @throws TopLogicException
	 *         If the current user may not create a tenant, or the tenant or the account cannot be
	 *         created.
	 */
	public static TLObject createTenant(String name, NewAccount administrator) {
		Person user = TLContext.currentUser();
		if (!ModelAccessRights.getInstance().isAllowedCreate(user, FactoryModel.tenantType(), (TLObject) null)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TENANT_CREATION);
		}
		return modify(I18NConstants.CREATED_TENANT__TENANT.fill(name), () -> {
			TLObject tenant = ModelService.getInstance().getFactory().createObject(FactoryModel.tenantType());
			tenant.tUpdateByName(FactoryModel.TENANT_NAME, name);
			Person account = createAccount(administrator);
			setRole(tenant, account, TenantRole.ADMINISTRATOR);
			return tenant;
		});
	}

	/**
	 * Creates a member of a tenant with a new account.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param role
	 *        The role of the member in the tenant.
	 * @param data
	 *        The account to create.
	 * @return The account of the new member.
	 * @throws TopLogicException
	 *         If the current user may not manage the members of the tenant, or the account cannot
	 *         be created, e.g. because its login name is used already.
	 */
	public static Person createMember(TLObject tenant, TenantRole role, NewAccount data) {
		FactoryAccess.checkWrite(tenant);
		return modify(I18NConstants.CREATED_MEMBER__USER_TENANT.fill(data.login(), tenant), () -> {
			Person account = createAccount(data);
			setRole(tenant, account, role);
			return account;
		});
	}

	/**
	 * Changes the role of a member of a tenant.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param member
	 *        A member of the tenant.
	 * @param role
	 *        The new role of the member, which replaces all roles the member holds in the tenant.
	 * @throws TopLogicException
	 *         If the current user may not manage the members of the tenant, the account is no
	 *         member of it, or the tenant would be left without administrator.
	 */
	public static void changeRole(TLObject tenant, Person member, TenantRole role) {
		FactoryAccess.checkWrite(tenant);
		checkMember(tenant, member);
		if (role != TenantRole.ADMINISTRATOR) {
			checkOtherAdministrator(tenant, member);
		}
		modify(I18NConstants.CHANGED_ROLE__USER_TENANT.fill(member, tenant), () -> {
			setRole(tenant, member, role);
			return null;
		});
	}

	/**
	 * Removes a member from a tenant and deletes the account of the member.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param member
	 *        A member of the tenant, not the current user.
	 * @throws TopLogicException
	 *         If the current user may not manage the members of the tenant, the account is no
	 *         member of it, is the current user, or the tenant would be left without
	 *         administrator.
	 */
	public static void removeMember(TLObject tenant, Person member) {
		FactoryAccess.checkWrite(tenant);
		checkMember(tenant, member);
		if (member.equals(TLContext.currentUser())) {
			throw new TopLogicException(I18NConstants.ERROR_REMOVE_SELF);
		}
		checkOtherAdministrator(tenant, member);
		String login = member.getName();
		modify(I18NConstants.REMOVED_MEMBER__USER_TENANT.fill(login, tenant), () -> {
			setRole(tenant, member, null);
			Group group = member.getRepresentativeGroup();
			member.tDelete();
			if (group != null && group.tValid()) {
				group.tDelete();
			}
			return null;
		});
	}

	/**
	 * The members of a tenant in any of its roles.
	 */
	public static List<Person> members(TLObject tenant) {
		Set<Person> result = new LinkedHashSet<>();
		for (TenantRole role : TenantRole.values()) {
			for (Object member : membersOf(tenant, role)) {
				result.add((Person) member);
			}
		}
		return new ArrayList<>(result);
	}

	/**
	 * The role of a member in a tenant.
	 *
	 * @return The first of the roles the member holds in the tenant, in the order of
	 *         {@link TenantRole}; {@code null} if the account is no member of the tenant.
	 */
	public static TenantRole roleOf(TLObject tenant, Person member) {
		for (TenantRole role : TenantRole.values()) {
			if (membersOf(tenant, role).contains(member)) {
				return role;
			}
		}
		return null;
	}

	/**
	 * The e-mail address of an account, {@code null} for none or an account without user
	 * information.
	 */
	public static String email(Person account) {
		UserInterface user = account.getUser();
		return user == null ? null : user.getEMail();
	}

	private static Collection<?> membersOf(TLObject tenant, TenantRole role) {
		Object value = tenant.tValueByName(role.getReference());
		return value == null ? List.of() : (Collection<?>) value;
	}

	private static void checkMember(TLObject tenant, Person member) {
		if (roleOf(tenant, member) == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_MEMBER__USER_TENANT.fill(member, tenant));
		}
	}

	private static void checkOtherAdministrator(TLObject tenant, Person member) {
		Collection<?> administrators = membersOf(tenant, TenantRole.ADMINISTRATOR);
		if (administrators.size() == 1 && administrators.contains(member)) {
			throw new TopLogicException(I18NConstants.ERROR_LAST_ADMINISTRATOR__TENANT.fill(tenant));
		}
	}

	/**
	 * Makes the given account a member of the given tenant in exactly the given role.
	 *
	 * @param role
	 *        The role, {@code null} to remove the account from the tenant.
	 */
	private static void setRole(TLObject tenant, Person account, TenantRole role) {
		for (TenantRole other : TenantRole.values()) {
			Set<Object> members = new LinkedHashSet<>(membersOf(tenant, other));
			boolean changed = other == role ? members.add(account) : members.remove(account);
			if (changed) {
				tenant.tUpdateByName(other.getReference(), members);
			}
		}
	}

	/**
	 * Creates an account with the default authentication device of the application, the way the
	 * account administration does, and sets its initial password and name.
	 */
	private static Person createAccount(NewAccount data) {
		String login = data.login() == null ? null : data.login().trim();
		if (login == null || login.isEmpty()) {
			throw new TopLogicException(com.top_logic.layout.view.admin.I18NConstants.ERROR_MISSING_LOGIN);
		}
		AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
		boolean setsPassword = device != null && device.allowPwdChange();
		String password = data.password();
		if (setsPassword && (password == null || password.isEmpty())) {
			throw new TopLogicException(com.top_logic.layout.view.admin.I18NConstants.ERROR_MISSING_PASSWORD);
		}

		// Refuses an invalid login name and one used already.
		Person account = Person.create(kb(), login, device);
		if (setsPassword) {
			char[] chars = password.toCharArray();
			try {
				// Refuses a password violating the password policy.
				device.setPassword(account, chars);
			} finally {
				Arrays.fill(chars, (char) 0);
			}
		}
		UserInterface user = account.getUser();
		if (user != null) {
			if (!isEmpty(data.firstName())) {
				user.setFirstName(data.firstName().trim());
			}
			if (!isEmpty(data.lastName())) {
				user.setName(data.lastName().trim());
			}
			if (!isEmpty(data.email())) {
				user.setEMail(data.email().trim());
			}
		}
		return account;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * Runs the given modification without access checks in a transaction of its own, committed
	 * with the given message.
	 */
	private static <T> T modify(ResKey message, Supplier<T> modification) {
		return ThreadContext.<T, RuntimeException, RuntimeException> withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(message)) {
				T result = modification.get();
				tx.commit();
				return result;
			}
		});
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
