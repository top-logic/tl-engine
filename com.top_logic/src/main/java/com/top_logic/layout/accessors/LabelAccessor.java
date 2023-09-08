/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Accessor that returns the label of the object to get value for.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelAccessor extends ReadOnlyAccessor<Object> {

	/**
	 * Configuration of the {@link LabelAccessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<LabelAccessor> {

		/**
		 * {@link LabelProvider} to use to resolve the label for the object.
		 */
		@InstanceFormat
		@InstanceDefault(MetaLabelProvider.class)
		LabelProvider getLabels();
	}

	private LabelProvider _labels;

	/**
	 * Creates a new {@link LabelAccessor} with default configuration.
	 */
	public LabelAccessor() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration.newConfigItem(Config.class));
	}

	/**
	 * Creates a new {@link LabelAccessor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link LabelAccessor}.
	 */
	public LabelAccessor(InstantiationContext context, Config config) {
		_labels = config.getLabels();
	}

	@Override
	public Object getValue(Object object, String property) {
		return _labels.getLabel(object);
	}

}

