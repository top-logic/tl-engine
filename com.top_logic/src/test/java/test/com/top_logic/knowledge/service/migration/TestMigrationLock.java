/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.db.schema.properties.DBPropertiesTableSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.migration.MigrationLock;

/**
 * Test case for {@link MigrationLock}.
 */
public class TestMigrationLock extends BasicTestCase {

	private ConnectionPool _pool;

	private DBProperties _properties;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_properties = new DBProperties(_pool);
		_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
	}

	@Override
	protected void tearDown() throws Exception {
		_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
		_properties = null;
		_pool = null;
		super.tearDown();
	}

	/**
	 * The lock property exists exactly while the lock is held.
	 */
	public void testLockProperty() throws Exception {
		try (MigrationLock lock = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
			assertEquals(lock.getOwner(), lockProperty());
		}
		assertNull(lockProperty());
	}

	/**
	 * A second acquirer (another node) waits until the first one has released the lock.
	 */
	public void testSecondWaitsForFirst() throws Exception {
		AtomicBoolean secondAcquired = new AtomicBoolean();

		TestFuture second;
		try (MigrationLock first = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
			second = inParallel(new Execution() {
				@Override
				public void run() throws Exception {
					try (MigrationLock lock = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
						secondAcquired.set(true);
					}
				}
			});

			Thread.sleep(2500);
			assertFalse("Lock acquired while held by another node.", secondAcquired.get());
		}

		second.check(30000);
		assertTrue("Lock not acquired after release.", secondAcquired.get());
	}

	/**
	 * A lock left behind by a terminated node blocks until the property is deleted manually.
	 */
	public void testLeftOverLockBlocks() throws Exception {
		_properties.setProperty(MigrationLock.LOCK_PROPERTY, "crashed node");

		AtomicBoolean acquired = new AtomicBoolean();
		TestFuture future = inParallel(new Execution() {
			@Override
			public void run() throws Exception {
				try (MigrationLock lock = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
					acquired.set(true);
				}
			}
		});

		Thread.sleep(2500);
		assertFalse("Lock acquired while held by another node.", acquired.get());

		_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
		future.check(30000);
		assertTrue("Lock not acquired after manual deletion.", acquired.get());
	}

	/**
	 * After the properties table has been re-created, the lock is restored.
	 */
	public void testRestore() throws Exception {
		try (MigrationLock lock = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
			// Simulate re-creating the properties table.
			_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);

			assertTrue(lock.restore());
			assertEquals(lock.getOwner(), lockProperty());

			// Restoring a lock still present is a no-op.
			assertTrue(lock.restore());
			assertEquals(lock.getOwner(), lockProperty());
		}
		assertNull(lockProperty());
	}

	/**
	 * Restoring fails, if another node took the lock in the meantime. Releasing does not delete
	 * the lock of the other node.
	 */
	public void testRestoreTakenByOther() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		try (MigrationLock lock = MigrationLock.acquire(log, _pool)) {
			_properties.setProperty(MigrationLock.LOCK_PROPERTY, "other node");

			assertFalse(lock.restore());
			assertTrue(log.hasErrors());
		}
		assertEquals("other node", lockProperty());
	}

	/**
	 * Closing a lock twice is harmless.
	 */
	public void testCloseTwice() throws Exception {
		MigrationLock lock = MigrationLock.acquire(new BufferingProtocol(), _pool);
		lock.close();
		lock.close();

		try (MigrationLock other = MigrationLock.acquire(new BufferingProtocol(), _pool)) {
			assertEquals(other.getOwner(), lockProperty());
		}
	}

	private String lockProperty() {
		return _properties.getProperty(MigrationLock.LOCK_PROPERTY);
	}

	/**
	 * Creates a test suite with a database.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			DatabaseTestSetup.getDBTest(TestMigrationLock.class, new TestFactory() {
				@Override
				public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
					Test test = new TestSuite(testCase);
					test = DBPropertiesTableSetup.setup(test);
					test = ServiceTestSetup.createSetup(test, DataAccessService.Module.INSTANCE);
					return ServiceTestSetup.withThreadContext(test);
				}
			}));
	}

}
