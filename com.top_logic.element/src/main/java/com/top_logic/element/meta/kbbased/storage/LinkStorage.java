/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;


import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
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
	 * @return The storage configuration.
	 */
	protected static <C extends LinkStorageConfig> C defaultConfig(Class<C> configType, boolean composite, HistoryType historyType) {
		C result = TypedConfiguration.newConfigItem(configType);
		switch (historyType) {
			case CURRENT:
				if (composite) {
					result.setTable(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
				}
				break;
			case HISTORIC:
				result.setTable(ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION);
				break;
			case MIXED:
				result.setTable(ApplicationObjectUtil.MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION);
				break;

		}
		return result;
	}

	@Override
	public final void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		initStorageMapping(attribute);
		initReference(attribute);
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
