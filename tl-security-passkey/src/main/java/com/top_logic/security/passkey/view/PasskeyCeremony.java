/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.function.BiConsumer;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl;
import com.top_logic.layout.react.control.webauthn.WebAuthnCeremony;
import com.top_logic.layout.react.control.webauthn.WebAuthnListener;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PendingCeremony;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * The server side of a passkey ceremony run by a {@link ReactWebAuthnControl}: starts the ceremony
 * with the {@link PasskeyService}, checks the credential the browser answers with, and hands the
 * outcome on.
 *
 * <p>
 * A ceremony is {@link #offer(Person) offered} for an account (or, for a login with a passkey alone,
 * for no account): a {@link PendingCeremony} is started and its options go to the control. The
 * pending ceremony stays in this object, so the challenge never leaves the server. Every answer of
 * the browser - a credential or a failure - consumes it, and a fresh one is started for the next
 * attempt. A refused credential and a failure of the browser are shown as localized message below
 * the button; a cancelled browser dialog only shows a short hint. Shortly before the pending
 * ceremony expires, the control asks for a fresh one, which is started without a message, so that
 * a button left alone for a long time still works on the first click.
 * </p>
 *
 * @param <P>
 *        The type of the pending ceremony.
 * @param <R>
 *        The outcome of a successful ceremony, handed to the success handler.
 */
public abstract class PasskeyCeremony<P extends PendingCeremony, R> implements WebAuthnListener {

	/**
	 * Error name of the browser for a ceremony cancelled by the user, timed out, or started without
	 * a user gesture where the browser requires one.
	 */
	public static final String NOT_ALLOWED_ERROR = "NotAllowedError";

	/** Error name of the browser for an aborted ceremony. */
	public static final String ABORT_ERROR = "AbortError";

	/** Error name of the browser for a browser without Web Authentication. */
	public static final String NOT_SUPPORTED_ERROR = "NotSupportedError";

	/** Error name of the browser for an authenticator already holding an excluded credential. */
	public static final String INVALID_STATE_ERROR = "InvalidStateError";

	/** Error name of the browser for a relying party ID that does not match the page. */
	public static final String SECURITY_ERROR = "SecurityError";

	private final BiConsumer<ReactContext, R> _onSuccess;

	private ReactWebAuthnControl _control;

	private boolean _offered;

	private Person _account;

	private P _pending;

	private boolean _autoStart;

	/**
	 * Creates a {@link PasskeyCeremony}.
	 *
	 * @param onSuccess
	 *        Receives the outcome of every successful ceremony, with the context of the control.
	 */
	protected PasskeyCeremony(BiConsumer<ReactContext, R> onSuccess) {
		_onSuccess = onSuccess;
	}

	/**
	 * Creates the control running this ceremony in the browser.
	 *
	 * <p>
	 * The control offers no ceremony until one is {@link #offer(Person) offered}.
	 * </p>
	 *
	 * @param context
	 *        The context of the control.
	 * @param label
	 *        The label of the button.
	 * @param autoStart
	 *        Whether the browser starts the first ceremony as soon as the control is displayed, see
	 *        {@link ReactWebAuthnControl#setAutoStart(boolean)}. A browser that refuses the automatic
	 *        start shows no message, the button stays available.
	 */
	public ReactWebAuthnControl createControl(ReactContext context, String label, boolean autoStart) {
		_control = new ReactWebAuthnControl(context, kind(), null, label, this);
		_control.setAutoStart(autoStart);
		_autoStart = autoStart;
		return _control;
	}

	/**
	 * The control running this ceremony, see {@link #createControl(ReactContext, String, boolean)}.
	 */
	public ReactWebAuthnControl getControl() {
		return _control;
	}

	/**
	 * Offers a ceremony for the given account, replacing the one offered before.
	 *
	 * @param account
	 *        The account of the ceremony; <code>null</code> only for a ceremony that does not need
	 *        one, see {@link #start(Person)}.
	 */
	public void offer(Person account) {
		_offered = true;
		_account = account;
		_control.setError(null);
		restart();
	}

	/**
	 * Offers no ceremony, e.g. because there is no account to run it for.
	 *
	 * @param reason
	 *        Why there is no ceremony, shown below the button; <code>null</code> for no message.
	 */
	public void withdraw(ResKey reason) {
		_offered = false;
		_account = null;
		_pending = null;
		_control.setOptions(null);
		_control.setError(reason == null ? null : text(reason));
	}

	/**
	 * The account the ceremony is offered for, <code>null</code> if none.
	 */
	public Person getAccount() {
		return _account;
	}

	@Override
	public void onCredential(ReactContext context, String credentialJson) {
		_autoStart = false;
		P pending = _pending;
		_pending = null;
		if (pending == null) {
			fail(com.top_logic.security.passkey.I18NConstants.ERROR_REQUEST_EXPIRED);
			return;
		}

		R result;
		try {
			result = finish(pending, credentialJson);
		} catch (TopLogicException ex) {
			fail(ex.getErrorKey());
			return;
		}

		// Ready for a further ceremony before the outcome is handed on, since the success handler may
		// discard the display this control belongs to (e.g. a completed login rebuilds the window).
		_control.setError(null);
		restart();
		try {
			_onSuccess.accept(context, result);
		} catch (TopLogicException ex) {
			_control.setError(text(ex.getErrorKey()));
		}
	}

	@Override
	public void onFailure(ReactContext context, String errorName, String message) {
		boolean automatic = _autoStart;
		_autoStart = false;
		Logger.info("Passkey ceremony failed in the browser: " + errorName + " " + message, PasskeyCeremony.class);

		boolean refusedAutoStart = automatic && NOT_ALLOWED_ERROR.equals(errorName);
		_control.setError(refusedAutoStart ? null : text(hint(errorName)));
		restart();
	}

	/**
	 * Starts a fresh ceremony, since the pending one is about to expire; the message shown stays.
	 */
	@Override
	public void onExpired(ReactContext context) {
		restart();
	}

	/**
	 * The message for a failure the browser reported with the given error name.
	 */
	protected ResKey hint(String errorName) {
		switch (errorName) {
			case NOT_ALLOWED_ERROR:
			case ABORT_ERROR:
				return I18NConstants.ERROR_CANCELLED;
			case NOT_SUPPORTED_ERROR:
				return I18NConstants.ERROR_NOT_SUPPORTED;
			case INVALID_STATE_ERROR:
				return I18NConstants.ERROR_ALREADY_ON_AUTHENTICATOR;
			case SECURITY_ERROR:
				return I18NConstants.ERROR_WRONG_DOMAIN;
			default:
				return I18NConstants.ERROR_BROWSER__NAME.fill(errorName);
		}
	}

	/**
	 * Shows the given error and offers a fresh ceremony.
	 */
	private void fail(ResKey error) {
		_control.setError(text(error));
		restart();
	}

	/**
	 * Starts a fresh ceremony for the {@link #offer(Person) offered} account and gives its options to
	 * the control; without an offer, or if the start fails, the control offers none.
	 */
	private void restart() {
		_pending = null;
		if (!_offered) {
			_control.setOptions(null);
			return;
		}
		try {
			_pending = start(_account);
			_control.setOptions(_pending.toOptionsJson());
		} catch (TopLogicException ex) {
			_pending = null;
			_control.setOptions(null);
			_control.setError(text(ex.getErrorKey()));
		}
	}

	/**
	 * The kind of ceremony the browser runs.
	 */
	protected abstract WebAuthnCeremony kind();

	/**
	 * Starts a ceremony with the {@link PasskeyService}.
	 *
	 * @param account
	 *        The account the ceremony is {@link #offer(Person) offered} for.
	 */
	protected abstract P start(Person account);

	/**
	 * Completes the given ceremony with the {@link PasskeyService}.
	 *
	 * @param pending
	 *        The ceremony {@link #start(Person) started} last.
	 * @param credentialJson
	 *        The credential the browser answered with, unverified.
	 * @return The outcome handed to the success handler.
	 * @throws TopLogicException
	 *         If the credential is refused.
	 */
	protected abstract R finish(P pending, String credentialJson);

	/**
	 * The request to derive the relying party from, if it is not configured; <code>null</code> if
	 * there is none.
	 */
	protected HttpServletRequest request() {
		if (!DefaultDisplayContext.hasDisplayContext()) {
			return null;
		}
		return DefaultDisplayContext.getDisplayContext().asRequest();
	}

	/**
	 * The {@link PasskeyService} running the ceremonies.
	 */
	protected static PasskeyService service() {
		return PasskeyService.getInstance();
	}

	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

}
