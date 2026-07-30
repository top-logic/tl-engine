/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import java.util.stream.Collectors;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;

/**
 * UIElement that wraps {@link ReactAppShellControl}.
 *
 * <p>
 * Provides three slots: an optional header, a mandatory content area, and an optional footer. Slot
 * properties use list types so that {@code @TagName} resolution works (e.g. {@code <stack>} inside
 * {@code <content>}). If multiple elements are configured in a slot, they are wrapped in a
 * {@link com.top_logic.layout.react.control.layout.ReactStackControl}.
 * </p>
 */
@InApp
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
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getHeader();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * Optional footer element (e.g. a bottom bar).
		 */
		@Name(FOOTER)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
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
	public IReactControl createControl(ViewContext context) {
		// Create snackbar and error sink first.
		ReactSnackbarControl snackbar = new ReactSnackbarControl(context, "",
			ReactSnackbarControl.Variant.SUCCESS, () -> { /* no-op */ });
		ErrorSink errorSink = snackbar.asErrorSink();

		// Establish a shared command scope so that commands contributed by descendant
		// elements (forms, dashboards, ...) can bubble up to the app bar in the header.
		// If a parent already provides a scope, reuse it.
		CommandScope sharedScope = context.getScope(CommandScope.class);
		if (sharedScope == null) {
			sharedScope = new CommandScope(List.of());
		}

		// Derive context with error sink and shared command scope. The context-menu overlay belongs
		// to the browser window, so the opener is inherited from the enclosing context rather than
		// established here.
		ViewContext scopedContext = context
			.withErrorSink(errorSink)
			.withScope(CommandScope.class, sharedScope);

		// Create slot controls. Each of the three structural slots (header, content, footer) gets
		// its own slot-path segment so that <slot> placeholders and <slot-content> contributions
		// declared in different regions have distinct positions for routing.
		ReactControl header = createSlotControl(scopedContext.withChildSlotPath("header"), _header);
		ReactControl content = createSlotControl(scopedContext.withChildSlotPath("content"), _content);
		ReactControl footer = createSlotControl(scopedContext.withChildSlotPath("footer"), _footer);

		ReactAppShellControl shellControl =
			new ReactAppShellControl(context, header, content, footer, snackbar, errorSink);
		shellControl.attach();
		return shellControl;
	}

	private static ReactControl createSlotControl(ViewContext context, List<UIElement> elements) {
		if (elements.isEmpty()) {
			return null;
		}
		return ContentControls.toControl(elements, context);
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
