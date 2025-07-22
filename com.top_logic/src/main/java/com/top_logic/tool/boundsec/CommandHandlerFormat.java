package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;

/**
 * {@link ConfigurationValueProvider} to allow short-cut configuration of {@link CommandHandler}s by
 * giving their ID registered in the {@link CommandHandlerFactory}.
 */
public class CommandHandlerFormat extends AbstractConfigurationValueProvider<CommandHandler.ConfigBase<?>> {

	/**
	 * Creates a {@link CommandHandlerFormat}.
	 */
	public CommandHandlerFormat() {
		super(CommandHandler.ConfigBase.class);
	}

	@Override
	protected ConfigBase<?> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String id = propertyValue.toString();
		if (id.indexOf('.') < 0 ) {
			CommandHandlerReference.Config refConfig =
				TypedConfiguration.newConfigItem(CommandHandlerReference.Config.class);
			refConfig.setCommandId(id);
			return refConfig;
		} else {
			CommandHandler.Config config =
				TypedConfiguration.newConfigItem(CommandHandler.Config.class);
			config.setImplementationClass(ConfigUtil.getClassForNameMandatory(CommandHandler.class, propertyName, id));
			return config;
		}
	}

	@Override
	protected String getSpecificationNonNull(ConfigBase<?> configValue) {
		if (configValue instanceof CommandHandler.Config config) {
			return config.getId();
		} else {
			return ((CommandHandlerReference.Config) configValue).getCommandId();
		}
	}

	@Override
	public boolean isLegalValue(Object value) {
		if (value instanceof CommandHandler.Config config) {
			CommandHandler canonicalValue = CommandHandlerFactory.getInstance().getHandler(config.getId());
			return value == canonicalValue;
		}
		return value instanceof CommandHandlerReference.Config;
	}

}
