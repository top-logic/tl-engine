/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentResolver} adding a {@link FormStateHandler} to the resolved component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public class FormStateRecorder extends ComponentResolver implements
		ConfiguredInstance<PolymorphicConfiguration<ComponentResolver>> {

	/**
	 * Configuration of a {@link FormStateRecorder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ComponentResolver> {

		/**
		 * Configuration option to request keeping form state over component model changes.
		 */
		String DROP_ON_MODEL_CHANGE_PROPERTY = "dropOnModelChange";

		/**
		 * Whether the state must be dropped when a new model is set.
		 */
		@Name(DROP_ON_MODEL_CHANGE_PROPERTY)
		@BooleanDefault(true)
		boolean getDropOnModelChange();
	}

	private final Config _config;

	/**
	 * Creates a {@link FormStateRecorder} from configuration.
	 */
	public FormStateRecorder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		FormStateHandler handler = new FormStateHandler();
		component.addListener(FormHandler.FORM_CONTEXT_EVENT, handler);
		if (_config.getDropOnModelChange()) {
			component.modelChannel().addListener(handler);
		}
	}

	@Override
	public PolymorphicConfiguration<ComponentResolver> getConfig() {
		return _config;
	}

}

