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
import com.top_logic.model.TLTypePart;

/**
 * {@link ValueConstraint} checking that the name is a qualified type name.
 * 
 * @see QualifiedTypeNameConstraint
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QualifiedTypePartNameConstraint extends ValueConstraint<String> {

	/**
	 * Creates a new {@link QualifiedTypePartNameConstraint}.
	 */
	public QualifiedTypePartNameConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		String value = propertyModel.getValue();
		if (StringServices.isEmpty(value)) {
			return;
		}
		ResKey problem = checkQualifiedTypePartName(value);
		if (problem != null) {
			propertyModel.setProblemDescription(problem);
		}
	}

	/**
	 * Checks that the given name is a qualified {@link TLTypePart} name.
	 * 
	 * @param value
	 *        The value to check.
	 * @return <code>null</code> if the name is a qualified name or a {@link ResKey} describing the
	 *         type of invalidity.
	 */
	public ResKey checkQualifiedTypePartName(String value) {
		int partSeparator = value.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
		if (partSeparator < 0) {
			return I18NConstants.ERROR_INVALID_QUALIFIED_TYPE_PART_NAME__VALUE.fill(value);
		}
		ResKey error = QualifiedTypeNameConstraint.checkQualifiedTypeName(value.substring(0, partSeparator));
		if (error != null) {
			return I18NConstants.ERROR_INVALID_QUALIFIED_TYPE_PART_NAME__VALUE.fill(value);
		}
		if (value.substring(partSeparator + 1).indexOf(' ') >= 0) {
			return I18NConstants.ERROR_INVALID_QUALIFIED_TYPE_PART_NAME__VALUE.fill(value);
		}
		return null;
	}

}
