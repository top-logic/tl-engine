/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * A verification code sent by e-mail to prove that the person at the keyboard reads the mails of
 * an address.
 *
 * <p>
 * The code consists of the {@link InvitationModule.Config#getVerificationCodeSize() configured
 * number} of digits and is accepted for the {@link InvitationModule.Config#getCodeValidity() configured
 * validity} after its creation. After {@link #MAX_MISMATCHES} wrong entries it is no longer
 * accepted at all. The code is kept on the server only, by whoever sent it, e.g. a dialog of the
 * user interface.
 * </p>
 */
public class VerificationCode {

	/**
	 * The number of wrong entries after which a code is no longer accepted.
	 */
	public static final int MAX_MISMATCHES = 3;

	/**
	 * The number of failures a code request counts in the {@link LoginFailuresModule}: more than
	 * are allowed without delay, so that a second code is only sent after a delay.
	 */
	private static final int REQUEST_FAILURES = 4;

	private final String _code;

	private final long _createdAt;

	private int _mismatches;

	/**
	 * Creates a new random {@link VerificationCode}.
	 */
	public VerificationCode() {
		_code = newCode();
		_createdAt = System.currentTimeMillis();
	}

	/**
	 * The digits of the code, to be sent to the person.
	 */
	public String getCode() {
		return _code;
	}

	/**
	 * Checks the code the person has entered.
	 *
	 * @param entered
	 *        The entered code, may be <code>null</code>.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_VERIFICATION_CODE_EXPIRED} if the code has expired or
	 *         was entered wrongly too often, with {@link I18NConstants#ERROR_VERIFICATION_CODE_MISMATCH}
	 *         or, for the last allowed attempt,
	 *         {@link I18NConstants#ERROR_VERIFICATION_CODE_TOO_MANY_MISMATCHES} if the entered code
	 *         is wrong.
	 */
	public synchronized void check(String entered) {
		if (_mismatches >= MAX_MISMATCHES || isExpired()) {
			throw new TopLogicException(I18NConstants.ERROR_VERIFICATION_CODE_EXPIRED);
		}
		String code = entered == null ? "" : entered.trim();
		if (!MessageDigest.isEqual(_code.getBytes(StandardCharsets.UTF_8), code.getBytes(StandardCharsets.UTF_8))) {
			_mismatches++;
			if (_mismatches >= MAX_MISMATCHES) {
				throw new TopLogicException(I18NConstants.ERROR_VERIFICATION_CODE_TOO_MANY_MISMATCHES);
			}
			throw new TopLogicException(I18NConstants.ERROR_VERIFICATION_CODE_MISMATCH);
		}
	}

	private boolean isExpired() {
		return System.currentTimeMillis() > _createdAt + InvitationModule.getInstance().getConfig().getCodeValidity();
	}

	/**
	 * A new random code of {@link InvitationModule.Config#getVerificationCodeSize() configured
	 * size}, with leading zeros.
	 */
	public static String newCode() {
		int size = InvitationModule.getInstance().getConfig().getVerificationCodeSize();
		long upperBound = (long) Math.pow(10, size);
		long value = SecureRandomService.getInstance().getRandom().nextLong(0, upperBound);
		return String.format("%0" + size + "d", Long.valueOf(value));
	}

	/**
	 * Refuses a request for a code under the given key while requests under it are backing off,
	 * and counts the request otherwise.
	 *
	 * <p>
	 * Each request lets the next one under the same key wait longer (see
	 * {@link LoginFailuresModule}), so that nobody can trigger a stream of mails.
	 * </p>
	 *
	 * @param key
	 *        The key the request is counted under, e.g. derived from the invitation or account the
	 *        code is for.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_VERIFICATION_CODE_REQUESTED_TOO_OFTEN__SECONDS} if
	 *         the request must wait.
	 */
	public static void throttleRequest(String key) {
		LoginFailuresModule<?> failures = LoginFailuresModule.getInstance();
		failures.checkRetry(key, I18NConstants.ERROR_VERIFICATION_CODE_REQUESTED_TOO_OFTEN__SECONDS);
		LoginFailure failure = failures.notifyLoginFailed(key);
		while (failure.getFailureCnt() < REQUEST_FAILURES) {
			failure.incFailures();
		}
	}

}
