/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link TLAttributeAnnotation} attaching {@link ConstraintCheck}s to a model attribute.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("constraints")
@InApp
public interface TLConstraints extends TLAttributeAnnotation {

	/**
	 * Constraints to be evaluated in the context of the annotated attribute.
	 */
	@DefaultContainer
	List<PolymorphicConfiguration<? extends ConstraintCheck>> getConstraints();

}
