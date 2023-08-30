/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.util.model.ModelService;

/**
 * Data for a single data import run.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImportContext {
	private final PooledConnection _connection;

	private final DBSchema _schema;

	private final TLModule _module;

	private final DBHelper _sqlDialect;

	private final TLFactory _factory;

	private Map<String, Map<Object, TLObject>> _objectByTableAndId;

	private Map<String, Map<Object, Map<TLReference, ReferencePromise>>> _referenceByTableReferenceAndId;

	private List<Runnable> _resolvers;

	/**
	 * Creates an {@link ImportContext}.
	 */
	public ImportContext(PooledConnection connection, DBSchema schema, TLModule module) throws SQLException {
		_connection = connection;
		_schema = schema;
		_module = module;

		_sqlDialect = connection.getSQLDialect();
		_factory = ModelService.getInstance().getFactory();

		_objectByTableAndId = new HashMap<>();
		_referenceByTableReferenceAndId = new HashMap<>();
		_resolvers = new ArrayList<>();
	}

	/**
	 * The connection to the external data.
	 */
	public PooledConnection getConnection() {
		return _connection;
	}

	/**
	 * The schema extracted from the external data.
	 */
	public DBSchema getSchema() {
		return _schema;
	}

	/**
	 * The module to import data to.
	 */
	public TLModule getModule() {
		return _module;
	}

	/**
	 * The factory to create objects from.
	 */
	public TLFactory getFactory() {
		return _factory;
	}

	/**
	 * The database abstraction layer for the external schema.
	 */
	public DBHelper getSqlDialect() {
		return _sqlDialect;
	}

	/**
	 * Processing to be done before the import is finished.
	 */
	public void resolve() {
		for (Entry<String, Map<Object, Map<TLReference, ReferencePromise>>> tableEntry : _referenceByTableReferenceAndId
			.entrySet()) {
			String tableName = tableEntry.getKey();
			Map<Object, TLObject> index = typeIndex(tableName);
			for (Entry<Object, Map<TLReference, ReferencePromise>> objectEntry : tableEntry.getValue().entrySet()) {
				Object sourceId = objectEntry.getKey();
				TLObject source = index.get(sourceId);
				if (source == null) {
					// TODO: Warning.
				} else {
					for (Entry<TLReference, ReferencePromise> refEntry : objectEntry.getValue().entrySet()) {
						refEntry.getValue().resolve(source, refEntry.getKey());
					}
				}
			}
		}
		for (Runnable resolver : _resolvers) {
			resolver.run();
		}
	}

	public Map<Object, TLObject> typeIndex(String tableName) {
		return _objectByTableAndId.computeIfAbsent(tableName, x -> new HashMap<>());
	}

	public void addResolver(Runnable action) {
		_resolvers.add(action);
	}

	public static class ReferencePromise {

		private final List<RefEntry> _entries = new ArrayList<>();

		/**
		 * TODO
		 */
		public void addOrdered(TLObject target, Object orderValue) {
			_entries.add(new RefEntry(target, orderValue));
		}

		/**
		 * TODO
		 */
		public void resolve(TLObject source, TLReference reference) {
			Collections.sort(_entries);
			source.tUpdate(reference, _entries.stream().map(e -> e.getTarget()).collect(Collectors.toList()));
		}

		private static class RefEntry implements Comparable<RefEntry> {

			private final TLObject _target;

			private final Object _orderValue;

			/**
			 * Creates a {@link RefEntry}.
			 */
			public RefEntry(TLObject target, Object orderValue) {
				_target = target;
				_orderValue = orderValue;
			}

			/**
			 * The target object to store.
			 */
			public TLObject getTarget() {
				return _target;
			}

			@Override
			public int compareTo(RefEntry o) {
				return Objects.compare(_orderValue, o._orderValue, ComparableComparator.INSTANCE);
			}

		}

	}

	public ReferencePromise reference(String table, Object idRef, TLReference reference) {
		return _referenceByTableReferenceAndId
			.computeIfAbsent(table, x -> new HashMap<>())
			.computeIfAbsent(idRef, x -> new HashMap<>())
			.computeIfAbsent(reference, x -> new ReferencePromise());
	}

}