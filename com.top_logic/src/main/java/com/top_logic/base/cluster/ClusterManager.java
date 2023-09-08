/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.CopyOnChangeListProvider;
import com.top_logic.basic.col.FirstCharacterMapping;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.NullSaveMapping;
import com.top_logic.basic.col.ParseBooleanMapping;
import com.top_logic.basic.col.ParseDoubleMapping;
import com.top_logic.basic.col.ParseFloatMapping;
import com.top_logic.basic.col.ParseIntMapping;
import com.top_logic.basic.col.ParseLongMapping;
import com.top_logic.basic.col.ParseStringMapping;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sched.SchedulerServiceHandle;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.thread.InContext;
import com.top_logic.knowledge.service.SimpleDBExecutor;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.db.DBUtil;
import com.top_logic.util.db.DBUtil.ResultAsLongList;

/**
 * Manages communications between nodes in a multi cluster system and provides cluster wide
 * global properties.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
@ServiceDependencies({ ConnectionPoolRegistry.Module.class, SchedulerService.Module.class })
public class ClusterManager extends ManagedClass {

    /** Mapping that maps everything to <code>null</code>. */
	private static final Mapping<String, String> NULL_STRING_MAPPING = new Mapping<>() {
		@Override
		public String map(String input) {
            return null;
        }
    };

    /**
     * The possible states of cluster nodes.
     */
    public static enum NodeState {
		WAIT_FOR_STARTUP, STARTUP, RUNNING,
		/** Application is Shutting Down */
		SHUTDOWN
		// After shutdown the node is removed and this way has no state any more
    }

    /**
     * Predefined types for cluster wide properties.
     */
    public static enum PropertyType {
        NULL, BOOLEAN, INT, LONG, FLOAT, DOUBLE, CHAR, STRING
    }

	/** The configuration of the {@link ClusterManager}. */
	public interface Config extends ServiceConfiguration<ClusterManager> {

		/** In milliseconds. */
		@LongDefault(1000 * 60)
		long getTimeoutRunningNode();

		/** In milliseconds. */
		@LongDefault(1000 * 60 * 15)
		long getTimeoutOtherNode();

		/** The refetch interval in milliseconds. Has to be >0. */
		@LongDefault(16 * 1000)
		long getRefetchInterval();

		/** Property name of {@link #getRefetchWaitOnStopTimeout()}. */
		String REFETCH_WAIT_ON_STOP_TIMEOUT = "refetch-wait-on-stop-timeout";

		/**
		 * Milliseconds how long the {@link ClusterManager} should wait on shutdown for a running
		 * refetch to complete.
		 * <p>
		 * If the time is up, the shutdown proceeds and the refetch might fail because the
		 * {@link ClusterManager} is already shut down. <br/>
		 * 0 and negative values mean: Don't wait. <br/>
		 * To wait "forever", use for example {@link Long#MAX_VALUE}.
		 * </p>
		 */
		@Name(REFETCH_WAIT_ON_STOP_TIMEOUT)
		@LongDefault(30000)
		long getRefetchWaitOnStopTimeout();

		/** Name of {@link #isCluster()} property. */
		String CLUSTER_PROPERTY = "cluster";

		/** Defines whether the application runs in cluster mode. */
		@Mandatory
		@Name(CLUSTER_PROPERTY)
		boolean isCluster();

		/** Name of the {@link #getConnectionPool()} property. */
		String CONNECTION_POOL = "connection-pool";

		/**
		 * The name of the {@link ConnectionPool} registered in the {@link ConnectionPoolRegistry}
		 * used by this {@link ClusterManager}.
		 */
		@Name(CONNECTION_POOL)
		@StringDefault(ConnectionPoolRegistry.DEFAULT_POOL_NAME)
		String getConnectionPool();
	}

    protected static final int TUPLE_INDEX_SERIALIZER = 0;
    protected static final int TUPLE_INDEX_PARSER = 1;
    protected static final int TUPLE_INDEX_SERIALIZED_VALUE = 0;
    protected static final int TUPLE_INDEX_PARSED_VALUE = 1;

    protected static final int TUPLE_INDEX_PROPERTY_CHANGED = 0;
    protected static final int TUPLE_INDEX_PROPERTY_CHANGE_CONFIRMED = 1;


    /** The time in milliseconds after that not responding running nodes will be removed from cluster. */
	protected final long timeoutRunningNode;

    /** The time in milliseconds after that not responding other nodes will be removed from cluster. */
	protected final long timeoutOtherNode;


    /** The DB executor for DB queries. */
	protected final ClusterManagerDBExecutor db;

    /** Flag indicating whether the application runs in cluster mode. */
	protected final boolean clusterMode;

    /** The ID of this cluster node. */
    protected Long nodeID;

    /** The state of this cluster node. */
	protected NodeState nodeState;


    /** Holds declared values and their types (Tuple of type serializer and type parser). */
	protected final Map<String, Tuple> declaredValues;

    /**
	 * Holds cluster wide global values (property name, property value).
	 *
	 * Note: The values are parsed values, but may be raw values for properties which are undeclared
	 * in this cluster node yet.
	 */
	protected final Map<String, Object> globalValues;

    /**
	 * Holds properties with pending changes until they are confirmed (messageID by property name).
	 * The messageID is <code>null</code> except for changes caused by this node to distinguish
	 * changes from this and other nodes.
	 */
	protected final Map<String, Long> pendingChanges;


    /** Holds the registered listeners. */
	protected final CopyOnChangeListProvider<ClusterManagerListener> messageListeners;

	private volatile SchedulerServiceHandle _periodicalRefetch;

	private final long _refetchInterval;

	/** @see Config#getRefetchWaitOnStopTimeout() */
	private final long _refetchWaitOnStopTimeout;

	/** @see Config#getConnectionPool() */
	private final ConnectionPool _connectionPool;

    /**
     * Creates a new instance of this class.
     *
     * Note: Use the {@link #getInstance()} method instead of this constructor!
     *
     * @throws ClusterManagerException
     *         if database access fails
     */
	public ClusterManager(InstantiationContext context, Config config) throws ClusterManagerException {
		this(context, config, true);
    }

	/** 
	 * Creates a new {@link ClusterManager} from the given config.
	 * 
	 * @param initNode Whether the node should be initialized. 
	 */
	protected ClusterManager(InstantiationContext context, Config config, boolean initNode)
			throws ClusterManagerException {
		super(context, config);
		_connectionPool = getConnectionPool(context, config);
		timeoutRunningNode = config.getTimeoutRunningNode();
		timeoutOtherNode = config.getTimeoutOtherNode();
		_refetchInterval = config.getRefetchInterval();
		_refetchWaitOnStopTimeout = config.getRefetchWaitOnStopTimeout();
		
		try {
		    db = new ClusterManagerDBExecutor();
		}
		catch (SQLException e) {
		    throw new ClusterManagerException(e);
		}
		clusterMode = config.isCluster();
		declaredValues = new HashMap<>();
		globalValues = new HashMap<>();
		pendingChanges = new HashMap<>();
		messageListeners = new CopyOnChangeListProvider<>();

		if (initNode) {
			initNode();
		}
	}

	private ConnectionPool getConnectionPool(InstantiationContext context, Config config) {
		try {
			return ConnectionPoolRegistry.getConnectionPool(config.getConnectionPool());
		} catch (IllegalArgumentException ex) {
			context.error("No connection pool with name '" + config.getConnectionPool() + "' configured.", ex);
			return ConnectionPoolRegistry.getDefaultConnectionPool();
		}
	}

	/**
	 * Returns the {@link ConnectionPool} used by this {@link ClusterManager}.
	 */
	public ConnectionPool getConnectionPool() {
		return _connectionPool;
	}

	/**
	 * Whether cluster mode is configured.
	 */
	public static boolean isClusterModeConfigured() {
		ServiceConfiguration<ClusterManager> serviceConfig;
		try {
			serviceConfig = ApplicationConfig.getInstance().getServiceConfiguration(ClusterManager.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(
				"Unable to create service configuration for " + ClusterManager.class.getName(), ex);
		}
		return ((Config) serviceConfig).isCluster();
	}

    /**
     * Gets the singleton instance of this class.
     */
    public static ClusterManager getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }



    /**
     * Registers the given listener to get informed about value changes.
     */
    public synchronized void addClusterMessageListener(ClusterManagerListener listener) {
        messageListeners.add(listener);
    }

    /**
     * Removes the given listener from the listener list.
     */
    public synchronized void removeClusterMessageListener(ClusterManagerListener listener) {
        messageListeners.remove(listener);
    }

    /**
     * Informs the registered listeners about a detected value change.
     */
    protected void firePropertyChanged(String propertyName, Object oldValue, Object newValue, boolean isRawValue, boolean thisNode) {
        if (isRawValue) {
            Tuple tuple = declaredValues.get(propertyName);
            Mapping<String, ?> parser = tuple == null ? null : (Mapping<String, ?>)tuple.get(TUPLE_INDEX_PARSER);
            if (parser == null) return;
            oldValue = parser.map((String)oldValue);
            newValue = parser.map((String)newValue);
        }
		for (ClusterManagerListener listener : messageListeners.get()) {
            try {
                listener.clusterPropertyChanged(propertyName, oldValue, newValue, thisNode);
            }
            catch (Exception e) {
                Logger.error("ClusterMessageListener caused an exception in propertyChanged.", e, ClusterManager.class);
            }
        }
    }

    /**
     * Informs the registered listeners about a detected value change confirmation.
     */
    protected void firePropertyChangeConfirmed(String propertyName, Object currentValue, boolean isRawValue) {
        if (isRawValue) {
            Tuple tuple = declaredValues.get(propertyName);
            Mapping<String, ?> parser = tuple == null ? null : (Mapping<String, ?>)tuple.get(TUPLE_INDEX_PARSER);
            if (parser == null) return;
            currentValue = parser.map((String)currentValue);
        }
		for (ClusterManagerListener listener : messageListeners.get()) {
            try {
                listener.clusterPropertyChangeConfirmed(propertyName, currentValue);
            }
            catch (Exception e) {
                Logger.error("ClusterMessageListener caused an exception in propertyChangeConfirmed.", e, ClusterManager.class);
            }
        }
    }


    /**
     * Gets whether the application is in cluster mode and the cluster mode is active
     * (cluster manager has already been initialized). Cluster mode means that value changes
     * are propagated to other nodes in cluster.
     */
    public synchronized boolean isClusterModeActive() {
        return clusterMode && nodeID != null;
    }

    /**
     * Gets whether the application is in cluster mode or not. Cluster mode means that value
     * changes are propagated to other nodes in cluster.
     */
	public boolean isClusterMode() {
        return clusterMode;
    }

    /**
     * Gets the state of current cluster node. May be <code>null</code> if this node has not
     * been initialized yet.
     */
    public synchronized NodeState getNodeState() {
        return nodeState;
    }

    /**
     * Gets the current timestamp of the database. This method is required to synchronize
     * the server date of each node in cluster.
     *
     * @return the current date of the database server
     */
	public Date getTimeStamp() {
        if (isClusterMode()) try {
            return new Date(DBUtil.currentTimeMillis());
        }
        catch (SQLException e) {
            Logger.warn("Failed to get current timestamp from database.", e, ClusterManager.class);
        }
        return new Date();
    }

	/**
	 * A List containing the Id's of the currently active running or starting nodes.
	 */
	public synchronized List<Long> getActiveNodes() throws SQLException {
		if (isClusterMode()) {
			return db.internalGetRunningOrStartingNodes();
		}
		return Collections.emptyList();
	}

    /**
     * Sets the state of this cluster node to the given state.
     *
     * @param state
     *        the state to set
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized void setNodeState(NodeState state) throws ClusterManagerException {
        try {
            if (isClusterModeActive()) db.setNodeState(nodeID, state.name());
            nodeState = state;
            refetch();
        }
        catch (SQLException e) {
            Logger.error("Failed to change node state to " + state + ".", e, ClusterManager.class);
            throw new ClusterManagerException(e);
        }
    }

    /**
     * Checks whether all nodes in this cluster are in RUNNING state.
     *
     * @return <code>true</code>, if there is no (living) node in another state than
     *         RUNNING, <code>false</code> otherwise
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized boolean allNodesRunning() throws ClusterManagerException {
        if (!isClusterMode()) return nodeState == NodeState.RUNNING;
        try {
            return db.allNodesRunning();
        }
        catch (SQLException e) {
            Logger.error("Failed to check other nodes in cluster.", e, ClusterManager.class);
            throw new ClusterManagerException(e);
        }
    }



    /**
     * Initializes the ClusterManager and sets its state to
     * {@link NodeState#WAIT_FOR_STARTUP}.
     *
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized void initNode() throws ClusterManagerException {
        if (nodeID != null) {
            throw new IllegalArgumentException("This cluster node was already initialized.");
        }
        try {
            nodeID = isClusterMode() ? db.initNode(NodeState.WAIT_FOR_STARTUP.name()) : Long.valueOf(Long.MAX_VALUE);
            nodeState = NodeState.WAIT_FOR_STARTUP;
            refetch();
        }
        catch (SQLException e) {
            Logger.error("Failed to initialze cluster manager.", e, ClusterManager.class);
            throw new ClusterManagerException(e);
        }
    }

    /**
     * Shuts down the cluster manager and removes this node from the cluster.
     *
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized void removeNode() throws ClusterManagerException {
		if (isClusterModeActive()) {
			TLContext.inSystemContext(ClusterManager.class, new InContext() {
				@Override
				public void inContext() {
					try {
						db.removeNode();
					} catch (SQLException e) {
						Logger.error("Failed to shut down cluster manager.", e, ClusterManager.class);
						throw new ClusterManagerException(e);
					}
				}
			});
		}
		nodeID = null;
		nodeState = null;
    }



    /**
     * Refetches information from other nodes in the cluster and informs listeners about
     * changes. This will be called periodically by a task, but can be called manually to
     * ensure values are up-to-date.
     *
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized void refetch() throws ClusterManagerException {
        if (!isClusterModeActive()) return;
        try {
            db.refetch();
        }
        catch (SQLException e) {
            Logger.error("Failed to refetch cluster manager.", e, ClusterManager.class);
            throw new ClusterManagerException(e);
        }
    }



    /**
	 * Updates the life sign of this node in cluster. This is required in startup or wait for
	 * startup state, while {@link ClusterManagerRefetch} doesn't run.
	 * 
	 * @throws ClusterManagerException
	 *         if database access fails
	 */
    public synchronized void updateLifesign() throws ClusterManagerException {
        if (!isClusterModeActive()) return;
        try {
            db.updateLifesign();
        }
        catch (SQLException e) {
            Logger.error("Failed to update lifesign of cluster manager.", e, ClusterManager.class);
            throw new ClusterManagerException(e);
        }
    }

    /**
     * Declares a cluster wide property with a predefined simple type.
     *
     * @param propertyName
     *        the property name
     * @param propertyType
     *        the type of the property
     */
    public void declareValue(String propertyName, PropertyType propertyType) {
        switch (propertyType) {
        case NULL:
				declareValue(propertyName, NULL_STRING_MAPPING, NULL_STRING_MAPPING);
            return;
        case BOOLEAN:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(ParseBooleanMapping.INSTANCE));
            return;
        case INT:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(ParseIntMapping.INSTANCE));
            return;
        case LONG:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(ParseLongMapping.INSTANCE));
            return;
        case FLOAT:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(ParseFloatMapping.INSTANCE));
            return;
        case DOUBLE:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(ParseDoubleMapping.INSTANCE));
            return;
        case CHAR:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				new NullSaveMapping<>(FirstCharacterMapping.INSTANCE));
            return;
        case STRING:
			declareValue(propertyName, new NullSaveMapping<>(ParseStringMapping.INSTANCE),
				Mappings.<String> identity());
            return;
        }
        throw new IllegalArgumentException("Invalid property type: " + propertyType);
    }

    /**
     * Declares a cluster wide property with a wrapper type.
     *
     * @param propertyName
     *        the property name
     * @param koType
     *        the wrapper KO type of the property
     */
	public void declareWrapperValue(String propertyName, final String koType) {
		Mapping<Wrapper, String> serializer = new Mapping<>() {
			@Override
			public String map(Wrapper input) {
				if (input == null) {
					return null;
				}
				assert koType.equals(input.tTable().getName()) : "Unexpected type of object " + input
					+ ", expected was '" + koType + "'.";
				return IdentifierUtil.toExternalForm(input.tHandle().getObjectName());
			}
		};
		Mapping<String, Wrapper> parser = new Mapping<>() {
			@Override
			public Wrapper map(String input) {
				if (StringServices.isEmpty(input)) {
					return null;
				}
				return WrapperFactory.getWrapper(IdentifierUtil.fromExternalForm(input), koType);
			}
		};
		declareValue(propertyName, serializer, parser);
    }

    /**
     * Declares a cluster wide property with a complex type using the given serializer and
     * parser Mapping. Note: Serializer and parser must be bijective, so that the following is true:
     * <code>parser.map(serializer.map(object)).equals(object)</code>
     *
     * @param propertyName
     *        the property name
     * @param serializer
     *        the mapping to convert an object to a raw string value
     * @param parser
     *        the mapping to convert the raw string value to an object
     * @throws IllegalArgumentException
     *         if the given property is already declared or is empty
     */
    public synchronized <T> void declareValue(String propertyName, Mapping<? super T, String> serializer, Mapping<String, ? extends T> parser) {
        if (StringServices.isEmpty(propertyName)) {
            throw new IllegalArgumentException("Property must not be empty or null.");
        }
        if (isDeclared(propertyName)) {
            throw new IllegalArgumentException("Property '" + propertyName + "' is already declared.");
        }
        declaredValues.put(propertyName, TupleFactory.newTuple(serializer, parser));

        // parse values already set by other nodes
        if (globalValues.containsKey(propertyName)) {
            globalValues.put(propertyName, parser.map((String)globalValues.get(propertyName)));
        }
    }

    /**
     * Undeclares a cluster wide property. An already set value for this property will remain in
     * cluster, so that other nodes can still use the value. Redeclaring the property will also keep
     * the last set value.
     *
     * @param propertyName
     *        the name of the property to undeclare
     */
    public synchronized void undeclareValue(String propertyName) {
        if (!isDeclared(propertyName)) {
            throw new IllegalArgumentException("Property '" + propertyName + "' is not declared.");
        }
        // serialize values already set
        if (globalValues.containsKey(propertyName)) {
            Tuple tuple = declaredValues.get(propertyName);
			Mapping<Object, Object> serializer = (Mapping<Object, Object>) tuple.get(TUPLE_INDEX_SERIALIZER);
			globalValues.put(propertyName, serializer.map(globalValues.get(propertyName)));
        }
        declaredValues.remove(propertyName);
    }

    /**
     * Checks whether the given property is already declared.
     *
     * @param propertyName
     *        the name of the property to check
     * @return <code>true</code>, if the given property is already declared.
     */
    public synchronized boolean isDeclared(String propertyName) {
        return declaredValues.containsKey(propertyName);
    }



    /**
     * Gets the current value of the given cluster wide property. This method just looks in
     * the cluster managers cache, which will not trigger a database query. So this method
     * may return values which are not confirmed by all nodes in the clusters yet or which
     * are out of date because of pending changes from other nodes in cluster. Use
     * {@link #getConfirmedValue(String)} instead to get the up-to-date value of the
     * property.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the cluster wide property to get the value for
     * @return the value of the given property
     * @throws IllegalArgumentException
     *         if the given property is not declared yet
     */
    public synchronized <T> T getValue(String property) {
        if (!declaredValues.containsKey(property)) {
            throw new IllegalArgumentException("Property '" + property + "' is not declared yet.");
        }
        return (T)globalValues.get(property);
    }


    /**
     * Gets the current value of the given cluster wide property. In opposition to the
     * {@link #getValue(String)} method, this method will trigger a database refetch to get
     * the up-to-date value of the given property.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the cluster wide property to get the value for
     * @return the confirmed value of the given property
     * @throws PendingChangeException
     *         if there is a pending value change which is not confirmed by all nodes in the
     *         cluster yet; The exception contains further information about the old and the
     *         new value.
     * @throws IllegalArgumentException
     *         if the given property is not declared yet
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized <T> T getConfirmedValue(String property) throws PendingChangeException, ClusterManagerException {
        if (!isClusterModeActive()) return this.<T>getValue(property);
        Tuple tuple = declaredValues.get(property);
        if (tuple == null) {
            throw new IllegalArgumentException("Property '" + property + "' is not declared yet.");
        }
        Mapping<String, T> parser = (Mapping<String, T>)tuple.get(TUPLE_INDEX_PARSER);
        try {
            return parser.map(db.getValue(property));
        }
        catch (PendingChangeException e) {
            throw new PendingChangeException(e.getProperty(), parser.map((String)e.getOldValue()), parser.map((String)e.getNewValue()));
        }
        catch (SQLException e) {
            throw new ClusterManagerException(e);
        }
    }

    /**
     * Gets the current value of the given cluster wide property. In opposition to the
     * {@link #getValue(String)} method, this method will trigger a database refetch to get
     * the up-to-date value of the given property. If there is a pending change, the method
     * will block until the property is confirmed.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the cluster wide property to get the value for
     * @return the last set value of the given property
     * @throws IllegalArgumentException
     *         if the given property is not declared yet
     * @throws ClusterManagerException
     *         if database access fails
     */
    public <T> T getConfirmedValueWaiting(String property) throws ClusterManagerException {
        try {
            return this.<T>getConfirmedValue(property);
        }
        catch (PendingChangeException e) {
            waitForConfirmation(property);
            return this.<T>getValue(property);
        }
    }

    /**
     * Gets the current value of the given cluster wide property. In opposition to the
     * {@link #getValue(String)} method, this method will trigger a database refetch to get
     * the up-to-date value of the given property. If there is a pending change, the new
     * value is returned instead of a PendingChangeException.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the cluster wide property to get the value for
     * @return the last set value of the given property
     * @throws IllegalArgumentException
     *         if the given property is not declared yet
     * @throws ClusterManagerException
     *         if database access fails
     */
    public <T> T getLatestUnconfirmedValue(String property) throws ClusterManagerException {
        try {
            return this.<T>getConfirmedValue(property);
        }
        catch (PendingChangeException e) {
            return (T)e.getNewValue();
        }
    }

    /**
     * Gets the current value of the given cluster wide property. In opposition to the
     * {@link #getValue(String)} method, this method will trigger a database refetch to get
     * the up-to-date value of the given property. If there is a pending change, the old
     * value is returned instead of a PendingChangeException.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the cluster wide property to get the value for
     * @return the last set value of the given property
     * @throws IllegalArgumentException
     *         if the given property is not declared yet
     * @throws ClusterManagerException
     *         if database access fails
     */
    public <T> T getLatestConfirmedValue(String property) throws ClusterManagerException {
        try {
            return this.<T>getConfirmedValue(property);
        }
        catch (PendingChangeException e) {
            return (T)e.getOldValue();
        }
    }



    /**
     * Sets the value of the given cluster wide property and informs other nodes in the
     * cluster about the change. Pending changes will be overwritten.
     *
     * The property must have been declared before it can be set.
     *
     * @param <T>
     *        the value type
     * @param property
     *        the property to set
     * @param value
     *        the new value of the property; may be <code>null</code>
     * @throws IllegalArgumentException
     *         if the given property was not declared or the cluster manager not initialized yet
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized <T> void setValue(String property, T value) throws ClusterManagerException {
        setValue(property, value, true);
    }

    /**
     * Sets the value of the given cluster wide property and informs other nodes in the
     * cluster about the change. In opposition to the {@link #setValue(String, Object)}
     * method, in case of a pending change a PendingChangeException will be thrown and the
     * value will not get changed by this node
     *
     * The property must have been declared before it can be set.
     *
     * @param <T>
     *        the value type
     * @param property
     *        the property to set
     * @param value
     *        the new value of the property; may be <code>null</code>
     * @throws IllegalArgumentException
     *         if the given property was not declared or the cluster manager not initialized yet
     * @throws PendingChangeException
     *         if there is a pending value change which is not confirmed by all nodes in the
     *         cluster yet; The exception contains further information about the old and the
     *         new value.
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized <T> void setValueIfUnchanged(String property, T value) throws PendingChangeException, ClusterManagerException {
        setValue(property, value, false);
    }

    /**
     * Sets the value of the given cluster wide property and informs other nodes in the
     * cluster about the change.
     *
     * The property must have been declared before it can be set.
     *
     * @param <T>
     *        the value type
     * @param property
     *        the property to set
     * @param value
     *        the new value of the property; may be <code>null</code>
     * @param overwrite
     *        indicates the behavior in case that there is a pending change for the given property:<br/>
     *        <code>true</code> indicates, that the pending change shall be overwritten with the new value;<br/>
     *        <code>false</code> indicates, that the value change shall be aborted
     * @throws IllegalArgumentException
     *         if the given property was not declared or the cluster manager not initialized
     *         yet
     * @throws PendingChangeException
     *         if overwrite = <code>false</code> and there is a pending value change which is not
     *         confirmed by all nodes in the cluster yet; The exception contains further information
     *         about the old and the new value.
     * @throws ClusterManagerException
     *         if database access fails
     */
    protected synchronized <T> void setValue(String property, T value, boolean overwrite) throws PendingChangeException, ClusterManagerException {
        if (nodeID == null) {
            throw new IllegalStateException("The cluster manager was not initialized yet.");
        }
        Tuple values = checkValue(property, value);
        value = (T)values.get(TUPLE_INDEX_PARSED_VALUE);
        try {
            if (isClusterModeActive()) {
                String rawValue = (String)values.get(TUPLE_INDEX_SERIALIZED_VALUE);
                db.setValue(property, rawValue, overwrite);
            }
            Object oldValue = globalValues.put(property, value);
            if (!isClusterModeActive()) {
                firePropertyChanged(property, oldValue, value, false, true);
                firePropertyChangeConfirmed(property, value, false);
            }
            refetch();
        }
        catch (PendingChangeException e) {
            Mapping<String, T> parser = (Mapping<String, T>)declaredValues.get(property).get(TUPLE_INDEX_PARSER);
            throw new PendingChangeException(e.getProperty(), parser.map((String)e.getOldValue()), parser.map((String)e.getNewValue()));
        }
        catch (SQLException e) {
            throw new ClusterManagerException(e);
        }
    }

    /**
     * Checks the given value for the given property whether it is valid and gets the
     * serialized and parsed value of the given value.
     *
     * @param <T>
     *        the type of the value
     * @param property
     *        the property to check for
     * @param value
     *        the value to check
     * @return a tuple (serialized value, parsed value) of the given value
     * @throws IllegalArgumentException
     *         if the given property was not declared or the given value is invalid for the
     *         given property type
     */
    protected <T> Tuple/*<String, T>*/ checkValue(String property, T value) {
        Tuple tuple = declaredValues.get(property);
        if (tuple == null) {
            throw new IllegalArgumentException("The property '" + property + "' was not declared yet.");
        }
        try {
            String rawValue = ((Mapping<T, String>)tuple.get(TUPLE_INDEX_SERIALIZER)).map(value);
            T parsedValue = ((Mapping<String, T>)tuple.get(TUPLE_INDEX_PARSER)).map(rawValue);
            if (!Utils.equals(value, parsedValue)) {
                throw new IllegalArgumentException("The given value is invalid for property '" + property +
                    "': Parsed value (" + parsedValue + ") is not equal to original value (" + value + ").");
            }
            return TupleFactory.newTuple(rawValue, parsedValue);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("The given value (" + value + ") is invalid for property '" + property + "'.", e);
        }
    }



    /**
     * Sets the value of the given cluster wide property and informs other clusters about
     * the change. Waits until all nodes have confirmed the change.
     *
     * See {@link #setValue(String, Object)} for details.
     */
    public <T> void setValueAndWait(String property, T value) throws ClusterManagerException {
        setValue(property, value);
        waitForConfirmation(property);
    }

    /**
     * Waits until the current value of the given property has been confirmed by all nodes
     * in the cluster.
     *
     * @param property
     *        the property to wait for confirmation
     * @throws ClusterManagerException
     *         if database access fails
     */
    public void waitForConfirmation(String property) throws ClusterManagerException {
        while (!isConfirmed(property)) try {
             Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            // ignore
        }
    }


    /**
     * Checks whether the given property is confirmed by all nodes in the cluster.
     *
     * Note: This method causes a database query.
     *
     * @param property
     *        the property to check; may be <code>null</code>, then this method checks
     *        whether all declared properties are confirmed
     * @return <code>true</code>, if currently each node in the cluster has confirmed the
     *         current value of the given property
     * @throws ClusterManagerException
     *         if database access fails
     */
    public synchronized boolean isConfirmed(String property) throws ClusterManagerException {
        if (property != null && !declaredValues.containsKey(property)) {
            throw new IllegalArgumentException("The property '" + property + "' was not declared yet.");
        }
        try {
            return !isClusterModeActive() || db.isConfirmed(property);
        }
        catch (SQLException e) {
            throw new ClusterManagerException(e);
        }
    }

	/** The ID of this cluster node. */
	public synchronized Long getNodeId() {
		return nodeID;
	}

	/**
	 * Returns the {@link NodeState} of the node with the given id or <b><code>null</code></b>, if
	 * there is no longer a cluster node with the given id.
	 */
	public synchronized NodeState getNodeState(long nodeId) {
		if (nodeId == this.nodeID.longValue()) {
			return nodeState;
		}
		return db.getNodeState(nodeId);
	}



    /**
     * This class encapsulates the DB queries of the ClusterManager.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public class ClusterManagerDBExecutor {

        // node table and columns
        public static final String TABLE_NODE = "CLUSTER_NODE";
        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_STATE = "STATE";
        public static final String COLUMN_LIFE_SIGN = "LIFE_SIGN";
        public static final String COLUMN_CONFIRMED = "CONFIRMED";

		public final DBType[] TYPES = new DBType[] {
			DBType.STRING,
			DBType.STRING,
			DBType.STRING,
			DBType.LONG
		};

        // value table and columns
        public static final String TABLE_VALUE = "CLUSTER_VALUE";
        public static final String COLUMN_NAME = "NAME";
        public static final String COLUMN_VALUE = "VALUE";
        public static final String COLUMN_OLD_VALUE = "OLD_VALUE";
        public static final String COLUMN_SEQ_NUMBER = "SEQ_NUMBER";

        public static final int INDEX_COLUMN_NAME = 0;
        public static final int INDEX_COLUMN_VALUE = 1;
        public static final int INDEX_COLUMN_OLD_VALUE = 2;
        public static final int INDEX_COLUMN_SEQ_NUMBER = 3;

        // sequences for sequence manager
        public static final String NODE_ID = "cm_node";
        public static final String MESSAGE_ID = "cm_message";
        public static final String REQUEST_ID = "cm_request";


        /** The DB executor to manage DB transactions. */
		protected final SimpleDBExecutor dbExec = new SimpleDBExecutor(getConnectionPool());

        /** The sequence manager to get unique IDs. */
        protected final RowLevelLockingSequenceManager sequenceManager = new RowLevelLockingSequenceManager();

        /** The SQL dialect. */
        protected final DBHelper dbHelper;


        /** The last message ID this node has already confirmed. */
        private long lastSeenMessage = -1;



        /**
         * Creates SQL dialect dependent statements.
         */
        public ClusterManagerDBExecutor() throws SQLException {
			dbHelper = getConnectionPool().getSQLDialect();

			EXISTS_ANY_CLUSTER_NODE_STATEMENT = createExistsAnyClusterNodeStatement();
			EXISTS_NOT_RUNNING_NODE_STATEMENT = createExistsNotRunningNodeStatement();
			NOT_AFFIRMED_MESSAGE_STATEMENT = createNotAffirmedMessageStatement();
			NOT_AFFIRMED_PROPERTY_STATEMENT = createNotAffirmedPropertyStatement();
			CHECK_NODE_TABLE_STATEMENT = createCheckNodeTableStatement();
			CHECK_VALUE_TABLE_STATEMENT = createCheckValueTableStatement();
			CHECK_NODE_COLUMNS_STATEMENT = createCheckNodeColumnsStatement();
			CHECK_VALUE_COLUMNS_STATEMENT = createCheckValueColumnsStatement();
			SELECT_NODES_WITH_STATE_STATEMENT = createSelectNodesWithSateStatement();
			LIFE_SIGN_STATEMENT = createLifeSignStatement();
			REMOVE_DEAD_STATEMENT = createRemoveDeadStatement();
			REMOVE_LONG_DEAD_STATEMENT = createRemoveLongDeadStatement();
			REVIVE_STATEMENT = createReceiveStatement();
			CLEANUP_STATEMENT = createCleanupStatement();
			INIT_NODE_STATEMENT = createInitNodeStatement();
			REMOVE_NODE_STATEMENT = createRemoveNodeStatement();
			SET_STATE_STATEMENT = createSetStateStatement();
			READ_PROPERTY_STATEMENT = createReadPropertyStatement();
			INSERT_PROPERY_STATEMENT = createInsertPropertyStatement();
			UPDATE_PROPERY_STATEMENT = createUpdatePropertyStatement();
			READ_CHANGES_STATEMENT = createReadChangesStatement();
			CONFIRM_STATEMENT = createConfirmStatement();
			GET_NODE_STATE_FOR_ID = createGetNodeStateForIdStatement();
		}

		private String createCheckNodeTableStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE 1=0");
			return sb.toString();
		}

		private String createCheckValueTableStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" WHERE 1=0");
			return sb.toString();
		}

		private String createCheckNodeColumnsStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_LIFE_SIGN));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE 1=0");
			return sb.toString();
		}

		private String createCheckValueColumnsStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(dbHelper.columnRef(COLUMN_NAME));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_VALUE));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_OLD_VALUE));
			sb.append(",");
			sb.append(dbHelper.columnRef(COLUMN_SEQ_NUMBER));
			sb.append(" FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" WHERE 1=0");
			return sb.toString();
		}

		private String createSelectNodesWithSateStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ? OR ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createLifeSignStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" SET ");
			sb.append(dbHelper.columnRef(COLUMN_LIFE_SIGN));
			sb.append(" = ? WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createRemoveDeadStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ? AND ");
			sb.append(dbHelper.columnRef(COLUMN_LIFE_SIGN));
			sb.append(" < ?");
			return sb.toString();
		}

		private String createRemoveLongDeadStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_LIFE_SIGN));
			sb.append(" < ?");
			return sb.toString();
		}

		private String createReceiveStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" VALUES (?, ?, ?, ?)");
			return sb.toString();
		}

		private String createCleanupStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			return sb.toString();
		}

		private String createInitNodeStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" VALUES (?, ?, ?, NULL)");
			return sb.toString();
		}

		private String createRemoveNodeStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createSetStateStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" SET ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ? WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createReadPropertyStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_NAME));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createInsertPropertyStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" VALUES (?, ?, NULL, ?)");
			return sb.toString();
		}

		private String createUpdatePropertyStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" SET ");
			sb.append(dbHelper.columnRef(COLUMN_VALUE));
			sb.append(" = ?, ");
			sb.append(dbHelper.columnRef(COLUMN_OLD_VALUE));
			sb.append(" = ?, ");
			sb.append(dbHelper.columnRef(COLUMN_SEQ_NUMBER));
			sb.append(" = ? WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_NAME));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createReadChangesStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_SEQ_NUMBER));
			sb.append(" > ?");
			return sb.toString();
		}

		private String createConfirmStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" SET ");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" = ? WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createGetNodeStateForIdStatement() {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_ID));
			sb.append(" = ?");
			return sb.toString();
		}

		private String createNotAffirmedPropertyStatement() {
			StringBuilder sb = new StringBuilder();
			dbHelper.limitStart(sb, 1);
            sb.append("SELECT ");
            dbHelper.limitColumns(sb, 1);
			sb.append(" 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ? AND (");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" IS NULL OR ");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" < (SELECT ");
			sb.append(dbHelper.columnRef(COLUMN_SEQ_NUMBER));
			sb.append(" FROM ");
			sb.append(dbHelper.tableRef(TABLE_VALUE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_NAME));
			sb.append(" = ?)) ");
            dbHelper.limitLast(sb, 1);
			return sb.toString();
		}

		private String createNotAffirmedMessageStatement() {
			StringBuilder sb = new StringBuilder();
			dbHelper.limitStart(sb, 1);
            sb.append("SELECT ");
            dbHelper.limitColumns(sb, 1);
			sb.append(" 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" = ? AND (");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" IS NULL OR ");
			sb.append(dbHelper.columnRef(COLUMN_CONFIRMED));
			sb.append(" < ?)");
            dbHelper.limitLast(sb, 1);
			return sb.toString();
		}

		private String createExistsNotRunningNodeStatement() {
			StringBuilder sb = new StringBuilder();
			dbHelper.limitStart(sb, 1);
            sb.append("SELECT ");
            dbHelper.limitColumns(sb, 1);
			sb.append(" 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			sb.append(" WHERE ");
			sb.append(dbHelper.columnRef(COLUMN_STATE));
			sb.append(" != ? ");
            dbHelper.limitLast(sb, 1);
			return sb.toString();
		}

		private String createExistsAnyClusterNodeStatement() {
			StringBuilder sb = new StringBuilder();
			dbHelper.limitStart(sb, 1);
			sb.append("SELECT ");
			dbHelper.limitColumns(sb, 1);
			sb.append(" 1 FROM ");
			sb.append(dbHelper.tableRef(TABLE_NODE));
			dbHelper.limitLast(sb, 1);
			return sb.toString();
		}

        /**
         * Convenience method to create an array containing all parameters of the method as
         * elements.
         */
        protected final <T> T[] params(T... elements) {
            return elements;
        }


        /**
         * Generates a new sequence number and locks the sequence generator table for other
         * nodes until end of transaction.
         *
         * Note: To synchronize database access with other nodes in cluster, start each
         * access with nextSequenceNumber(REQUEST_ID).
         *
         * @param sequence
         *        the sequence to get next number for
         */
        protected long nextSequenceNumber(String sequence) throws SQLException {
            return sequenceManager.nextSequenceNumber(dbHelper, dbExec.getTransactionConnection(), dbHelper.retryCount(), sequence);
        }

        /**
         * Fires the events contained in the given pendingInfoLists.<br/>
         * See {@link #createPendingInfoLists()} for details to the information within the lists.
         */
        protected void firePendingInfos(List<Object[]>[] pendingInfoLists) {
            for (Object[] values : pendingInfoLists[TUPLE_INDEX_PROPERTY_CHANGED]) {
                firePropertyChanged((String)values[0], values[1], values[2], ((Boolean)values[3]).booleanValue(), ((Boolean)values[4]).booleanValue());
            }
            for (Object[] values : pendingInfoLists[TUPLE_INDEX_PROPERTY_CHANGE_CONFIRMED]) {
                firePropertyChangeConfirmed((String)values[0], values[1], ((Boolean)values[2]).booleanValue());
            }
        }

        /**
         * Creates an array containing two lists. The lists contain information about
         * events to send. This first list contains property changed events, the second list
         * property change confirmed events. The events are represented as parameter list as
         * required by the firePropertyChanged and firePropertyChangeConfirmed methods.
         */
        protected List<Object[]>[] createPendingInfoLists() {
            List<Object[]>[] lists = new List[2];
            lists[0] = new ArrayList<>();
            lists[1] = new ArrayList<>();
            return lists;
        }

		private final String SELECT_NODES_WITH_STATE_STATEMENT;

		private final String LIFE_SIGN_STATEMENT;

		private final String REMOVE_DEAD_STATEMENT;

		private final String REMOVE_LONG_DEAD_STATEMENT;

		private final String REVIVE_STATEMENT;

		private final String CLEANUP_STATEMENT;

		private final String INIT_NODE_STATEMENT;

		private final String REMOVE_NODE_STATEMENT;

		private final String SET_STATE_STATEMENT;

		private final String READ_PROPERTY_STATEMENT;

		private final String INSERT_PROPERY_STATEMENT;

		private final String UPDATE_PROPERY_STATEMENT;

		private final String CHECK_NODE_TABLE_STATEMENT;
        private final String CHECK_VALUE_TABLE_STATEMENT;
        private final String CHECK_NODE_COLUMNS_STATEMENT;
        private final String CHECK_VALUE_COLUMNS_STATEMENT;
		
		private final String READ_CHANGES_STATEMENT;

		private final String CONFIRM_STATEMENT;

		private final String GET_NODE_STATE_FOR_ID;

		private final String NOT_AFFIRMED_MESSAGE_STATEMENT;

		private final String NOT_AFFIRMED_PROPERTY_STATEMENT;

		private final String EXISTS_ANY_CLUSTER_NODE_STATEMENT;

		private final String EXISTS_NOT_RUNNING_NODE_STATEMENT;

        protected Long initNode(String initialState) throws SQLException {
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalCleanUp();
				Long newNodeID = Long.valueOf(nextSequenceNumber(NODE_ID));
                Long timeStamp = Long.valueOf(DBUtil.currentTimeMillis(dbExec.getTransactionConnection(), dbHelper));
                dbExec.executeUpdate(INIT_NODE_STATEMENT, params(newNodeID, initialState, timeStamp));
                dbExec.commitTransaction();
                return newNodeID;
            }
            finally {
                dbExec.closeTransaction();
            }
        }

        protected void removeNode() throws SQLException {
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalUpdateLifesign();
                dbExec.executeUpdate(REMOVE_NODE_STATEMENT, params(nodeID));
                internalCleanUp();
                dbExec.commitTransaction();
            }
            finally {
                dbExec.closeTransaction();
            }
        }

        protected boolean allNodesRunning() throws SQLException {
            List<Object[]>[] pendingInfoLists = createPendingInfoLists();
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalRefetch(pendingInfoLists);
                boolean allRunning = !DBUtil.executeQueryAsBoolean(dbExec.getTransactionConnection(), EXISTS_NOT_RUNNING_NODE_STATEMENT, params(NodeState.RUNNING.name()));
                dbExec.commitTransaction();
                return allRunning;
            }
            finally {
                dbExec.closeTransaction();
                firePendingInfos(pendingInfoLists);
            }
        }

        protected void setNodeState(Long nodeID, String state) throws SQLException {
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalUpdateLifesign();
                dbExec.executeUpdate(SET_STATE_STATEMENT, params(state, nodeID));
                dbExec.commitTransaction();
            }
            finally {
                dbExec.closeTransaction();
            }
        }

        protected void setValue(String property, String value, boolean overwrite) throws PendingChangeException, SQLException {
            List<Object[]>[] pendingInfoLists = createPendingInfoLists();
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                PendingChangeException e = internalSetValue(property, value, overwrite, pendingInfoLists);
                dbExec.commitTransaction();
                if (e != null) throw e;
            }
            finally {
                dbExec.closeTransaction();
                firePendingInfos(pendingInfoLists);
            }
        }

        protected String getValue(String property) throws PendingChangeException, SQLException {
            List<Object[]>[] pendingInfoLists = createPendingInfoLists();
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalRefetch(pendingInfoLists);

				Object[] row =
					CollectionUtil.getFirst(DBUtil.executeQueryAsMatrix(dbHelper, dbExec.getTransactionConnection(),
						READ_PROPERTY_STATEMENT, params(property), TYPES));
                if (row == null) return null;
				boolean isConfirmed = internalIsConfirmed((Long) row[INDEX_COLUMN_SEQ_NUMBER]);
                dbExec.commitTransaction();

				if (isConfirmed)
					return (String) row[INDEX_COLUMN_VALUE];
                throw new PendingChangeException(property, row[INDEX_COLUMN_OLD_VALUE], row[INDEX_COLUMN_VALUE]);
            }
            finally {
                dbExec.closeTransaction();
                firePendingInfos(pendingInfoLists);
            }
        }

        protected boolean isConfirmed(String property) throws SQLException {
            List<Object[]>[] pendingInfoLists = createPendingInfoLists();
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalRefetch(pendingInfoLists);
                boolean confirmed = true;
                if (property == null) {
                    for (String prop : declaredValues.keySet()) {
                        if (!internalIsConfirmed(prop)) {
                            confirmed = false;
                            break;
                        }
                    }
                }
                else {
                    confirmed = internalIsConfirmed(property);
                }
                dbExec.commitTransaction();
                return confirmed;
            }
            finally {
                dbExec.closeTransaction();
                firePendingInfos(pendingInfoLists);
            }
        }

        protected void refetch() throws SQLException {
            List<Object[]>[] pendingInfoLists = createPendingInfoLists();
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalRefetch(pendingInfoLists);
                dbExec.commitTransaction();
            }
            finally {
                dbExec.closeTransaction();
                firePendingInfos(pendingInfoLists);
            }
        }

        protected void updateLifesign() throws SQLException {
            dbExec.beginTransaction();
            try {
                nextSequenceNumber(REQUEST_ID);
                internalUpdateLifesign();
                dbExec.commitTransaction();
            }
            finally {
                dbExec.closeTransaction();
            }
        }



        // core methods

        protected boolean internalIsConfirmed(Long messageID) throws SQLException {
            return !DBUtil.executeQueryAsBoolean(dbExec.getTransactionConnection(), NOT_AFFIRMED_MESSAGE_STATEMENT, params(NodeState.RUNNING.name(), messageID));
        }

        protected boolean internalIsConfirmed(String property) throws SQLException {
            return !DBUtil.executeQueryAsBoolean(dbExec.getTransactionConnection(), NOT_AFFIRMED_PROPERTY_STATEMENT, params(NodeState.RUNNING.name(), property));
        }


        protected PendingChangeException internalSetValue(String property, String rawValue, boolean overwrite, List<Object[]>[] pendingInfoLists) throws SQLException {
			Long messageID = Long.valueOf(nextSequenceNumber(MESSAGE_ID));
			Object[] row =
				CollectionUtil.getFirst(DBUtil.executeQueryAsMatrix(dbHelper, dbExec.getTransactionConnection(),
					READ_PROPERTY_STATEMENT, params(property), TYPES));
            String oldRawValue = null;
            if (row == null) {
                dbExec.executeUpdate(INSERT_PROPERY_STATEMENT, params(property, rawValue, messageID));
            }
            else {
				if (internalIsConfirmed((Long) row[INDEX_COLUMN_SEQ_NUMBER])) {
					oldRawValue = (String) row[INDEX_COLUMN_VALUE];
                }
                else {
					if (overwrite)
						oldRawValue = (String) row[INDEX_COLUMN_OLD_VALUE];
                    else return new PendingChangeException(property, row[INDEX_COLUMN_OLD_VALUE], row[INDEX_COLUMN_VALUE]);
                }
                dbExec.executeUpdate(UPDATE_PROPERY_STATEMENT, params(rawValue, oldRawValue, messageID, property));
            }
            pendingChanges.put(property, messageID);
            pendingInfoLists[TUPLE_INDEX_PROPERTY_CHANGED].add(params(property, oldRawValue, rawValue, Boolean.TRUE, Boolean.TRUE));
            return null;
        }


        protected void internalRefetch(List<Object[]>[] pendingInfoLists) throws SQLException {
            internalUpdateNodeTable();
            internalReadChanges(pendingInfoLists);
            internalCheckForConfirmation(pendingInfoLists);
        }

		protected void internalUpdateNodeTable() throws SQLException {
			long timeStamp = DBUtil.currentTimeMillis(dbExec.getTransactionConnection(), dbHelper);
            internalUpdateLifesign(timeStamp);
            internalRemoveDeadEntries(timeStamp);
		}

		protected List<Long> internalGetRunningOrStartingNodes() throws SQLException {
			dbExec.beginTransaction();
			try {
				internalUpdateNodeTable();
				List<Long> result =
					DBUtil.executeQuery(dbExec.getTransactionConnection(), SELECT_NODES_WITH_STATE_STATEMENT,
						params(NodeState.RUNNING.name(), NodeState.STARTUP.name()),
						ResultAsLongList.INSTANCE);
				dbExec.commitTransaction();
				return result;
			} finally {
				dbExec.closeTransaction();
			}
		}

        protected void internalReadChanges(List<Object[]>[] pendingInfoLists) throws SQLException, NumberFormatException {
            // read changed values
            List<Tuple> changedValues = new ArrayList();
			List<Object[]> changes =
				DBUtil.executeQueryAsMatrix(dbHelper, dbExec.getTransactionConnection(), READ_CHANGES_STATEMENT,
					params(Long.valueOf(lastSeenMessage)), TYPES);
            long newLastSeen = lastSeenMessage;
			for (Object[] row : changes) {
				long messageID = (Long) row[INDEX_COLUMN_SEQ_NUMBER];
                newLastSeen = Math.max(newLastSeen, messageID);

				String property = (String) row[INDEX_COLUMN_NAME];
				String rawValue = (String) row[INDEX_COLUMN_VALUE];
				String oldRawValue = (String) row[INDEX_COLUMN_OLD_VALUE];

                Long pendingID = pendingChanges.put(property, null);
                if (pendingID != null && pendingID.longValue() == messageID) {
                    continue; // this own change was already published.
                }

                Object value = rawValue;
                Tuple tuple = declaredValues.get(property);
                if (tuple != null) {
                    Mapping<String, ?> parser = (Mapping<String, ?>)tuple.get(TUPLE_INDEX_PARSER);
                    value = parser.map(rawValue);
                    changedValues.add(TupleFactory.newTuple(params(property, parser.map(oldRawValue), value)));
                }
                globalValues.put(property, value);
            }

            // confirm reads to cluster
            if (newLastSeen != lastSeenMessage) {
                lastSeenMessage = newLastSeen;
                dbExec.executeUpdate(CONFIRM_STATEMENT, params(Long.valueOf(lastSeenMessage), nodeID));
            }

            // inform about value changes from other nodes in cluster
            for (Tuple property : changedValues) {
                pendingInfoLists[TUPLE_INDEX_PROPERTY_CHANGED].add(params(property.get(0), property.get(1), property.get(2), Boolean.FALSE, Boolean.FALSE));
            }
        }

        protected void internalCheckForConfirmation(List<Object[]>[] pendingInfoLists) throws SQLException {
            // inform about confirmed value changes
            for (Iterator<String> it = pendingChanges.keySet().iterator(); it.hasNext(); ) {
                String property = it.next();
                if (internalIsConfirmed(property)) {
                    it.remove();
                    pendingInfoLists[TUPLE_INDEX_PROPERTY_CHANGE_CONFIRMED].add(params(property, globalValues.get(property), Boolean.FALSE));
                }
            }
        }

        protected void internalUpdateLifesign() throws SQLException {
            internalUpdateLifesign(DBUtil.currentTimeMillis(dbExec.getTransactionConnection(), dbHelper));
        }

        protected void internalUpdateLifesign(long currentTimeStamp) throws SQLException {
            Long timeStamp = Long.valueOf(currentTimeStamp);
            int result = dbExec.executeUpdate(LIFE_SIGN_STATEMENT, params(timeStamp, nodeID));
            if (result < 1) { // someone has declared this node wrongly as dead...
                dbExec.executeUpdate(REVIVE_STATEMENT, params(nodeID, nodeState.name(), timeStamp, Long.valueOf(lastSeenMessage)));
            }
        }

        protected void internalRemoveDeadEntries(long currentTimeStamp) throws SQLException {
            Long deadNodes = Long.valueOf(currentTimeStamp - timeoutRunningNode);
            dbExec.executeUpdate(REMOVE_DEAD_STATEMENT, params(NodeState.RUNNING.name(), deadNodes));
            deadNodes = Long.valueOf(currentTimeStamp - timeoutOtherNode);
            dbExec.executeUpdate(REMOVE_LONG_DEAD_STATEMENT, params(deadNodes));
        }

        protected void internalCleanUp() throws SQLException {
            internalRemoveDeadEntries(DBUtil.currentTimeMillis(dbExec.getTransactionConnection(), dbHelper));
            if (!DBUtil.executeQueryAsBoolean(dbExec.getTransactionConnection(), EXISTS_ANY_CLUSTER_NODE_STATEMENT, null)) {
                dbExec.executeUpdate(CLEANUP_STATEMENT);
            }
        }

		protected NodeState getNodeState(long nodeId) {
			try {
				String nodeStateString = DBUtil.executeQueryAsString(GET_NODE_STATE_FOR_ID, new Object[] { nodeId });
				if (StringServices.isEmpty(nodeStateString)) {
					return null;
				}
				return NodeState.valueOf(nodeStateString);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}

    }

	@Override
	protected void startUp() {
		if (isClusterMode()) {
			_periodicalRefetch = ClusterManagerRefetch.createPeriodic(this);
			_periodicalRefetch.start(_refetchInterval, TimeUnit.MILLISECONDS);
		}
	}

    @Override
    protected void shutDown() {
		if (_periodicalRefetch != null) {
			_periodicalRefetch.stop(_refetchWaitOnStopTimeout, TimeUnit.MILLISECONDS);
		}
        removeNode();
		super.shutDown();
    }

	public static final class Module extends TypedRuntimeModule<ClusterManager> {

        public static final Module INSTANCE = new Module();

        @Override
        public Class<ClusterManager> getImplementation() {
            return ClusterManager.class;
        }

    }

    public static final void startUpClusterManager() throws IllegalArgumentException, ModuleException {
        Module module = Module.INSTANCE;
        if (module.isActive()) {
            ModuleUtil.INSTANCE.restart(module, null);
        } else {
            ModuleUtil.INSTANCE.startUp(module);
        }
    }


    public static void shutDownClusterManager() {
        Module module = Module.INSTANCE;
        if (module.isActive()) {
            ModuleUtil.INSTANCE.shutDown(module);
        }
    }

}
