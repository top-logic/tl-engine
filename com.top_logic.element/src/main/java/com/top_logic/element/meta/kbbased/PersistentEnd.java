/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Persistent implementation of {@link TLAssociationEnd} directly embedded into a
 * {@link PersistentReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentEnd extends ConfiguredAttributeImpl implements TLAssociationEnd {

	/** Name of the attribute where the name of the {@link TLAssociationEnd} is stored. */
	public static final String END_NAME_ATTR = "name";

	private static final AssociationSetQuery<TLReference> REFERENCE_QUERY = AssociationQuery.createQuery(
		"referenceOfAssociationEnd", TLReference.class, OBJECT_NAME, PersistentReference.END_ATTR);

	/**
	 * Creates a {@link PersistentEnd}.
	 * 
	 * @see PersistentReference
	 */
	@CalledByReflection
	public PersistentEnd(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLAssociation getOwner() {
		return tGetDataReference(TLAssociation.class, ownerRef());
	}

	@Override
	protected String ownerRef() {
		return KBBasedMetaAttribute.OWNER_REF;
	}

	@Override
	public String getName() {
		return tGetDataString(END_NAME_ATTR);
	}

	@Override
	public void setName(String value) {
		tSetDataString(END_NAME_ATTR, value);
	}

	@Override
	public TLReference getReference() {
		Set<TLReference> result = resolveLinks(REFERENCE_QUERY);
		if (result.isEmpty()) {
			return null;
		}
		return result.iterator().next();
	}

	@Override
	public boolean isMandatory() {
		return tGetDataBooleanValue(MANDATORY_ATTR);
	}

	@Override
	public void setMandatory(boolean value) {
		tSetDataBoolean(MANDATORY_ATTR, value);
	}

	@Override
	public boolean isComposite() {
		return tGetDataBooleanValue(PersistentReference.COMPOSITE_ATTR);
	}

	@Override
	public void setComposite(boolean value) {
		tSetDataBoolean(PersistentReference.COMPOSITE_ATTR, value);
	}

	@Override
	public boolean isAggregate() {
		return tGetDataBooleanValue(PersistentReference.AGGREGATE_ATTR);
	}

	@Override
	public void setAggregate(boolean value) {
		tSetDataBoolean(PersistentReference.AGGREGATE_ATTR, value);
	}

	@Override
	public boolean isMultiple() {
		return tGetDataBooleanValue(PersistentReference.MULTIPLE_ATTR);
	}

	@Override
	public void setMultiple(boolean value) {
		tSetDataBoolean(PersistentReference.MULTIPLE_ATTR, value);
	}

	@Override
	public boolean isBag() {
		return tGetDataBooleanValue(PersistentReference.BAG_ATTR);
	}

	@Override
	public void setBag(boolean value) {
		tSetDataBoolean(PersistentReference.BAG_ATTR, value);
	}

	@Override
	public void setHistoryType(HistoryType type) {
		tSetData(PersistentReference.HISTORY_TYPE_ATTR, type);
	}

	@Override
	public HistoryType getHistoryType() {
		return (HistoryType) tGetData(PersistentReference.HISTORY_TYPE_ATTR);
	}

	@Override
	public boolean isOrdered() {
		return tGetDataBooleanValue(PersistentReference.ORDERED_ATTR);
	}

	@Override
	public void setOrdered(boolean value) {
		tSetDataBoolean(PersistentReference.ORDERED_ATTR, value);
	}

	@Override
	public boolean isAbstract() {
		return tGetDataBooleanValue(ABSTRACT_ATTR);
	}

	@Override
	public void setAbstract(boolean value) {
		tSetDataBoolean(ABSTRACT_ATTR, value);
	}

	@Override
	public boolean canNavigate() {
		return tGetDataBooleanValue(PersistentReference.NAVIGATE_ATTR);
	}

	@Override
	public void setNavigate(boolean value) {
		tSetDataBoolean(PersistentReference.NAVIGATE_ATTR, value);
	}

	@Override
	public TLStructuredTypePart getDefinition() {
		return this;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

}
