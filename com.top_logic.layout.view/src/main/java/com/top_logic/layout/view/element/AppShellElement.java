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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;

/**
 * UIElement that wraps {@link ReactAppShellControl}.
 *
 * <p>
 * Provides four slots: an optional header, an optional notice area, a mandatory content area, and
 * an optional footer. Slot properties use list types so that {@code @TagName} resolution works
 * (e.g. {@code <stack>} inside {@code <content>}). If multiple elements are configured in a slot,
 * they are wrapped in a {@link com.top_logic.layout.react.control.layout.ReactStackControl}.
 * </p>
 *
 * <p>
 * A slot holds at most one element of each kind. A same-path overlay of the view therefore
 * extends the element of a kind the base view already places in a slot - e.g. adds items to the
 * sidebar of the content slot or sets the title of the app bar in the header slot - instead of
 * placing a second one beside it.
 * </p>
 *
 * @implNote The slot lists are keyed by the configuration interface of their entries, which makes
 *           an entry of an overlay an update of the base's entry of the same kind.
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

		/** Configuration name for {@link #getNotices()}. */
		String NOTICES = "notices";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getFooter()}. */
		String FOOTER = "footer";

		/**
		 * Optional header element (e.g. an app bar).
		 */
		@Name(HEADER)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getHeader();

		/**
		 * Optional system-wide notices displayed between header and content.
		 *
		 * <p>
		 * Holds elements that announce a state of the whole application rather than of any single
		 * view, e.g. a {@code <maintenance-notice/>}. Each of them decides for itself whether it
		 * has anything to say; the area occupies no space while all of them stay silent.
		 * </p>
		 */
		@Name(NOTICES)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getNotices();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * Optional footer element (e.g. a bottom bar).
		 */
		@Name(FOOTER)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getFooter();
	}

	private final List<UIElement> _header;

	private final List<UIElement> _notices;

	private final List<UIElement> _content;

	private final List<UIElement> _footer;

	private final String _cssClass;

	/**
	 * Creates a new {@link AppShellElement} from configuration.
	 */
	@CalledByReflection
	public AppShellElement(InstantiationContext context, Config config) {
		_header = instantiateAll(context, config.getHeader());
		_notices = instantiateAll(context, config.getNotices());
		_content = instantiateAll(context, config.getContent());
		_footer = instantiateAll(context, config.getFooter());

		if (_content.isEmpty()) {
			context.error("AppShell element must have a content element.");
		}
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(
			ChildGroup.elements(_header),
			ChildGroup.elements(_notices),
			ChildGroup.elements(_content),
			ChildGroup.elements(_footer));
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

		// Create slot controls. Each of the four structural slots (header, notices, content, footer)
		// gets its own slot-path segment so that <slot> placeholders and <slot-content>
		// contributions declared in different regions have distinct positions for routing.
		ReactControl header = createSlotControl(scopedContext.withChildSlotPath("header"), _header);
		ReactControl notices = createSlotControl(scopedContext.withChildSlotPath("notices"), _notices);
		ReactControl content = createSlotControl(scopedContext.withChildSlotPath("content"), _content);
		ReactControl footer = createSlotControl(scopedContext.withChildSlotPath("footer"), _footer);

		ReactAppShellControl shellControl =
			new ReactAppShellControl(context, header, notices, content, footer, snackbar, errorSink);
		shellControl.setCssClass(_cssClass);
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
