/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.flexdata;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.FilteredIterator;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;

/**
 * Operation directly on the database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DBOperation implements Runnable {

	private final Protocol _log;

	private final String _kbName;

	private KnowledgeBase _kb;

	private PooledConnection _connection;

	/**
	 * Creates a {@link DBOperation}.
	 * 
	 * @param log
	 *        The error output.
	 * @param kbName
	 *        The configuration name of the {@link KnowledgeBase} to read DB settings from.
	 */
	public DBOperation(Protocol log, String kbName) {
		_log = log;
		_kbName = kbName;
	}

	/**
	 * Name of {@link #kb()}.
	 */
	public String kbName() {
		return _kbName;
	}

	/**
	 * The error output.
	 */
	public Protocol log() {
		return _log;
	}

	@Override
	public void run() {
		try {
			setupService();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void setupService() throws Exception {
		try (ModuleContext context = ModuleUtil.beginContext()) {
			ModuleUtil.INSTANCE.startUp(PersistencyLayer.Module.INSTANCE);
			setupKB();
		}
	}

	private void setupKB() throws SQLException {
		KnowledgeBaseFactory kbf = KnowledgeBaseFactory.getInstance();
		_kb = kbf.createNamedKnowledgeBase(_kbName, _log);
		try {
			setupConnection();
		} finally {
			_kb = null;
		}
	}

	private void setupConnection() throws SQLException {
		ConnectionPool db = KBUtils.getConnectionPool(_kb);

		_connection = db.borrowWriteConnection();
		try {
			operate();
		} finally {
			db.releaseWriteConnection(_connection);
		}
	}

	/**
	 * Performs the operation on the database.
	 */
	protected abstract void operate() throws SQLException;

	/**
	 * The current connection to {@link #operate()} on.
	 */
	public PooledConnection connection() {
		if (_connection == null) {
			throw new IllegalStateException("Connection not set up");
		}
		return _connection;
	}

	/**
	 * The current {@link KnowledgeBase}.
	 */
	public KnowledgeBase kb() {
		if (_kb == null) {
			throw new IllegalStateException("KB not set up");
		}
		return _kb;
	}

	/**
	 * All non abstract and non system types in the {@link #kb()}.
	 */
	protected final Iterable<MOKnowledgeItem> concreteTypes() {
		return new Iterable<>() {
			@Override
			public Iterator<MOKnowledgeItem> iterator() {
				return new ConcreteTypesIterator(kb().getMORepository());
			}
		};
	}

	/**
	 * Execute the given SQL on the {@link #connection()}.
	 */
	protected final void exec(String sql) throws SQLException {
		PreparedStatement statement = _connection.prepareStatement(sql);
		try {
			statement.executeUpdate();
		} finally {
			statement.close();
		}
	}

	private static class ConcreteTypesIterator extends FilteredIterator<MOKnowledgeItem> {

		public ConcreteTypesIterator(MORepository types) {
			super(FilterUtil.filterIterator(MOKnowledgeItem.class, types.getMetaObjects().iterator()));
		}

		@Override
		protected boolean test(MOKnowledgeItem type) {
			if (type.isAbstract()) {
				return false;
			}
			if (type.isSystem()) {
				return false;
			}
			return true;
		}

	}

}
