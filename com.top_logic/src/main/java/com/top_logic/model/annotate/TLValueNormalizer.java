/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.util.ValueNormalizer;

/**
 * {@link TLAttributeAnnotation} attaching a {@link ValueNormalizer} to a model attribute.
 *
 * <p>
 * The forms and value inputs of the React view layer apply the normalizer to a value when it is
 * taken from the input, before it reaches the edited object. The forms of the classic user
 * interface, scripts and imports store a value as given.
 * </p>
 */
@TagName("value-normalizer")
@InApp
public interface TLValueNormalizer extends TLAttributeAnnotation {

	/** Configuration name for {@link #getNormalizer()}. */
	String NORMALIZER = "normalizer";

	/**
	 * Algorithm computing the canonical form of a value entered for the annotated attribute.
	 */
	@Mandatory
	@Name(NORMALIZER)
	@DefaultContainer
	PolymorphicConfiguration<? extends ValueNormalizer> getNormalizer();

	/**
	 * @see #getNormalizer()
	 */
	void setNormalizer(PolymorphicConfiguration<? extends ValueNormalizer> value);

}
