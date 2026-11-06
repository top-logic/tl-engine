/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Synchronization of the group memberships of an account with the groups an
 * {@link ExternalIdentity} yields.
 *
 * <p>
 * The groups an identity yields are the groups its claim {@link Config#getGroupClaim()} names
 * through {@link Config#getGroupMapping()} (and with {@link Config#getGroupsByName()} the groups
 * of the same name), or the {@link Config#getDefaultGroups()} while the claim is missing.
 * </p>
 *
 * <p>
 * The {@link OidcIdentity#getGrantedGroups() granted groups} of the {@link OidcIdentity} record
 * which direct memberships the synchronization has created. A yielded group the account is not a
 * direct member of is added and recorded, also when it was removed by hand: the identity provider
 * is authoritative for the groups it yields. A membership another identity of the account has
 * granted is recorded as well. A recorded group no longer yielded is removed, unless another
 * identity of the account has granted it as well. A membership the account had before the
 * identity yielded the group is not recorded and therefore never removed: memberships assigned by
 * hand are not touched.
 * </p>
 */
class GroupSynchronization {

	private final String _groupClaim;

	private final Map<String, Set<String>> _mapping;

	private final boolean _groupsByName;

	private final List<String> _defaultGroups;

	/**
	 * Creates a {@link GroupSynchronization} from the configuration of the mapping.
	 */
	GroupSynchronization(Config config) {
		_groupClaim = StringServices.nonEmpty(config.getGroupClaim());
		_mapping = config.getGroupMapping();
		_groupsByName = config.getGroupsByName();
		_defaultGroups = config.getDefaultGroups().stream()
			.map(String::strip)
			.filter(name -> !name.isEmpty())
			.toList();
	}

	/**
	 * Whether group memberships are synchronized at all.
	 */
	boolean isActive() {
		return _groupClaim != null || !_defaultGroups.isEmpty();
	}

	/**
	 * Whether {@link #synchronize(KnowledgeBase, Person, OidcIdentity, ExternalIdentity)} would
	 * change something.
	 */
	boolean needsUpdate(KnowledgeBase kb, Person account, OidcIdentity stored, ExternalIdentity identity) {
		if (!isActive()) {
			return false;
		}
		Set<Group> yielded = yieldedGroups(kb, identity);
		Set<? extends Group> granted = stored.getGrantedGroups();
		for (Group group : yielded) {
			if (!isDirectMember(group, account)) {
				return true;
			}
			if (!granted.contains(group) && grantedByOtherIdentity(account, stored, group)) {
				return true;
			}
		}
		for (Group group : granted) {
			if (!yielded.contains(group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Brings the group memberships of the given account up to date with the given identity.
	 *
	 * <p>
	 * Must be called in a transaction.
	 * </p>
	 *
	 * @param account
	 *        The account to update.
	 * @param stored
	 *        The {@link OidcIdentity} of the account for the given identity.
	 * @param identity
	 *        The identity of the login.
	 */
	void synchronize(KnowledgeBase kb, Person account, OidcIdentity stored, ExternalIdentity identity) {
		if (!isActive()) {
			return;
		}
		Set<Group> yielded = yieldedGroups(kb, identity);
		Set<Group> granted = stored.getGrantedGroupsModifiable();

		for (Group group : new ArrayList<>(granted)) {
			if (yielded.contains(group)) {
				continue;
			}
			granted.remove(group);
			if (!grantedByOtherIdentity(account, stored, group)) {
				group.removeMember(account);
			}
		}

		for (Group group : yielded) {
			if (isDirectMember(group, account)) {
				if (!granted.contains(group) && grantedByOtherIdentity(account, stored, group)) {
					// Shared with the other identity, so that the membership stays while one of them
					// yields the group.
					granted.add(group);
				}
				continue;
			}
			group.addMember(account);
			if (isDirectMember(group, account)) {
				granted.add(group);
			}
		}
	}

	private static boolean grantedByOtherIdentity(Person account, OidcIdentity stored, Group group) {
		for (OidcIdentity other : OidcIdentities.getIdentities(account)) {
			if (other != stored && other.getGrantedGroups().contains(group)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDirectMember(Group group, Person account) {
		return group.getDirectMembers().contains(account);
	}

	/**
	 * The existing groups the given identity yields.
	 */
	Set<Group> yieldedGroups(KnowledgeBase kb, ExternalIdentity identity) {
		Object claim = _groupClaim == null ? null : identity.getClaim(_groupClaim);
		Collection<String> groupNames;
		if (claim == null) {
			groupNames = _defaultGroups;
		} else {
			groupNames = new LinkedHashSet<>();
			for (String value : claimValues(claim)) {
				Set<String> mapped = _mapping.get(value);
				if (mapped != null) {
					groupNames.addAll(mapped);
				} else if (_groupsByName) {
					groupNames.add(value);
				}
			}
		}

		Set<Group> result = new HashSet<>();
		for (String groupName : groupNames) {
			Group group = Group.getGroupByName(kb, groupName.strip());
			if (group != null) {
				result.add(group);
			} else if (_defaultGroups.contains(groupName) || isMappedGroup(groupName)) {
				Logger.warn("Group '" + groupName + "' of the external login does not exist, ignored for "
					+ identity + ".", GroupSynchronization.class);
			}
		}
		return result;
	}

	private boolean isMappedGroup(String groupName) {
		for (Set<String> mapped : _mapping.values()) {
			if (mapped.contains(groupName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The single values of a claim: the elements of a list, otherwise the value itself.
	 */
	private static Collection<String> claimValues(Object claim) {
		if (claim instanceof Collection<?> collection) {
			List<String> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				addValue(result, element);
			}
			return result;
		}
		if (claim instanceof Object[] array) {
			List<String> result = new ArrayList<>(array.length);
			for (Object element : array) {
				addValue(result, element);
			}
			return result;
		}
		List<String> result = new ArrayList<>(1);
		addValue(result, claim);
		return result.isEmpty() ? Collections.emptyList() : result;
	}

	private static void addValue(List<String> result, Object element) {
		if (element == null) {
			return;
		}
		String value = element.toString().strip();
		if (!value.isEmpty()) {
			result.add(value);
		}
	}

}
