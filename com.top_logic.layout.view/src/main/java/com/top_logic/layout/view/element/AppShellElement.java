/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactAppShellControl}.
 *
 * <p>
 * Provides three slots: an optional header, a mandatory content area, and an optional footer. Slot
 * properties use list types so that {@code @TagName} resolution works (e.g. {@code <stack>} inside
 * {@code <content>}). If multiple elements are configured in a slot, they are wrapped in a
 * {@link ReactStackControl}.
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
		List<PolymorphicConfiguration<? extends UIElement>> getHeader();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * Optional footer element (e.g. a bottom bar).
		 */
		@Name(FOOTER)
		List<PolymorphicConfiguration<? extends UIElement>> getFooter();
	}

	private final List<UIElement> _header;

	private final List<UIElement> _content;

	private final List<UIElement> _footer;

	/**
	 * Creates a new {@link AppShellElement} from configuration.
	 */
	@CalledByReflection
	public AppShellElement(InstantiationContext context, Config config) {
		_header = instantiateAll(context, config.getHeader());
		_content = instantiateAll(context, config.getContent());
		_footer = instantiateAll(context, config.getFooter());

		if (_content.isEmpty()) {
			context.error("AppShell element must have a content element.");
		}
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		ReactControl header = createSlotControl(context, _header);
		ReactControl content = createSlotControl(context, _content);
		ReactControl footer = createSlotControl(context, _footer);

		return new ReactAppShellControl(header, content, footer);
	}

	private static ReactControl createSlotControl(ViewContext context, List<UIElement> elements) {
		if (elements.isEmpty()) {
			return null;
		}
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(context);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}

	private static List<UIElement> instantiateAll(InstantiationContext context,
			List<PolymorphicConfiguration<? extends UIElement>> configs) {
		if (configs == null || configs.isEmpty()) {
			return List.of();
		}
		return configs.stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}
}
