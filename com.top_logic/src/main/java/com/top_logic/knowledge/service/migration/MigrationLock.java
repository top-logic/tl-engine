/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.SQLException;
import java.time.Instant;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;

/**
 * Database lock that serializes the data migration of the nodes of a cluster.
 *
 * <p>
 * The lock is the global {@link DBProperties database property} {@value #LOCK_PROPERTY}. A node
 * acquires the lock by creating the property with its {@link #getOwner() owner identification} as
 * value, and releases the lock by deleting the property. Creating the property is atomic due to the
 * primary key of the properties table: Of several nodes trying concurrently, only one succeeds, the
 * others wait until the property is deleted.
 * </p>
 *
 * <p>
 * The owner identification consists of the runtime name of the JVM (<code>pid@host</code>) and
 * the start time of the JVM. The lock is not released when the node holding it crashes during the
 * migration. In that case the state of the database is unknown: The database must be restored from
 * a backup, which does not contain the lock property. A node that finds a lock with its own runtime
 * name but another start time knows that the holder was a former run of itself that has been
 * terminated (e.g. after a restart of its container, where the process ID is always the same). It
 * does not wait for the lock but fails with an error. A lock held by another node is waited for,
 * since this node cannot tell whether the holder is still alive.
 * </p>
 */
public final class MigrationLock implements AutoCloseable {

	/**
	 * Name of the global {@link DBProperties database property} representing the lock.
	 */
	public static final String LOCK_PROPERTY = "migration.lock";

	/**
	 * Separator between the runtime name and the start time in the {@link #getOwner() owner
	 * identification}.
	 */
	private static final String SINCE = " since ";

	/**
	 * Milliseconds to wait before checking again whether the lock has been released.
	 */
	private static final long RETRY_DELAY = 1000;

	/**
	 * Milliseconds between log messages reporting that the node is still waiting for the lock.
	 */
	private static final long REPORT_INTERVAL = 60 * 1000;

	/**
	 * Result of {@link #tryLock(Log, ConnectionPool, String)}, if the lock was not acquired but the
	 * holder is unknown: The holder released the lock in the meantime, or another node created the
	 * lock property concurrently.
	 */
	private static final String UNKNOWN_HOLDER = "";

	private final Log _log;

	private final ConnectionPool _pool;

	private final String _owner;

	private boolean _released;

	private MigrationLock(Log log, ConnectionPool pool, String owner) {
		_log = log;
		_pool = pool;
		_owner = owner;
	}

	/**
	 * Identification of the node holding this lock, the value of the {@link #LOCK_PROPERTY}.
	 */
	public String getOwner() {
		return _owner;
	}

	/**
	 * Acquires the migration lock, waits until another node has released it.
	 *
	 * <p>
	 * A lock with the runtime name of this JVM but another start time was left behind by a former
	 * run of this node that has been terminated during a migration: Instead of waiting forever,
	 * the acquisition fails. A lock with the {@link #getOwner() owner identification} of this JVM
	 * is held by another acquisition in this JVM: It is waited for like the lock of another node,
	 * it is never taken over.
	 * </p>
	 *
	 * @param log
	 *        Log to report progress to.
	 * @param pool
	 *        The pool of the database to lock.
	 * @return The acquired lock, must be {@link #close() closed} after the migration.
	 * @throws InterruptedException
	 *         If the thread is interrupted while waiting for the lock.
	 * @throws IllegalStateException
	 *         If the lock was left behind by a former run of this node.
	 */
	public static MigrationLock acquire(Log log, ConnectionPool pool) throws InterruptedException {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String runtimeName = runtime.getName();
		String owner = runtimeName + SINCE + Instant.ofEpochMilli(runtime.getStartTime());

		log.info("Acquiring migration lock.");
		StopWatch timer = StopWatch.createStartedWatch();
		String reportedHolder = null;
		long lastReport = 0;
		while (true) {
			String holder = tryLock(log, pool, owner);
			if (holder == null) {
				break;
			}
			long now = System.currentTimeMillis();
			if (holder == UNKNOWN_HOLDER) {
				// Retry without reporting, the holder is known at the next attempt.
			} else if (isFormerRun(holder, runtimeName, owner)) {
				String message = "Migration lock is held by '" + holder + "': This node was terminated during a"
					+ " migration started at " + holder.substring(runtimeName.length() + SINCE.length())
					+ ". The state of the database is unknown. Restore the database from a backup, or delete the"
					+ " global database property '" + LOCK_PROPERTY + "' after checking the state of the database.";
				throw new IllegalStateException(message);
			} else if (!holder.equals(reportedHolder)) {
				log.info("Migration lock is held by '" + holder + "', waiting. If that node was terminated during"
					+ " the migration, the database must be restored from a backup (alternatively, the global"
					+ " database property '" + LOCK_PROPERTY + "' can be deleted after checking the state of"
					+ " the database).");
				reportedHolder = holder;
				lastReport = now;
			} else if (now - lastReport >= REPORT_INTERVAL) {
				log.info("Still waiting for migration lock held by '" + holder + "' (waiting for " + timer + ").");
				lastReport = now;
			}
			Thread.sleep(RETRY_DELAY);
		}
		log.info("Acquired migration lock in " + timer + ".");

		return new MigrationLock(log, pool, owner);
	}

	/**
	 * Whether the given lock holder is a former run of this node.
	 *
	 * @param holder
	 *        The current value of the {@link #LOCK_PROPERTY}.
	 * @param runtimeName
	 *        The runtime name of this JVM.
	 * @param owner
	 *        The {@link #getOwner() owner identification} of this JVM.
	 */
	private static boolean isFormerRun(String holder, String runtimeName, String owner) {
		return holder.startsWith(runtimeName + SINCE) && !holder.equals(owner);
	}

	/**
	 * Tries to create the lock property.
	 *
	 * @return <code>null</code>, if the lock was acquired, the current holder otherwise, or
	 *         {@link #UNKNOWN_HOLDER}, if the holder could not be determined.
	 */
	private static String tryLock(Log log, ConnectionPool pool, String owner) {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			if (DBProperties.compareAndSet(connection, DBProperties.GLOBAL_PROPERTY, LOCK_PROPERTY, null, owner)) {
				connection.commit();
				return null;
			}
			String holder = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, LOCK_PROPERTY);
			connection.rollback();

			// Note: The holder may have released the lock in the meantime.
			return holder == null ? UNKNOWN_HOLDER : holder;
		} catch (SQLException ex) {
			// E.g. a duplicate key, when another node created the property concurrently.
			rollback(connection);
			log.info("Concurrent access to migration lock: " + ex.getMessage(), Log.DEBUG);
			return UNKNOWN_HOLDER;
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Creates the lock property again after the properties table has been dropped and re-created
	 * during the migration.
	 *
	 * <p>
	 * Between re-creating the table and calling this method, another node may acquire the lock.
	 * </p>
	 *
	 * @return Whether the lock is held by this node.
	 */
	public boolean restore() {
		String holder;
		while ((holder = tryLock(_log, _pool, _owner)) == UNKNOWN_HOLDER) {
			try {
				Thread.sleep(RETRY_DELAY);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				_log.error("Interrupted while restoring the migration lock.", ex);
				return false;
			}
		}
		if (holder == null) {
			return true;
		}
		if (_owner.equals(holder)) {
			// Still present.
			return true;
		}
		_log.error("Migration lock was taken by '" + holder + "' while migrating.");
		return false;
	}

	/**
	 * Releases the lock.
	 */
	@Override
	public void close() {
		if (_released) {
			return;
		}
		_released = true;

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			if (DBProperties.compareAndSet(connection, DBProperties.GLOBAL_PROPERTY, LOCK_PROPERTY, _owner, null)) {
				connection.commit();
				_log.info("Released migration lock.");
			} else {
				connection.rollback();
				_log.info("Migration lock was no longer held by this node.", Log.WARN);
			}
		} catch (SQLException ex) {
			rollback(connection);
			_log.error("Unable to release migration lock, delete the global database property '" + LOCK_PROPERTY
				+ "' manually.", ex);
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	private static void rollback(PooledConnection connection) {
		try {
			connection.rollback();
		} catch (SQLException ex) {
			// Ignore, the original problem is reported.
		}
	}

}
