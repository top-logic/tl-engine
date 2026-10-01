/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.storage.BinaryColumnsStorage;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.SimpleDBAttribute;

/**
 * Base class for binary attributes that store a {@link BinaryData} value in a fixed set of
 * columns.
 *
 * <p>
 * Besides the content (inline in a BLOB column, or as reference to a blob store), every kind stores
 * size, content type and name of the value, so that a value read from the database has the same
 * metadata as the value that was stored.
 * </p>
 *
 * <p>
 * The columns are named after the database name of the attribute with a suffix.
 * </p>
 *
 * @implNote Column suffixes: {@link #SUFFIX_KEY} (key of the blob in the blob store),
 *           {@link #SUFFIX_HASH} (SHA-256 hash of the content of the blob), {@link #SUFFIX_SIZE}
 *           (size of the content in bytes), {@link #SUFFIX_CONTENT_TYPE} (content type),
 *           {@link #SUFFIX_NAME} (name), {@link #SUFFIX_DATA} (content stored inline).
 *
 * @see InlineBinaryAttribute
 * @see RefBinaryAttribute
 * @see HybridBinaryAttribute
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBinaryAttribute extends AbstractMOAttribute {

	/** Column suffix of the column holding the content inline. */
	public static final String SUFFIX_DATA = "_DATA";

	/** Column suffix of the column holding the key of the blob. */
	public static final String SUFFIX_KEY = "_KEY";

	/** Column suffix of the column holding the SHA-256 hash of the blob content. */
	public static final String SUFFIX_HASH = "_HASH";

	/** Column suffix of the column holding the size of the content. */
	public static final String SUFFIX_SIZE = "_SIZE";

	/** Column suffix of the column holding the content type. */
	public static final String SUFFIX_CONTENT_TYPE = "_TYPE";

	/** Column suffix of the column holding the name. */
	public static final String SUFFIX_NAME = "_NAME";

	/** Size of the {@link #SUFFIX_KEY} column. */
	public static final int KEY_COLUMN_SIZE = 128;

	/** Size of the {@link #SUFFIX_HASH} column: a SHA-256 hash as hex string. */
	public static final int HASH_COLUMN_SIZE = 64;

	/** Size of the {@link #SUFFIX_CONTENT_TYPE} column. */
	public static final int CONTENT_TYPE_COLUMN_SIZE = 255;

	/** Size of the {@link #SUFFIX_NAME} column. */
	public static final int NAME_COLUMN_SIZE = 1024;

	/**
	 * Configuration of an {@link AbstractBinaryAttribute}.
	 */
	@Abstract
	public interface Config extends PrimitiveAttributeConfig {

		/** Name of the {@link MOPrimitive} type of the values of all binary attributes. */
		String BLOB_TYPE_NAME = "Blob";

		/**
		 * The type of the values, always the {@link MOPrimitive} {@value #BLOB_TYPE_NAME}.
		 */
		@Override
		@FormattedDefault(BLOB_TYPE_NAME)
		MetaObject getValueType();

		@Override
		@InstanceDefault(BinaryColumnsStorage.class)
		AttributeStorage getStorage();

	}

	/**
	 * Configuration of an {@link AbstractBinaryAttribute} storing content in a blob store.
	 */
	@Abstract
	public interface ExternalConfig extends Config {

		/** Configuration name of {@link #getStore()}. */
		String STORE = "store";

		/**
		 * The name of the blob store the content is stored in.
		 *
		 * <p>
		 * The name refers to a store configured in the blob store service. If no store is given,
		 * the default store of the service is used. Changing the store of an existing attribute
		 * requires moving its stored content to the new store.
		 * </p>
		 */
		@Name(STORE)
		@Nullable
		String getStore();

		/**
		 * @see #getStore()
		 */
		void setStore(String value);

	}

	private AttributeStorage _storage;

	private final String _dbBaseName;

	private final DBAttribute _keyColumn;

	private final DBAttribute _hashColumn;

	private final DBAttribute _sizeColumn;

	private final DBAttribute _contentTypeColumn;

	private final DBAttribute _nameColumn;

	private final DBAttribute _dataColumn;

	private final DBAttribute[] _dbMapping;

	/**
	 * Creates an {@link AbstractBinaryAttribute} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 * @param inline
	 *        Whether the attribute has a column for inline content.
	 * @param external
	 *        Whether the attribute has columns for references to blobs.
	 */
	protected AbstractBinaryAttribute(InstantiationContext context, Config config, boolean inline,
			boolean external) {
		super(context, config);
		setStorage(config.getStorage());
		setMetaObject(config.getValueType());

		_dbBaseName = config.getDBNameEffective();
		_keyColumn = external ? keyColumn(inline) : null;
		_hashColumn = external ? hashColumn(inline) : null;
		_sizeColumn = sizeColumn();
		_contentTypeColumn = contentTypeColumn();
		_nameColumn = nameColumn();
		_dataColumn = inline ? dataColumn(external) : null;
		_dbMapping = dbMapping();
	}

	/**
	 * Creates a copy of the given attribute.
	 *
	 * @param name
	 *        The name of the copy.
	 * @param type
	 *        The value type of the copy.
	 * @param orig
	 *        The attribute to copy.
	 */
	protected AbstractBinaryAttribute(String name, MetaObject type, AbstractBinaryAttribute orig) {
		super(name, type);
		initFrom(orig);
		setStorage(orig.getStorage());

		_dbBaseName = name.equals(orig.getName()) ? orig._dbBaseName : SQLH.mangleDBName(name);
		boolean external = orig._keyColumn != null;
		boolean inline = orig._dataColumn != null;
		_keyColumn = external ? keyColumn(inline) : null;
		_hashColumn = external ? hashColumn(inline) : null;
		_sizeColumn = sizeColumn();
		_contentTypeColumn = contentTypeColumn();
		_nameColumn = nameColumn();
		_dataColumn = inline ? dataColumn(external) : null;
		_dbMapping = dbMapping();
	}

	private DBAttribute keyColumn(boolean inline) {
		return new SimpleDBAttribute(this, MOPrimitive.STRING, _dbBaseName + SUFFIX_KEY, KEY_COLUMN_SIZE, true,
			isMandatory() && !inline);
	}

	private DBAttribute hashColumn(boolean inline) {
		return new SimpleDBAttribute(this, MOPrimitive.STRING, _dbBaseName + SUFFIX_HASH, HASH_COLUMN_SIZE, true,
			isMandatory() && !inline);
	}

	private DBAttribute sizeColumn() {
		return new SimpleDBAttribute(this, MOPrimitive.LONG, _dbBaseName + SUFFIX_SIZE, -1, false, isMandatory());
	}

	private DBAttribute contentTypeColumn() {
		return new SimpleDBAttribute(this, MOPrimitive.STRING, _dbBaseName + SUFFIX_CONTENT_TYPE,
			CONTENT_TYPE_COLUMN_SIZE, false, isMandatory());
	}

	private DBAttribute nameColumn() {
		// An empty name is stored as null.
		return new SimpleDBAttribute(this, MOPrimitive.STRING, _dbBaseName + SUFFIX_NAME, NAME_COLUMN_SIZE, false,
			false);
	}

	private DBAttribute dataColumn(boolean external) {
		return new SimpleDBAttribute(this, MOPrimitive.BLOB, _dbBaseName + SUFFIX_DATA, -1,
			MOPrimitive.BLOB.getDefaultSQLType().binaryParam, isMandatory() && !external);
	}

	/**
	 * The columns in the order of {@link #getDbMapping()}: the BLOB column comes last, since some
	 * drivers require the stream of a BLOB to be read after all other columns of a row.
	 */
	private DBAttribute[] dbMapping() {
		List<DBAttribute> result = new ArrayList<>();
		if (_keyColumn != null) {
			result.add(_keyColumn);
			result.add(_hashColumn);
		}
		result.add(_sizeColumn);
		result.add(_contentTypeColumn);
		result.add(_nameColumn);
		if (_dataColumn != null) {
			result.add(_dataColumn);
		}
		return result.toArray(new DBAttribute[result.size()]);
	}

	/**
	 * The column holding the key of the blob, <code>null</code> if this attribute stores no blob
	 * references.
	 */
	public DBAttribute getKeyColumn() {
		return _keyColumn;
	}

	/**
	 * The column holding the hash of the blob content, <code>null</code> if this attribute stores
	 * no blob references.
	 */
	public DBAttribute getHashColumn() {
		return _hashColumn;
	}

	/**
	 * The column holding the size of the content.
	 */
	public DBAttribute getSizeColumn() {
		return _sizeColumn;
	}

	/**
	 * The column holding the content type.
	 */
	public DBAttribute getContentTypeColumn() {
		return _contentTypeColumn;
	}

	/**
	 * The column holding the name.
	 */
	public DBAttribute getNameColumn() {
		return _nameColumn;
	}

	/**
	 * The BLOB column holding the content inline, <code>null</code> if this attribute stores no
	 * inline content.
	 */
	public DBAttribute getDataColumn() {
		return _dataColumn;
	}

	/**
	 * The name of the blob store for content stored externally, <code>null</code> for the default
	 * store.
	 */
	public String getStoreName() {
		return null;
	}

	/**
	 * Prepares the given value for storing it in this attribute.
	 *
	 * <p>
	 * Called when the value is assigned to the attribute. Content that is stored in a blob store is
	 * uploaded by this method, so that writing the row only writes plain column values.
	 * </p>
	 *
	 * @param value
	 *        The value to assign, not <code>null</code>.
	 * @return The value to keep in the cache of the object: a
	 *         {@link com.top_logic.basic.io.blob.BlobBinaryData} for uploaded content, a
	 *         {@link BinaryData} with known size for inline content.
	 */
	public abstract BinaryData toStoredValue(BinaryData value) throws IOException;

	@Override
	public DBAttribute[] getDbMapping() {
		return _dbMapping;
	}

	@Override
	public AttributeStorage getStorage() {
		return _storage;
	}

	@Override
	public void setStorage(AttributeStorage storage) {
		_storage = storage;
	}

	@Override
	public MOAttribute copy(String newName, MetaObject newType) {
		return createCopy(newName, newType);
	}

	/**
	 * Creates a copy of this attribute with the given name and type.
	 *
	 * @see #AbstractBinaryAttribute(String, MetaObject, AbstractBinaryAttribute)
	 */
	protected abstract AbstractBinaryAttribute createCopy(String newName, MetaObject newType);

}
