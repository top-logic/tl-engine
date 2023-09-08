/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link Function1} returning {@link TLClass#getAllParts() all parts} of a set of
 * {@link TLClass}es.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllTypesAttributes
		extends Function1<Collection<? extends TLStructuredTypePart>, Collection<TLModelPartRef>> {

	/**
	 * Singleton {@link AllTypesAttributes} instance.
	 */
	public static AllTypesAttributes INSTANCE = new AllTypesAttributes();

	@Override
	public Collection<? extends TLStructuredTypePart> apply(Collection<TLModelPartRef> typeRefs) {
		Collection<TLStructuredTypePart> parts = new HashSet<>();

		for (TLModelPartRef typeRef : typeRefs) {
			parts.addAll(AllTypeAttributes.INSTANCE.apply(typeRef));
		}

		return parts;
	}
}