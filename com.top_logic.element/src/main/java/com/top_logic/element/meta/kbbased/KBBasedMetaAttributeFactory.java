/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;

/**
 * Create/remove MetaAttributes in/from MetaElements.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
@ServiceDependencies({ PersistencyLayer.Module.class })
public class KBBasedMetaAttributeFactory extends MetaAttributeFactory {

	/**
	 * Marker value for {@link TLReference}s.
	 * 
	 * @see KBBasedMetaAttribute#IMPLEMENTATION_NAME
	 * @deprecated Use {@link TLStructuredTypeColumns#REFERENCE_IMPL} instead
	 */
	public static final String REFERENCE_IMPL = TLStructuredTypeColumns.REFERENCE_IMPL;

	/**
	 * Marker value for {@link TLProperty} instances of a {@link TLClass}.
	 * 
	 * @see KBBasedMetaAttribute#IMPLEMENTATION_NAME
	 * @deprecated Use {@link TLStructuredTypeColumns#CLASS_PROPERTY_IMPL} instead
	 */
	public static final String CLASS_PROPERTY_IMPL = TLStructuredTypeColumns.CLASS_PROPERTY_IMPL;

	/**
	 * Marker value for {@link TLProperty} instances of a {@link TLAssociation}.
	 * 
	 * @see KBBasedMetaAttribute#IMPLEMENTATION_NAME
	 * @deprecated Use {@link TLStructuredTypeColumns#ASSOCIATION_PROPERTY_IMPL} instead
	 */
	public static final String ASSOCIATION_PROPERTY_IMPL = TLStructuredTypeColumns.ASSOCIATION_PROPERTY_IMPL;

	/**
	 * Marker value for {@link TLAssociationEnd}s.
	 * 
	 * @see KBBasedMetaAttribute#IMPLEMENTATION_NAME
	 * @deprecated Use {@link TLStructuredTypeColumns#ASSOCIATION_END_IMPL} instead
	 */
	public static final String ASSOCIATION_END_IMPL = TLStructuredTypeColumns.ASSOCIATION_END_IMPL;

	/**
	 * Creates a {@link KBBasedMetaAttributeFactory} from configuration.
	 */
	public KBBasedMetaAttributeFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public TLClassProperty createClassProperty(KnowledgeBase kb) {
		NameValueBuffer initialValues = new NameValueBuffer();
		initialValues.put(ConfiguredAttributeImpl.IMPLEMENTATION_NAME, TLStructuredTypeColumns.CLASS_PROPERTY_IMPL);
		KBBasedMetaAttribute newMetaAttribute = newMetaAttribute(kb, initialValues);
		return (PersistentClassProperty) newMetaAttribute;
	}

	@Override
	public TLAssociationProperty createAssocationProperty(KnowledgeBase kb) {
		NameValueBuffer initialValues = new NameValueBuffer();
		initialValues.put(ConfiguredAttributeImpl.IMPLEMENTATION_NAME, TLStructuredTypeColumns.ASSOCIATION_PROPERTY_IMPL);
		KBBasedMetaAttribute newMetaAttribute = newMetaAttribute(kb, initialValues);
		return (PersistentAssociationProperty) newMetaAttribute;
	}

	@Override
	public TLAssociationEnd createEnd(KnowledgeBase kb) {
		NameValueBuffer initialValues = new NameValueBuffer();
		initialValues.put(ConfiguredAttributeImpl.IMPLEMENTATION_NAME, TLStructuredTypeColumns.ASSOCIATION_END_IMPL);
		PersistentEnd end = (PersistentEnd) newMetaAttribute(kb, initialValues);
		return end;
	}

	@Override
	public TLReference createTLReference(KnowledgeBase kb, TLAssociationEnd end) {
		NameValueBuffer initialValues = new NameValueBuffer();
		initialValues.put(ConfiguredAttributeImpl.IMPLEMENTATION_NAME, TLStructuredTypeColumns.REFERENCE_IMPL);
		PersistentReference reference = (PersistentReference) newMetaAttribute(kb, initialValues);
		reference.tSetDataReference(PersistentReference.END_ATTR, end);
		return reference;
	}

	private KBBasedMetaAttribute newMetaAttribute(KnowledgeBase kb, NameValueBuffer values) {
		try {
			KnowledgeObject theKO = kb.createKnowledgeObject(KBBasedMetaAttribute.OBJECT_NAME, values);

			return (KBBasedMetaAttribute) WrapperFactory.getWrapper(theKO);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Error creating new attribute .", ex);
		}
	}

	@Override
	public void removeMetaAttribute (TLClass aMetaElement, TLStructuredTypePart aMetaAttribute) 
			throws NoSuchAttributeException, IllegalArgumentException {
		super.removeMetaAttribute(aMetaElement, aMetaAttribute);
		
		aMetaAttribute.tDelete();
	}
	
	@Override
	public TLStructuredTypePart getMetaAttribute(TLID anIdentifier) {
		TLStructuredTypePart theMetaAttr=null;
		theMetaAttr = (TLStructuredTypePart) WrapperFactory.getWrapper(anIdentifier, KBBasedMetaAttribute.OBJECT_NAME);
		return theMetaAttr;
	}

}
