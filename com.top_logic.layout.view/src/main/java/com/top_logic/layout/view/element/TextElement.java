/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.common.TextAppearance;
import com.top_logic.layout.react.control.common.TextOverflow;
import com.top_logic.layout.react.control.common.TextTone;
import com.top_logic.layout.react.control.common.TextVariant;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays a text value via the {@link ReactTextControl} ({@code TLText}).
 *
 * <p>
 * Either a static {@link Config#getLabel() label} or the value of an {@link Config#getInput() input
 * channel} (rendered through {@link MetaLabelProvider}, updating reactively when the channel
 * changes).
 * </p>
 *
 * <p>
 * A channel value the model gives a color - an enumeration literal, an object whose type computes
 * the color of its instances - is displayed as a pill in that color.
 * </p>
 *
 * <p>
 * How the text is drawn is stated as roles: a {@link Config#getVariant() typographic role}, a
 * {@link Config#getTone() color role} and the {@link Config#getAppearance() shape} it takes. Each
 * role is filled from the design tokens of the active theme, so an application theme restyles every
 * text of a role at once.
 * </p>
 *
 * <p>
 * The display follows a new value on the channel and a change of the object the channel holds: the
 * label and the color are recomputed together, so the pill of an object whose state was edited
 * follows that edit without the object being selected anew.
 * </p>
 */
@InApp
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

		/** Configuration name for {@link #getOverflow()}. */
		String OVERFLOW = "overflow";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/** Configuration name for {@link #getTone()}. */
		String TONE = "tone";

		/** Configuration name for {@link #getAppearance()}. */
		String APPEARANCE = "appearance";

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
		 * How text longer than the available width is handled: {@link TextOverflow#WRAP wrapped}
		 * onto multiple lines (the default) or truncated on a single line with an
		 * {@link TextOverflow#ELLIPSIS ellipsis}.
		 */
		@Name(OVERFLOW)
		TextOverflow getOverflow();

		/**
		 * What the text is for: {@link TextVariant#BODY running text} (the default), a
		 * {@link TextVariant#TITLE title}, a {@link TextVariant#HEADLINE headline}, a
		 * {@link TextVariant#DISPLAY display} statement, the {@link TextVariant#LABEL label} of a
		 * value or a {@link TextVariant#CAPTION caption}.
		 *
		 * <p>
		 * The font family, size, line height and weight of the role come from the design tokens of
		 * the active theme.
		 * </p>
		 */
		@Name(VARIANT)
		TextVariant getVariant();

		/**
		 * What the color of the text means: the {@link TextTone#PRIMARY color it is read in} (the
		 * default), a {@link TextTone#SECONDARY lesser weight}, an explaining
		 * {@link TextTone#HELPER hint}, an {@link TextTone#ACCENT accent}, or an outcome
		 * ({@link TextTone#SUCCESS success}, {@link TextTone#WARNING warning},
		 * {@link TextTone#ERROR error}); {@link TextTone#ON_COLOR on-color} for text on a filled
		 * surface.
		 *
		 * <p>
		 * The color of the role comes from the design tokens of the active theme.
		 * </p>
		 */
		@Name(TONE)
		TextTone getTone();

		/**
		 * The shape the text is drawn in: {@link TextAppearance#TEXT plain text} (the default), or a
		 * {@link TextAppearance#PILL pill} whether or not the displayed value carries a color of its
		 * own.
		 */
		@Name(APPEARANCE)
		TextAppearance getAppearance();
	}

	private final ResKey _label;

	private final ChannelRef _inputRef;

	private final String _cssClass;

	private final TextOverflow _overflow;

	private final TextVariant _variant;

	private final TextTone _tone;

	private final TextAppearance _appearance;

	/**
	 * Creates a new {@link TextElement} from configuration.
	 */
	@CalledByReflection
	public TextElement(InstantiationContext context, Config config) {
		_label = config.getLabel();
		_inputRef = config.getInput();
		_cssClass = config.getCssClass();
		_overflow = config.getOverflow();
		_variant = config.getVariant();
		_tone = config.getTone();
		_appearance = config.getAppearance();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef);
			Object value = channel.get();
			ReactTextControl control = text(context, label(value));
			control.setColor(ReactValueColor.cssColorOf(value));

			Runnable update = () -> {
				Object current = channel.get();
				control.setText(label(current), ReactValueColor.cssColorOf(current));
			};

			ChannelListener listener = (sender, oldValue, newValue) -> update.run();
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));

			ChannelObjectObserver observer = new ChannelObjectObserver(List.of(channel), Set.of(), update);
			control.addAttachListener(() -> observer.attach(context.getModelScope()));
			control.addDetachListener(observer::detach);

			return control;
		}
		return text(context, _label != null ? Resources.getInstance().getString(_label) : "");
	}

	/**
	 * The control displaying the given text with the configured styling.
	 */
	private ReactTextControl text(ViewContext context, String text) {
		ReactTextControl result = new ReactTextControl(context, text, _cssClass);
		result.setOverflow(_overflow);
		result.setVariant(_variant);
		result.setTone(_tone);
		result.setAppearance(_appearance);
		return result;
	}

	private static String label(Object value) {
		String text = ValueLabel.label(value);
		return text == null ? "" : text;
	}

}
