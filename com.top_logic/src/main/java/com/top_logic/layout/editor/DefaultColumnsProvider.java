/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Provides the default collection of column names for a {@link TLModelPartRef}.
 * 
 * @see TLModelPartRef
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultColumnsProvider extends Function1<List<String>, TLModelPartRef> {

	/**
	 * Singleton {@link DefaultColumnsProvider} instance.
	 */
	public static DefaultColumnsProvider INSTANCE = new DefaultColumnsProvider();

	@Override
	public List<String> apply(TLModelPartRef typeRef) {
		if (typeRef != null) {
			return getDefaultColumnNames(typeRef);
		}

		return Collections.emptyList();
	}

	private List<String> getDefaultColumnNames(TLModelPartRef typeRef) {
		try {
			TLType type = typeRef.resolveType();

			if (type instanceof TLStructuredType) {
				return getDefaultColumnNames((TLStructuredType) type);
			}
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(I18NConstants.MODEL_TYPE_NOT_RESOLVED_ERROR, exception);
		}

		return Collections.emptyList();
	}

	private List<String> getDefaultColumnNames(TLStructuredType type) {
		List<String> mainProperties = DisplayAnnotations.getMainProperties(type);

		if (mainProperties.isEmpty()) {
			return getAllVisibleAttributes(type);
		} else {
			return mainProperties;
		}
	}

	private List<String> getAllVisibleAttributes(TLStructuredType type) {
		List<String> partNames = new ArrayList<>();

		for (TLStructuredTypePart part : type.getAllParts()) {
			if (isVisible(part)) {
				partNames.add(part.getName());
			}
		}

		return partNames;
	}

	private boolean isVisible(TLStructuredTypePart typePart) {
		return DisplayAnnotations.getVisibility(typePart) != Visibility.HIDDEN;
	}

}
