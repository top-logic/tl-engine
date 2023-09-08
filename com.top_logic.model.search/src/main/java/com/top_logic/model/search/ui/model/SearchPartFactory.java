/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.util.Resources;

/**
 * Factory for {@link SearchPart}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchPartFactory {

	/**
	 * Creates {@link PredefinedReferenceValue}s for the {@link PredefinedSearchParameter}s of the
	 * given {@link Search}.
	 * 
	 * @param search
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static List<ReferenceValue> predefinedReferenceValues(Search search) {
		return predefinedReferenceValues(search.getPredefinedParameters());
	}

	/**
	 * Creates {@link PredefinedReferenceValue}s for the {@link PredefinedSearchParameter}s of the
	 * given {@link Search}.
	 * 
	 * @param searchParameters
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static List<ReferenceValue> predefinedReferenceValues(
			Collection<? extends PredefinedSearchParameter> searchParameters) {
		List<ReferenceValue> result = list();
		for (PredefinedSearchParameter searchParameter : searchParameters) {
			result.add(predefinedReferenceValue(searchParameter));
		}
		return result;
	}

	/**
	 * Creates a {@link PredefinedReferenceValue} for the given {@link PredefinedSearchParameter}.
	 * 
	 * @param searchParameter
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static PredefinedReferenceValue predefinedReferenceValue(PredefinedSearchParameter searchParameter) {
		PredefinedReferenceValue result = newConfigItem(PredefinedReferenceValue.class);
		result.setTranslation(searchParameter.getTranslation());
		result.setName(searchParameter.getName());
		result.setValueType(searchParameter.getValueType());
		result.setValueMultiplicity(searchParameter.getValueMultiplicity());
		return result;
	}

	/**
	 * Creates a {@link ReferenceValue} for the given {@link NamedDefinition}.
	 * <p>
	 * Creates a {@link PredefinedReferenceValue} if the {@link NamedDefinition} is a
	 * {@link PredefinedSearchParameter}.
	 * </p>
	 * 
	 * @param namedDefinition
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static ReferenceValue referenceValue(NamedDefinition namedDefinition) {
		if (namedDefinition instanceof PredefinedSearchParameter) {
			return predefinedReferenceValue((PredefinedSearchParameter) namedDefinition);
		}
		return referenceValueInternal(namedDefinition);
	}

	private static ReferenceValue referenceValueInternal(NamedDefinition namedDefinition) {
		ReferenceValue result = newConfigItem(ReferenceValue.class);
		result.setName(namedDefinition.getName());
		result.setValueType(namedDefinition.getValueType());
		result.setValueMultiplicity(namedDefinition.getValueMultiplicity());
		return result;
	}

	/**
	 * Factory method for {@link PredefinedSearchParameter}.
	 * <p>
	 * None of the parameters is allowed to be null.
	 * </p>
	 * 
	 * @return Never null.
	 */
	public static PredefinedSearchParameter create(ResKey translation, Object value, TLType valueType,
			boolean valueMultiplicity) {
		PredefinedSearchParameter result = newConfigItem(PredefinedSearchParameter.class);
		result.setName(toName(translation));
		result.setTranslation(requireNonNull(translation));
		result.setValue(requireNonNull(value));
		result.setValueType(requireNonNull(valueType));
		result.setValueMultiplicity(requireNonNull(valueMultiplicity));
		return result;
	}

	private static String toName(ResKey translation) {
		Locale defaultLocale = ResourcesModule.getInstance().getDefaultLocale();
		return Resources.getInstance(defaultLocale).getString(translation);
	}

}
