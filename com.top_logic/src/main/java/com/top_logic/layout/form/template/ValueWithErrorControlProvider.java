/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ErrorControl;

/**
 * {@link ControlProvider} which delegates the actual creation to another {@link ControlProvider}
 * but creates also an additional {@link ErrorControl} for {@link FormField}s.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueWithErrorControlProvider extends AbstractConfiguredInstance<ValueWithErrorControlProvider.Config>
		implements ControlProvider {

	/**
	 * Configuration options for {@link ValueWithErrorControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<ValueWithErrorControlProvider> {
		/**
		 * The {@link ControlProvider} for creating the value display.
		 */
		@Name("innerControlProvider")
		@InstanceFormat
		@InstanceDefault(DefaultFormFieldControlProvider.class)
		ControlProvider getInnerControlProvider();

		/**
		 * Setter for {@link #getInnerControlProvider()}.
		 */
		void setInnerControlProvider(ControlProvider provider);

		/**
		 * Whether to prepend the error icon before the value (instead of appending it).
		 */
		@Name("errorFirst")
		@BooleanDefault(ERROR_FIRST_DEFAULT)
		boolean getErrorFirst();

		/**
		 * Setter for {@link #getErrorFirst()}.
		 */
		void setErrorFirst(boolean value);
	}

	/** Default instance which dispatches to {@link DefaultFormFieldControlProvider#INSTANCE}. */
	public static final ValueWithErrorControlProvider INSTANCE =
		newInstance(DefaultFormFieldControlProvider.INSTANCE);

	/**
	 * Default instance which dispatches to {@link DefaultFormFieldControlProvider#INSTANCE}, but
	 * creates an {@link ErrorControl} which is displayed before the actual {@link FormField}.
	 */
	public static final ValueWithErrorControlProvider INSTANCE_ERROR_FIRST =
		newInstance(DefaultFormFieldControlProvider.INSTANCE, true);

	/** Default for {@link Config#getErrorFirst()}. */
	private static final boolean ERROR_FIRST_DEFAULT = false;

	/**
	 * Creates an {@link ValueWithErrorControlProvider} with the given {@link ControlProvider} as
	 * implementation.
	 * 
	 * @param innerControlProvider
	 *        the {@link ControlProvider} to dispatch creation of control to.
	 * @param errorFirst
	 *        whether the error control must be displayed before or after the actual control
	 */
	public static ValueWithErrorControlProvider newInstance(ControlProvider innerControlProvider, boolean errorFirst) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setInnerControlProvider(innerControlProvider);
		config.setErrorFirst(errorFirst);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Creates an {@link ValueWithErrorControlProvider} with the given {@link ControlProvider} as
	 * implementation. The {@link ErrorControl} will be rendered after the created control.
	 * 
	 * @param innerControlProvider
	 *        the {@link ControlProvider} to dispatch creation of control to.
	 * 
	 */
	public static ValueWithErrorControlProvider newInstance(ControlProvider innerControlProvider) {
		return newInstance(innerControlProvider, ERROR_FIRST_DEFAULT);
	}

	/**
	 * Creates a {@link ValueWithErrorControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ValueWithErrorControlProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Control createControl(Object model, String style) {
		Config config = getConfig();
		Control innerControl = config.getInnerControlProvider().createControl(model, style);
		if (model instanceof FormMember && !FormTemplateConstants.STYLE_ERROR_VALUE.equals(style)) {
			BlockControl block = new BlockControl();
			ErrorControl errorControl = new ErrorControl((FormMember) model, true);
			if (config.getErrorFirst()) {
				block.addChild(errorControl);
				block.addChild(innerControl);
			} else {
				block.addChild(innerControl);
				block.addChild(errorControl);
			}
			return block;
		} else {
			return innerControl;
		}
	}

}
