/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.func.Function4;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ui.model.AttributeStep;
import com.top_logic.model.util.TLModelUtil;

/**
 * All {@link TLStructuredTypePart}s from the context type that are either references, or match the
 * given expected type.
 * 
 * @see AttributeStep#getAttribute() Option provider for property.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ReferencesOrCompatibleParts
		extends Function4<List<TLStructuredTypePart>, TLType, TLType, Boolean, String> {

	/** The {@link ReferencesOrCompatibleParts} instance. */
	public static final ReferencesOrCompatibleParts INSTANCE = new ReferencesOrCompatibleParts();

	@Override
	public List<TLStructuredTypePart> apply(TLType contextType, TLType expectedType, Boolean expectedMultiple,
			String configName) {
		if (containsNull(contextType, expectedType, expectedMultiple, configName)) {
			return emptyList();
		}
		if (!(contextType instanceof TLClass)) {
			return emptyList();
		}
		TLClass contextClass = (TLClass) contextType;
		List<TLStructuredTypePart> result = new ArrayList<>();
		result.addAll(getUsefulReferences(contextClass, expectedType, expectedMultiple));
		result.addAll(getCompatibleProperties(contextClass, expectedType, expectedMultiple));
		return ModelBasedSearch.getInstance().filterModel(result, configName);
	}

	/**
	 * Returns references to {@link TLStructuredType}s and references to the expectedType.
	 */
	private List<TLReference> getUsefulReferences(TLClass contextClass, TLType expectedType,
			boolean expectedMultiple) {
		List<TLReference> result = new ArrayList<>();
		for (TLReference reference : TLModelUtil.getAllReferences(contextClass)) {
			if (reference.isMultiple() && !expectedMultiple) {
				continue;
			}
			TLType actualType = reference.getType();
			if (TLModelUtil.isCompatibleType(expectedType, actualType)) {
				result.add(reference);
			} else if (isNavigable(actualType)) {
				result.add(reference);
			}
		}
		return result;
	}

	private boolean isNavigable(TLType actualType) {
		// Note: Enums are not navigable, but are structured types!
		return actualType.getModelKind() == ModelKind.CLASS || actualType.getModelKind() == ModelKind.ASSOCIATION;
	}

	private List<TLStructuredTypePart> getCompatibleProperties(TLClass contextClass, TLType expectedType,
			Boolean expectedMultiple) {
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLProperty property : TLModelUtil.getAllProperties(contextClass)) {
			if (property.isMultiple() && !expectedMultiple) {
				continue;
			}
			if (SearchTypeUtil.areComparableWithEachOther(expectedType, property.getType())) {
				result.add(property);
			} else if (isNavigable(property.getType())) {
				result.add(property);
			}
		}
		return result;
	}

}
