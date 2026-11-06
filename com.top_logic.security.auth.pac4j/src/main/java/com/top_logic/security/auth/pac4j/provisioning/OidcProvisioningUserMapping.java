/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalLoginListener;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ExternalUserMapping} for the <code>user-mapping</code> of a pac4j client that links the
 * accounts to the identities of the identity provider and creates the accounts on demand.
 *
 * <p>
 * The account of an {@link ExternalIdentity} is the first one found in this order:
 * </p>
 * <ol>
 * <li>the account carrying an {@link OidcIdentity} with the
 * {@link ExternalIdentity#getClientName() client} and {@link ExternalIdentity#getSubject()
 * subject} of the identity,</li>
 * <li>with {@link Config#getLinkByEmail()}: the single account with the e-mail address of the
 * identity, if the identity provider has confirmed it,</li>
 * <li>with {@link Config#getLinkByName()}: the account with the {@link #getLoginName(ExternalIdentity)
 * login name} of the identity,</li>
 * <li>with {@link Config#getCreateAccounts()}: a new account.</li>
 * </ol>
 *
 * <p>
 * An account found by e-mail or name gets the identity attached, a new account as well. If the
 * account already carries another identity of the same client, the login is refused. Without an
 * account the login is refused with an {@link UnknownAccountException}.
 * </p>
 *
 * <p>
 * After the account is known, the {@link Config#getListeners() listeners} are called in the same
 * transaction. An exception rolls the transaction back and refuses the login.
 * </p>
 *
 * <p>
 * The e-mail address of an account is the {@link UserInterface#getEMail() e-mail} of its
 * {@link Person#getUser() user information}, which exists only with the contact module. Without
 * it, a created account gets only its first and last name, and {@link Config#getLinkByEmail()}
 * finds no account.
 * </p>
 */
@Label("OIDC account provisioning")
public class OidcProvisioningUserMapping extends AbstractConfiguredInstance<OidcProvisioningUserMapping.Config>
		implements ExternalUserMapping {

	/**
	 * Name of the OIDC claim with the login name the user prefers.
	 */
	public static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

	/**
	 * Name of the OIDC claim with the first name of the user.
	 */
	public static final String GIVEN_NAME_CLAIM = "given_name";

	/**
	 * Name of the OIDC claim with the last name of the user.
	 */
	public static final String FAMILY_NAME_CLAIM = "family_name";

	/**
	 * Name of the OIDC claim with the e-mail address of the user.
	 */
	public static final String EMAIL_CLAIM = "email";

	/**
	 * Name of the OIDC claim telling whether the identity provider has confirmed the e-mail
	 * address of the user.
	 */
	public static final String EMAIL_VERIFIED_CLAIM = "email_verified";

	/**
	 * Name of the attribute of <code>tl.accounts:Person</code> with the first name of an account
	 * without {@link Person#getUser() user information}.
	 */
	public static final String FIRST_NAME_ATTR = "firstName";

	/**
	 * Name of the attribute of <code>tl.accounts:Person</code> with the last name of an account
	 * without {@link Person#getUser() user information}.
	 */
	public static final String LAST_NAME_ATTR = "lastName";

	/**
	 * Separator between the login name and the client name of an account created under another
	 * name, see {@link NameConflict#RENAME}.
	 */
	public static final char CLIENT_SEPARATOR = '@';

	/**
	 * Separator before the counter of an account created under another name, see
	 * {@link NameConflict#RENAME}.
	 */
	public static final char COUNTER_SEPARATOR = '-';

	/**
	 * Configuration options of {@link OidcProvisioningUserMapping}.
	 */
	public interface Config extends PolymorphicConfiguration<OidcProvisioningUserMapping> {

		/**
		 * Configuration name of {@link #getLinkByEmail()}.
		 */
		String LINK_BY_EMAIL = "link-by-email";

		/**
		 * Configuration name of {@link #getLinkByName()}.
		 */
		String LINK_BY_NAME = "link-by-name";

		/**
		 * Configuration name of {@link #getCreateAccounts()}.
		 */
		String CREATE_ACCOUNTS = "create-accounts";

		/**
		 * Configuration name of {@link #getNameConflict()}.
		 */
		String NAME_CONFLICT = "name-conflict";

		/**
		 * Configuration name of {@link #getRequireVerifiedEmail()}.
		 */
		String REQUIRE_VERIFIED_EMAIL = "require-verified-email";

		/**
		 * Configuration name of {@link #getAllowedEmailDomains()}.
		 */
		String ALLOWED_EMAIL_DOMAINS = "allowed-email-domains";

		/**
		 * Configuration name of {@link #getAuthenticationDevice()}.
		 */
		String AUTHENTICATION_DEVICE = "authentication-device";

		/**
		 * Configuration name of {@link #getLoginNameClaim()}.
		 */
		String LOGIN_NAME_CLAIM = "login-name-claim";

		/**
		 * Configuration name of {@link #getFirstNameClaim()}.
		 */
		String FIRST_NAME_CLAIM = "first-name-claim";

		/**
		 * Configuration name of {@link #getLastNameClaim()}.
		 */
		String LAST_NAME_CLAIM = "last-name-claim";

		/**
		 * Configuration name of {@link #getEmailClaim()}.
		 */
		String EMAIL_CLAIM = "email-claim";

		/**
		 * Configuration name of {@link #getEmailVerifiedClaim()}.
		 */
		String EMAIL_VERIFIED_CLAIM = "email-verified-claim";

		/**
		 * Configuration name of {@link #getListeners()}.
		 */
		String LISTENERS = "listeners";

		/**
		 * Whether an identity without account is linked to the account with its e-mail address.
		 *
		 * <p>
		 * Only if the identity provider has confirmed the address (claim
		 * {@link #getEmailVerifiedClaim()}) and exactly one account has it. Switch it on only for
		 * an identity provider whose confirmation of e-mail addresses is trusted: whoever controls
		 * an address there gets the account with that address.
		 * </p>
		 */
		@Name(LINK_BY_EMAIL)
		boolean getLinkByEmail();

		/**
		 * Whether an identity without account is linked to the account with its
		 * {@link OidcProvisioningUserMapping#getLoginName(ExternalIdentity) login name}.
		 */
		@Name(LINK_BY_NAME)
		@BooleanDefault(true)
		boolean getLinkByName();

		/**
		 * Whether an account is created for an identity for which no account is found.
		 *
		 * <p>
		 * The account has no password and logs in only through the external login.
		 * </p>
		 */
		@Name(CREATE_ACCOUNTS)
		boolean getCreateAccounts();

		/**
		 * What happens when the login name of an account to create is taken by an account the
		 * identity is not linked to.
		 */
		@Name(NAME_CONFLICT)
		NameConflict getNameConflict();

		/**
		 * Whether an account is created only for an identity whose e-mail address the identity
		 * provider has confirmed (claim {@link #getEmailVerifiedClaim()}).
		 */
		@Name(REQUIRE_VERIFIED_EMAIL)
		boolean getRequireVerifiedEmail();

		/**
		 * The domains of the e-mail addresses for which accounts are created, e.g.
		 * <code>example.com</code>.
		 *
		 * <p>
		 * Empty: accounts are created for every e-mail address, and for identities without one.
		 * The domains are compared ignoring case.
		 * </p>
		 */
		@Name(ALLOWED_EMAIL_DOMAINS)
		@Format(CommaSeparatedStrings.class)
		List<String> getAllowedEmailDomains();

		/**
		 * The ID of the authentication device of a created account.
		 *
		 * <p>
		 * Empty: the account has no authentication device and can log in only through an external
		 * login.
		 * </p>
		 */
		@Name(AUTHENTICATION_DEVICE)
		String getAuthenticationDevice();

		/**
		 * The claim with the login name of the account.
		 *
		 * <p>
		 * The name of an account to create, and the name an identity is linked by
		 * ({@link #getLinkByName()}). Without this claim, the
		 * {@link ExternalIdentity#getName() name} the client extracts is used.
		 * </p>
		 */
		@Name(LOGIN_NAME_CLAIM)
		@StringDefault(PREFERRED_USERNAME_CLAIM)
		String getLoginNameClaim();

		/**
		 * The claim with the first name of a created account.
		 */
		@Name(FIRST_NAME_CLAIM)
		@StringDefault(GIVEN_NAME_CLAIM)
		String getFirstNameClaim();

		/**
		 * The claim with the last name of a created account.
		 */
		@Name(LAST_NAME_CLAIM)
		@StringDefault(FAMILY_NAME_CLAIM)
		String getLastNameClaim();

		/**
		 * The claim with the e-mail address of the identity.
		 */
		@Name(EMAIL_CLAIM)
		@StringDefault(OidcProvisioningUserMapping.EMAIL_CLAIM)
		String getEmailClaim();

		/**
		 * The claim telling whether the identity provider has confirmed the e-mail address.
		 */
		@Name(EMAIL_VERIFIED_CLAIM)
		@StringDefault(OidcProvisioningUserMapping.EMAIL_VERIFIED_CLAIM)
		String getEmailVerifiedClaim();

		/**
		 * The {@link ExternalLoginListener}s called at every login.
		 *
		 * <p>
		 * They are called in the transaction of the login, after the account has been found or
		 * created.
		 * </p>
		 */
		@Name(LISTENERS)
		List<PolymorphicConfiguration<? extends ExternalLoginListener>> getListeners();

	}

	private final List<ExternalLoginListener> _listeners;

	private final Set<String> _allowedDomains;

	/**
	 * Creates a {@link OidcProvisioningUserMapping} from configuration.
	 *
	 * @param context
	 *        The context for instantiating the configured parts.
	 * @param config
	 *        The configuration.
	 */
	public OidcProvisioningUserMapping(InstantiationContext context, Config config) {
		super(context, config);
		_listeners = TypedConfiguration.getInstanceListReadOnly(context, config.getListeners());
		_allowedDomains = config.getAllowedEmailDomains().stream()
			.map(String::strip)
			.filter(domain -> !domain.isEmpty())
			.map(domain -> domain.toLowerCase(Locale.ROOT))
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Person findAccountForExternalName(String externalUserName) throws LoginDeniedException {
		return ExternalUserMapping.findAccount(externalUserName);
	}

	@Override
	public Person findAccountForIdentity(ExternalIdentity identity) throws LoginDeniedException {
		if (StringServices.isEmpty(identity.getClientName()) || StringServices.isEmpty(identity.getSubject())) {
			throw new LoginDeniedException("Identity without client or subject: " + identity);
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.EXTERNAL_LOGIN)) {
			boolean created = false;
			Person account = findAccount(kb, identity);
			if (account == null) {
				account = createAccount(kb, identity);
				created = true;
			}
			if (created) {
				for (ExternalLoginListener listener : _listeners) {
					listener.onCreate(account, identity);
				}
			}
			for (ExternalLoginListener listener : _listeners) {
				listener.onLogin(account, identity);
			}
			tx.commit();
			return account;
		}
	}

	/**
	 * The existing account of the given identity, attaching the identity to an account found by
	 * e-mail or name.
	 *
	 * @return The account, <code>null</code> if none was found.
	 */
	private Person findAccount(KnowledgeBase kb, ExternalIdentity identity) {
		String client = identity.getClientName();
		OidcIdentity known = OidcIdentities.findIdentity(kb, client, identity.getSubject());
		if (known != null) {
			Person owner = OidcIdentities.getOwner(known);
			if (owner != null) {
				return owner;
			}
		}

		if (getConfig().getLinkByEmail() && isEmailVerified(identity)) {
			Person account = findByEmail(getEmail(identity));
			if (account != null) {
				return link(account, identity);
			}
		}

		if (getConfig().getLinkByName()) {
			String loginName = getLoginName(identity);
			Person account = StringServices.isEmpty(loginName) ? null : Person.byName(kb, loginName);
			if (account != null) {
				return link(account, identity);
			}
		}

		return null;
	}

	private Person link(Person account, ExternalIdentity identity) {
		String client = identity.getClientName();
		if (OidcIdentities.getIdentity(account, client) != null) {
			throw new LoginDeniedException(I18NConstants.ERROR_OTHER_IDENTITY__NAME.fill(account.getName()),
				"Account '" + account.getName() + "' already has another identity of client '" + client
					+ "', refusing " + identity + ".");
		}
		OidcIdentities.addIdentity(account, client, identity.getSubject());
		return account;
	}

	private Person createAccount(KnowledgeBase kb, ExternalIdentity identity) {
		String loginName = getLoginName(identity);
		if (!getConfig().getCreateAccounts()) {
			throw new UnknownAccountException(loginName, "No account for " + identity + ".");
		}
		if (StringServices.isEmpty(loginName)) {
			throw new LoginDeniedException(I18NConstants.ERROR_NO_LOGIN_NAME,
				"No login name for creating an account for " + identity + ".");
		}
		checkEmail(identity);

		String accountName = accountName(kb, loginName, identity);
		AuthenticationDevice device = authenticationDevice();
		Person account;
		try {
			account = Person.create(kb, accountName, device);
		} catch (TopLogicException ex) {
			throw new LoginDeniedException(ex.getErrorKey(),
				"Cannot create account '" + accountName + "' for " + identity + ": " + ex.getMessage());
		}
		String firstName = getClaimString(identity, getConfig().getFirstNameClaim());
		String lastName = getClaimString(identity, getConfig().getLastNameClaim());
		String email = getEmail(identity);
		UserInterface user = account.getUser();
		if (user != null) {
			if (firstName != null) {
				user.setFirstName(firstName);
			}
			if (lastName != null) {
				user.setName(lastName);
			}
			if (email != null) {
				user.setEMail(email);
			}
		} else {
			account.tUpdateByName(FIRST_NAME_ATTR, firstName);
			account.tUpdateByName(LAST_NAME_ATTR, lastName);
		}
		OidcIdentities.addIdentity(account, identity.getClientName(), identity.getSubject());
		return account;
	}

	private void checkEmail(ExternalIdentity identity) {
		String email = getEmail(identity);
		if (getConfig().getRequireVerifiedEmail()) {
			if (email == null) {
				throw new LoginDeniedException(I18NConstants.ERROR_NO_EMAIL,
					"No e-mail address for creating an account for " + identity + ".");
			}
			if (!isEmailVerified(identity)) {
				throw new LoginDeniedException(I18NConstants.ERROR_EMAIL_NOT_VERIFIED,
					"E-mail address not verified, not creating an account for " + identity + ".");
			}
		}
		if (!_allowedDomains.isEmpty()) {
			if (email == null) {
				throw new LoginDeniedException(I18NConstants.ERROR_NO_EMAIL,
					"No e-mail address for creating an account for " + identity + ".");
			}
			String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
			if (!_allowedDomains.contains(domain)) {
				throw new LoginDeniedException(I18NConstants.ERROR_EMAIL_DOMAIN_NOT_ALLOWED__DOMAIN.fill(domain),
					"E-mail domain '" + domain + "' not allowed, not creating an account for " + identity + ".");
			}
		}
	}

	/**
	 * The name of the account to create for the given login name.
	 */
	private String accountName(KnowledgeBase kb, String loginName, ExternalIdentity identity) {
		if (Person.byName(kb, loginName) == null) {
			return loginName;
		}
		switch (getConfig().getNameConflict()) {
			case RENAME: {
				String base = loginName + CLIENT_SEPARATOR + identity.getClientName();
				String candidate = base;
				for (int counter = 2; Person.byName(kb, candidate) != null; counter++) {
					candidate = base + COUNTER_SEPARATOR + counter;
				}
				return candidate;
			}
			case REFUSE:
			default:
				throw new LoginDeniedException(I18NConstants.ERROR_NAME_TAKEN__NAME.fill(loginName),
					"Login name '" + loginName + "' is taken by an account not linked to " + identity + ".");
		}
	}

	private AuthenticationDevice authenticationDevice() {
		String deviceId = getConfig().getAuthenticationDevice();
		if (StringServices.isEmpty(deviceId)) {
			return null;
		}
		AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getAuthenticationDevice(deviceId);
		if (device == null) {
			throw new IllegalStateException("No authentication device '" + deviceId + "' configured.");
		}
		return device;
	}

	/**
	 * The single account with the given e-mail address, compared ignoring case.
	 *
	 * @return The account, <code>null</code> if no account or more than one account has the
	 *         address.
	 */
	private static Person findByEmail(String email) {
		if (email == null) {
			return null;
		}
		Person result = null;
		for (Person account : Person.all()) {
			UserInterface user = account.getUser();
			if (user != null && user.getEMail() != null && email.equalsIgnoreCase(user.getEMail().strip())) {
				if (result != null) {
					return null;
				}
				result = account;
			}
		}
		return result;
	}

	/**
	 * The login name of the given identity: the value of the claim {@link Config#getLoginNameClaim()},
	 * without it the {@link ExternalIdentity#getName() name} the client extracts.
	 */
	public String getLoginName(ExternalIdentity identity) {
		String claim = getClaimString(identity, getConfig().getLoginNameClaim());
		return claim != null ? claim : StringServices.nonEmpty(identity.getName());
	}

	private String getEmail(ExternalIdentity identity) {
		return getClaimString(identity, getConfig().getEmailClaim());
	}

	private boolean isEmailVerified(ExternalIdentity identity) {
		Object value = identity.getClaim(getConfig().getEmailVerifiedClaim());
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return value != null && Boolean.parseBoolean(value.toString().strip());
	}

	/**
	 * The value of the given claim as stripped, non-empty string.
	 *
	 * @return The value, <code>null</code> if the claim is not configured, missing or empty.
	 */
	private static String getClaimString(ExternalIdentity identity, String claimName) {
		if (StringServices.isEmpty(claimName)) {
			return null;
		}
		Object value = identity.getClaim(claimName);
		if (value == null) {
			return null;
		}
		return StringServices.nonEmpty(value.toString().strip());
	}

}
