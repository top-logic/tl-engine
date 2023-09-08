/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.model.TLObject;

/**
 * {@link AssociationQueryImpl} that resolves to an (unordered) set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class AssociationSetQueryImpl<T extends TLObject> extends FilteredAssociationQuery<T, Set<T>>
		implements AssociationSetQuery<T> {

	/**
	 * Creates a new {@link AssociationQuery}.
	 * 
	 * <p>
	 * Use {@link AssociationQuery#createQuery(String, Class, String, String, Map)} for creating
	 * queries in the application.
	 * </p>
	 */
	public AssociationSetQueryImpl(NamedConstant cacheKey, Class<T> expectedType, String associationTypeName,
			String referenceAttribute, Map<String, ?> attributeQuery) {
		super(cacheKey, expectedType, associationTypeName, referenceAttribute, attributeQuery);
	}

	@Override
	AbstractAssociationCache<T, Set<T>> createCache(KnowledgeItemInternal baseObject) {
		return new AssociationSetCache<>(baseObject, this);
	}

}
