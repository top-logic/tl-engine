/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.fieldprovider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.element.meta.form.tag.IndirectDisplayProvider;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;

/**
 * {@link DisplayProvider} for <code>Html</code>-typed attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTextTagProvider extends IndirectDisplayProvider
		implements ConfiguredInstance<StructuredTextTagProvider.Config<?>> {

	private Config<?> _config;

	/**
	 * Configuration options for {@link StructuredTextTagProvider}.
	 */
	public interface Config<I extends StructuredTextTagProvider> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the feature set in the {@link StructuredTextConfigService} to use for editing
		 * report attributes.
		 */
		@Name("feature-set")
		@StringDefault(StructuredTextConfigService.FEATURE_SET_HTML)
		String getFeatureSet();

	}

	/**
	 * Creates a {@link StructuredTextTagProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StructuredTextTagProvider(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		if (editContext.isSearchUpdate()) {
			return new TextInputTag();
		}

		CustomInputTag result = new CustomInputTag();
		result.setControlProvider(new StructuredTextControlProvider(getConfig().getFeatureSet()));
		return result;
	}
}
