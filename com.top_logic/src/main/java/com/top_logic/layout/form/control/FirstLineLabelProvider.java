/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.regex.Matcher;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.format.WikiWrite;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link LabelProvider} displaying a potential multi-line text value by cutting of the first line
 * of text with an optional character limit.
 * 
 * @see Config#getFullText()
 * @see Config#getMaxChars()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FirstLineLabelProvider extends AbstractConfiguredInstance<FirstLineLabelProvider.Config<?>>
		implements LabelProvider {

	/**
	 * Configuration options for {@link FirstLineLabelProvider}.
	 */
	public interface Config<I extends FirstLineLabelProvider> extends PolymorphicConfiguration<I> {

		/**
		 * {@link LabelProvider} producing the full-text of the displayed object.
		 * 
		 * <p>
		 * From the resulting text, the first line is extracted and returned.
		 * </p>
		 */
		@Name("fullTextProvider")
		@ItemDefault(MetaLabelProvider.class)
		PolymorphicConfiguration<? extends LabelProvider> getFullText();

		/**
		 * @see #getFullText()
		 */
		void setFullText(PolymorphicConfiguration<? extends LabelProvider> value);

		/**
		 * Optional character limit.
		 * 
		 * <p>
		 * The resulting text never exceeds the given number of characters.
		 * </p>
		 */
		@Name("maxChars")
		Integer getMaxChars();

		/**
		 * @see #getMaxChars()
		 */
		void setMaxChars(Integer value);
	}

	/**
	 * Default {@link FirstLineLabelProvider} instance with all options set to their respective
	 * default values.
	 */
	// Note: Explicit cast is required for certain Java compilers.
	@SuppressWarnings("unchecked")
	public static final LabelProvider DEFAULT_INSTANCE =
		(LabelProvider) TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(Config.class));

	/**
	 * Showed at the end if more characters than the allowed amount exist.
	 */
	public static final String TEXT_POPUP_ELLIPSIS = "...";

	private LabelProvider _fullText;

	private int _maxChars;

	/**
	 * Creates a {@link FirstLineLabelProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FirstLineLabelProvider(InstantiationContext context, Config<?> config) {
		super(context, config);

		_fullText = context.getInstance(config.getFullText());
		Integer maxChars = getConfig().getMaxChars();
		_maxChars = maxChars == null ? 0 : maxChars.intValue();
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}

		String text = _fullText.getLabel(object);
		if (text == null) {
			return null;
		}

		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return null;
		}

		Matcher matcher = WikiWrite.NEWLINE_PATTERN.matcher(trimmed);
		int endOfFirstLine;
		if (matcher.find(0)) {
			endOfFirstLine = matcher.start();
		} else {
			endOfFirstLine = -1;
		}

		boolean addEllipsis;
		if (endOfFirstLine < 0) {
			addEllipsis = false;
			endOfFirstLine = trimmed.length();
		} else {
			addEllipsis = true;
		}
		
		if (_maxChars > 0 && endOfFirstLine > _maxChars) {
			addEllipsis = true;
			endOfFirstLine = Math.max(_maxChars - TEXT_POPUP_ELLIPSIS.length(), 0);
		}
		
		if (addEllipsis) {
			return trimmed.substring(0, endOfFirstLine) + TEXT_POPUP_ELLIPSIS;
		} else {
			return trimmed;
		}
	}

}
