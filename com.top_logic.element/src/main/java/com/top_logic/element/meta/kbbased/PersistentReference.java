/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.element.meta.ReferenceStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Persistent implementation of {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentReference extends ConfiguredAttributeImpl implements TLReference {

	/** @see #getName() */
	private static final String REF_NAME_ATTRIBUTE = TLReference.NAME_ATTR;

	/** @see #getEnd() */
	public static final String END_ATTR = TLReference.END_ATTR;

	/** @see TLAssociationEnd#isComposite() */
	public static final String COMPOSITE_ATTR = TLAssociationEnd.COMPOSITE_ATTR;

	/** @see TLAssociationEnd#isAggregate() */
	public static final String AGGREGATE_ATTR = TLAssociationEnd.AGGREGATE_ATTR;

	/** @see TLAssociationEnd#isMultiple() */
	public static final String MULTIPLE_ATTR = TLReference.MULTIPLE_ATTR;

	/** @see TLAssociationEnd#isBag() */
	public static final String BAG_ATTR = TLReference.BAG_ATTR;

	/**
	 * Name of the database attribute in which the history type is stored.
	 * 
	 * @see PersistentEnd#getHistoryType()
	 */
	public static final String HISTORY_TYPE_ATTR = "historyType";

	/** @see TLAssociationEnd#isOrdered() */
	public static final String ORDERED_ATTR = "ordered";

	/** @see TLAssociationEnd#canNavigate() */
	public static final String NAVIGATE_ATTR = "navigate";

	/**
	 * Creates a {@link PersistentReference}.
	 */
	@CalledByReflection
	public PersistentReference(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public String getName() {
		return tGetDataString(REF_NAME_ATTRIBUTE);
	}

	@Override
	public void setName(String value) {
		tSetDataString(REF_NAME_ATTRIBUTE, value);
	}

	@Override
	public TLClass getOwner() {
		return tGetDataReference(TLClass.class, ownerRef());
	}

	@Override
	protected String ownerRef() {
		return OWNER_REF;
	}

	@Override
	public PersistentEnd getEnd() {
		return tGetDataReference(PersistentEnd.class, END_ATTR);
	}

	@Override
	public TLType getType() {
		return getEnd().getType();
	}

	@Override
	public void setType(TLType value) {
		getEnd().setType(value);
	}

	@Override
	public void setEnd(TLAssociationEnd value) {
		throw new UnsupportedOperationException("");
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject element) {
		StorageImplementation storageImplementation = getStorageImplementation();
		if (!(storageImplementation instanceof ReferenceStorage)) {
			// Cannot navigate backwards.
			return Collections.emptySet();
		}
		ReferenceStorage refStorage = (ReferenceStorage) storageImplementation;
		return refStorage.getReferers(element, this);
	}

	/**
	 * Delete underlying association, if the forwards reference is deleted.
	 */
	@Override
	protected Modification notifyUpcomingDeletion() {
		Modification result = Modification.NONE;

		TLAssociationEnd end = getEnd();
		if (end != null && end.tValid()) {
			List<TLAssociationEnd> ends = TLModelUtil.getEnds(end.getOwner());
			if (ends.size() == 2) {
				if (ends.indexOf(end) == 1) {
					// The forwards reference is being deleted, delete the association with its ends
					// and a potential backwards reference.
					TLReference backRef = ends.get(0).getReference();
					if (backRef != null) {
						result.andThen(backRef::tDelete);
					}

					// Deletes the association together with its ends.
					TLAssociation owner = end.getOwner();
					result.andThen(owner::tDelete);
				}
			}
		}

		return result;
	}

}
