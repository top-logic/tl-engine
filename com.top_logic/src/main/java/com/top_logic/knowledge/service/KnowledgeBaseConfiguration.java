/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.concurrent.TimeUnit;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;

/**
 * Configuration interface of a {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KnowledgeBaseConfiguration extends NamedConfiguration {

	/** Name of the {@link #isSingleNodeOptimization()} property */
	String SINGLE_NODE_OPTIMIZATION_PROPERTY = "single-node-optimization";

	/**
	 * The name of the {@link KnowledgeBase}
	 */
	@Override
	String getName();

	/**
	 * {@link Boolean} configuration property that enables single node optimizations.
	 * 
	 * <p>
	 * This optimization must only be enabled, if the application is not deployed in a cluster.
	 * </p>
	 */
	@Mandatory
	@Name(SINGLE_NODE_OPTIMIZATION_PROPERTY)
	boolean isSingleNodeOptimization();

	/**
	 * @see #isSingleNodeOptimization()
	 */
	void setSingleNodeOptimization(boolean value);

	/**
	 * Property for configuring {@link #getRefetchInterval()}.
	 */
	String REFETCH_INTERVAL_PROPERTY = "refetch-interval";

	/**
	 * The interval between two refetch operations, in milliseconds.
	 * 
	 * <p>
	 * A value <= 0 means no automatic refetch.
	 * </p>
	 */
	@Name(REFETCH_INTERVAL_PROPERTY)
	@LongDefault(30 * 1000)
	long getRefetchInterval();

	/**
	 * @see #getRefetchInterval()
	 */
	void setRefetchInterval(long value);

	/** Property name of {@link #getRefetchWaitOnStopTimeout()}. */
	String REFETCH_WAIT_ON_STOP_TIMEOUT = "refetch-wait-on-stop-timeout";

	/**
	 * Milliseconds how long the {@link KnowledgeBase} should wait on shutdown for a running refetch
	 * to complete.
	 * <p>
	 * If the time is up, the shutdown proceeds and the refetch might fail because the
	 * {@link KnowledgeBase} is already shut down. <br/>
	 * 0 and negative values mean: Don't wait. <br/>
	 * To wait "forever", use for example {@link Long#MAX_VALUE}.
	 * </p>
	 */
	@Name(REFETCH_WAIT_ON_STOP_TIMEOUT)
	@LongDefault(30000)
	long getRefetchWaitOnStopTimeout();

	/**
	 * @see #getRefetchWaitOnStopTimeout()
	 */
	void setRefetchWaitOnStopTimeout(long value);

	/**
	 * Property for configuring {@link #getRefetchTimeout()}.
	 */
	String REFETCH_TIMEOUT_PROPERTY = "refetch-timeout";

	/**
	 * Time in milliseconds that a {@link KnowledgeBase#refetch() refetch operation} waits for the
	 * refetch lock before aborting the operation.
	 */
	@LongDefault(2 * 60 * 1000)
	@Name(REFETCH_TIMEOUT_PROPERTY)
	long getRefetchTimeout();

	/**
	 * @see #getRefetchTimeout()
	 */
	void setRefetchTimeout(long value);

	/**
	 * Property for configuring {@link #getDisableVersioning}.
	 */
	String DISABLE_VERSIONING_PROPERTY = "disable-versioning";

	/**
	 * Whether versioning is globally disabled in the {@link KnowledgeBase}.
	 */
	@Name(DISABLE_VERSIONING_PROPERTY)
	boolean getDisableVersioning();

	/**
	 * @see #getDisableVersioning()
	 */
	void setDisableVersioning(boolean value);

	/**
	 * Chunk size for reading history events.
	 * 
	 * <p>
	 * During reading the history, the given number of revisions are cached in memory before they
	 * are returned by the readers.
	 * </p>
	 */
	@IntDefault(500)
	int getReaderChunkSize();

	/**
	 * @see #getReaderChunkSize()
	 */
	void setReaderChunkSize(int value);

	/**
	 * Property for configuring {@link #getConnectionPool()}.
	 */
	String CONNECTION_POOL_PROPERTY = "connection-pool";

	/**
	 * The name of the {@link ConnectionPool} of the {@link KnowledgeBase}.
	 */
	@StringDefault(ConnectionPoolRegistry.DEFAULT_POOL_NAME)
	@Name(CONNECTION_POOL_PROPERTY)
	String getConnectionPool();

	/**
	 * @see #getConnectionPool()
	 */
	void setConnectionPool(String value);

	/** Property for configuring {@link #isStopReplayOnError()}. */
	String STOP_REPLAY_ON_ERROR_PROPERTY = "stop-replay-on-error";

	/**
	 * Whether replay should fail when an error occurs.
	 */
	@BooleanDefault(true)
	@Name(STOP_REPLAY_ON_ERROR_PROPERTY)
	boolean isStopReplayOnError();

	/**
	 * @see #isStopReplayOnError()
	 */
	void setStopReplayOnError(boolean value);

	/** Name of the property declaring the type system. */
	String TYPE_SYSTEM_PROPERTY = "type-system";

	/**
	 * The name of the {@link TypeSystemConfiguration} used in the {@link KnowledgeBase}.
	 */
	@StringDefault(TypeSystemConfiguration.DEFAULT_NAME)
	@Name(TYPE_SYSTEM_PROPERTY)
	String getTypeSystem();

	/**
	 * @see #getTypeSystem()
	 */
	void setTypeSystem(String value);

	/**
	 * Refetch operations are logged, if they last longer than the specified number of milliseconds.
	 */
	@LongDefault(500)
	long getRefetchLogTime();

	/**
	 * @see #getRefetchLogTime()
	 */
	void setRefetchLogTime(long value);

	/**
	 * Warnings are logged, if commit operations last longer than the specified number of
	 * milliseconds.
	 */
	@LongDefault(5000)
	long getCommitWarnTime();

	/**
	 * @see #getCommitWarnTime()
	 */
	void setCommitWarnTime(long value);

	/** Name of the {@link #getCleanupUnversionedTypesInterval()} property. */
	String CLEANUP_UNVERSIONED_TYPES_INTERVAL_PROPERTY = "cleanup-unversioned-types-interval";

	/**
	 * The interval in {@link TimeUnit#SECONDS} in which the cleanup of old data of unversioned
	 * types must occur.
	 * 
	 * <p>
	 * A value <code>&lt;= 0</code> will disable cleaning.
	 * </p>
	 */
	@LongDefault(60 * 60)
	@Name(CLEANUP_UNVERSIONED_TYPES_INTERVAL_PROPERTY)
	long getCleanupUnversionedTypesInterval();

	/**
	 * @see #getCleanupUnversionedTypesInterval()
	 */
	void setCleanupUnversionedTypesInterval(long value);

	/**
	 * Whether the type configuration must be read from configuration files ignoring potential
	 * entries in the database.
	 */
	boolean isOverridePersistentTypeDefinitions();

	/**
	 * Setter for {@link #isOverridePersistentTypeDefinitions()}.
	 * 
	 * @param b
	 *        New value of {@link #isOverridePersistentTypeDefinitions()}.
	 */
	void setOverridePersistentTypeDefinitions(boolean b);
}
