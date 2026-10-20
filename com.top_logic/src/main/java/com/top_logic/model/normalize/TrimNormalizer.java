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
 * Value normalizer removing white space at the beginning and at the end of a text.
 *
 * <p>
 * A value that is not a text is kept unchanged, the texts of a multiple attribute are normalized
 * one by one.
 * </p>
 */
@InApp
@Label("Trim")
public class TrimNormalizer extends TextNormalizer<TrimNormalizer.Config<?>> {

	/**
	 * Configuration options for {@link TrimNormalizer}.
	 */
	@TagName("trim")
	public interface Config<I extends TrimNormalizer> extends PolymorphicConfiguration<I> {
		// No options.
	}

	/**
	 * Creates a {@link TrimNormalizer} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TrimNormalizer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected String normalizeText(String text) {
		return text.strip();
	}

}
