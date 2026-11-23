/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.TLContextManager;

/**
 * Checks the rules of a {@link CredentialProvider#isPersonal() personal} {@link CredentialProvider}.
 *
 * <p>
 * The test of a personal provider extends this class, implements {@link #createProvider()} and
 * {@link #connect(Person)}, and runs in a suite with a knowledge base and the person manager, e.g.
 * <code>PersonManagerSetup.createPersonManagerSetup(...)</code>, together with the services the
 * provider needs.
 * </p>
 *
 * <p>
 * The checks assume that the provider sends the same credential of a person again until it is
 * invalidated.
 * </p>
 */
public abstract class AbstractPersonalCredentialProviderTest extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	private Person _anna;

	private Person _ben;

	private CredentialProvider _provider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_anna = TestPerson.createPerson("personalCredentialsAnna");
		_ben = TestPerson.createPerson("personalCredentialsBen");
		_provider = createProvider();
	}

	@Override
	protected void tearDown() throws Exception {
		Person anna = _anna;
		Person ben = _ben;
		_anna = null;
		_ben = null;
		_provider = null;
		// Another thread may have changed the accounts after the session revision of this thread.
		inNewThread(() -> ThreadContext.inSystemContext(AbstractPersonalCredentialProviderTest.class, () -> {
			for (Person person : new Person[] { anna, ben }) {
				if (person.tValid()) {
					TestPerson.deletePersonAndUser(person);
				}
			}
			return null;
		}));
		super.tearDown();
	}

	/**
	 * Creates the provider to check.
	 */
	protected abstract CredentialProvider createProvider() throws Exception;

	/**
	 * Connects the account of the given person in the other system, so that the provider has a
	 * credential of this person.
	 */
	protected abstract void connect(Person person) throws Exception;

	/** The provider says that it is personal. */
	public void testIsPersonal() {
		assertTrue(_provider.isPersonal());
	}

	/** The acting person gets the own credential, not the credential of another person. */
	public void testActingPersonGetsOwnCredential() throws Exception {
		connect(_anna);
		connect(_ben);

		Map<String, String> ofAnna = asActingPerson(_anna, this::authenticate);
		Map<String, String> ofBen = asActingPerson(_ben, this::authenticate);

		assertFalse("No credential was added for the acting person.", ofAnna.isEmpty());
		assertFalse("No credential was added for the acting person.", ofBen.isEmpty());
		assertFalse("Two persons got the same credential.", ofAnna.equals(ofBen));
	}

	/** A run of the account that no person started - a scheduled task - is refused. */
	public void testRefusedWithoutActingPerson() throws Exception {
		connect(_anna);

		TLContextManager.inPersonContext(_anna, this::assertRefused);
	}

	/** A step in a system context of a request of the person is refused. */
	public void testRefusedInSystemContext() throws Exception {
		connect(_anna);

		asActingPerson(_anna, () -> {
			ThreadContext.inSystemContext(AbstractPersonalCredentialProviderTest.class, this::assertRefused);
			return null;
		});
	}

	/**
	 * A step in which another account acts than the acting person is refused, also when that account
	 * is connected.
	 */
	public void testRefusedForAnotherAccount() throws Exception {
		connect(_anna);
		connect(_ben);

		asActingPerson(_anna, () -> {
			TLContextManager.inPersonContext(_ben, this::assertRefused);
			return null;
		});
	}

	/**
	 * A person who did not connect the account is refused: the provider never falls back to the
	 * credential of another person or of the application.
	 */
	public void testRefusedWithoutConnectedAccount() throws Exception {
		connect(_ben);

		asActingPerson(_anna, () -> {
			assertRefused();
			return null;
		});
	}

	/** A run of a person whose account is locked meanwhile is refused at its next call. */
	public void testRefusedForLockedAccount() throws Exception {
		connect(_anna);

		asActingPerson(_anna, () -> {
			assertFalse(authenticate().isEmpty());
			inNewThread(() -> ThreadContext.inSystemContext(AbstractPersonalCredentialProviderTest.class, () -> {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					_anna.setLocked(true);
					tx.commit();
				}
				return null;
			}));
			assertRefused();
			return null;
		});
	}

	/** A run of a person whose account is deleted meanwhile is refused at its next call. */
	public void testRefusedForDeletedAccount() throws Exception {
		connect(_anna);

		asActingPerson(_anna, () -> {
			assertFalse(authenticate().isEmpty());
			inNewThread(() -> ThreadContext.inSystemContext(AbstractPersonalCredentialProviderTest.class, () -> {
				TestPerson.deletePersonAndUser(_anna);
				return null;
			}));
			assertRefused();
			return null;
		});
	}

	/**
	 * A background thread of a library, which knows no acting person, is refused, also while the
	 * thread that started it has an acting person.
	 */
	public void testRefusedOnLibraryThread() throws Exception {
		connect(_anna);

		asActingPerson(_anna, () -> {
			assertFalse(authenticate().isEmpty());
			inNewThread(() -> {
				assertRefused();
				return null;
			});
			return null;
		});
	}

	/**
	 * {@link CredentialProvider#invalidate()} asks for the acting person: it drops only the credential
	 * of the acting person, and it is refused without an acting person.
	 */
	public void testInvalidateActsForActingPerson() throws Exception {
		connect(_anna);
		connect(_ben);

		Map<String, String> before = asActingPerson(_anna, this::authenticate);
		asActingPerson(_ben, () -> {
			_provider.invalidate();
			return null;
		});
		assertEquals("The invalidation for another person dropped the credential of the acting person.", before,
			asActingPerson(_anna, this::authenticate));

		TLContextManager.inPersonContext(_anna, () -> {
			try {
				_provider.invalidate();
				fail("An invalidation without acting person must be refused.");
			} catch (I18NRuntimeException ex) {
				// Expected.
			}
		});
	}

	/**
	 * Runs the given job as a request of the given person in a browser window.
	 */
	protected static <T> T asActingPerson(Person person, ComputationEx2<T, Exception, RuntimeException> job)
			throws Exception {
		return TLContextManager.inPersonContext(person, () -> {
			try (ThreadLocalBinding<Person>.Handle acting = ActingPerson.bind(person)) {
				return job.run();
			}
		});
	}

	/**
	 * The headers the provider adds to a request in the current context.
	 */
	protected Map<String, String> authenticate() {
		Map<String, String> headers = new LinkedHashMap<>();
		_provider.authenticate(headers::put);
		return headers;
	}

	/**
	 * Asserts that the provider refuses a request in the current context and adds nothing to it.
	 */
	protected void assertRefused() {
		Map<String, String> headers = new LinkedHashMap<>();
		try {
			_provider.authenticate(headers::put);
			fail("The request must be refused: no acting person, or no credential of the acting person.");
		} catch (I18NRuntimeException ex) {
			assertTrue("A refused request got headers: " + headers.keySet(), headers.isEmpty());
		}
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, AbstractPersonalCredentialProviderTest.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

}
