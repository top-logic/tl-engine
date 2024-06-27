/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.util.regex.Pattern;

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

	private static final String CHARACTERS_PATTERN = "[a-zA-Z][a-zA-Z0-9_\\.]*";

	private static final String KEY_PATTERN = "(\\:name\\=)";

	private static final Pattern FULL_NAME_PATTERN = Pattern.compile(CHARACTERS_PATTERN + KEY_PATTERN + CHARACTERS_PATTERN + "+");

	/** Creates a new {@link MBeanNameConstraint}. */
	public MBeanNameConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		ResKey error = checkPattern(propertyModel);

		if (error != null) {
			propertyModel.setProblemDescription(error);
		}
	}

	private ResKey checkPattern(PropertyModel<String> propertyModel) {
		String name = propertyModel.getValue();

		if (name != null && !FULL_NAME_PATTERN.matcher(name).matches()) {
			return I18NConstants.WRONG_PATTERN_MBEAN_NAME;
		}

		return null;
	}

}
