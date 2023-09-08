/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.util.Utils;

/**
 * {@link AssociationCache} that filters chached links with a key/value filter.
 * 
 * @see FilteredAssociationQuery#getAttributeQuery()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class FilteredAssociationCache<T extends TLObject, C> extends AssociationCache<T, C> {

	/**
	 * Creates a {@link FilteredAssociationCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	FilteredAssociationCache(KnowledgeItemInternal baseItem, AssociationQueryImpl<T, C> query) {
		super(baseItem, query);
	}

	@Override
	protected final boolean matchesQuery(DBContext context, long lookupRevision, KnowledgeItemInternal link) {
		// Create match via ObjectKey to avoid loading referenced object.
		ObjectKey currentReferencedKey = link.getReferencedKey(getReferenceAttribute(), lookupRevision);
		if (!getBaseItem().tId().equals(currentReferencedKey)) {
			/* Association link does not match the reference the query is for. This occurs when the
			 * attribute of the link is set a different object. */
			return false;
		}

		Map<String, ?> attributeQuery = _query.getAttributeQuery();
		{
			MOStructure type = link.tTable();
			// After a local attribute change, the global link may
			// no longer match the cache query.
			for (Entry<String, ?> queryEntry : attributeQuery.entrySet()) {

				String queryAttribute = queryEntry.getKey();
				Object queryValue = queryEntry.getValue();

				MOAttribute attribute = type.getAttributeOrNull(queryAttribute);
				if (attribute == null) {
					throw new IllegalArgumentException("Query '" + attributeQuery
						+ "' does not match association type '" + _query.getAssociationTypeName() + "'.");
				}
				Object currentValue;
				if (attribute instanceof MOReference) {
					// filter does not hold items but their object keys.
					currentValue = link.getReferencedKey((MOReference) attribute, lookupRevision);
				} else {
					currentValue = link.getValue(attribute, lookupRevision);
				}

				if (!Utils.equals(currentValue, queryValue)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Concrete: This method checks whether the given item has a type which is subtype of
	 * {@link #getAssociationType()}.
	 * 
	 * @see AbstractAssociationCache#hasCacheType(KnowledgeItem)
	 */
	@Override
	public boolean hasCacheType(KnowledgeItem potentialLink) {
		return potentialLink.tTable().isSubtypeOf(getAssociationType());
	}
}
