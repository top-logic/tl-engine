/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactAppShellControl}.
 *
 * <p>
 * Provides three slots: an optional header, a mandatory content area, and an optional footer.
 * </p>
 */
public class AppShellElement implements UIElement {

	/**
	 * Configuration for {@link AppShellElement}.
	 */
	@TagName("app-shell")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AppShellElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getHeader()}. */
		String HEADER = "header";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getFooter()}. */
		String FOOTER = "footer";

		/**
		 * Optional header element (e.g. an app bar).
		 */
		@Name(HEADER)
		PolymorphicConfiguration<? extends UIElement> getHeader();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		@Mandatory
		PolymorphicConfiguration<? extends UIElement> getContent();

		/**
		 * Optional footer element (e.g. a bottom bar).
		 */
		@Name(FOOTER)
		PolymorphicConfiguration<? extends UIElement> getFooter();
	}

	private final UIElement _header;

	private final UIElement _content;

	private final UIElement _footer;

	/**
	 * Creates a new {@link AppShellElement} from configuration.
	 */
	@CalledByReflection
	public AppShellElement(InstantiationContext context, Config config) {
		_header = config.getHeader() != null ? context.getInstance(config.getHeader()) : null;
		_content = context.getInstance(config.getContent());
		_footer = config.getFooter() != null ? context.getInstance(config.getFooter()) : null;
	}

	@Override
	public Control createControl(ViewContext context) {
		ReactControl header = _header != null ? (ReactControl) _header.createControl(context) : null;
		ReactControl content = (ReactControl) _content.createControl(context);
		ReactControl footer = _footer != null ? (ReactControl) _footer.createControl(context) : null;

		return new ReactAppShellControl(header, content, footer);
	}
}
