/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Comparator;
import java.util.List;

import com.top_logic.model.TLObject;

/**
 * {@link FilteredAssociationQuery} that establishes an order in the retrieved links.
 */
public interface OrderedLinkQuery<T extends TLObject> extends AbstractAssociationQuery<T, List<T>> {

	/**
	 * The attribute according to which {@link #getLinkOrder()} sorts.
	 */
	public String getOrderAttribute();

	/**
	 * The {@link Comparator} for ordering links according to {@link #getOrderAttribute()}.
	 */
	public Comparator<? super T> getLinkOrder();

	/**
	 * Whether the query result should be modifiable.
	 */
	public boolean hasLiveResult();

	/**
	 * Whether the oder attribute is actually an index attribute.
	 */
	public boolean orderIsIndex();

}
