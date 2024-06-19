/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.version.Version;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.tabbar.TabbarUtil;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * Factory for a application logo view in the header region of a sidebar.
 */
public class LogoViewConfiguration extends AbstractConfiguredInstance<LogoViewConfiguration.Config<?>>
		implements ViewConfiguration {

	/**
	 * Configuration options for {@link LogoViewConfiguration}.
	 */
	public interface Config<I extends LogoViewConfiguration> extends PolymorphicConfiguration<I> {
		/**
		 * The application's logo.
		 * 
		 * <p>
		 * Must be an image path, not a font icon.
		 * </p>
		 */
		@Name("logo")
		@FormattedDefault("theme:IMAGES_APP_LOGO")
		ThemeImage getLogo();

		/**
		 * The application's logo shown in the side bar when minimized.
		 * 
		 * <p>
		 * Must be an image path, not a font icon.
		 * </p>
		 */
		@Name("logoMinimized")
		@FormattedDefault("theme:IMAGES_APP_LOGO_MINIMIZED")
		ThemeImage getLogoMinimized();

		/**
		 * A view typically showing the quick-search.
		 */
		@Name("quickSearch")
		PolymorphicConfiguration<? extends ViewConfiguration> getQuickSearch();

		/**
		 * The component whose context menu is displayed.
		 */
		@Name("contextMenuComponent")
		ComponentName getContextMenuComponent();
	}

	private ViewConfiguration _quickSearchFactory;

	/**
	 * Creates a {@link LogoViewConfiguration}.
	 */
	public LogoViewConfiguration(InstantiationContext context, Config<?> config) {
		super(context, config);

		_quickSearchFactory = context.getInstance(config.getQuickSearch());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return new LogoModel(component, _quickSearchFactory == null ? null : _quickSearchFactory.createView(component));
	}

	/**
	 * Model of the rendered application logo template.
	 */
	public class LogoModel extends WithPropertiesBase implements HTMLFragment {
		private LayoutComponent _component;

		private HTMLFragment _quickSearch;

		/**
		 * Creates a {@link LogoModel}.
		 */
		public LogoModel(LayoutComponent component, HTMLFragment quickSearch) {
			_component = component;
			_quickSearch = quickSearch;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			Icons.LOGO_VIEW.get().write(context, out, this);
		}

		/**
		 * The application's logo.
		 * 
		 * <p>
		 * Must be an image path, not a font icon.
		 * </p>
		 */
		@TemplateVariable("logo")
		public void writeLogo(DisplayContext context, TagWriter out) throws IOException {
			Img logo = (Img) getConfig().getLogo().resolve();
			logo.appendUrl(context, out);
		}

		/**
		 * The application's logo shown in the side bar when minimized.
		 * 
		 * <p>
		 * Must be an image path, not a font icon.
		 * </p>
		 */
		@TemplateVariable("logoMinimized")
		public void writeLogoMinimized(DisplayContext context, TagWriter out) throws IOException {
			Img logo = (Img) getConfig().getLogoMinimized().resolve();
			logo.appendUrl(context, out);
		}

		/**
		 * The application's title.
		 * 
		 * @throws IOException
		 *         When rendering fails.
		 */
		@TemplateVariable("title")
		public void writeTitle(DisplayContext context, TagWriter out) throws IOException {
			out.writeText(context.getResources().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		}

		/**
		 * The application version.
		 * 
		 * @throws IOException
		 *         When rendering fails.
		 */
		@TemplateVariable("version")
		public void writeVersion(TagWriter out) throws IOException {
			out.writeText(Version.getApplicationVersion().getVersionString());
			out.writeText(' ');
			out.writeText('(');
			out.writeText(TLContext.getContext().getCurrentUserName());
			out.writeText(')');
		}

		/**
		 * The popup menu button offering design-mode commands.
		 */
		@TemplateVariable("popup")
		public void writePopUp(DisplayContext context, TagWriter out) throws IOException {
			TabbarUtil.writePopup(_component, getConfig().getContextMenuComponent(), context, out);
		}

		/**
		 * The quick-search input.
		 */
		@TemplateVariable("quickSearch")
		public void writeQuickSearch(DisplayContext context, TagWriter out) throws IOException {
			if (_quickSearch != null) {
				_quickSearch.write(context, out);
			}
		}
	}

}
