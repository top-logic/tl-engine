/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.selfservice.invitation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.security.selfservice.invitation.InvitationLink;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.InvitationState;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of the {@link Invitation} lifecycle through the operations of the {@link InvitationModule}.
 */
public class TestInvitationLifecycle extends BasicTestCase {

	private static final String PASSWORD = "Secret-Passw0rd";

	/** The variable defining the public address of the application, {@link AliasManager#HOST}. */
	private static final String HOST_VARIABLE = "tl_host";

	private final List<Invitation> _invitations = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Delete test invitations"))) {
			for (Invitation invitation : _invitations) {
				if (invitation.tValid()) {
					Person account = invitation.getAccount();
					invitation.tDelete();
					if (account != null && account.tValid()) {
						TestPerson.deletePersonAndUser(account);
					}
				}
			}
			tx.commit();
		}
		_invitations.clear();
		super.tearDown();
	}

	/** A created invitation is open and expires after the configured validity. */
	public void testCreate() {
		long before = System.currentTimeMillis();
		Invitation invitation = invite("created@example.com");
		long after = System.currentTimeMillis();

		assertEquals(InvitationState.OPEN_CLASSIFIER, invitation.getState().getName());
		assertTrue(InvitationModule.isOpen(invitation));
		assertNotNull(invitation.getCreatedAt());
		assertNull(invitation.getAccepted());
		assertNull(invitation.getAccount());
		assertEquals(MfaRequirement.OPTIONAL, invitation.getMfaRequirement());

		long validity = module().getConfig().getInvitationValidity();
		long expires = invitation.getExpires().getTime();
		assertTrue(expires >= before + validity);
		assertTrue(expires <= after + validity);
	}

	/** The token of an invitation encodes 32 random bytes. */
	public void testTokenHas32Bytes() {
		Invitation invitation = invite("token@example.com");

		byte[] token = HexFormat.of().parseHex(invitation.getId().replace("-", ""));
		assertEquals(32, token.length);
	}

	/** An expired invitation cannot be accepted, and no account is created. */
	public void testAcceptExpiredRefused() {
		String email = "expired@example.com";
		Invitation invitation = invite(email);
		expire(invitation);
		assertTrue(InvitationModule.isExpired(invitation));

		assertRefused(I18NConstants.ERROR_INVITATION_EXPIRED,
			() -> module().acceptInvitation(invitation, "Doe", "Jane", PASSWORD.toCharArray()));
		assertRefused(I18NConstants.ERROR_INVITATION_EXPIRED, () -> module().findOpenInvitation(invitation.getId()));

		assertNull(Person.byName(kb(), email));
		assertNull(invitation.getAccepted());
		assertNull(invitation.getAccount());
	}

	/**
	 * Accepting an open invitation creates the account on the configured device and records the
	 * acceptance.
	 */
	public void testAccept() {
		String email = "accepted@example.com";
		Invitation invitation = invite(email);

		Invitation found = module().findOpenInvitation(invitation.getId());
		assertSame(invitation, found);

		Person account = module().acceptInvitation(found, "Doe", "Jane", PASSWORD.toCharArray());

		assertSame(account, Person.byName(kb(), email));
		assertEquals(module().getConfig().getAuthenticationDevice(), account.getAuthenticationDeviceID());
		assertEquals(MfaRequirement.OPTIONAL, account.getMFARequirement());
		assertEquals("Doe", account.getUser().getName());
		assertEquals("Jane", account.getUser().getFirstName());
		assertEquals(email, account.getUser().getEMail());
		assertTrue(account.getAuthenticationDevice()
			.authentify(LoginCredentials.fromUserAndPassword(account, PASSWORD.toCharArray())));

		assertNotNull(invitation.getAccepted());
		assertSame(account, invitation.getAccount());
		assertTrue(InvitationModule.isAccepted(invitation));
		assertSame(invitation, module().findInvitation(invitation.getId()));
	}

	/** An accepted invitation cannot be accepted again, nor be renewed or sent. */
	public void testAcceptTwiceRefused() {
		Invitation invitation = invite("twice@example.com");
		module().acceptInvitation(invitation, "Doe", null, PASSWORD.toCharArray());

		assertRefused(I18NConstants.ERROR_INVITATION_ACCEPTED,
			() -> module().acceptInvitation(invitation, "Other", null, PASSWORD.toCharArray()));
		assertRefused(I18NConstants.ERROR_INVITATION_ACCEPTED, () -> module().findOpenInvitation(invitation.getId()));
		assertRefused(I18NConstants.ERROR_INVITATION_ACCEPTED, () -> module().renewInvitation(invitation));
		assertRefused(I18NConstants.ERROR_INVITATION_ACCEPTED, () -> module().sendInvitation(invitation));
	}

	/** A password violating the rules of the device creates no account. */
	public void testAcceptWithWeakPasswordRefused() {
		String email = "weak@example.com";
		Invitation invitation = invite(email);

		assertRefused(ValidationResult.TO_SHORT.messageKey(),
			() -> module().acceptInvitation(invitation, "Doe", null, "abc".toCharArray()));
		assertRefused(I18NConstants.ERROR_PASSWORD_EMPTY,
			() -> module().acceptInvitation(invitation, "Doe", null, new char[0]));

		assertNull(Person.byName(kb(), email));
		assertTrue(InvitationModule.isOpen(invitation));
	}

	/** Renewing an expired invitation gives it a new token and a new validity. */
	public void testRenew() {
		Invitation invitation = invite("renew@example.com");
		String oldToken = invitation.getId();
		expire(invitation);
		Date oldExpires = invitation.getExpires();

		module().renewInvitation(invitation);

		assertFalse(oldToken.equals(invitation.getId()));
		assertTrue(invitation.getExpires().after(oldExpires));
		assertTrue(invitation.getExpires().after(new Date()));
		assertTrue(InvitationModule.isOpen(invitation));
		assertNull(module().findInvitation(oldToken));
		assertSame(invitation, module().findOpenInvitation(invitation.getId()));
	}

	/** A token without invitation is reported as unknown. */
	public void testUnknownToken() {
		assertNull(module().findInvitation("unknown"));
		assertRefused(I18NConstants.ERROR_UNKNOWN_INVITATION, () -> module().findOpenInvitation("unknown"));
	}

	/**
	 * Only open invitations count for the check that an address is invited only once, which is
	 * checked when the invitation is stored.
	 */
	public void testUniqueOnlyAmongOpenInvitations() {
		String email = "unique@example.com";
		Invitation first = invite(email);
		assertRefused(com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED, () -> invite(email));

		expire(first);
		Invitation second = invite(email);
		assertTrue(InvitationModule.isOpen(second));

		try (Transaction tx = kb().beginTransaction(ResKey.text("Mark invitation accepted"))) {
			second.setAccepted(new Date());
			tx.commit();
		}
		assertTrue(InvitationModule.isAccepted(second));
		Invitation third = invite(email);
		assertTrue(InvitationModule.isOpen(third));
	}

	/** The link of an invitation leads to the configured page with the token as parameter. */
	public void testLink() {
		Invitation invitation = invite("link@example.com");

		String path = link().path(invitation.getId());
		assertTrue(path, path.endsWith("?" + InvitationLink.INVITATION_PARAMETER + "=" + invitation.getId()));
	}

	/** The link starts with the configured public address of the application. */
	public void testLinkWithPublicAddress() throws Exception {
		Invitation invitation = invite("public@example.com");

		withPublicAddress("https://app.example.com/", () -> {
			String url = module().invitationLink(invitation);
			String contextPath = StringServices.nonNull(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
			assertEquals("https://app.example.com" + contextPath + link().path(invitation.getId()), url);
		});
	}

	/** Without public address and without a current request, no link can be built. */
	public void testLinkWithoutPublicAddressAndRequest() throws Exception {
		Invitation invitation = invite("nohost@example.com");

		withPublicAddress("", () -> {
			InteractionContext interaction = ThreadContextManager.getInteraction();
			assertTrue("Test runs within a request.", interaction == null || interaction.asRequest() == null);
			assertRefused(I18NConstants.ERROR_NO_PUBLIC_ADDRESS, () -> module().invitationLink(invitation));
		});
	}

	/**
	 * Runs the given check with the given public address of the application
	 * ({@link AliasManager#HOST}, from the variable {@link #HOST_VARIABLE}).
	 */
	private static void withPublicAddress(String host, Runnable check) throws Exception {
		String before = System.getProperty(HOST_VARIABLE);
		System.setProperty(HOST_VARIABLE, host);
		try {
			AliasManager.getInstance().reload();
			check.run();
		} finally {
			if (before == null) {
				System.clearProperty(HOST_VARIABLE);
			} else {
				System.setProperty(HOST_VARIABLE, before);
			}
			AliasManager.getInstance().reload();
		}
	}

	private InvitationLink<?> link() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(module().getConfig().getInvitationLink());
	}

	private Invitation invite(String email) {
		Invitation invitation = module().createInvitation(email, MfaRequirement.OPTIONAL);
		_invitations.add(invitation);
		return invitation;
	}

	private void expire(Invitation invitation) {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Expire invitation"))) {
			invitation.setExpires(new Date(System.currentTimeMillis() - 1000));
			tx.commit();
		}
	}

	private static void assertRefused(ResKey expected, Runnable action) {
		try {
			action.run();
			fail("Expected failure: " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static InvitationModule module() {
		return InvitationModule.getInstance();
	}

	/**
	 * The suite, with a knowledge base holding the self-service model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInvitationLifecycle.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				InvitationModule.Module.INSTANCE));
	}

}
