/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.selfservice.invitation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.mail.MailText;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.provider.CurrentUserLanguageDefault;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Test of the texts and the language of the mails sent by the {@link InvitationModule}.
 *
 * <p>
 * The tests use the configured texts of the {@link InvitationModule} with mail scripts that answer
 * the texts they would send instead of sending a mail.
 * </p>
 */
public class TestInvitationMails extends BasicTestCase {

	private static final String PASSWORD = "Secret-Passw0rd";

	private static final String PUBLIC_ADDRESS = "https://app.example.com/";

	private static final String CODE = "123456";

	/**
	 * Invitation script answering the filled subject and the filled content.
	 */
	private static final String INVITATION_SCRIPT =
		"invitation -> applicationName -> link -> subject -> body -> "
			+ "list($subject.fill($applicationName).localize(), $body.fill($applicationName, $link).localize())";

	/**
	 * Invitation script declaring only the first three parameters, answering the link.
	 */
	private static final String INVITATION_SCRIPT_WITHOUT_TEXTS = "invitation -> applicationName -> link -> $link";

	/**
	 * Verification script answering whether the filled subject is the text passed as code.
	 */
	private static final String VERIFICATION_SCRIPT =
		"invitation -> applicationName -> code -> subject -> body -> $subject.fill($applicationName).localize() == $code";

	/**
	 * Reset script answering the address, the filled subject and the filled content.
	 */
	private static final String RESET_SCRIPT =
		"email -> applicationName -> code -> subject -> body -> "
			+ "list($email, $subject.fill($applicationName).localize(), $body.fill($applicationName, $code).localize())";

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

	/** A created invitation has the language of the current user. */
	public void testCreatedInvitationHasCurrentUserLanguage() {
		Invitation invitation = invite("created-language@example.com");

		assertNotNull(invitation.getLanguage());
		assertEquals(CurrentUserLanguageDefault.INSTANCE.currentUserLanguage(), invitation.getLanguage());
	}

	/**
	 * The invitation script gets the configured texts unfilled and runs in the language of the
	 * invitation.
	 */
	public void testInvitationMailInInvitationLanguage() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT, VERIFICATION_SCRIPT, RESET_SCRIPT);
		MailText text = module.getConfig().getInvitationText();

		Invitation invitation = invite("invitation-language@example.com");
		List<Object> results = new ArrayList<>();
		for (Locale language : supportedLanguages()) {
			setLanguage(invitation, language);
			TestInvitationLifecycle.withPublicAddress(PUBLIC_ADDRESS, () -> {
				String link = module.invitationLink(invitation);
				String title = title(language);
				Object sent = inCurrentLocale(otherLanguage(language), () -> module.sendInvitation(invitation));
				assertEquals(
					Arrays.asList(localize(language, ResKey.message(text.getSubject(), title)),
						localize(language, ResKey.message(text.getBody(), title, link))),
					sent);
				results.add(sent);
			});
		}
		assertEquals("Mails differ in the supported languages.", results.size(), results.stream().distinct().count());
	}

	/** An invitation without language is written in the default language of the application. */
	public void testInvitationMailWithoutLanguage() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT, VERIFICATION_SCRIPT, RESET_SCRIPT);
		MailText text = module.getConfig().getInvitationText();
		Locale defaultLanguage = ResourcesModule.getInstance().getDefaultLocale();

		Invitation invitation = invite("invitation-default@example.com");
		setLanguage(invitation, null);
		TestInvitationLifecycle.withPublicAddress(PUBLIC_ADDRESS, () -> {
			String link = module.invitationLink(invitation);
			String title = title(defaultLanguage);
			Object sent = inCurrentLocale(otherLanguage(defaultLanguage), () -> module.sendInvitation(invitation));
			assertEquals(
				Arrays.asList(localize(defaultLanguage, ResKey.message(text.getSubject(), title)),
					localize(defaultLanguage, ResKey.message(text.getBody(), title, link))),
				sent);
		});
	}

	/** A script declaring only the first three parameters ignores the texts. */
	public void testScriptWithoutTexts() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT_WITHOUT_TEXTS, VERIFICATION_SCRIPT, RESET_SCRIPT);

		Invitation invitation = invite("three-arguments@example.com");
		TestInvitationLifecycle.withPublicAddress(PUBLIC_ADDRESS,
			() -> assertEquals(module.invitationLink(invitation), module.sendInvitation(invitation)));
	}

	/** The verification mail is written in the language of the invitation. */
	public void testVerificationMailInInvitationLanguage() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT, VERIFICATION_SCRIPT, RESET_SCRIPT);
		ResKey subject = module.getConfig().getVerificationText().getSubject();

		Invitation invitation = invite("verification-language@example.com");
		for (Locale language : supportedLanguages()) {
			setLanguage(invitation, language);
			Locale other = otherLanguage(language);
			String expected = localize(language, ResKey.message(subject, title(language)));
			String unexpected = localize(other, ResKey.message(subject, title(other)));
			assertFalse(expected.equals(unexpected));

			assertTrue(inCurrentLocale(other, () -> module.sendVerificationMail(invitation, expected)));
			assertFalse(inCurrentLocale(other, () -> module.sendVerificationMail(invitation, unexpected)));
		}
	}

	/** The reset mails are written in the language of the account. */
	public void testResetMailsInAccountLanguage() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT, VERIFICATION_SCRIPT, RESET_SCRIPT);
		MailText passwordText = module.getConfig().getResetPasswordText();
		MailText mfaText = module.getConfig().getResetMFAText();

		String email = "reset-language@example.com";
		Person account = module.acceptInvitation(invite(email), "Doe", null, PASSWORD.toCharArray());
		for (Locale language : supportedLanguages()) {
			try (Transaction tx = kb().beginTransaction(ResKey.text("Set account language"))) {
				account.setLanguage(language);
				tx.commit();
			}
			String title = title(language);
			Locale other = otherLanguage(language);

			assertEquals(
				Arrays.asList(email, localize(language, ResKey.message(passwordText.getSubject(), title)),
					localize(language, ResKey.message(passwordText.getBody(), title, CODE))),
				inCurrentLocale(other, () -> module.sendResetPasswordMail(account, CODE)));
			assertEquals(
				Arrays.asList(email, localize(language, ResKey.message(mfaText.getSubject(), title)),
					localize(language, ResKey.message(mfaText.getBody(), title, CODE))),
				inCurrentLocale(other, () -> module.sendResetMFAMail(account, CODE)));
		}
	}

	/** Without an e-mail address of the account, no reset mail is sent. */
	public void testResetMailWithoutAddress() throws Exception {
		InvitationModule module = recordingModule(INVITATION_SCRIPT, VERIFICATION_SCRIPT, RESET_SCRIPT);

		Person account =
			module.acceptInvitation(invite("reset-no-address@example.com"), "Doe", null, PASSWORD.toCharArray());
		try (Transaction tx = kb().beginTransaction(ResKey.text("Remove address"))) {
			account.getUser().setEMail(null);
			tx.commit();
		}

		assertEquals(Boolean.FALSE, module.sendResetPasswordMail(account, CODE));
		assertEquals(Boolean.FALSE, module.sendResetMFAMail(account, CODE));
	}

	/** An accepted invitation gives the account its language. */
	public void testAcceptSetsLanguage() {
		for (Locale language : supportedLanguages()) {
			Invitation invitation = invite("accept-" + language.getLanguage() + "@example.com");
			setLanguage(invitation, language);

			Person account = module().acceptInvitation(invitation, "Doe", null, PASSWORD.toCharArray());

			assertEquals(language, account.getLanguage());
		}
	}

	/** An invitation without language gives the account the default language. */
	public void testAcceptWithoutLanguage() {
		Invitation invitation = invite("accept-default@example.com");
		setLanguage(invitation, null);

		Person account = module().acceptInvitation(invitation, "Doe", null, PASSWORD.toCharArray());

		assertEquals(ResourcesModule.getInstance().getDefaultLocale(), account.getLanguage());
	}

	/**
	 * An {@link InvitationModule} with the configuration of the running one, but the given mail
	 * scripts.
	 */
	private static InvitationModule recordingModule(String invitationMail, String verificationMail,
			String resetMail) throws ConfigurationException {
		InvitationModule.Config config = TypedConfiguration.copy(module().getConfig());
		setScript(config, InvitationModule.Config.INVITATION_MAIL, invitationMail);
		setScript(config, InvitationModule.Config.VERIFICATION_MAIL, verificationMail);
		setScript(config, InvitationModule.Config.RESET_PASSWORD_MAIL, resetMail);
		setScript(config, InvitationModule.Config.RESET_MFA_MAIL, resetMail);
		return (InvitationModule) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static void setScript(InvitationModule.Config config, String property, String script)
			throws ConfigurationException {
		config.update(config.descriptor().getProperty(property), ExprFormat.INSTANCE.getValue(property, script));
	}

	/**
	 * Computes the given value with the given locale as current locale, so that a mail written in
	 * the current locale is detected.
	 */
	private static <T> T inCurrentLocale(Locale locale, Supplier<T> computation) {
		SubSessionContext subSession = ThreadContextManager.getSubSession();
		assertNotNull("Test runs in a sub-session.", subSession);
		return subSession.withLocale(locale, computation);
	}

	private static List<Locale> supportedLanguages() {
		List<Locale> languages = ResourcesModule.getInstance().getSupportedLocales();
		assertTrue("Test needs two supported languages.", languages.size() >= 2);
		return languages;
	}

	private static Locale otherLanguage(Locale language) {
		for (Locale supported : supportedLanguages()) {
			if (!supported.getLanguage().equals(language.getLanguage())) {
				return supported;
			}
		}
		throw new AssertionError("No other language than " + language);
	}

	private static String title(Locale language) {
		return Resources.getInstance(language).getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
	}

	private static String localize(Locale language, ResKey key) {
		return Resources.getInstance(language).getString(key);
	}

	private void setLanguage(Invitation invitation, Locale language) {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Set invitation language"))) {
			invitation.setLanguage(language);
			tx.commit();
		}
	}

	private Invitation invite(String email) {
		Invitation invitation = module().createInvitation(email, MfaRequirement.OPTIONAL);
		_invitations.add(invitation);
		return invitation;
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
				new TestSuite(TestInvitationMails.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				InvitationModule.Module.INSTANCE));
	}

}
