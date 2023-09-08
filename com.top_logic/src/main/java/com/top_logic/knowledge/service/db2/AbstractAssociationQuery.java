/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * Common interface for queries resolving links from the {@link KnowledgeBase} cache.
 */
public interface AbstractAssociationQuery<T extends TLObject, C> {

	/**
	 * The name of the reference to navigate.
	 */
	String getReferenceAttribute();

	/**
	 * The dynamic type of object expected to be found.
	 */
	Class<T> getExpectedType();

	/**
	 * The unique name of this query that identifies this query within its base object.
	 */
	NamedConstant getCacheKey();

	/**
	 * The type name of the type that is queried.
	 */
	String getAssociationTypeName();

	/**
	 * Map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	Map<String, ?> getAttributeQuery();

}
