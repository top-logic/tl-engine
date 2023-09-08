/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogAdaptor;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.attr.storage.ComputedAttributeStorage;
import com.top_logic.dob.attr.storage.InitialAttributeStorage;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.ByIDReferenceStorageImpl;
import com.top_logic.knowledge.ByValueReferenceStorageImpl;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.InternalBranch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.db2.SequenceTypeProvider;
import com.top_logic.knowledge.service.db2.migration.IDumpWriter;
import com.top_logic.knowledge.service.db2.migration.KnowledgeBaseDumper;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.TLContext;

/**
 * {@link EventWriter} directly producing a textual SQL dump file from a series of
 * {@link KnowledgeEvent}s without driving a {@link KnowledgeBase} with the events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLDumper implements EventWriter {

	/** Constant to use when the SQL dialect dependent insert chunk size must be used. */
	public static final int DEFAULT_INSERT_CHUNK_SIZE = -1;

	/**
	 * Default for the maximum numbers of elements to hold in cache before the insert statements are
	 * created.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 1000000;

	final Log _log;

	/**
	 * Cache of all current objects by their IDs.
	 */
	private final Map<ObjectBranchId, Item> _cache = new HashMap<>();

	/**
	 * All object versions produced in the current transaction.
	 */
	private final Map<ObjectBranchId, Item> _tx = new HashMap<>();

	private final MORepository _typeRepository;

	private final InsertBuilder _insertBuilder;

	private final MetaObject _revType;

	private final MetaObject _xrefType;

	private final MetaObject _branchType;

	private final MetaObject _branchSwitchType;

	private EventWriter _unversionedWriter = new EventWriter() {

		@Override
		public void write(ChangeSet cs) {
			for (ObjectCreation creation : cs.getCreations()) {
				processUnversionedCreation(creation);
			}
		}

		/**
		 * Writes the creation of an unversioned object.
		 */
		private void processUnversionedCreation(ObjectCreation creation) {
			ObjectBranchId id = creation.getObjectId();
			if (!checkType(creation)) {
				return;
			}

			Long createRev = (Long) creation.getValues().get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME);
			Long revMin = (Long) creation.getValues().get(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
			Item obj = new Obj(id, createRev, revMin, Revision.CURRENT_REV);
			obj.init(createRev, creation.getValues());

			Item clash = enterCurrent(obj);
			if (clash != null) {
				_log.error("Object creation overrides existing object: " + creation);
			}
		}

		@Override
		public void flush() {
			SQLDumper.this.flush();
		}

		@Override
		public void close() {
			// Ignore.
		}
	};

	private RowWriter _rowWriter = new RowWriter() {
		
		@Override
		public void write(RowValue row) {
			Row rowObject = new Row(row.getTable(), _log);
			rowObject.init(-1, row.getValues());
			enterCurrent(rowObject);
		}

	};

	/**
	 * Creates a {@link SQLDumper}.
	 * 
	 * @param log
	 *        The error {@link Log}.
	 * @param pool
	 *        Database that is dumped.
	 * @param typeRepository
	 *        The {@link MORepository} to resolve types from.
	 * @param bufferSize
	 *        Maximal number of items to hold in cache before the insert SQL is created.
	 * @param insertWriter
	 *        The output to write insertions to.
	 */
	public SQLDumper(Log log, ConnectionPool pool, MORepository typeRepository, int bufferSize, InsertWriter insertWriter) throws UnknownTypeException {
		Log flog = new LogAdaptor() {
			@Override
			public void error(String message) {
				super.error(enhance(message));
			}

			@Override
			public void error(String message, Throwable ex) {
				super.error(enhance(message), ex);
			}

			private String enhance(String message) {
				return message;
			}

			@Override
			protected Log impl() {
				return log;
			}
		};
		_log = flog;
		_typeRepository = typeRepository;
		_insertBuilder = new InsertBuilder(flog, pool, typeRepository, bufferSize, insertWriter);
		_revType = typeRepository.getMetaObject(BasicTypes.REVISION_TYPE_NAME);
		_xrefType = typeRepository.getMetaObject(RevisionXref.REVISION_XREF_TYPE_NAME);
		_branchType = typeRepository.getMetaObject(BasicTypes.BRANCH_TYPE_NAME);
		_branchSwitchType = typeRepository.getMetaObject(BranchSupport.BRANCH_SWITCH_TYPE_NAME);
	}

	/**
	 * Loads the base-line data from the given {@link KnowledgeBase}.
	 */
	public void loadCurrent(KnowledgeBase kb, Set<String> ignoreTables) {
		KnowledgeBaseDumper kbDumper = new KnowledgeBaseDumper(kb);
		kbDumper.setIgnoreTypes(ignoreTables);
		try {
			kbDumper.dump(new IDumpWriter() {

				@Override
				public void writeUnversionedType(MetaObject type, Iterator<? extends KnowledgeItem> items) {
					// Unversioned objects already contained in change sets.
				}

				@Override
				public void writeTable(String tableName, Iterable<RowValue> rows) {
					RowWriter rowWriter = getRowWriter();
					for (RowValue row : rows) {
						rowWriter.write(row);
					}
				}

				@Override
				public void writeChangeSet(ChangeSet cs) {
					SQLDumper.this.write(cs);
				}

			});
		} catch (IOException ex) {
			throw new UnreachableAssertion("Actually no IOException is thrown.", ex);
		}

		_insertBuilder.notifyCurrentValuesLoaded();
	}

	@Override
	public void write(ChangeSet cs) {
		Set<Item> touchedTypes = new HashSet<>();
		long revision = cs.getRevision();
		for (ItemDeletion deletion : cs.getDeletions()) {
			processDeletion(cs, deletion);
			touchedTypes.add(xref(revision, deletion));
		}
		for (ObjectCreation creation : cs.getCreations()) {
			processCreation(cs, creation);
			touchedTypes.add(xref(revision, creation));
		}
		for (ItemUpdate update : cs.getUpdates()) {
			processUpdate(cs, update);
			touchedTypes.add(xref(revision, update));
		}
		for (BranchEvent branchEvt : cs.getBranchEvents()) {
			processBranch(branchEvt);
		}
		enterVersions(touchedTypes);
		enterRev(cs.getCommit());
		try {
			flushTx();
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Item xref(long rev, ItemChange change) {
		return xref(rev, change.getObjectId());
	}

	private Item xref(long rev, ObjectBranchId changedId) {
		long branchId = changedId.getBranchId();
		String typeName = changedId.getObjectType().getName();

		Map<String, Object> values = new HashMap<>();
		values.put(RevisionXref.XREF_REV_ATTRIBUTE, rev);
		values.put(RevisionXref.XREF_BRANCH_ATTRIBUTE, branchId);
		values.put(RevisionXref.XREF_TYPE_ATTRIBUTE, typeName);

		ObjectBranchId id = new ObjectBranchId(branchId, _xrefType, StringID.valueOf(typeName));

		Sys item = new Sys(id, rev, rev);
		item.init(rev, values);
		return item;
	}

	private void enterVersions(Set<Item> versions) {
		for (Item version : versions) {
			enterVersion(version);
		}
	}

	private void enterRev(CommitEvent commit) {
		Item revItem =
			new Rev(_revType, commit.getRevision(), commit.getDate(), commit.getAuthor(), commit.getLog());
		enterVersion(revItem);
	}

	private void processCreation(ChangeSet cs, ObjectCreation creation) {
		ObjectBranchId id = creation.getObjectId();
		if (!checkType(creation)) {
			return;
		}

		long createRev = creation.getRevision();
		Item obj = new Obj(id, createRev);
		obj.init(createRev, creation.getValues());

		Item clash = enterCurrent(obj);
		if (clash != null) {
			_log.error("Object creation overrides existing object in revision " + cs.getRevision() + ": " + creation);
		}
	}

	private void processBranch(BranchEvent branchEvent) {
		Item branchItem = new Brc(_branchType, branchEvent.getBranchId(), branchEvent.getBaseBranchId(),
			branchEvent.getRevision(), branchEvent.getBaseRevisionNumber());
		enterVersion(branchItem);

		branchSwitch(branchEvent);

		// TODO #22141: Add inserts for the branched objects.

		_insertBuilder.updateMaxBranch(branchEvent.getBranchId());

	}

	private void branchSwitch(BranchEvent branchEvent) {
		Set<String> branchedTypes = branchEvent.getBranchedTypeNames();
		long branchId = branchEvent.getBranchId();
		long rev = branchEvent.getRevision();
		long dataBranchId = branchEvent.getBaseBranchId();
		if (dataBranchId == 0) {
			/* No base branch means creation of trunk. In that case all data live in trunk. */
			dataBranchId = TLContext.TRUNK_ID;
		}
		for (String typeName : branchedTypes) {
			ObjectBranchId id = new ObjectBranchId(branchId, _branchSwitchType, StringID.valueOf(typeName));

			Sys item = new Sys(id, rev, rev);
			Map<String, Object> values = new HashMap<>();
			values.put(BranchSupport.LINK_BRANCH_ATTRIBUTE_NAME, branchId);
			values.put(BranchSupport.LINK_TYPE_ATTRIBUTE, typeName);
			values.put(BranchSupport.LINK_DATA_BRANCH_ATTRIBUTE, dataBranchId);

			item.init(rev, values);
			enterVersion(item);
		}

	}

	public RowWriter getRowWriter() {
		return _rowWriter;
	}

	public EventWriter getUnversionedWriter() {
		return _unversionedWriter;
	}

	private void processUpdate(ChangeSet cs, ItemUpdate update) {
		if (!checkType(update)) {
			return;
		}
		Item obj = lookupCurrent(update.getObjectId());
		if (obj == null) {
			_log.error("Updated object does not exist in revision " + cs.getRevision() + ": " + update);
			return;
		}

		enterVersion(obj.update(update.getRevision(), update.getValues()));
	}

	private void processDeletion(ChangeSet cs, ItemDeletion deletion) {
		if (!checkType(deletion)) {
			return;
		}
		Item old = deleteCurrent(deletion.getObjectId());
		if (old == null) {
			_log.error("Object deleted does not exist in revision " + cs.getRevision() + ": " + deletion);
			return;
		}

		enterVersion(old.delete(deletion.getRevision()));
	}

	final boolean checkType(ItemEvent event) {
		MetaObject type = event.getObjectType();
		String typeName = type.getName();
		MetaObject targetType;
		try {
			targetType = lookupType(typeName);
		} catch (UnknownTypeException ex) {
			_log.error("Object type '" + typeName + "' not found in target type repository: " + event);
			return false;
		}
		if (targetType != type) {
			_log.error("Object type does not match the target type system, got '', ");
			return false;
		}
		return true;
	}

	private MetaObject lookupType(String typeName) throws UnknownTypeException {
		return _typeRepository.getMetaObject(typeName);
	}

	final Item enterCurrent(Item current) {
		return _cache.put(current.getId(), current);
	}

	private Item lookupCurrent(ObjectBranchId id) {
		return _cache.get(id);
	}

	private Item deleteCurrent(ObjectBranchId id) {
		return _cache.remove(id);
	}

	private void enterVersion(Item obj) {
		if (obj == null) {
			return;
		}
		Item clash = _tx.put(obj.getId(), obj);
		if (clash != null) {
			_log.error("Object modified twice in the same revision: " + clash + ", " + obj);

			// Revert.
			_tx.put(clash.getId(), clash);
		}
		_insertBuilder.updateMaxRevision(obj.getCreateRev());
	}

	@Override
	public void flush() {
		try {
			_insertBuilder.flushBuffer();
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void flushTx() throws IOException, NoSuchAttributeException {
		for (Item obj : _tx.values()) {
			insert(obj);
		}
		_tx.clear();
	}

	private void insert(Item obj) throws IOException, NoSuchAttributeException {
		_insertBuilder.insert(obj);
	}

	@Override
	public void close() {
		try {
			for (Item obj : _cache.values()) {
				insert(obj);
			}
			_cache.clear();
			_insertBuilder.close();
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates an {@link InsertWriter} for the given {@link DBHelper}.
	 * 
	 * @param out
	 *        The {@link Writer} to write the SQL to.
	 */
	public static InsertWriter createInsertWriter(DBHelper sqlDialect, int insertChunkSize, Writer out) {
		if (sqlDialect instanceof OracleHelper) {
			if (insertChunkSize < 1) {
				// The Oracle multi-insert statement is extremely verbose. There is some
				// incomprehensible limit "The sum of all the INTO columns cannot exceed 999", see
				// https://oracle-base.com/articles/9i/multitable-inserts.
				insertChunkSize = 500;
			}
			return new OracleInsertWriter(sqlDialect, out, insertChunkSize);
		} else if (sqlDialect instanceof MySQLHelper) {
			if (insertChunkSize < 1) {
				insertChunkSize = 10000;
			}
			return new MySQLInsertWriter(sqlDialect, out, insertChunkSize);
		} else if (sqlDialect instanceof H2Helper) {
			if (insertChunkSize < 1) {
				insertChunkSize = 10000;
			}
			return new H2InsertWriter(sqlDialect, out, insertChunkSize);
		} else {
			if (insertChunkSize < 1) {
				insertChunkSize = 10000;
			}
			return new DefaultInsertWriter(sqlDialect, out, insertChunkSize);
		}
	}

	private static class InsertBuilder implements Closeable {

		private static class TypedList extends ArrayList<Item> {

			final MOStructure _type;

			public TypedList(MOStructure type) {
				_type = type;
			}

		}

		private static final Set<String> SKIP_SEQUENCES = new HashSet<>(Arrays.asList(
			DBKnowledgeBase.ID_SEQ,
			DBKnowledgeBase.BRANCH_SEQUENCE,
			DBKnowledgeBase.REVISION_SEQUENCE));

		private static final long MAX_DATA_SIZE = 10 * 1024 * 1024;
	
		private final int _bufferSize;

		/**
		 * The approximate amount of data bytes currently buffered.
		 */
		private long _dataSize;

		final Log _log;

		MOStructure _flexValueType;

		private final Map<String, TypedList> _bufferByType;

		final List<Val> _flexValues;

		private long _lastCommitNumber = -1;

		private long _currentBranch = -1;

		private long _maxID = -1;

		private MOStructure _sequenceType;

		private InsertWriter _insertWriter;

		/**
		 * Cache holding the {@link Item} of the {@link BasicTypes#REVISION_TYPE_NAME}.
		 *
		 * <p>
		 * When this variable is not <code>null</code> it holds the {@link Rev} waiting for
		 * flushing.
		 * </p>
		 * 
		 * <p>
		 * This variable serves as marker, that the {@link Rev#getDate()} must be adapted in the
		 * next revision to ensure an increasing sequence of dates.
		 * </p>
		 */
		private List<Item> _revisionValues;

		private ConnectionPool _pool;

		public InsertBuilder(Log log, ConnectionPool pool, MORepository types, int bufferSize,
				InsertWriter insertWriter) {
			_log = log;
			_pool = pool;
			_insertWriter = insertWriter;
			
			_bufferSize = bufferSize;
			_flexValues = new ArrayList<>();
			_bufferByType = new HashMap<>();
			for (MetaObject mo : types.getMetaObjects()) {
				{
					if (mo instanceof MOStructure) {
						MOStructure type = (MOStructure) mo;
						_bufferByType.put(type.getName(), new TypedList(type));
					}
				}
			}

			try {
				_flexValueType = (MOStructure) types.getMetaObject(AbstractFlexDataManager.FLEX_DATA);
			} catch (UnknownTypeException ex) {
				_log.error("No flex data type.", ex);
			}
			try {
				_sequenceType = (MOStructure) types.getMetaObject(SequenceTypeProvider.SEQUENCE_TYPE_NAME);
				String[] expectedSequenceAttributes = new String[] { "id", "value" };
				if (!Arrays.equals(expectedSequenceAttributes, _sequenceType.getAttributeNames())) {
					_log.error("The code in this class requires the sequence type attributes "
						+ Arrays.toString(expectedSequenceAttributes) + " in given order. Attributes: "
						+ Arrays.toString(_sequenceType.getAttributeNames()));
				}
			} catch (UnknownTypeException ex) {
				_log.error("No sequence type.", ex);
			}
		}

		void notifyCurrentValuesLoaded() {
			for (Entry<String, TypedList> entry : _bufferByType.entrySet()) {
				if (entry.getKey().equals(BasicTypes.REVISION_TYPE_NAME)) {
					_revisionValues = entry.getValue();
					break;
				}
			}
		}

		public void updateMaxBranch(long branchId) {
			_currentBranch = Math.max(_currentBranch, branchId);
		}

		public void updateMaxRevision(long commitNumber) {
			_lastCommitNumber = Math.max(_lastCommitNumber, commitNumber);
		}

		public void insert(Item version) throws IOException, NoSuchAttributeException {
			updateMaxId(version);
			TypedList buffer = _bufferByType.get(version.getType().getName());
			if (buffer == _revisionValues) {
				updateFormerRevisionDates((Rev) version);
				_revisionValues = null;
			}
			
			buffer.add(version);
			_flexValues.addAll(version.flexValues().values());

			_dataSize += version.aproximateSize();

			if (buffer.size() > _bufferSize || _flexValues.size() > _bufferSize || _dataSize > MAX_DATA_SIZE) {
				flushBuffer();
			}
		}

		/**
		 * Updates {@link Rev#getDate()} of all {@link Item} in {@link #_revisionValues}, such that
		 * the dates are in increasing order.
		 */
		private void updateFormerRevisionDates(Rev baseRevision) {
			long baseDate = baseRevision.getDate();
			_revisionValues.sort(null);
			int numberRevs = _revisionValues.size();
			if (numberRevs == 0) {
				// nothing to adapt
				return;
			}
			long lastDate = ((Rev) _revisionValues.get(numberRevs - 1)).getDate();
			if (lastDate < baseDate) {
				// already in correct order.
				return;
			}
			long diff = (lastDate - baseDate) + 1;
			for (int i = numberRevs - 1; i >= 0; i--) {
				Rev rev = (Rev) _revisionValues.get(i);
				rev.updateDate(rev.getDate() - diff);
			}
		}

		private void updateMaxId(Item version) {
			if (version instanceof Row) {
				// A row has a generated id which is independent of the KnowledgeBase identifier.
				return;
			}
			TLID objectName = version.tId().getObjectName();
			if (objectName instanceof LongID) {
				long longValue = ((LongID) objectName).longValue();
				_maxID = Math.max(_maxID, longValue);
			}
		}

		public void flushBuffer() throws IOException, NoSuchAttributeException {
			for (TypedList entry : _bufferByType.values()) {
				flush(entry);
			}
			flushValues();

			_dataSize = 0;
		}

		private void flushSequences() throws IOException {
			// Not the next ID itself, but the 1000^th chunk for the ID is stored in the table.
			long idChunkStart = (_maxID / 1000) + 1;
			_insertWriter.appendInsert(table(_sequenceType), Arrays.asList(
				new Object[] { DBKnowledgeBase.ID_SEQ, idChunkStart },
				new Object[] { DBKnowledgeBase.BRANCH_SEQUENCE, _currentBranch },
				new Object[] { DBKnowledgeBase.REVISION_SEQUENCE, _lastCommitNumber }));
		}

		private void flush(TypedList buffer) throws IOException {
			flush(buffer._type, buffer);
		}

		private void flush(MOStructure type, List<Item> buffer) throws IOException {
			if (buffer.isEmpty()) {
				return;
			}
			DBTable table = table(type);
			List<MOAttribute> attributes = type.getAttributes();
			List<DBColumn> columns = table.getColumns();
			int columnCount = columns.size();

			int attributeCount = attributes.size();

			AttributeStorage[] storages = new AttributeStorage[attributeCount];
			for (int n = 0; n < attributeCount; n++) {
				AttributeStorage storage = attributes.get(n).getStorage();
				if (storage instanceof ByValueReferenceStorageImpl) {
					// No by-value storage supported, works only on keys and cache values.
					storage = ByIDReferenceStorageImpl.INSTANCE;
				}
				storages[n] = storage;
			}

			Filter<? super Object[]> excludeValue;
			if (type == _sequenceType) {
				// First column is the the "id" column.
				excludeValue = sequenceValues -> SKIP_SEQUENCES.contains(sequenceValues[0]);
			} else {
				excludeValue = FilterFactory.falseFilter();
			}

			int revMinIndex = index(type, BasicTypes.REV_MIN_ATTRIBUTE_NAME);
			int revMaxIndex = index(type, BasicTypes.REV_MAX_ATTRIBUTE_NAME);
			_insertWriter.appendInsert(table, buffer.stream().map(obj -> {
			
					Object[] cacheValues = obj.rowValues();
					if (excludeValue.accept(cacheValues)) {
						return null;
					}
			
					if (revMinIndex >= 0) {
						cacheValues[revMinIndex] = obj.getStartRev();
					}
					if (revMaxIndex >= 0) {
						cacheValues[revMaxIndex] = obj.getStopRev();
					}
			
					Object[] columnValues = new Object[columnCount];
					for (int n = 0; n < attributeCount; n++) {
						MOAttribute attr = attributes.get(n);
			
						DBAttribute[] dbMapping = attr.getDbMapping();
						if (dbMapping.length > 0) {
							AttributeStorage storage = storages[n];
							try {
							storage.storeValue(_pool, columnValues, 0, attr, obj, cacheValues, obj.getCommitRev());
							} catch (SQLException ex) {
								StringBuilder errorSerializing = new StringBuilder();
								errorSerializing.append("Error serializing value of attribute '");
								errorSerializing.append(attr.getName());
								errorSerializing.append("' in '");
								errorSerializing.append(obj);
								errorSerializing.append("': Values: ");
								errorSerializing.append(Arrays.toString(cacheValues));
								_log.error(errorSerializing.toString(), ex);
								return null;
							}
						}
					}
					return columnValues;
			}).filter(Objects::nonNull).collect(Collectors.toList()));
	
			buffer.clear();
		}

		int index(MOStructure type, String attributeName) {
			MOAttribute attr = type.getAttributeOrNull(attributeName);
			if (attr == null) {
				return -1;
			}
			return attr.getCacheIndex();
		}

		private void flushValues() throws IOException, NoSuchAttributeException {
			if (_flexValues.isEmpty()) {
				return;
			}

			DBTable table = table(_flexValueType);
			List<MOAttribute> attributes = _flexValueType.getAttributes();
			List<DBColumn> columns = table.getColumns();
			int columnCount = columns.size();

			int attributeCount = attributes.size();
			Object[] cacheValues = new Object[attributeCount];

			AttributeStorage[] storages = new AttributeStorage[attributeCount];
			for (int n = 0; n < attributeCount; n++) {
				AttributeStorage storage = attributes.get(n).getStorage();
				if (storage instanceof ByValueReferenceStorageImpl) {
					// No by-value storage supported, works only on keys and cache values.
					storage = ByIDReferenceStorageImpl.INSTANCE;
				}
				storages[n] = storage;
			}

			int branchIndex = _flexValueType.getAttribute(AbstractFlexDataManager.BRANCH).getCacheIndex();
			int idIndex = _flexValueType.getAttribute(AbstractFlexDataManager.IDENTIFIER).getCacheIndex();
			int revMinIndex = _flexValueType.getAttribute(AbstractFlexDataManager.REV_MIN).getCacheIndex();
			int revMaxIndex = _flexValueType.getAttribute(AbstractFlexDataManager.REV_MAX).getCacheIndex();
			int typeIndex = _flexValueType.getAttribute(AbstractFlexDataManager.TYPE).getCacheIndex();
			int attrIndex = _flexValueType.getAttribute(AbstractFlexDataManager.ATTRIBUTE).getCacheIndex();
			int dataTypeIndex = _flexValueType.getAttribute(AbstractFlexDataManager.DATA_TYPE).getCacheIndex();
			int longIndex = _flexValueType.getAttribute(AbstractFlexDataManager.LONG_DATA).getCacheIndex();
			int doubleIndex = _flexValueType.getAttribute(AbstractFlexDataManager.DOUBLE_DATA).getCacheIndex();
			int varcharIndex =
				_flexValueType.getAttribute(AbstractFlexDataManager.VARCHAR_DATA).getCacheIndex();
			int clobIndex = _flexValueType.getAttribute(AbstractFlexDataManager.CLOB_DATA).getCacheIndex();
			int blobIndex = _flexValueType.getAttribute(AbstractFlexDataManager.BLOB_DATA).getCacheIndex();
			_insertWriter.appendInsert(table, _flexValues.stream().filter(val -> val.getValue() != null).map(val -> {
			
					Object[] columnValues = new Object[columnCount];
					cacheValues[branchIndex] = val.getOwner().getId().getBranchId();
					cacheValues[idIndex] = val.getOwner().getId().getObjectName();
					cacheValues[revMinIndex] = val.getStartRev();
					cacheValues[revMaxIndex] = val.getStopRev();
					cacheValues[typeIndex] = val.getOwner().getType().getName();
					cacheValues[attrIndex] = val.getName();
			
					try {
						AbstractFlexDataManager.setData(val.getCommitRev(), cacheValues, val.getValue(), dataTypeIndex,
							longIndex, doubleIndex, varcharIndex, clobIndex, blobIndex);
					} catch (SQLException ex) {
						StringBuilder errorSerializing = new StringBuilder();
						errorSerializing.append("Error setting flex data values in '");
						errorSerializing.append(val);
						errorSerializing.append("': Values: ");
						errorSerializing.append(Arrays.toString(cacheValues));
						_log.error(errorSerializing.toString(), ex);
						return null;
					}
			
					for (int n = 0; n < attributeCount; n++) {
						MOAttribute attr = attributes.get(n);
			
						DBAttribute[] dbMapping = attr.getDbMapping();
						if (dbMapping.length > 0) {
							AttributeStorage storage = storages[n];
							try {
							storage.storeValue(_pool, columnValues, 0, attr, val.getOwner(), cacheValues,
								val.getCommitRev());
							} catch (SQLException ex) {
								StringBuilder errorSerializing = new StringBuilder();
								errorSerializing.append("Error serializing value of flex attribute '");
								errorSerializing.append(attr.getName());
								errorSerializing.append("' in '");
								errorSerializing.append(val);
								errorSerializing.append("': Values: ");
								errorSerializing.append(Arrays.toString(cacheValues));
								_log.error(errorSerializing.toString(), ex);
								return null;
							}
						}
					}
					return columnValues;
			}).filter(Objects::nonNull).collect(Collectors.toList()));

			_flexValues.clear();
		}

		private DBTable table(MOStructure type) {
			return SchemaSetup.createTable((MOClass) type);
		}

		@Override
		public void close() throws IOException {
			try {
				flushBuffer();
				flushSequences();
			} catch (NoSuchAttributeException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static final class Brc extends Sys implements InternalBranch {

		private long _baseRevision;

		private long _baseBranch;

		public Brc(MetaObject branchType, long branchId, long baseBranchId, long revision, long baseRevisionNumber) {
			super(new ObjectBranchId(baseBranchId, branchType, LongID.valueOf(branchId)), revision, revision);
			_baseBranch = baseBranchId;
			_baseRevision = baseRevisionNumber;
		}

		@Override
		public long baseRevision() {
			return _baseRevision;
		}

		@Override
		public long baseBranch() {
			return _baseBranch;
		}

		@Override
		public long createRevision() {
			return getCreateRev();
		}

		@Override
		public long branchId() {
			return ((LongID) getId().getObjectName()).longValue();
		}

	}

	private static final class Rev extends Sys implements Revision {

		private long _date;

		private final String _author;

		private final String _log;

		public Rev(MetaObject revisionType, long rev, long date, String author, String log) {
			super(new ObjectBranchId(0, revisionType, LongID.valueOf(rev)), rev, rev);
			_date = date;
			_author = author;
			_log = log;
		}

		@Override
		public long getCommitNumber() {
			return getCreateRev();
		}

		@Override
		public String getAuthor() {
			return _author;
		}

		@Override
		public String getLog() {
			return _log;
		}

		void updateDate(long newDate) {
			_date = newDate;
		}

		@Override
		public long getDate() {
			return _date;
		}

		@Override
		public int compareTo(Revision o) {
			return Long.compare(getCommitNumber(), o.getCommitNumber());
		}

		@Override
		public boolean isCurrent() {
			return false;
		}

	}

	private static final class Row extends Item {

		private static Map<MetaObject, Map<String, MOAttribute>> _attributeByColumnName =
			new HashMap<>();

		private static long _nextId = Long.MAX_VALUE / 2;

		private final Log _log;

		public Row(MetaObject type, Log log) {
			super(new ObjectBranchId(TLContext.TRUNK_ID, type, LongID.valueOf(_nextId++)), Revision.CURRENT_REV);
			_log = log;
		}

		@Override
		Map<String, Val> flexValues() {
			return Collections.emptyMap();
		}

		@Override
		protected Val storeFlexAttribute(String name, Val value) {
			_log.error("Row " + this + " does not support flex attribute " + name
				+ ". Supported: " + Arrays.toString(getType().getAttributeNames()));
			return null;
		}

		@Override
		public Obj update(long revision, Map<String, Object> values) {
			throw new UnreachableAssertion("No update of object without identity " + this);
		}

		@Override
		protected MOAttribute getAttributeOrNull(MOStructure type, String name) {
			Map<String, MOAttribute> map = _attributeByColumnName.get(type);
			if (map == null) {
				map = new HashMap<>();
				for (MOAttribute attribute : type.getAttributes()) {
					for (DBAttribute column : attribute.getDbMapping()) {
						map.put(asKey(column.getDBName()), attribute);
					}
				}
				_attributeByColumnName.put(type, map);
			}
			return map.get(asKey(name));
		}

		private String asKey(String dbName) {
			return dbName.toLowerCase();
		}
	}

	private static final class Obj extends Item {

		public Obj(ObjectBranchId id, long createRev, long startRev, long stopRev) {
			super(id, createRev, startRev, stopRev);
		}

		public Obj(ObjectBranchId id, long revision) {
			super(id, revision);
		}

	}

	private static class Sys extends Item implements TLObject {

		public Sys(ObjectBranchId id, long createRev, long startRev) {
			super(id, createRev, startRev, Revision.CURRENT_REV);
		}

		@Override
		public KnowledgeItem tHandle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public TLStructuredType tType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object tValue(TLStructuredTypePart part) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void tUpdate(TLStructuredTypePart part, Object value) {
			throw new UnsupportedOperationException();
		}

	}

	private static class Val {

		private Item _owner;

		private String _name;

		private long _startRev;

		private Object _value;

		public Val(Item owner, String name, long startRev, Object value) {
			_owner = owner;
			_name = name;
			_startRev = startRev;
			_value = value;
		}

		public long getCommitRev() {
			return Item.getCommitRev(getStartRev(), getStopRev());
		}

		public Item getOwner() {
			return _owner;
		}

		public String getName() {
			return _name;
		}

		public long getStartRev() {
			return _startRev;
		}

		public long getStopRev() {
			return _owner.getStopRev();
		}

		public Object getValue() {
			return _value;
		}

		public Val update(Item versionItem, long revision, Object value) {
			Val version = new Val(versionItem, _name, _startRev, _value);
			_startRev = revision;
			_value = value;
			return version;
		}

		public Val moveTo(Item owner) {
			_owner = owner;
			return this;
		}

		@Override
		public String toString() {
			return _owner.getId() + "#" + _name + " in [" + getStartRev() + "," + getStopRev() + "]";
		}

	}

	private abstract static class Item implements KnowledgeItem {
	
		private final ObjectBranchId _id;
	
		private final Object[] _rowValues;

		private final Map<String, Val> _flexValues;
	
		private final long _createRev;
	
		private long _startRev;
	
		private long _stopRev;
	
		public Item(ObjectBranchId id, long revision) {
			this(id, revision, revision, Revision.CURRENT_REV);
		}

		public Item(ObjectBranchId id, long createRev, long startRev, long stopRev) {
			_id = id;
			if (!(createRev <= startRev)) {
				throw new IllegalArgumentException("Object can not be valid when is not yet created: rev_create: "
					+ createRev + ", rev_min: " + startRev);
			}
			if (!(startRev <= stopRev)) {
				throw new IllegalArgumentException(
					"Start revision of object must not be larger than stop revision: rev_min: " + startRev
						+ ", rev_max: " + stopRev);
			}
			_createRev = createRev;
			_startRev = startRev;
			_stopRev = stopRev;
			_rowValues = new Object[getType().getCacheSize()];
			_flexValues = new HashMap<>();
		}

		public void init(long revision, Map<String, Object> values) {
			MOStructure type = getType();
			for (Entry<String, Object> entry : values.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();

				MOAttribute attribute = getAttributeOrNull(type, name);
				if (attribute == null) {
					storeFlexAttribute(name, new Val(this, name, _startRev, value));
				} else {
					AttributeStorage storage = attribute.getStorage();
					if (!(storage instanceof InitialAttributeStorage || storage instanceof ComputedAttributeStorage)) {
						storage.setCacheValue(attribute, this, _rowValues,
							resolveNextCommitNumberFuture(value, revision));
					}
				}
			}
		}

		/**
		 * Approximate size in bytes of values of this item.
		 */
		public long aproximateSize() {
			long result = 0;
			for (Object value : _rowValues) {
				result = addSize(result, value);
			}
			for (Object value : _flexValues.values()) {
				result = addSize(result, value);
			}
			return result;
		}

		private long addSize(long result, Object value) {
			if (value instanceof CharSequence) {
				// In the Java heap, a char is stored as 16 bit value.
				result += 2 * ((CharSequence) value).length();
			} else if (value instanceof BinaryDataSource) {
				// When serialized, Base64 encoding must be used.
				result += 2 * ((BinaryDataSource) value).getSize();
			} else {
				// Any other value is considered 64 bit.
				result += 8;
			}
			return result;
		}

		protected Val storeFlexAttribute(String name, Val value) {
			return flexValues().put(name, value);
		}

		protected MOAttribute getAttributeOrNull(MOStructure type, String name) {
			return type.getAttributeOrNull(name);
		}
	
		public ObjectBranchId getId() {
			return _id;
		}
	
		public MOStructure getType() {
			return (MOStructure) getId().getObjectType();
		}
	
		Object[] rowValues() {
			return _rowValues;
		}

		Map<String, Val> flexValues() {
			return _flexValues;
		}

		public Object get(String attrName) {
			MOAttribute attribute = getType().getAttributeOrNull(attrName);
			if (attribute == null) {
				Val val = flexValues().get(attrName);
				if (val == null) {
					return null;
				} else {
					return val.getValue();
				}
			} else {
				return attribute.getStorage().getCacheValue(attribute, this, _rowValues);
			}

//			Object result = _values.get(attrName);
//			if (result == null && !_values.containsKey(attrName)) {
//				if (BasicTypes.BRANCH_ATTRIBUTE_NAME.equals(attrName)) {
//					return getId().getBranchId();
//				}
//				if (BasicTypes.IDENTIFIER_ATTRIBUTE_NAME.equals(attrName)) {
//					return getId().getObjectName();
//				}
//				else if (BasicTypes.REV_CREATE_ATTRIBUTE_NAME.equals(attrName)) {
//					return getCreateRev();
//				}
//				else if (BasicTypes.REV_MIN_ATTRIBUTE_NAME.equals(attrName)) {
//					return getStartRev();
//				}
//				else if (BasicTypes.REV_MAX_ATTRIBUTE_NAME.equals(attrName)) {
//					return getStopRev();
//				}
//			}
//			return result;
		}
	
		public long getCreateRev() {
			return _createRev;
		}
	
		/**
		 * First revision (inclusive) in which {@link #rowValues()} and {@link #flexValues()} are
		 * valid.
		 */
		public long getStartRev() {
			return _startRev;
		}
	
		/**
		 * Last revision (inclusive) in which {@link #rowValues()} and {@link #flexValues()} are
		 * valid.
		 */
		public long getStopRev() {
			return _stopRev;
		}

		public final long getCommitRev() {
			return Item.getCommitRev(getStartRev(), getStopRev());
		}

		public static long getCommitRev(long startRev, long stopRev) {
			if (stopRev == Revision.CURRENT_REV) {
				return startRev;
			} else {
				return stopRev + 1;
			}
		}
	
		public Obj update(long revision, Map<String, Object> values) {
			Obj oldVersion;
			if (isVersioned()) {
				// Create finalized version of this object.
				oldVersion = new Obj(getId(), getCreateRev(), getStartRev(), revision - 1);
				System.arraycopy(_rowValues, 0, oldVersion.rowValues(), 0, _rowValues.length);
				
				store(revision, values, oldVersion);
			} else {
				oldVersion = null;

				store(revision, values, null);
			}
	
			return oldVersion;
		}

		private boolean isVersioned() {
			MOStructure type = getType();
			return type instanceof MOClass && ((MOClass) type).isVersioned();
		}

		private void store(long revision, Map<String, Object> values, Item version) {
			_startRev = revision;

			for (Entry<String, Object> entry : values.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();

				MOAttribute attribute = getType().getAttributeOrNull(name);
				if (attribute == null) {
					// A flex value.

					if (value == null) {
						// Deleting a value.

						Val val = flexValues().remove(name);
						if (version != null) {
							if (val != null) {
								version.flexValues().put(name, val.moveTo(version));
							}
						}
					} else {
						// Setting a value.

						Val val = flexValues().get(name);
						if (val == null) {
							// Creating a value.

							flexValues().put(name, new Val(this, name, revision, value));
						} else {
							// Updating a value.

							if (!value.equals(val.getValue())) {
								if (version != null) {
									version.flexValues().put(name, val.update(version, revision, value));
								}
							}
						}
					}
				} else {
					// A row value.
					AttributeStorage storage = attribute.getStorage();
					if (storage.isDerived()) {
						continue;
					}
					storage.setCacheValue(attribute, this, _rowValues, resolveNextCommitNumberFuture(value, revision));
				}
			}
		}
	
		private Object resolveNextCommitNumberFuture(Object value, long commitNumber) {
			if (value == NextCommitNumberFuture.INSTANCE) {
				value = commitNumber;
			}
			return value;
		}

		public Item delete(long deleteRevision) {
			/* As the object is deleted in deleteRevision, the data are not longer valid after
			 * deleteRevision-1 */
			_stopRev = deleteRevision - 1;
			return isVersioned() ? this : null;
		}
	
		@Override
		public String toString() {
			return getId().toString() + " in [" + _startRev + "," + _stopRev + "]";
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_id == null) ? 0 : _id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Item other = (Item) obj;
			if (_id == null) {
				if (other._id != null)
					return false;
			} else if (!_id.equals(other._id))
				return false;
			return true;
		}

		@Deprecated
		@Override
		public String[] getAttributeNames() {
			return MetaObjectUtils.getAttributeNames(tTable());
		}
	
		@Deprecated
		@Override
		public Object getAttributeValue(String attrName) {
			return get(attrName);
		}
	
		@Deprecated
		@Override
		public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
			throw new UnsupportedOperationException();
		}
	
		@Deprecated
		@Override
		public boolean hasAttribute(String attributeName) {
			return MetaObjectUtils.hasAttribute(tTable(), attributeName);
		}

		@Override
		public MOStructure tTable() {
			return getType();
		}
	
		@Deprecated
		@Override
		public boolean isInstanceOf(MetaObject type) {
			throw new UnsupportedOperationException();
		}
	
		@Deprecated
		@Override
		public boolean isInstanceOf(String typeName) {
			throw new UnsupportedOperationException();
		}
	
		@Deprecated
		@Override
		public TLID getIdentifier() {
			return getId().getObjectName();
		}
	
		@Deprecated
		@Override
		public void setIdentifier(TLID anIdentifier) {
			throw new UnsupportedOperationException();
		}
	
		@Deprecated
		@Override
		public Iterable<? extends MOAttribute> getAttributes() {
			return MetaObjectUtils.getAttributes(tTable());
		}
	
		@Deprecated
		@Override
		public Object getValue(MOAttribute attribute) {
			return getAttributeValue(attribute.getName());
		}
	
		@Deprecated
		@Override
		public ObjectKey getReferencedKey(MOReference reference) {
			return (ObjectKey) getAttributeValue(reference.getName());
		}
	
		@Deprecated
		@Override
		public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public MORepository getTypeRepository() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public IdentifiedObject resolveObject(ObjectKey objectKey) {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public ObjectKey getKnownKey(ObjectKey key) {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public boolean isAlive() {
			return true;
		}
	
		@Override
		public State getState() {
			return State.PERSISTENT;
		}
	
		@Override
		public void touch() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public KnowledgeBase getKnowledgeBase() {
			return null;
		}
	
		/**
		 * TODO #2829: Delete TL 6 deprecation.
		 * 
		 * @deprecated Use {@link #tId()} instead
		 */
		@Deprecated
		@Override
		public final ObjectKey getObjectKey() {
			return tId();
		}

		@Override
		public ObjectKey tId() {
			return getId().toCurrentObjectKey();
		}
	
		@Override
		public TLID getObjectName() {
			return getId().getObjectName();
		}
	
		@Override
		public long getBranchContext() {
			return getId().getBranchId();
		}
	
		@Override
		public long getHistoryContext() {
			return Revision.CURRENT_REV;
		}

		@Override
		public long getCreateCommitNumber() {
			return getCreateRev();
		}

		@Override
		public long getLastUpdate() {
			return getStartRev();
		}
	
		@Override
		public void delete() throws DataObjectException {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends TLObject> T getWrapper() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public Set<String> getAllAttributeNames() {
			return new HashSet<>(Arrays.asList(getType().getAttributeNames()));
		}
	
		@Override
		public Object getGlobalAttributeValue(String attribute) throws NoSuchAttributeException {
			throw new UnsupportedOperationException();
		}
	
	}

}
