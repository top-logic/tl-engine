/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import java.util.Date;
import java.util.Locale;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.security.selfservice.model.Registration;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Registration of an account by a visitor without an invitation.
 *
 * <p>
 * A visitor {@link #requestCode(String, String, String) requests a verification code} for an
 * e-mail address and {@link #register(String, String, String, char[]) registers} the account
 * after entering the code. Depending on the {@link Config#getMode() mode}, the account can log in
 * right away, or is locked until an administrator {@link #approve(Registration) approves} the
 * registration. Each registration is recorded as a {@link Registration}.
 * </p>
 *
 * <p>
 * The operations are independent of a user interface. A user interface keeps the
 * {@link VerificationCode} and the address it was sent to on the server side of its dialog, checks
 * the entered code and then registers the account for this address.
 * </p>
 *
 * @see InvitationModule#getSelfRegistration()
 */
public class SelfRegistration {

	/**
	 * Configuration of the {@link SelfRegistration}, the option
	 * {@link InvitationModule.Config#getSelfRegistration()}.
	 */
	@DisplayOrder({
		Config.MODE,
		Config.CAPTCHA,
		Config.CALLBACK,
		Config.REGISTRATION_MAIL,
		Config.REGISTRATION_EXISTS_MAIL,
		Config.APPROVAL_MAIL,
	})
	public interface Config extends ConfigurationItem {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getCaptcha()}. */
		String CAPTCHA = "captcha";

		/** Configuration name for {@link #getCallback()}. */
		String CALLBACK = "callback";

		/** Configuration name for {@link #getRegistrationMail()}. */
		String REGISTRATION_MAIL = "registration-mail";

		/** Configuration name for {@link #getRegistrationExistsMail()}. */
		String REGISTRATION_EXISTS_MAIL = "registration-exists-mail";

		/** Configuration name for {@link #getApprovalMail()}. */
		String APPROVAL_MAIL = "approval-mail";

		/**
		 * Whether visitors register an account of their own, and whether an administrator must
		 * approve it before it can log in.
		 */
		@Name(MODE)
		SelfRegistrationMode getMode();

		/**
		 * Whether a visitor must solve a captcha to request a verification code.
		 *
		 * <p>
		 * The captcha is checked by the {@link CaptchaService} of the module
		 * <code>tl-security-captcha</code>, which must be active then.
		 * </p>
		 */
		@Name(CAPTCHA)
		boolean getCaptcha();

		/**
		 * Function called with each registered account before the registration is stored.
		 *
		 * <p>
		 * The function assigns e.g. roles or groups to the account. It answers <code>null</code> to
		 * accept the account, or an internationalizable value as the reason to refuse it, which
		 * rolls the registration back. It runs without access checks, since nobody is logged in.
		 * </p>
		 */
		@Name(CALLBACK)
		@Nullable
		Expr getCallback();

		/**
		 * The TL-Script expression sending the verification code of a registration.
		 *
		 * <p>
		 * The expression is called with the e-mail address as first argument, the application title
		 * as second argument, and the verification code as third argument. It runs without access
		 * checks, since nobody is logged in. An answer <code>false</code> means that the mail was
		 * not sent.
		 * </p>
		 */
		@Name(REGISTRATION_MAIL)
		@Nullable
		Expr getRegistrationMail();

		/**
		 * The TL-Script expression sent instead of the {@link #getRegistrationMail() verification
		 * code} when an account with the address exists already.
		 *
		 * <p>
		 * The expression is called with the e-mail address as first argument and the application
		 * title as second argument. It runs without access checks, since nobody is logged in. An
		 * answer <code>false</code> means that the mail was not sent.
		 * </p>
		 */
		@Name(REGISTRATION_EXISTS_MAIL)
		@Nullable
		Expr getRegistrationExistsMail();

		/**
		 * The TL-Script expression telling a registered person that the registration has been
		 * approved.
		 *
		 * <p>
		 * The expression is called with the e-mail address of the registration as first argument,
		 * the application title as second argument, and the address of the login page of the
		 * application as third argument. It runs without access checks: the recipient is fixed by
		 * the registration. Without a value, no mail is sent.
		 * </p>
		 */
		@Name(APPROVAL_MAIL)
		@Nullable
		Expr getApprovalMail();
	}

	/**
	 * Prefix of the key under which code requests for an e-mail address are throttled, followed by
	 * the address in lower case.
	 */
	public static final String THROTTLE_ADDRESS_PREFIX = "registration:";

	/**
	 * Prefix of the key under which code requests of a client are throttled, followed by the
	 * address of the client.
	 */
	public static final String THROTTLE_CLIENT_PREFIX = "registration-client:";

	private final InvitationModule _module;

	private final Config _config;

	private final QueryExecutor _registrationMail;

	private final QueryExecutor _registrationExistsMail;

	private final QueryExecutor _approvalMail;

	private final QueryExecutor _callback;

	/**
	 * Creates a {@link SelfRegistration}.
	 *
	 * @param module
	 *        The module creating the accounts.
	 * @param config
	 *        The configuration.
	 */
	SelfRegistration(InvitationModule module, Config config) {
		_module = module;
		_config = config;
		_registrationMail = compileAnonymous(config.getRegistrationMail());
		_registrationExistsMail = compileAnonymous(config.getRegistrationExistsMail());
		_callback = compileAnonymous(config.getCallback());
		_approvalMail = compileAnonymous(config.getApprovalMail());
	}

	/**
	 * Compiles an expression that runs without access checks, e.g. before anyone is logged in.
	 */
	private static QueryExecutor compileAnonymous(Expr expr) {
		if (expr == null) {
			return null;
		}
		QueryExecutor executor = QueryExecutor.compile(expr);
		executor.disableSecurity();
		return executor;
	}

	/**
	 * The {@link SelfRegistration} of the application.
	 */
	public static SelfRegistration getInstance() {
		return InvitationModule.getInstance().getSelfRegistration();
	}

	/**
	 * The configured mode.
	 *
	 * @see Config#getMode()
	 */
	public SelfRegistrationMode getMode() {
		return _config.getMode();
	}

	/**
	 * Whether a visitor must solve a captcha to request a verification code.
	 *
	 * @see Config#getCaptcha()
	 */
	public boolean isCaptchaRequired() {
		return _config.getCaptcha();
	}

	/**
	 * Whether visitors can register an account.
	 *
	 * <p>
	 * Registration is possible if the {@link #getMode() mode} is not
	 * {@link SelfRegistrationMode#OFF}, the {@link Config#getRegistrationMail() mail scripts} are
	 * configured and the application can send mails ({@link MailSenderService#isConfigured()}).
	 * </p>
	 */
	public boolean isEnabled() {
		return getMode() != SelfRegistrationMode.OFF
			&& _registrationMail != null
			&& _registrationExistsMail != null
			&& MailSenderService.isConfigured();
	}

	private void checkEnabled() {
		if (!isEnabled()) {
			throw new TopLogicException(I18NConstants.ERROR_SELF_REGISTRATION_DISABLED);
		}
	}

	/**
	 * Sends a verification code for registering an account to the given address.
	 *
	 * <p>
	 * The code is sent through {@link Config#getRegistrationMail()}. If an account with the address
	 * exists already, {@link Config#getRegistrationExistsMail()} is sent instead, and the returned
	 * code is mailed to nobody: the caller behaves the same in both cases, so it does not reveal
	 * which addresses have an account. No record is written.
	 * </p>
	 *
	 * <p>
	 * Requests are throttled per address (key {@link #THROTTLE_ADDRESS_PREFIX} and the address in
	 * lower case) and per client (key {@link #THROTTLE_CLIENT_PREFIX} and the client address), see
	 * {@link VerificationCode#throttleRequest(String)}. The captcha, if
	 * {@link Config#getCaptcha() required}, is checked before, so that requests without a solved
	 * captcha do not delay the requests of others.
	 * </p>
	 *
	 * @param email
	 *        The e-mail address of the visitor, the user name of the account to register.
	 * @param captchaPayload
	 *        The solution of the captcha as the widget submits it; ignored if no captcha is
	 *        required.
	 * @param clientAddress
	 *        The network address of the requesting client, <code>null</code> if unknown.
	 * @return The code the caller keeps on the server side and checks before calling
	 *         {@link #register(String, String, String, char[])}.
	 *
	 * @throws TopLogicException
	 *         If registration is not {@link #isEnabled() enabled}, the captcha is not solved, the
	 *         address is invalid, the request must wait, or the mail was not sent.
	 */
	public VerificationCode requestCode(String email, String captchaPayload, String clientAddress) {
		checkEnabled();
		if (isCaptchaRequired()) {
			CaptchaService.getInstance().check(captchaPayload);
		}
		String address = normalizeAddress(email);
		InvitationModule.checkEmail(address);
		if (!StringServices.isEmpty(clientAddress)) {
			VerificationCode.throttleRequest(THROTTLE_CLIENT_PREFIX + clientAddress);
		}
		VerificationCode.throttleRequest(THROTTLE_ADDRESS_PREFIX + address.toLowerCase(Locale.ROOT));

		VerificationCode code = new VerificationCode();
		String applicationTitle = InvitationModule.applicationTitle();
		Object sent;
		if (Person.byName(_module.kb(), address) != null) {
			sent = _registrationExistsMail.execute(address, applicationTitle);
		} else {
			sent = _registrationMail.execute(address, applicationTitle, code.getCode());
		}
		if (Boolean.FALSE.equals(sent)) {
			throw new TopLogicException(I18NConstants.ERROR_VERIFICATION_CODE_NOT_SENT);
		}
		return code;
	}

	/**
	 * Registers the account of a visitor.
	 *
	 * <p>
	 * The caller has checked before that the visitor reads the mails of the address, with the
	 * {@link VerificationCode} {@link #requestCode(String, String, String) sent} to it.
	 * </p>
	 *
	 * <p>
	 * In one transaction, the account is created like the account of an accepted invitation (see
	 * {@link InvitationModule#createAccount(KnowledgeBase, String, String, String, char[], com.top_logic.knowledge.wrap.person.MfaRequirement)}),
	 * {@link Person#setLocked(boolean) locked} in mode {@link SelfRegistrationMode#APPROVAL},
	 * passed to the {@link Config#getCallback() callback}, and recorded as {@link Registration},
	 * which is pending in mode {@link SelfRegistrationMode#APPROVAL} and approved otherwise. Either
	 * all of this happens, or nothing.
	 * </p>
	 *
	 * @param email
	 *        The verified e-mail address, the user name of the account.
	 * @param surname
	 *        The surname of the visitor.
	 * @param givenName
	 *        The given name of the visitor, may be <code>null</code>.
	 * @param password
	 *        The password of the account; ignored if the device does not manage passwords. The
	 *        caller clears it after the call.
	 * @return The created account.
	 *
	 * @throws TopLogicException
	 *         If registration is not {@link #isEnabled() enabled}, the address is invalid or has an
	 *         account, the password is missing or violates the password rules, or the callback
	 *         refuses the account.
	 */
	public Person register(String email, String surname, String givenName, char[] password) {
		checkEnabled();
		String address = normalizeAddress(email);
		InvitationModule.checkEmail(address);
		boolean approval = getMode() == SelfRegistrationMode.APPROVAL;

		KnowledgeBase kb = _module.kb();
		try (Transaction tx = kb.beginTransaction(I18NConstants.REGISTERED_ACCOUNT__EMAIL.fill(address))) {
			Person account = _module.createAccount(kb, address, surname, givenName, password, null);
			if (approval) {
				account.setLocked(true);
			}
			if (_callback != null) {
				InvitationModule.checkAccountCallback(_callback, account);
			}

			Registration registration = TlSecuritySelfserviceFactory.getInstance().createRegistration();
			registration.setEmail(address);
			registration.setAccount(account);
			if (!approval) {
				registration.setApproved(new Date());
			}
			tx.commit();
			return account;
		}
	}

	/**
	 * Approves a pending {@link Registration}: unlocks its account and records the approval by the
	 * current user, then sends the {@link Config#getApprovalMail() approval mail}.
	 *
	 * @param registration
	 *        The pending registration.
	 * @return The result of the approval mail script, by default whether the mail was sent;
	 *         <code>null</code> if no mail is configured.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_REGISTRATION_NOT_PENDING} if the registration is
	 *         approved already, or with {@link I18NConstants#ERROR_NO_PUBLIC_ADDRESS} if a mail is
	 *         configured and the address of the login page cannot be built; nothing is changed
	 *         then.
	 */
	public Object approve(Registration registration) {
		if (!isPending(registration)) {
			throw new TopLogicException(I18NConstants.ERROR_REGISTRATION_NOT_PENDING);
		}
		String loginUrl = _approvalMail == null ? null : ApplicationAddress.loginUrl();
		Person account = registration.getAccount();

		try (Transaction tx = registration.tKnowledgeBase()
			.beginTransaction(I18NConstants.APPROVED_REGISTRATION__EMAIL.fill(registration.getEmail()))) {
			if (account != null) {
				account.setLocked(false);
			}
			registration.setApproved(new Date());
			registration.setApprovedBy(TLContext.currentUser());
			tx.commit();
		}

		if (_approvalMail == null) {
			return null;
		}
		return _approvalMail.execute(registration.getEmail(), InvitationModule.applicationTitle(), loginUrl);
	}

	/**
	 * Whether the given {@link Registration} waits for its approval.
	 *
	 * <p>
	 * A registration waits while it is not approved and its account is locked. An account unlocked
	 * otherwise than by {@link #approve(Registration)}, e.g. in the account administration, can log
	 * in, so its registration no longer waits.
	 * </p>
	 */
	public static boolean isPending(Registration registration) {
		Person account = registration.getAccount();
		return registration.getApproved() == null && account != null && account.isLocked();
	}

	/**
	 * The pending {@link Registration} of the given account.
	 *
	 * @return The registration waiting for its approval (see {@link #isPending(Registration)}),
	 *         <code>null</code> if the account was not registered by its owner, its registration is
	 *         approved or the account is not locked.
	 */
	public static Registration pendingRegistration(Person account) {
		if (account == null) {
			return null;
		}
		for (TLObject referer : account.tReferers(TlSecuritySelfserviceFactory.getAccountRegistrationAttr())) {
			Registration registration = (Registration) referer;
			if (isPending(registration)) {
				return registration;
			}
		}
		return null;
	}

	private static String normalizeAddress(String email) {
		return email == null ? "" : email.strip();
	}

}
