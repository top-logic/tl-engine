/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} fills an order attribute of a link table.
 */
public class SynthesizeLinkOrderProcessor extends AbstractConfiguredInstance<SynthesizeLinkOrderProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link SynthesizeLinkOrderProcessor}.
	 */
	@TagName("synthesize-link-order")
	public interface Config<I extends SynthesizeLinkOrderProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The name of the link table that defines the order attribute to be filled.
		 */
		@Mandatory
		@Name("link-table")
		String getLinkTable();

		/**
		 * The object table from which the order information is extracted.
		 */
		@Mandatory
		@Name("source-table")
		String getSourceTable();

		/**
		 * The order column in the link table to fill.
		 */
		@StringDefault(KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR)
		@Name("order-column")
		String getOrderColumn();

		/**
		 * Reference to the scope/parent object.
		 * 
		 * <p>
		 * An order is established by sorting all values of the {@link #getSourceOrderAttribute()}
		 * for all links originating from the same scope object.
		 * </p>
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		@Name("scope-ref")
		String getScopeRef();

		/**
		 * The reference to the object from which the order information is taken.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		@Name("source-ref")
		String getSourceRef();

		/**
		 * The attribute in the source table to take the order information from.
		 */
		@Mandatory
		@Name("source-order-attribute")
		String getSourceOrderAttribute();

		/**
		 * Factor to multiply the index with to get the internal ordering value.
		 */
		@IntDefault(1024)
		int getFactor();
	}

	/**
	 * Creates a {@link SynthesizeLinkOrderProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SynthesizeLinkOrderProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		Config<?> config = getConfig();
		try {
			MORepository repository = context.getPersistentRepository();

			MOClass sourceSpec = (MOClass) repository.getType(config.getSourceTable());
			MOAttribute sourceOrderAttr = sourceSpec.getAttribute(config.getSourceOrderAttribute());
			
			MOClass linkTable = (MOClass) repository.getType(config.getLinkTable());
			MOReference scopeRef = (MOReference) linkTable.getAttribute(config.getScopeRef());
			MOReference sourceRef = (MOReference) linkTable.getAttribute(config.getSourceRef());
			MOAttribute orderAttr = linkTable.getAttribute(config.getOrderColumn());
			
			for (MetaObject type : repository.getMetaObjects()) {
				if (!(type instanceof MOClass)) {
					continue;
				}
				
				MOClass sourceTable = (MOClass) type;
				if (sourceTable.isAbstract()) {
					continue;
				}
				
				if (!sourceTable.isSubtypeOf(sourceSpec)) {
					continue;
				}
				
				boolean hasBranches = util.hasBranches();
				String link = "l";
				String source = "s";
				CompiledStatement select = query(
					select(
						Util.listWithoutNull(
							util.branchColumnDefOrNull(link),
							columnDef(column(link, BasicTypes.IDENTIFIER_DB_NAME)),
							columnDef(column(link, BasicTypes.REV_MAX_DB_NAME)),
							columnDef(column(link, scopeRef.getColumn(ReferencePart.name).getDBName())),
							columnDef(column(source, sourceOrderAttr.getDbMapping()[0].getDBName()))
						),
						leftJoin(
							table(linkTable.getDBMapping().getDBName(), link), 
							table(sourceTable.getDBMapping().getDBName(), source), 
							and(
								hasBranches ?
									eqSQL(util.branchColumnRef(link), util.branchColumnRef(source))
									: literalTrueLogical(),
								eqSQL(column(link, sourceRef.getColumn(ReferencePart.name).getDBName()), column(source, BasicTypes.IDENTIFIER_DB_NAME)),
								le(column(link, BasicTypes.REV_MAX_DB_NAME), column(source, BasicTypes.REV_MAX_DB_NAME)),
								ge(column(link, BasicTypes.REV_MAX_DB_NAME), column(source, BasicTypes.REV_MIN_DB_NAME))
							)
						),
						eqSQL(
							literal(DBType.STRING, sourceTable.getName()), 
							column(link, sourceRef.getColumn(ReferencePart.type).getDBName())),
						Util.listWithoutNull(
							util.branchOrderOrNull(link),
							order(false, column(link, scopeRef.getColumn(ReferencePart.name).getDBName()))
						)
					)
				).toSql(connection.getSQLDialect());
								
				CompiledStatement update = query(
					select(
						Util.listWithoutNull(
							util.branchColumnDefOrNull(),
							columnDef(column(BasicTypes.IDENTIFIER_DB_NAME)),
							columnDef(column(BasicTypes.REV_MAX_DB_NAME)),
							columnDef(column(scopeRef.getColumn(ReferencePart.name).getDBName())),
							columnDef(column(orderAttr.getDbMapping()[0].getDBName()))
						),
						table(linkTable.getDBMapping().getDBName()),
						eqSQL(
							literal(DBType.STRING, sourceTable.getName()), 
							column(sourceRef.getColumn(ReferencePart.type).getDBName())),
						Util.listWithoutNull(
							util.branchOrderOrNull(),
							order(false, column(scopeRef.getColumn(ReferencePart.name).getDBName()))
						)
					)
				).toSql(connection.getSQLDialect());

				update.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

				// Since this processor requires two concurrent connections, the state must be
				// committed to have a consistent view to the database from both connections.
				connection.commit();

				int cnt = 0;
				int factor = getConfig().getFactor();
				int idIndex = util.getBranchIndexInc() + 1;
				int revIndex = util.getBranchIndexInc() + 2;
				int scopeIndex = util.getBranchIndexInc() + 3;
				int orderIndex = util.getBranchIndexInc() + 4;
				try (ResultSet cursor = update.executeQuery(connection)) {
					// Use additional connection for executing the concurrent join. Databases man
					// not handle concurrent queries well.
					PooledConnection additional = connection.getPool().borrowWriteConnection();
					try (OrderGenerator generator = new OrderGenerator(util, select, factor, additional)) {
						while (cursor.next()) {
							long branch = hasBranches ? cursor.getLong(1) : TLContext.TRUNK_ID;
							long id = cursor.getLong(idIndex);
							long revMax = cursor.getLong(revIndex);
							long scopeId = cursor.getLong(scopeIndex);

							if (!generator.next(branch, scopeId)) {
								log.info("Inconsistent line count, missing order for branch=" + branch + ", id=" + id
									+ ", revMax=" + revMax, Log.WARN);
								break;
							}

							cursor.updateInt(orderIndex, generator.getSortOrder(branch, id, revMax));
							cursor.updateRow();

							cnt++;
						}
					} finally {
						connection.getPool().releaseWriteConnection(additional);
					}
				}

				log.info("Synthesized " + cnt + " order values in table '" + sourceTable.getName() + "'.");
			}
		} catch (SQLException ex) {
			log.error("Failed to synthesize order of table '" + config.getLinkTable() + "': " + ex.getMessage(), ex);
		}
	}

	static class OrderGenerator implements AutoCloseable {
		private ResultSet _result;

		private int _scopeIndex;

		private int _orderIndex;

		private boolean _hasBranches;

		private boolean _continuation;

		private List<Value> _values;

		private Map<Value, Value> _valueById;

		private int _factor;

		private int _idIndex;

		private int _revIndex;

		/**
		 * Creates a {@link OrderGenerator}.
		 */
		public OrderGenerator(Util util, CompiledStatement select, int factor, PooledConnection connection)
				throws SQLException {
			_factor = factor;

			_hasBranches = util.hasBranches();
			_idIndex = util.getBranchIndexInc() + 1;
			_revIndex = util.getBranchIndexInc() + 2;
			_scopeIndex = util.getBranchIndexInc() + 3;
			_orderIndex = util.getBranchIndexInc() + 4;

			_result = select.executeQuery(connection);
			_valueById = new HashMap<>();
			_values = new ArrayList<>();
		}

		public int getSortOrder(long branch, long id, long revMax) {
			Value value = _valueById.get(new Value(branch, id, revMax));
			if (value == null) {
				throw new IllegalArgumentException(
					"No information available for link with branch=" + branch + ", id=" + id + ", revMax=" + revMax);
			}
			return value.getIndex() * _factor;
		}

		public boolean next(long branch, long scopeId) throws SQLException {
			if (!_values.isEmpty() && _values.get(0).sameScope(branch, scopeId)) {
				// Same batch.
				return true;
			}
			_values.clear();
			_valueById.clear();
			boolean ok = readValues();
			if (ok) {
				_values.sort(Value::compareOrder);
				for (int n = 0, cnt = _values.size(); n < cnt; n++) {
					_values.get(n).initIndex(n);
				}
			}
			return ok;
		}

		private boolean readValues() throws SQLException {
			Value lastValue = null;
			while (_continuation || _result.next()) {
				long branch = _hasBranches ? _result.getLong(1) : TLContext.TRUNK_ID;
				long id = _result.getLong(_idIndex);
				long revMax = _result.getLong(_revIndex);
				long scope = _result.getLong(_scopeIndex);
				Object order = _result.getObject(_orderIndex);

				Value nextValue = new Value(branch, id, revMax, scope, order);

				if (lastValue == null || lastValue.sameScope(nextValue)) {
					_values.add(nextValue);
					_valueById.put(nextValue, nextValue);
					_continuation = false;
					lastValue = nextValue;
				} else {
					_continuation = true;
					return true;
				}
			}
			_continuation = false;
			return !_values.isEmpty();
		}

		@Override
		public void close() throws SQLException {
			_result.close();
		}

		private static class Value {
			private long _branch;

			private long _scope;

			private Object _order;

			private int _index;

			private long _id;

			private long _revMax;

			public Value(long branch, long id, long revMax, long scope, Object order) {
				this(branch, id, revMax);
				_scope = scope;
				_order = order;
			}

			public Value(long branch, long id, long revMax) {
				_branch = branch;
				_id = id;
				_revMax = revMax;
			}

			public void initIndex(int n) {
				_index = n;
			}

			public int getIndex() {
				return _index;
			}

			public boolean sameScope(Value other) {
				long branch = other._branch;
				long scope = other._scope;
				return sameScope(branch, scope);
			}

			public boolean sameScope(long branch, long scope) {
				return _branch == branch && _scope == scope;
			}

			public int compareOrder(Value other) {
				return ComparableComparator.INSTANCE.compare(_order, other._order);
			}

			@Override
			public int hashCode() {
				return Objects.hash(_branch, _id, _revMax);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Value other = (Value) obj;
				return _branch == other._branch && _id == other._id && _revMax == other._revMax;
			}
		}
	}
}
