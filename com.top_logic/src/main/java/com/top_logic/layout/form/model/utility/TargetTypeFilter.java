/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.util.AnnotationFilter;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AnnotationFilter} selecting implementations that are compatible with a given
 * {@link TLType}.
 * 
 * @see TypeKindFilter
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TargetTypeFilter extends AnnotationFilter {

	private TLType _type;

	private TLTypeKind _kind;

	/**
	 * Creates a {@link TargetTypeFilter}.
	 */
	public TargetTypeFilter(AnnotationCustomizations customizations, TLType type, TLTypeKind kind) {
		super(customizations);

		_type = type;
		_kind = kind;
	}

	@Override
	public boolean test(Class<?> option) {
		TargetType targetAnnotation = getAnnotation(option, TargetType.class);
		if (targetAnnotation == null) {
			return true;
		}

		for (TLTypeKind expectedKind : targetAnnotation.value()) {
			if (expectedKind == _kind) {
				String[] expectedTypeNames = targetAnnotation.name();
				return expectedTypeNames.length == 0 || isAnyCompatible(expectedTypeNames, _type);
			}
		}

		// No matching target type found.
		return false;
	}

	private static boolean isAnyCompatible(String[] expectedTypeNames, TLType actualType) {
		for (String expectedTypeName : expectedTypeNames) {
			if (isCompatible(expectedTypeName, actualType)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCompatible(String expectedTypeName, TLType actualType) {
		try {
			TLType expectedType = TLModelUtil.findType(expectedTypeName);
			return TLModelUtil.isCompatibleType(expectedType, actualType);
		} catch (TopLogicException ex) {
			return false;
		}
	}

}
