/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.RelyingParty;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;

/**
 * A started passkey ceremony waiting for the answer of the browser.
 *
 * <p>
 * A ceremony is started on the server ({@link PasskeyService#startRegistration(Person)},
 * {@link PasskeyService#startAuthentication(Person)}), its {@link #toOptionsJson() options} are sent
 * to the browser, and the credential the browser answers with is passed to the matching
 * <code>finish</code> method of {@link PasskeyService} together with this object. The object holds
 * the challenge, so it must stay on the server - in the user's session, e.g. in the control or
 * dialog that runs the ceremony - and is answered at most once: a second answer, or an answer after
 * the timeout, is refused.
 * </p>
 */
public abstract class PendingCeremony {

	private final RelyingParty _relyingParty;

	private final Person _account;

	private final long _deadline;

	private boolean _answered;

	PendingCeremony(RelyingParty relyingParty, Person account, long timeout) {
		_relyingParty = relyingParty;
		_account = account;
		_deadline = System.currentTimeMillis() + timeout;
	}

	/**
	 * The account the ceremony was started for.
	 *
	 * @return The account, or <code>null</code> for a login that lets the user pick a discoverable
	 *         passkey of any account.
	 */
	public Person getAccount() {
		return _account;
	}

	/**
	 * The options for the browser in the form <code>{"publicKey": {...}}</code>, ready for
	 * <code>PublicKeyCredential.parseCreationOptionsFromJSON()</code> or
	 * <code>parseRequestOptionsFromJSON()</code> of the <code>publicKey</code> member.
	 */
	public final String toOptionsJson() {
		try {
			return optionsJson();
		} catch (JsonProcessingException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CEREMONY_DATA, ex);
		}
	}

	/**
	 * Implementation of {@link #toOptionsJson()}.
	 */
	protected abstract String optionsJson() throws JsonProcessingException;

	final RelyingParty getRelyingParty() {
		return _relyingParty;
	}

	/**
	 * Marks the ceremony as answered.
	 *
	 * @throws TopLogicException
	 *         If it was answered before or the timeout has passed.
	 */
	final synchronized void answer() {
		if (_answered || System.currentTimeMillis() > _deadline) {
			_answered = true;
			throw new TopLogicException(I18NConstants.ERROR_REQUEST_EXPIRED);
		}
		_answered = true;
	}

}
