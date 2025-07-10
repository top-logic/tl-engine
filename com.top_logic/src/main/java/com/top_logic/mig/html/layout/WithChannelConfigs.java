/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelFactory;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration of additional channels for a component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithChannelConfigs extends ConfigurationItem {

	/**
	 * Configuration for additional channels.
	 * 
	 * <p>
	 * Configuring an additional channel involves providing a name for the new channel, implementing
	 * the channel, and optionally assigning a value to it.
	 * </p>
	 */
	@Key(ChannelConfig.NAME_ATTRIBUTE)
	@DisplayMinimized
	Map<String, ChannelConfig> getAdditionalChannels();

	/**
	 * Configuration of an in app channel.
	 */
	@DisplayOrder({
		ChannelConfig.NAME_ATTRIBUTE,
		ChannelConfig.IMPL,
		ChannelConfig.VALUE,
	})
	interface ChannelConfig extends NamedConfigMandatory {

		/** Configuration name for the property {@link #getImpl()}. */
		String IMPL = "impl";

		/** Configuration name for the property {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The name of the channel created by {@link #getImpl()}.
		 */
		@Override
		String getName();

		/**
		 * The channel implementation which is created for the given {@link #getName() name}.
		 */
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		@Name(IMPL)
		PolymorphicConfiguration<? extends ChannelFactory> getImpl();

		/**
		 * Setter for {@link #getImpl()}.
		 */
		void setImpl(PolymorphicConfiguration<? extends ChannelFactory> value);

		/**
		 * The value of the new channel.
		 */
		@Name(VALUE)
		ModelSpec getValue();

	}

}

