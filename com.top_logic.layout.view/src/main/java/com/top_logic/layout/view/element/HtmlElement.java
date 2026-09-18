/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.view.HtmlValues;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays HTML content via the {@link ReactHtmlControl} ({@code TLHtml}).
 *
 * <p>
 * The content is read from an input channel and kept in sync with it: a new value on the channel
 * replaces what is displayed. The channel may carry the HTML source as text, an
 * {@link HTMLFragment} - what an HTML literal of a script expression evaluates to - or a
 * {@link BinaryData} document of content type {@code text/html}. A value of any other type has no
 * HTML representation, and a message saying so is displayed in its place.
 * </p>
 *
 * <p>
 * Content displayed as part of the page passes the application's HTML safety check first. Content
 * the check rejects - a script, an attribute that carries one - is not displayed; its place is taken
 * by the message the check reports. Content displayed as a document or as a thumbnail is not
 * checked: it is isolated from the page in a frame of its own, in which no script is executed.
 * </p>
 */
@InApp
public class HtmlElement implements UIElement {

	/**
	 * Configuration for {@link HtmlElement}.
	 */
	@TagName("html")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		@Override
		@ClassDefault(HtmlElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose value is displayed as HTML.
		 *
		 * <p>
		 * The HTML source as text, a fragment produced by an HTML literal of a script expression,
		 * or a document of content type {@code text/html}. A value of another type is reported in
		 * place of the content.
		 * </p>
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * How the content is displayed.
		 */
		@Name(DISPLAY)
		HtmlDisplay getDisplay();

		/**
		 * Optional additional CSS class appended to the default {@code tlHtml} class.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();
	}

	private final ChannelRef _inputRef;

	private final HtmlDisplay _display;

	private final String _cssClass;

	/**
	 * Creates a new {@link HtmlElement} from configuration.
	 */
	@CalledByReflection
	public HtmlElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_display = config.getDisplay();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactHtmlControl control = new ReactHtmlControl(context, _display.getExternalName(), _cssClass);

		ViewChannel channel = context.resolveChannel(_inputRef);
		display(control, channel.get());

		ChannelListener listener = (sender, oldValue, newValue) -> display(control, newValue);
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * Displays the given channel value, or the reason why it is not displayed.
	 */
	private void display(ReactHtmlControl control, Object value) {
		String html;
		try {
			html = HtmlValues.toHtml(value);
		} catch (I18NRuntimeException ex) {
			control.setError(message(ex));
			return;
		}

		if (_display == HtmlDisplay.INLINE) {
			try {
				SafeHTML.getInstance().check(html);
			} catch (I18NException ex) {
				control.setError(message(ex));
				return;
			}
		}

		control.setHtml(html);
	}

	/**
	 * The message reported to the user for the given failure.
	 */
	private static String message(I18NFailure failure) {
		return Resources.getInstance().getString(failure.getErrorKey());
	}

}
