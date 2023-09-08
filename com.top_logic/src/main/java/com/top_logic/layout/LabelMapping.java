/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * {@link Mapping} that maps objects to labels using a delegate
 * {@link LabelProvider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelMapping extends AbstractConfiguredInstance<LabelMapping.Config> implements Mapping<Object, String> {

	/** A default instance of {@link LabelMapping}. */
	public static final LabelMapping INSTANCE = newLabelMapping(MetaResourceProvider.INSTANCE);

	/**
	 * Configuration for a {@link LabelMapping}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<LabelMapping> {

		/**
		 * The {@link LabelProvider} to compute labels for mapped objects.
		 */
		@InstanceFormat
		@InstanceDefault(MetaResourceProvider.class)
		LabelProvider getLabels();

		/**
		 * Setter for {@link #getLabels()}.
		 */
		void setLabels(LabelProvider labels);

	}

	/**
	 * Creates a new {@link LabelMapping} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link LabelMapping}.
	 */
	public LabelMapping(InstantiationContext context, Config config) {
		super(context, config);
	}

	public static LabelMapping newLabelMapping(LabelProvider labelProvider) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setLabels(labelProvider);
		return TypedConfigUtil.createInstance(config);
	}

	@Override
	public String map(Object input) {
		return getConfig().getLabels().getLabel(input);
    }

}
