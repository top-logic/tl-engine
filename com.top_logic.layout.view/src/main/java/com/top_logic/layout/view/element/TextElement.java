/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays a text value via the {@link ReactTextControl} ({@code TLText}).
 *
 * <p>
 * Either a static {@link Config#getLabel() label} or the value of an {@link Config#getInput() input
 * channel} (rendered through {@link MetaLabelProvider}, updating reactively when the channel
 * changes).
 * </p>
 */
public class TextElement implements UIElement {

	/**
	 * Configuration for {@link TextElement}.
	 */
	@TagName("text")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TextElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		/** Configuration name for {@link #getOverflow()}. */
		String OVERFLOW = "overflow";

		/**
		 * Static text to display. Ignored when an {@link #getInput() input channel} is set.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Channel whose value is displayed (rendered via {@link MetaLabelProvider}).
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional additional CSS class appended to the default {@code tlText} class.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();

		/**
		 * How text longer than the available width is handled: {@link TextOverflow#WRAP wrapped}
		 * onto multiple lines (the default) or truncated on a single line with an
		 * {@link TextOverflow#ELLIPSIS ellipsis}.
		 */
		@Name(OVERFLOW)
		TextOverflow getOverflow();
	}

	private final ResKey _label;

	private final ChannelRef _inputRef;

	private final String _cssClass;

	/**
	 * Creates a new {@link TextElement} from configuration.
	 */
	@CalledByReflection
	public TextElement(InstantiationContext context, Config config) {
		_label = config.getLabel();
		_inputRef = config.getInput();
		_cssClass = cssClass(config);
	}

	private static String cssClass(Config config) {
		String userClass = config.getCssClass();
		if (config.getOverflow() != TextOverflow.ELLIPSIS) {
			return userClass;
		}
		String ellipsis = "tlText--ellipsis";
		return userClass == null || userClass.isEmpty() ? ellipsis : ellipsis + " " + userClass;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef);
			ReactTextControl control = new ReactTextControl(context, label(channel.get()), _cssClass);
			ChannelListener listener = (sender, oldValue, newValue) -> control.setText(label(newValue));
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
			return control;
		}
		String text = _label != null ? Resources.getInstance().getString(_label) : "";
		return new ReactTextControl(context, text, _cssClass);
	}

	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

}
