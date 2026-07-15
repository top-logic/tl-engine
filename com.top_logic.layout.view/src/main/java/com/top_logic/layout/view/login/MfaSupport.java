/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import org.bouncycastle.util.encoders.Base32;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.component.configuration.MFAConfig;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Headless helpers for the React login MFA steps: TOTP secret generation, QR-code rendering and code
 * verification.
 *
 * <p>
 * Reuses the same services as the legacy login dialog (the {@link MFAConfig}, the {@code totp}
 * library and {@link SecureRandomService}), so both UIs validate against an identical TOTP
 * contract.
 * </p>
 */
public class MfaSupport {

	/** Number of Base32 characters in a generated secret. */
	private static final int SECRET_CHARACTERS = 32;

	/**
	 * Generates a fresh random TOTP secret.
	 */
	public static Password generateSecret() {
		// 5 bits per Base32 character.
		byte[] bytes = new byte[(SECRET_CHARACTERS * 5) / 8];
		SecureRandomService.getInstance().getRandom().nextBytes(bytes);
		return Password.fromPlainText(Base32.toBase32String(bytes));
	}

	/**
	 * Renders the {@code otpauth://} provisioning URI for the given account and secret as a QR-code
	 * PNG that an authenticator app can scan.
	 */
	public static BinaryData createQrCode(Person account, Password secret) {
		MFAConfig mfaConfig = mfaConfig();
		QrData qrData = new QrData.Builder()
			.label(account.getName())
			.secret(secret.decrypt())
			.issuer(Resources.getInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE))
			.algorithm(HashingAlgorithm.SHA1)
			.digits(mfaConfig.getDigits())
			.period(mfaConfig.getPeriod())
			.build();
		QrGenerator generator = new ZxingPngQrGenerator();
		try {
			byte[] imageData = generator.generate(qrData);
			return BinaryDataFactory.createBinaryData(imageData, generator.getImageMimeType());
		} catch (QrGenerationException ex) {
			throw new TopLogicException(I18NConstants.MFA_QR_CODE_FAILED, ex);
		}
	}

	/**
	 * Whether the given one-time code is currently valid for the given secret.
	 */
	public static boolean isValidCode(Password secret, String code) {
		if (secret == null || code == null || code.isEmpty()) {
			return false;
		}
		MFAConfig mfaConfig = mfaConfig();
		DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, mfaConfig.getDigits());
		DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
		verifier.setTimePeriod(mfaConfig.getPeriod());
		verifier.setAllowedTimePeriodDiscrepancy(mfaConfig.getAllowedTimePeriodDiscrepancy());
		return verifier.isValidCode(secret.decrypt(), code);
	}

	private static MFAConfig mfaConfig() {
		return ApplicationConfig.getInstance().getConfig(MFAConfig.class);
	}

}
