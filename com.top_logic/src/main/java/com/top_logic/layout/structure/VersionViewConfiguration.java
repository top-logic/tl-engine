/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Factory for a view rendered to the application's status bar.
 */
public class VersionViewConfiguration extends AbstractConfiguredInstance<VersionViewConfiguration.Config<?>>
		implements ViewConfiguration {

	/**
	 * Configuration options for {@link VersionViewConfiguration}.
	 */
	public interface Config<I extends VersionViewConfiguration> extends PolymorphicConfiguration<I> {
		/**
		 * See {@link #getEnvironment}.
		 */
		String ENVIRONMENT = "environment";

		/**
		 * See {@link #getShowEnvironment}.
		 */
		String SHOW_ENVIRONMENT = "showEnvironment";

		/** Information about the environment. */
		@Name(ENVIRONMENT)
		String getEnvironment();

		/**
		 * Whether to show the environment in the version field of the application, if the
		 * environment property is set.
		 */
		@Name(SHOW_ENVIRONMENT)
		@BooleanDefault(true)
		boolean getShowEnvironment();
	}

	/**
	 * Creates a {@link VersionViewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public VersionViewConfiguration(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return new VersionModel();
	}

	/**
	 * The model rendered by the HTML template.
	 */
	public class VersionModel extends WithPropertiesBase implements HTMLFragment {

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			Icons.VERSION_VIEW.get().write(context, out, this);
		}

		/**
		 * Renders the version string with optional environment information.
		 * 
		 * @throws IOException
		 *         If rendering fails.
		 */
		@TemplateVariable("version")
		public void writeVersion(DisplayContext context, TagWriter out) throws IOException {
			String environment = getConfig().getEnvironment();

			ResKey contentKey;
			if (getConfig().getShowEnvironment() && !StringServices.isEmpty(environment)) {
				contentKey = I18NConstants.STATUS_BAR__VERSION_ENV
					.fill(Version.getApplicationVersion().getVersionString(), environment);
			} else {
				contentKey = I18NConstants.STATUS_BAR__VERSION.fill(Version.getApplicationVersion().getVersionString());
			}

			out.writeText(context.getResources().getString(contentKey));
		}
	}

}
