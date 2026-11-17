/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.captcha;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.altcha.altcha.v2.Altcha;
import org.altcha.altcha.v2.Altcha.Challenge;
import org.altcha.altcha.v2.Altcha.CreateChallengeOptions;
import org.altcha.altcha.v2.Altcha.Solution;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.captcha.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link CaptchaService}: solutions are computed with the solver of the ALTCHA library,
 * as the widget in the browser computes them.
 */
public class TestCaptchaService extends BasicTestCase {

	private static final String KEY = "test-key";

	private static final String OTHER_KEY = "other-key";

	/** The cost of the challenges configured for the tests. */
	private static final int COST = 10;

	@Override
	protected void tearDown() throws Exception {
		useKey(null);
		super.tearDown();
	}

	/** A solved challenge is accepted. */
	public void testSolvedChallengeAccepted() throws Exception {
		String challenge = service().createChallenge();
		assertTrue(challenge, challenge.contains("\"" + CaptchaService.ALGORITHM + "\""));
		assertTrue(challenge, challenge.contains("\"expiresAt\""));

		assertTrue(service().verify(solve(challenge)));
	}

	/** A solution is accepted only once. */
	public void testReplayRefused() throws Exception {
		String payload = solve(service().createChallenge());

		assertTrue(service().verify(payload));
		assertFalse(service().verify(payload));
		assertCheckFails(payload);
	}

	/** Each challenge is solved on its own; solving one does not use up another. */
	public void testChallengesIndependent() throws Exception {
		String first = solve(service().createChallenge());
		String second = solve(service().createChallenge());

		assertTrue(service().verify(first));
		assertTrue(service().verify(second));
	}

	/** {@link CaptchaService#check(String)} passes for an accepted solution. */
	public void testCheck() throws Exception {
		service().check(solve(service().createChallenge()));
	}

	/** Missing and malformed solutions are refused. */
	public void testMalformedRefused() {
		assertFalse(service().verify(null));
		assertFalse(service().verify(""));
		assertFalse(service().verify("not base64 !"));
		assertFalse(service().verify(base64("{\"challenge\":{}}")));
		assertCheckFails(null);
	}

	/** A solution with a changed counter or derived key is refused. */
	public void testTamperedSolutionRefused() throws Exception {
		String challenge = service().createChallenge();
		Solution solution = solution(challenge);

		assertFalse(service().verify(payload(challenge, solution.counter() + 1, solution.derivedKey())));
		assertFalse(service().verify(payload(challenge, solution.counter(), flipLastHexDigit(solution.derivedKey()))));

		// The unchanged solution is still accepted.
		assertTrue(service().verify(payload(challenge, solution.counter(), solution.derivedKey())));
	}

	/** A challenge with a changed signature or changed parameters is refused. */
	public void testTamperedChallengeRefused() throws Exception {
		String challenge = service().createChallenge();
		Solution solution = solution(challenge);

		String signature = parse(challenge).signature();
		String otherSignature = challenge.replace(signature, flipLastHexDigit(signature));
		assertFalse(service().verify(payload(otherSignature, solution.counter(), solution.derivedKey())));

		String otherCost = challenge.replace("\"cost\":" + COST, "\"cost\":" + (COST + 1));
		assertFalse(otherCost, challenge.equals(otherCost));
		assertFalse(service().verify(payload(otherCost, solution.counter(), solution.derivedKey())));
	}

	/** A challenge signed with the configured key is accepted, one signed with another key not. */
	public void testOtherKeyRefused() throws Exception {
		useKey(KEY);

		assertTrue(service().verify(solve(externalChallenge(KEY, futureExpiry()))));
		assertFalse(service().verify(solve(externalChallenge(OTHER_KEY, futureExpiry()))));
	}

	/** A challenge created before the key changed is refused. */
	public void testChallengeOfFormerKeyRefused() throws Exception {
		useKey(KEY);
		String payload = solve(service().createChallenge());

		useKey(OTHER_KEY);
		assertFalse(service().verify(payload));
	}

	/** A challenge of a generated key is refused after a restart, which generates a new key. */
	public void testGeneratedKeyChangesOnRestart() throws Exception {
		String payload = solve(service().createChallenge());

		ModuleUtil.INSTANCE.restart(CaptchaService.Module.INSTANCE, null);
		assertFalse(service().verify(payload));
	}

	/** An expired challenge is refused. */
	public void testExpiredRefused() throws Exception {
		useKey(KEY);
		long expired = System.currentTimeMillis() / 1000 - 60;

		assertFalse(service().verify(solve(externalChallenge(KEY, Long.valueOf(expired)))));
	}

	/** A challenge without expiry is refused, also if it is signed with the configured key. */
	public void testWithoutExpiryRefused() throws Exception {
		useKey(KEY);

		assertFalse(service().verify(solve(externalChallenge(KEY, null))));
	}

	private static Long futureExpiry() {
		return Long.valueOf(System.currentTimeMillis() / 1000 + 600);
	}

	/**
	 * A challenge created with the library directly, as the service creates its challenges.
	 */
	private static String externalChallenge(String key, Long expiresAt) throws Exception {
		CreateChallengeOptions options = new CreateChallengeOptions()
			.algorithm(CaptchaService.ALGORITHM)
			.cost(COST)
			.counter(7)
			.hmacSignatureSecret(key);
		if (expiresAt != null) {
			options.expiresAt(expiresAt);
		}
		return Altcha.createChallenge(options).toJson();
	}

	private static void assertCheckFails(String payload) {
		try {
			service().check(payload);
			fail("Captcha solution must be refused.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_CAPTCHA_INVALID, ex.getErrorKey());
		}
	}

	/**
	 * Solves the given challenge and returns the payload the widget submits.
	 */
	private static String solve(String challenge) throws Exception {
		Solution solution = solution(challenge);
		return payload(challenge, solution.counter(), solution.derivedKey());
	}

	private static Solution solution(String challenge) throws Exception {
		Challenge parsed = parse(challenge);
		Solution solution = Altcha.solveChallenge(parsed, Altcha.kdf(parsed.parameters().algorithm()));
		assertNotNull("Challenge not solved: " + challenge, solution);
		return solution;
	}

	private static Challenge parse(String challenge) throws Exception {
		return Altcha.parsePayload(payload(challenge, 0, "00")).challenge();
	}

	/**
	 * The payload the widget submits: the Base64 encoded JSON of challenge and solution.
	 */
	private static String payload(String challenge, long counter, String derivedKey) {
		return base64("{\"challenge\":" + challenge + ",\"solution\":{\"counter\":" + counter
			+ ",\"derivedKey\":\"" + derivedKey + "\"}}");
	}

	private static String base64(String json) {
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

	private static String flipLastHexDigit(String hex) {
		char last = hex.charAt(hex.length() - 1);
		return hex.substring(0, hex.length() - 1) + (last == '0' ? '1' : '0');
	}

	/**
	 * Restarts the service with the given key, <code>null</code> for a generated key.
	 */
	private static void useKey(String key) throws Exception {
		CaptchaService.Config<?> config = (CaptchaService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(CaptchaService.class);
		PropertyDescriptor property = config.descriptor().getProperty(CaptchaService.Config.HMAC_KEY);
		LiteralSecretSource.Config value;
		if (key == null) {
			if (config.value(property) == null) {
				return;
			}
			value = null;
		} else {
			value = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
			value.setValue(key);
		}
		config.update(property, value);
		ModuleUtil.INSTANCE.restart(CaptchaService.Module.INSTANCE, null);
	}

	private static CaptchaService service() {
		return CaptchaService.getInstance();
	}

	/**
	 * The suite, with the {@link CaptchaService} started.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestCaptchaService.class), CaptchaService.Module.INSTANCE));
	}

}
