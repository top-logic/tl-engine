/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.InvalidSignatureCountException;
import com.yubico.webauthn.exception.RegistrationFailedException;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AuthenticationCategory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.passkey.PasskeyRefusedException.Category;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.model.TlSecurityPasskeyFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * The relying party of the passkeys (Web Authentication credentials) of the accounts: registers
 * passkeys and checks logins with them.
 *
 * <h2>Address of the relying party</h2>
 *
 * <p>
 * The relying party ID and the accepted origins are taken from the public address of the
 * application, the alias {@link AliasManager#HOST %HOST%} (set from the variable
 * <code>tl_host</code>), when the service starts: the origin of that address is the accepted
 * origin, its host the relying party ID. The options {@link Config#getRpId() rp-id} and
 * {@link Config#getOrigins() origins} override them, see
 * {@link RelyingPartyAddress#resolve(String, String, java.util.Collection) the resolution}. Nothing
 * is taken from a request: a reverse proxy decides what a request says, and the relying party ID
 * binds every passkey for good. If no origin can be resolved, the service logs an error and stays
 * up without passkeys: every <code>start</code> method refuses with
 * {@link I18NConstants#ERROR_NOT_SET_UP the message} that passkeys are not set up.
 * </p>
 *
 * <h2>Refusals</h2>
 *
 * <p>
 * A <code>finish</code> method refuses a credential with a {@link PasskeyRefusedException} that
 * names the {@link PasskeyRefusedException.Category category} of the refusal and the refused
 * credential, and logs the refusal as warning with the category, the reason, the credential ID, the
 * accounts involved and the origin the browser reported. The categories are decided by the checks
 * of this service before the library runs its own; a refusal of the library is
 * {@link PasskeyRefusedException.Category#FAILED} with its reason.
 * </p>
 *
 * <p>
 * Registered passkeys are discoverable, so they can log in without a user name. Such a login with
 * a passkey alone always requires that the authenticator verifies its user (PIN, biometrics),
 * because the passkey is its only proof. Registrations and logins with a passkey as second factor
 * after the password require the configured user verification.
 * </p>
 *
 * @implNote The protocol work (challenges, client data, authenticator data, attestation and
 *           signature checks) is done by the library <code>com.yubico:webauthn-server-core</code>.
 *           Each ceremony has two steps: a <code>start</code> method creates a
 *           {@link PendingCeremony} whose {@link PendingCeremony#toOptionsJson() options} go to the
 *           browser, and the matching <code>finish</code> method checks the credential the browser
 *           answers with. The passkeys are stored as {@link Passkey} parts of their accounts, see
 *           {@link PasskeyCredentialRepository}.
 */
@ServiceCategory.Of(AuthenticationCategory.class)
@ServiceDependencies({
	AliasManager.Module.class,
	ResourcesModule.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
	PersonManager.Module.class,
})
public class PasskeyService extends ConfiguredManagedClass<PasskeyService.Config<?>> {

	/**
	 * Configuration options of {@link PasskeyService}.
	 */
	public interface Config<I extends PasskeyService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration name for {@link #getRpId()}.
		 *
		 * @see #getRpId()
		 */
		String RP_ID = "rp-id";

		/**
		 * Configuration name for {@link #getOrigins()}.
		 *
		 * @see #getOrigins()
		 */
		String ORIGINS = "origins";

		/**
		 * Configuration name for {@link #getRpName()}.
		 *
		 * @see #getRpName()
		 */
		String RP_NAME = "rp-name";

		/**
		 * Configuration name for {@link #getUserVerification()}.
		 *
		 * @see #getUserVerification()
		 */
		String USER_VERIFICATION = "user-verification";

		/**
		 * Configuration name for {@link #getTimeout()}.
		 *
		 * @see #getTimeout()
		 */
		String TIMEOUT = "timeout";

		/**
		 * The relying party ID: the domain the passkeys are bound to, e.g.
		 * <code>example.com</code>.
		 *
		 * <p>
		 * A passkey can only be used on this domain and its subdomains, so changing it makes all
		 * registered passkeys unusable. It must be the host of every accepted origin or a parent
		 * domain of it. Empty: the host of the public address of the application
		 * ({@link AliasManager#HOST %HOST%}, variable <code>tl_host</code>), or, without one, the
		 * host of the first of the {@link #getOrigins() origins}.
		 * </p>
		 */
		@Name(RP_ID)
		@Label("Relying party ID")
		@Nullable
		String getRpId();

		/**
		 * The origins the browser may run a ceremony on, e.g. <code>https://app.example.com</code>.
		 *
		 * <p>
		 * Set, they replace the origin of the public address of the application, so list that
		 * address too if passkeys are to work there. Empty: the origin of the public address of the
		 * application ({@link AliasManager#HOST %HOST%}, variable <code>tl_host</code>).
		 * </p>
		 */
		@Name(ORIGINS)
		@Format(CommaSeparatedStrings.class)
		List<String> getOrigins();

		/**
		 * The name of the relying party, shown by an authenticator when a passkey is created.
		 *
		 * <p>
		 * Empty: the application title.
		 * </p>
		 */
		@Name(RP_NAME)
		@Label("Relying party name")
		@Nullable
		@NullDefault
		ResKey getRpName();

		/**
		 * Whether an authenticator must verify its user (PIN, biometrics) when a passkey is
		 * registered or used as second factor.
		 *
		 * <p>
		 * A login with a passkey alone always requires user verification.
		 * </p>
		 */
		@Name(USER_VERIFICATION)
		@FormattedDefault("REQUIRED")
		UserVerificationRequirement getUserVerification();

		/**
		 * How long the browser waits for the user to answer a ceremony, and how long a started
		 * ceremony is accepted.
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@FormattedDefault("5min")
		long getTimeout();
	}

	/**
	 * Name of the variable that sets the public address of the application, the alias
	 * {@link AliasManager#HOST}.
	 */
	public static final String HOST_VARIABLE = "tl_host";

	private static final String REGISTRATION = "Registration of a passkey";

	private static final String LOGIN = "Login with a passkey";

	private final PasskeyCredentialRepository _repository = new PasskeyCredentialRepository();

	/** The address of the relying party, <code>null</code> if passkeys are not set up. */
	private RelyingPartyAddress _address;

	/** The relying party, <code>null</code> if passkeys are not set up. */
	private RelyingParty _relyingParty;

	/**
	 * Creates a {@link PasskeyService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub-configurations.
	 * @param config
	 *        The configuration.
	 */
	public PasskeyService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		String host = AliasManager.getInstance().getAlias(AliasManager.HOST);
		try {
			_address = RelyingPartyAddress.resolve(host, getConfig().getRpId(), getConfig().getOrigins());
		} catch (IllegalArgumentException ex) {
			_address = null;
			_relyingParty = null;
			Logger.error("Passkeys are not available: " + ex.getMessage()
				+ " Set the public address of the application with the variable '" + HOST_VARIABLE
				+ "' (alias " + AliasManager.HOST + "), e.g. 'https://app.example.com', or configure the option '"
				+ Config.ORIGINS + "' (and '" + Config.RP_ID + "') of " + PasskeyService.class.getName()
				+ ". Value of " + AliasManager.HOST + ": '" + StringServices.nonNull(host) + "'.",
				PasskeyService.class);
			return;
		}
		_relyingParty = createRelyingParty(_address);
		Logger.info("Passkeys use the relying party ID '" + _address.getRpId() + "' and accept the origins "
			+ _address.getOrigins() + ".", PasskeyService.class);
	}

	@Override
	protected void shutDown() {
		_address = null;
		_relyingParty = null;
		super.shutDown();
	}

	/**
	 * Whether passkeys are set up: the address of the relying party could be resolved when the
	 * service started.
	 */
	public boolean isAvailable() {
		return _relyingParty != null;
	}

	/**
	 * The address of the relying party.
	 *
	 * @return The address, <code>null</code> if passkeys are not {@link #isAvailable() set up}.
	 */
	public RelyingPartyAddress getAddress() {
		return _address;
	}

	/**
	 * The relying party ID the passkeys are bound to.
	 *
	 * @return The ID, <code>null</code> if passkeys are not {@link #isAvailable() set up}.
	 */
	public String getRpId() {
		RelyingPartyAddress address = _address;
		return address == null ? null : address.getRpId();
	}

	/**
	 * Starts the registration of a passkey for the given account.
	 *
	 * <p>
	 * The options ask for a discoverable passkey and exclude the passkeys the account already has,
	 * so that an authenticator does not create a second one for the same account.
	 * </p>
	 *
	 * @param account
	 *        The account to register the passkey for.
	 * @return The started registration, to be passed to
	 *         {@link #finishRegistration(PendingRegistration, String, String)}.
	 * @throws TopLogicException
	 *         If passkeys are not {@link #isAvailable() set up}
	 *         ({@link I18NConstants#ERROR_NOT_SET_UP}), or the account must not use passkeys: the
	 *         anonymous account and an account with {@link MfaRequirement#DISABLED}.
	 */
	public PendingRegistration startRegistration(Person account) {
		RelyingParty relyingParty = relyingParty();
		RelyingPartyAddress address = _address;
		checkMayUsePasskeys(account);
		UserIdentity user = UserIdentity.builder()
			.name(account.getName())
			.displayName(displayName(account))
			.id(PasskeyCredentialRepository.userHandle(account))
			.build();
		PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
			StartRegistrationOptions.builder()
				.user(user)
				.authenticatorSelection(AuthenticatorSelectionCriteria.builder()
					.residentKey(ResidentKeyRequirement.REQUIRED)
					.userVerification(getConfig().getUserVerification())
					.build())
				.timeout(getConfig().getTimeout())
				.build());
		return new PendingRegistration(relyingParty, address, account, getConfig().getTimeout(), options);
	}

	/**
	 * Completes a registration with the credential the browser created and stores the passkey.
	 *
	 * <p>
	 * Every refusal after the credential could be parsed, except
	 * {@link Category#ALREADY_REGISTERED}, reports the credential as
	 * {@link PasskeyRefusedException#isCredentialUnknown() unknown}: the authenticator has already
	 * stored a key the application does not keep.
	 * </p>
	 *
	 * @param pending
	 *        The registration started with {@link #startRegistration(Person)}.
	 * @param credentialJson
	 *        The JSON form of the created credential (<code>PublicKeyCredential.toJSON()</code>).
	 * @param label
	 *        The name the user gives the passkey.
	 * @return The stored passkey, a part of the account of the registration.
	 * @throws PasskeyRefusedException
	 *         If the credential cannot be parsed, the ceremony ran on an origin the relying party
	 *         does not accept, the registration was answered before or has expired, the label is
	 *         empty, the account must not use passkeys, the authenticator did not verify the user
	 *         although required, the credential is registered already, or the credential does not
	 *         pass the checks of the library.
	 */
	public Passkey finishRegistration(PendingRegistration pending, String credentialJson, String label) {
		boolean open = pending.answer();
		Person account = pending.getAccount();
		Refusal refusal = new Refusal(REGISTRATION, true);
		refusal.account("account", account);

		PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;
		try {
			credential = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
		} catch (IOException | RuntimeException ex) {
			throw refusal.refuse(Category.INVALID_DATA, I18NConstants.ERROR_INVALID_CEREMONY_DATA,
				"Unparsable credential.", ex);
		}
		refusal.credential(credential.getId(), credential.getResponse());

		checkOrigin(pending, refusal);
		if (!open) {
			throw refusal.refuse(Category.EXPIRED, I18NConstants.ERROR_REQUEST_EXPIRED,
				"The registration was answered before or has expired.", null);
		}
		if (StringServices.isEmpty(label) || label.isBlank()) {
			throw refusal.refuse(Category.LABEL_MISSING, I18NConstants.ERROR_LABEL_MISSING,
				"No name for the passkey.", null);
		}
		if (!mayUsePasskeys(account)) {
			throw refusal.refuse(Category.NOT_ALLOWED,
				I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(account == null ? "" : account.getName()),
				"The account must not use passkeys.", null);
		}
		if (getConfig().getUserVerification() == UserVerificationRequirement.REQUIRED) {
			checkUserVerified(credential.getResponse(), refusal);
		}
		if (PasskeyCredentialRepository.findPasskey(credential.getId()) != null) {
			throw refusal.refuse(Category.ALREADY_REGISTERED, I18NConstants.ERROR_ALREADY_REGISTERED,
				"A passkey with this credential ID is already registered.", null);
		}

		RegistrationResult result;
		try {
			result = pending.getRelyingParty().finishRegistration(FinishRegistrationOptions.builder()
				.request(pending.getRequest())
				.response(credential)
				.build());
		} catch (RegistrationFailedException ex) {
			throw refusal.refuseByLibrary(ex);
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(
			ResKey.text("Register passkey"))) {
			Passkey passkey = TlSecurityPasskeyFactory.getInstance().createPasskey();
			passkey.setLabel(label.strip());
			passkey.setCredentialId(result.getKeyId().getId().getBase64Url());
			passkey.setPublicKey(result.getPublicKeyCose().getBase64Url());
			passkey.setSignatureCount(result.getSignatureCount());
			passkey.setTransports(PasskeyCredentialRepository.formatTransports(
				credential.getResponse().getTransports()));
			passkey.setBackupEligible(result.isBackupEligible());
			passkey.setBackedUp(result.isBackedUp());
			passkey.setCreatedAt(new Date());
			account.tAddByName(PasskeyCredentialRepository.PASSKEYS_ATTR, passkey);
			tx.commit();
			return passkey;
		}
	}

	/**
	 * Starts a login with a passkey.
	 *
	 * @param account
	 *        The account that logs in, if already known (the passkey as second factor after the
	 *        password): only its passkeys are offered. <code>null</code> for a login with a passkey
	 *        alone, in which the user picks a discoverable passkey of any account.
	 * @return The started login, to be passed to
	 *         {@link #finishAuthentication(PendingAuthentication, String)}.
	 * @throws TopLogicException
	 *         If passkeys are not {@link #isAvailable() set up}
	 *         ({@link I18NConstants#ERROR_NOT_SET_UP}), or the given account is the anonymous
	 *         account.
	 */
	public PendingAuthentication startAuthentication(Person account) {
		RelyingParty relyingParty = relyingParty();
		RelyingPartyAddress address = _address;
		if (account != null) {
			checkNotAnonymous(account);
		}
		StartAssertionOptions.StartAssertionOptionsBuilder options = StartAssertionOptions.builder()
			.timeout(getConfig().getTimeout());
		if (account == null) {
			options.userVerification(UserVerificationRequirement.REQUIRED);
		} else {
			options.userHandle(PasskeyCredentialRepository.userHandle(account));
			options.userVerification(getConfig().getUserVerification());
		}
		AssertionRequest assertion = relyingParty.startAssertion(options.build());
		return new PendingAuthentication(relyingParty, address, account, getConfig().getTimeout(), assertion);
	}

	/**
	 * Completes a login with the credential the browser answered with.
	 *
	 * <p>
	 * On success, the signature counter, the backup state and the time of last use of the passkey
	 * are updated. A refusal for {@link Category#UNKNOWN_PASSKEY} - no account has a passkey with
	 * the credential ID, or the user handle of the credential is not the one of the account of that
	 * passkey - reports the credential as {@link PasskeyRefusedException#isCredentialUnknown()
	 * unknown}.
	 * </p>
	 *
	 * @param pending
	 *        The login started with {@link #startAuthentication(Person)}.
	 * @param credentialJson
	 *        The JSON form of the assertion (<code>PublicKeyCredential.toJSON()</code>).
	 * @return The account the passkey belongs to. If the login was started for an account, it is
	 *         that account.
	 * @throws PasskeyRefusedException
	 *         If the credential cannot be parsed, the ceremony ran on an origin the relying party
	 *         does not accept, the login was answered before or has expired, the passkey is unknown
	 *         or belongs to another account than the one given at the start, the user was not
	 *         verified although required, the signature counter did not increase, or the signature
	 *         or another check of the library fails.
	 */
	public Person finishAuthentication(PendingAuthentication pending, String credentialJson) {
		boolean open = pending.answer();
		Person expected = pending.getAccount();
		Refusal refusal = new Refusal(LOGIN, false);
		refusal.account("expected account", expected);

		PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;
		try {
			credential = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
		} catch (IOException | RuntimeException ex) {
			throw refusal.refuse(Category.INVALID_DATA, I18NConstants.ERROR_INVALID_CEREMONY_DATA,
				"Unparsable credential.", ex);
		}
		refusal.credential(credential.getId(), credential.getResponse());

		Optional<ByteArray> userHandle = credential.getResponse().getUserHandle();
		if (userHandle.isPresent()) {
			refusal.account("account of the user handle",
				PasskeyCredentialRepository.accountByUserHandle(userHandle.get()));
		}
		Passkey passkey = PasskeyCredentialRepository.findPasskey(credential.getId());
		Person owner = passkey == null ? null : PasskeyCredentialRepository.getOwner(passkey);
		refusal.account("owner of the passkey", owner);

		checkOrigin(pending, refusal);
		if (!open) {
			throw refusal.refuse(Category.EXPIRED, I18NConstants.ERROR_REQUEST_EXPIRED,
				"The login was answered before or has expired.", null);
		}
		if (owner == null || !owner.isAlive()) {
			throw refusal.refuseUnknown(I18NConstants.ERROR_UNKNOWN_PASSKEY,
				"No account has a passkey with this credential ID.");
		}
		if (userHandle.isPresent() && !userHandle.get().equals(PasskeyCredentialRepository.userHandle(owner))) {
			throw refusal.refuseUnknown(I18NConstants.ERROR_UNKNOWN_PASSKEY,
				"The user handle of the credential is not the one of the owner of the passkey.");
		}
		if (expected != null && !expected.equals(owner)) {
			throw refusal.refuse(Category.OTHER_ACCOUNT,
				I18NConstants.ERROR_OTHER_ACCOUNT__ACCOUNT.fill(expected.getName()),
				"The passkey belongs to another account than the one of the login.", null);
		}
		if (pending.requiresUserVerification()) {
			checkUserVerified(credential.getResponse(), refusal);
		}

		AssertionResult result;
		try {
			result = pending.getRelyingParty().finishAssertion(FinishAssertionOptions.builder()
				.request(pending.getRequest())
				.response(credential)
				.build());
		} catch (InvalidSignatureCountException ex) {
			throw refusal.refuse(Category.SIGNATURE_COUNTER, I18NConstants.ERROR_SIGNATURE_COUNTER,
				"Signature counter did not increase: expected at least " + ex.getExpectedMinimum() + ", received "
					+ ex.getReceived() + ".",
				ex);
		} catch (AssertionFailedException ex) {
			throw refusal.refuseByLibrary(ex);
		}
		if (!result.isSuccess()) {
			throw refusal.refuse(Category.FAILED,
				I18NConstants.ERROR_AUTHENTICATION_FAILED__REASON.fill("Assertion not successful."),
				"Assertion not successful.", null);
		}
		if (!result.isSignatureCounterValid()) {
			throw refusal.refuse(Category.SIGNATURE_COUNTER, I18NConstants.ERROR_SIGNATURE_COUNTER,
				"Signature counter did not increase.", null);
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(
			ResKey.text("Login with passkey"))) {
			passkey.setSignatureCount(result.getSignatureCount());
			passkey.setBackedUp(result.isBackedUp());
			passkey.setLastUsed(new Date());
			tx.commit();
		}
		return owner;
	}

	/**
	 * The ID of the credential the browser created in a registration.
	 *
	 * @param credentialJson
	 *        The JSON form of the created credential (<code>PublicKeyCredential.toJSON()</code>),
	 *        unverified.
	 * @return The base64url-encoded credential ID, <code>null</code> if the value cannot be parsed.
	 */
	public static String credentialIdOfRegistration(String credentialJson) {
		try {
			return PublicKeyCredential.parseRegistrationResponseJson(credentialJson).getId().getBase64Url();
		} catch (IOException | RuntimeException ex) {
			return null;
		}
	}

	/**
	 * Whether the given account has a passkey.
	 */
	public boolean isEnrolled(Person account) {
		return !getPasskeys(account).isEmpty();
	}

	/**
	 * The passkeys of the given account.
	 */
	public List<Passkey> getPasskeys(Person account) {
		return PasskeyCredentialRepository.getPasskeys(account);
	}

	/**
	 * The base64url-encoded user handle identifying the given account towards the authenticators.
	 *
	 * @see PasskeyCredentialRepository#userHandle(Person)
	 */
	public String getUserId(Person account) {
		return PasskeyCredentialRepository.userHandle(account).getBase64Url();
	}

	/**
	 * The base64url-encoded credential IDs of all passkeys of the given account.
	 */
	public List<String> getCredentialIds(Person account) {
		List<String> result = new ArrayList<>();
		for (Passkey passkey : getPasskeys(account)) {
			result.add(passkey.getCredentialId());
		}
		return result;
	}

	/**
	 * Whether the given account may register passkeys.
	 *
	 * <p>
	 * Not the anonymous account and not an account with {@link MfaRequirement#DISABLED}.
	 * </p>
	 */
	public boolean mayUsePasskeys(Person account) {
		return account != null
			&& !PersonManager.getManager().isAnonymous(account)
			&& account.getMFARequirement() != MfaRequirement.DISABLED;
	}

	private void checkMayUsePasskeys(Person account) {
		if (!mayUsePasskeys(account)) {
			throw new TopLogicException(
				I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(account == null ? "" : account.getName()));
		}
	}

	private void checkNotAnonymous(Person account) {
		if (PersonManager.getManager().isAnonymous(account)) {
			throw new TopLogicException(I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(account.getName()));
		}
	}

	private static void checkOrigin(PendingCeremony pending, Refusal refusal) {
		String origin = refusal.getClientOrigin();
		if (!pending.getAddress().accepts(origin)) {
			throw refusal.refuse(Category.WRONG_ORIGIN,
				I18NConstants.ERROR_WRONG_ORIGIN__ORIGIN.fill(StringServices.nonNull(origin)),
				"The origin of the page is not accepted, accepted origins: " + pending.getAddress().getOrigins()
					+ ". Set the public address of the application with '" + HOST_VARIABLE + "' ("
					+ AliasManager.HOST + ") or the option '" + Config.ORIGINS + "' of PasskeyService.",
				null);
		}
	}

	private static void checkUserVerified(AuthenticatorResponse response, Refusal refusal) {
		if (!response.getParsedAuthenticatorData().getFlags().UV) {
			throw refusal.refuse(Category.USER_NOT_VERIFIED, I18NConstants.ERROR_USER_NOT_VERIFIED,
				"The authenticator did not verify the user, although required.", null);
		}
	}

	private RelyingParty relyingParty() {
		RelyingParty relyingParty = _relyingParty;
		if (relyingParty == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_SET_UP);
		}
		return relyingParty;
	}

	private RelyingParty createRelyingParty(RelyingPartyAddress address) {
		return RelyingParty.builder()
			.identity(RelyingPartyIdentity.builder()
				.id(address.getRpId())
				.name(rpName())
				.build())
			.credentialRepository(_repository)
			.origins(address.getOrigins())
			// Passkeys of platforms and password managers come without attestation; the relying
			// party does not restrict the authenticator models.
			.attestationConveyancePreference(AttestationConveyancePreference.NONE)
			.allowUntrustedAttestation(true)
			.validateSignatureCounter(true)
			.preferredPubkeyParams(PUBKEY_PARAMS)
			.build();
	}

	/**
	 * The signature algorithms offered to an authenticator, in order of preference.
	 *
	 * <p>
	 * The algorithms the JVM verifies without an additional security provider. The library's
	 * default also lists ML-DSA, which the JVM cannot verify.
	 * </p>
	 */
	private static final List<PublicKeyCredentialParameters> PUBKEY_PARAMS = List.of(
		PublicKeyCredentialParameters.ES256,
		PublicKeyCredentialParameters.EdDSA,
		PublicKeyCredentialParameters.ES384,
		PublicKeyCredentialParameters.ES512,
		PublicKeyCredentialParameters.RS256);

	private String rpName() {
		ResKey name = getConfig().getRpName();
		return Resources.getSystemInstance().getString(
			name != null ? name : com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
	}

	private static String displayName(Person account) {
		String fullName = account.getFullName();
		return StringServices.isEmpty(fullName) ? account.getName() : fullName;
	}

	/**
	 * Collects what is known about a refused credential and creates the
	 * {@link PasskeyRefusedException}, logging the refusal.
	 */
	private static final class Refusal {

		private final String _ceremony;

		private final boolean _registration;

		private final StringBuilder _accounts = new StringBuilder();

		private String _credentialId;

		private String _clientOrigin;

		Refusal(String ceremony, boolean registration) {
			_ceremony = ceremony;
			_registration = registration;
		}

		void account(String role, Person account) {
			if (account == null) {
				return;
			}
			if (_accounts.length() > 0) {
				_accounts.append(", ");
			}
			_accounts.append(role).append(" '").append(account.getName()).append('\'');
		}

		void credential(ByteArray credentialId, AuthenticatorResponse response) {
			_credentialId = credentialId.getBase64Url();
			try {
				_clientOrigin = response.getClientData().getOrigin();
			} catch (RuntimeException ex) {
				_clientOrigin = null;
			}
		}

		String getClientOrigin() {
			return _clientOrigin;
		}

		PasskeyRefusedException refuseUnknown(ResKey message, String reason) {
			return create(Category.UNKNOWN_PASSKEY, message, reason, null, true);
		}

		PasskeyRefusedException refuse(Category category, ResKey message, String reason, Throwable cause) {
			// The authenticator has stored the key of a refused registration, which the application
			// does not keep - unless it is the key of a passkey registered before.
			boolean unknown = _registration && category != Category.ALREADY_REGISTERED;
			return create(category, message, reason, cause, unknown);
		}

		PasskeyRefusedException refuseByLibrary(Exception ex) {
			String reason = reason(ex);
			ResKey message = _registration ? I18NConstants.ERROR_REGISTRATION_FAILED__REASON.fill(reason)
				: I18NConstants.ERROR_AUTHENTICATION_FAILED__REASON.fill(reason);
			return refuse(Category.FAILED, message, reason, ex);
		}

		private PasskeyRefusedException create(Category category, ResKey message, String reason, Throwable cause,
				boolean unknown) {
			StringBuilder log = new StringBuilder();
			log.append(_ceremony).append(" refused (").append(category).append("): ").append(reason);
			if (cause != null && !reason.equals(reason(cause))) {
				log.append(" Library: ").append(reason(cause));
			}
			log.append(" Credential ID: ").append(_credentialId == null ? "unknown" : _credentialId);
			if (_accounts.length() > 0) {
				log.append(", ").append(_accounts);
			}
			log.append(", origin of the page: ").append(_clientOrigin == null ? "unknown" : _clientOrigin);
			log.append('.');
			Logger.warn(log.toString(), PasskeyService.class);
			return new PasskeyRefusedException(category, message, _credentialId, unknown, cause);
		}

		/**
		 * The message of the given exception with the messages of its causes.
		 */
		private static String reason(Throwable ex) {
			StringBuilder result = new StringBuilder(String.valueOf(ex.getMessage()));
			Throwable cause = ex.getCause();
			while (cause != null && cause != ex) {
				if (cause.getMessage() != null && !result.toString().contains(cause.getMessage())) {
					result.append(" (").append(cause.getMessage()).append(')');
				}
				ex = cause;
				cause = cause.getCause();
			}
			return result.toString();
		}
	}

	/**
	 * The {@link PasskeyService} of the application.
	 */
	public static PasskeyService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link PasskeyService}.
	 */
	public static final class Module extends TypedRuntimeModule<PasskeyService> {

		/**
		 * Singleton {@link PasskeyService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<PasskeyService> getImplementation() {
			return PasskeyService.class;
		}
	}

}
