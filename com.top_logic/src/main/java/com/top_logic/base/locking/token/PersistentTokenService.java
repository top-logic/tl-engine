/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.module.ModuleRuntimeException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.PersistentIdFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link TokenService} using database tables for synchronization.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(ConnectionPoolRegistry.Module.class)
public class PersistentTokenService extends ConfiguredTokenService<ConfiguredTokenService.Config<?>> {

	private CompiledStatement _getLock;

	private PersistentIdFactory _idFactory;

	private CompiledStatement _createLock;

	private CompiledStatement _getToken;

	private DBHelper _sqlDialect;

	private CompiledStatement _createToken;

	private CompiledStatement _deleteToken;

	private CompiledStatement _deleteLock;

	private CompiledStatement _getTokens;

	private CompiledStatement _getAllTokens;

	/**
	 * Creates a {@link PersistentTokenService}.
	 */
	public PersistentTokenService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		try {
			tryStartUp();
		} catch (SQLException ex) {
			throw new ModuleRuntimeException(ex);
		}
	}

	private void tryStartUp() throws SQLException {
		_sqlDialect = pool().getSQLDialect();
		_idFactory = new PersistentIdFactory(pool(), PersistentIdFactory.class.getName());

		_getLock = compile(query(
			parameters(
				parameterDef(DBType.LONG, "objBranch"),
				parameterDef(DBType.ID, "objId"),
				parameterDef(DBType.STRING, "name")),
			select(
				columns(
					// Note: Must not use column aliases, since H2 cannot update a row if it was
					// selected with column aliases.
					columnDef(column("l", "OBJ_BRANCH")),
					columnDef(column("l", "OBJ_ID")),
					columnDef(column("l", "NAME")),
					columnDef(column("l", "ID")),
					columnDef(column("l", "EXCLUSIVE"))),
				table("TL_LOCK", "l"),
				and(
					eqSQL(column("l", "OBJ_BRANCH"), parameter(DBType.LONG, "objBranch")),
					eqSQL(column("l", "OBJ_ID"), parameter(DBType.ID, "objId")),
					eqSQL(column("l", "NAME"), parameter(DBType.STRING, "name")))).forUpdate()));
		_getLock.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		_createLock = compile(query(
			parameters(
				parameterDef(DBType.LONG, "id"),
				parameterDef(DBType.LONG, "objBranch"),
				parameterDef(DBType.ID, "objId"),
				parameterDef(DBType.STRING, "objType"),
				parameterDef(DBType.STRING, "name"),
				parameterDef(DBType.BOOLEAN, "excl")),
			insert(
				table("TL_LOCK", "l"),
				columnNames("ID", "OBJ_BRANCH", "OBJ_ID", "OBJ_TYPE", "NAME", "EXCLUSIVE"),
				expressions(
					parameter(DBType.LONG, "id"),
					parameter(DBType.LONG, "objBranch"),
					parameter(DBType.ID, "objId"),
					parameter(DBType.STRING, "objType"),
					parameter(DBType.STRING, "name"),
					parameter(DBType.BOOLEAN, "excl")))));

		_createToken = compile(query(
			parameters(
				parameterDef(DBType.LONG, "id"),
				parameterDef(DBType.LONG, "lockId"),
				parameterDef(DBType.LONG, "timeout"),
				parameterDef(DBType.LONG, "ownerBranch"),
				parameterDef(DBType.ID, "ownerId"),
				parameterDef(DBType.STRING, "ownerType"),
				parameterDef(DBType.LONG, "operationId"),
				parameterDef(DBType.STRING, "operation"),
				parameterDef(DBType.LONG, "clusterNodeId")),
			insert(
				table("TL_LOCK_TOKEN", "t"),
				columnNames("ID", "LOCK_ID", "TIMEOUT", "OWNER_BRANCH", "OWNER_ID", "OWNER_TYPE", "OPERATION_ID",
					"OPERATION", "CLUSTER_NODE_ID"),
				expressions(
					parameter(DBType.LONG, "id"),
					parameter(DBType.LONG, "lockId"),
					parameter(DBType.LONG, "timeout"),
					parameter(DBType.LONG, "ownerBranch"),
					parameter(DBType.ID, "ownerId"),
					parameter(DBType.STRING, "ownerType"),
					parameter(DBType.LONG, "operationId"),
					parameter(DBType.STRING, "operation"),
					parameter(DBType.LONG, "clusterNodeId")))));

		_getToken = compile(query(
			parameters(
				parameterDef(DBType.LONG, "id")),
			select(
				columns(
					// Note: Must not use column aliases, since H2 cannot update a row if it was
					// selected with column aliases.
					columnDef(column("t", "ID")),
					columnDef(column("t", "LOCK_ID")),
					columnDef(column("t", "TIMEOUT")),
					columnDef(column("t", "OWNER_BRANCH")),
					columnDef(column("t", "OWNER_ID")),
					columnDef(column("t", "OWNER_TYPE")),
					columnDef(column("t", "OPERATION_ID")),
					columnDef(column("t", "OPERATION")),
					columnDef(column("t", "CLUSTER_NODE_ID"))),
				table("TL_LOCK_TOKEN", "t"),
				eqSQL(column("t", "ID"), parameter(DBType.LONG, "id"))).forUpdate()));
		_getToken.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		_getTokens = compile(query(
			parameters(
				parameterDef(DBType.LONG, "lockId")),
			select(
				columns(
					columnDef(column("t", "ID")),
					columnDef(column("t", "TIMEOUT")),
					columnDef(column("t", "OWNER_BRANCH")),
					columnDef(column("t", "OWNER_ID")),
					columnDef(column("t", "OWNER_TYPE")),
					columnDef(column("t", "OPERATION_ID")),
					columnDef(column("t", "OPERATION")),
					columnDef(column("t", "CLUSTER_NODE_ID"))),
				table("TL_LOCK_TOKEN", "t"),
				eqSQL(column("t", "LOCK_ID"), parameter(DBType.LONG, "lockId")))));
		_getTokens.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		_getAllTokens = compile(query(
			select(
				columns(
					columnDef(column("t", "ID")),
					columnDef(column("t", "LOCK_ID")),
					columnDef(column("t", "OWNER_BRANCH")),
					columnDef(column("t", "OWNER_ID")),
					columnDef(column("t", "OWNER_TYPE")),
					columnDef(column("t", "OPERATION_ID")),
					columnDef(column("t", "OPERATION")),
					columnDef(column("t", "CLUSTER_NODE_ID")),
					columnDef(column("t", "TIMEOUT")),
					columnDef(column("l", "OBJ_BRANCH")),
					columnDef(column("l", "OBJ_ID")),
					columnDef(column("l", "OBJ_TYPE")),
					columnDef(column("l", "NAME")),
					columnDef(column("l", "EXCLUSIVE"))),
				join(false,
					table("TL_LOCK_TOKEN", "t"),
					table("TL_LOCK", "l"),
					eqSQL(column("t", "LOCK_ID"), column("l", "ID"))))));

		_deleteToken = compile(query(
			parameters(
				parameterDef(DBType.LONG, "id")),
			delete(
				table("TL_LOCK_TOKEN"),
				eqSQL(column(null, "ID"), parameter(DBType.LONG, "id")))));
		_deleteLock = compile(query(
			parameters(
				parameterDef(DBType.LONG, "id")),
			delete(
				table("TL_LOCK"),
				eqSQL(column(null, "ID"), parameter(DBType.LONG, "id")))));
	}

	private CompiledStatement compile(SQLQuery<?> sql) {
		return sql.toSql(_sqlDialect);
	}

	private ConnectionPool pool() {
		return ConnectionPoolRegistry.getDefaultConnectionPool();
	}

	@Override
	public void acquire(Date expireDate, Collection<Token> tokens) throws TopLogicException {
		ConnectionPool pool = pool();

		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		String operationName = getOperation(context);
		TLObject owner = context.getSubSessionContext().getPerson();

		long operationId = _idFactory.createId();
		Long nodeId = fetchNodeId();

		Throwable problem = null;
		for (int retry = 0; retry < _sqlDialect.retryCount(); retry++) {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				for (Token token : tokens) {
					lockToken(connection, owner, operationId, expireDate, token, nodeId, operationName);
				}
				connection.commit();
			} catch (SQLException ex) {
				problem = ex;
				continue;
			} finally {
				pool.releaseWriteConnection(connection);
			}

			try {
				// Make sure that reads after a successful token acquisition get up
				// to date data. See Ticket #1093.
				KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().refetch();
			} catch (RefetchTimeout ex) {
				throw new TopLogicException(I18NConstants.ERROR_REFETCH__PROBLEM.fill(ex.getMessage()));
			}

			// Success.
			return;
		}

		throw new TopLogicException(
			I18NConstants.ERROR_DATABASE_ACCESS__PROBLEM.fill(problem != null ? problem.getMessage() : ""));
	}

	private String getOperation(DisplayContext context) {
		ComponentName componentName = context.getProcessingInfo().getComponentName();
		String operation;
		if (componentName != null) {
			operation = componentName.qualifiedName();
		} else {
			operation = null;
		}
		return operation;
	}

	private Long fetchNodeId() {
		Long nodeId;
		ClusterManager.Module cmModule = ClusterManager.Module.INSTANCE;
		if (cmModule.isActive()) {
			ClusterManager cm = cmModule.getImplementationInstance();
			nodeId = cm.getNodeId();
		} else {
			nodeId = null;
		}
		return nodeId;
	}

	private void lockToken(PooledConnection connection, TLObject owner, long operationId, Date expireDate, Token token,
			Long nodeId, Object operationName) throws SQLException, TopLogicException {
		TLObject object = token.getObject();
		long objBranch = objBranch(object);
		TLID objId = objId(object);
		String objType = objTable(object);
		String tokenName = token.getName();
		boolean exclusive = token.getKind() == Kind.EXCLUSIVE;

		long lockId;
		try (ResultSet resultSet = _getLock.executeQuery(connection, objBranch, objId, tokenName)) {
			if (resultSet.next()) {
				lockId = resultSet.getLong("ID");
				if (resultSet.getBoolean("EXCLUSIVE") || exclusive) {
					if (tryCleanup(connection, lockId)) {
						resultSet.updateBoolean("EXCLUSIVE", exclusive);
						resultSet.updateRow();
					} else {
						throw reportLockConflict(connection, lockId);
					}
				}
			} else {
				lockId = _idFactory.createId();
				_createLock.executeUpdate(connection, lockId, objBranch, objId, objType, tokenName, exclusive);
			}
		}
		long tokenId = _idFactory.createId();

		_createToken.executeUpdate(connection, tokenId, lockId, expireDate.getTime(), objBranch(owner), objId(owner),
			objTable(owner), operationId, operationName, nodeId);
		token.setId(tokenId);
	}

	private TopLogicException reportLockConflict(PooledConnection connection, long lockId) throws SQLException {
		if (getConfig().getReportLockOwnerDetails()) {
			try (ResultSet resultSet = _getTokens.executeQuery(connection, lockId)) {
				// IGNORE FindBugs(RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE): Seems to be a bug in spotbugs.
				if (resultSet.next()) {
					TLObject owner = getOwner(resultSet);
					long timeout = getTimeout(resultSet);
					Long nodeId = getClusterNodeId(resultSet);
					return createDetailedLockConflictError(owner, timeout, nodeId);
				}
			}
		}

		return createGenericLockConflictError();
	}

	private String objTable(TLObject object) {
		return object == null ? null : object.tHandle().tTable().getName();
	}

	private TLID objId(TLObject object) {
		return object == null ? IdentifierUtil.nullIdForMandatoryDatabaseColumns() : object.tHandle().getObjectName();
	}

	private long objBranch(TLObject object) {
		return object == null ? 0L : object.tHandle().getBranchContext();
	}

	private boolean tryCleanup(PooledConnection connection, long lockId) throws SQLException {
		long now = System.currentTimeMillis();
		ResultSet resultSet = _getTokens.executeQuery(connection, lockId);
		boolean released = true;
		while (resultSet.next()) {
			long timeout = getTimeout(resultSet);
			if (timeout < now) {
				resultSet.deleteRow();
			} else {
				released = false;
			}
		}
		return released;
	}

	@Override
	public boolean allValid(Collection<Token> tokens) {
		ConnectionPool pool = pool();
		SQLException problem = null;
		for (int retry = 0; retry < _sqlDialect.retryCount(); retry++) {
			// Note: Using a write connection, since the statement used is a "for update" statement.
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				try {
					return tryCheck(connection, tokens);
				} catch (SQLException ex) {
					problem = ex;
					continue;
				}
			} finally {
				pool.releaseWriteConnection(connection);
			}
		}
		throw new RuntimeException("Error checking lock validity.", problem);
	}

	private boolean tryCheck(PooledConnection connection, Collection<Token> tokens) throws SQLException {
		long now = System.currentTimeMillis();
		for (Token token : tokens) {
			ResultSet resultSet = _getToken.executeQuery(connection, token.getId());
			if (resultSet.next()) {
				long timeout = getTimeout(resultSet);
				if (timeout < now) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean renew(Date expireDate, Collection<Token> tokens) {
		ConnectionPool pool = pool();
		SQLException problem = null;
		for (int retry = 0; retry < _sqlDialect.retryCount(); retry++) {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				try {
					boolean result = tryRenew(connection, expireDate, tokens);
					if (result) {
						connection.commit();
					}
					return result;
				} catch (SQLException ex) {
					problem = ex;
					continue;
				}
			} finally {
				pool.releaseWriteConnection(connection);
			}
		}
		throw new RuntimeException("Error renewing lock.", problem);
	}

	private boolean tryRenew(PooledConnection connection, Date expireDate, Collection<Token> tokens)
			throws SQLException {
		long now = System.currentTimeMillis();
		for (Token token : tokens) {
			ResultSet resultSet = _getToken.executeQuery(connection, token.getId());
			if (resultSet.next()) {
				long timeout = getTimeout(resultSet);
				if (timeout < now) {
					return false;
				}
				resultSet.updateLong("TIMEOUT", expireDate.getTime());
				resultSet.updateRow();
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void release(Collection<Token> tokens) {
		ConnectionPool pool = pool();
		SQLException problem = null;
		for (int retry = 0; retry < _sqlDialect.retryCount(); retry++) {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				try {
					tryRelease(connection, tokens);
					connection.commit();
					return;
				} catch (SQLException ex) {
					problem = ex;
					continue;
				}
			} finally {
				pool.releaseWriteConnection(connection);
			}
		}
		throw new RuntimeException("Error releasing lock.", problem);
	}

	private void tryRelease(PooledConnection connection, Collection<Token> tokens) throws SQLException {
		for (Token token : tokens) {
			TLObject object = token.getObject();
			long lockId;

			// Look up and lock corresponding lock row.
			try (ResultSet resultSet =
				_getLock.executeQuery(connection, objBranch(object), objId(object), token.getName())) {
				if (resultSet.next()) {
					lockId = resultSet.getLong("ID");
				} else {
					// This should never happen.
					lockId = 0;
				}
			}

			_deleteToken.executeUpdate(connection, token.getId());

			if (lockId > 0) {
				ResultSet resultSet = _getTokens.executeQuery(connection, lockId);
				if (!resultSet.next()) {
					// No more tokens left, delete lock.
					_deleteLock.executeUpdate(connection, lockId);
				}
			}
		}
	}

	@Override
	public Collection<LockInfo> getAllLocks() {
		ConnectionPool pool = pool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			return tryGetAllLocks(connection);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_DATABASE_ACCESS__PROBLEM.fill(ex.getMessage()));
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private Collection<LockInfo> tryGetAllLocks(PooledConnection connection) throws SQLException {
		Map<Long, LockInfo> result = new HashMap<>();

		ResultSet resultSet = _getAllTokens.executeQuery(connection);
		while (resultSet.next()) {
			long operationId = resultSet.getLong("OPERATION_ID");
			LockInfo lockInfo = result.get(operationId);
			if (lockInfo == null) {
				lockInfo = new LockInfo();

				lockInfo.setOwner(getOwner(resultSet));
				lockInfo.setClusterNodeId(getClusterNodeId(resultSet));
				lockInfo.setOperation(resultSet.getString("OPERATION"));

				result.put(operationId, lockInfo);
			}

			long timeout = getTimeout(resultSet);
			if (lockInfo.getTimeout() == null || lockInfo.getTimeout().getTime() > timeout) {
				lockInfo.setTimeout(new Date(timeout));
			}

			long objBranch = resultSet.getLong("OBJ_BRANCH");
			long objId = resultSet.getLong("OBJ_ID");
			String objType = resultSet.getString("OBJ_TYPE");
			TLObject object = resolve(objBranch, objId, objType);
			Kind kind = resultSet.getBoolean("EXCLUSIVE") ? Kind.EXCLUSIVE : Kind.SHARED;
			String name = resultSet.getString("NAME");

			Token token = Token.newToken(kind, object, name);
			token.setId(resultSet.getLong("ID"));
			lockInfo.addToken(token);
		}
		return result.values();
	}

	private long getTimeout(ResultSet resultSet) throws SQLException {
		return resultSet.getLong("TIMEOUT");
	}

	private Long getClusterNodeId(ResultSet resultSet) throws SQLException {
		return getOptionalLong(resultSet, "CLUSTER_NODE_ID");
	}

	private static Long getOptionalLong(ResultSet resultSet, String columnName) throws SQLException {
		long result = resultSet.getLong(columnName);
		return resultSet.wasNull() ? null : Long.valueOf(result);
	}

	private TLObject getOwner(ResultSet resultSet) throws SQLException {
		long ownerBranch = resultSet.getLong("OWNER_BRANCH");
		long ownerId = resultSet.getLong("OWNER_ID");
		String ownerType = resultSet.getString("OWNER_TYPE");
		return resolve(ownerBranch, ownerId, ownerType);
	}

	private TLObject resolve(long branch, long id, String type) {
		if (id == 0L) {
			return null;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		KnowledgeItem resolveObjectKey = kb.resolveObjectKey(
			new DefaultObjectKey(branch, Revision.CURRENT_REV, kb.getMORepository().getMetaObject(type),
				LongID.valueOf(id)));
		if (resolveObjectKey == null) {
			return null;
		}
		return resolveObjectKey.getWrapper();
	}

}
