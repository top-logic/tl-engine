/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Utilities for storing reference values using association links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LinkStorageUtil {

	/**
	 * Creates an outgoing {@link AssociationQuery} that resolves the given
	 * {@link TLStructuredTypePart} value.
	 * 
	 * @param metaAttribute
	 *        The meta attribute to create a query for.
	 * @param storage
	 *        The {@link AssociationStorage} to create query for.
	 * @return The query that implements the reference {@link TLStructuredTypePart} getter.
	 */
	static AssociationSetQuery<KnowledgeAssociation> createOutgoingQuery(TLStructuredTypePart metaAttribute, AssociationStorage storage) {
		String queryName = metaAttribute.getName();
		Map<String, ?> attributeQuery = query(metaAttribute, storage);
		return AssociationQuery.createQuery(queryName, KnowledgeAssociation.class, storage.getTable(),
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, attributeQuery);
	}

	private static Map<String, ?> query(TLStructuredTypePart metaAttribute, AssociationStorage storage) {
		Map<String, ?> attributeQuery;
		if (storage.monomophicTable()) {
			attributeQuery = Collections.emptyMap();
		} else {
			TLStructuredTypePart definition = WrapperMetaAttributeUtil.getDefinition(metaAttribute);
			attributeQuery = Collections.singletonMap(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR,
				definition.tId());
		}
		return attributeQuery;
	}

	static IndexedLinkQuery<KnowledgeItem, KnowledgeAssociation> createLiveQuery(TLStructuredTypePart metaAttribute,
			AssociationStorage storage) {
		Map<String, ?> attributeQuery = query(metaAttribute, storage);

		return IndexedLinkQuery.indexedLinkQuery(new NamedConstant("live"), KnowledgeAssociation.class,
			storage.getTable(),
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME,
			KnowledgeItem.class, attributeQuery, true);
	}

	/**
	 * Creates an incoming {@link AssociationQuery} that resolves the given
	 * {@link TLStructuredTypePart} value.
	 * 
	 * @param metaAttribute
	 *        The meta attribute to create a query for.
	 * @param storage
	 *        The {@link AssociationStorage} to create query for.
	 * @return The query that implements the reference {@link TLStructuredTypePart} getter.
	 */
	static AssociationSetQuery<KnowledgeAssociation> createIncomingQuery(TLStructuredTypePart metaAttribute, AssociationStorage storage) {
		String queryName = metaAttribute.getName() + "References";
		Map<String, ?> attributeQuery = query(metaAttribute, storage);
		return AssociationQuery.createIncomingQuery(queryName, storage.getTable(), attributeQuery);
	}

	static KnowledgeAssociation createWrapperAssociation(TLStructuredTypePart attribute,
			TLObject source, TLObject destination, AssociationStorage storage) throws AttributeException {
		// Note: The source reference may be initially null, since it is set when this link is
		// inserted into its association cache list.
		if (destination == null) {
			return null;
		}

		TLStructuredTypePart definition = WrapperMetaAttributeUtil.getDefinition(attribute);
		KnowledgeItem definitionKO = definition.tHandle();

		KnowledgeItem srcKO = source == null ? null : source.tHandle();
		KnowledgeItem dstKO = destination.tHandle();
		KnowledgeBase kb = definitionKO.getKnowledgeBase();
		String linkTable = storage.getTable();

		KnowledgeAssociation link = kb.createAssociation(srcKO, dstKO, linkTable);
		if (!storage.monomophicTable()) {
			link.setAttributeValue(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR, definitionKO);
		}
		return link;
	}

}
