/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;

/**
 * {@link ValueConstraint} checking that a {@link ComponentName} is either <code>null</code> or not
 * a {@link ComponentName#isLocalName() local name}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QualifiedComponentNameConstraint extends ValueConstraint<ComponentName> {

	/**
	 * Singleton {@link QualifiedComponentNameConstraint} instance.
	 */
	public static final QualifiedComponentNameConstraint INSTANCE = new QualifiedComponentNameConstraint();

	/**
	 * Creates a new {@link QualifiedComponentNameConstraint}.
	 */
	protected QualifiedComponentNameConstraint() {
		super(ComponentName.class);
	}

	@Override
	protected void checkValue(PropertyModel<ComponentName> propertyModel) {
		ComponentName value = propertyModel.getValue();
		if (value == null) {
			return;
		}
		if (value.isLocalName()) {
			propertyModel.setProblemDescription(
				I18NConstants.ERROR_UNQUALIFIED_COMPONENT_NAME__COMPONENT_NAME.fill(value.localName()));
		}
	}

}

