/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.basic.db.sql.SQLFactory.parameterDef;
import static com.top_logic.basic.db.sql.SQLFactory.setParameterDef;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExtID;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.io.binary.FileBasedBinaryData;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.attr.ComputedMOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem.KnowledgeItemResult;
import com.top_logic.util.TLContext;


/**
 * Base class for {@link FlexDataManager} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public abstract class AbstractFlexDataManager implements FlexDataManager {

	/**
	 * Configuration options for {@link AbstractFlexDataManager}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * {@link Statement#setFetchSize(int) Fetch size} for bulk-loading flex attributes.
		 */
		int getFetchSize();

	}

	/**
	 * @see Config#getFetchSize()
	 */
	private final int fetchSize = ApplicationConfig.getInstance().getConfig(Config.class).getFetchSize();

	/**
	 * Marker that identifies the {@link Revision#CURRENT current revision} in a
	 * {@link #createRevMaxAttr()} or {@link #createRevMinAttr()}.
	 */
	protected static final long CURRENT_REV = Revision.CURRENT_REV;

	/**
	 * Attribute that stores the {@link Branch} of the row value.
	 */
	private static MOAttribute createBranchAttr(boolean withDBColumn) {
		MOAttribute attr;
		if (withDBColumn) {
			attr = IdentifierTypes.newBranchReference(BRANCH, BRANCH_DBNAME);
		} else {
			attr = new ComputedMOAttribute(BRANCH, IdentifierTypes.BRANCH_REFERENCE_MO_TYPE);
			attr.setStorage(TrunkStorage.INSTANCE);
		}
		attr.setMandatory(true);
		attr.setImmutable(false);
		attr.setSystem(true);
		return attr;
	}

	/**
	 * Attribute that stores the {@link MetaObject#getName() type} of the
	 * object, the row value is associated with.
	 */
	private static MOAttributeImpl createTypeAttr() {
		return IdentifierTypes.newTypeReference(TYPE, TYPE_DBNAME);
	}

	/**
	 * Attribute that stores the {@link KnowledgeObject#getObjectName()} of the
	 * object, the row value is associated with.
	 */
	private static MOAttributeImpl createIdentifierAttr() {
		MOAttributeImpl attribute =
			IdentifierTypes.newIdentifierAttribute(IDENTIFIER, IDENTIFIER_DBNAME);
		attribute.setImmutable(true);
		attribute.setMandatory(true);
		return attribute;
	}

	/**
	 * Attribute that stores the {@link Revision#getCommitNumber()} of the last
	 * revision, the row value belongs to.
	 */
	private static MOAttributeImpl createRevMaxAttr() {
		MOAttributeImpl attr =
			IdentifierTypes.newRevisionReference(REV_MAX, BasicTypes.REV_MAX_DB_NAME, !MOAttribute.IMMUTABLE);
		attr.setSystem(true);
		return attr;
	}
	
	/**
	 * Maximum length of an attribute that can be stored in an {@link AbstractFlexDataManager}'s table.
	 */
	public static final int MAX_ATTRIBUTE_NAME_LENGTH = 254;

	/**
	 * Attribute that stores the name of the row value.
	 */
	private static MOAttributeImpl createAttributeAttr() {
		MOAttributeImpl attr =
			new MOAttributeImpl(ATTRIBUTE, ATTRIBUTE_DBNAME, MOPrimitive.STRING, true, true, DBType.STRING,
				MAX_ATTRIBUTE_NAME_LENGTH, 0);
		attr.setBinary(true);
		return attr;
	}

	/**
	 * Attribute that stores the {@link Revision#getCommitNumber()} of the first
	 * revision, the row value belongs to.
	 */
	private static MOAttributeImpl createRevMinAttr() {
		MOAttributeImpl attr =
			IdentifierTypes.newRevisionReference(REV_MIN, BasicTypes.REV_MIN_DB_NAME, !MOAttribute.IMMUTABLE);
		attr.setSystem(true);
		return attr;
	}
	
	/**
	 * Attribute that stores the a data type marker that describes the type of the stored row value.
	 * 
	 * @see #BOOLEAN_TRUE
	 * @see #BOOLEAN_FALSE
	 * @see #LONG_TYPE
	 * @see #INTEGER_TYPE
	 * @see #DOUBLE_TYPE
	 * @see #FLOAT_TYPE
	 * @see #STRING_TYPE
	 * @see #CLOB_TYPE
	 * @see #TL_ID_TYPE
	 * @see #EXT_ID_TYPE
	 */
	private static MOAttributeImpl createDataTypeAttr() {
		return new MOAttributeImpl(DATA_TYPE, DATA_TYPE_DBNAME, MOPrimitive.INTEGER, false, false, DBType.BYTE, 4, 0);
	}

	/**
	 * Attribute that can store {@link Long} data. 
	 */
	private static MOAttributeImpl createLongTypeAttr() {
		return new MOAttributeImpl(LONG_DATA, LONG_DATA_DBNAME, MOPrimitive.LONG, false, false, DBType.LONG, 20, 0);
	}
	/**
	 * Attribute that can store {@link Double} data. 
	 */
	private static MOAttributeImpl createDoubleDataAttr() {
		return new MOAttributeImpl(DOUBLE_DATA, DOUBLE_DATA_DBNAME, MOPrimitive.DOUBLE, false, false, DBType.DOUBLE,
			20,
			0);
	}
	/**
	 * Maximum length of a {@link String} value stored in an
	 * {@link AbstractFlexDataManager}'s <code>varchar</code> column.
	 */
	public static final int VARCHAR_DATA_ATTR_LEN = 254;
	
	/**
	 * Attribute that can store short {@link String} data. 
	 */
	private static MOAttributeImpl createVarcharDataAttr() {
		return new MOAttributeImpl(VARCHAR_DATA, VARCHAR_DATA_DBNAME, MOPrimitive.STRING, false, false, DBType.STRING,
			VARCHAR_DATA_ATTR_LEN, 0);
	}
	/**
	 * Attribute that can store long {@link String} data. 
	 */
	private static MOAttributeImpl createClobDataAttr() {
		return new MOAttributeImpl(CLOB_DATA, CLOB_DATA_DBNAME, MOPrimitive.STRING, false, false, DBType.CLOB, 0, 0);
	}
    /**
     * Attribute that can store long {@link String} data. 
     */
	private static MOAttributeImpl createBlobDataAttr() {
		return new MOAttributeImpl(BLOB_DATA, BLOB_DATA_DBNAME, MOPrimitive.BLOB, false, false, DBType.BLOB, 0, 0);
	}

    /**
	 * Name of the {@link KnowledgeItem} type that stores {@link NamedValues} data associated with a
	 * {@link KnowledgeObject}.
	 */
	public static final String FLEX_DATA = "FlexData";

	/**
	 * Database table name of {@link #FLEX_DATA}.
	 */
	public static final String FLEX_DATA_DB_NAME = "FLEX_DATA";

	/**
	 * Create {@link KnowledgeItem} type that stores {@link NamedValues} data associated with a
	 * {@link KnowledgeObject}.
	 * 
	 * @param typeName
	 *        The internal name of the flex data type.
	 * @param dbName
	 *        The name of the corresponding database table.
	 * @param multipleBranches
	 *        Whether multiple branches are supported
	 */
	public static MOKnowledgeItemImpl createFlexDataType(String typeName, String dbName, boolean multipleBranches) {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(typeName);
		type.setDBName(dbName);
    	type.setFinal(true);
    	type.setVersioned(false);
    	MOKnowledgeItemUtil.setSystem(type, true);
        type.setPkeyStorage(true);
		MOAttribute branchAttribute;
		MOAttributeImpl typeAttribute;
		MOAttributeImpl idAttribute;
		MOAttributeImpl revMaxAttribute;
		MOAttributeImpl attributeAttribute;
		MOAttributeImpl revMinAttribute;
    	try {
			branchAttribute = createBranchAttr(multipleBranches);
			type.addAttribute(branchAttribute);
			typeAttribute = createTypeAttr();
			type.addAttribute(typeAttribute);
			idAttribute = createIdentifierAttr();
			type.addAttribute(idAttribute);
			revMaxAttribute = createRevMaxAttr();
			type.addAttribute(revMaxAttribute);
			attributeAttribute = createAttributeAttr();
			type.addAttribute(attributeAttribute);
			
			revMinAttribute = createRevMinAttr();
			type.addAttribute(revMinAttribute);
			type.addAttribute(createDataTypeAttr());
			
			type.addAttribute(createLongTypeAttr());
			type.addAttribute(createDoubleDataAttr());
			type.addAttribute(createVarcharDataAttr());
			type.addAttribute(createClobDataAttr());
			type.addAttribute(createBlobDataAttr());
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		DBAttribute[] primaryKeyColumns =
			BasicTypeProvider.primaryKeyColumns(branchAttribute, revMaxAttribute, typeAttribute, idAttribute,
				attributeAttribute);
		type.setPrimaryKey(primaryKeyColumns);
		/* Compress value must be strict less than number of columns in the prefix. Otherwise Oracle
		 * sends a ORA-25194 error. */
		type.setCompress(primaryKeyColumns.length - 1);
		BasicTypeProvider.addMaxMinIndexes(type, revMaxAttribute, revMinAttribute);

		return type;
    }

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createBranchAttr(boolean)} data.
	 */
	public static final String BRANCH_DBNAME = "BRANCH";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createTypeAttr()} data.
	 */
	public static final String TYPE_DBNAME = "TYPE";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createIdentifierAttr()} data.
	 */
	public static final String IDENTIFIER_DBNAME = "IDENTIFIER";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createAttributeAttr()} data.
	 */
	public static final String ATTRIBUTE_DBNAME = "ATTR";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createTypeAttr()} data.
	 */
	public static final String DATA_TYPE_DBNAME = "DATA_TYPE";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createLongTypeAttr()} data.
	 */
	public static final String LONG_DATA_DBNAME = "LONG_DATA";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createDoubleDataAttr()} data.
	 */
	public static final String DOUBLE_DATA_DBNAME = "DOUBLE_DATA";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createVarcharDataAttr()} data.
	 */
	public static final String VARCHAR_DATA_DBNAME = "VARCHAR_DATA";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createClobDataAttr()} data.
	 */
	public static final String CLOB_DATA_DBNAME = "CLOB_DATA";

	/**
	 * Name of the {@link MOAttribute} that stores {@link #createBlobDataAttr()} data.
	 */
	public static final String BLOB_DATA_DBNAME = "BLOB_DATA";

	/**
	 * Name of the DB column that stores {@link #createBranchAttr(boolean)} data.
	 */
	public static final String BRANCH = "_branch";

	/**
	 * Name of the DB column that stores {@link #createTypeAttr()} data.
	 */
	public static final String TYPE = "type";

	/**
	 * Name of the DB column that stores {@link #createIdentifierAttr()} data.
	 */
	public static final String IDENTIFIER = "identifier";

	/**
	 * Name of the DB column that stores {@link #createAttributeAttr()} data.
	 */
	public static final String ATTRIBUTE = "attr";

	/**
	 * Name of the DB column that stores {@link #createRevMinAttr()} data.
	 */
	public static final String REV_MIN = "_rev_min";

	/**
	 * Name of the DB column that stores {@link #createRevMaxAttr()} data.
	 */
	public static final String REV_MAX = "_rev_max";

	/**
	 * Name of the DB column that stores {@link #createTypeAttr()} data.
	 */
	public static final String DATA_TYPE = "dataType";

	/**
	 * Name of the DB column that stores {@link #createLongTypeAttr()} data.
	 */
	public static final String LONG_DATA = "longData";

	/**
	 * Name of the DB column that stores {@link #createDoubleDataAttr()} data.
	 */
	public static final String DOUBLE_DATA = "doubleData";

	/**
	 * Name of the DB column that stores {@link #createVarcharDataAttr()} data.
	 */
	public static final String VARCHAR_DATA = "varcharData";

	/**
	 * Name of the DB column that stores {@link #createClobDataAttr()} data.
	 */
	public static final String CLOB_DATA = "clobData";

	/**
	 * Name of the DB column that stores {@link #createBlobDataAttr()} data.
	 */
	public static final String BLOB_DATA = "blobData";
    
	// Special row types that use no data columns.
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Boolean#TRUE} value.  
     */
	public static final byte BOOLEAN_TRUE = 1;
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Boolean#FALSE} value.  
     */
	public static final byte BOOLEAN_FALSE = 2;
    
	// Row types that use the long data column.
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Long} value in the {@link #createLongTypeAttr()}.  
     */
	public static final byte LONG_TYPE = 10;
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Integer} value in the {@link #createLongTypeAttr()}.
     */
	public static final byte INTEGER_TYPE = 11;
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Date} value in the {@link #createLongTypeAttr()}.  
     */
	public static final byte DATE_TYPE = 12;
    
	// Row types that use the double data column.
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Double} value in the {@link #createDoubleDataAttr()}.  
     */
	public static final byte DOUBLE_TYPE = 20;
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link Float} value in the {@link #createDoubleDataAttr()}.  
     */
	public static final byte FLOAT_TYPE = 21;
    
	// Row types that use the varchar data column.
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link String} value in the {@link #createVarcharDataAttr()}.  
     */
	public static final byte STRING_TYPE = 30;

	/**
	 * {@link #createDataTypeAttr()} value that marks an empty (non null size 0)
	 * {@link String} value.
	 * 
	 * <p>
	 * This special handling is required for databases that cannot differentiate
	 * between <code>null</code> and the empty string in <code>VARCHAR</code>
	 * data. See e.g. Oracle.
	 * </p>
	 */
	public static final byte EMPTY_STRING_TYPE = 31;
	
	// Row types that use the clob data column.
    
    /**
     * {@link #createDataTypeAttr()} value that marks a {@link String} value in the {@link #createClobDataAttr()}.  
     */
	public static final byte CLOB_TYPE = 40;

    /**
     * {@link #createDataTypeAttr()} value that marks a {@link String} value in the {@link #createBlobDataAttr()}.  
     */
	public static final byte BLOB_TYPE = 50;

	/**
	 * {@link #createDataTypeAttr()} value that marks a {@link TLID}. If value is {@link LongID} it
	 * is stored in {@link #LONG_DATA}, otherwise it is stored in {@link #VARCHAR_DATA}.
	 */
	public static final byte TL_ID_TYPE = 60;

	/**
	 * {@link #createDataTypeAttr()} value that marks a {@link ExtID}. {@link ExtID#systemId()} is
	 * stored in {@link #LONG_DATA} (it is random long and therefore large),
	 * {@link ExtID#objectId()} is stored in {@link #VARCHAR_DATA}
	 */
	public static final byte EXT_ID_TYPE = 70;

	/**
	 * {@link Comparator} that compares {@link ObjectKey} by
	 * <ol>
	 * <li>{@link ObjectKey#getHistoryContext() revision}</li>
	 * <li>{@link ObjectKey#getBranchContext() branch}</li>
	 * <li>{@link ObjectKey#getObjectType() type}</li>
	 * </ol>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class KeyPartition implements Comparator<ObjectKey> {

		/**
		 * Singleton {@link AbstractFlexDataManager.KeyPartition} instance.
		 */
		public static final AbstractFlexDataManager.KeyPartition INSTANCE = new KeyPartition();

		private KeyPartition() {
			// Singleton constructor.
		}

		@Override
		public int compare(ObjectKey o1, ObjectKey o2) {
			int historyComparision = CollectionUtil.compareLong(o1.getHistoryContext(), o2.getHistoryContext());
			if (historyComparision != 0) {
				return historyComparision;
			}
			int branchComparision = CollectionUtil.compareLong(o1.getBranchContext(), o2.getBranchContext());
			if (branchComparision != 0) {
				return branchComparision;
			}
			int typeComparision = o1.getObjectType().getName().compareTo(o2.getObjectType().getName());
			return typeComparision;
		}
	}

	/**
	 * {@link Comparator} that compares {@link ObjectKey} by
	 * <ol>
	 * <li>{@link ObjectKey#getHistoryContext() revision}</li>
	 * <li>{@link ObjectKey#getBranchContext() branch}</li>
	 * <li>{@link ObjectKey#getObjectType() type}</li>
	 * <li>{@link ObjectKey#getObjectName() id}</li>
	 * </ol>
	 * 
	 * @see KeyPartition
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class KeyOrder extends KeyPartition {

		/**
		 * Singleton {@link AbstractFlexDataManager.KeyOrder} instance.
		 */
		public static final KeyOrder INSTANCE = new KeyOrder();

		private KeyOrder() {
			// Singleton constructor.
		}

		@Override
		public int compare(ObjectKey o1, ObjectKey o2) {
			int partitionCompare = super.compare(o1, o2);
			if (partitionCompare != 0) {
				return partitionCompare;
			}

			int nameComparision = o1.getObjectName().compareTo(o2.getObjectName());
			return nameComparision;
		}
	}

	/**
	 * {@link QueryResult} interface this {@link FlexDataManager} is able to
	 * retrieve attribute values from.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
    public interface AttributeResult extends QueryResult {
    	/**
    	 * The name of the attribute this row contains the value.
    	 */
    	String getAttributeName() throws SQLException;

		/**
		 * The minimum revision from which the row is valid.
		 */
		long getRevMin() throws SQLException;

		/**
		 * The encoding of the type of the value and the column that contains
		 * the data.
		 */
    	byte getDataType() throws SQLException;
    	
    	/**
    	 * The data from the {@link AbstractFlexDataManager#createLongTypeAttr()}.
    	 */
    	long getLongData() throws SQLException;
    	
    	/**
    	 * The data from the {@link AbstractFlexDataManager#createDoubleDataAttr()}.
    	 */
    	double getDoubleData() throws SQLException;
    	
    	/**
    	 * The data from the {@link AbstractFlexDataManager#createVarcharDataAttr()}.
    	 */
    	String getVarcharData() throws SQLException;
    	
    	/**
    	 * The data from the {@link AbstractFlexDataManager#createClobDataAttr()}.
    	 */
    	String getClobData() throws SQLException;
    	
        /**
         * The data from the {@link AbstractFlexDataManager#createBlobDataAttr()}.
         */
        BinaryData getBlobData() throws SQLException;
    }
    
	static abstract class AttributeResultSetWrapper extends ResultSetWrapper implements AttributeResult {

		private final ConnectionPool _basePool;

		/**
		 * May be {@link VersionedBLOBData#NO_BRANCH} in case there is no database column for
		 * branch.
		 */
		private final long _branch;

		private final String _type;

		private final long _commitNumber;

		private MOKnowledgeItemImpl _dataType;

		public AttributeResultSetWrapper(ConnectionPool aPool, ResultSet result,
				long aBranch, String aType, MOKnowledgeItemImpl dataType, long aCommitNr) {
			super(result);
			_basePool = aPool;
			_branch = aBranch;
			_type = aType;
			_dataType = dataType;
			_commitNumber = aCommitNr;
		}

		@Override
		public long getRevMin() throws SQLException {
			return resultSet.getLong(revMinIndex());
		}

		protected abstract int revMinIndex();

		@Override
		public String getAttributeName() throws SQLException {
			return resultSet.getString(attributeIndex());
		}

		protected abstract int attributeIndex();

		@Override
		public byte getDataType() throws SQLException {
			return resultSet.getByte(dataTypeIndex());
		}

		protected abstract int dataTypeIndex();

		@Override
		public double getDoubleData() throws SQLException {
			return resultSet.getDouble(doubleIndex());
		}

		protected abstract int doubleIndex();

		@Override
		public long getLongData() throws SQLException {
			return resultSet.getLong(longIndex());
		}

		protected abstract int longIndex();

		@Override
		public String getVarcharData() throws SQLException {
			return resultSet.getString(varcharIndex());
		}

		protected abstract int varcharIndex();

		@Override
		public String getClobData() throws SQLException {
			final DBHelper sqlDialect = _basePool.getSQLDialect();
			return sqlDialect.getClobValue(resultSet, clobIndex());
		}

		protected abstract int clobIndex();

		@Override
		public BinaryData getBlobData() throws SQLException {
			try {
				String attr = getAttributeName();
				long size = getLongData();

				String contentType = getVarcharData();
				String name = DBBinaryData.noEmptyName(getClobData());

				if (size < BinaryDataFactory.MAX_MEMORY_SIZE) {
					final DBHelper sqlDialect = _basePool.getSQLDialect();
					InputStream stream = sqlDialect.getBinaryStream(resultSet, blobIndex());

					return BinaryDataFactory.createMemoryBinaryData(stream, size, contentType, name);
				}
				// Will fetch Stream on demand, later
				return new VersionedBLOBData(size, _basePool, _dataType, _branch, _type, getObjectName(), attr,
					_commitNumber, name);
			} catch (IOException ex) {
				throw (SQLException) new SQLException("Reading stream attribute failed.").initCause(ex);
			}
		}

		protected abstract int blobIndex();

		protected abstract TLID getObjectName() throws SQLException;

	}

	private static class GetHistoricAttributesStatement {

		private static final String HISTORY_CONTEXT = "historyContext";

		final int RESULT_ATTRIBUTE_IDX;

		final int RESULT_REV_MIN_IDX;

		final int RESULT_DATA_TYPE_IDX;

		final int RESULT_LONG_DATA_IDX;

		final int RESULT_DOUBLE_DATA_IDX;

		final int RESULT_VARCHAR_DATA_IDX;

		final int RESULT_CLOB_DATA_IDX;

		final int RESULT_BLOB_DATA_IDX;
    	
		private final CompiledStatement statement;

		final MOKnowledgeItemImpl dataType;

		public GetHistoricAttributesStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			String tableAlias = "x";
			List<SQLColumnDefinition> columns = new ArrayList<>();
			columns.add(columnDef(column(tableAlias, ATTRIBUTE_DBNAME), ATTRIBUTE_DBNAME));
			RESULT_ATTRIBUTE_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, BasicTypes.REV_MIN_DB_NAME), BasicTypes.REV_MIN_DB_NAME));
			RESULT_REV_MIN_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, DATA_TYPE_DBNAME), DATA_TYPE_DBNAME));
			RESULT_DATA_TYPE_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, LONG_DATA_DBNAME), LONG_DATA_DBNAME));
			RESULT_LONG_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, DOUBLE_DATA_DBNAME), DOUBLE_DATA_DBNAME));
			RESULT_DOUBLE_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, VARCHAR_DATA_DBNAME), VARCHAR_DATA_DBNAME));
			RESULT_VARCHAR_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, CLOB_DATA_DBNAME), CLOB_DATA_DBNAME));
			RESULT_CLOB_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, BLOB_DATA_DBNAME), BLOB_DATA_DBNAME));
			RESULT_BLOB_DATA_IDX = columns.size();
			SQLTableReference from = table(dataType, tableAlias);
			boolean multipleBranches = dataType.multipleBranches();
			SQLExpression where;
			if (multipleBranches) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				eq(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				ge(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT)),
				le(column(tableAlias, BasicTypes.REV_MIN_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT))
				);
			SQLSelect select = select(columns, from, where);
			select.setNoBlockHint(true);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				parameterDef(DBType.ID, IDENTIFIER_DBNAME),
				parameterDef(DBType.LONG, HISTORY_CONTEXT)
				);
			this.statement = SQLFactory.query(parameters, select).toSql(sqlDialect);

			this.dataType = dataType;
		}

		public AttributeResult query(ConnectionPool basePool, PooledConnection connection,
				long branch, String type, TLID id, long commitNumber)
				throws SQLException {
			if (dataType.multipleBranches()) {
				ResultSet result = statement.executeQuery(connection, branch, type, id, commitNumber);
				return new HistoricAttributeResult(basePool, result, branch, type, id, commitNumber);
			} else {
				ResultSet result = statement.executeQuery(connection, type, id, commitNumber);
				return new HistoricAttributeResult(basePool, result, VersionedBLOBData.NO_BRANCH, type, id,
					commitNumber);
			}
    	}
    	
		public final class HistoricAttributeResult extends AttributeResultSetWrapper {

			private TLID _id;

			public HistoricAttributeResult(ConnectionPool aPool, ResultSet result,
					long aBranch, String aType, TLID anId, long aCommitNr) {
				super(aPool, result, aBranch, aType, dataType, aCommitNr);
				_id = anId;
 			}

			@Override
			protected int revMinIndex() {
				return RESULT_REV_MIN_IDX;
			}

			@Override
			protected int attributeIndex() {
				return RESULT_ATTRIBUTE_IDX;
			}

			@Override
			protected int dataTypeIndex() {
				return RESULT_DATA_TYPE_IDX;
			}

			@Override
			protected int doubleIndex() {
				return RESULT_DOUBLE_DATA_IDX;
			}

			@Override
			protected int longIndex() {
				return RESULT_LONG_DATA_IDX;
			}

			@Override
			protected int varcharIndex() {
				return RESULT_VARCHAR_DATA_IDX;
			}

			@Override
			protected int clobIndex() {
				return RESULT_CLOB_DATA_IDX;
			}

			@Override
			protected int blobIndex() {
				return RESULT_BLOB_DATA_IDX;
			}
			
			@Override
			protected TLID getObjectName() throws SQLException {
				return _id;
			}

    	}
    	
	}
	
    /**
     * This allows re-fetching of DBBinaryData in case the {@link FileBasedBinaryData} becomes invalid.
     */
    private static class VersionedBLOBData extends DBBinaryData {

		static final long NO_BRANCH = -1;
        
		private static final String HISTORY_CONTEXT = "historyContext";
		private final long   branch;
        private final String type;

		private final TLID id;
        private final String attr;
        private final long   commitNumber;

		private final CompiledStatement statement;

		private final String _name;

		public VersionedBLOBData(long aSize, ConnectionPool aPool, MOKnowledgeItemImpl dataType, long aBranch,
				String aType, TLID anId, String anAttr, long aCommitNr, String name) throws SQLException {
			super(aSize, aPool);
			this._name = name;
            this.branch = aBranch;
            this.type   = aType;
            this.id     = anId;
            this.attr   = anAttr;
            this.commitNumber = aCommitNr;
			DBHelper sqlDialect = aPool.getSQLDialect();
			String tableAlias = NO_TABLE_ALIAS;
			List<SQLColumnDefinition> columns = columns(
				columnDef(column(tableAlias, VARCHAR_DATA_DBNAME), null),
				columnDef(column(tableAlias, LONG_DATA_DBNAME), null),
				columnDef(column(tableAlias, BLOB_DATA_DBNAME), null)
				);
			SQLTableReference from = table(dataType, tableAlias);
			SQLExpression where;
			if (multipleBranches()) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				eq(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				eq(column(tableAlias, ATTRIBUTE_DBNAME, NOT_NULL), parameter(DBType.STRING, ATTRIBUTE_DBNAME)),
				ge(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT)),
				le(column(tableAlias, BasicTypes.REV_MIN_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT))
				);
			SQLSelect select = select(columns, from, where);
			select.setNoBlockHint(true);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches()) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				parameterDef(DBType.ID, IDENTIFIER_DBNAME),
				parameterDef(DBType.STRING, ATTRIBUTE_DBNAME),
				parameterDef(DBType.LONG, HISTORY_CONTEXT)
				);
			this.statement = query(parameters, select).toSql(sqlDialect);
        }

		private boolean multipleBranches() {
			return this.branch != NO_BRANCH;
		}

		@Override
		protected BinaryData refetch(PooledConnection connection) throws SQLException {
			try (ResultSet res = fetchResult(connection)) {
				if (res.next()) {
					return fromBlobColumn(connection.getSQLDialect(), res, getName(), 1, 2, 3);
				}
				throw new SQLException("No binary data found.");
			}
		}

        /**
		 * Create the {@link ResultSet} need to re-fetch the historic BLOB-Data.
		 */
		protected ResultSet fetchResult(PooledConnection connection) throws SQLException {
			if (multipleBranches()) {
				return statement.executeQuery(connection, branch, type, id, attr, commitNumber);
			} else {
				return statement.executeQuery(connection, type, id, attr, commitNumber);
			}
        }

		@Override
		public String getName() {
			return _name;
		}
    }

	
	private static class GetBulkAttributesStatement {
		private static final String HISTORY_CONTEXT = "historyContext";

		final int RESULT_ID_IDX;

		final int RESULT_REV_MIN_IDX;

		final int RESULT_ATTRIBUTE_IDX;

		final int RESULT_DATA_TYPE_IDX;

		final int RESULT_LONG_DATA_IDX;

		final int RESULT_DOUBLE_DATA_IDX;

		final int RESULT_VARCHAR_DATA_IDX;

		final int RESULT_CLOB_DATA_IDX;

		final int RESULT_BLOB_DATA_IDX;

		private final CompiledStatement statement;

		final MOKnowledgeItemImpl _dataType;

		final DBHelper _sqlDialect;

		public GetBulkAttributesStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType, int fetchSize) {
			_sqlDialect = sqlDialect;
			_dataType = dataType;
			boolean multipleBranches = _dataType.multipleBranches();

			String tableAlias = "x";
			List<SQLColumnDefinition> columns = new ArrayList<>();
			columns.add(columnDef(column(tableAlias, IDENTIFIER_DBNAME), IDENTIFIER_DBNAME));
			RESULT_ID_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, ATTRIBUTE_DBNAME), ATTRIBUTE_DBNAME));
			RESULT_ATTRIBUTE_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, BasicTypes.REV_MIN_DB_NAME), BasicTypes.REV_MIN_DB_NAME));
			RESULT_REV_MIN_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, DATA_TYPE_DBNAME), DATA_TYPE_DBNAME));
			RESULT_DATA_TYPE_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, LONG_DATA_DBNAME), LONG_DATA_DBNAME));
			RESULT_LONG_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, DOUBLE_DATA_DBNAME), DOUBLE_DATA_DBNAME));
			RESULT_DOUBLE_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, VARCHAR_DATA_DBNAME), VARCHAR_DATA_DBNAME));
			RESULT_VARCHAR_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, CLOB_DATA_DBNAME), CLOB_DATA_DBNAME));
			RESULT_CLOB_DATA_IDX = columns.size();
			columns.add(columnDef(column(tableAlias, BLOB_DATA_DBNAME), BLOB_DATA_DBNAME));
			RESULT_BLOB_DATA_IDX = columns.size();
			SQLTableReference from = table(dataType, tableAlias);
			SQLExpression where;
			if (multipleBranches) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				inSet(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), setParameter(IDENTIFIER_DBNAME, DBType.ID)),
				ge(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT)),
				le(column(tableAlias, BasicTypes.REV_MIN_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT))
				);
			List<SQLOrder> order = orders(order(false, column(tableAlias, IDENTIFIER_DBNAME)));
			SQLSelect select = select(columns, from, where, order);
			select.setNoBlockHint(true);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				setParameterDef(IDENTIFIER_DBNAME, DBType.ID),
				parameterDef(DBType.LONG, HISTORY_CONTEXT)
				);

			SQLQuery<SQLSelect> query = SQLFactory.query(parameters, select);
			CompiledStatement statement = query.toSql(sqlDialect);
			statement.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (fetchSize > 0) {
				statement.setFetchSize(fetchSize);
			}
			this.statement = statement;
		}

		public BulkAttributeResult query(ConnectionPool basePool, PooledConnection connection,
				long branch, String type, Collection<TLID> ids, long commitNumber) throws SQLException {
			if (_dataType.multipleBranches()) {
				ResultSet resultSet = statement.executeQuery(connection, branch, type, ids, commitNumber);
				return new BulkAttributeResult(basePool, resultSet, branch, type, commitNumber);
			} else {
				ResultSet resultSet = statement.executeQuery(connection, type, ids, commitNumber);
				return new BulkAttributeResult(basePool, resultSet, VersionedBLOBData.NO_BRANCH, type, commitNumber);
			}
		}

		public final class BulkAttributeResult extends AttributeResultSetWrapper {

			public BulkAttributeResult(ConnectionPool aPool, ResultSet resultSet, long aBranch,
					String aType, long aCommitNr) {
				super(aPool, resultSet, aBranch, aType, _dataType, aCommitNr);
			}

			@Override
			protected TLID getObjectName() throws SQLException {
				return IdentifierUtil.getId(resultSet, RESULT_ID_IDX);
			}

			@Override
			protected int revMinIndex() {
				return RESULT_REV_MIN_IDX;
			}

			@Override
			protected int attributeIndex() {
				return RESULT_ATTRIBUTE_IDX;
			}

			@Override
			protected int dataTypeIndex() {
				return RESULT_DATA_TYPE_IDX;
			}

			@Override
			protected int doubleIndex() {
				return RESULT_DOUBLE_DATA_IDX;
			}

			@Override
			protected int longIndex() {
				return RESULT_LONG_DATA_IDX;
			}

			@Override
			protected int varcharIndex() {
				return RESULT_VARCHAR_DATA_IDX;
			}

			@Override
			protected int clobIndex() {
				return RESULT_CLOB_DATA_IDX;
			}

			@Override
			protected int blobIndex() {
				return RESULT_BLOB_DATA_IDX;
			}

		}

	}

    protected static class AddAttributeStatement {
    	
		/**
		 * Collector for batches of {@link AddAttributeStatement#statement()}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface AddAttributeBatchCollector {

			/**
			 * Adds a new batch for {@link AddAttributeStatement#statement()}.
			 * 
			 * @param arguments
			 *        Arguments for the batch.
			 * @throws SQLException
			 *         when creating batch failed.
			 */
			void addAddAttributeBatch(Object... arguments) throws SQLException;

		}

		private static final int NO_BRANCH_COLUMN = -1;

		private final int NUMBER_PARAMETERS;

		private final int MAX_BATCH_SIZE;

		private final int PARAM_BRANCH_IDX;

		private final int PARAM_TYPE_IDX;

		private final int PARAM_IDENTIFIER_IDX;

		private final int PARAM_ATTRIBUTE_IDX;

		private final int PARAM_REV_IDX;

		private final int PARAM_DATA_TYPE_IDX;

		private final int PARAM_LONG_DATA_IDX;

		private final int PARAM_DOUBLE_DATA_IDX;

		private final int PARAM_VARCHAR_DATA_IDX;

		private final int PARAM_CLOB_DATA_IDX;

		private final int PARAM_BLOB_DATA_IDX;

		private final CompiledStatement statement;

		public AddAttributeStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(dataType, tableAlias);
			boolean multipleBranches = dataType.multipleBranches();
			List<String> columnNames = new ArrayList<>();
			if (multipleBranches) {
				columnNames.add(BRANCH_DBNAME);
			}
			Collections.addAll(columnNames,
				TYPE_DBNAME,
				IDENTIFIER_DBNAME,
				BasicTypes.REV_MAX_DB_NAME,
				ATTRIBUTE_DBNAME,
				BasicTypes.REV_MIN_DB_NAME,
				DATA_TYPE_DBNAME,
				LONG_DATA_DBNAME,
				DOUBLE_DATA_DBNAME,
				VARCHAR_DATA_DBNAME,
				CLOB_DATA_DBNAME,
				BLOB_DATA_DBNAME
				);
			List<SQLExpression> values = new ArrayList<>();
			if (multipleBranches) {
				values.add(parameter(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(values,
				parameter(DBType.STRING, TYPE_DBNAME),
				parameter(DBType.ID, IDENTIFIER_DBNAME),
				literal(DBType.LONG, CURRENT_REV),
				parameter(DBType.STRING, ATTRIBUTE_DBNAME),
				parameter(DBType.LONG, BasicTypes.REV_MIN_DB_NAME),
				parameter(DBType.BYTE, DATA_TYPE_DBNAME),
				parameter(DBType.LONG, LONG_DATA_DBNAME),
				parameter(DBType.DOUBLE, DOUBLE_DATA_DBNAME),
				parameter(DBType.STRING, VARCHAR_DATA_DBNAME),
				parameter(DBType.CLOB, CLOB_DATA_DBNAME),
				parameter(DBType.BLOB, BLOB_DATA_DBNAME)
				);
			SQLInsert insert = insert(table, columnNames, values);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				PARAM_BRANCH_IDX = parameters.size();
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			} else {
				PARAM_BRANCH_IDX = NO_BRANCH_COLUMN;
			}
			PARAM_TYPE_IDX = parameters.size();
			parameters.add(parameterDef(DBType.STRING, TYPE_DBNAME));
			PARAM_IDENTIFIER_IDX = parameters.size();
			parameters.add(parameterDef(DBType.ID, IDENTIFIER_DBNAME));
			PARAM_ATTRIBUTE_IDX = parameters.size();
			parameters.add(parameterDef(DBType.STRING, ATTRIBUTE_DBNAME));
			PARAM_REV_IDX = parameters.size();
			parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
			PARAM_DATA_TYPE_IDX = parameters.size();
			parameters.add(parameterDef(DBType.BYTE, DATA_TYPE_DBNAME));
			PARAM_LONG_DATA_IDX = parameters.size();
			parameters.add(parameterDef(DBType.LONG, LONG_DATA_DBNAME));
			PARAM_DOUBLE_DATA_IDX = parameters.size();
			parameters.add(parameterDef(DBType.DOUBLE, DOUBLE_DATA_DBNAME));
			PARAM_VARCHAR_DATA_IDX = parameters.size();
			parameters.add(parameterDef(DBType.STRING, VARCHAR_DATA_DBNAME));
			PARAM_CLOB_DATA_IDX = parameters.size();
			parameters.add(parameterDef(DBType.CLOB, CLOB_DATA_DBNAME));
			PARAM_BLOB_DATA_IDX = parameters.size();
			parameters.add(parameterDef(DBType.BLOB, BLOB_DATA_DBNAME));
			NUMBER_PARAMETERS = parameters.size();
			MAX_BATCH_SIZE = sqlDialect.getMaxBatchSize(NUMBER_PARAMETERS);

			this.statement = query(parameters, insert).toSql(sqlDialect);
		}

		public CompiledStatement statement() {
			return statement;
		}

		private Object[] newArguments() {
			return new Object[NUMBER_PARAMETERS];
		}

		public final void addAttribute(AddAttributeBatchCollector collector, long branch, long commitNumber,
				String type, TLID id, String attributeName, Object value) throws SQLException {
			Object[] args = newArguments();
			if (PARAM_BRANCH_IDX != NO_BRANCH_COLUMN) {
				args[PARAM_BRANCH_IDX] = branch;
			}
			args[PARAM_TYPE_IDX] = type;
			args[PARAM_IDENTIFIER_IDX] = id;
			args[PARAM_ATTRIBUTE_IDX] = attributeName;
			args[PARAM_REV_IDX] = commitNumber;

			setData(commitNumber, args,
				value,
				PARAM_DATA_TYPE_IDX,
				PARAM_LONG_DATA_IDX,
				PARAM_DOUBLE_DATA_IDX,
				PARAM_VARCHAR_DATA_IDX,
				PARAM_CLOB_DATA_IDX, PARAM_BLOB_DATA_IDX);

			collector.addAddAttributeBatch(args);
		}

		/**
		 * Determines the maximal possible numbers of batches for {@link #statement()}.
		 */
		public int maxBatchSize() {
			return MAX_BATCH_SIZE;
		}
	}

	/**
	 * The database abstraction layer.
	 */
    protected final DBHelper sqlDialect;

	private ConnectionPool connectionPool;
	private final DBAccess branchAccess;
	private final MOKnowledgeItemImpl dataType;

	private final GetHistoricAttributesStatement getHistoricAttributesStatement;

	private final GetBulkAttributesStatement getBulkAttributesStatement;

	/**
	 * Creates a {@link AbstractFlexDataManager}.
	 * 
	 * @param aPool used to fetch the actual Data.
	 */
	public AbstractFlexDataManager(ConnectionPool aPool, MOKnowledgeItemImpl dataType) {
        this.connectionPool = aPool;
        try {
			this.sqlDialect = connectionPool.getSQLDialect();
		} catch (SQLException ex) {
			throw new RuntimeException("Failed to access required database connection.");
		}
		this.branchAccess = new VersionedDBAccess(sqlDialect, dataType, BRANCH, IDENTIFIER, REV_MAX, REV_MIN);
		this.dataType = dataType;
		this.getHistoricAttributesStatement = new GetHistoricAttributesStatement(sqlDialect, dataType);
		this.getBulkAttributesStatement = new GetBulkAttributesStatement(sqlDialect, dataType, fetchSize);
	}
    
	@Override
	public final <T> void loadAll(long revision, AttributeLoader<T> callback,
			Mapping<? super T, ? extends ObjectKey> keyMapping, List<T> baseObjects, KnowledgeBase kb) {
		/* sort base objects by rev, branch, type, and name */
		baseObjects.sort(new MappedComparator<>(keyMapping, KeyOrder.INSTANCE));

		int count = baseObjects.size();
		KeyPartition partition = KeyPartition.INSTANCE;

		int maxChunkSize = sqlDialect.getMaxSetSize();
		List<TLID> bulkIds = new ArrayList<>(Math.min(count, maxChunkSize));

		int start = 0;
		while (start < count) {
			bulkIds.clear();

			T representativObject = baseObjects.get(start);
			ObjectKey representativeKey = keyMapping.map(representativObject);
			bulkIds.add(representativeKey.getObjectName());

			boolean needsCopy = false;
			int readIndex = start + 1;
			int stop = readIndex;
			TLID lastBaseObjectName = representativeKey.getObjectName();
			while (true) {
				if (readIndex >= count) {
					assert readIndex == count : "Counter stop is only incremented by one.";
					// last base item reached
					break;
				}
				T candidateObject = baseObjects.get(readIndex++);
				ObjectKey candidateKey = keyMapping.map(candidateObject);
				TLID candidateName = candidateKey.getObjectName();
				if (partition.compare(representativeKey, candidateKey) != 0) {
					/* read object is not yet used. Decrement counter to read again. */
					readIndex--;
					/* break when rev, branch, or type changes */
					break;
				}
				if (candidateName.equals(lastBaseObjectName)) {
					// Multiple loads of same object
					needsCopy = true;
					continue;
				}

				if (bulkIds.size() >= maxChunkSize) {
					/* ids are filled into an IN statement, so the maximum number of possible
					 * elements must not be exceeded */
					assert bulkIds.size() == maxChunkSize : "Bulk ids are enlarged by one element.";
					/* read object is not yet used. Decrement counter to read again. */
					readIndex--;
					break;
				}
				bulkIds.add(candidateName);
				if (needsCopy) {
					baseObjects.set(stop, candidateObject);
				}
				stop++;
				lastBaseObjectName = candidateName;
			}

			/* All elements have the same revision, branch, and type */
			List<T> bulkObjects = baseObjects.subList(start, stop);

			long branch = representativeKey.getBranchContext();
			long dataRevision = toDataRevision(kb, revision, representativeKey);
			MetaObject type = representativeKey.getObjectType();
			String typeName = type.getName();

			int retry = sqlDialect.retryCount();
			while (true) {
				PooledConnection readConnection = this.connectionPool.borrowReadConnection();
				try {
					GetBulkAttributesStatement.BulkAttributeResult res =
						this.getBulkAttributesStatement.query(connectionPool, readConnection, branch, typeName,
							bulkIds, dataRevision);
					try {
						Iterator<T> baseIt = bulkObjects.iterator();

						ImmutableFlexData flexData = null;
						TLID currentId = null;
						while (res.next()) {
							TLID nextId = res.getObjectName();
							if (!nextId.equals(currentId)) {
								if (currentId != null) {
									// Flush current data.
									flushData(dataRevision, callback, keyMapping, baseIt, flexData, currentId);
								}

								// Construct new data.
								currentId = nextId;
								flexData = new ImmutableFlexData();
							}

							assert flexData != null;
							String name = res.getAttributeName();
							long revMin = res.getRevMin();
							Object value = fetchValue(res);

							flexData.initAttributeValue(name, value, revMin);
						}

						// flush last data
						flushData(dataRevision, callback, keyMapping, baseIt, flexData, currentId);

						// skip objects without attributes
						while (baseIt.hasNext()) {
							T baseObject = baseIt.next();
							callback.loadEmpty(dataRevision, baseObject);
						}
					} finally {
						res.close();
					}
					break;
				} catch (SQLException sqx) {
					retry--;
					readConnection.closeConnection(sqx);
					if (retry < 0 || (!sqlDialect.canRetry(sqx))) {
						throw new RuntimeException(sqx);
					}
				} finally {
					connectionPool.releaseReadConnection(readConnection);
				}
			}

			start = readIndex;
		}
	}

	/**
	 * Computes the actual revision in which the data for an element with the given
	 * {@link ObjectKey} must be fetched, when the caller requests data in revision
	 * <code>requestedRevision</code>.
	 * 
	 * @implNote When the requested revision is different from
	 *           {@link AbstractDBKnowledgeItem#IN_SESSION_REVISION}, it will be used.
	 *           {@link Revision#CURRENT_REV} is not allowed, because the data are not stable. If
	 *           the given revision is {@link AbstractDBKnowledgeItem#IN_SESSION_REVISION}, then the
	 *           current session revision for "current objects" is returned, the revision of the
	 *           item for "historical objects".
	 * 
	 * @param requestedRevision
	 *        Explicitly requested data revision in
	 *        {@link #loadAll(long, AttributeLoader, Mapping, List, KnowledgeBase)}. Must not be
	 *        {@link Revision#CURRENT_REV}.
	 * @param item
	 *        {@link ObjectKey Key} of the object whose flex data must be loaded.
	 * 
	 * @return The actual data revision in which the data are fetched.
	 */
	private static long toDataRevision(KnowledgeBase kb, long requestedRevision, ObjectKey item) {
		assert requestedRevision != Revision.CURRENT_REV : "Must not load revision in current, because these data are not stable.";
		if (requestedRevision == AbstractDBKnowledgeItem.IN_SESSION_REVISION) {
			return dataRevisionForItem(kb, item);
		}
		return requestedRevision;
	}

	/**
	 * Computes the data revision for an item with the {@link ObjectKey}.
	 * 
	 * @param item
	 *        {@link KnowledgeItem#tId() Key} of the item to load.
	 * 
	 * @return The revision in which the data must be loaded.
	 */
	private static long dataRevisionForItem(KnowledgeBase kb, ObjectKey item) {
		return ((DBKnowledgeBase) kb).getDataRevision(item.getHistoryContext());
	}

	private <T> void flushData(long dataRevision, AttributeLoader<T> callback,
			Mapping<? super T, ? extends ObjectKey> keyMapping, Iterator<T> baseIt, FlexData flexData,
			TLID currentId) {
		while (baseIt.hasNext()) {
			T baseObject = baseIt.next();
			ObjectKey baseKey = keyMapping.map(baseObject);
			
			if (baseKey.getObjectName().equals(currentId)) {
				callback.loadData(dataRevision, baseObject, flexData);
				return;
			} else {
				callback.loadEmpty(dataRevision, baseObject);
			}
		}

		// Either the loaded data has been assigned, or no data was available at all.
		assert flexData == null : "Target '" + currentId
			+ "' for flush of data " + flexData + " not found (potential identifier order mismatch).";
	}

	@Override
	public FlexData load(KnowledgeBase kb, ObjectKey key, boolean mutable) {
		long dataRevision = dataRevisionForItem(kb, key);
		return load(kb, key, dataRevision, mutable);
	}

	@Override
	public FlexData load(KnowledgeBase kb, ObjectKey key, long dataRevision, boolean mutable) {
		long branch = key.getBranchContext();
		String type = key.getObjectType().getName();
		TLID id = key.getObjectName();

		int retry = sqlDialect.retryCount();
		while (true) {
			CommitContext context = KBUtils.getCurrentContext(kb);
			PooledConnection readConnection;
			boolean borrowConnection = context == null || !context.transactionStarted();
			if (borrowConnection) {
				readConnection = this.connectionPool.borrowReadConnection();
			} else {
				@SuppressWarnings("null")
				PooledConnection commitConnection = context.getConnection();
				readConnection = commitConnection;
			}
			try {
				AttributeResult res =
					getHistoricAttributesStatement.query(connectionPool, readConnection, branch, type, id,
						dataRevision);
				try {
					if (res.next()) {
						AbstractFlexData flexData;
						if (mutable) {
							flexData = new MutableFlexData();
						} else {
							flexData = new ImmutableFlexData();
						}
						do {
							String name = res.getAttributeName();
							long revMin = res.getRevMin();
							Object value = fetchValue(res);
							flexData.initAttributeValue(name, value, revMin);
						} while (res.next());
						return flexData;
					} else {
						if (mutable) {
							return new MutableFlexData();
						} else {
							return NoFlexData.INSTANCE;
						}
					}
				} finally {
					res.close();
				}
			} catch (SQLException sqx) {
				retry--;
				readConnection.closeConnection(sqx);
				if (retry < 0 || (! sqlDialect.canRetry(sqx))) {
					throw new RuntimeException(sqx);
				}
			} finally {
				if (borrowConnection) {
					connectionPool.releaseReadConnection(readConnection);
				}
			}
		}
	}

	public static Object fetchValue(AttributeResult resultSet) throws SQLException {
		byte dataType = resultSet.getDataType();
		switch (dataType) {
		case STRING_TYPE: {
			return resultSet.getVarcharData();
		}
		case EMPTY_STRING_TYPE: {
			return "";
		}
		case CLOB_TYPE: {
			return resultSet.getClobData();  
		}
        case BLOB_TYPE: {
            return resultSet.getBlobData();  
        }
		case INTEGER_TYPE: {
			return Integer.valueOf(((int) resultSet.getLongData()));
		}
		case LONG_TYPE: {
			return Long.valueOf(resultSet.getLongData());
		}
		case DATE_TYPE: {
			long longValue = resultSet.getLongData();
			return new Date(longValue);
		}
		case FLOAT_TYPE: {
				return Float.valueOf((float) resultSet.getDoubleData());
		}
		case DOUBLE_TYPE: {
				return Double.valueOf(resultSet.getDoubleData());
		}
		case BOOLEAN_TRUE: {
			return Boolean.TRUE;
		}
		case BOOLEAN_FALSE: {
			return Boolean.FALSE;
		}
			case TL_ID_TYPE: {
				if (IdentifierUtil.SHORT_IDS) {
					long longData = resultSet.getLongData();
					if (longData != 0) {
						return LongID.valueOf(longData);
					} else {
						/* Either a StringID is stored or a LongID for object with id 0 which is
						 * unlikely but not impossible. */
						String varcharData = resultSet.getVarcharData();
						if (varcharData != null) {
							return StringID.valueOf(varcharData);
						} else {
							return LongID.valueOf(0);
						}
					}
				} else {
					String varcharData = resultSet.getVarcharData();
					if (varcharData != null) {
						return StringID.valueOf(varcharData);
					} else {
						return LongID.valueOf(resultSet.getLongData());
					}
				}
			}
			case EXT_ID_TYPE: {
				long systemId = resultSet.getLongData();
				String objectIdString = resultSet.getVarcharData();
				long objectId = Long.parseLong(objectIdString, Character.MAX_RADIX);
				return new ExtID(systemId, objectId);
			}
		default:
			throw new UnreachableAssertion("Unknown data type '" + dataType + "'");
		}
	}

	@Override
	public void addAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		updateAll(items, context);
	}

	@Override
	public void updateAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		for (DBKnowledgeItem item : items) {
			FlexData dynamicValues = item.getLocalDynamicValues(context);
			if (dynamicValues != null) {
				internalStore(item.tId(), dynamicValues, context);
			}
		}
	}

	@Override
	public boolean store(ObjectKey key, FlexData flexData, CommitContext context) {
		try {
			internalStore(key, flexData, context);
			return true;
		} catch (SQLException ex) {
			Logger.error("Failed to store flex data for '" + key + "'.", ex, FlexVersionedDataManager.class);
			return false;
		}
	}
	
	/**
	 * {@link #store(ObjectKey, FlexData, CommitContext)} implementation.
	 */
	protected abstract void internalStore(ObjectKey key, FlexData data, CommitContext context) throws SQLException;
	
    /**
	 * Inserts the given data value and its concrete type description into the given array.
	 * 
	 * @param commitNumber
	 *        Commit number of the current commit.
	 * @param args
	 *        The arguments the data value should be inserted to.
	 * @param value
	 *        The data value to insert.
	 * @param dataTypeIdx
	 *        The statement index, where the data type is stored.
	 * @param longDataIdx
	 *        The statement index, where long data is stored.
	 * @param doubleDataIdx
	 *        The statement index, where double data is stored.
	 * @param varcharDataIdx
	 *        The statement index, where varchar data is stored.
	 * @param clobDataIdx
	 *        The statement index, where clob data is stored.
	 */
	public static final void setData(long commitNumber, Object[] args, Object value, int dataTypeIdx,
			int longDataIdx, int doubleDataIdx, int varcharDataIdx, int clobDataIdx, int blobDataIndex) throws SQLException {
		if (value instanceof String) {
			String s = (String) value;
			args[longDataIdx] = null;
			args[doubleDataIdx] = null;
			int dataLength = s.length();
			if (dataLength == 0) {
				args[dataTypeIdx] = EMPTY_STRING_TYPE;

				args[varcharDataIdx] = null;
				args[clobDataIdx] = null;
			} else if (dataLength < VARCHAR_DATA_ATTR_LEN) {
				args[dataTypeIdx] = STRING_TYPE;

				args[varcharDataIdx] = s;
				args[clobDataIdx] = null;
			} else {
				args[dataTypeIdx] = CLOB_TYPE;

				args[varcharDataIdx] = null;
				args[clobDataIdx] = s;
			}
			args[blobDataIndex] = null;
		} else if (value instanceof Number) {
			if (value instanceof Integer) {
				args[dataTypeIdx] = INTEGER_TYPE;

				args[longDataIdx] = Long.valueOf(((Number) value).longValue());
				args[doubleDataIdx] = null;
			} else if (value instanceof Double) {
				args[dataTypeIdx] = DOUBLE_TYPE;

				args[longDataIdx] = null;
				args[doubleDataIdx] = value;
			} else if (value instanceof Float) {
				args[dataTypeIdx] = FLOAT_TYPE;

				args[longDataIdx] = null;
				args[doubleDataIdx] = Double.valueOf(((Number) value).floatValue());
			} else if (value instanceof Long) {
				args[dataTypeIdx] = LONG_TYPE;

				args[longDataIdx] = value;
				args[doubleDataIdx] = null;
			}
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
		} else if (value instanceof Date) {
			args[dataTypeIdx] = DATE_TYPE;

			args[longDataIdx] = ((Date) value).getTime();
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
		} else if (value instanceof Boolean) {
			args[dataTypeIdx] = DATE_TYPE;
			if (((Boolean) value).booleanValue()) {
				args[dataTypeIdx] = BOOLEAN_TRUE;
			} else {
				args[dataTypeIdx] = BOOLEAN_FALSE;
			}

			args[longDataIdx] = null;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
		} else if (value instanceof BinaryDataSource) {
			args[dataTypeIdx] = BLOB_TYPE;

			BinaryDataSource bd = (BinaryDataSource) value;
			long size = bd.getSize();
			args[longDataIdx] = size;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = bd.getContentType();
			String name = bd.getName();
			if (!BinaryData.NO_NAME.equals(name)) {
				args[clobDataIdx] = name;
			} else {
				args[clobDataIdx] = null;
			}
			args[blobDataIndex] = bd;
		} else if (value instanceof TLID) {
			args[dataTypeIdx] = TL_ID_TYPE;

			args[doubleDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
			TLID tlId = (TLID) value;
			if (tlId instanceof LongID) {
				args[longDataIdx] = ((LongID) tlId).longValue();
				args[varcharDataIdx] = null;
			} else if (tlId instanceof StringID) {
				args[longDataIdx] = null;
				args[varcharDataIdx] = ((StringID) tlId).stringValue();
			} else {
				args[longDataIdx] = null;
				args[varcharDataIdx] = null;
				throw new SQLException("Dont know how to store a TLID of type " + value.getClass());
			}
		} else if (value instanceof ExtID) {
			args[dataTypeIdx] = EXT_ID_TYPE;

			args[doubleDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
			ExtID extId = (ExtID) value;
			args[longDataIdx] = extId.systemId();
			args[varcharDataIdx] = Long.toString(extId.objectId(), Character.MAX_RADIX);
		} else if (value == NextCommitNumberFuture.INSTANCE) {
			args[dataTypeIdx] = LONG_TYPE;
			args[longDataIdx] = commitNumber;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
		} else {
			args[longDataIdx] = null;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
			args[blobDataIndex] = null;
			if (value != null) {
				throw new SQLException("Cannot store values of type '" + value.getClass() + "' to flex data.");
			} else {
				throw new IllegalArgumentException("Must not try to store null to flex data.");
			}
		}
	}
	
	@Override
	public final boolean delete(ObjectKey key, CommitContext context) {
		try {
			boolean result = internalDelete(key, context);
			return result;
		} catch (SQLException ex) {
            Logger.error("Failed to delete data for ''.", ex, FlexVersionedDataManager.class);
            return false;
		}
	}

	/**
	 * {@link #delete(ObjectKey, CommitContext)} implementation.
	 */
	protected abstract boolean internalDelete(ObjectKey key, CommitContext context) throws SQLException;

	@Override
	public final void branch(PooledConnection context, long branchId, long createRev, long baseBranchId,
			long baseRevision, Collection<String> branchedTypNames) throws SQLException {
		SQLExpression filterExpr;
		if (!CollectionUtil.isEmptyOrNull(branchedTypNames)) {
			try {
				DBAttribute typeAttribute = (DBAttribute) dataType.getAttribute(TYPE);
				SQLExpression column = column(NO_TABLE_ALIAS, typeAttribute, true);
				filterExpr = inSet(column, branchedTypNames, typeAttribute.getSQLType());
			} catch (NoSuchAttributeException ex) {
				throw new RuntimeException(ex);
			}
		} else {
			filterExpr = literalTrueLogical();
		}
		branchAccess.branch(context, branchId, createRev, baseBranchId, baseRevision, filterExpr);
	}

	/**
	 * {@link ItemQuery} that reports attribute value assignments managed by the
	 * {@link FlexDataManager}.
	 * 
	 * @see AttributeItemResult
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class AttributeItemQuery extends MultipleItemQuery {

		/**
		 * Creates a {@link AttributeItemQuery}.
		 */
		public AttributeItemQuery(DBHelper sqlDialect, MOClass type, String tableAlias, SQLExpression[] filter,
				DBAttribute[] order, boolean[] descending) {
			super(sqlDialect, type, tableAlias, filter, order, descending);
		}
	
		@Override
		public AttributeItemResult query(Connection context) throws SQLException {
			return new AttributeItemResult(getType(), super.query(context));
		}

		/**
		 * {@link KnowledgeItemResult} that is able to decode the attribute
		 * value of a reported flexible attribute value assignment.
		 * 
		 * @see #getAttributeValue()
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public static class AttributeItemResult extends ItemResultAdapter implements KnowledgeItemResult, AttributeResult {
			
			private ItemResult result;

			private final MOClass dataType;

			public AttributeItemResult(MOClass dataType, ItemResult result) {
				assert dataType != null;

				this.result = result;
				this.dataType = dataType;
			}
		
			@Override
			protected ItemResult getImplementation() {
				return result;
			}
			
			@Override
			public final long getBranch() throws SQLException {
				if (dataType.getDBMapping().multipleBranches()) {
					return getImplementation().getLongValue(getAttribute(dataType, BRANCH));
				} else {
					return TLContext.TRUNK_ID;
				}
			}
			
			@Override
			public final String getTypeName() throws SQLException {
				return getImplementation().getStringValue(getAttribute(dataType, TYPE));
			}
			
			@Override
			public final TLID getIdentifier() throws SQLException {
				return getImplementation().getIDValue(getAttribute(dataType, IDENTIFIER));
			}
			
			@Override
			public final long getRevMax() throws SQLException {
				return getImplementation().getLongValue(getAttribute(dataType, REV_MAX));
			}
			
			@Override
			public final long getRevMin() throws SQLException {
				return getImplementation().getLongValue(getAttribute(dataType, REV_MIN));
			}
			
			@Override
			public final String getAttributeName() throws SQLException {
				return getImplementation().getStringValue(getAttribute(dataType, ATTRIBUTE));
			}
		
			@Override
			public final byte getDataType() throws SQLException {
				return getImplementation().getByteValue(getAttribute(dataType, DATA_TYPE));
			}
		
			@Override
			public final double getDoubleData() throws SQLException {
				return getImplementation().getDoubleValue(getAttribute(dataType, DOUBLE_DATA));
			}
		
			@Override
			public final long getLongData() throws SQLException {
				return getImplementation().getLongValue(getAttribute(dataType, LONG_DATA));
			}
		
			@Override
			public final String getVarcharData() throws SQLException {
				return getImplementation().getStringValue(getAttribute(dataType, VARCHAR_DATA));
			}
		
			@Override
			public final String getClobData() throws SQLException {
				return getImplementation().getClobStringValue(getAttribute(dataType, CLOB_DATA));
			}
			
			@Override
			public BinaryData getBlobData() throws SQLException {
				return getImplementation().getBlobValue(
					getAttribute(dataType, CLOB_DATA),
					getAttribute(dataType, VARCHAR_DATA),
					getAttribute(dataType, LONG_DATA),
					getAttribute(dataType, BLOB_DATA));
			}

			/**
			 * The decoded value of the attribute that is represented by the
			 * current row.
			 */
			public Object getAttributeValue() throws SQLException {
				return AbstractFlexDataManager.fetchValue(this);
			}
		}
	}

	/**
	 * The DB column of an attribute.
	 */
	public static DBAttribute getDBAttribute(MOKnowledgeItemImpl dataType, String attributeName) {
		return (DBAttribute) dataType.getAttributeOrNull(attributeName);
	}

	public static MOAttributeImpl getAttribute(MOClass dataType, String attributeName) {
		try {
			return (MOAttributeImpl) dataType.getAttribute(attributeName);
		} catch (NoSuchAttributeException e) {
			throw new RuntimeException("Excepted " + attributeName + " to be an attribute of " + dataType.getName(), e);
		}
	}
	
}
