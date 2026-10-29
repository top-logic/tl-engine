/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.normalize;

import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Value normalizer removing white space at the beginning and at the end of a text and replacing
 * each sequence of white space within the text by a single space.
 *
 * <p>
 * A line break or a tab within the text also becomes a single space. A value that is not a text is
 * kept unchanged, the texts of a multiple attribute are normalized one by one.
 * </p>
 */
@InApp
@Label("Collapse white space")
public class CollapseWhitespaceNormalizer extends TextNormalizer<CollapseWhitespaceNormalizer.Config<?>> {

	/** A sequence of characters that are {@link Character#isWhitespace(char) white space}. */
	private static final Pattern WHITESPACE = Pattern.compile("\\p{javaWhitespace}+");

	/**
	 * Configuration options for {@link CollapseWhitespaceNormalizer}.
	 */
	@TagName("collapse-whitespace")
	public interface Config<I extends CollapseWhitespaceNormalizer> extends PolymorphicConfiguration<I> {
		// No options.
	}

	/**
	 * Creates a {@link CollapseWhitespaceNormalizer} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CollapseWhitespaceNormalizer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected String normalizeText(String text) {
		return WHITESPACE.matcher(text.strip()).replaceAll(" ");
	}

}
