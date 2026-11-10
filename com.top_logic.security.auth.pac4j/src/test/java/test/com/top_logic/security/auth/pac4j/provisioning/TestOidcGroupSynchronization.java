/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import static test.com.top_logic.security.auth.pac4j.provisioning.TestOidcProvisioningUserMapping.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.provisioning.OidcIdentities;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Tests the synchronization of the group memberships by {@link OidcProvisioningUserMapping}.
 */
public class TestOidcGroupSynchronization extends BasicTestCase {

	private static final String CLIENT = "group-idp";

	private static final String GROUPS_CLAIM = "groups";

	private final List<String> _accountNames = new ArrayList<>();

	private final List<Group> _groups = new ArrayList<>();

	private Group _developers;

	private Group _operations;

	private Group _guests;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_developers = group("Developers");
		_operations = group("Operations");
		_guests = group("Guests");
	}

	@Override
	protected void tearDown() throws Exception {
		for (String name : _accountNames) {
			Person account = Person.byName(name);
			if (account != null) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accountNames.clear();
		try (Transaction tx = kb().beginTransaction()) {
			for (Group group : _groups) {
				group.tDelete();
			}
			tx.commit();
		}
		_groups.clear();
		super.tearDown();
	}

	/**
	 * A created account becomes a member of the mapped groups of its claim; values without
	 * mapping are ignored.
	 */
	public void testMappedGroupAdded() {
		OidcProvisioningUserMapping mapping = groupMapping();

		Person account = login(mapping, identity("sub-add", "gAdd", List.of("devs", "unmapped")));

		assertMembers(account, _developers);
		assertGranted(account, _developers);
	}

	/**
	 * A group the claim no longer yields is removed, a further one added.
	 */
	public void testLostGroupRemoved() {
		OidcProvisioningUserMapping mapping = groupMapping();
		Person account = login(mapping, identity("sub-lost", "gLost", List.of("devs", "ops")));
		assertMembers(account, _developers, _operations);

		login(mapping, identity("sub-lost", "gLost", List.of("ops")));

		assertMembers(account, _operations);
		assertGranted(account, _operations);
	}

	/**
	 * A membership assigned by hand stays, also when the claim yielded the same group for a while.
	 */
	public void testHandAssignedStays() {
		Person account = account("gHand");
		inTransaction(() -> {
			_operations.addMember(account);
			_guests.addMember(account);
		});
		OidcProvisioningUserMapping mapping = groupMapping();

		login(mapping, identity("sub-hand", "gHand", List.of("devs", "ops")));
		assertMembers(account, _developers, _operations, _guests);
		assertGranted(account, _developers);

		login(mapping, identity("sub-hand", "gHand", List.of()));
		assertMembers(account, _operations, _guests);
		assertGranted(account);
	}

	/**
	 * Without group claim, the account gets the default groups, which it loses once the claim
	 * names other groups.
	 */
	public void testDefaultGroup() {
		OidcProvisioningUserMapping mapping = groupMapping(Config.DEFAULT_GROUPS, List.of("Guests"));

		Person account = login(mapping, identity("sub-default", "gDefault", null));
		assertMembers(account, _guests);

		login(mapping, identity("sub-default", "gDefault", List.of("devs")));
		assertMembers(account, _developers);

		login(mapping, identity("sub-default", "gDefault", null));
		assertMembers(account, _guests);
	}

	/**
	 * With {@link Config#getGroupsByName()}, an unmapped value names the existing group of the
	 * same name.
	 */
	public void testGroupsByName() {
		OidcProvisioningUserMapping mapping = groupMapping(Config.GROUPS_BY_NAME, true);

		Person account = login(mapping, identity("sub-name", "gName", List.of("Operations", "NoSuchGroup", "devs")));

		assertMembers(account, _developers, _operations);
	}

	/**
	 * Without groups-by-name, a value with the name of a group does not make the account its member.
	 */
	public void testNoGroupsByNameByDefault() {
		OidcProvisioningUserMapping mapping = groupMapping();

		Person account = login(mapping, identity("sub-noname", "gNoName", List.of("Operations")));

		assertMembers(account);
	}

	/**
	 * A single value instead of a list is accepted.
	 */
	public void testSingleValueClaim() {
		OidcProvisioningUserMapping mapping = groupMapping();

		Person account = login(mapping, identity("sub-single", "gSingle", "ops"));

		assertMembers(account, _operations);
	}

	/**
	 * A yielded group removed by hand is assigned again at the next login: the identity provider
	 * is authoritative for the groups it yields.
	 */
	public void testRemovedByHandAddedAgain() {
		OidcProvisioningUserMapping mapping = groupMapping();
		Person account = login(mapping, identity("sub-again", "gAgain", List.of("devs")));
		inTransaction(() -> _developers.removeMember(account));
		assertMembers(account);

		login(mapping, identity("sub-again", "gAgain", List.of("devs")));

		assertMembers(account, _developers);
	}

	/**
	 * A group granted by the identities of two clients stays while one of them still yields it.
	 */
	public void testGrantedByTwoClients() {
		OidcProvisioningUserMapping mapping = groupMapping();
		Person account = login(mapping, identity("sub-two", "gTwo", List.of("devs")));
		login(mapping, identity("other-client", "sub-two", "gTwo", List.of("devs")));
		assertMembers(account, _developers);

		login(mapping, identity("sub-two", "gTwo", List.of()));
		assertMembers(account, _developers);

		login(mapping, identity("other-client", "sub-two", "gTwo", List.of()));
		assertMembers(account);
	}

	/**
	 * A login by subject that changes nothing commits no transaction.
	 */
	public void testNoTransactionWithoutChange() {
		OidcProvisioningUserMapping mapping = groupMapping();
		Person account = login(mapping, identity("sub-quiet", "gQuiet", List.of("devs")));
		long revision = kb().getHistoryManager().getLastRevision();

		assertSame(account, login(mapping, identity("sub-quiet", "gQuiet", List.of("devs", "unmapped"))));
		assertEquals(revision, kb().getHistoryManager().getLastRevision());

		login(mapping, identity("sub-quiet", "gQuiet", List.of("ops")));
		assertTrue(kb().getHistoryManager().getLastRevision() > revision);
		assertMembers(account, _operations);
	}

	private void assertMembers(Person account, Group... expected) {
		Set<Group> actual = new HashSet<>();
		for (Group group : _groups) {
			if (group.getDirectMembers().contains(account)) {
				actual.add(group);
			}
		}
		assertEquals(Set.of(expected), actual);
	}

	private void assertGranted(Person account, Group... expected) {
		OidcIdentity identity = OidcIdentities.getIdentity(account, CLIENT);
		assertEquals(Set.of(expected), Set.copyOf(identity.getGrantedGroups()));
	}

	private OidcProvisioningUserMapping groupMapping(Object... options) {
		Object[] all = new Object[options.length + 6];
		all[0] = Config.CREATE_ACCOUNTS;
		all[1] = true;
		all[2] = Config.GROUP_CLAIM;
		all[3] = GROUPS_CLAIM;
		all[4] = Config.GROUP_MAPPING;
		all[5] = Map.of("devs", Set.of("Developers"), "ops", Set.of("Operations"));
		System.arraycopy(options, 0, all, 6, options.length);
		return mapping(all);
	}

	private Person login(OidcProvisioningUserMapping mapping, ExternalIdentity identity) {
		Person account = mapping.findAccountForIdentity(identity);
		_accountNames.add(account.getName());
		return account;
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accountNames.add(name);
		return account;
	}

	private Group group(String name) {
		Group[] result = new Group[1];
		inTransaction(() -> result[0] = Group.createGroup(name));
		_groups.add(result[0]);
		return result[0];
	}

	private static ExternalIdentity identity(String subject, String userName, Object groups) {
		return identity(CLIENT, subject, userName, groups);
	}

	private static ExternalIdentity identity(String client, String subject, String userName, Object groups) {
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put(OidcProvisioningUserMapping.PREFERRED_USERNAME_CLAIM, userName);
		if (groups != null) {
			claims.put(GROUPS_CLAIM, groups);
		}
		return new ExternalIdentity(client, subject, userName, claims);
	}

	private static void inTransaction(Runnable change) {
		try (Transaction tx = kb().beginTransaction()) {
			change.run();
			tx.commit();
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite, with a knowledge base holding the model of the external identities.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestOidcGroupSynchronization.class),
				ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}
