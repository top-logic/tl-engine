/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configEdit;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * {@link CommandHandler} to remove an entry from the {@link PropertyDescriptor property} of a
 * {@link ConfigurationItem} model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveConfiguration extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link RemoveConfiguration}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Reference to the base object to remove an element from.
		 */
		ModelSpec getItem();

		/**
		 * The property to remove the value form.
		 * 
		 * <p>
		 * The value is allowed to be a dot-separated path of property names.
		 * </p>
		 */
		String getProperty();

	}

	/**
	 * Creates a {@link RemoveConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		Config config = (Config) getConfig();

		ModelSpec itemSpec = config.getItem();
		ConfigurationItem item = (ConfigurationItem) ChannelLinking.eval(component, itemSpec);
		ValueModel valueModel = DefaultValueModel.resolvePath(item, config.getProperty());
		valueModel.removeValue(model);

		AddNewConfiguration.invalidateTarget(itemSpec, component);

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return NullModelDisabled.INSTANCE;
	}

}
