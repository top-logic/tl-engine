/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.UnboundListener;

/**
 * {@link ConnectionPool} base implementation that can be configured through a
 * {@link AbstractConfiguredConnectionPool.Config} configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfiguredConnectionPool extends AbstractConfiguredConnectionPoolBase {

	/**
	 * Configuration options for an {@link AbstractConfiguredConnectionPool}.
	 */
	public interface Config extends AbstractConfiguredConnectionPoolBase.Config {

		/**
		 * Settings for pooling read connections.
		 */
		@Name("read-pool")
		@ItemDefault
		PoolSettings getReadPool();

		/**
		 * Settings for pooling write connections.
		 */
		@Name("write-pool")
		@ItemDefault
		PoolSettings getWritePool();

		/**
		 * Warn, if read connection allocation is nested.
		 * 
		 * <p>
		 * Note: Nesting read connection allocation cannot be avoided in all circumstances. Passing
		 * a read connection from the point of allocation to all usage locations is only a matter of
		 * optimization. Enabling this option allows finding such locations.
		 * </p>
		 */
		@Name("warn-nested-read-allocation")
		boolean getWarnNestedReadAllocation();

		/**
		 * Enable tracking borrow operations.
		 * 
		 * <p>
		 * Each borrow operation associates the borrowed connection with the stack trace of the
		 * allocator. If a release error is detected, the stack trace of the allocator is available.
		 * </p>
		 * 
		 * <p>
		 * Note: Enabling this option degrades system performance.
		 * </p>
		 */
		@Name("debug-resources")
		boolean getDebugResources();

	}

	/**
	 * @see GenericObjectPool#setWhenExhaustedAction(byte)
	 */
	static final String WHEN_EXHAUSTED_ACTION_PROPERTY = "whenExhaustedAction";
	
	/**
	 * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis(long)
	 */
	static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS_PROPERTY = "timeBetweenEvictionRunsMillis";
	
	/**
	 * @see GenericObjectPool#setTestWhileIdle(boolean)
	 */
	static final String TEST_WHILE_IDLE_PROPERTY = "testWhileIdle";
	
	/**
	 * @see GenericObjectPool#setTestOnReturn(boolean)
	 */
	static final String TEST_ON_RETURN_PROPERTY = "testOnReturn";
	
	/**
	 * @see GenericObjectPool#setTestOnBorrow(boolean)
	 */
	static final String TEST_ON_BORROW_PROPERTY = "testOnBorrow";
	
	/**
	 * @see GenericObjectPool#setSoftMinEvictableIdleTimeMillis(long)
	 */
	static final String SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS_PROPERTY = "softMinEvictableIdleTimeMillis";
	
	/**
	 * @see GenericObjectPool#setNumTestsPerEvictionRun(int)
	 */
	static final String NUM_TESTS_PER_EVICTION_RUN_PROPERTY = "numTestsPerEvictionRun";
	
	/**
	 * @see GenericObjectPool#setMinIdle(int)
	 */
	static final String MIN_IDLE_PROPERTY = "minIdle";
	
	/**
	 * @see GenericObjectPool#setMinEvictableIdleTimeMillis(long)
	 */
	static final String MIN_EVICTABLE_IDLE_TIME_MILLIS_PROPERTY = "minEvictableIdleTimeMillis";
	
	/**
	 * @see GenericObjectPool#setMaxWait(long)
	 */
	static final String MAX_WAIT_PROPERTY = "maxWait";
	
	/**
	 * @see GenericObjectPool#setMaxActive(int)
	 */
	static final String MAX_ACTIVE_PROPERTY = "maxActive";
	
	/**
	 * @see GenericObjectPool#setLifo(boolean)
	 */
	static final String LIFO_PROPERTY = "lifo";
	
	private final boolean _debugResources;

	private final boolean _warnNestedRead;

	/**
	 * Internal read pool implementation.
	 */
	private final ObjectPool readPool;
	
	/**
	 * Internal write pool implementation.
	 */
	private final ObjectPool writePool;

	/**
	 * {@link Property} to store a {@link LocalConnections} object to check correct connection
	 * release.
	 */
	private final Property<LocalConnections> _localCache = TypedAnnotatable.property(LocalConnections.class, null);

    /**
	 * Creates a new {@link AbstractConfiguredConnectionPool} using the given configuration.
	 * 
	 * @param aDs
	 *        The {@link DataSource} that should be used by the pool to create new connections from.
	 * @param sqlDialect
	 *        The {@link DBHelper} to use.
	 * @param config
	 *        The configuration.
	 */
	AbstractConfiguredConnectionPool(DataSource aDs, DBHelper sqlDialect, Config config) {
		super(aDs, sqlDialect, config);
		
		this.readPool = initReadPool(config);
		this.writePool = initWritePool(config);

		_warnNestedRead = false;
		_debugResources = false;
	}

	/**
	 * Creates a {@link AbstractConfiguredConnectionPool} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConfiguredConnectionPool(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		this.readPool = initReadPool(config);
		this.writePool = initWritePool(config);

		boolean warnNestedReadAllocation = config.getWarnNestedReadAllocation();
		boolean debugResources = config.getDebugResources();

		_warnNestedRead = warnNestedReadAllocation;
		_debugResources = debugResources || warnNestedReadAllocation;
	}
	
	private GenericObjectPool initWritePool(Config config) {
		return initPool(new ConfigAdapter().setValues(config.getReadPool()).setValues(config.getWritePool()),
			ConnectionType.WRITE);
	}

	private GenericObjectPool initReadPool(Config config) {
		return initPool(new ConfigAdapter().setValues(config.getReadPool()), ConnectionType.READ);
	}

	private GenericObjectPool initPool(GenericObjectPool.Config config, ConnectionType connectionType) {
		return new GenericObjectPool(new PooledConnectionFactory(this, connectionType), config);
	}

	/**
	 * See {@link Config#getDebugResources()}
	 */
	public boolean debugResources() {
		return _debugResources;
	}

	/**
	 * See {@link Config#getWarnNestedReadAllocation()}
	 */
	public boolean warnNestedRead() {
		return _warnNestedRead;
	}

	@Override
	public PooledConnection borrowWriteConnection() {
				
		// To avoid deadlock, request read connection first, so that a thread can complete his tasks definitely
		// (excepting real deadlock on database)
		borrowReadConnection();
		
		LocalConnections localConnections = getLocalConnections();
		assert localConnections != null : "Borrow read connection ensures existence of cache.";

		ObservedPooledConnectionImpl result = internalBorrowConnection(ConnectionType.WRITE);
		assert !checkConnectionAutoCommitState(result, false);
		assert !checkConnectionReadOnlyState(result, false);

		localConnections.notifyBorrowWriteConnection(result);

		return result;
	}
	
	@Override
	public PooledConnection borrowReadConnection() {
		LocalConnections localConnections = getLocalConnections();
		if (localConnections != null) {
			ObservedPooledConnectionImpl alreadBorrowedConnection = localConnections.getLocalReadConnection();
			if (alreadBorrowedConnection != null) {
				return alreadBorrowedConnection;
			}
		} else {
			localConnections = new LocalConnections(this);
			setLocalConnections(localConnections);
		}

		ObservedPooledConnectionImpl newConnection = internalBorrowConnection(ConnectionType.READ);
		assert checkConnectionAutoCommitState(newConnection, true);
		assert checkConnectionReadOnlyState(newConnection, true);

		// Remember for later check.
		localConnections.initReadConnection(newConnection);

		return newConnection;
	}

	@Override
	public void releaseReadConnection(PooledConnection connection) {
		LocalConnections localConnections = getLocalConnections();
		if (localConnections.notifyReleaseReadConnection(connection)) {
			internalReleaseReadConnection(connection);
		}
	}

	/**
	 * Internal callback for the {@link #connectionReleaser} to free connections
	 * without triggering release events.
	 */
	/*package protected*/ final void internalReleaseReadConnection(PooledConnection connection) {
		internalReleaseConnection(connection, ConnectionType.READ);
	}
	
	@Override
	public void invalidateReadConnection(PooledConnection connection) {
		LocalConnections localConnections = getLocalConnections();
		if (localConnections.notifyReleaseReadConnection(connection)) {
			internalInvalidateConnection(connection, ConnectionType.READ);
		} else {
			// Try to heal connection.
			connection.closeConnection(null);
		}
	}

	@Override
	public void releaseWriteConnection(PooledConnection connection) {
		LocalConnections localConnections = getLocalConnections();
		if (localConnections.notifyReleaseWriteConnection(connection)) {
			internalReleaseWriteConnection(connection);
		}
		
		ObservedPooledConnectionImpl alreadyBorrowedConnection = localConnections.internalGetLocalReadConnection();
		if(alreadyBorrowedConnection != null) {
			releaseReadConnection(alreadyBorrowedConnection);
		}
	}
	
	/**
	 * Internal callback for the {@link #connectionReleaser} to free connections
	 * without triggering release events.
	 */
	/*package protected*/ final void internalReleaseWriteConnection(PooledConnection connection) {
		internalReleaseConnection(connection, ConnectionType.WRITE);
	}

	@Override
	public void invalidateWriteConnection(PooledConnection connection) {
		LocalConnections localConnections = getLocalConnections();
		if (localConnections.notifyReleaseWriteConnection(connection)) {
			internalInvalidateConnection(connection, ConnectionType.WRITE);
		}
		
		ObservedPooledConnectionImpl alreadyBorrowedConnection = localConnections.internalGetLocalReadConnection();
		if(alreadyBorrowedConnection != null) {
			invalidateReadConnection(alreadyBorrowedConnection);
		}	
	}
	
	/**
	 * Getter for the thread-local {@link LocalConnections} object.
	 */
	private final LocalConnections getLocalConnections() {
		return getLocalConnections(currentInteraction());
	}

	/* package protected */LocalConnections getLocalConnections(InteractionContext currentInteraction) {
		return currentInteraction.get(_localCache);
	}
	
	/**
	 * Setter for the thread-local {@link LocalConnections} object.
	 */
	private void setLocalConnections(LocalConnections localConnections) {
		InteractionContext currentInteraction = currentInteraction();
		currentInteraction.set(_localCache, localConnections);
		currentInteraction.addUnboundListener(connectionReleaser);
	}

	private InteractionContext currentInteraction() {
		return ThreadContextManager.getInteraction();
	}
	
	/**
	 * Internal implementation of borrowing a connection.
	 */
	private ObservedPooledConnectionImpl internalBorrowConnection(ConnectionType connectionType) {
		try {
			
			ObservedPooledConnectionImpl connection = null;
			switch(connectionType) {
				case READ: {
					connection = (ObservedPooledConnectionImpl) readPool.borrowObject();
					break;
				}
				
				case WRITE: {
					connection = (ObservedPooledConnectionImpl) writePool.borrowObject();
					break;
				}
				
				default: {
					throw new IllegalStateException("The given connection type was invalid. Must be either ConnectionType.READ or ConnectionType.WRITE");
				}
			}
			if (debugResources()) {
				connection.initBorrowStackTrace();
			}
			return connection;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new UnreachableAssertion("Statement cache factory does not throw any declared exceptions.", ex);
		}
	}
	
	/**
	 * Internal implementation of releasing a connection.
	 */
	private final void internalReleaseConnection(PooledConnection connection, ConnectionType connectionType) {
		try {
			switch(connectionType) {
				case READ: {
					readPool.returnObject(connection);
					break;
				}
				
				case WRITE: {
					writePool.returnObject(connection);
					break;
				}
				
				default: {
					throw new IllegalStateException("The given connection type was invalid. Must be either ConnectionType.READ or ConnectionType.WRITE");
				}
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new UnreachableAssertion("Statement cache factory does not throw any declared exceptions.", ex);
		}
	}
	
	/**
	 * Internal implementation of invalidating a connection.
	 */
	private final void internalInvalidateConnection(PooledConnection connection, ConnectionType connectionType) {
		try {
			switch(connectionType) {
				case READ: {
					readPool.invalidateObject(connection);
					break;
				}
				
				case WRITE: {
					writePool.invalidateObject(connection);
					break;
				}
				
				default: {
					throw new IllegalStateException("The given connection type was invalid. Must be either ConnectionType.READ or ConnectionType.WRITE");
				}
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new UnreachableAssertion("Statement cache factory does not throw any declared exceptions.", ex);
		}
	}
	
	@Override
	public void clear() {
		clearPool(this.readPool);
		clearPool(this.writePool);
	}

	private static void clearPool(ObjectPool pool) {
		try {
			pool.clear();
		} catch (UnsupportedOperationException ex) {
			Logger.info("Unsupported operation 'clear'.", AbstractConfiguredConnectionPool.class);
		} catch (Exception ex) {
			Logger.error("Failed to clear underlying object pool.", ex, AbstractConfiguredConnectionPool.class);
		}
	}

	@Override
	public void close() {
		super.close();

		try {
			this.readPool.close();
			this.writePool.close();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new UnreachableAssertion("Statement cache factory does not throw any declared exceptions.", ex);
		}
	}

	private static final class ObservedPooledConnectionImpl extends PooledConnectionImpl {
		private Exception borrowStackTrace;
		
		public ObservedPooledConnectionImpl(ConnectionPool pool, int transactionIsolationLevel, boolean autoCommit,
											boolean readOnly) {
			super(pool, transactionIsolationLevel, autoCommit, readOnly);
		}
		
		final void initBorrowStackTrace() {
			this.borrowStackTrace = new Exception("BorrowStackTrace");
		}
		
		final Exception getBorrowStackTrace() {
			return borrowStackTrace;
		}
		
		final void clearBorrowStackTrace() {
			this.borrowStackTrace = null;
		}

		@Override
		void cleanup() {
			clearBorrowStackTrace();
			
			super.cleanup();
		}
	}
	
	/**
	 * {@link PoolableObjectFactory} that creates {@link PooledConnection}s
	 * representing connections.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class PooledConnectionFactory implements PoolableObjectFactory {
		
		private boolean readOnly;
		private boolean autoCommit;

		private final ConnectionPool _pool;
		
		/**
		 * Creates a {@link PooledConnectionFactory}.
		 */
		public PooledConnectionFactory(ConnectionPool pool, ConnectionType connectionType) {
			_pool = pool;
			switch (connectionType) {
				case READ: {
					
					readOnly = true;
					autoCommit = true;
					break;
				}
				
				case WRITE: {
					
					readOnly = false;
					autoCommit = false;
					break;
				}
				
				default: {
					throw new IllegalStateException("The given connection type was invalid. Must be either ConnectionType.READ or ConnectionType.WRITE");
				}
			}
		}

		@Override
		public Object makeObject() {
			return new ObservedPooledConnectionImpl(_pool, PooledConnection.TRANSACTION_READ_COMMITTED, autoCommit,
												    readOnly);
		}

		@Override
		public void activateObject(Object obj) {
			ObservedPooledConnectionImpl currentConnection = (ObservedPooledConnectionImpl) obj;
			currentConnection.activate();
		}

		@Override
		public void passivateObject(Object obj) {
			ObservedPooledConnectionImpl currentConnection = (ObservedPooledConnectionImpl) obj;
			currentConnection.cleanup();
		}

		@Override
		public void destroyObject(Object obj) {
			ObservedPooledConnectionImpl connection = (ObservedPooledConnectionImpl) obj;
			connection.closeConnection(null);
		}

		@Override
		public boolean validateObject(Object obj) {
			try {
				return _pool.getSQLDialect().ping((Connection) obj);
			} catch (SQLException e) {
				Logger.warn("Database access failed.", e, PooledConnectionFactory.class);
				return false;
			}
		}
	}

	/**
	 * Typed configuration variant of {@link org.apache.commons.pool.impl.GenericObjectPool.Config}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface PoolSettings extends ConfigurationItem {

		/**
		 * @see GenericObjectPool#setLifo(boolean)
		 */
		@Name(LIFO_PROPERTY)
		Boolean getLifo();

		/**
		 * @see #getLifo()
		 */
		void setLifo(Boolean value);

		/**
		 * @see GenericObjectPool#setMaxActive(int)
		 */
		@Name(MAX_ACTIVE_PROPERTY)
		Integer getMaxActive();

		/**
		 * @see #setMaxActive(Integer)
		 */
		void setMaxActive(Integer value);

		/**
		 * @see GenericObjectPool#setMaxWait(long)
		 */
		@Name(MAX_WAIT_PROPERTY)
		Long getMaxWait();

		/**
		 * @see #getMaxWait()
		 */
		void setMaxWait(Long value);

		/**
		 * @see GenericObjectPool#setMinEvictableIdleTimeMillis(long)
		 */
		@Name(MIN_EVICTABLE_IDLE_TIME_MILLIS_PROPERTY)
		Long getMinEvictableIdleTimeMillis();

		/**
		 * @see #getMinEvictableIdleTimeMillis()
		 */
		void setMinEvictableIdleTimeMillis(Long value);

		/**
		 * @see GenericObjectPool#setMinIdle(int)
		 */
		@Name(MIN_IDLE_PROPERTY)
		Integer getMinIdle();

		/**
		 * @see #getMinIdle()
		 */
		void setMinIdle(Integer value);

		/**
		 * @see GenericObjectPool#setNumTestsPerEvictionRun(int)
		 */
		@Name(NUM_TESTS_PER_EVICTION_RUN_PROPERTY)
		Integer getNumTestsPerEvicionRun();

		/**
		 * @see #getNumTestsPerEvicionRun()
		 */
		void setNumTestsPerEvicionRun(Integer value);

		/**
		 * @see GenericObjectPool#setSoftMinEvictableIdleTimeMillis(long)
		 */
		@Name(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS_PROPERTY)
		Long getSoftMinEvictableIdleTimeMillis();

		/**
		 * @see #getSoftMinEvictableIdleTimeMillis()
		 */
		void setSoftMinEvictableIdleTimeMillis(Long value);

		/**
		 * @see GenericObjectPool#setTestOnBorrow(boolean)
		 */
		@Name(TEST_ON_BORROW_PROPERTY)
		Boolean getTestOnBorrow();

		/**
		 * @see #getTestOnBorrow()
		 */
		void setTestOnBorrow(Boolean value);

		/**
		 * @see GenericObjectPool#setTestOnReturn(boolean)
		 */
		@Name(TEST_ON_RETURN_PROPERTY)
		Boolean getTestOnReturn();

		/**
		 * @see #getTestOnReturn()
		 */
		void setTestOnReturn(Boolean value);

		/**
		 * @see GenericObjectPool#setTestWhileIdle(boolean)
		 */
		@Name(TEST_WHILE_IDLE_PROPERTY)
		Boolean getTestWhileIdle();

		/**
		 * @see #getTestWhileIdle()
		 */
		void setTestWhileIdle(Boolean value);

		/**
		 * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis(long)
		 */
		@Name(TIME_BETWEEN_EVICTION_RUNS_MILLIS_PROPERTY)
		Long getTimeBetweenEvictionRunsMillis();

		/**
		 * @see #getTimeBetweenEvictionRunsMillis()
		 */
		void setTimeBetweenEvictionRunsMillis(Long value);

		/**
		 * @see GenericObjectPool#setWhenExhaustedAction(byte)
		 */
		@Name(WHEN_EXHAUSTED_ACTION_PROPERTY)
		@NullDefault
		ExhaustedAction getWhenExhaustedAction();

		/**
		 * @see #getWhenExhaustedAction()
		 */
		void setWhenExhaustedAction(ExhaustedAction value);

	}

	/**
	 * Adapter that reads settings for {@link org.apache.commons.pool.impl.GenericObjectPool.Config}
	 * from a {@link PoolSettings} configuration.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	static class ConfigAdapter extends GenericObjectPool.Config {
		public ConfigAdapter setValues(PoolSettings config) {
			this.lifo = getBooleanValue(config.getLifo(), this.lifo);
			this.maxActive = getIntValue(config.getMaxActive(), this.maxActive);
			this.maxWait = getLongValue(config.getMaxWait(), this.maxWait);
			this.minEvictableIdleTimeMillis =
				getLongValue(config.getMinEvictableIdleTimeMillis(), this.minEvictableIdleTimeMillis);
			this.minIdle = getIntValue(config.getMinIdle(), this.minIdle);
			this.numTestsPerEvictionRun =
				getIntValue(config.getNumTestsPerEvicionRun(), this.numTestsPerEvictionRun);
			this.softMinEvictableIdleTimeMillis =
				getLongValue(config.getSoftMinEvictableIdleTimeMillis(), this.softMinEvictableIdleTimeMillis);
			this.testOnBorrow = getBooleanValue(config.getTestOnBorrow(), this.testOnBorrow);
			this.testOnReturn = getBooleanValue(config.getTestOnReturn(), this.testOnReturn);
			this.testWhileIdle = getBooleanValue(config.getTestWhileIdle(), this.testWhileIdle);
			this.timeBetweenEvictionRunsMillis =
				getLongValue(config.getTimeBetweenEvictionRunsMillis(), this.timeBetweenEvictionRunsMillis);
			final ExhaustedAction defaultEnum = ExhaustedAction.values()[this.whenExhaustedAction];
			this.whenExhaustedAction =
				(byte) getValue(config.getWhenExhaustedAction(), defaultEnum).ordinal();

			return this;
		}

		private <T> T getValue(T value, T defaultValue) {
			return value == null ? defaultValue : value;
		}

		private long getLongValue(Long value, long defaultValue) {
			return value == null ? defaultValue : value.longValue();
		}

		private boolean getBooleanValue(Boolean value, boolean defaultValue) {
			return value == null ? defaultValue : value.booleanValue();
		}

		private int getIntValue(Integer value, int defaultValue) {
			return value == null ? defaultValue : value.intValue();
		}

	}
	
	/**
	 * {@link UnboundListener} that {@link #internalReleaseReadConnection(PooledConnection)
	 * releases} all connections, a thread still holds at the time the thread goes out of scope.
	 * 
	 */
	private final UnboundListener connectionReleaser = new UnboundListener() {

		@Override
		public void threadUnbound(InteractionContext context) {
			LocalConnections localConnections = getLocalConnections(context);
			if (localConnections != null) {
				localConnections.releaseAll();
			}
		}
		
	};
	
	/**
	 * Bookkeeper for allocated connection resources per thread. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	static class LocalConnections {
		private ObservedPooledConnectionImpl readConnection = null;
		
		/**
		 * Number of nested read connection allocations.
		 */
		private int readConnectionCnt = 0;
		
		private final ArrayList<PooledConnection> writeConnections = new ArrayList<>();
		private final AbstractConfiguredConnectionPool owner;

		public LocalConnections(AbstractConfiguredConnectionPool owner) {
			this.owner = owner;
		}
		
		/**
		 * This is a repair method. It frees up all resources that were not released explicitly.
		 */
		public void releaseAll() {
			ObservedPooledConnectionImpl lostReadConnection = readConnection;
			if (lostReadConnection != null) {
				Logger.warn("Open invalid read connections detected. Framework does cleanup. If the borrow stack trace of the connection is not part of the exception stack trace, enable flag 'DEBUG_RESOURCES' and rerun.", new Exception("Stack trace").initCause(lostReadConnection.getBorrowStackTrace()), AbstractConfiguredConnectionPool.class);

				owner.internalReleaseReadConnection(lostReadConnection);
				
				readConnection = null;
				readConnectionCnt = 0;
			}
			
			int lostWriteConnectionCnt = writeConnections.size();
			if (lostWriteConnectionCnt > 0) {
				for (int n = 0; n < lostWriteConnectionCnt; n++) {
					ObservedPooledConnectionImpl lostWriteConnection = (ObservedPooledConnectionImpl) writeConnections.get(n);
					Logger.warn("Open invalid write connections detected. Framework does cleanup. If the borrow stack trace of the connection is not part of the exception strack trace, enable flag 'DEBUG_RESOURCES' and rerun.", new Exception("Stack trace").initCause(lostWriteConnection.getBorrowStackTrace()), AbstractConfiguredConnectionPool.class);
					
					owner.internalReleaseWriteConnection(lostWriteConnection);
				}
				
				writeConnections.clear();
			}			
		}

		public boolean notifyReleaseReadConnection(PooledConnection connection) {
			if (connection == null) {
				// must handle separate in case own read connection is null.
				throw new IllegalArgumentException("Null not released from this pool.");
			}
			if (this.readConnection != connection) {
				if (connection instanceof ObservedPooledConnectionImpl) {
					ObservedPooledConnectionImpl pooledConnection = (ObservedPooledConnectionImpl) connection;
					throw (IllegalArgumentException) new IllegalArgumentException("Must only release connections borrowed with borrowReadConnection(). If the borrow stack trace of the connection is not part of the exception strack trace, enable flag 'DEBUG_RESOURCES' and rerun.").initCause(pooledConnection.getBorrowStackTrace());
				} else {
					throw new IllegalArgumentException("Connection not borrowed from this pool.");
				}
			}
			
			int openRequests = --this.readConnectionCnt;
			if (openRequests == 0) {
				this.readConnection = null;
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * This method just is a getter for the internal state of the read connection. For acquiring a local read connection
		 * while borrow-process use getLocalReadConnection(), to detect number of access attempts.
		 * 
		 * @return internal cached read connection
		 */
		ObservedPooledConnectionImpl internalGetLocalReadConnection() {
			return readConnection;
		}

		public ObservedPooledConnectionImpl getLocalReadConnection() {
			ObservedPooledConnectionImpl alreadBorrowedConnection = readConnection;
			if (alreadBorrowedConnection != null) {
				if (owner.debugResources() && owner.warnNestedRead()) {
					Logger.warn("Borrowed a read connection twice.", 
							new Exception("Stack trace").initCause(alreadBorrowedConnection.getBorrowStackTrace()), 
							AbstractConfiguredConnectionPool.class);
				}
				readConnectionCnt++;
			}
			return alreadBorrowedConnection;
		}
		
		public void initReadConnection(ObservedPooledConnectionImpl connection) {
			this.readConnection = connection;
			this.readConnectionCnt = 1;
		}

		public boolean notifyReleaseWriteConnection(PooledConnection connection) {
			boolean wasBorrowed = this.writeConnections.remove(connection);
			if (! wasBorrowed) {
				if (connection instanceof ObservedPooledConnectionImpl) {
					ObservedPooledConnectionImpl pooledConnection = (ObservedPooledConnectionImpl) connection;
					throw (IllegalArgumentException) new IllegalArgumentException("Must only release connections borrowed with borrowWriteConnection()").initCause(pooledConnection.getBorrowStackTrace());
				} else {
					throw new IllegalArgumentException("Connection not borrowed from this pool.");
				}
			}
			
			return wasBorrowed;
		}

		public void notifyBorrowWriteConnection(PooledConnection connection) {
			this.writeConnections.add(connection);
		}
	}
	
	/**
	 * This enumeration provides constants for connection types.
	 * 
	 * @author    <a href=mailto:sts@top-logic.com>sts</a>
	 */
	public enum ConnectionType {
		/**
		 * A read-only connection.
		 */
		READ,

		/**
		 * A connection that can update the database.
		 */
		WRITE
	}
	
	/**
	 * This method checks the auto commit state of the given connection 
	 * 
	 * @param connection - the database connection to check
	 * @param exceptionReturnValue - the value to return in case of exception
	 * @return true, if the connection is set to auto commit, false otherwise
	 */
	private boolean checkConnectionAutoCommitState(ObservedPooledConnectionImpl connection,
			 									   boolean exceptionReturnValue) {
		try {
			return connection.getAutoCommit();
		}
		catch(SQLException ex){
			Logger.warn("Could not check connection properties. Connection is not valid.", ex, AbstractConfiguredConnectionPool.class);
			return exceptionReturnValue;
		}
		
	}
	
	/**
	 * This method checks the read only state of the given connection 
	 * 
	 * @param connection - the database connection to check
	 * @param exceptionReturnValue - the value to return in case of exception
	 * @return true, if the connection is set to read only, false otherwise
	 */
	private boolean checkConnectionReadOnlyState(ObservedPooledConnectionImpl connection,
												 boolean exceptionReturnValue) {
		try {
			return connection.isReadOnly();
		}
		catch(SQLException ex){
			Logger.warn("Could not check connection properties. Connection is not valid.", ex, AbstractConfiguredConnectionPool.class);
			return exceptionReturnValue;
		}
	}
}

