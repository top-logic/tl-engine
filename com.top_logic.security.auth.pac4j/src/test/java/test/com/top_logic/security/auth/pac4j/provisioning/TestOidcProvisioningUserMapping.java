/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.provisioning.I18NConstants;
import com.top_logic.security.auth.pac4j.provisioning.NameConflict;
import com.top_logic.security.auth.pac4j.provisioning.OidcIdentities;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.util.model.ModelService;

/**
 * Tests the lookup, linking and creation of accounts by {@link OidcProvisioningUserMapping}.
 */
public class TestOidcProvisioningUserMapping extends BasicTestCase {

	/** The pac4j client of the logins. */
	private static final String CLIENT = "test-idp";

	private final List<String> _accountNames = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		for (String name : _accountNames) {
			Person account = Person.byName(name);
			if (account != null) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accountNames.clear();
		super.tearDown();
	}

	/**
	 * An unknown identity gets an account with the names and e-mail address of its claims.
	 */
	public void testCreateAccount() {
		OidcProvisioningUserMapping mapping = mapping(Config.CREATE_ACCOUNTS, true);
		ExternalIdentity identity = identity("sub-create", "pCreate", "Paula", "Creator", "paula@example.com", true);

		Person account = login(mapping, identity);

		assertEquals("pCreate", account.getName());
		assertSame(account, Person.byName("pCreate"));
		assertEquals("Paula", account.getUser().getFirstName());
		assertEquals("Creator", account.getUser().getName());
		assertEquals("paula@example.com", account.getUser().getEMail());
		assertNull("Created account must have no password.", account.getAuthenticationDevice());
		OidcIdentity oidcIdentity = OidcIdentities.getIdentity(account, CLIENT);
		assertNotNull(oidcIdentity);
		assertEquals("sub-create", oidcIdentity.getSubject());
		assertSame(oidcIdentity,
			OidcIdentities.findIdentity(PersistencyLayer.getKnowledgeBase(), CLIENT, "sub-create"));
	}

	/**
	 * A second login finds the account by its subject, even after the user has changed the login
	 * name at the identity provider.
	 */
	public void testFindBySubjectAfterRename() {
		OidcProvisioningUserMapping mapping = mapping(Config.CREATE_ACCOUNTS, true);
		Person account = login(mapping, identity("sub-rename", "pBefore", "Ben", "Before", null, false));

		Person again = login(mapping, identity("sub-rename", "pAfter", "Ben", "After", null, false));

		assertSame(account, again);
		assertEquals("pBefore", again.getName());
		assertNull("No account created for the changed name.", Person.byName("pAfter"));
		assertEquals(1, OidcIdentities.getIdentities(account).size());
	}

	/**
	 * Without account creation, an unknown identity is refused as unknown account.
	 */
	public void testCreationOff() {
		OidcProvisioningUserMapping mapping = mapping();
		try {
			login(mapping, identity("sub-off", "pOff", null, null, null, false));
			fail("Login of an unknown identity must be refused.");
		} catch (UnknownAccountException ex) {
			assertEquals("pOff", ex.getLoginName());
		}
		assertNull(Person.byName("pOff"));
	}

	/**
	 * An account requiring a confirmed e-mail address is not created for an unconfirmed one.
	 */
	public void testUnverifiedEmailRefused() {
		OidcProvisioningUserMapping mapping =
			mapping(Config.CREATE_ACCOUNTS, true, Config.REQUIRE_VERIFIED_EMAIL, true);
		assertRefused(I18NConstants.ERROR_EMAIL_NOT_VERIFIED, mapping,
			identity("sub-unverified", "pUnverified", null, null, "unverified@example.com", false));
		assertNull(Person.byName("pUnverified"));
		assertRefused(I18NConstants.ERROR_NO_EMAIL, mapping,
			identity("sub-unverified", "pUnverified", null, null, null, true));
		assertNull(Person.byName("pUnverified"));

		Person account = login(mapping,
			identity("sub-unverified", "pUnverified", null, null, "unverified@example.com", true));
		assertEquals("pUnverified", account.getName());
	}

	/**
	 * An account is not created for an e-mail address of a domain that is not allowed.
	 */
	public void testForeignDomainRefused() {
		OidcProvisioningUserMapping mapping =
			mapping(Config.CREATE_ACCOUNTS, true, Config.ALLOWED_EMAIL_DOMAINS, List.of("example.com", "Other.org"));
		assertRefused(I18NConstants.ERROR_EMAIL_DOMAIN_NOT_ALLOWED__DOMAIN.fill("foreign.net"), mapping,
			identity("sub-domain", "pDomain", null, null, "p@foreign.net", true));
		assertNull(Person.byName("pDomain"));
		assertRefused(I18NConstants.ERROR_NO_EMAIL, mapping,
			identity("sub-domain", "pDomain", null, null, null, true));
		assertNull(Person.byName("pDomain"));

		Person account = login(mapping, identity("sub-domain", "pDomain", null, null, "p@OTHER.org", true));
		assertEquals("pDomain", account.getName());
	}

	/**
	 * An identity is linked to the existing account of its login name.
	 */
	public void testLinkByName() {
		Person existing = account("pLinked");
		OidcProvisioningUserMapping mapping = mapping();

		Person account = login(mapping, identity("sub-linked", "pLinked", null, null, null, false));

		assertSame(existing, account);
		assertEquals("sub-linked", OidcIdentities.getIdentity(account, CLIENT).getSubject());

		Person again = login(mapping, identity("sub-linked", "pLinkedRenamed", null, null, null, false));
		assertSame(existing, again);
	}

	/**
	 * Without linking by name, the existing account of the login name is not used; creating an
	 * account under that name is refused.
	 */
	public void testNoLinkByNameConflictRefused() {
		Person existing = account("pTaken");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_NAME, false, Config.CREATE_ACCOUNTS, true);

		assertRefused(I18NConstants.ERROR_NAME_TAKEN__NAME.fill("pTaken"), mapping,
			identity("sub-taken", "pTaken", null, null, null, false));

		assertTrue(OidcIdentities.getIdentities(existing).isEmpty());
		assertNull(OidcIdentities.findIdentity(PersistencyLayer.getKnowledgeBase(), CLIENT, "sub-taken"));
	}

	/**
	 * Without linking by name and without account creation, the existing account of the login
	 * name is not used, the identity is refused as unknown.
	 */
	public void testNoLinkByNameNoCreation() {
		Person existing = account("pTakenOff");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_NAME, false);

		try {
			login(mapping, identity("sub-taken-off", "pTakenOff", null, null, null, false));
			fail("Login of an unlinked identity must be refused.");
		} catch (UnknownAccountException ex) {
			// expected
		}
		assertTrue(OidcIdentities.getIdentities(existing).isEmpty());
	}

	/**
	 * With {@link NameConflict#RENAME}, an account whose name is taken is created under the name
	 * extended by the client and a counter.
	 */
	public void testNameConflictRename() {
		account("pDouble");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_NAME, false, Config.CREATE_ACCOUNTS, true,
			Config.NAME_CONFLICT, NameConflict.RENAME);

		Person first = login(mapping, identity("sub-double-1", "pDouble", null, null, null, false));
		assertEquals("pDouble@" + CLIENT, first.getName());

		Person second = login(mapping, identity("sub-double-2", "pDouble", null, null, null, false));
		assertEquals("pDouble@" + CLIENT + "-2", second.getName());

		Person third = login(mapping, identity("sub-double-3", "pDouble", null, null, null, false));
		assertEquals("pDouble@" + CLIENT + "-3", third.getName());

		assertSame(second, login(mapping, identity("sub-double-2", "pDouble", null, null, null, false)));
	}

	/**
	 * An account that already carries an identity of the client is not linked to another identity
	 * of the same client.
	 */
	public void testOtherIdentityOfClientRefused() {
		OidcProvisioningUserMapping mapping = mapping(Config.CREATE_ACCOUNTS, true);
		Person account = login(mapping, identity("sub-first", "pFirst", null, null, null, false));

		assertRefused(I18NConstants.ERROR_OTHER_IDENTITY__NAME.fill("pFirst"), mapping,
			identity("sub-second", "pFirst", null, null, null, false));

		assertEquals(1, OidcIdentities.getIdentities(account).size());
		assertNull(OidcIdentities.findIdentity(PersistencyLayer.getKnowledgeBase(), CLIENT, "sub-second"));

		// Another client may be linked.
		Person other = login(mapping,
			new ExternalIdentity("other-idp", "sub-second", "pFirst", Map.of("preferred_username", "pFirst")));
		assertSame(account, other);
		assertEquals(2, OidcIdentities.getIdentities(account).size());
	}

	/**
	 * With linking by e-mail, an identity is linked to the single account with its confirmed
	 * e-mail address.
	 */
	public void testLinkByEmail() {
		Person existing = account("pMail");
		setEMail(existing, "mail.user@example.com");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_EMAIL, true, Config.LINK_BY_NAME, false);

		try {
			login(mapping, identity("sub-mail", "otherName", null, null, "Mail.User@example.com", false));
			fail("An unconfirmed e-mail address must not link the account.");
		} catch (UnknownAccountException ex) {
			// expected
		}
		assertTrue(OidcIdentities.getIdentities(existing).isEmpty());

		Person account = login(mapping, identity("sub-mail", "otherName", null, null, "Mail.User@example.com", true));
		assertSame(existing, account);
		assertEquals("sub-mail", OidcIdentities.getIdentity(account, CLIENT).getSubject());
	}

	/**
	 * Linking by e-mail does not apply when several accounts have the address.
	 */
	public void testLinkByEmailAmbiguous() {
		setEMail(account("pMailA"), "shared@example.com");
		setEMail(account("pMailB"), "shared@example.com");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_EMAIL, true, Config.LINK_BY_NAME, false);

		try {
			login(mapping, identity("sub-shared", "pShared", null, null, "shared@example.com", true));
			fail("An ambiguous e-mail address must not link an account.");
		} catch (UnknownAccountException ex) {
			// expected
		}
	}

	/**
	 * Without linking by e-mail, the confirmed e-mail address of an account does not link it.
	 */
	public void testNoLinkByEmailByDefault() {
		setEMail(account("pMailDefault"), "default@example.com");
		OidcProvisioningUserMapping mapping = mapping(Config.LINK_BY_NAME, false);

		try {
			login(mapping, identity("sub-default", "pOtherDefault", null, null, "default@example.com", true));
			fail("Linking by e-mail is off by default.");
		} catch (UnknownAccountException ex) {
			// expected
		}
	}

	private Person login(OidcProvisioningUserMapping mapping, ExternalIdentity identity) {
		Person account = mapping.findAccountForIdentity(identity);
		_accountNames.add(account.getName());
		return account;
	}

	private void assertRefused(ResKey expectedReason, OidcProvisioningUserMapping mapping,
			ExternalIdentity identity) {
		try {
			Person account = mapping.findAccountForIdentity(identity);
			_accountNames.add(account.getName());
			fail("Login must be refused: " + identity);
		} catch (UnknownAccountException ex) {
			throw ex;
		} catch (LoginDeniedException ex) {
			ResKey reason = ex.getReason();
			assertNotNull("No reason for the user: " + ex.getMessage(), reason);
			assertEquals(expectedReason.plain().getKey(), reason.plain().getKey());
			assertEquals(expectedReason.toString(), reason.toString());
		}
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accountNames.add(name);
		return account;
	}

	private static void setEMail(Person account, String eMail) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			account.getUser().setEMail(eMail);
			tx.commit();
		}
	}

	private static ExternalIdentity identity(String subject, String userName, String givenName, String familyName,
			String email, boolean emailVerified) {
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
		claims.put(OidcProvisioningUserMapping.EMAIL_VERIFIED_CLAIM, emailVerified);
		return new ExternalIdentity(CLIENT, subject, userName, claims);
	}

	/**
	 * A mapping with the given configuration options, given as alternating property names and
	 * values.
	 */
	static OidcProvisioningUserMapping mapping(Object... options) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		for (int n = 0; n < options.length; n += 2) {
			set(config, (String) options[n], options[n + 1]);
		}
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/**
	 * The suite, with a knowledge base holding the model of the external identities and the
	 * contacts of the accounts.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestOidcProvisioningUserMapping.class),
				ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}
