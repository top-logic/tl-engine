/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.util;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLType;

/**
 * {@link ValueConstraint} checking that the name is a qualified type name.
 * 
 * @see QualifiedTypePartNameConstraint
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QualifiedTypeNameConstraint extends ValueConstraint<String> {

	/**
	 * Creates a new {@link QualifiedTypeNameConstraint}.
	 */
	public QualifiedTypeNameConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		String value = propertyModel.getValue();
		if (StringServices.isEmpty(value)) {
			return;
		}
		ResKey problem = checkQualifiedTypeName(value);
		if (problem != null) {
			propertyModel.setProblemDescription(problem);
		}
	}

	/**
	 * Checks that the given name is a qualified {@link TLType} name.
	 * 
	 * @param value
	 *        The value to check.
	 * @return <code>null</code> if the name is a qualified name or a {@link ResKey} describing the
	 *         type of invalidity.
	 */
	public static ResKey checkQualifiedTypeName(String value) {
		if (value.indexOf(' ') >= 0 || value.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) < 0) {
			return I18NConstants.ERROR_INVALID_QUALIFIED_TYPE_NAME__VALUE.fill(value);
		}
		return null;
	}

}
