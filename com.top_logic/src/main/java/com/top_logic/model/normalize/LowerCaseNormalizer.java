/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.normalize;

import java.util.Locale;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Value normalizer converting a text to lower case.
 *
 * <p>
 * The conversion follows the rules that are independent of a language. A value that is not a text
 * is kept unchanged, the texts of a multiple attribute are normalized one by one.
 * </p>
 */
@InApp
@Label("Lower case")
public class LowerCaseNormalizer extends TextNormalizer<LowerCaseNormalizer.Config<?>> {

	/**
	 * Configuration options for {@link LowerCaseNormalizer}.
	 */
	@TagName("lower-case")
	public interface Config<I extends LowerCaseNormalizer> extends PolymorphicConfiguration<I> {
		// No options.
	}

	/**
	 * Creates a {@link LowerCaseNormalizer} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LowerCaseNormalizer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected String normalizeText(String text) {
		return text.toLowerCase(Locale.ROOT);
	}

}
