/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.meta;

import java.util.Collection;

import com.top_logic.contact.external.ExternalContact;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.CollectionFilterProvider;

/**
 * {@link CollectionFilterProvider} for {@link TLStructuredTypePart}s of type {@link ExternalContact}.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public final class ExternalContactFilterProvider implements CollectionFilterProvider {

	/**
	 * Singleton {@link ExternalContactFilterProvider}
	 * instance.
	 */
	public static final ExternalContactFilterProvider INSTANCE = new ExternalContactFilterProvider();

	private ExternalContactFilterProvider() {
		// Singleton constructor.
	}

	@Override
	public CollectionFilter getFilter(TLStructuredTypePart anAttribute, Collection value, boolean doNegate, boolean searchForEmptyValues, String anAccessPath) {
		return new ExternalContactAttributeFilter(anAttribute, value, doNegate, true, anAccessPath);
	}
}