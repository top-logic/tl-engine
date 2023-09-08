/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configured {@link Generator} that dispatches to the {@link GeneratorFactory}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NamedOptions implements Generator, ConfiguredInstance<NamedOptions.Config> {

	/**
	 * Configuration options of {@link NamedOptions}.
	 */
	@TagName("config-reference")
	public interface Config extends PolymorphicConfiguration<NamedOptions> {

		/**
		 * The {@link Generator} name form {@link GeneratorFactory}.
		 */
		@Mandatory
		@Options(fun = AllGeneratorNames.class)
		String getName();

		/**
		 * All {@link Generator} names registered in the {@link GeneratorFactory}.
		 */
		class AllGeneratorNames extends Function0<List<String>> {
			@Override
			public List<String> apply() {
				List<String> result = new ArrayList<>();
				result.addAll(GeneratorFactory.Module.INSTANCE.getImplementationInstance().getGeneratorNames());
				Collections.sort(result);
				return result;
			}
		}

	}

	private final Config _config;

	private final Generator _impl;

	/**
	 * Creates a {@link NamedOptions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NamedOptions(InstantiationContext context, Config config) throws ConfigurationException {
		_config = config;
		Generator generator = GeneratorFactory.getGenerator(config.getName());
		if (generator == null) {
			throw new ConfigurationException("No such global generator: " + config.getName());
		}
		_impl = generator;
	}

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return _impl.generate(editContext);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
