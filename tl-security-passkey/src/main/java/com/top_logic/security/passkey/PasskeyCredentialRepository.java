/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.model.TlSecurityPasskeyFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * The {@link CredentialRepository} of the relying party: the {@link Passkey}s stored in the
 * knowledge base as parts of their accounts ({@link #PASSKEYS_ATTR}).
 *
 * <h2>User handle</h2>
 *
 * <p>
 * The user handle that identifies an account towards the authenticators is the UTF-8 encoding of
 * the external form of the account's object ID ({@link #userHandle(Person)}). It is stable for the
 * lifetime of the account, unique (an object ID is never reused) and needs no attribute of its
 * own. It is deliberately not derived from the login name: a login name may change, and
 * authenticators show the user handle in their diagnostics. The "user name" of the library (the
 * key of {@link #getCredentialIdsForUsername(String)}) is the login name of the account, which is
 * also what an authenticator shows as account name.
 * </p>
 */
public class PasskeyCredentialRepository implements CredentialRepository {

	/**
	 * The composite reference of {@link Person} holding the account's {@link Passkey}s.
	 */
	public static final String PASSKEYS_ATTR = "passkeys";

	/**
	 * Separator of the transport IDs in {@link Passkey#getTransports()}.
	 */
	static final String TRANSPORT_SEPARATOR = ",";

	@Override
	public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
		Person account = accountByName(username);
		if (account == null) {
			return Collections.emptySet();
		}
		Set<PublicKeyCredentialDescriptor> result = new LinkedHashSet<>();
		for (Passkey passkey : getPasskeys(account)) {
			ByteArray id = decodeOrNull(passkey.getCredentialId());
			if (id == null) {
				continue;
			}
			result.add(PublicKeyCredentialDescriptor.builder()
				.id(id)
				.transports(parseTransports(passkey.getTransports()))
				.build());
		}
		return result;
	}

	@Override
	public Optional<ByteArray> getUserHandleForUsername(String username) {
		return Optional.ofNullable(accountByName(username)).map(PasskeyCredentialRepository::userHandle);
	}

	@Override
	public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
		return Optional.ofNullable(accountByUserHandle(userHandle)).map(Person::getName);
	}

	@Override
	public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
		Passkey passkey = findPasskey(credentialId);
		if (passkey == null) {
			return Optional.empty();
		}
		Person owner = getOwner(passkey);
		if (owner == null || !userHandle(owner).equals(userHandle)) {
			return Optional.empty();
		}
		return Optional.of(toRegisteredCredential(passkey, owner));
	}

	@Override
	public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
		Passkey passkey = findPasskey(credentialId);
		if (passkey == null) {
			return Collections.emptySet();
		}
		Person owner = getOwner(passkey);
		if (owner == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(toRegisteredCredential(passkey, owner));
	}

	/**
	 * The {@link Passkey}s of the given account.
	 */
	public static List<Passkey> getPasskeys(Person account) {
		@SuppressWarnings("unchecked")
		Collection<Passkey> passkeys = (Collection<Passkey>) account.tValueByName(PASSKEYS_ATTR);
		return passkeys == null ? Collections.emptyList() : List.copyOf(passkeys);
	}

	/**
	 * The account a {@link Passkey} belongs to.
	 *
	 * @return The account, or <code>null</code> for a passkey that is not part of an account.
	 */
	public static Person getOwner(Passkey passkey) {
		TLObject container = passkey.tContainer();
		return container instanceof Person ? (Person) container : null;
	}

	/**
	 * The stored {@link Passkey} with the given credential ID.
	 *
	 * <p>
	 * One indexed query on the column {@link Passkey#CREDENTIAL_ID_ATTR}.
	 * </p>
	 *
	 * @return The passkey, or <code>null</code> if none has this ID.
	 */
	public static Passkey findPasskey(ByteArray credentialId) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(TlSecurityPasskeyFactory.KO_NAME_PASSKEY,
			Passkey.CREDENTIAL_ID_ATTR, credentialId.getBase64Url());
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	/**
	 * The user handle identifying the given account towards the authenticators.
	 *
	 * @see PasskeyCredentialRepository
	 */
	public static ByteArray userHandle(Person account) {
		String externalId = IdentifierUtil.toExternalForm(account.tId().getObjectName());
		return new ByteArray(externalId.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * The account with the given {@link #userHandle(Person) user handle}.
	 *
	 * @return The account, or <code>null</code> if no account has this user handle.
	 */
	public static Person accountByUserHandle(ByteArray userHandle) {
		String externalId = new String(userHandle.getBytes(), StandardCharsets.UTF_8);
		if (StringServices.isEmpty(externalId)) {
			return null;
		}
		TLID id;
		try {
			id = IdentifierUtil.fromExternalForm(externalId);
		} catch (RuntimeException ex) {
			// Not a handle of this application.
			return null;
		}
		KnowledgeObject handle = kb().getKnowledgeObject(Person.OBJECT_NAME, id);
		if (handle == null || !handle.isAlive()) {
			return null;
		}
		Object account = handle.getWrapper();
		return account instanceof Person ? (Person) account : null;
	}

	/**
	 * Encodes the given transports for {@link Passkey#getTransports()}.
	 */
	static String formatTransports(Set<AuthenticatorTransport> transports) {
		StringBuilder result = new StringBuilder();
		for (AuthenticatorTransport transport : transports) {
			if (result.length() > 0) {
				result.append(TRANSPORT_SEPARATOR);
			}
			result.append(transport.getId());
		}
		return result.toString();
	}

	/**
	 * Decodes the value of {@link Passkey#getTransports()}.
	 */
	static Set<AuthenticatorTransport> parseTransports(String transports) {
		Set<AuthenticatorTransport> result = new LinkedHashSet<>();
		if (transports != null) {
			for (String id : transports.split(TRANSPORT_SEPARATOR)) {
				String trimmed = id.trim();
				if (!trimmed.isEmpty()) {
					result.add(AuthenticatorTransport.of(trimmed));
				}
			}
		}
		return result;
	}

	/**
	 * Decodes a base64url value stored with a {@link Passkey}.
	 *
	 * @throws TopLogicException
	 *         If the value is not base64url.
	 */
	static ByteArray decode(String base64Url) {
		try {
			return ByteArray.fromBase64Url(base64Url);
		} catch (Base64UrlException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CEREMONY_DATA, ex);
		}
	}

	private static ByteArray decodeOrNull(String base64Url) {
		try {
			return decode(base64Url);
		} catch (TopLogicException ex) {
			Logger.warn("Passkey with an invalid credential ID: " + base64Url, ex, PasskeyCredentialRepository.class);
			return null;
		}
	}

	private static RegisteredCredential toRegisteredCredential(Passkey passkey, Person owner) {
		return RegisteredCredential.builder()
			.credentialId(decode(passkey.getCredentialId()))
			.userHandle(userHandle(owner))
			.publicKeyCose(decode(passkey.getPublicKey()))
			.signatureCount(passkey.getSignatureCount())
			.transports(parseTransports(passkey.getTransports()))
			.backupEligible(passkey.getBackupEligible())
			.backupState(passkey.getBackedUp())
			.build();
	}

	private static Person accountByName(String username) {
		if (StringServices.isEmpty(username)) {
			return null;
		}
		return Person.byName(kb(), username);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
