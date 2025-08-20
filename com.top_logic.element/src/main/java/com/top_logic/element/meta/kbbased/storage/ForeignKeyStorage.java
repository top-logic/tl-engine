/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ReferenceStorage;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.WithStorageAttribute;
import com.top_logic.model.annotate.persistency.AllReferenceColumns;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.SourceTable;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.SinglePreloadContribution;
import com.top_logic.model.v5.AssociationCachePreload;
import com.top_logic.model.v5.ReferencePreload;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} that stores a reference value as foreign key attribute.
 * 
 * @see ReverseForeignKeyStorage
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Storage in the source table")
public class ForeignKeyStorage<C extends ForeignKeyStorage.Config<?>> extends TLItemStorage<C>
		implements ReferenceStorage, CompositeStorage, ColumnStorage {

	/**
	 * {@link ForeignKeyStorage} configuration options.
	 */
	@TagName("foreign-key-storage")
	@DisplayOrder({ Config.STORAGE_TYPE, Config.STORAGE_ATTRIBUTE })
	public interface Config<I extends ForeignKeyStorage<?>> extends TLItemStorage.Config<I>, WithStorageAttribute {

		/** Property name of {@link #getStorageType()}. */
		String STORAGE_TYPE = "storage-type";

		/**
		 * Table type name that defines the {@link #getStorageAttribute()}.
		 * 
		 * @deprecated TODO: Remove since it must be clear from the final mapping of the concrete
		 *             types to the persistency layer.
		 */
		@Name(STORAGE_TYPE)
		@Mandatory
		@Deprecated
		@Options(fun = TableName.AllTLObjectTables.class)
		String getStorageType();

		/**
		 * @see #getStorageType()
		 */
		void setStorageType(String value);

		/**
		 * The name of the {@link MOReference} which is used to store the reference to.
		 * 
		 * <p>
		 * If not set, it defaults to the name of the reference attribute name.
		 * </p>
		 */
		@Override
		@Options(fun = AllReferenceColumns.class, args = { @Ref(STORAGE_TYPE) })
		String getStorageAttribute();

	}

	private String _storageAttributeName;

	private AssociationSetQuery<TLObject> _incomingQuery;

	/**
	 * Creates a {@link ForeignKeyStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ForeignKeyStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_storageAttributeName = getStorageAttributeName(attribute, getConfig().getStorageAttribute());
		_incomingQuery =
			AssociationQuery.createQuery(attribute.getName() + "References", TLObject.class,
				getConfig().getStorageType(), getStorageAttribute());
	}

	private String getTable() {
		return _incomingQuery.getAssociationTypeName();
	}

	private String getStorageAttributeName(TLStructuredTypePart attribute, String configuredStorageAttribute) {
		if (StringServices.isEmpty(configuredStorageAttribute)) {
			return attribute.getName();
		} else {
			return configuredStorageAttribute;
		}
	}

	@Override
	protected Object getReferencedTLObject(TLObject self, TLStructuredTypePart attribute) {
		KnowledgeItem storageObject = getStorageObject(self);
		Object storedReference;
		try {
			storedReference = storageObject.getAttributeValue(getStorageAttribute());
		} catch (NoSuchAttributeException ex) {
			throw new AttributeException("Object '" + self + "' does not define attribute '"
				+ attribute.getName() + "'.", ex);
		}
		if (storedReference == null) {
			return null;
		}
		return ((KnowledgeItem) storedReference).getWrapper();
	}

	private KnowledgeItem getStorageObject(TLObject attributed) {
		return attributed.tHandle();
	}

	@Override
	protected void storeReferencedTLObject(TLObject object, TLStructuredTypePart attribute, Object oldValue,
			Object newValue) throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		KnowledgeItem storageObject = getStorageObject(object);

		{
			KnowledgeItem reference = unwrapReference(newValue);
			storageObject.setAttributeValue(getStorageAttribute(), reference);
			AttributeOperations.touch(object, attribute);
		}
	}

	private KnowledgeItem unwrapReference(Object value) {
		KnowledgeItem reference;
		if (value == null) {
			reference = null;
		} else if (value instanceof TLObject) {
			reference = ((TLObject) value).tHandle();
		} else if (value instanceof KnowledgeItem) {
			reference = (KnowledgeItem) value;
		} else {
			throw new IllegalArgumentException("Can not resolve " + KnowledgeItem.class.getName() + " from " + value);
		}
		return reference;
	}

	@Override
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object simpleValue) throws TopLogicException {
		super.checkSetValue(object, attribute, simpleValue);
		checkCorrectAttribute(object, attribute);
	}

	private void checkCorrectAttribute(TLObject context, TLStructuredTypePart attribute) {
		if (context == null) {
			return;
		}
		AttributeUtil.checkHasAttribute(context, attribute);
		checkHasKOAttribute(context);
	}

	private void checkHasKOAttribute(TLObject attributed) {
		MetaObject metaObject = attributed.tTable();
		MOStructure structure = checkStructure(metaObject);
		MOAttribute moAttribute = structure.getAttributeOrNull(getStorageAttribute());
		checkAttributeNotNull(structure, moAttribute);
		checkAttributeIsReference(structure, moAttribute);
	}

	private MOStructure checkStructure(MetaObject metaObject) {
		if (!(metaObject instanceof MOStructure)) {
			throw new IllegalArgumentException(metaObject + " must have attributes");
		}
		return (MOStructure) metaObject;
	}

	private void checkAttributeIsReference(MOStructure structure, MOAttribute moAttribute) {
		if (!(moAttribute instanceof MOReference)) {
			throw new IllegalArgumentException(
				"Type " + structure + " define attribute " + getStorageAttribute() + " which is not a reference.");
		}
	}

	private void checkAttributeNotNull(MOStructure structure, MOAttribute moAttribute) {
		if (moAttribute == null) {
			throw new IllegalArgumentException(
				"Type " + structure + " does not define attribute " + getStorageAttribute());
		}
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		return AbstractWrapper.resolveLinks(self, _incomingQuery);
	}

	/**
	 * The name of the {@link MOAttribute} storing the reference in the database table.
	 */
	@Override
	public final String getStorageAttribute() {
		return _storageAttributeName;
	}

	@Override
	public ContainerStorage getContainerStorage(TLReference reference) {
		return new SourceTable(getTable(), getStorageAttribute(), reference);
	}

	@Override
	public PreloadContribution getPreload() {
		return new ReferencePreload(getTable(), getStorageAttribute());
	}

	@Override
	public PreloadContribution getReversePreload() {
		return new SinglePreloadContribution(new AssociationCachePreload(_incomingQuery));
	}

}
