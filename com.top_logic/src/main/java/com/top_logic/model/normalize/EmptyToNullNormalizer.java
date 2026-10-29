/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.normalize;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Value normalizer storing an empty text as no value.
 *
 * <p>
 * A text consisting of white space only is not empty. To store such a text as no value as well,
 * the white space is removed by a preceding normalizer that trims the text. A value that is not a
 * text is kept unchanged, the texts of a multiple attribute are normalized one by one.
 * </p>
 */
@InApp
@Label("Empty text to no value")
public class EmptyToNullNormalizer extends TextNormalizer<EmptyToNullNormalizer.Config<?>> {

	/**
	 * Configuration options for {@link EmptyToNullNormalizer}.
	 */
	@TagName("empty-to-null")
	public interface Config<I extends EmptyToNullNormalizer> extends PolymorphicConfiguration<I> {
		// No options.
	}

	/**
	 * Creates a {@link EmptyToNullNormalizer} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EmptyToNullNormalizer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected String normalizeText(String text) {
		return text.isEmpty() ? null : text;
	}

}
