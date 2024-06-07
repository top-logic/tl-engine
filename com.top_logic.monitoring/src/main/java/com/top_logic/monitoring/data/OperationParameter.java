/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Class to configure a parameter for operations of dynamic MBeans.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class OperationParameter extends AbstractConfiguredInstance<OperationParameter.Config> {

	/** {@link ConfigurationItem} for the {@link AbstractDynamicMBean}. */
	public interface Config extends NamedPolymorphicConfiguration<OperationParameter> {
		/** The type of this parameter. */
		@Options(fun = ParameterTypes.class)
		@Mandatory
		Class<?> getType();

		/** The description of this parameter. */
		String getDescription();
	}

	/** {@link TypedConfiguration} constructor for {@link AbstractDynamicMBean}. */
	public OperationParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Option provider for
	 * {@link com.top_logic.monitoring.data.OperationParameter.Config#getType()}.
	 */
	public static class ParameterTypes extends Function0<Collection<Class<?>>> {
		@Override
		public Collection<Class<?>> apply() {
			List<Class<?>> types = List.of(
				String.class,
				Number.class,
				Boolean.class);
			return types;
		}
	}

}
