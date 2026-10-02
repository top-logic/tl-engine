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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
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
import com.top_logic.basic.io.binary.BinaryDataSource;
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
import com.top_logic.dob.attr.HybridBinaryAttribute;
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
import com.top_logic.knowledge.service.BinaryStorageSettings;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.DynamicBinaryStoragePolicy;
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
		return createFlexType(typeName, dbName, multipleBranches, false);
	}

	/**
	 * Name of the {@link KnowledgeItem} type that stores the binary values of dynamic attributes.
	 */
	public static final String FLEX_BINARY_DATA = "FlexBinaryData";

	/**
	 * Database table name of {@link #FLEX_BINARY_DATA}.
	 */
	public static final String FLEX_BINARY_DATA_DB_NAME = "FLEX_BINARY";

	/**
	 * Name of the {@link HybridBinaryAttribute} of {@link #FLEX_BINARY_DATA} storing the value.
	 * 
	 * <p>
	 * The attribute has a store column, so that each row names the blob store holding its content.
	 * </p>
	 */
	public static final String CONTENT = "content";

	/**
	 * Create {@link KnowledgeItem} type that stores the binary values of dynamic attributes of
	 * {@link KnowledgeObject}s.
	 * 
	 * <p>
	 * The type has the same key columns as a {@link #createFlexDataType(String, String, boolean)
	 * flex data type} and stores the value in a {@link HybridBinaryAttribute} {@link #CONTENT}.
	 * </p>
	 * 
	 * @param typeName
	 *        The internal name of the type.
	 * @param dbName
	 *        The name of the corresponding database table.
	 * @param multipleBranches
	 *        Whether multiple branches are supported
	 */
	public static MOKnowledgeItemImpl createFlexBinaryDataType(String typeName, String dbName,
			boolean multipleBranches) {
		return createFlexType(typeName, dbName, multipleBranches, true);
	}

	private static HybridBinaryAttribute createContentAttr() {
		HybridBinaryAttribute.Config config = TypedConfiguration.newConfigItem(HybridBinaryAttribute.Config.class);
		config.setAttributeName(CONTENT);
		config.setStoreColumn(true);
		return new HybridBinaryAttribute(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private static MOKnowledgeItemImpl createFlexType(String typeName, String dbName, boolean multipleBranches,
			boolean binary) {
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
			if (binary) {
				type.addAttribute(createContentAttr());
			} else {
				type.addAttribute(createDataTypeAttr());

				type.addAttribute(createLongTypeAttr());
				type.addAttribute(createDoubleDataAttr());
				type.addAttribute(createVarcharDataAttr());
				type.addAttribute(createClobDataAttr());
			}
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}

		// Note: Even if it sounds appealing to move the REV_MAX column more to the start of the
		// index to bring rows for the current revision more close together, this is not advisable,
		// since optimizers of some DBs (including PostgreSQL) then do not consider this index for
		// the most frequent queries for attribute load. Those queries are typically structured like
		// this:

		// select * from FLEX_DATA
		// where branch=?
		// and type=?
		// and identifier=?
		// and rev_min <= [rev]
		// and rev_max >= [rev]

		// Here, the REV_MAX column only occurs in a range condition. Even if this is no problem for
		// query execution, since the range most likely only consists of a single value
		// Long.MAX_VALUE, this may prevent the optimizer from using this index at all preferring a
		// full-table-scan. This results in catastrophic performance degradation for non-trivial
		// datasets.
		DBAttribute[] primaryKeyColumns =
			BasicTypeProvider.primaryKeyColumns(branchAttribute, typeAttribute, idAttribute, revMaxAttribute,
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
	 * Interface to access the actually coded flex data value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface FlexDataValue {

		/**
		 * The encoding of the type of the value and the column that contains the data.
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

	}

	/**
	 * {@link QueryResult} interface this {@link FlexDataManager} is able to
	 * retrieve attribute values from.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface AttributeResult extends QueryResult, FlexDataValue {

		/**
		 * The name of the attribute this row contains the value.
		 */
		String getAttributeName() throws SQLException;

		/**
		 * The minimum revision from which the row is valid.
		 */
		long getRevMin() throws SQLException;

    }
    
	static abstract class AttributeResultSetWrapper extends ResultSetWrapper implements AttributeResult {

		private final ConnectionPool _basePool;

		public AttributeResultSetWrapper(ConnectionPool aPool, ResultSet result) {
			super(result);
			_basePool = aPool;
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
				return new HistoricAttributeResult(basePool, result, id);
			} else {
				ResultSet result = statement.executeQuery(connection, type, id, commitNumber);
				return new HistoricAttributeResult(basePool, result, id);
			}
    	}
    	
		public final class HistoricAttributeResult extends AttributeResultSetWrapper {

			private TLID _id;

			public HistoricAttributeResult(ConnectionPool aPool, ResultSet result, TLID anId) {
				super(aPool, result);
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
			protected TLID getObjectName() throws SQLException {
				return _id;
			}

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
				List<SQLOrder> order = orders(order(column(tableAlias, IDENTIFIER_DBNAME)));
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
				return new BulkAttributeResult(basePool, resultSet);
			} else {
				ResultSet resultSet = statement.executeQuery(connection, type, ids, commitNumber);
				return new BulkAttributeResult(basePool, resultSet);
			}
		}

		public final class BulkAttributeResult extends AttributeResultSetWrapper {

			public BulkAttributeResult(ConnectionPool aPool, ResultSet resultSet) {
				super(aPool, resultSet);
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
				CLOB_DATA_DBNAME
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
				parameter(DBType.CLOB, CLOB_DATA_DBNAME)
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
				PARAM_CLOB_DATA_IDX);

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

	private final DBAccess _binaryBranchAccess;

	/**
	 * Access to the table of binary values.
	 */
	protected final FlexBinaryAccess _binaryAccess;

	private final BinaryStorageSettings _binaryDefaults;

	private final DynamicBinaryStoragePolicy _binaryStoragePolicy;

	/**
	 * Creates a {@link AbstractFlexDataManager}.
	 * 
	 * @param aPool
	 *        Used to fetch the actual Data.
	 * @param dataType
	 *        The type of the table storing all values except binary ones, see
	 *        {@link #createFlexDataType(String, String, boolean)}.
	 * @param binaryDataType
	 *        The type of the table storing binary values, see
	 *        {@link #createFlexBinaryDataType(String, String, boolean)}.
	 * @param binaryDefaults
	 *        The storage settings for binary values, if the policy chooses no other.
	 * @param binaryStoragePolicy
	 *        The policy choosing the storage settings for a binary value.
	 */
	public AbstractFlexDataManager(ConnectionPool aPool, MOKnowledgeItemImpl dataType,
			MOKnowledgeItemImpl binaryDataType, BinaryStorageSettings binaryDefaults,
			DynamicBinaryStoragePolicy binaryStoragePolicy) {
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
		_binaryBranchAccess =
			new VersionedDBAccess(sqlDialect, binaryDataType, BRANCH, IDENTIFIER, REV_MAX, REV_MIN);
		_binaryAccess = new FlexBinaryAccess(aPool, sqlDialect, binaryDataType, fetchSize);
		_binaryDefaults = binaryDefaults;
		_binaryStoragePolicy = binaryStoragePolicy;
	}

	/**
	 * Whether the given value is stored in the table of binary values.
	 */
	protected static boolean isBinary(Object value) {
		return value instanceof BinaryDataSource;
	}

	/**
	 * Keeps the content inline or uploads it to the blob store, as decided by the
	 * {@link BinaryStorageSettings} chosen by the {@link DynamicBinaryStoragePolicy}.
	 */
	@Override
	public BinaryData toStoredValue(KnowledgeItem item, String attribute, BinaryData value) throws IOException {
		BinaryStorageSettings settings = _binaryStoragePolicy.forValue(item, attribute, _binaryDefaults);
		return settings.toStoredValue(value);
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
					Map<TLID, ImmutableFlexData> dataById = new HashMap<>();
					try (GetBulkAttributesStatement.BulkAttributeResult res =
						this.getBulkAttributesStatement.query(connectionPool, readConnection, branch, typeName,
							bulkIds, dataRevision)) {
						while (res.next()) {
							TLID id = res.getObjectName();
							String name = res.getAttributeName();
							long revMin = res.getRevMin();
							Object value = fetchValue(res);
							dataById.computeIfAbsent(id, x -> new ImmutableFlexData())
								.initAttributeValue(name, value, revMin);
						}
					}
					_binaryAccess.loadAllValues(readConnection, branch, typeName, bulkIds, dataRevision,
						(id, name, value, revMin) -> dataById.computeIfAbsent(id, x -> new ImmutableFlexData())
							.initAttributeValue(name, value, revMin));

					for (T baseObject : bulkObjects) {
						FlexData flexData = dataById.get(keyMapping.map(baseObject).getObjectName());
						if (flexData != null) {
							callback.loadData(dataRevision, baseObject, flexData);
						} else {
							callback.loadEmpty(dataRevision, baseObject);
						}
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
			try (AttributeResult res =
				getHistoricAttributesStatement.query(connectionPool, readConnection, branch, type, id,
					dataRevision)) {
				AbstractFlexData flexData;
				if (mutable) {
					flexData = new MutableFlexData();
				} else {
					flexData = new ImmutableFlexData();
				}
				while (res.next()) {
					String name = res.getAttributeName();
					long revMin = res.getRevMin();
					Object value = fetchValue(res);
					flexData.initAttributeValue(name, value, revMin);
				}
				_binaryAccess.loadValues(readConnection, branch, type, id, dataRevision,
					(valueId, name, value, revMin) -> flexData.initAttributeValue(name, value, revMin));
				if (!mutable && flexData.getAttributes().isEmpty()) {
					return NoFlexData.INSTANCE;
				}
				return flexData;
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

	/**
	 * Fetches the value from the given {@link FlexDataValue}.
	 */
	public static Object fetchValue(FlexDataValue resultSet) throws SQLException {
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
			int longDataIdx, int doubleDataIdx, int varcharDataIdx, int clobDataIdx) throws SQLException {
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
		} else if (value instanceof Date) {
			args[dataTypeIdx] = DATE_TYPE;

			args[longDataIdx] = ((Date) value).getTime();
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
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
		} else if (value instanceof TLID) {
			args[dataTypeIdx] = TL_ID_TYPE;

			args[doubleDataIdx] = null;
			args[clobDataIdx] = null;
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
			ExtID extId = (ExtID) value;
			args[longDataIdx] = extId.systemId();
			args[varcharDataIdx] = Long.toString(extId.objectId(), Character.MAX_RADIX);
		} else if (value == NextCommitNumberFuture.INSTANCE) {
			args[dataTypeIdx] = LONG_TYPE;
			args[longDataIdx] = commitNumber;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
		} else {
			args[longDataIdx] = null;
			args[doubleDataIdx] = null;
			args[varcharDataIdx] = null;
			args[clobDataIdx] = null;
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
		branchAccess.branch(context, branchId, createRev, baseBranchId, baseRevision,
			typeFilter(dataType, branchedTypNames));
		_binaryBranchAccess.branch(context, branchId, createRev, baseBranchId, baseRevision,
			typeFilter(_binaryAccess.getType(), branchedTypNames));
	}

	private static SQLExpression typeFilter(MOKnowledgeItemImpl table, Collection<String> typeNames) {
		if (CollectionUtil.isEmptyOrNull(typeNames)) {
			return literalTrueLogical();
		}
		try {
			DBAttribute typeAttribute = (DBAttribute) table.getAttribute(TYPE);
			SQLExpression column = column(NO_TABLE_ALIAS, typeAttribute, true);
			return inSet(column, typeNames, typeAttribute.getSQLType());
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Result reporting rows of attribute value assignments managed by the {@link FlexDataManager}.
	 * 
	 * @see AttributeItemQuery
	 * @see BinaryAttributeItemQuery
	 */
	public interface AttributeValueResult extends KnowledgeItemResult {

		/**
		 * The name of the attribute whose value is reported by the current row.
		 */
		String getAttributeName() throws SQLException;

		/**
		 * The value of the attribute that is represented by the current row.
		 */
		Object getAttributeValue() throws SQLException;

	}

	/**
	 * {@link ItemQuery} that reports binary attribute value assignments managed by the
	 * {@link FlexDataManager}.
	 * 
	 * @see #createFlexBinaryDataType(String, String, boolean)
	 */
	public static class BinaryAttributeItemQuery extends MultipleItemQuery {

		/**
		 * Creates a {@link BinaryAttributeItemQuery}.
		 */
		public BinaryAttributeItemQuery(DBHelper sqlDialect, MOClass type, String tableAlias,
				SQLExpression[] filter, DBAttribute[] order, boolean[] descending) {
			super(sqlDialect, type, tableAlias, filter, order, descending);
		}

		@Override
		public BinaryAttributeItemResult query(Connection context) throws SQLException {
			return new BinaryAttributeItemResult(getType(), super.query(context));
		}

		/**
		 * {@link AttributeValueResult} of a {@link BinaryAttributeItemQuery}.
		 */
		public static class BinaryAttributeItemResult extends ItemResultAdapter implements AttributeValueResult {

			private final ItemResult _result;

			private final MOClass _dataType;

			private final MOAttribute _content;

			/**
			 * Creates a {@link BinaryAttributeItemResult}.
			 */
			public BinaryAttributeItemResult(MOClass dataType, ItemResult result) {
				_result = result;
				_dataType = dataType;
				_content = dataType.getAttributeOrNull(CONTENT);
			}

			@Override
			protected ItemResult getImplementation() {
				return _result;
			}

			@Override
			public final long getBranch() throws SQLException {
				if (_dataType.getDBMapping().multipleBranches()) {
					return getImplementation().getLongValue(getAttribute(_dataType, BRANCH));
				} else {
					return TLContext.TRUNK_ID;
				}
			}

			@Override
			public final String getTypeName() throws SQLException {
				return getImplementation().getStringValue(getAttribute(_dataType, TYPE));
			}

			@Override
			public final TLID getIdentifier() throws SQLException {
				return getImplementation().getIDValue(getAttribute(_dataType, IDENTIFIER));
			}

			@Override
			public final long getRevMax() throws SQLException {
				return getImplementation().getLongValue(getAttribute(_dataType, REV_MAX));
			}

			@Override
			public final long getRevMin() throws SQLException {
				return getImplementation().getLongValue(getAttribute(_dataType, REV_MIN));
			}

			@Override
			public final String getAttributeName() throws SQLException {
				return getImplementation().getStringValue(getAttribute(_dataType, ATTRIBUTE));
			}

			@Override
			public Object getAttributeValue() throws SQLException {
				return getImplementation().getValue(_content, null);
			}
		}
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
		public static class AttributeItemResult extends ItemResultAdapter
				implements KnowledgeItemResult, AttributeResult, AttributeValueResult {
			
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
			
			/**
			 * The decoded value of the attribute that is represented by the
			 * current row.
			 */
			@Override
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
