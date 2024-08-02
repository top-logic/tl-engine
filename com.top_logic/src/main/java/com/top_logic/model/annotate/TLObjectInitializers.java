/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.initializer.TLObjectInitializer;

/**
 * {@link TLTypeAnnotation} that holds initializers to execute when a new item of the annotated type
 * is created.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@TagName("object-initializers")
@TargetType({ TLTypeKind.REF })
public interface TLObjectInitializers extends TLTypeAnnotation {

	/**
	 * The {@link TLObjectInitializer}s to execute when a new item of the annotated type is created.
	 */
	@Options(fun = AllInAppImplementations.class)
	@DefaultContainer
	List<PolymorphicConfiguration<TLObjectInitializer>> getInitializers();

}
