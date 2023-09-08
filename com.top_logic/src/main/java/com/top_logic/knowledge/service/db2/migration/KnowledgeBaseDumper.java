/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.db2.migration.db.DefaultRowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.VersionDescriptor;

/**
 * Helper class to dump a {@link KnowledgeBase} to a given {@link OutputStream}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KnowledgeBaseDumper {

	private Log _log = new LogProtocol(KnowledgeBaseDumper.class);

	private int _flushSequence = 1000;

	private final KnowledgeBase _kb;

	private static final Set<String> NO_DUMP_TYPES = CollectionFactory.set(
		BasicTypes.BRANCH_TYPE_NAME,
		BranchSupport.BRANCH_SWITCH_TYPE_NAME,
		BasicTypes.REVISION_TYPE_NAME,
		RevisionXref.REVISION_XREF_TYPE_NAME,
		AbstractFlexDataManager.FLEX_DATA
	);

	private Set<String> _ignoreTypes = Collections.emptySet();

	/**
	 * Creates a new {@link KnowledgeBaseDumper}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to dump.
	 */
	public KnowledgeBaseDumper(KnowledgeBase kb) {
		_kb = kb;
	}

	/**
	 * After how many changes the stream must be flushed?
	 */
	public int getFlushSequence() {
		return _flushSequence;
	}

	/**
	 * Setter for {@link #getFlushSequence()}.
	 * 
	 * @param flushSequence
	 *        Value for {@link #getFlushSequence()}.
	 */
	public void setFlushSequence(int flushSequence) {
		_flushSequence = flushSequence;
	}

	/**
	 * Log to write messages to.
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * Setter for {@link #getLog()}.
	 * 
	 * @param log
	 *        Value of {@link #getLog()}.
	 */
	public void setLog(Log log) {
		_log = log;
	}

	/**
	 * Types that are not dumped.
	 */
	public Set<String> getIgnoreTypes() {
		return _ignoreTypes;
	}

	/**
	 * Setter for {@link #getIgnoreTypes()}.
	 *
	 * @param ignoreTypes
	 *        Value of {@link #getIgnoreTypes()}.
	 */
	public void setIgnoreTypes(Set<String> ignoreTypes) {
		_ignoreTypes = ignoreTypes;
	}

	/**
	 * Dumps the data of the {@link KnowledgeBase} to the given {@link OutputStream}.
	 * 
	 * @param streamOut
	 *        The {@link OutputStream} to write to.
	 */
	public void dump(OutputStream streamOut) throws IOException {
		OutputStreamWriter streamWriter = new OutputStreamWriter(streamOut, StringServices.CHARSET_UTF_8);
		TagWriter tagout = new TagWriter(streamWriter);
		VersionDescriptor version;
		try {
			version = MigrationUtil.loadDataVersionDescriptor();
		} catch (ConfigurationException | SQLException ex) {
			version = null;
			Logger.error("Cannot load application version.", ex);
		}
		DumpWriter out = new DumpWriter(tagout, version);
		dump(out);
	}

	/**
	 * Dumps the data of the {@link KnowledgeBase} to the given {@link IDumpWriter}.
	 * 
	 * @param out
	 *        The {@link IDumpWriter} to write to.
	 */
	public void dump(IDumpWriter out) throws IOException {
		if (out instanceof DumpWriter) {
			((DumpWriter) out).startDocument();
		}

		dumpChangeSets(out);
		dumpOtherTypes(out);

		if (out instanceof DumpWriter) {
			((DumpWriter) out).endDocument();
			((DumpWriter) out).flush();
		}
	}

	private void dumpChangeSets(IDumpWriter out) throws IOException {
		getLog().info("Start processing change sets.");
		out.beginChangeSets();

		ReaderConfigBuilder readerConfig =
			ReaderConfigBuilder.createConfig(_kb.getHistoryManager().getRevision(1L), Revision.CURRENT);
		Set<String> ignoreTypes = getIgnoreTypes();
		if (!ignoreTypes.isEmpty()) {
			Set<String> allMetaObjects = new HashSet<>(_kb.getMORepository().getMetaObjectNames());
			allMetaObjects.removeAll(ignoreTypes);
			readerConfig.setTypeNames(allMetaObjects);
		}
		try (ChangeSetReader reader = _kb.getChangeSetReader(readerConfig)) {
			ChangeSet cs;
			long csNr = 0;
			while ((cs = reader.read()) != null) {
				out.writeChangeSet(cs);
				csNr++;
				if (csNr > getFlushSequence()) {
					getLog().info("Flush dump after processing changeset " + cs.getRevision());
					csNr = 0;
				}
			}
		}

		out.endChangeSets();
	}

	private void dumpOtherTypes(IDumpWriter out) throws IOException {
		out.beginTypes();
		try {
			dumpUnversionedTypes(out);
		} catch (Exception ex) {
			getLog().error("Unable to dump unversioned KB types", ex);
		}
		out.endTypes();

		out.beginTables();
		try {
			dumpNonItemTypes(out);
		} catch (Exception ex) {
			getLog().error("Unable to dump non KB types", ex);
		}
		out.endTables();
	}

	private void dumpUnversionedTypes(IDumpWriter out) {
		MetaObject itemType = itemType();
		for (MetaObject metaObject : typeSystem().getMetaObjects()) {
			String moName = metaObject.getName();
			if (getIgnoreTypes().contains(moName)) {
				continue;
			}
			if (!metaObject.isSubtypeOf(itemType)) {
				// Only Item types can be unversioned KI types.
				continue;
			}
			MOClass clazz = (MOClass) metaObject;
			if (clazz.isVersioned()) {
				continue;
			}
			try {
				dumpUnversionedType(out, clazz);
			} catch (Exception ex) {
				_log.error("Unable to dump unversioned type " + moName, ex);
			}
		}

	}

	private MetaObject itemType() throws UnreachableAssertion {
		try {
			return typeSystem().getMetaObject(BasicTypes.ITEM_TYPE_NAME);
		} catch (UnknownTypeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	private void dumpUnversionedType(IDumpWriter out, final MOClass type) throws IOException {
		getLog().info("Dump values for unversioned type " + type.getName() + ".");

		RevisionQuery<KnowledgeItem> query = queryUnresolved(allOf(type), KnowledgeItem.class);
		try (CloseableIterator<KnowledgeItem> search = _kb.searchStream(query)) {
			try (UnversionedTypeValues<KnowledgeItem> items = new UnversionedTypeValues<>(search)) {
				out.writeUnversionedType(type, items);
			}
		}
	}

	private void dumpNonItemTypes(IDumpWriter out) throws SQLException {
		ConnectionPool connectionPool = KBUtils.getConnectionPool(_kb);
		PooledConnection connection = connectionPool.borrowReadConnection();
		try {
			dumpNonItemKBTypes(out, connection);
			dumpTables(out, connection);
		} finally {
			connectionPool.releaseReadConnection(connection);
		}
	}

	/**
	 * Returns a {@link DBSchema} containing {@link DBTable} for all relevant tables in the given
	 * {@link PooledConnection} that have no {@link MetaObject} representation in the given
	 * {@link #typeSystem()}.
	 * 
	 * @param connection
	 *        The {@link PooledConnection} to access the database.
	 * @param typeSystem
	 *        The {@link MORepository type system} of the {@link KnowledgeBase}.
	 * 
	 * @throws SQLException
	 *         When accessing the database fails.
	 */
	protected DBSchema getNonKBTables(PooledConnection connection, MORepository typeSystem) throws SQLException {
		return _kb.getSchemaSetup().getConfig().getAdditionalTables();
	}

	private MORepository typeSystem() {
		return _kb.getMORepository();
	}

	private void dumpNonItemKBTypes(IDumpWriter out, PooledConnection connection) throws UnreachableAssertion {
		MetaObject itemType = itemType();
		for (MetaObject metaObject : typeSystem().getMetaObjects()) {
			if (metaObject.isSubtypeOf(itemType)) {
				// Item types are already processed.
				continue;
			}
			if (!(metaObject instanceof MOStructure)) {
				continue;
			}
			String moName = metaObject.getName();
			if (NO_DUMP_TYPES.contains(moName)) {
				continue;
			}
			if (getIgnoreTypes().contains(moName)) {
				continue;
			}
			try {
				dumpTableType(out, connection, (MOStructure) metaObject);
			} catch (Exception ex) {
				_log.error("Unable to dump non item KB type " + moName, ex);
			}
		}
	}

	private void dumpTables(IDumpWriter out, PooledConnection connection) throws SQLException {
		for (DBTable table : getNonKBTables(connection, typeSystem()).getTables()) {
			String tableName = table.getName();
			if (getIgnoreTypes().contains(tableName)) {
				continue;
			}
			MOStructure tableType = SchemaSetup.createTableType(table, DefaultMOFactory.INSTANCE);
			tableType.freeze();

			try {
				dumpTableType(out, connection, tableType);
			} catch (Exception ex) {
				_log.error("Unable to dump table type " + tableName, ex);
			}
		}
	}

	private void dumpTableType(IDumpWriter out, PooledConnection readConnection, final MOStructure tableType)
			throws SQLException, IOException {
		String tableName = tableType.getDBMapping().getDBName();
		getLog().info("Dump values for table " + tableName + ".");
		DBHelper sqlDialect = readConnection.getSQLDialect();
		StringBuilder fromColumns = new StringBuilder();
		boolean first = true;
		for (DBAttribute column : tableType.getAttributes().stream()
			.flatMap(attribute -> Arrays.stream(attribute.getDbMapping()))
			.toArray(size -> new DBAttribute[size])) {
			if (first) {
				first = false;
			} else {
				fromColumns.append(", ");
			}

			fromColumns.append(sqlDialect.columnRef(column.getDBName()));
		}

		// Copy data.
		String selectDataSql = "SELECT " + fromColumns + " FROM " + sqlDialect.tableRef(tableName);
		try (PreparedStatement readData = readConnection.prepareStatement(selectDataSql, ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet data = readData.executeQuery()) {
				out.writeTable(tableName, new Iterable<RowValue>() {

					@Override
					public Iterator<RowValue> iterator() {
						return new RowData(KnowledgeBaseDumper.this.getLog(), sqlDialect, tableType, data);
					}
				});
			}
		}
	}

	private static final class RowData extends AbstractIterator<RowValue> {

		private final DBHelper _sqlDialect;

		private final ResultSet _data;

		private final List<String> _columnNames;

		private final List<DBType> _types;

		private final Log _log;

		private final MOStructure _table;

		public RowData(Log log, DBHelper sqlDialect, MOStructure table, ResultSet data) {
			_log = log;
			_sqlDialect = sqlDialect;
			_table = table;
			_data = data;
			_columnNames = new ArrayList<>();
			_types = new ArrayList<>();
			for (MOAttribute attr : table.getAttributes()) {
				for (DBAttribute column : attr.getDbMapping()) {
					_columnNames.add(column.getDBName());
					_types.add(column.getSQLType());
				}
			}
		}

		@Override
		protected RowValue computeNext() {
			boolean next;
			try {
				next = _data.next();
			} catch (SQLException ex) {
				_log.error("Unable to get next data.", ex);
				return null;
			}
			if (!next) {
				return null;
			}
			return new DefaultRowValue(_table, fetchValueByColumnName());
		}

		private Map<String, Object> fetchValueByColumnName() {
			Map<String, Object> values = new HashMap<>();
			// Transfer data.
			for (int n = 0, cnt = _types.size(); n < cnt; n++) {
				DBType sqlType = _types.get(n);
				String columnName = _columnNames.get(n);
				int index = 1 + n;

				try {
					Object value = _sqlDialect.mapToJava(_data, index, sqlType);
					values.put(columnName, value);
				} catch (SQLException ex) {
					_log.error("Unable to read column " + columnName + ".", ex);
				}
			}
			return values;
		}

	}

}
