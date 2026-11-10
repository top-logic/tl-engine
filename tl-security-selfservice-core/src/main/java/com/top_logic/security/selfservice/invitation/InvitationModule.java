/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import java.util.Date;
import java.util.regex.Pattern;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Bounds;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AuthenticationCategory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.InvitationState;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConfiguredManagedClass} for elements in
 * {@value TlSecuritySelfserviceFactory#TL_SECURITY_SELFSERVICE_STRUCTURE}.
 *
 * <p>
 * Offers the UI-independent operations of the {@link Invitation} lifecycle: an invitation is
 * {@link #createInvitation(String, MfaRequirement) created} and
 * {@link #sendInvitation(Invitation) sent}, it is open until it
 * {@link Config#getInvitationValidity() expires} or is
 * {@link #acceptInvitation(Invitation, String, String, char[]) accepted}, and an open or expired
 * invitation can be {@link #renewInvitation(Invitation) renewed} or
 * {@link #resendInvitation(Invitation) sent again}. An accepted invitation is kept
 * as a record of the account created from it.
 * </p>
 *
 * <p>
 * Visitors register an account without an invitation through the {@link #getSelfRegistration()
 * self-registration}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Service module for invitations")
@ServiceCategory.Of(AuthenticationCategory.class)
public class InvitationModule extends KBBasedManagedClass<InvitationModule.Config> {

	/**
	 * Typed configuration interface definition for {@link InvitationModule}.
	 */
	@DisplayOrder({
		Config.INVITATION_MAIL,
		Config.VERIFICATION_MAIL,
		Config.RESET_PASSWORD_MAIL,
		Config.RESET_MFA_MAIL,
		Config.INVITATION_VALIDITY,
		Config.INVITATION_LINK,
		Config.AUTHENTICATION_DEVICE,
		Config.CODE_VALIDITY,
		Config.VERIFICATION_CODE_SIZE,
		Config.PASSWORD_RESET_TIMEOUT,
		Config.SELF_REGISTRATION,
	})
	public interface Config extends KBBasedManagedClass.Config<InvitationModule> {

		/** Configuration name for {@link #getInvitationMail()}. */
		String INVITATION_MAIL = "invitation-mail";

		/** Configuration name for {@link #getVerificationMail()}. */
		String VERIFICATION_MAIL = "verification-mail";

		/** Configuration name for {@link #getResetPasswordMail()}. */
		String RESET_PASSWORD_MAIL = "reset-password-mail";

		/** Configuration name for {@link #getResetMFAMail()}. */
		String RESET_MFA_MAIL = "reset-mfa-mail";

		/** Configuration name for {@link #getCodeValidity()}. */
		String CODE_VALIDITY = "code-validity";

		/** Configuration name for {@link #getPasswortResetTimeOut()}. */
		String PASSWORD_RESET_TIMEOUT = "password-reset-timeout";

		/** Configuration name for {@link #getVerificationCodeSize()}. */
		String VERIFICATION_CODE_SIZE = "verification-code-size";

		/** Configuration name for {@link #getInvitationValidity()}. */
		String INVITATION_VALIDITY = "invitation-validity";

		/** Configuration name for {@link #getInvitationLink()}. */
		String INVITATION_LINK = "invitation-link";

		/** Configuration name for {@link #getAuthenticationDevice()}. */
		String AUTHENTICATION_DEVICE = "authentication-device";

		/** Configuration name for {@link #getSelfRegistration()}. */
		String SELF_REGISTRATION = "self-registration";

		/**
		 * How long an invitation can be accepted after it was created or sent again.
		 */
		@Format(MillisFormat.class)
		@Name(INVITATION_VALIDITY)
		@FormattedDefault("7d")
		long getInvitationValidity();

		/**
		 * The link of an invitation, the page of the application where the invited person accepts
		 * it.
		 *
		 * <p>
		 * By default, the link leads to the login of the classic user interface.
		 * </p>
		 */
		@Name(INVITATION_LINK)
		@ItemDefault
		@ImplementationClassDefault(InvitationLink.class)
		PolymorphicConfiguration<? extends InvitationLink<?>> getInvitationLink();

		/**
		 * The ID of the {@link AuthenticationDevice} on which an accepted invitation or a
		 * registration creates the account.
		 */
		@Name(AUTHENTICATION_DEVICE)
		@StringDefault(TLSecurityDeviceManager.DB_SECURITY)
		String getAuthenticationDevice();

		/**
		 * The TL-Script expression sending the invitation mail.
		 *
		 * <p>
		 * The expression is called with the {@link Invitation} object as first argument, the
		 * application title as second argument, and the invitation link as third argument.
		 * </p>
		 */
		@Name(INVITATION_MAIL)
		@Mandatory
		Expr getInvitationMail();

		/**
		 * The TL-Script expression sending the verification mail.
		 *
		 * <p>
		 * The expression is called with the {@link Invitation} object as first argument, the
		 * application title as second argument, and the verification code as third argument.
		 * </p>
		 */
		@Name(VERIFICATION_MAIL)
		@Mandatory
		Expr getVerificationMail();

		/**
		 * The TL-Script expression that sends an email when a password reset is requested.
		 *
		 * <p>
		 * The expression is called with the email as first argument, the application title as
		 * second argument, and the reset code as third argument.
		 * </p>
		 */
		@Name(RESET_PASSWORD_MAIL)
		@Mandatory
		Expr getResetPasswordMail();

		/**
		 * The TL-Script expression that sends an email when reset of the multi-factor
		 * authentication is requested.
		 *
		 * <p>
		 * The expression is called with the email as first argument, the application title as
		 * second argument, and the reset code as third argument.
		 * </p>
		 */
		@Name(RESET_MFA_MAIL)
		@Mandatory
		@Label("Reset multi-factor-authentication mail")
		Expr getResetMFAMail();

		/**
		 * How long a verification code remains valid after it was created.
		 */
		@Format(MillisFormat.class)
		@Name(CODE_VALIDITY)
		@Mandatory
		long getCodeValidity();

		/**
		 * Time in milliseconds how long a password reset request is valid.
		 */
		@Format(MillisFormat.class)
		@Name(PASSWORD_RESET_TIMEOUT)
		@Mandatory
		long getPasswortResetTimeOut();

		/**
		 * The number of digits that are used to create a verification code.
		 */
		@Mandatory
		@Bounds({
			@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 4),
			@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 18)
		})
		@Name(VERIFICATION_CODE_SIZE)
		int getVerificationCodeSize();

		/**
		 * Whether and how visitors register an account of their own without an invitation.
		 *
		 * @see SelfRegistration
		 */
		@Name(SELF_REGISTRATION)
		@ItemDefault
		SelfRegistration.Config getSelfRegistration();
	}

	/**
	 * Syntax of an e-mail address accepted for an invitation or a registration.
	 */
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private final QueryExecutor _verificationMail;

	private final QueryExecutor _invitationMail;

	private final QueryExecutor _resetPasswordMail;

	private final QueryExecutor _resetMFAMail;

	private final InvitationLink<?> _invitationLink;

	private final SelfRegistration _selfRegistration;

	/**
	 * Create a {@link InvitationModule}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InvitationModule(InstantiationContext context, Config config) {
		super(context, config);
		_verificationMail = QueryExecutor.compile(config.getVerificationMail());
		_invitationMail = QueryExecutor.compile(config.getInvitationMail());
		_resetPasswordMail = QueryExecutor.compile(config.getResetPasswordMail());
		_resetMFAMail = QueryExecutor.compile(config.getResetMFAMail());
		_invitationLink = context.getInstance(config.getInvitationLink());
		_selfRegistration = new SelfRegistration(this, config.getSelfRegistration());

		// "Forgot password", "reset MFA" and the confirmation of an invitation by the invited
		// person are triggered from the login screen, i.e. before the user is authenticated. The
		// corresponding scripts therefore run without a logged-in user and must not be subject to
		// a user's access rights (they would otherwise be denied, e.g. reading the address of the
		// invitation). The invitation mail is sent by an administrator and keeps security.
		_resetPasswordMail.disableSecurity();
		_resetMFAMail.disableSecurity();
		_verificationMail.disableSecurity();
	}

	/**
	 * The registration of accounts by visitors without an invitation.
	 *
	 * @see Config#getSelfRegistration()
	 */
	public SelfRegistration getSelfRegistration() {
		return _selfRegistration;
	}

	/**
	 * Compiled executor for {@link Config#getInvitationMail()}.
	 */
	public QueryExecutor getInvitationMail() {
		return _invitationMail;
	}

	/**
	 * Compiled executor for {@link Config#getVerificationMail()}.
	 */
	public QueryExecutor getVerificationMail() {
		return _verificationMail;
	}

	/**
	 * Compiled executor for {@link Config#getResetPasswordMail()}.
	 */
	public QueryExecutor getResetPasswordMail() {
		return _resetPasswordMail;
	}

	/**
	 * Compiled executor for {@link Config#getResetMFAMail()}.
	 */
	public QueryExecutor getResetMFAMail() {
		return _resetMFAMail;
	}

	/**
	 * Creates an open {@link Invitation} for the given address.
	 *
	 * <p>
	 * The invitation records when and by whom it was created and expires after the
	 * {@link Config#getInvitationValidity() configured validity}. It is not sent, see
	 * {@link #sendInvitation(Invitation)}.
	 * </p>
	 *
	 * @param email
	 *        The e-mail address of the invited person, also the user name of the account created
	 *        from the invitation.
	 * @param mfaRequirement
	 *        Whether the account created from the invitation must use a second factor,
	 *        <code>null</code> for the requirement of the
	 *        {@link Config#getAuthenticationDevice() authentication device}.
	 * @return The created invitation.
	 */
	public Invitation createInvitation(String email, MfaRequirement mfaRequirement) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.CREATED_INVITATION__EMAIL.fill(email))) {
			Invitation invitation = TlSecuritySelfserviceFactory.getInstance().createInvitation();
			invitation.setEmail(email);
			if (mfaRequirement != null) {
				invitation.setMfaRequirement(mfaRequirement);
			}
			tx.commit();
			return invitation;
		}
	}

	/**
	 * The {@link Invitation} with the given {@link Invitation#getId() token} in any state.
	 *
	 * @param token
	 *        The token from the invitation link.
	 * @return The invitation, or <code>null</code> if there is none with the given token.
	 *
	 * @see #findOpenInvitation(String)
	 */
	public Invitation findInvitation(String token) {
		if (token == null || token.isEmpty()) {
			return null;
		}
		DataObject item = kb().getObjectByAttribute(Invitation.TABLE_NAME, Invitation.ID_ATTR, token);
		if (item == null) {
			return null;
		}
		return ((KnowledgeItem) item).getWrapper();
	}

	/**
	 * The open {@link Invitation} with the given {@link Invitation#getId() token}.
	 *
	 * @param token
	 *        The token from the invitation link.
	 * @return The open invitation.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_UNKNOWN_INVITATION} if there is no invitation with
	 *         the given token, with {@link I18NConstants#ERROR_INVITATION_EXPIRED} or
	 *         {@link I18NConstants#ERROR_INVITATION_ACCEPTED} if it is not open.
	 */
	public Invitation findOpenInvitation(String token) {
		Invitation invitation = findInvitation(token);
		if (invitation == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_INVITATION);
		}
		checkOpen(invitation);
		return invitation;
	}

	/**
	 * Checks that the given {@link Invitation} can be accepted.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_INVITATION_EXPIRED} or
	 *         {@link I18NConstants#ERROR_INVITATION_ACCEPTED} if it is not open.
	 */
	public static void checkOpen(Invitation invitation) {
		checkNotAccepted(invitation);
		if (isExpired(invitation)) {
			throw new TopLogicException(I18NConstants.ERROR_INVITATION_EXPIRED);
		}
	}

	private static void checkNotAccepted(Invitation invitation) {
		if (isAccepted(invitation)) {
			throw new TopLogicException(I18NConstants.ERROR_INVITATION_ACCEPTED);
		}
	}

	/**
	 * Whether the given {@link Invitation} is {@link InvitationState#OPEN_CLASSIFIER open}.
	 */
	public static boolean isOpen(Invitation invitation) {
		return hasState(invitation, InvitationState.OPEN_CLASSIFIER);
	}

	/**
	 * Whether the given {@link Invitation} is {@link InvitationState#EXPIRED_CLASSIFIER expired}.
	 */
	public static boolean isExpired(Invitation invitation) {
		return hasState(invitation, InvitationState.EXPIRED_CLASSIFIER);
	}

	/**
	 * Whether the given {@link Invitation} is {@link InvitationState#ACCEPTED_CLASSIFIER
	 * accepted}.
	 */
	public static boolean isAccepted(Invitation invitation) {
		return hasState(invitation, InvitationState.ACCEPTED_CLASSIFIER);
	}

	private static boolean hasState(Invitation invitation, String classifierName) {
		TLClassifier state = invitation.getState();
		return state != null && classifierName.equals(state.getName());
	}

	/**
	 * Creates the account of the invited person and marks the {@link Invitation} accepted.
	 *
	 * <p>
	 * The account is created on the {@link Config#getAuthenticationDevice() configured
	 * authentication device} with the e-mail address of the invitation as user name and the
	 * {@link Invitation#getMfaRequirement() second-factor requirement} of the invitation, see
	 * {@link #createAccount(KnowledgeBase, String, String, String, char[], MfaRequirement)}. The {@link Invitation#getCreateCallback() callback} of the invitation may
	 * refuse the account. The invitation records the time of the acceptance and the account.
	 * Either all of this happens, or nothing.
	 * </p>
	 *
	 * @param invitation
	 *        The invitation to accept.
	 * @param surname
	 *        The surname of the invited person.
	 * @param givenName
	 *        The given name of the invited person, may be <code>null</code>.
	 * @param password
	 *        The password of the account; ignored if the device does not manage passwords. The
	 *        caller clears it after the call.
	 * @return The created account.
	 *
	 * @throws TopLogicException
	 *         If the invitation is not open, the password is missing or violates the password
	 *         rules, the account cannot be created, or the callback refuses it.
	 */
	public Person acceptInvitation(Invitation invitation, String surname, String givenName, char[] password) {
		checkOpen(invitation);
		String email = invitation.getEmail();

		KnowledgeBase kb = invitation.tKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.ACCEPTED_INVITATION__EMAIL.fill(email))) {
			Person account = createAccount(kb, email, surname, givenName, password, invitation.getMfaRequirement());

			SearchExpression createCallback = invitation.getCreateCallback();
			if (createCallback != null) {
				checkAccountCallback(
					QueryExecutor.executor(kb, ModelService.getApplicationModel(), createCallback), account);
			}

			invitation.setAccepted(new Date());
			invitation.setAccount(account);
			tx.commit();
			return account;
		}
	}

	/**
	 * Creates an account for a person who has proven to read the mails of the given address.
	 *
	 * <p>
	 * The account is created on the {@link Config#getAuthenticationDevice() configured
	 * authentication device} with the e-mail address as user name, and gets the given surname,
	 * given name and e-mail address. If the device manages passwords, the given password is
	 * checked against the password rules of the device and set. Must be called within a
	 * transaction, which the caller commits.
	 * </p>
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} of the account.
	 * @param email
	 *        The e-mail address, also the user name of the account.
	 * @param surname
	 *        The surname of the person.
	 * @param givenName
	 *        The given name of the person, may be <code>null</code>.
	 * @param password
	 *        The password of the account; ignored if the device does not manage passwords. The
	 *        caller clears it after the call.
	 * @param mfaRequirement
	 *        Whether the account must use a second factor, <code>null</code> for the requirement
	 *        of the authentication device.
	 * @return The created account.
	 *
	 * @throws TopLogicException
	 *         If the password is missing or violates the password rules, or the account cannot be
	 *         created, e.g. because an account with the user name exists.
	 */
	public Person createAccount(KnowledgeBase kb, String email, String surname, String givenName,
			char[] password, MfaRequirement mfaRequirement) {
		AuthenticationDevice device = authenticationDevice();
		Person account = Person.create(kb, email, device);
		if (mfaRequirement != null) {
			account.setMFARequirement(mfaRequirement);
		}

		UserInterface user = account.getUser();
		user.setName(surname);
		if (givenName != null && !givenName.isEmpty()) {
			user.setFirstName(givenName);
		}
		user.setEMail(email);

		if (device.allowPwdChange()) {
			setPassword(device, account, password);
		}
		return account;
	}

	private static void setPassword(AuthenticationDevice device, Person account, char[] password) {
		if (password == null || password.length == 0) {
			throw new TopLogicException(I18NConstants.ERROR_PASSWORD_EMPTY);
		}
		PasswordValidator validator = device.getPasswordValidator();
		if (validator != null) {
			ValidationResult result = validator.validatePassword(account, password);
			if (result != ValidationResult.OK) {
				throw new TopLogicException(result.messageKey());
			}
		}
		device.setPassword(account, password);
	}

	/**
	 * Calls a function deciding whether a new account is accepted.
	 *
	 * @param callback
	 *        The function, called with the new account. It answers <code>null</code> to accept the
	 *        account, otherwise the reason to refuse it.
	 * @param account
	 *        The new account.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_ACCOUNT_CREATION_DENIED} and the answer of the
	 *         function as details if the function refuses the account.
	 */
	public static void checkAccountCallback(QueryExecutor callback, Person account) {
		Object result = callback.execute(account);
		if (result != null) {
			TopLogicException refusal = new TopLogicException(I18NConstants.ERROR_ACCOUNT_CREATION_DENIED);
			ResKey deniedKey = SearchExpression.asResKey(result);
			if (deniedKey != null) {
				refusal.initDetails(deniedKey);
			}
			throw refusal;
		}
	}

	/**
	 * Why the given value is not accepted as e-mail address of an invitation or a registration.
	 *
	 * @param email
	 *        The address to check, may be <code>null</code>.
	 * @return {@link I18NConstants#ERROR_INVALID_EMAIL__ADDRESS}, or <code>null</code> if the
	 *         address is valid.
	 */
	public static ResKey emailRefusal(String email) {
		if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
			return I18NConstants.ERROR_INVALID_EMAIL__ADDRESS.fill(email == null ? "" : email);
		}
		return null;
	}

	/**
	 * Checks that the given value is accepted as e-mail address of an invitation or a
	 * registration.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_INVALID_EMAIL__ADDRESS} otherwise.
	 *
	 * @see #emailRefusal(String)
	 */
	public static void checkEmail(String email) {
		ResKey refusal = emailRefusal(email);
		if (refusal != null) {
			throw new TopLogicException(refusal);
		}
	}

	/**
	 * The {@link AuthenticationDevice} on which an accepted invitation or a registration creates the
	 * account.
	 *
	 * @see Config#getAuthenticationDevice()
	 */
	public AuthenticationDevice authenticationDevice() {
		String deviceId = getConfig().getAuthenticationDevice();
		AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getAuthenticationDevice(deviceId);
		if (device == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_AUTHENTICATION_DEVICE__ID.fill(deviceId));
		}
		return device;
	}

	/**
	 * Gives an open or expired {@link Invitation} a new {@link Invitation#getId() token} and a new
	 * {@link Invitation#getExpires() expiry date}.
	 *
	 * <p>
	 * The previous link of the invitation is no longer valid. The invitation is not sent, see
	 * {@link #sendInvitation(Invitation)}.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_INVITATION_ACCEPTED} if the invitation is accepted.
	 */
	public void renewInvitation(Invitation invitation) {
		checkNotAccepted(invitation);
		try (Transaction tx = invitation.tKnowledgeBase()
			.beginTransaction(I18NConstants.RENEWED_INVITATION__EMAIL.fill(invitation.getEmail()))) {
			TLStructuredTypePart idPart = invitation.tType().getPart(Invitation.ID_ATTR);
			invitation.setId((String) DisplayAnnotations.getDefaultProvider(idPart).createDefault(invitation, idPart));
			invitation.setExpires(expiryFromNow());
			tx.commit();
		}
	}

	/**
	 * The absolute link of the given {@link Invitation}, built from the public address of the
	 * application.
	 *
	 * @throws TopLogicException
	 *         If the public address of the application is not configured.
	 *
	 * @see Config#getInvitationLink()
	 */
	public String invitationLink(Invitation invitation) {
		return _invitationLink.url(invitation.getId());
	}

	/**
	 * Sends the {@link Config#getInvitationMail() invitation mail} for the given open
	 * {@link Invitation}.
	 *
	 * <p>
	 * An expired invitation is sent again with {@link #resendInvitation(Invitation)}.
	 * </p>
	 *
	 * @return The result of the mail script, by default whether the mail was sent.
	 *
	 * @throws TopLogicException
	 *         If the invitation is not open, or the public address of the application is not
	 *         configured.
	 */
	public Object sendInvitation(Invitation invitation) {
		checkOpen(invitation);
		return getInvitationMail().execute(invitation, applicationTitle(), invitationLink(invitation));
	}

	/**
	 * Sends the {@link Config#getInvitationMail() invitation mail} for an open or expired
	 * {@link Invitation} again.
	 *
	 * <p>
	 * An open invitation is sent with its link and expiry date. An expired invitation is
	 * {@link #renewInvitation(Invitation) renewed} first, so it is sent with a new link and a new
	 * expiry date.
	 * </p>
	 *
	 * @return The result of the mail script, by default whether the mail was sent.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_INVITATION_ACCEPTED} if the invitation is accepted,
	 *         or if the public address of the application is not configured.
	 */
	public Object resendInvitation(Invitation invitation) {
		if (isExpired(invitation)) {
			renewInvitation(invitation);
		}
		return sendInvitation(invitation);
	}

	/**
	 * Sends the {@link Config#getVerificationMail() verification mail} with the given code to the
	 * address of the given {@link Invitation}.
	 *
	 * @param invitation
	 *        The invitation the invited person confirms with the code.
	 * @param code
	 *        The code, see {@link VerificationCode}.
	 * @return Whether the mail was sent; only an answer <code>false</code> of the mail script
	 *         counts as failure.
	 */
	public boolean sendVerificationMail(Invitation invitation, String code) {
		Object sent = getVerificationMail().execute(invitation, applicationTitle(), code);
		return !Boolean.FALSE.equals(sent);
	}

	/**
	 * The reason why the {@link Invitation} with the given token cannot be accepted.
	 *
	 * @param token
	 *        The token from the invitation link, may be <code>null</code>.
	 * @return The message telling why the invitation cannot be accepted (see
	 *         {@link #findOpenInvitation(String)}), or <code>null</code> if it is open.
	 */
	public ResKey refusal(String token) {
		try {
			findOpenInvitation(token);
			return null;
		} catch (TopLogicException ex) {
			return ex.getErrorKey();
		}
	}

	/**
	 * The title of the application, the second argument of the mail scripts.
	 */
	public static String applicationTitle() {
		return Resources.getInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
	}

	/**
	 * The expiry date of an {@link Invitation} created or renewed now.
	 *
	 * <p>
	 * Uses the configuration of the {@link InvitationModule} also if the module is not started.
	 * </p>
	 *
	 * @see Config#getInvitationValidity()
	 */
	public static Date expiryFromNow() {
		return new Date(System.currentTimeMillis() + configuration().getInvitationValidity());
	}

	private static Config configuration() {
		if (Module.INSTANCE.isActive()) {
			return getInstance().getConfig();
		}
		try {
			return (Config) ApplicationConfig.getInstance().getServiceConfiguration(InvitationModule.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Access to the single instance of {@link InvitationModule}.
	 */
	public static InvitationModule getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link InvitationModule}.
	 */
	public static final class Module extends TypedRuntimeModule<InvitationModule> {

		/**
		 * Singleton {@link InvitationModule.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<InvitationModule> getImplementation() {
			return InvitationModule.class;
		}

	}
}

