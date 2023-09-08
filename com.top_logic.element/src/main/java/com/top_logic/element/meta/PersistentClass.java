/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.meta.kbbased.PersistentAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLTypeVisitor;

/**
 * {@link TLClass} implementation based on a {@link KBBasedMetaElement}.
 * 
 * @see PersistentAssociation
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentClass extends KBBasedMetaElement implements TLClass {

	/**
	 * Table name where {@link PersistentClass}es are stored.
	 */
	public static final String OBJECT_NAME = META_ELEMENT_KO;

	/**
	 * {@link AssociationQuery} to resolve the {@link TLClassPart}s that have a given
	 * {@link PersistentClass} as their owner.
	 * 
	 * @see KBBasedMetaAttribute#OWNER_REF
	 * @see PersistentAssociation#ASSOCIATION_PARTS
	 */
	public static final OrderedLinkQuery<TLClassPart> ATTRIBUTES_ATTR =
		AssociationQuery.createOrderedLinkQuery("attributes", TLClassPart.class, KBBasedMetaAttribute.OBJECT_NAME,
			KBBasedMetaAttribute.OWNER_REF, KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR, null, true);

	/**
	 * Returns the {@link AssociationQuery queries} that are used by instances of
	 * {@link PersistentClass} to cache relations to other model elements.
	 */
	@FrameworkInternal
	public static List<AbstractAssociationQuery<? extends TLObject, ?>> getAssociationQueries() {
		List<AbstractAssociationQuery<? extends TLObject, ?>> queries =
			new ArrayList<>();
		queries.add(SUB_MES_ATTR);
		queries.add(SUPER_ME_ATTR);
		queries.add(ATTRIBUTES_ATTR);
		return queries;
	}

	/**
	 * Creates a new {@link PersistentClass}.
	 */
	public PersistentClass(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public List<TLClass> getGeneralizations() {
		return getGeneralizations(TLClass.class);
	}

	@Override
	public Collection<TLClass> getSpecializations() {
		return AbstractWrapper.resolveWrappersTyped(this, SUB_MES_ATTR, TLClass.class);
	}

	@Override
	public List<TLClassPart> getLocalParts() {
		return AbstractWrapper.resolveLinks(this, ATTRIBUTES_ATTR);
	}

	@Override
	public List<TLClassPart> getLocalClassParts() {
		return getLocalParts();
	}

	@Override
	public <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitClass(this, arg);
	}

}
