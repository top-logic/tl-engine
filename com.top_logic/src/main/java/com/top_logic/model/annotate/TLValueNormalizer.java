/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.util.ValueNormalizer;

/**
 * {@link TLAttributeAnnotation} attaching {@link ValueNormalizer}s to a model attribute.
 *
 * <p>
 * The normalizers are applied in the order they are given, each one to the result of the one
 * before.
 * </p>
 *
 * <p>
 * The forms and value inputs of the React view layer apply the normalizers to a value when it is
 * taken from the input, before it reaches the edited object. The forms of the classic user
 * interface, scripts and imports store a value as given.
 * </p>
 */
@TagName("value-normalizer")
@InApp
public interface TLValueNormalizer extends TLAttributeAnnotation {

	/** Configuration name for {@link #getNormalizers()}. */
	String NORMALIZERS = "normalizers";

	/**
	 * Algorithms computing the canonical form of a value entered for the annotated attribute,
	 * applied in the given order.
	 */
	@Name(NORMALIZERS)
	@DefaultContainer
	List<PolymorphicConfiguration<? extends ValueNormalizer>> getNormalizers();

}
