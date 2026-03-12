/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} configuring the React input control for a model attribute or type.
 *
 * <p>
 * When set on an attribute, it overrides the default control selection. When set on a type, it
 * provides the default control for all attributes of that type (via
 * {@link DefaultStrategy.Strategy#VALUE_TYPE}).
 * </p>
 *
 * @see FieldControlService
 */
@TagName("input-control")
@InApp
@DefaultStrategy(Strategy.VALUE_TYPE)
public interface TLInputControl extends TLAttributeAnnotation, TLTypeAnnotation {

	/** @see #getImpl() */
	String IMPL_PROPERTY = "impl";

	/**
	 * The control provider implementation.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	/** @see #getImpl() */
	void setImpl(PolymorphicConfiguration<? extends ReactFieldControlProvider> value);

}
