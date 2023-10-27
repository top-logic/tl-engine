/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.setting;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} updating the output channel of a component.
 * 
 * @see WithOutputChannel
 */
public class UpdateOutput extends AbstractCommandHandler {

	private final OutputProducer _producer;

	/**
	 * Configuration options for {@link UpdateOutput}.
	 */
	@TagName("update-output")
	public interface Config<I extends UpdateOutput> extends AbstractCommandHandler.Config {

		/**
		 * The function creating a new output value.
		 */
		@Mandatory
		PolymorphicConfiguration<OutputProducer> getProducer();

		@Override
		Class<? extends I> getImplementationClass();

	}

	/**
	 * Creates a {@link UpdateOutput} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateOutput(InstantiationContext context, UpdateOutput.Config<?> config) {
		super(context, config);

		_producer = context.getInstance(config.getProducer());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		FormContext formContext = ((FormHandler) component).getFormContext();
		boolean ok = formContext.checkAll();
		if (!ok) {
			return AbstractApplyCommandHandler.createErrorResult(formContext);
		}
		formContext.store();

		Object newValue = _producer.createOutput(context, component, model);
		((WithOutputChannel) component).setOutput(newValue);

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return I18NConstants.PRODUCE_OUTPUT_COMMAND;
	}

}