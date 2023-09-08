/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.wrap.binding.ImplementationBinding;
import com.top_logic.knowledge.wrap.binding.MonomorphicBinding;

/**
 * {@link MOAnnotation} that adds an {@link ImplementationBinding algorithm} to a {@link MOClass}
 * for determining the Java class to instantiate for rows in that table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImplementationBindingAnnotation extends MOAnnotation {

	/**
	 * The {@link ImplementationBinding} to use for the annotated table.
	 */
	@ImplementationClassDefault(MonomorphicBinding.class)
	PolymorphicConfiguration<? extends ImplementationBinding> getBinding();

	/**
	 * @see #getBinding()
	 */
	void setBinding(PolymorphicConfiguration<? extends ImplementationBinding> value);

}
