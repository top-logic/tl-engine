/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.report;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.wysiwyg.ui.MacroControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.model.search.persistency.attribute.report.ui.ReportControlProvider;
import com.top_logic.model.search.persistency.attribute.report.ui.ReportTagProvider;
import com.top_logic.util.TLContext;

/**
 * {@link ReportTagProvider} that allows editing the report template with an HTML editor.
 */
public class HtmlReportTagProvider extends ReportTagProvider
		implements ConfiguredInstance<HtmlReportTagProvider.Config<?>> {

	private Config<?> _config;

	/**
	 * Configuration options for {@link HtmlReportTagProvider}.
	 */
	public interface Config<I extends HtmlReportTagProvider> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the feature set in the {@link StructuredTextConfigService} to use for editing
		 * report attributes.
		 * 
		 * @see StructuredTextConfigService#getEditorConfig(String, String, List, String)
		 */
		@Name("feature-set")
		@StringDefault(MacroControlProvider.FEATURE_SET)
		String getFeatureSet();

	}

	/**
	 * Creates a {@link HtmlReportTagProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HtmlReportTagProvider(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	protected ReportControlProvider createControlProvider(EditContext editContext) {
		StructuredTextConfigService service = StructuredTextConfigService.getInstance();
		String language = TLContext.getLocale().getLanguage();
		String editorConfig = service.getEditorConfig(getConfig().getFeatureSet(), language, null, null);

		return new HtmlReportControlProvider(editContext.getObject(), editorConfig);
	}

}
