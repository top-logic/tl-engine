/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link DisplayProvider} using {@link CustomInputTag}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomInputTagProvider extends AbstractConfiguredInstance<CustomInputTagProvider.Config<?>>
		implements DisplayProvider {

	/**
	 * Configuration options for {@link CustomInputTagProvider}.
	 */
	public interface Config<I extends CustomInputTagProvider> extends PolymorphicConfiguration<I> {
		/**
		 * The {@link ControlProvider} to use for displaying the field.
		 * 
		 * <p>
		 * If no {@link ControlProvider} is specified, {@link DefaultFormFieldControlProvider} is
		 * used by default.
		 * </p>
		 */
		@Name("controlProvider")
		@InstanceFormat
		ControlProvider getControlProvider();
	}

	/**
	 * Creates a {@link CustomInputTagProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomInputTagProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return cp(member).createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	private ControlProvider cp(FormMember member) {
		ControlProvider controlProvider = getConfig().getControlProvider();
		if (controlProvider == null) {
			ControlProvider cp = member.getControlProvider();
			if (cp != null) {
				controlProvider = cp;
			} else {
				controlProvider = DefaultFormFieldControlProvider.INSTANCE;
			}
		}
		return controlProvider;
	}

}
