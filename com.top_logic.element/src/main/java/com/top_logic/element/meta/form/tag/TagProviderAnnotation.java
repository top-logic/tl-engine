/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} for annotating a {@link DisplayProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("tag-provider")
@DefaultStrategy(Strategy.PRIMARY_GENERALIZATION)
public interface TagProviderAnnotation extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration of the {@link DisplayProvider} to use.
	 */
	@Mandatory
	PolymorphicConfiguration<? extends DisplayProvider> getImpl();

}

