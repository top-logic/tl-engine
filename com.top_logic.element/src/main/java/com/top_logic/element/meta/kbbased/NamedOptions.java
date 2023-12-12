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
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link Generator} that delegates to a {@link Generator} in the application configuration.
 * 
 * <p>
 * The {@link Generator} in the application configuration are normally special generators that are
 * generally not reusable but are intended for a specific application case.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(priority = -10)
@Label("Predefined options")
public final class NamedOptions extends AbstractConfiguredInstance<NamedOptions.Config> implements Generator {

	/**
	 * Configuration options of {@link NamedOptions}.
	 */
	@TagName("config-reference")
	public interface Config extends PolymorphicConfiguration<NamedOptions>, NamedConfigMandatory {

		/**
		 * Name of the {@link Generator} in the application configuration to delegate to.
		 */
		@Override
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
		super(context, config);
		String generatorName = config.getName();
		Generator generator = GeneratorFactory.getGenerator(generatorName);
		if (generator == null) {
			throw new ConfigurationException(I18NConstants.NO_SUCH_GENERATOR__NAME.fill(generatorName),
				Config.NAME_ATTRIBUTE, generatorName);
		}
		_impl = generator;
	}

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return _impl.generate(editContext);
	}

}
