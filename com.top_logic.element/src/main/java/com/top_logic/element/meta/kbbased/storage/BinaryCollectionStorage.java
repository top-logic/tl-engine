/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.DefaultAssociationStorageDescriptor;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.StaticItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.PreloadBuilder;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.v5.AssociationCachePreload;

/**
 * {@link AssociationQueryBasedStorage} for multi-valued binary attributes.
 * 
 * <p>
 * Each file of the value is stored in a row of a separate table (see {@link Config#getTable()}),
 * together with its position within the value, its file name and its content type. Deleting the
 * owner deletes its rows.
 * </p>
 * 
 * <p>
 * The value of an ordered attribute is a {@link List}, the value of an unordered attribute a
 * {@link java.util.Set} in the order of the stored rows. Each element is a {@link BinaryData}
 * that is backed by its row. When such a value is set again to the same object and attribute, its
 * row is kept (only its position is updated), all other values get a new row, and rows of values
 * no longer contained are deleted.
 * </p>
 * 
 * @implNote A row references the owner in column {@link #OBJECT_ATTRIBUTE_NAME} and the attribute
 *           definition in column {@link #META_ATTRIBUTE_ATTRIBUTE_NAME}. The position of the file
 *           within the value is stored in {@link #SORT_ORDER_ATTRIBUTE_NAME}, its file name in
 *           {@link #NAME_ATTRIBUTE_NAME}, its content type in
 *           {@link #CONTENT_TYPE_ATTRIBUTE_NAME}, and its content in {@link #DATA_ATTRIBUTE_NAME}.
 *           The default tables are {@link #TABLE} and, for owners stored in an unversioned table,
 *           {@link #UNVERSIONED_TABLE}. The table is described by {@link #getStorageDescriptors()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = TLStorage.PRIMITIVE_CLASSIFIER)
@Label("Files in separate table")
public class BinaryCollectionStorage<C extends BinaryCollectionStorage.Config<?>> extends AssociationQueryBasedStorage<C>
		implements SeparateTableStorage {

	/**
	 * Table for values of attributes whose owners are stored in a versioned table.
	 */
	public static final String TABLE = "BinaryAttributeValue";

	/**
	 * Table for values of attributes whose owners are stored in an unversioned table.
	 */
	public static final String UNVERSIONED_TABLE = "BinaryAttributeValueUnversioned";

	/** Column storing the position of a file within the attribute value. */
	public static final String SORT_ORDER_ATTRIBUTE_NAME = "sortOrder";

	/** Column storing the file name. */
	public static final String NAME_ATTRIBUTE_NAME = "name";

	/** Column storing the content type of the file. */
	public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "contentType";

	/** Column storing the content of the file. */
	public static final String DATA_ATTRIBUTE_NAME = "data";

	private static final Collection<? extends Class<?>> COMPATIBLE_TYPES = List.of(BinaryDataSource.class);

	private static final Comparator<KnowledgeItem> SORT_ORDER =
		Comparator.comparingInt(BinaryCollectionStorage::getSortOrder);

	/**
	 * Configuration options for {@link BinaryCollectionStorage}.
	 */
	@TagName("binary-collection-storage")
	public interface Config<I extends BinaryCollectionStorage<?>> extends AssociationQueryBasedStorage.Config<I> {

		/**
		 * @see #getTable()
		 */
		String TABLE_PROPERTY = "table";

		/**
		 * Name of the table storing one row per file.
		 */
		@Name(TABLE_PROPERTY)
		@StringDefault(BinaryCollectionStorage.TABLE)
		String getTable();

		/** @see #getTable() */
		void setTable(String value);

	}

	private AssociationSetQuery<StaticItem> _query;

	private PreloadContribution _preload;

	private DefaultAssociationStorageDescriptor _storageDescriptor;

	/**
	 * Creates a {@link BinaryCollectionStorage} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BinaryCollectionStorage(InstantiationContext context, C config) {
		super(context, config);
		_storageDescriptor = new DefaultAssociationStorageDescriptor(config.getTable(), OBJECT_ATTRIBUTE_NAME,
			META_ATTRIBUTE_ATTRIBUTE_NAME, DATA_ATTRIBUTE_NAME);
	}

	/**
	 * Creates the configuration of a {@link BinaryCollectionStorage}.
	 *
	 * @param unversioned
	 *        Whether the owners of the attribute are stored in an unversioned table.
	 */
	public static Config<?> defaultConfig(boolean unversioned) {
		Config<?> result = TypedConfiguration.newConfigItem(Config.class);
		if (unversioned) {
			result.setTable(UNVERSIONED_TABLE);
		}
		return result;
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_query = createQuery(getTable(), attribute.getDefinition(), StaticItem.class);
		_preload = new AssociationCachePreload(_query);
		_storageDescriptor.checkKeyAttributes(attribute);
	}

	private String getTable() {
		return getConfig().getTable();
	}

	@Override
	public List<? extends AssociationStorageDescriptor> getStorageDescriptors() {
		return Collections.singletonList(_storageDescriptor);
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		_preload.contribute(preloadBuilder);
	}

	@Override
	public Collection<BinaryData> getAttributeValue(TLObject owner, TLStructuredTypePart attribute) {
		Collection<BinaryData> result = attribute.isOrdered() ? new ArrayList<>() : new LinkedHashSet<>();
		for (KnowledgeItem row : rows(owner)) {
			result.add(new StoredBinaryData(row));
		}
		return result;
	}

	@Override
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object value) {
		if (object != null) {
			AttributeUtil.checkHasAttribute(object, attribute);
		}
		for (Object element : toCollection(value)) {
			checkElement(attribute, element);
		}
	}

	private void checkElement(TLStructuredTypePart attribute, Object element) {
		if (element == null) {
			throw new IllegalArgumentException("Attribute '" + attribute + "' cannot store an empty file.");
		}
		super.checkSetValue(null, attribute, element);
	}

	@Override
	protected void internalSetAttributeValue(TLObject owner, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		Map<KnowledgeItem, Boolean> unused = new IdentityHashMap<>();
		for (KnowledgeItem row : resolveQuery(owner, _query)) {
			unused.put(row, Boolean.TRUE);
		}

		int sortOrder = 0;
		for (Object element : toCollection(value)) {
			KnowledgeItem row = element instanceof StoredBinaryData stored ? stored.getRow() : null;
			if (row != null && unused.remove(row) != null) {
				if (getSortOrder(row) != sortOrder) {
					row.setAttributeValue(SORT_ORDER_ATTRIBUTE_NAME, Integer.valueOf(sortOrder));
				}
			} else {
				createRow(owner, attribute, sortOrder, BinaryData.cast(element));
			}
			sortOrder++;
		}

		getKnowledgeBase().deleteAll(unused.keySet());
		AttributeOperations.touch(owner, attribute);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		checkIncrementalChange(object, attribute, value);

		List<KnowledgeItem> rows = rows(object);
		if (value instanceof StoredBinaryData stored && rows.contains(stored.getRow())) {
			// Already part of the value.
			return;
		}
		int sortOrder = rows.isEmpty() ? 0 : getSortOrder(rows.get(rows.size() - 1)) + 1;
		createRow(object, attribute, sortOrder, BinaryData.cast(value));
		AttributeOperations.touch(object, attribute);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, AttributeException {
		checkIncrementalChange(object, attribute, value);

		List<KnowledgeItem> rows = rows(object);
		KnowledgeItem match = null;
		if (value instanceof StoredBinaryData stored && rows.contains(stored.getRow())) {
			match = stored.getRow();
		} else {
			for (KnowledgeItem row : rows) {
				if (new StoredBinaryData(row).equals(value)) {
					match = row;
					break;
				}
			}
		}
		if (match != null) {
			match.delete();
			AttributeOperations.touch(object, attribute);
		}
	}

	private void checkIncrementalChange(TLObject object, TLStructuredTypePart attribute, Object value) {
		AttributeUtil.checkHasAttribute(object, attribute);
		if (!attribute.isMultiple()) {
			throw new IllegalArgumentException("Attribute '" + attribute + "' is not multiple.");
		}
		checkElement(attribute, value);
	}

	private void createRow(TLObject owner, TLStructuredTypePart attribute, int sortOrder, BinaryData data) {
		KnowledgeItem row = createItem(getTable());
		row.setAttributeValue(OBJECT_ATTRIBUTE_NAME, owner.tHandle());
		row.setAttributeValue(META_ATTRIBUTE_ATTRIBUTE_NAME, attribute.getDefinition().tHandle());
		row.setAttributeValue(SORT_ORDER_ATTRIBUTE_NAME, Integer.valueOf(sortOrder));
		row.setAttributeValue(NAME_ATTRIBUTE_NAME, data.getName());
		row.setAttributeValue(CONTENT_TYPE_ATTRIBUTE_NAME, data.getContentType());
		row.setAttributeValue(DATA_ATTRIBUTE_NAME, data);
	}

	/**
	 * The rows storing the value of the given owner, sorted by {@link #SORT_ORDER_ATTRIBUTE_NAME}.
	 */
	private List<KnowledgeItem> rows(TLObject owner) {
		List<KnowledgeItem> result = new ArrayList<>(resolveQuery(owner, _query));
		result.sort(SORT_ORDER);
		return result;
	}

	private static int getSortOrder(KnowledgeItem row) {
		return ((Integer) row.getAttributeValue(SORT_ORDER_ATTRIBUTE_NAME)).intValue();
	}

	private static Collection<?> toCollection(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return Collections.singletonList(value);
	}

	@Override
	protected Collection<? extends Class<?>> getApplicationValueTypes() {
		return COMPATIBLE_TYPES;
	}

	/**
	 * {@link BinaryData} read from a row of the table of a {@link BinaryCollectionStorage}.
	 */
	private static final class StoredBinaryData extends AbstractBinaryData {

		private final KnowledgeItem _row;

		private final String _name;

		private final String _contentType;

		private final BinaryData _data;

		StoredBinaryData(KnowledgeItem row) {
			_row = row;
			_name = (String) row.getAttributeValue(NAME_ATTRIBUTE_NAME);
			_contentType = nonNullContentType((String) row.getAttributeValue(CONTENT_TYPE_ATTRIBUTE_NAME));
			_data = (BinaryData) row.getAttributeValue(DATA_ATTRIBUTE_NAME);
		}

		/**
		 * The row this value was read from.
		 */
		KnowledgeItem getRow() {
			return _row;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getContentType() {
			return _contentType;
		}

		@Override
		public long getSize() {
			return _data.getSize();
		}

		@Override
		public InputStream getStream() throws IOException {
			return _data.getStream();
		}

	}

}
