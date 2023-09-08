/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Factory returning constantly a given {@link SlideReplacerBuilder.TextCutter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StaticTextCutterFactory extends AbstractConfiguredInstance<StaticTextCutterFactory.Config>
		implements TextCutterFactory {

	/**
	 * Configuration for a {@link StaticTextCutterFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<StaticTextCutterFactory> {

		/**
		 * The {@link SlideReplacerBuilder.TextCutter} which is returned by the configured
		 * {@link TextCutterFactory}.
		 */
		@InstanceFormat
		@Mandatory
		SlideReplacerBuilder.TextCutter getTextCutter();
	}

	/**
	 * Creates a new {@link StaticTextCutterFactory} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link StaticTextCutterFactory}.
	 */
	public StaticTextCutterFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public SlideReplacerBuilder.TextCutter createTextCutter(List<String> columns) {
		return getConfig().getTextCutter();
	}

}
