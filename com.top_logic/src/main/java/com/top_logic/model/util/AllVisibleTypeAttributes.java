/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLVisibility;
import com.top_logic.model.annotate.Visibility;

/**
 * Returning all {@link TLTypePart}'s for a given {@link TLClass} excluding parts which are
 * explicitly mark as hidden through the {@link TLVisibility} annotation or are duplicates.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllVisibleTypeAttributes extends AllTypesAttributes {

	/**
	 * Singleton {@link AllVisibleTypeAttributes} instance.
	 */
	@SuppressWarnings("hiding")
	public static AllVisibleTypeAttributes INSTANCE = new AllVisibleTypeAttributes();

	@Override
	public Collection<? extends TLStructuredTypePart> apply(Collection<TLModelPartRef> typeReferences) {
		Collection<? extends TLStructuredTypePart> typeParts = super.apply(typeReferences);

		List<TLStructuredTypePart> visibleTypeParts = new ArrayList<>();

		for (TLStructuredTypePart typePart : typeParts) {
			if (isVisible(typePart)) {
				visibleTypeParts.add(typePart);
			}
		}

		return visibleTypeParts;
	}

	private boolean isVisible(TLStructuredTypePart typePart) {
		return DisplayAnnotations.getVisibility(typePart) != Visibility.HIDDEN;
	}

}
