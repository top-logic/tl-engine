/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

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
import com.yubico.webauthn.exception.RegistrationFailedException;

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
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.model.TlSecurityPasskeyFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * The relying party of the passkeys (Web Authentication credentials) of the accounts: registers
 * passkeys and checks logins with them.
 *
 * <p>
 * The relying party ID and the accepted origins are either configured or taken from the request
 * that starts a registration or login: the host name of the request is then the relying party ID,
 * its scheme, host and port the only accepted origin. A local installation therefore works without
 * configuration; behind a reverse proxy that does not forward the original host, configure both.
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
		 * registered passkeys unusable. Empty: the host name of the request that starts a ceremony.
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
		 * Empty: the scheme, host and port of the request that starts a ceremony.
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

	private final PasskeyCredentialRepository _repository = new PasskeyCredentialRepository();

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

	/**
	 * Starts the registration of a passkey for the given account, with the relying party derived
	 * from the current request, if not configured.
	 *
	 * @see #startRegistration(Person, HttpServletRequest)
	 */
	public PendingRegistration startRegistration(Person account) {
		return startRegistration(account, currentRequest());
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
	 * @param request
	 *        The request to derive the relying party from, if not configured; may be
	 *        <code>null</code> if it is configured.
	 * @return The started registration, to be passed to
	 *         {@link #finishRegistration(PendingRegistration, String, String)}.
	 * @throws TopLogicException
	 *         If the account must not use passkeys: the anonymous account and an account with
	 *         {@link MfaRequirement#DISABLED}.
	 */
	public PendingRegistration startRegistration(Person account, HttpServletRequest request) {
		checkMayUsePasskeys(account);
		RelyingParty relyingParty = relyingParty(request);
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
		return new PendingRegistration(relyingParty, account, getConfig().getTimeout(), options);
	}

	/**
	 * Completes a registration with the credential the browser created and stores the passkey.
	 *
	 * @param pending
	 *        The registration started with {@link #startRegistration(Person, HttpServletRequest)}.
	 * @param credentialJson
	 *        The JSON form of the created credential (<code>PublicKeyCredential.toJSON()</code>).
	 * @param label
	 *        The name the user gives the passkey.
	 * @return The stored passkey, a part of the account of the registration.
	 * @throws TopLogicException
	 *         If the label is empty, the registration was answered before or has expired, or the
	 *         credential does not pass the checks of the relying party.
	 */
	public Passkey finishRegistration(PendingRegistration pending, String credentialJson, String label) {
		if (StringServices.isEmpty(label) || label.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_LABEL_MISSING);
		}
		pending.answer();

		Person account = pending.getAccount();
		checkMayUsePasskeys(account);

		PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;
		try {
			credential = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
		} catch (IOException | RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CEREMONY_DATA, ex);
		}

		RegistrationResult result;
		try {
			result = pending.getRelyingParty().finishRegistration(FinishRegistrationOptions.builder()
				.request(pending.getRequest())
				.response(credential)
				.build());
		} catch (RegistrationFailedException ex) {
			Logger.info("Registration of a passkey for '" + account.getName() + "' refused: " + ex.getMessage(),
				PasskeyService.class);
			throw new TopLogicException(I18NConstants.ERROR_REGISTRATION_FAILED, ex);
		}

		ByteArray credentialId = result.getKeyId().getId();
		if (PasskeyCredentialRepository.findPasskey(credentialId) != null) {
			throw new TopLogicException(I18NConstants.ERROR_ALREADY_REGISTERED);
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(
			ResKey.text("Register passkey"))) {
			Passkey passkey = TlSecurityPasskeyFactory.getInstance().createPasskey();
			passkey.setLabel(label.strip());
			passkey.setCredentialId(credentialId.getBase64Url());
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
	 * Starts a login with a passkey, with the relying party derived from the current request, if
	 * not configured.
	 *
	 * @see #startAuthentication(Person, HttpServletRequest)
	 */
	public PendingAuthentication startAuthentication(Person account) {
		return startAuthentication(account, currentRequest());
	}

	/**
	 * Starts a login with a passkey.
	 *
	 * @param account
	 *        The account that logs in, if already known (the passkey as second factor after the
	 *        password): only its passkeys are offered. <code>null</code> for a login with a passkey
	 *        alone, in which the user picks a discoverable passkey of any account.
	 * @param request
	 *        The request to derive the relying party from, if not configured; may be
	 *        <code>null</code> if it is configured.
	 * @return The started login, to be passed to
	 *         {@link #finishAuthentication(PendingAuthentication, String)}.
	 * @throws TopLogicException
	 *         If the given account is the anonymous account.
	 */
	public PendingAuthentication startAuthentication(Person account, HttpServletRequest request) {
		if (account != null) {
			checkNotAnonymous(account);
		}
		RelyingParty relyingParty = relyingParty(request);
		StartAssertionOptions.StartAssertionOptionsBuilder options = StartAssertionOptions.builder()
			.timeout(getConfig().getTimeout());
		if (account == null) {
			options.userVerification(UserVerificationRequirement.REQUIRED);
		} else {
			options.userHandle(PasskeyCredentialRepository.userHandle(account));
			options.userVerification(getConfig().getUserVerification());
		}
		AssertionRequest assertion = relyingParty.startAssertion(options.build());
		return new PendingAuthentication(relyingParty, account, getConfig().getTimeout(), assertion);
	}

	/**
	 * Completes a login with the credential the browser answered with.
	 *
	 * <p>
	 * On success, the signature counter, the backup state and the time of last use of the passkey
	 * are updated.
	 * </p>
	 *
	 * @param pending
	 *        The login started with {@link #startAuthentication(Person, HttpServletRequest)}.
	 * @param credentialJson
	 *        The JSON form of the assertion (<code>PublicKeyCredential.toJSON()</code>).
	 * @return The account the passkey belongs to. If the login was started for an account, it is
	 *         that account.
	 * @throws TopLogicException
	 *         If the login was answered before or has expired, the credential is unknown or belongs
	 *         to another account than the one given at the start, the signature is invalid, the
	 *         user was not verified although required, or the signature counter did not increase.
	 */
	public Person finishAuthentication(PendingAuthentication pending, String credentialJson) {
		pending.answer();

		PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;
		try {
			credential = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
		} catch (IOException | RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CEREMONY_DATA, ex);
		}

		AssertionResult result;
		try {
			result = pending.getRelyingParty().finishAssertion(FinishAssertionOptions.builder()
				.request(pending.getRequest())
				.response(credential)
				.build());
		} catch (AssertionFailedException ex) {
			throw authenticationFailed(ex.getMessage(), ex);
		}
		if (!result.isSuccess()) {
			throw authenticationFailed("Assertion not successful.", null);
		}
		if (!result.isSignatureCounterValid()) {
			throw authenticationFailed("Signature counter did not increase.", null);
		}
		if (pending.requiresUserVerification() && !result.isUserVerified()) {
			throw authenticationFailed("User not verified.", null);
		}

		Passkey passkey = PasskeyCredentialRepository.findPasskey(result.getCredentialId());
		Person owner = passkey == null ? null : PasskeyCredentialRepository.getOwner(passkey);
		if (owner == null || !owner.isAlive()) {
			throw authenticationFailed("Unknown passkey.", null);
		}
		Person expected = pending.getAccount();
		if (expected != null && !expected.equals(owner)) {
			throw authenticationFailed("Passkey of '" + owner.getName() + "' presented for '" + expected.getName()
				+ "'.", null);
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

	private static TopLogicException authenticationFailed(String reason, Throwable cause) {
		Logger.info("Login with passkey refused: " + reason, PasskeyService.class);
		return new TopLogicException(I18NConstants.ERROR_AUTHENTICATION_FAILED, cause);
	}

	private RelyingParty relyingParty(HttpServletRequest request) {
		String rpId = getConfig().getRpId();
		if (StringServices.isEmpty(rpId)) {
			if (request == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_RELYING_PARTY);
			}
			rpId = request.getServerName();
		}

		Set<String> origins = new LinkedHashSet<>(getConfig().getOrigins());
		if (origins.isEmpty()) {
			if (request == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_RELYING_PARTY);
			}
			origins.add(origin(request));
		}

		return RelyingParty.builder()
			.identity(RelyingPartyIdentity.builder()
				.id(rpId)
				.name(rpName())
				.build())
			.credentialRepository(_repository)
			.origins(origins)
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

	/**
	 * The origin of the given request as a browser sends it: scheme, host and the port, unless it
	 * is the default port of the scheme.
	 */
	static String origin(HttpServletRequest request) {
		String scheme = request.getScheme();
		int port = request.getServerPort();
		StringBuilder result = new StringBuilder();
		result.append(scheme).append("://").append(request.getServerName());
		boolean defaultPort = port <= 0
			|| ("http".equalsIgnoreCase(scheme) && port == 80)
			|| ("https".equalsIgnoreCase(scheme) && port == 443);
		if (!defaultPort) {
			result.append(':').append(port);
		}
		return result.toString();
	}

	private String rpName() {
		ResKey name = getConfig().getRpName();
		return Resources.getInstance().getString(
			name != null ? name : com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
	}

	private static String displayName(Person account) {
		String fullName = account.getFullName();
		return StringServices.isEmpty(fullName) ? account.getName() : fullName;
	}

	private static HttpServletRequest currentRequest() {
		if (!DefaultDisplayContext.hasDisplayContext()) {
			return null;
		}
		return DefaultDisplayContext.getDisplayContext().asRequest();
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
