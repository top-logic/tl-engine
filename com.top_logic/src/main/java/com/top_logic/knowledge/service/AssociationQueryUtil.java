/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;

/**
 * Utility for navigating {@link AssociationQuery}s based on {@link KnowledgeAssociation} to the
 * other end instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationQueryUtil {

	/**
	 * Execute the given {@link KnowledgeAssociation}-based {@link AssociationQuery} in the context
	 * of the given base object and navigate to the other end instance.
	 * 
	 * @param aKO
	 *        The base object that is the context of the query to execute
	 * @param expectedType
	 *        The expected type on the other end of the association link.
	 * @param query
	 *        the query to execute
	 * 
	 * @return the set of opposite objects that result from the query.
	 */
	public static <C extends Collection<KnowledgeAssociation>, T extends TLObject> Set<T> resolveWrappers(
			KnowledgeObject aKO,
			Class<T> expectedType, AbstractAssociationQuery<? extends KnowledgeAssociation, C> query) {
		KnowledgeBase kb = aKO.getKnowledgeBase();
		C allLinks = kb.resolveLinks(aKO, query);
		boolean outgoingQuery = AssociationQueryUtil.isOutgoing(query);
		switch (allLinks.size()) {
			case 0: {
				return CollectionUtil.newSet(0);
			}
			case 1: {
				HashSet<T> result = CollectionUtil.newSet(1);
				KnowledgeAssociation link = allLinks.iterator().next();
				KnowledgeObject opposite;
				try {
					if (outgoingQuery) {
						opposite = link.getDestinationObject();
					} else {
						opposite = link.getSourceObject();
					}
				} catch (InvalidLinkException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				}
				result.add(CollectionUtil.dynamicCast(expectedType, WrapperFactory.getWrapper(opposite)));
				return result;
			}
			default: {
				String otherAttribute;
				if (outgoingQuery) {
					otherAttribute = DBKnowledgeAssociation.REFERENCE_DEST_NAME;
				} else {
					otherAttribute = DBKnowledgeAssociation.REFERENCE_SOURCE_NAME;
				}
				List<KnowledgeItem> referencedItems;
				try {
					referencedItems = getReferences(kb, allLinks, otherAttribute);
				} catch (NoSuchAttributeException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				}
				// KnowledgeAssociations are designed to reference KnowledgeObjects
				List<KnowledgeObject> referencedObjects =
					CollectionUtil.dynamicCastView(KnowledgeObject.class, referencedItems);
				return wrap(expectedType, referencedObjects);
			}
		}
	}

	/**
	 * Returns a set containing the {@link Wrapper}s for the given {@link KnowledgeObject}s.
	 */
	private static <T extends TLObject> Set<T> wrap(Class<T> expectedType, Collection<KnowledgeObject> kos) {
		Set<T> referencedWrappers = CollectionUtil.newSet(kos.size());
		WrapperFactory.addWrappersForKOs(expectedType, referencedWrappers, kos);
		return referencedWrappers;
	}

	/**
	 * Resolves the referenced {@link KnowledgeItem}s.
	 * 
	 * <p>
	 * All links must be of same type, i.e. there must be a common super type of the concrete types
	 * having an {@link MOReference} with the given name.
	 * </p>
	 * 
	 * @param links
	 *        The referees.
	 * @param attributeName
	 *        The reference to navigate.
	 */
	private static List<KnowledgeItem> getReferences(KnowledgeBase kb, Collection<? extends KnowledgeItem> links,
			String attributeName)
			throws NoSuchAttributeException {
		if (links.size() == 0) {
			// Mutable set because getAllValues(Collection, MOReference) returns also mutable.
			return new ArrayList<>();
		}
		MetaObject representiveType = links.iterator().next().tTable();
		MOReference reference = MetaObjectUtils.getReference(representiveType, attributeName);
		return getReferences(kb, links, reference);
	}

	/**
	 * Resolves the referenced {@link KnowledgeItem}s.
	 * 
	 * <p>
	 * All links must be of same type, i.e. all types must have the given reference.
	 * </p>
	 * 
	 * @param links
	 *        The referees.
	 * @param reference
	 *        The reference to navigate.
	 */
	private static List<KnowledgeItem> getReferences(KnowledgeBase kb, Collection<? extends KnowledgeItem> links,
			MOReference reference) {
		HashSet<ObjectKey> keys = CollectionUtil.newSet(links.size());
		for (KnowledgeItem link : links) {
			ObjectKey referencedKey = link.getReferencedKey(reference);
			keys.add(referencedKey);
		}
		return BulkIdLoad.load(kb, keys);
	}

	/**
	 * Whether a {@link KnowledgeAssociation}-based {@link AssociationQuery} searches links through
	 * the {@link DBKnowledgeAssociation#REFERENCE_SOURCE_NAME} reference (links that start at the
	 * base object on which the query is executed).
	 */
	public static boolean isOutgoing(AbstractAssociationQuery<? extends KnowledgeAssociation, ?> associationQuery) {
		String referenceAttribute = associationQuery.getReferenceAttribute();
		assert checkLegacyReference(referenceAttribute);
		return referenceAttribute.equals(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
	}

	private static boolean checkLegacyReference(String referenceAttribute) throws AssertionError {
		boolean isLegacyReference = referenceAttribute.equals(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
			|| referenceAttribute.equals(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		if (!isLegacyReference) {
			StringBuilder error = new StringBuilder();
			error.append("Expected association query for attribute of legacy association '");
			error.append(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
			error.append("' or '");
			error.append(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
			error.append("' but was '");
			error.append(referenceAttribute);
			error.append("'.");
			throw new AssertionError(error.toString());
		}
		return true;
	}

}
