/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.annotation;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link TLAttributeAnnotation} annotating an {@link AttributeValueBinding} algorithm to an
 * attribute or type.
 * 
 * <p>
 * The type default is configured in
 * <code>com.top_logic.element.meta.AttributeSettings.Config.AttributeSetting#getExportBinding()</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("export-binding")
@DefaultStrategy(Strategy.PRIMARY_GENERALIZATION)
public interface TLExportBinding extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * @see #getImpl()
	 */
	String IMPL = "impl";

	/**
	 * The {@link AttributeValueBinding} algorithm to export and import the annotated attribute.
	 */
	@Name(IMPL)
	@DefaultContainer
	PolymorphicConfiguration<AttributeValueBinding<?>> getImpl();

}
