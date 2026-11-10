/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.altcha.altcha.v2.Altcha;
import org.altcha.altcha.v2.Altcha.Challenge;
import org.altcha.altcha.v2.Altcha.CounterMode;
import org.altcha.altcha.v2.Altcha.CreateChallengeOptions;
import org.altcha.altcha.v2.Altcha.KeyDerivationFunction;
import org.altcha.altcha.v2.Altcha.Payload;
import org.altcha.altcha.v2.Altcha.VerifySolutionResult;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AuthenticationCategory;
import com.top_logic.util.error.TopLogicException;

/**
 * Proof-of-work captcha (ALTCHA) protecting a form an anonymous visitor submits.
 *
 * <p>
 * The service creates signed challenges for the ALTCHA widget in the browser. The widget solves
 * the challenge by computation, without any interaction of the visitor, and submits its solution
 * with the form. The service accepts a solution only if it solves a challenge the service has
 * signed, the challenge has not expired, and the solution has not been accepted before. No
 * external service is involved.
 * </p>
 *
 * <p>
 * The challenges are signed with the {@link Config#getHmacKey() key} of the installation. Without
 * one, the service signs with a random key it creates when it starts: a challenge is then accepted
 * only by the node that created it, and only until that node restarts.
 * </p>
 *
 * <p>
 * The difficulty is the {@link Config#getCost() cost} of one attempt times the number of attempts,
 * which is drawn for each challenge from the upper half of the {@link Config#getMaxNumber() maximum
 * number}. A challenge is valid for the time given by {@link Config#getExpires()}.
 * </p>
 *
 * @implNote The protocol (challenge creation, signature, key derivation, verification) is version
 *           2 of the ALTCHA protocol, implemented by the library <code>org.altcha:altcha</code> (MIT
 *           license). The challenges use the key derivation function {@link #ALGORITHM} in
 *           deterministic mode: the service picks the counter the browser must find and publishes a
 *           prefix of the key derived from it. A submitted solution is checked by deriving the key
 *           once more. {@link #createChallenge()} creates a challenge, {@link #verify(String)} and
 *           {@link #check(String)} check a submitted solution.
 *
 *           <p>
 *           Accepted solutions are remembered, by the signature of their challenge, until the
 *           challenge expires. This memory belongs to the node: in a cluster sharing the key, a
 *           solution submitted to two nodes is accepted by each of them once.
 *           </p>
 */
@ServiceCategory.Of(AuthenticationCategory.class)
@ServiceDependencies({
	SecretStoreService.Module.class,
})
public class CaptchaService extends ConfiguredManagedClass<CaptchaService.Config<?>> {

	/**
	 * Configuration options of {@link CaptchaService}.
	 */
	public interface Config<I extends CaptchaService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration name for {@link #getHmacKey()}.
		 *
		 * @see #getHmacKey()
		 */
		String HMAC_KEY = "hmac-key";

		/**
		 * Configuration name for {@link #getCost()}.
		 *
		 * @see #getCost()
		 */
		String COST = "cost";

		/**
		 * Configuration name for {@link #getMaxNumber()}.
		 *
		 * @see #getMaxNumber()
		 */
		String MAX_NUMBER = "max-number";

		/**
		 * Configuration name for {@link #getExpires()}.
		 *
		 * @see #getExpires()
		 */
		String EXPIRES = "expires";

		/**
		 * The secret key signing the challenges.
		 *
		 * <p>
		 * Recommended is a reference to the key by its secret name, e.g.
		 * <code>&lt;hmac-key name="tl_captcha_hmac_key"/&gt;</code>. An attribute value is an
		 * encrypted key, or a plain key with the prefix <code>unencrypted:</code>. The key is read
		 * whenever a challenge is created or checked; changing it invalidates the challenges
		 * created before.
		 * </p>
		 *
		 * <p>
		 * Empty: the service creates a random key when it starts, and logs a warning when the key
		 * is used for the first time. A challenge is then only accepted by the node that created
		 * it, and only until this node restarts. A production installation, at least one with
		 * several nodes, configures the key.
		 * </p>
		 */
		@Name(HMAC_KEY)
		@Label("HMAC key")
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getHmacKey();

		/**
		 * The work of one attempt to solve a challenge: the number of iterations of the key
		 * derivation function.
		 */
		@Name(COST)
		@IntDefault(5000)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1)
		int getCost();

		/**
		 * The maximum number of attempts the browser needs to solve a challenge.
		 *
		 * <p>
		 * The number of attempts of each challenge is drawn at random from the upper half of this
		 * value. Together with the {@link #getCost() cost} of one attempt, it decides how long a
		 * browser computes, usually a few seconds.
		 * </p>
		 */
		@Name(MAX_NUMBER)
		@IntDefault(10000)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 2)
		int getMaxNumber();

		/**
		 * How long a created challenge can be solved and submitted.
		 */
		@Name(EXPIRES)
		@Format(MillisFormat.class)
		@FormattedDefault("10min")
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1000)
		long getExpires();
	}

	/**
	 * The key derivation function of the challenges, the default of the ALTCHA widget.
	 */
	public static final String ALGORITHM = "PBKDF2/SHA-256";

	/**
	 * Interval in which expired entries are removed from the memory of accepted solutions.
	 */
	private static final long CLEANUP_INTERVAL_SECONDS = 60;

	/** Length of a generated key in bytes. */
	private static final int GENERATED_KEY_LENGTH = 32;

	private final SecureRandom _random = new SecureRandom();

	private final KeyDerivationFunction _kdf = Altcha.kdf(ALGORITHM);

	/** The configured key, <code>null</code> if none is configured. */
	private final SecretSource _hmacKey;

	/** The key created at startup, if none is configured. */
	private String _generatedKey;

	/** Whether the use of {@link #_generatedKey} has been reported since the start. */
	private final AtomicBoolean _generatedKeyReported = new AtomicBoolean();

	/**
	 * Signatures of the challenges of accepted solutions, mapped to the time their challenge
	 * expires, in seconds since the epoch.
	 */
	private final Map<String, Long> _accepted = new HashMap<>();

	/** Time of the next removal of expired entries from {@link #_accepted}, in seconds. */
	private long _nextCleanup;

	/**
	 * Creates a {@link CaptchaService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub-configurations.
	 * @param config
	 *        The configuration.
	 */
	public CaptchaService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_hmacKey = context.getInstance(config.getHmacKey());
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (_hmacKey == null) {
			byte[] key = new byte[GENERATED_KEY_LENGTH];
			_random.nextBytes(key);
			_generatedKey = HexFormat.of().formatHex(key);
			_generatedKeyReported.set(false);
		}
	}

	@Override
	protected void shutDown() {
		_generatedKey = null;
		synchronized (_accepted) {
			_accepted.clear();
		}
		super.shutDown();
	}

	/**
	 * Creates a new challenge for the ALTCHA widget.
	 *
	 * @return The challenge in the JSON form the widget takes as challenge data.
	 */
	public String createChallenge() {
		Config<?> config = getConfig();
		int maxNumber = config.getMaxNumber();
		int minNumber = maxNumber / 2;
		long counter = minNumber + _random.nextInt(maxNumber - minNumber);
		long expiresInSeconds = Math.max(1, (config.getExpires() + 999) / 1000);
		CreateChallengeOptions options = new CreateChallengeOptions()
			.algorithm(ALGORITHM)
			.cost(config.getCost())
			.counter(counter)
			.expiresInSeconds(expiresInSeconds)
			.hmacSignatureSecret(hmacKey());
		Challenge challenge;
		try {
			challenge = Altcha.createChallenge(options);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Creating a captcha challenge failed: " + ex.getMessage(), ex);
		}
		return challenge.toJson();
	}

	/**
	 * Checks the solution of a challenge the ALTCHA widget submits.
	 *
	 * <p>
	 * A solution is accepted if it solves a challenge created by {@link #createChallenge()} with
	 * the current key, the challenge has not expired, and no solution of the challenge has been
	 * accepted before. An accepted solution is used up: submitting it again fails.
	 * </p>
	 *
	 * @param payload
	 *        The solution as the widget submits it: the Base64 encoded JSON of the challenge and
	 *        its solution. <code>null</code> or empty if the widget submitted nothing.
	 * @return Whether the solution is accepted.
	 */
	public boolean verify(String payload) {
		if (StringServices.isEmpty(payload)) {
			return false;
		}
		Payload parsed;
		try {
			parsed = Altcha.parsePayload(payload);
		} catch (Exception ex) {
			Logger.info("Captcha refused, malformed solution: " + ex.getMessage(), CaptchaService.class);
			return false;
		}
		Challenge challenge = parsed.challenge();
		Number expiresAt = challenge.parameters().expiresAt();
		if (expiresAt == null) {
			Logger.info("Captcha refused, challenge without expiry.", CaptchaService.class);
			return false;
		}

		VerifySolutionResult result;
		try {
			result = Altcha.verifySolution(challenge, parsed.solution(), hmacKey(), null, null,
				CounterMode.UINT32, _kdf);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Checking a captcha solution failed: " + ex.getMessage(), ex);
		}
		if (!result.verified()) {
			Logger.info("Captcha refused: " + result.toJson(), CaptchaService.class);
			return false;
		}

		if (!accept(challenge.signature(), expiresAt.longValue() + 1)) {
			Logger.info("Captcha refused, solution already used.", CaptchaService.class);
			return false;
		}
		return true;
	}

	/**
	 * Checks the solution of a challenge the ALTCHA widget submits, see {@link #verify(String)}.
	 *
	 * @param payload
	 *        The solution as the widget submits it.
	 * @throws TopLogicException
	 *         {@link I18NConstants#ERROR_CAPTCHA_INVALID} if the solution is not accepted.
	 */
	public void check(String payload) throws TopLogicException {
		if (!verify(payload)) {
			throw new TopLogicException(I18NConstants.ERROR_CAPTCHA_INVALID);
		}
	}

	/**
	 * Remembers the challenge of an accepted solution.
	 *
	 * @return Whether the challenge was not accepted before.
	 */
	private boolean accept(String signature, long expiresAt) {
		long now = System.currentTimeMillis() / 1000;
		synchronized (_accepted) {
			if (now >= _nextCleanup) {
				for (Iterator<Long> it = _accepted.values().iterator(); it.hasNext();) {
					if (it.next().longValue() < now) {
						it.remove();
					}
				}
				_nextCleanup = now + CLEANUP_INTERVAL_SECONDS;
			}
			return _accepted.putIfAbsent(signature, Long.valueOf(expiresAt)) == null;
		}
	}

	private String hmacKey() {
		if (_hmacKey != null) {
			return _hmacKey.getSecret();
		}
		String generatedKey = _generatedKey;
		if (generatedKey == null) {
			throw new IllegalStateException(CaptchaService.class.getName() + " is not started.");
		}
		if (!_generatedKeyReported.getAndSet(true)) {
			Logger.warn("No key is configured for signing captcha challenges (option '" + Config.HMAC_KEY
				+ "' of " + CaptchaService.class.getName() + "). A random key is used: challenges are"
				+ " only accepted by this node and until it restarts.", CaptchaService.class);
		}
		return generatedKey;
	}

	/**
	 * The {@link CaptchaService} of the application.
	 */
	public static CaptchaService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link CaptchaService}.
	 */
	public static final class Module extends TypedRuntimeModule<CaptchaService> {

		/**
		 * Singleton {@link CaptchaService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CaptchaService> getImplementation() {
			return CaptchaService.class;
		}
	}

}
