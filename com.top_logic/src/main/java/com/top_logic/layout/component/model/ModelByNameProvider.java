/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelProvider} returns a model identified by a {@link ModelName} as
 * {@link #getBusinessModel(LayoutComponent) business model} of a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelByNameProvider extends AbstractConfiguredInstance<ModelByNameProvider.Config>
		implements ModelProvider {

	/**
	 * Typed configuration interface definition for {@link ModelByNameProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ModelByNameProvider> {

		/**
		 * Name of the model to return.
		 */
		@Mandatory
		ModelName getModelName();

		/**
		 * Setter of {@link #getModelName()}.
		 */
		void setModelName(ModelName name);
	}

	/**
	 * Create a {@link ModelByNameProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelByNameProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		ActionContext actionContext =
			new LiveActionContext(DefaultDisplayContext.getDisplayContext(), businessComponent);
		ModelName modelName = getConfig().getModelName();
		try {
			return ModelResolver.locateModel(actionContext, modelName);
		} catch (ApplicationAssertion ex) {
			// Model can not be resolved.
			return null;
		}
	}

}

