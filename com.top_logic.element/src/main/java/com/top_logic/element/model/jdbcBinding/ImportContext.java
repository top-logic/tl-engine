/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
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

	private final Map<String, Set<List<String>>> _nonPrimaryIndices = new HashMap<>();

	private final Map<String, Map<Object, TLObject>> _objectByTableAndId = new HashMap<>();

	private final Map<String, Map<List<String>, Map<Object, TLObject>>> _objectByTableAndIndexAndId = new HashMap<>();

	private final Map<String, Map<Object, Map<TLReference, ReferencePromise>>> _referenceByTableReferenceAndId =
		new HashMap<>();

	private final List<Runnable> _resolvers = new ArrayList<>();

	private final Map<TLType, List<TLStructuredTypePart>> _referrers;

	/**
	 * Creates an {@link ImportContext}.
	 */
	public ImportContext(PooledConnection connection, DBSchema schema, TLModule module) throws SQLException {
		_connection = connection;
		_schema = schema;
		_module = module;

		_sqlDialect = connection.getSQLDialect();
		_factory = ModelService.getInstance().getFactory();

		_referrers = Map.copyOf(computeTypeReferrers(module));
	}

	private final Map<TLType, List<TLStructuredTypePart>> computeTypeReferrers(TLModule module) {
		Map<TLType, List<TLStructuredTypePart>> referrers = new HashMap<>();
		for (TLType type : module.getTypes()) {
			if (type instanceof TLStructuredType) {
				for (TLStructuredTypePart part : ((TLStructuredType) type).getLocalParts()) {
					referrers.computeIfAbsent(part.getType(), x -> new ArrayList<>()).add(part);
				}
			}
		}
		return referrers;
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
		for (var tableEntry : _referenceByTableReferenceAndId.entrySet()) {
			String tableName = tableEntry.getKey();
			Map<Object, TLObject> index = typeIndex(tableName);
			for (var objectEntry : tableEntry.getValue().entrySet()) {
				Object sourceId = objectEntry.getKey();
				TLObject source = index.get(sourceId);
				if (source == null) {
					// TODO JST: Warning.
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

	/**
	 * The indices which are defined not by the table itself, but by other tables which are
	 * referencing it through their own custom set of columns.
	 */
	public Set<List<String>> getNonPrimaryIndices(String table) {
		return Set.copyOf(_nonPrimaryIndices.getOrDefault(table, emptySet()));
	}

	/** @see #getNonPrimaryIndices(String) */
	public void addNonPrimaryIndex(String table, List<String> index) {
		_nonPrimaryIndices.computeIfAbsent(table, x -> new HashSet<>()).add(List.copyOf(index));
	}

	/**
	 * Looks up the index mapping a primary key value to an imported object of the given table.
	 */
	public Map<Object, TLObject> typeIndex(String tableName) {
		return _objectByTableAndId.computeIfAbsent(tableName, x -> new HashMap<>());
	}

	/**
	 * Looks up the index mapping a non-primary key value to an imported object of the given table.
	 */
	public Map<Object, TLObject> typeBySecondaryIndex(String tableName, List<String> indexColumns) {
		return _objectByTableAndIndexAndId
			.computeIfAbsent(tableName, x -> new HashMap<>())
			.computeIfAbsent(indexColumns, x -> new HashMap<>());
	}

	/**
	 * Adds a resolver action running after all data has been read.
	 */
	public void addResolver(Runnable action) {
		_resolvers.add(action);
	}

	// TODO JST in eigene Datei extrahieren
	/**
	 * Collected data for synthesizing a multiple reference from target objects with foreign key
	 * references.
	 */
	public static class ReferencePromise {

		private final List<RefEntry> _entries = new ArrayList<>();

		/**
		 * Adds the ID of a value object with an order value to the reference.
		 */
		public void addOrderedDeferred(Map<Object, TLObject> index, Object destId, Object orderValue) {
			_entries.add(new DeferredRefEntry(index, destId, orderValue));
		}

		/**
		 * Adds a value object with an order value to the reference.
		 */
		public void addOrdered(TLObject target, Object orderValue) {
			_entries.add(new DirectRefEntry(target, orderValue));
		}

		/**
		 * Called after all data has been read to actually create the reference value.
		 */
		protected void resolve(TLObject source, TLReference reference) {
			Collections.sort(_entries);
			Object oldValue = source.tValue(reference);
			List<TLObject> newValues = resolveEntriesToTargets();
			if (oldValue != null) {
				logErrorReferenceClash(reference, source, oldValue, newValues);
			}
			source.tUpdate(reference, newValues);
		}

		private List<TLObject> resolveEntriesToTargets() {
			return _entries.stream()
				.map(e -> e.getTarget())
				.filter(Objects::nonNull)
				.collect(toList());
		}

		private void logErrorReferenceClash(TLReference reference, Object source, Object newTarget,
				Object oldTarget) {
			logError("Reference " + qualifiedName(reference) + ": Multiple values for object '" + label(source)
				+ "'. First: '" + label(oldTarget) + "'. Second: '" + label(newTarget));
		}

		private String label(Object object) {
			return MetaLabelProvider.INSTANCE.getLabel(object);
		}

		private static void logError(String message) {
			Logger.error(message, ReferencePromise.class);
		}

		private static abstract class RefEntry implements Comparable<RefEntry> {
			/**
			 * Creates a {@link RefEntry}.
			 */
			public RefEntry(Object orderValue) {
				_orderValue = orderValue;
			}

			private final Object _orderValue;

			/**
			 * The target object to store.
			 */
			public abstract TLObject getTarget();

			@Override
			public int compareTo(RefEntry o) {
				return Objects.compare(_orderValue, o._orderValue, ComparableComparator.INSTANCE);
			}
		}

		private static class DirectRefEntry extends RefEntry {

			private final TLObject _target;

			/**
			 * Creates a {@link DirectRefEntry}.
			 */
			public DirectRefEntry(TLObject target, Object orderValue) {
				super(orderValue);
				_target = target;
			}

			@Override
			public TLObject getTarget() {
				return _target;
			}
		}

		private static class DeferredRefEntry extends RefEntry {

			private final Map<Object, TLObject> _index;

			private final Object _targetId;

			/**
			 * Creates a {@link DeferredRefEntry}.
			 */
			public DeferredRefEntry(Map<Object, TLObject> index, Object targetId, Object orderValue) {
				super(orderValue);
				_index = index;
				_targetId = targetId;
			}

			@Override
			public TLObject getTarget() {
				TLObject result = _index.get(_targetId);
				if (result == null) {
					logError("Failed to resolve id '" + _targetId + "'.");
				}
				return result;
			}

			private static void logError(String message) {
				Logger.error(message, DeferredRefEntry.class);
			}

		}
	}

	/**
	 * Creates a reference value collector.
	 *
	 * @param table
	 *        The table from where the objects are imported that have the reference.
	 * @param idRef
	 *        The primary key value of the object whose reference is built.
	 * @param reference
	 *        The reference that should be set.
	 * @return A builder for a (multiple) reference value.
	 */
	public ReferencePromise reference(String table, Object idRef, TLReference reference) {
		return _referenceByTableReferenceAndId
			.computeIfAbsent(table, x -> new HashMap<>())
			.computeIfAbsent(idRef, x -> new HashMap<>())
			.computeIfAbsent(reference, x -> new ReferencePromise());
	}

	/**
	 * Resolve references that have the given type as target type.
	 */
	public List<TLStructuredTypePart> referrers(TLType type) {
		return _referrers.getOrDefault(type, emptyList());
	}

}