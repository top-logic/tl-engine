/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.DefaultExternalUserMapping;
import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.AliasDefinition;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.provisioning.OidcIdentities;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@link ClientConfigurator.Config#getUserMapping() user mapping} of the generic OpenID
 * client of the configuration {@link #GENERIC_CONFIG}, with and without its environment variables.
 */
public class TestGenericClientUserMapping extends BasicTestCase {

	/** The configuration of the generic OpenID client. */
	private static final String GENERIC_CONFIG = "/WEB-INF/conf/pac4j-generic.config.xml";

	/** The name of the generic OpenID client. */
	private static final String CLIENT = "pac4j-generic";

	/** The alias switching the creation of accounts. */
	private static final String CREATE_ACCOUNTS_ALIAS = "%PAC_CREATE_ACCOUNTS%";

	/** The alias with the name of the group claim. */
	private static final String GROUP_CLAIM_ALIAS = "%PAC_GROUP_CLAIM%";

	/** The environment variable switching the creation of accounts. */
	private static final String CREATE_ACCOUNTS_VARIABLE = "tl_pac_create_accounts";

	/** The environment variable with the name of the group claim. */
	private static final String GROUP_CLAIM_VARIABLE = "tl_pac_group_claim";

	/** The group claim of the identities in this test. */
	private static final String GROUPS_CLAIM = "groups";

	/** A group name in the claim for which the application has no group. */
	private static final String UNKNOWN_GROUP = "GenericUnknownGroup";

	private final List<String> _accountNames = new ArrayList<>();

	private Group _developers;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction()) {
			_developers = Group.createGroup("GenericDevelopers");
			tx.commit();
		}
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
			_developers.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * The aliases of the user mapping are set by their environment variables and default to no
	 * account creation and no group claim.
	 */
	public void testAliases() {
		assertAlias(CREATE_ACCOUNTS_ALIAS, CREATE_ACCOUNTS_VARIABLE, "false");
		assertAlias(GROUP_CLAIM_ALIAS, GROUP_CLAIM_VARIABLE, "");

		Config config = genericMapping(Collections.emptyMap()).getConfig();
		assertFalse(config.getCreateAccounts());
		assertFalse(config.getLinkIdentities());
		assertEquals("", config.getGroupClaim());
		assertTrue(config.getGroupsByName());
	}

	/**
	 * Without variables, the login of an unknown name is refused as unknown account.
	 */
	public void testUnknownRefusedWithoutVariables() {
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());
		ExternalIdentity identity = identity("sub-unknown", "gUnknown", List.of());

		assertUnknown(mapping, identity);
		assertUnknown(DefaultExternalUserMapping.INSTANCE, identity);
		assertNull(Person.byName("gUnknown"));
		assertNull(OidcIdentities.findIdentity(kb(), CLIENT, "sub-unknown"));
	}

	/**
	 * Without variables, an existing account is found by name, is not bound to the identity, and
	 * keeps its names and e-mail address.
	 */
	public void testExistingFoundWithoutVariables() {
		Person existing = account("gExisting");
		setUserData(existing, "Erika", "Existing", "erika@example.com");
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());
		ExternalIdentity identity =
			identity("sub-existing", "gExisting", "Other", "Name", "other@example.com", List.of());

		assertSame(existing, mapping.findAccountForIdentity(identity));
		assertSame(existing, DefaultExternalUserMapping.INSTANCE.findAccountForIdentity(identity));

		assertTrue("No identity bound.", OidcIdentities.getIdentities(existing).isEmpty());
		assertEquals("Erika", existing.getUser().getFirstName());
		assertEquals("Existing", existing.getUser().getName());
		assertEquals("erika@example.com", existing.getUser().getEMail());
	}

	/**
	 * Without variables, a changed login name at the identity provider finds the account of the
	 * changed name.
	 */
	public void testChangedNameWithoutVariables() {
		Person before = account("gBefore");
		Person after = account("gAfter");
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());

		assertSame(before, mapping.findAccountForIdentity(identity("sub-change", "gBefore", List.of())));
		ExternalIdentity changed = identity("sub-change", "gAfter", List.of());
		assertSame(after, mapping.findAccountForIdentity(changed));
		assertSame(after, DefaultExternalUserMapping.INSTANCE.findAccountForIdentity(changed));
	}

	/**
	 * Without variables, an identity with another subject for the same name logs in, e.g. after
	 * the user has been created again at the identity provider.
	 */
	public void testNewSubjectWithoutVariables() {
		Person existing = account("gRecreated");
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());

		assertSame(existing, mapping.findAccountForIdentity(identity("sub-first", "gRecreated", List.of())));
		assertSame(existing, mapping.findAccountForIdentity(identity("sub-second", "gRecreated", List.of())));
		assertTrue(OidcIdentities.getIdentities(existing).isEmpty());
	}

	/**
	 * Without binding, an account that already carries the identity, e.g. one connected by its
	 * user, is found by it.
	 */
	public void testConnectedIdentityFoundWithoutBinding() {
		Person connected = account("gConnected");
		try (Transaction tx = kb().beginTransaction()) {
			OidcIdentities.addIdentity(connected, CLIENT, "sub-connected");
			tx.commit();
		}
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());

		assertSame(connected,
			mapping.findAccountForIdentity(identity("sub-connected", "gOtherName", List.of())));
	}

	/**
	 * An account deleted in the current transaction is refused as unknown account, like by
	 * {@link ExternalUserMapping#findAccount(String)}.
	 */
	public void testNotAliveRefused() {
		Person existing = account("gDeleted");
		OidcProvisioningUserMapping mapping = genericMapping(Collections.emptyMap());
		ExternalIdentity identity = identity("sub-deleted", "gDeleted", List.of());

		try (Transaction tx = kb().beginTransaction()) {
			existing.tDelete();
			assertFalse(existing.isAlive());

			assertUnknown(DefaultExternalUserMapping.INSTANCE, identity);
			assertUnknown(mapping, identity);
			// Rolled back.
		}
		assertTrue(existing.isAlive());
	}

	/**
	 * With creation of accounts, an unknown name gets an account with the names, the e-mail
	 * address and the groups of its claims, bound to the identity. An unknown group is ignored.
	 */
	public void testCreateAccounts() {
		OidcProvisioningUserMapping mapping = genericMapping(Map.of(
			CREATE_ACCOUNTS_ALIAS, "true",
			GROUP_CLAIM_ALIAS, GROUPS_CLAIM));

		Person account = login(mapping, identity("sub-created", "gCreated", "Carla", "Created", "carla@example.com",
			List.of(_developers.getName(), UNKNOWN_GROUP)));

		assertEquals("gCreated", account.getName());
		assertEquals("Carla", account.getUser().getFirstName());
		assertEquals("Created", account.getUser().getName());
		assertEquals("carla@example.com", account.getUser().getEMail());
		assertTrue(_developers.getDirectMembers().contains(account));
		assertNull(Group.getGroupByName(kb(), UNKNOWN_GROUP));
		assertNotNull("Created account bound.", OidcIdentities.getIdentity(account, CLIENT));
	}

	/**
	 * With the group claim alone, an existing account is bound to its identity, so that the group
	 * memberships the claim grants are revoked again. An unknown name is still refused.
	 */
	public void testGroupClaimWithoutCreation() {
		Person existing = account("gMember");
		OidcProvisioningUserMapping mapping = genericMapping(Map.of(GROUP_CLAIM_ALIAS, GROUPS_CLAIM));
		Config config = mapping.getConfig();
		assertFalse(config.getCreateAccounts());
		assertFalse(config.getLinkIdentities());

		assertSame(existing,
			mapping.findAccountForIdentity(identity("sub-member", "gMember", List.of(_developers.getName()))));
		assertNotNull("Account with synchronized groups bound.", OidcIdentities.getIdentity(existing, CLIENT));
		assertTrue(_developers.getDirectMembers().contains(existing));

		assertSame(existing, mapping.findAccountForIdentity(identity("sub-member", "gMember", List.of())));
		assertFalse("Membership revoked.", _developers.getDirectMembers().contains(existing));

		assertUnknown(mapping, identity("sub-stranger", "gStranger", List.of(_developers.getName())));
		assertNull(Person.byName("gStranger"));
	}

	/**
	 * By default, {@link OidcProvisioningUserMapping} binds an account found by name to the
	 * identity.
	 */
	public void testBindingByDefault() {
		Person existing = account("gDefault");
		OidcProvisioningUserMapping mapping = TestOidcProvisioningUserMapping.mapping();
		assertTrue(mapping.getConfig().getLinkIdentities());

		assertSame(existing, mapping.findAccountForIdentity(identity("sub-default", "gDefault", List.of())));
		assertNotNull("Account bound.", OidcIdentities.getIdentity(existing, CLIENT));
	}

	private static void assertAlias(String alias, String variable, String defaultValue) {
		AliasDefinition definition = AliasManager.getInstance().getDefinition(alias);
		assertNotNull("Alias not defined: " + alias, definition);
		assertEquals(List.of(variable), definition.getEnvironmentNames());
		assertEquals(defaultValue, definition.getDefaultValue());
	}

	private static void assertUnknown(ExternalUserMapping mapping, ExternalIdentity identity) {
		try {
			mapping.findAccountForIdentity(identity);
			fail("Login must be refused as unknown account: " + identity);
		} catch (UnknownAccountException ex) {
			assertEquals(identity.getName(), ex.getLoginName());
		}
	}

	/**
	 * The user mapping of the generic client, read from the application configuration extended by
	 * {@link #GENERIC_CONFIG} with the aliases
	 * of the application, the given ones replacing them.
	 */
	private static OidcProvisioningUserMapping genericMapping(Map<String, String> aliases) {
		VariableExpander expander = AliasManager.getInstance().getExpander().derive();
		AliasManager.putVariables(expander, aliases);

		List<BinaryContent> sources = new ArrayList<>(XMLProperties.Module.INSTANCE.config().getTypedConfigs());
		sources.add(FileManager.getInstance().getData(GENERIC_CONFIG));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestGenericClientUserMapping.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(ApplicationConfig.ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		try {
			ApplicationConfig.Config config = (ApplicationConfig.Config) new ConfigurationReader(context, descriptors)
				.setSources(sources)
				.setVariableExpander(expander)
				.read();
			context.checkErrors();
			Pac4jConfigFactory.Config<?> factoryConfig =
				(Pac4jConfigFactory.Config<?>) config.getServices().get(Pac4jConfigFactory.class).getInstance();
			ExternalUserMapping mapping =
				context.getInstance(factoryConfig.getClients().get(CLIENT).getUserMapping());
			context.checkErrors();
			return (OidcProvisioningUserMapping) mapping;
		} catch (Exception ex) {
			throw new AssertionError("Cannot read " + GENERIC_CONFIG + ".", ex);
		}
	}

	private Person login(ExternalUserMapping mapping, ExternalIdentity identity) {
		Person account = mapping.findAccountForIdentity(identity);
		_accountNames.add(account.getName());
		return account;
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accountNames.add(name);
		return account;
	}

	private static void setUserData(Person account, String firstName, String lastName, String eMail) {
		try (Transaction tx = kb().beginTransaction()) {
			account.getUser().setFirstName(firstName);
			account.getUser().setName(lastName);
			account.getUser().setEMail(eMail);
			tx.commit();
		}
	}

	private static ExternalIdentity identity(String subject, String userName, List<String> groups) {
		return identity(subject, userName, null, null, null, groups);
	}

	private static ExternalIdentity identity(String subject, String userName, String givenName, String familyName,
			String email, List<String> groups) {
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put(OidcProvisioningUserMapping.PREFERRED_USERNAME_CLAIM, userName);
		if (givenName != null) {
			claims.put(OidcProvisioningUserMapping.GIVEN_NAME_CLAIM, givenName);
		}
		if (familyName != null) {
			claims.put(OidcProvisioningUserMapping.FAMILY_NAME_CLAIM, familyName);
		}
		if (email != null) {
			claims.put(OidcProvisioningUserMapping.EMAIL_CLAIM, email);
		}
		claims.put(GROUPS_CLAIM, groups);
		return new ExternalIdentity(CLIENT, subject, userName, claims);
	}

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite, with a knowledge base holding the model of the external identities and the
	 * contacts of the accounts.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestGenericClientUserMapping.class),
				ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}
