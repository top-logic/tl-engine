/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ValueConstraint} to check whether the name follows the requested pattern of
 * "domain:key=property".
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MBeanNameConstraint extends ValueConstraint<String> {

	/** Singleton {@link MBeanNameConstraint} instance. */
	public static final MBeanNameConstraint INSTANCE = new MBeanNameConstraint();

	private String NAME_PATTERN = "[a-zA-Z][a-zA-Z0-9_\\.]*";

	/** Creates a new {@link MBeanNameConstraint}. */
	public MBeanNameConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		ResKey error = null;

		error = checkPattern(propertyModel);

		if (error != null) {
			propertyModel.setProblemDescription(error);
			return;
		}
	}

	private ResKey checkPattern(PropertyModel<String> propertyModel) {
		String name = propertyModel.getValue();

		if (name != null && !name.matches(NAME_PATTERN + "(\\:name\\=)" + NAME_PATTERN + "+")) {
			return I18NConstants.WRONG_PATTERN_MBEAN_NAME;
		}

		return null;
	}

}
