/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;

/**
 * Builds a form for the configuration of the given TL service.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLServiceConfigEditorFormBuilder
		extends
		DeclarativeFormBuilder<BasicRuntimeModule<?>, TLServiceConfigEditorFormBuilder.EditModel> {

	/**
	 * Creates a {@link TLServiceConfigEditorFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLServiceConfigEditorFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Model of the displayed service configuration.
	 */
	public interface EditModel extends ConfigurationItem {

		/**
		 * Configuration of a {@link BasicRuntimeModule}.
		 * 
		 * @see ApplicationConfig#getServiceConfiguration(Class)
		 * 
		 * @return Configuration of the given service class.
		 */
		@ControlProvider(GroupInlineControlProvider.class)
		ServiceConfiguration<?> getServiceConfiguration();

		/**
		 * @see #getServiceConfiguration()
		 */
		void setServiceConfiguration(ServiceConfiguration<?> serviceConfiguration);

	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	private ServiceConfiguration<? extends ManagedClass> getServiceConfiguration(
			Class<? extends ManagedClass> serviceClass) {
		try {
			return ConfigCopier.copy(ApplicationConfig.getInstance().getServiceConfiguration(serviceClass));
		} catch (ConfigurationException exception) {
			return null;
		}
	}

	@Override
	protected void fillFormModel(EditModel formModel, BasicRuntimeModule<?> serviceModule) {
		formModel.setServiceConfiguration(getServiceConfig(serviceModule));
	}

	private ServiceConfiguration<? extends ManagedClass> getServiceConfig(BasicRuntimeModule<?> module) {
		return getServiceConfiguration(module.getImplementation());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<? extends BasicRuntimeModule<?>> getModelType() {
		return (Class) BasicRuntimeModule.class;
	}

}
