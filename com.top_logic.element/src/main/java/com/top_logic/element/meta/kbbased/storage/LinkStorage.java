/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.persistency.LinkTables;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.SinglePreloadContribution;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * {@link CollectionStorage} that creates association links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LinkStorage<C extends LinkStorage.Config<?>> extends CollectionStorage<C>
		implements AssociationStorage {

	/**
	 * Configuration options for storages that store references to other objects.
	 */
	public interface LinkStorageConfig extends ConfigurationItem {

		/**
		 * @see #getTable()
		 */
		String TABLE = "table";

		/**
		 * Configuration of the name of the table in which this {@link AssociationStorage} stores
		 * the reference.
		 */
		@Name(TABLE)
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		@Options(fun = LinkTables.class)
		String getTable();

		/** see {@link #getTable()} */
		void setTable(String tableName);

		/**
		 * The sort order attribute in {@link #getTable()}.
		 */
		@Hidden
		@StringDefault(SORT_ORDER)
		String getOrderAttribute();

		/**
		 * @see #getOrderAttribute()
		 */
		void setOrderAttribute(String attribute);

		/**
		 * Whether the table in {@link #getTable()} is a monomorphic table.
		 */
		@Hidden
		boolean isMonomorphicTable();

		/**
		 * @see #isMonomorphicTable()
		 */
		void setMonomorphicTable(boolean monomorphic);
	}

	/**
	 * Configuration options for {@link LinkStorage}.
	 */
	public interface Config<I extends LinkStorage<?>> extends CollectionStorage.Config<I>, LinkStorageConfig {

		/**
		 * Optional configuration of a {@link StorageMapping} which maps the business object value
		 * to a {@link TLObject} database value.
		 */
		@Hidden
		PolymorphicConfiguration<? extends StorageMapping<?>> getStorageMapping();

	}

	/**
	 * Attribute name for integer valued sort key of
	 * {@link WrapperMetaAttributeUtil#isAttributeReferenceAssociation(MetaObject)} associations.
	 */
	public static final String SORT_ORDER = "sortOrder";

	private AssociationSetQuery<KnowledgeAssociation> _incomingQuery;

	private PreloadContribution _preload;

	private PreloadContribution _reversePreload;

	private StorageMapping<?> _storageMapping;

	/**
	 * Creates a {@link LinkStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LinkStorage(InstantiationContext context, C config) {
		super(context, config);
		_storageMapping = context.getInstance(config.getStorageMapping());
	}

	/**
	 * Creates a configuration of the given configuration type.
	 *
	 * @param configType
	 *        The concrete storage configuration type.
	 * @param composite
	 *        Whether the reference is a composition.
	 * @param historyType
	 *        The history type of the value of the reference.
	 * @param deletionPolicy
	 *        What happens, if a value of the reference is deleted.
	 * @param unversioned
	 *        Whether reference values must be stored unversioned.
	 * @param bag
	 *        Whether the reference may hold the same value more than once, see
	 *        {@link TLStructuredTypePart#isBag()}. The links of a bag reference are stored in a table
	 *        without uniqueness constraint. A composition holds each part only once, so this flag
	 *        has no effect for compositions.
	 * @return The storage configuration.
	 */
	protected static <C extends LinkStorageConfig> C defaultConfig(Class<C> configType, boolean composite,
			HistoryType historyType, DeletionPolicy deletionPolicy, boolean unversioned, boolean bag) {
		C result = TypedConfiguration.newConfigItem(configType);
		String table = defaultTable(composite, historyType, deletionPolicy, unversioned, bag);
		if (table != null) {
			result.setTable(table);
		}
		return result;
	}

	private static String defaultTable(boolean composite, HistoryType historyType, DeletionPolicy deletionPolicy,
			boolean unversioned, boolean bag) {
		switch (historyType) {
			case CURRENT:
				if (composite) {
					switch (deletionPolicy) {
						case CLEAR_REFERENCE:
							return unversioned ? ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION_UNVERSIONED
								: ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION;
						case DELETE_REFERER:
							return unversioned
								? ApplicationObjectUtil.STRUCTURE_CHILD_DELETE_REFERER_ASSOCIATION_UNVERSIONED
								: ApplicationObjectUtil.STRUCTURE_CHILD_DELETE_REFERER_ASSOCIATION;
						case STABILISE_REFERENCE:
							// This an unsupported combination of settings.
							return null;
						case VETO:
							return unversioned ? ApplicationObjectUtil.STRUCTURE_CHILD_VETO_ASSOCIATION_UNVERSIONED
								: ApplicationObjectUtil.STRUCTURE_CHILD_VETO_ASSOCIATION;
					}
				} else {
					switch (deletionPolicy) {
						case CLEAR_REFERENCE:
							return select(unversioned, bag,
								ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED,
								ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED);
						case DELETE_REFERER:
							return select(unversioned, bag,
								ApplicationObjectUtil.WRAPPER_DELETE_REFERER_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_DELETE_REFERER_ASSOCIATION_UNVERSIONED,
								ApplicationObjectUtil.WRAPPER_DELETE_REFERER_BAG_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_DELETE_REFERER_BAG_ASSOCIATION_UNVERSIONED);
						case STABILISE_REFERENCE:
							return mixedTable(unversioned, bag);
						case VETO:
							return select(unversioned, bag,
								ApplicationObjectUtil.WRAPPER_VETO_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_VETO_ASSOCIATION_UNVERSIONED,
								ApplicationObjectUtil.WRAPPER_VETO_BAG_ASSOCIATION,
								ApplicationObjectUtil.WRAPPER_VETO_BAG_ASSOCIATION_UNVERSIONED);
					}
				}
				return null;
			case HISTORIC:
				return select(unversioned, bag,
					ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION,
					ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED,
					ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION,
					ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED);
			case MIXED:
				return mixedTable(unversioned, bag);
		}
		return null;
	}

	private static String mixedTable(boolean unversioned, boolean bag) {
		return select(unversioned, bag,
			ApplicationObjectUtil.MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION,
			ApplicationObjectUtil.MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED,
			ApplicationObjectUtil.MIXED_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION,
			ApplicationObjectUtil.MIXED_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED);
	}

	private static String select(boolean unversioned, boolean bag, String table, String unversionedTable,
			String bagTable, String unversionedBagTable) {
		if (bag) {
			return unversioned ? unversionedBagTable : bagTable;
		} else {
			return unversioned ? unversionedTable : table;
		}
	}

	@Override
	public final void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		initStorageMapping(attribute);
		initReference(attribute);

		if (!monomophicTable()) {
			checkKeyAttributes(attribute,
				WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR,
				DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		}
		checkBagTable(attribute);
	}

	/**
	 * Reports a configuration error, if the given attribute is a bag but {@link #getTable()}
	 * forbids storing the same value more than once.
	 * 
	 * <p>
	 * A composition is not checked, since it holds each part only once.
	 * </p>
	 * 
	 * @see TLStructuredTypePart#isBag()
	 */
	private void checkBagTable(TLStructuredTypePart attribute) {
		if (!attribute.isBag() || (attribute instanceof TLReference reference && reference.isComposite())) {
			return;
		}
		MetaObject table = attribute.tKnowledgeBase().getMORepository().getTypeOrNull(getTable());
		if (!(table instanceof MOClass)) {
			// Reported elsewhere.
			return;
		}
		MOIndex index = uniqueLinkIndex((MOClass) table);
		if (index != null) {
			Logger.error("Bag attribute '" + TLModelUtil.qualifiedName(attribute) + "' is stored in table '"
				+ getTable() + "' with unique index '" + index.getName()
				+ "', which does not allow storing a value more than once. Use a table for bag references instead.",
				LinkStorage.class);
		}
	}

	/**
	 * The unique index of the given table that forbids storing a link from the same source to the
	 * same destination for the same attribute more than once, or <code>null</code> if there is no
	 * such index.
	 */
	private MOIndex uniqueLinkIndex(MOClass table) {
		Set<String> linkColumns = new HashSet<>();
		linkColumns.add(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		linkColumns.add(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		if (!monomophicTable()) {
			linkColumns.add(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR);
		}
		for (MOIndex index : table.getIndexes()) {
			if (!index.isUnique()) {
				continue;
			}
			boolean coveredByLink = true;
			for (MOAttribute indexAttribute : index.getAttributes()) {
				if (!linkColumns.contains(indexAttribute.getName())) {
					coveredByLink = false;
					break;
				}
			}
			if (coveredByLink) {
				return index;
			}
		}
		return null;
	}

	/**
	 * Updates {@link #storageMapping()} based on the given attribute
	 * 
	 * @see #init(TLStructuredTypePart)
	 */
	protected void initStorageMapping(TLStructuredTypePart attribute) {
		TLType type = attribute.getType();
		if (type instanceof TLPrimitive) {
			// Do not override explicitly configured storage mapping.
			if (_storageMapping == null) {
				_storageMapping = ((TLPrimitive) type).getStorageMapping();
			}
		}
	}

	/**
	 * @see #init(TLStructuredTypePart)
	 */
	protected void initReference(TLStructuredTypePart attribute) {
		_incomingQuery = LinkStorageUtil.createIncomingQuery(attribute, this);
		_preload = new SinglePreloadContribution(new AssociationNavigationPreload(getOutgoingQuery()));
		_reversePreload = new SinglePreloadContribution(new AssociationNavigationPreload(getIncomingQuery()));
	}

	@Override
	public ObjectKey getBaseObjectId(Map<String, Object> row) {
		return (ObjectKey) row.get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
	}

	@Override
	public String getBaseObjectColumn() {
		return DBKnowledgeAssociation.REFERENCE_SOURCE_NAME;
	}

	@Override
	public String getStorageColumn() {
		return DBKnowledgeAssociation.REFERENCE_DEST_NAME;
	}

	@Override
	public ObjectKey getPartId(Map<String, Object> row) {
		if (monomophicTable()) {
			return getAttribute().getDefinition().tId();
		} else {
			return (ObjectKey) row.get(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR);
		}
	}

	@Override
	public AssociationSetQuery<KnowledgeAssociation> getIncomingQuery() {
		return _incomingQuery;
	}

	@Override
	public PreloadContribution getPreload() {
		return _preload;
	}

	@Override
	public PreloadContribution getReversePreload() {
		return _reversePreload;
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		return AbstractWrapper.resolveWrappersTyped(self, getIncomingQuery(), TLObject.class);
	}

	/**
	 * Check if the given value (an element of the collection value of the attribute) fulfils basic
	 * constraints.
	 * 
	 * @param object
	 *        The object to check.
	 * @param attribute
	 *        The attribute whose value to check.
	 * @param aValue
	 *        An element of the collection to store into the given {@link TLStructuredTypePart}.
	 * 
	 * @throws IllegalArgumentException
	 *         if the value does not match constraints
	 */
	protected void checkBasicValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws IllegalArgumentException {
		TLItemStorage.checkCorrectType(attribute, aValue, storageMapping());
	}

	@Override
	public String getTable() {
		return ((Config<?>) getConfig()).getTable();
	}

	@Override
	public boolean monomophicTable() {
		return ((Config<?>) getConfig()).isMonomorphicTable();
	}

	/**
	 * The instantiated {@link Config#getStorageMapping()}. May be <code>null</code>.
	 */
	public final StorageMapping<?> storageMapping() {
		return _storageMapping;
	}

	/**
	 * Maps the given business object to a storage value.
	 */
	protected Object toStorageObject(Object businessObject) {
		if (storageMapping() != null) {
			businessObject = storageMapping().getStorageObject(businessObject);
		}
		return businessObject;
	}

	/**
	 * Maps the given storage value to a business object.
	 */
	protected Object toBusinessObject(Object storageValue) {
		if (storageMapping() != null) {
			storageValue = storageMapping().getBusinessObject(storageValue);
		}
		return storageValue;
	}

}
