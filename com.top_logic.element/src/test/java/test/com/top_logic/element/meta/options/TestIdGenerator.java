/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.options;

import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;

/**
 * {@link Generator} with a single option: its configured {@link Config#getId() identifier}.
 */
public class TestIdGenerator extends AbstractConfiguredInstance<TestIdGenerator.Config<?>> implements Generator {

	/**
	 * Configuration options for {@link TestIdGenerator}.
	 */
	public interface Config<I extends TestIdGenerator> extends PolymorphicConfiguration<I> {

		/**
		 * The single option, identifying the generator.
		 */
		@Name("id")
		String getId();

	}

	/**
	 * Creates a {@link TestIdGenerator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TestIdGenerator(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return new DefaultListOptionModel<>(List.of(getConfig().getId()));
	}

}
