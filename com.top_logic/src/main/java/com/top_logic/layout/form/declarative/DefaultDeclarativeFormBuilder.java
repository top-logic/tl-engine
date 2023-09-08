/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Configurable {@link DeclarativeFormBuilder} without any initialization code of the form model
 * based on the business model.
 * 
 * <p>
 * For non-trivial applications, the {@link DeclarativeFormBuilder} should be sub-classed directly.
 * </p>
 * 
 * @see DeclarativeFormBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDeclarativeFormBuilder extends DeclarativeFormBuilder<Object, ConfigurationItem> {

	/**
	 * Configuration options for {@link DefaultDeclarativeFormBuilder}.
	 */
	public interface Config extends DeclarativeFormBuilder.Config {

		/**
		 * See {@link #getModelType()}
		 */
		public static final String MODEL_TYPE = "modelType";

		/**
		 * See {@link #getFormType()}
		 */
		public static final String FORM_TYPE = "formType";

		/**
		 * Required type of the application model.
		 */
		@Name(MODEL_TYPE)
		@ClassDefault(Object.class)
		Class<? extends Object> getModelType();

		/**
		 * Form configuration item type.
		 */
		@Name(FORM_TYPE)
		@Mandatory
		Class<? extends ConfigurationItem> getFormType();

	}

	/**
	 * Creates a {@link DefaultDeclarativeFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultDeclarativeFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected Class<? extends Object> getModelType() {
		return config().getModelType();
	}

	@Override
	protected Class<? extends ConfigurationItem> getFormType(Object contextModel) {
		return config().getFormType();
	}

	@Override
	protected void fillFormModel(ConfigurationItem formModel, Object businessModel) {
		// No default values.
	}

}
