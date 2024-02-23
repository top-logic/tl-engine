/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.element.model.DynamicType;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;

/**
 * {@link TLClass} backed by a KnowledgeObject.
 * 
 * @author <a href="mailto:kbu@top-logic.com"></a>
 */
public abstract class KBBasedMetaElement extends DynamicType implements TLClass {

	/** @see #isFinal() */
	private static final String FINAL = TLClass.FINAL_ATTR;

	/** @see #isAbstract() */
	private static final String META_ELEMENT_ABSTRACT = TLClass.ABSTRACT_ATTR;

	/** {@link KnowledgeObject} type used to store {@link TLClass}s in. */
	public static final String META_ELEMENT_KO = ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE;

	/**
	 * {@link KnowledgeObject} attribute used to store the {@link TLClass}
	 * {@link TLClass#getName() name}.
	 */
	public static final String META_ELEMENT_TYPE = ApplicationObjectUtil.META_ELEMENT_ME_TYPE_ATTR;
	
	/**
	 * Marker for different implementations stored in the {@link #META_ELEMENT_KO} table.
	 * 
	 * @see TLStructuredTypeColumns#CLASS_TYPE
	 * @see TLStructuredTypeColumns#ASSOCIATION_TYPE
	 * @deprecated Use {@link TLStructuredTypeColumns#META_ELEMENT_IMPL} instead
	 */
	@Deprecated
	public static final String META_ELEMENT_IMPL = TLStructuredTypeColumns.META_ELEMENT_IMPL;

    /** The KA for getting the sub meta elements */
	protected static final String META_ELEMENT_GENERALIZATIONS = ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS;
	
	/**
	 * The ordering attribute of generalization links.
	 * @deprecated Use {@link TLStructuredTypeColumns#META_ELEMENT_GENERALIZATIONS__ORDER} instead
	 */
	@Deprecated
	public static final String META_ELEMENT_GENERALIZATIONS__ORDER = TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER;

	/**
	 * {@link AssociationQuery} to resolve the super types.
	 */
	public static final OrderedLinkQuery<KnowledgeAssociation> SUPER_ME_ATTR = AssociationQuery.createOrderedLinkQuery(
		"superMetaElement", KnowledgeAssociation.class, META_ELEMENT_GENERALIZATIONS,
		DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER, null, true);
	
	/**
	 * {@link AssociationQuery} to resolve the sub {@link TLClass}s of a given
	 * {@link TLClass}.
	 */
	public static final AssociationSetQuery<KnowledgeAssociation> SUB_MES_ATTR = AssociationQuery.createIncomingQuery(
		"subMetaElements", META_ELEMENT_GENERALIZATIONS);

	/** Creates a {@link KBBasedMetaElement}. */
	public KBBasedMetaElement(KnowledgeObject ko) {
		super(ko);
	}

	/**
	 * generalizations of this {@link TLClass}.
	 * 
	 * @param type
	 *        Actual implementation type.
	 */
	protected final <T extends TLClass> List<T> getGeneralizations(Class<T> type) {
		return AbstractWrapper.resolveOrderedValue(this, SUPER_ME_ATTR, type);
	}

    /** 
     * Return the meta element defined by the given ID.
     * 
     * @param    anID    The unique ID of the requested meta element, must not be <code>null</code>.
     * @return   The requested meta element, never <code>null</code>.
     */
    public static TLClass getInstance(TLID anID) {
		return (TLClass) WrapperFactory.getWrapper(anID, META_ELEMENT_KO);
    }

	@Override
	public boolean isAbstract() {
		return tGetDataBoolean(META_ELEMENT_ABSTRACT);
	}

	@Override
	public void setAbstract(boolean value) {
		tSetDataBoolean(META_ELEMENT_ABSTRACT, value);
	}

	@Override
	public boolean isFinal() {
		return tGetDataBooleanValue(FINAL);
	}

	@Override
	public void setFinal(boolean value) {
		tSetDataBoolean(FINAL, value);
	}

	@Override
	public TLStructuredTypePart getPart(String name) {
		return TLModelCacheService.getOperations().getAttribute(this, name);
	}

	@Override
	public List<? extends TLStructuredTypePart> getAllParts() {
		return TLModelCacheService.getOperations().getAllAttributes(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends TLClassPart> getAllClassParts() {
		/* The cast is okay: Either this is a PersistentClass, in which case all its parts are
		 * TLClassParts. Or this is a PersistentAssociation, in which case this method is overridden
		 * and always throws an exception anyway, making the implementation here irrelevant. */
		return (List<? extends TLClassPart>) getAllParts();
	}

	/**
	 * {@link KBBasedMetaElement} implements {@link TLClass} for technical reasons. Therefore is is
	 * logically <b>not</b> a {@link TLClass}. Default visit method would be wrong.
	 * 
	 * @see com.top_logic.model.TLType#visitType(com.top_logic.model.TLTypeVisitor,
	 *      java.lang.Object)
	 */
	@Override
	public abstract <R, A> R visitType(TLTypeVisitor<R, A> v, A arg);

}
