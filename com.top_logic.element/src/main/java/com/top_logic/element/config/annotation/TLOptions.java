/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.FormContextProxy;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} specifying an algorithm to generate possible options for an
 * attribute.
 * 
 * @see #getGenerator()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("options")
@InApp
@TargetType(value = {
	TLTypeKind.STRING, TLTypeKind.INT, TLTypeKind.FLOAT,
	TLTypeKind.ENUMERATION, TLTypeKind.REF,
	TLTypeKind.CUSTOM })
public interface TLOptions extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Custom tag to create a {@link TLOptions} annotation.
	 */
	String TAG_NAME = "options";

	/** @see #getGenerator() */
	String GENERATOR_PROPERTY = "generator";

	/**
	 * The {@link Generator} computing options for a select field.
	 * 
	 * @see AttributeOperations#getOptions(TLObject, EditContext, FormContextProxy) The configured generator must not be invoked directly by the
	 *      application. The Utility method must be used instead to include type-based default
	 *      options.
	 */
	@Name(GENERATOR_PROPERTY)
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<Generator> getGenerator();

	/** @see #getGenerator() */
	void setGenerator(PolymorphicConfiguration<Generator> value);

}
