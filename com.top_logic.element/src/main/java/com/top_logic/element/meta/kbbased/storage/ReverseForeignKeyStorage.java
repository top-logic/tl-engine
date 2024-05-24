/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
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
import com.top_logic.element.meta.kbbased.storage.InlineCollectionStorage.StoreInTargetConfig;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.MonomorphicContainerColumn;
import com.top_logic.model.composite.PolymorphicContainerColumn;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} that stores a reference value by storing the referrer in a foreign key
 * column in the target object.
 * 
 * <p>
 * This storage is therefore just usable when the "backwards reference" is also "to one".
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Storage in the target table")
public class ReverseForeignKeyStorage<C extends ReverseForeignKeyStorage.Config<?>> extends TLItemStorage<C>
		implements ReferenceStorage, CompositeStorage {

	/**
	 * {@link ReverseForeignKeyStorage} configuration options.
	 */
	@TagName("reverse-foreign-key-storage")
	public interface Config<I extends ReverseForeignKeyStorage<?>>
			extends TLItemStorage.Config<I>, StoreInTargetConfig {

		// No additional properties here.

	}

	private AssociationSetQuery<TLObject> _outgoingQuery;

	/**
	 * Creates a {@link ReverseForeignKeyStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReverseForeignKeyStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		String table = TLAnnotations.getTable(attribute.getType());
		Map<String, KnowledgeItem> filter;
		if (getConfig().getReferenceColumn() != null) {
			filter = Collections.singletonMap(getConfig().getReferenceColumn(), attribute.getDefinition().tHandle());
		} else {
			filter = Collections.emptyMap();
		}
		_outgoingQuery = AssociationQuery.createQuery(attribute.getName() + " in target", TLObject.class, table,
			getConfig().getContainerColumn(), filter);
	}

	private KnowledgeItem attributeDefinition(TLStructuredTypePart attribute) {
		return attribute.getDefinition().tHandle();
	}

	private String getTable() {
		return _outgoingQuery.getAssociationTypeName();
	}

	@Override
	protected Object getReferencedTLObject(TLObject self, TLStructuredTypePart attribute) {
		Set<TLObject> links = AbstractWrapper.resolveLinks(self, _outgoingQuery);
		switch (links.size()) {
			case 0:
				return null;
			case 1:
				return links.iterator().next();
			default:
				throw new IllegalStateException(
					"Multiple elements '" + links + "' points to the same target '" + self + "'.");
		}
	}

	private KnowledgeItem getStorageObject(TLObject attributed) {
		return attributed.tHandle();
	}

	@Override
	protected void storeReferencedTLObject(TLObject object, TLStructuredTypePart attribute, Object oldValue,
			Object newValue) throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		KnowledgeItem referrerObject = getStorageObject(object);

		{
			String referenceColumn = getConfig().getReferenceColumn();
			KnowledgeItem newTarget = unwrapTarget(newValue);
			if (newTarget != null) {
				Object oldContainer = newTarget.setAttributeValue(getConfig().getContainerColumn(), referrerObject);
				if (oldContainer != null) {
					newTarget.setAttributeValue(getConfig().getContainerColumn(), oldContainer);
					TLObject oldRef;
					if (referenceColumn == null) {
						oldRef = attribute;
					} else {
						oldRef = ((KnowledgeItem) newTarget.getAttributeValue(referenceColumn)).getWrapper();
					}
					throw new IllegalArgumentException("Reference target '" + newValue + "' for reference '" + attribute
							+ "' of object '" + object + "' is already part of a different container '"
							+ ((KnowledgeItem) oldContainer).getWrapper() + "' in reference '" + oldRef + "'.");
				}
				if (referenceColumn != null) {
					newTarget.setAttributeValue(referenceColumn, attributeDefinition(attribute));
				}
			}
			KnowledgeItem oldTarget = unwrapTarget(oldValue);
			if (oldTarget != null) {
				oldTarget.setAttributeValue(getConfig().getContainerColumn(), null);
				if (referenceColumn != null) {
					oldTarget.setAttributeValue(referenceColumn, null);
				}
			}
			AttributeOperations.touch(object, attribute);
		}
	}

	private KnowledgeItem unwrapTarget(Object value) {
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
		AttributeUtil.checkHasAttribute(object, attribute);
		if (simpleValue != null) {
			checkHasKOAttribute((TLObject) simpleValue);
		}
	}

	private void checkHasKOAttribute(TLObject attributed) {
		MetaObject metaObject = attributed.tTable();
		MOStructure structure = checkStructure(metaObject);
		MOAttribute moAttribute = structure.getAttributeOrNull(getStorageAttribute());
		checkAttributeNotNull(structure, moAttribute);
		checkAttributeIsReference(structure, moAttribute);
	}

	private String getStorageAttribute() {
		return getConfig().getContainerColumn();
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
		return CollectionUtil
			.singletonOrEmptySet(InlineCollectionStorage.resolveContainer(getConfig(), getTable(), self, reference));
	}

	@Override
	public ContainerStorage getContainerStorage(TLReference reference) {
		String containerColumn = getConfig().getContainerColumn();
		String referenceColumn = getConfig().getReferenceColumn();
		if (referenceColumn == null) {
			return new MonomorphicContainerColumn(containerColumn, reference);
		} else {
			return new PolymorphicContainerColumn(containerColumn, referenceColumn);
		}
	}

}
