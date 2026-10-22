/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cookiepool;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.cookiepool.model.CookiePool;
import com.top_logic.tl.ai.core.cookiepool.model.TlAiCookiepoolFactory;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link CookiePoolService}: the storageState is encrypted at rest (round-trips on read,
 * not readable as plaintext), the owner-vs-shared selection filter isolates private pools while
 * exposing shared ones (including to an unattended, no-user session), a <em>private</em> save
 * without an owner is refused rather than producing a pool nobody can select, and the explicit
 * shared save is the one path that works without an owner - without ever widening a private pool.
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
			pool = _service.upsertPool("round-trip", STATE_JSON, _alice, false);
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
			priv = _service.upsertPool("alice-private", STATE_JSON, _alice, false);
			shared = _service.upsertPool("team-shared", STATE_JSON, _alice, false);
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
			_service.upsertPool("list-alice-private", STATE_JSON, _alice, false);
			CookiePool shared = _service.upsertPool("list-team-shared", STATE_JSON, _alice, false);
			shared.setShared(Boolean.TRUE);
			_service.upsertPool("list-bob-private", STATE_JSON, _bob, false);
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

	/**
	 * An unattended <em>private</em> save (no {@link Person} to own the pool) is refused, and creates
	 * nothing: an owner-less private pool would be selectable by no one, so it must not reach the
	 * store. The explicit shared save is the only owner-less path, see
	 * {@link #testUnattendedSharedSaveRoundTrips()}.
	 */
	public void testUnattendedSaveIsRefusedAndCreatesNothing() throws Exception {
		String name = "unattended-refused";
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			try {
				_service.upsertPool(name, STATE_JSON, null, false);
				fail("Saving a private cookie pool without an owner must be refused.");
			} catch (IllegalArgumentException expected) {
				assertContains("The refusal must name the missing owner as the reason.", "owner",
					expected.getMessage());
			}
			tx.commit();
		}

		// Assert on the store, not just on the message: nothing was created, under any visibility.
		assertFalse("no pool must exist for the refused unattended save",
			_service.findSelectablePool(name, null).isPresent());
		assertFalse("no pool must exist for the refused unattended save",
			_service.findSelectablePool(name, _alice).isPresent());
		assertFalse("no pool object of that name must have been created at all",
			names(allPools()).contains(name));
	}

	/**
	 * The name-collision case from the ticket - two unattended callers saving the same pool name
	 * privately - is unreachable now that such a save is refused: neither call creates a pool, so
	 * neither can overwrite the other's login.
	 */
	public void testUnattendedNameCollisionCannotHappen() throws Exception {
		String name = "shared-bucket";
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			for (int run = 0; run < 2; run++) {
				try {
					_service.upsertPool(name, STATE_JSON, null, false);
					fail("Unattended private save " + run + " must be refused.");
				} catch (IllegalArgumentException expected) {
					// Expected: no bucket to collide in.
				}
			}
			tx.commit();
		}
		assertFalse("no unattended pool must exist to be overwritten", names(allPools()).contains(name));
	}

	/** The owned path still works end to end: the save succeeds and the pool stays selectable by its owner. */
	public void testOwnedSaveRemainsSelectable() throws Exception {
		CookiePool pool;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			pool = _service.upsertPool("owned-selectable", STATE_JSON, _alice, false);
			tx.commit();
		}
		assertSame("the pool must be owned by its saver", _alice, pool.getOwner());
		assertEquals("a saved pool defaults to private", Boolean.FALSE, pool.getShared());

		Optional<CookiePool> selected = _service.findSelectablePool("owned-selectable", _alice);
		assertTrue("the owner must be able to select the pool they just saved", selected.isPresent());
		assertSame(pool, selected.get());
		assertTrue("the selected pool must still decrypt to the saved state",
			Arrays.equals(STATE_JSON, _service.decryptState(selected.get())));
	}

	/**
	 * The round trip that was impossible before: an unattended run (no {@link Person}) saves a pool
	 * with the explicit shared flag, and can select it again afterwards - as can any user.
	 */
	public void testUnattendedSharedSaveRoundTrips() throws Exception {
		String name = "unattended-shared";
		CookiePool pool;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			pool = _service.upsertPool(name, STATE_JSON, null, true);
			tx.commit();
		}
		assertEquals("an explicit shared save must mark the pool shared", Boolean.TRUE, pool.getShared());
		assertNull("an unattended save has no Person to record as owner", pool.getOwner());

		Optional<CookiePool> unattendedView = _service.findSelectablePool(name, null);
		assertTrue("the unattended run must find the shared pool it just saved", unattendedView.isPresent());
		assertSame(pool, unattendedView.get());
		assertTrue("the pool must decrypt to the saved state",
			Arrays.equals(STATE_JSON, _service.decryptState(unattendedView.get())));
		assertTrue("a shared pool is selectable by any user", _service.findSelectablePool(name, _bob).isPresent());
	}

	/** A shared save by a logged-in user records that user as the owner AND marks the pool shared. */
	public void testSharedSaveRecordsOwnerAndSharesPool() throws Exception {
		String name = "owned-shared";
		CookiePool pool;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			pool = _service.upsertPool(name, STATE_JSON, _alice, true);
			tx.commit();
		}
		assertSame("a resolvable Person must still be recorded as the owner", _alice, pool.getOwner());
		assertEquals("the pool must be marked shared", Boolean.TRUE, pool.getShared());
		assertSame("the owner selects their own pool", pool, _service.findSelectablePool(name, _alice).get());
		assertSame("another user selects it because it is shared", pool,
			_service.findSelectablePool(name, _bob).get());
	}

	/**
	 * A second shared save under the same name updates that shared pool in place - whoever owns it -
	 * instead of piling up a duplicate; the recorded owner survives an unattended update.
	 */
	public void testSharedSaveUpdatesSharedPoolInPlace() throws Exception {
		String name = "shared-in-place";
		byte[] otherState = "{\"cookies\":[],\"origins\":[]}".getBytes(StandardCharsets.UTF_8);
		CookiePool first;
		CookiePool second;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			first = _service.upsertPool(name, STATE_JSON, _alice, true);
			tx.commit();
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			// An unattended run updating the shared login alice saved.
			second = _service.upsertPool(name, otherState, null, true);
			tx.commit();
		}
		assertSame("the second shared save must update the same pool", first, second);
		assertEquals("no duplicate pool must be created", 1, poolsNamed(name).size());
		assertSame("the recorded owner must survive an unattended update", _alice, second.getOwner());
		assertEquals("the pool must stay shared", Boolean.TRUE, second.getShared());
		assertTrue("the state must reflect the second save",
			Arrays.equals(otherState, _service.decryptState(second)));
	}

	/**
	 * A shared save is refused when the caller already owns a private pool of that name: selection
	 * prefers their own pool, so the shared pool would be invisible to the very caller who asked for
	 * it. The private pool must be left alone - never widened - and no second pool created.
	 */
	public void testSharedSaveRefusedWhenOwnPrivatePoolExists() throws Exception {
		String name = "private-then-shared";
		CookiePool priv;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			priv = _service.upsertPool(name, STATE_JSON, _alice, false);
			tx.commit();
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			try {
				_service.upsertPool(name, STATE_JSON, _alice, true);
				fail("A shared save colliding with the caller's own private pool must be refused.");
			} catch (IllegalArgumentException expected) {
				assertContains("The refusal must name the private pool as the reason.", "private",
					expected.getMessage());
			}
			tx.commit();
		}
		assertEquals("the refused shared save must not have created a pool", 1, poolsNamed(name).size());
		assertEquals("the caller's private pool must not have been widened", Boolean.FALSE, priv.getShared());
		assertFalse("nothing shared must have become selectable for an unattended run",
			_service.findSelectablePool(name, null).isPresent());
	}

	/**
	 * Somebody else's private pool of the same name blocks nothing and is not touched: the shared
	 * save creates its own pool, and each caller keeps selecting the one meant for them.
	 */
	public void testSharedSaveLeavesForeignPrivatePoolAlone() throws Exception {
		String name = "foreign-private";
		CookiePool bobsPrivate;
		CookiePool shared;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			bobsPrivate = _service.upsertPool(name, STATE_JSON, _bob, false);
			tx.commit();
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			shared = _service.upsertPool(name, STATE_JSON, _alice, true);
			tx.commit();
		}
		assertNotSame("bob's private pool must not be reused by a shared save", bobsPrivate, shared);
		assertEquals("bob's private pool must stay private", Boolean.FALSE, bobsPrivate.getShared());
		assertEquals("the shared pool must be marked shared", Boolean.TRUE, shared.getShared());
		assertSame("bob keeps selecting his own private pool", bobsPrivate,
			_service.findSelectablePool(name, _bob).get());
		assertSame("an unattended run selects the shared pool", shared,
			_service.findSelectablePool(name, null).get());
	}

	/** Saving again under the same name updates the caller's pool in place rather than creating a second. */
	public void testUpsertUpdatesOwnPoolInPlace() throws Exception {
		CookiePool first;
		CookiePool second;
		byte[] otherState = "{\"cookies\":[],\"origins\":[]}".getBytes(StandardCharsets.UTF_8);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			first = _service.upsertPool("reused", STATE_JSON, _alice, false);
			tx.commit();
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			second = _service.upsertPool("reused", otherState, _alice, false);
			tx.commit();
		}
		assertSame("second save must update the same pool object", first, second);
		assertTrue("state must reflect the second save", Arrays.equals(otherState, _service.decryptState(second)));
	}

	private static List<String> names(List<CookiePool> pools) {
		return pools.stream().map(CookiePool::getName).toList();
	}

	/** Every pool in the store, regardless of owner or shared flag - the check for "nothing was created". */
	private static List<CookiePool> allPools() {
		List<CookiePool> result = new ArrayList<>();
		for (TLObject obj : MetaElementUtil.getAllInstancesOf(TlAiCookiepoolFactory.getCookiePoolType(),
			TLObject.class)) {
			result.add((CookiePool) obj);
		}
		return result;
	}

	/** Every pool carrying that name, regardless of owner or shared flag - the duplicate check. */
	private static List<CookiePool> poolsNamed(String name) {
		return allPools().stream().filter(pool -> name.equals(pool.getName())).toList();
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
