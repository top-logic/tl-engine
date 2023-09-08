/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.persist;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.CommitContextWrapper;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.simple.GenericDataObject;


/**
 * Base class for DB-based {@link DataManager} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(ConnectionPoolRegistry.Module.class)
public abstract class AbstractDataManager extends DataManager {

	protected static final byte STRING_TYPE = 's';

	protected static final byte INTEGER_TYPE = 'i';

	protected static final byte LONG_TYPE = 'l';

	protected static final byte FLOAT_TYPE = 'f';

	protected static final byte DOUBLE_TYPE = 'd';

	protected static final byte BOOLEAN_TRUE = 'T';

	protected static final byte BOOLEAN_FALSE = 'F';

	protected static final byte DATE_TYPE = 'D';

	protected static final byte LINK_TYPE = 'L';

	protected static final byte UNKNOWN_TYPE = 'U';

	protected static final byte NULL_TYPE = 'N';


	/** A DataSource to fork of new Database Connections */
	protected DataSource dataSource;

	/** The DBHeloper to maneuver around the differnces in these Databases */
	protected DBHelper dbHelper;

	protected final ConnectionPool connectionPool;

	/**
	 * Configuration for {@link AbstractDataManager}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<AbstractDataManager> {
		/**
		 * Connection Pool.
		 */
		@StringDefault(ConnectionPoolRegistry.DEFAULT_POOL_NAME)
		String getConnectionPool();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link AbstractDataManager}.
	 */
	public AbstractDataManager(InstantiationContext context, Config config) throws SQLException {
		super(context, config);

		this.connectionPool = ConnectionPoolRegistry.getConnectionPool(config.getConnectionPool());
		this.dataSource = connectionPool.getDataSource();
		init();
	}

	/**
	 * Secondary CTor to setup the DBHelper (will setup the first Connection as
	 * well)
	 */
	protected final void init() throws SQLException {
		dbHelper = connectionPool.getSQLDialect();
	}

	@Override
	public final boolean store(DataObject anObject) throws SQLException {
		PooledConnection connection = connectionPool.borrowWriteConnection();
		try {
			CommitContextWrapper commitContext = new CommitContextWrapper(connection);
			boolean result = optimizedStore(commitContext, anObject);
			
			connection.commit();
			return result;
		} finally {
			connectionPool.releaseWriteConnection(connection);
		}
	}

	@Override
	public final boolean store(DataObject anObject, CommitContext aCache) throws SQLException {
		return optimizedStore(aCache, anObject);
	}

	private boolean optimizedStore(CommitContext context, DataObject anObject) throws SQLException {
		if (anObject instanceof GenericDataObject) {
			return internalStore(context, ((GenericDataObject) anObject).getMetaObjectName(), anObject.getIdentifier(), anObject);
		} else {
			return internalStore(context, anObject.tTable().getName(), anObject.getIdentifier(), anObject);
		}
	}
	
	@Override
	public final boolean storeValues(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException {
        return internalStore(context, type, id, values);
	}

	/**
	 * Implementation of {@link DataManager#storeValues(CommitContext, String, TLID, NamedValues)}
	 */
	protected abstract boolean internalStore(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException;


	@Override
	public void close() {
		// TODO KHA: Why?
		// Ignore.
	}

}
