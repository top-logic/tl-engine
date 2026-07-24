/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cookiepool;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.cookiepool.CookiePool;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link CookiePoolService}: the storageState is encrypted at rest (round-trips on read,
 * not readable as plaintext), and the owner-vs-shared selection filter isolates private pools while
 * exposing shared ones (including to an unattended, no-user session).
 *
 * <p>
 * Encryption is provided by the platform {@link EncryptionService}: the tl-ai-core test config
 * ({@code tl-ai-core.test.config.xml}) wires a {@code DummyEncryptionService} instance with a real
 * AES key, so {@code getEncryption()} works unattended in the test.
 * </p>
 */
public class TestCookiePoolService extends BasicTestCase {

	/** A recognizable marker inside the plaintext state, used to prove the stored blob is not plaintext. */
	private static final String MARKER = "SECRET_COOKIE_MARKER_9f3a";

	private static final byte[] STATE_JSON =
		("{\"cookies\":[{\"name\":\"session\",\"value\":\"" + MARKER + "\"}],\"origins\":[]}")
			.getBytes(StandardCharsets.UTF_8);

	private CookiePoolService _service;

	private Person _alice;

	private Person _bob;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = CookiePoolService.getInstance();
		_alice = PersonManager.getManager().getRoot();
		_bob = TestPerson.createPerson("cookiepool-bob");
	}

	@Override
	protected void tearDown() throws Exception {
		if (_bob != null) {
			TestPerson.deletePersonAndUser(_bob);
			_bob = null;
		}
		super.tearDown();
	}

	/** The state is stored encrypted (not the plaintext bytes / marker) and decrypts back to the original. */
	public void testEncryptDecryptRoundTrip() throws Exception {
		CookiePool pool;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			pool = _service.upsertPool("round-trip", STATE_JSON, _alice);
			tx.commit();
		}

		BinaryDataSource state = pool.getState();
		assertNotNull("state binary persisted", state);
		byte[] stored = readBytes(state);
		assertFalse("stored blob must not equal the plaintext state", Arrays.equals(stored, STATE_JSON));
		String storedAsText = new String(stored, StandardCharsets.UTF_8);
		assertFalse("plaintext cookie marker must not be readable in the stored blob",
			storedAsText.contains(MARKER));

		byte[] decrypted = _service.decryptState(pool);
		assertTrue("decrypted state must match the original", Arrays.equals(decrypted, STATE_JSON));
	}

	/** A private pool is selectable only by its owner; a shared pool is selectable by anyone. */
	public void testOwnerVsSharedSelection() throws Exception {
		CookiePool priv;
		CookiePool shared;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			priv = _service.upsertPool("alice-private", STATE_JSON, _alice);
			shared = _service.upsertPool("team-shared", STATE_JSON, _alice);
			shared.setShared(Boolean.TRUE);
			tx.commit();
		}

		// Owner sees their own private pool.
		Optional<CookiePool> aliceView = _service.findSelectablePool("alice-private", _alice);
		assertTrue("owner must see their own private pool", aliceView.isPresent());
		assertSame(priv, aliceView.get());

		// A different user must NOT see the private pool.
		assertFalse("another user must not see a private pool",
			_service.findSelectablePool("alice-private", _bob).isPresent());

		// An unattended (no-user) session must NOT see a private pool.
		assertFalse("an unattended session must not see a private pool",
			_service.findSelectablePool("alice-private", null).isPresent());

		// A shared pool is selectable by another user and by an unattended session.
		assertTrue("another user must see a shared pool",
			_service.findSelectablePool("team-shared", _bob).isPresent());
		assertTrue("an unattended session must see a shared pool",
			_service.findSelectablePool("team-shared", null).isPresent());
		assertSame(shared, _service.findSelectablePool("team-shared", null).get());
	}

	/** Listing returns the caller's own pools plus shared pools, and only shared for an unattended session. */
	public void testListSelectablePools() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			_service.upsertPool("list-alice-private", STATE_JSON, _alice);
			CookiePool shared = _service.upsertPool("list-team-shared", STATE_JSON, _alice);
			shared.setShared(Boolean.TRUE);
			_service.upsertPool("list-bob-private", STATE_JSON, _bob);
			tx.commit();
		}

		List<String> aliceNames = names(_service.listSelectablePools(_alice));
		assertTrue(aliceNames.contains("list-alice-private"));
		assertTrue(aliceNames.contains("list-team-shared"));
		assertFalse("alice must not see bob's private pool", aliceNames.contains("list-bob-private"));

		List<String> bobNames = names(_service.listSelectablePools(_bob));
		assertTrue(bobNames.contains("list-bob-private"));
		assertTrue("bob sees the shared pool", bobNames.contains("list-team-shared"));
		assertFalse("bob must not see alice's private pool", bobNames.contains("list-alice-private"));

		List<String> anonNames = names(_service.listSelectablePools(null));
		assertTrue("unattended sees the shared pool", anonNames.contains("list-team-shared"));
		assertFalse("unattended must not see any private pool", anonNames.contains("list-alice-private"));
		assertFalse("unattended must not see any private pool", anonNames.contains("list-bob-private"));
	}

	/** Saving again under the same name updates the caller's pool in place rather than creating a second. */
	public void testUpsertUpdatesOwnPoolInPlace() throws Exception {
		CookiePool first;
		CookiePool second;
		byte[] otherState = "{\"cookies\":[],\"origins\":[]}".getBytes(StandardCharsets.UTF_8);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			first = _service.upsertPool("reused", STATE_JSON, _alice);
			tx.commit();
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			second = _service.upsertPool("reused", otherState, _alice);
			tx.commit();
		}
		assertSame("second save must update the same pool object", first, second);
		assertTrue("state must reflect the second save", Arrays.equals(otherState, _service.decryptState(second)));
	}

	private static List<String> names(List<CookiePool> pools) {
		return pools.stream().map(CookiePool::getName).toList();
	}

	private static byte[] readBytes(BinaryDataSource source) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		source.deliverTo(buffer);
		return buffer.toByteArray();
	}

	/**
	 * Creates the test suite: KB + PersonManager (for real owner Persons) around ModelService,
	 * EncryptionService (Dummy, from the test config) and CookiePoolService.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestCookiePoolService.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			EncryptionService.Module.INSTANCE,
			CookiePoolService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
