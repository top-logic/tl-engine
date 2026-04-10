/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
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
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getHeader();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * Optional footer element (e.g. a bottom bar).
		 */
		@Name(FOOTER)
		@TreeProperty
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
		ErrorSink errorSink = createErrorSink(snackbar);

		// Derive context with error sink for children.
		ViewContext scopedContext = context.withErrorSink(errorSink);

		// Create slot controls in the scoped context.
		ReactControl header = createSlotControl(scopedContext, _header);
		ReactControl content = createSlotControl(scopedContext, _content);
		ReactControl footer = createSlotControl(scopedContext, _footer);

		return new ReactAppShellControl(context, header, content, footer, snackbar, errorSink);
	}

	private static ErrorSink createErrorSink(ReactSnackbarControl snackbar) {
		return new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				snackbar.showHtml(renderToHtml(content), ReactSnackbarControl.Variant.ERROR);
			}

			@Override
			public void showWarning(HTMLFragment content) {
				snackbar.showHtml(renderToHtml(content), ReactSnackbarControl.Variant.WARNING);
			}

			@Override
			public void showInfo(HTMLFragment content) {
				snackbar.showHtml(renderToHtml(content), ReactSnackbarControl.Variant.INFO);
			}
		};
	}

	private static String renderToHtml(HTMLFragment fragment) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		StringWriter buffer = new StringWriter();
		try {
			TagWriter out = new TagWriter(buffer);
			fragment.write(displayContext, out);
		} catch (IOException ex) {
			Logger.error("Failed to render info service message.", ex, AppShellElement.class);
		}
		return buffer.toString();
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
		return new ReactStackControl(context, children);
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
