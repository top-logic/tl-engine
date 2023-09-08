/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAnnotation} that provides a {@link AttributeValueLocator} for locating a template for a
 * {@link Document}-typed attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("template-locator")
@TargetType(value = TLTypeKind.REF, name = Document.DOCUMENT_TYPE)
@InApp
public interface Template extends TLAttributeAnnotation {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * Algorithm to locate a template {@link Document}.
	 */
	@Name(VALUE)
	@Mandatory
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends AttributeValueLocator> getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(PolymorphicConfiguration<? extends AttributeValueLocator> value);

}
