/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypeVisitor;

/**
 * Implementation of a {@link TLAssociation} based on a {@link KBBasedMetaElement}.
 * 
 * @see PersistentClass
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentAssociation extends KBBasedMetaElement implements TLAssociation {

	/**
	 * Query finding the <b>{@link TLReference}</b> that implement an {@link TLAssociationEnd} of
	 * this {@link TLAssociation} and {@link TLAssociationProperty}s.
	 * 
	 * @see PersistentClass#ATTRIBUTES_ATTR
	 */
	public static final OrderedLinkQuery<TLAssociationPart> ASSOCIATION_PARTS =
		AssociationQuery.createOrderedLinkQuery("parts", TLAssociationPart.class, KBBasedMetaAttribute.OBJECT_NAME,
			KBBasedMetaAttribute.OWNER_REF, KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR, null, true);

	private static final String TL_ASSOCIATION_UNIONS_ASS = "MetaElement_unions";

	/**
	 * Query searching the {@link KnowledgeAssociation} which points to the base object as union,
	 * i.e. the base object is the union of all source objects of the result associations.
	 * 
	 * @see TLAssociation#getSubsets()
	 */
	private static final AssociationSetQuery<KnowledgeAssociation> SUBSETS = AssociationQuery.createIncomingQuery(
		"subsets", TL_ASSOCIATION_UNIONS_ASS);

	/**
	 * Query searching the {@link KnowledgeAssociation} which points to the base object as subset,
	 * i.e. the base object is a subset of each destination object of the result association.
	 * 
	 * @see TLAssociation#getUnions()
	 */
	private static final AssociationSetQuery<KnowledgeAssociation> UNIONS = AssociationQuery.createOutgoingQuery(
		"unions", TL_ASSOCIATION_UNIONS_ASS);

	/**
	 * Returns the {@link AssociationQuery queries} that are used by instances of
	 * {@link PersistentAssociation} to cache relations to other model elements.
	 */
	@FrameworkInternal
	public static List<AbstractAssociationQuery<? extends TLObject, ?>> getAssociationQueries() {
		List<AbstractAssociationQuery<? extends TLObject, ?>> queries =
			new ArrayList<>();
		queries.add(SUB_MES_ATTR);
		queries.add(SUPER_ME_ATTR);
		queries.add(UNIONS);
		queries.add(SUBSETS);
		queries.add(ASSOCIATION_PARTS);
		return queries;
	}

	public PersistentAssociation(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public Set<TLAssociation> getSubsets() {
		return AssociationQueryUtil.resolveWrappers(tHandle(), TLAssociation.class, SUBSETS);
	}

	@Override
	public Set<TLAssociation> getUnions() {
		return AssociationQueryUtil.resolveWrappers(tHandle(), TLAssociation.class, UNIONS);
	}

	/**
	 * @param unionPart
	 *        A {@link TLAssociation} which determines this {@link PersistentAssociation} as union
	 *        and the given {@link TLAssociation} is a part from.
	 */
	public void addUnionPart(PersistentAssociation unionPart) {
		tKnowledgeBase().createAssociation(unionPart.tHandle(), tHandle(),
			TL_ASSOCIATION_UNIONS_ASS);
	}

	@Override
	public boolean isAbstract() {
		return !getSubsets().isEmpty();
	}

	@Override
	public boolean isFinal() {
		return !isAbstract();
	}
	
	/**
	 * @implNote Method is redeclared, because the super class {@link KBBasedMetaElement} is
	 *           (technically) a {@link TLClass} with {@link ModelKind#CLASS} which clashes with
	 *           {@link TLAssociation#getModelKind()}.
	 */
	@Override
	public ModelKind getModelKind() {
		return ModelKind.ASSOCIATION;
	}

	@Override
	public <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitAssociation(this, arg);
	}

	@Override
	public List<TLAssociationPart> getLocalParts() {
		return AbstractWrapper.resolveLinks(this, ASSOCIATION_PARTS);
	}
	
	@Override
	public List<? extends TLStructuredTypePart> getAllParts() {
		return getAssociationParts();
	}

	@Override
	public List<TLAssociationPart> getAssociationParts() {
		return getLocalParts();
	}

	@Override
	public List<TLClassPart> getLocalClassParts() {
		throw notTLCLass(this);
	}

	@Override
	public List<TLClass> getGeneralizations() {
		return emptyList();
	}

	@Override
	public Collection<TLClass> getSpecializations() {
		return emptyList();
	}

	@Override
	public List<TLClassPart> getAllClassParts() {
		throw notTLCLass(this);
	}

	private static IllegalStateException notTLCLass(TLClass me) {
		return new IllegalStateException("Type '" + me + "' is not a '" + TLClass.class.getName() + "'");
	}

}

