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
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
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
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.SingleInput;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
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
 *
 * <p>
 * A {@link Config#getTooltip() tooltip} explains the text to the user pointing at it. Over an input
 * channel it is computed from the channel value and follows it as the label does.
 * </p>
 */
@InApp
public class TextElement implements UIElement {

	/**
	 * Configuration for {@link TextElement}.
	 */
	@TagName("text")
	@TypeIcon("css:bi bi-file-text")
	public interface Config extends UIElement.Config, SingleInput {

		@Override
		@ClassDefault(TextElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getOverflow()}. */
		String OVERFLOW = "overflow";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/** Configuration name for {@link #getTone()}. */
		String TONE = "tone";

		/** Configuration name for {@link #getAppearance()}. */
		String APPEARANCE = "appearance";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/**
		 * Static text to display. Ignored when an {@link #getInput() input channel} is set.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Channel whose value is displayed (rendered via {@link MetaLabelProvider}).
		 *
		 * <p>
		 * The {@link #getTooltip() tooltip} sees the value as a variable named after the channel, or
		 * by the name given with {@code as}: {@code state as s} names it {@code s}.
		 * </p>
		 */
		@Override
		@Name(INPUT)
		@Nullable
		@Format(InputRefFormat.class)
		InputRef getInput();

		/**
		 * How text longer than the available width is handled: {@link TextOverflow#WRAP wrapped}
		 * onto multiple lines (the default) or truncated on a single line with an
		 * {@link TextOverflow#ELLIPSIS ellipsis}.
		 */
		@Name(OVERFLOW)
		TextOverflow getOverflow();

		/**
		 * What the text is for, a role of the type scale of the design system:
		 * {@link TextVariant#BODY running text} (the default) or
		 * {@link TextVariant#BODY_STRONG running text that stands out}, a
		 * {@link TextVariant#DISPLAY display} statement such as a key figure, a heading of one of
		 * three levels ({@link TextVariant#HEADING_LG large}, {@link TextVariant#HEADING_MD medium},
		 * {@link TextVariant#HEADING_SM small}), {@link TextVariant#LABEL small text} such as the
		 * name of a value or {@link TextVariant#LABEL_STRONG small text that stands out}, an
		 * {@link TextVariant#OVERLINE overline} above a heading, or {@link TextVariant#CODE code} in
		 * a fixed-width font.
		 *
		 * <p>
		 * The font family, size, weight, line height and letter spacing of the role come from the
		 * design tokens of the active theme.
		 * </p>
		 */
		@Name(VARIANT)
		TextVariant getVariant();

		/**
		 * What the color of the text means: the {@link TextTone#PRIMARY color it is read in} (the
		 * default), a {@link TextTone#SECONDARY lesser weight}, an explaining
		 * {@link TextTone#HELPER hint}, a {@link TextTone#PLACEHOLDER placeholder}, or an outcome
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

		/**
		 * The tooltip of the text: a TL-Script expression computing what the user reads when
		 * pointing at it.
		 *
		 * <p>
		 * With an {@link #getInput() input channel}, the expression sees the channel value as a
		 * variable and is recomputed together with the displayed text, whenever the channel takes
		 * another value or the object it holds changes. Without one, the expression is a constant,
		 * e.g. a text in the languages of the application:
		 * <code>#("Explanation"@en, "Erklärung"@de)</code>.
		 * </p>
		 *
		 * <p>
		 * The result is a text, an internationalized text, or any other value shown by its label;
		 * an empty text or nothing at all leaves the text without a tooltip. Unset, the text has
		 * none.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Nullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();
	}

	private final ResKey _label;

	private final InputRef _inputRef;

	private final String _cssClass;

	private final TextOverflow _overflow;

	private final TextVariant _variant;

	private final TextTone _tone;

	private final TextAppearance _appearance;

	private final QueryExecutor _tooltip;

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
		_tooltip = InputScript.compile(config, Config.TOOLTIP);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef.getChannel());
			Object value = channel.get();
			ReactTextControl control = text(context, label(value));
			control.setColorRole(ReactValueColor.roleOf(value));
			setTooltip(control, value);

			Runnable update = () -> {
				Object current = channel.get();
				control.setText(label(current), ReactValueColor.roleOf(current));
				setTooltip(control, current);
			};

			ChannelListener listener = (sender, oldValue, newValue) -> update.run();
			Registration registration = channel.addListener(listener);
			control.addCleanupAction(registration::dispose);

			ChannelObjectObserver observer = new ChannelObjectObserver(List.of(channel), Set.of(), update);
			control.addAttachListener(() -> observer.attach(context.getModelScope()));
			control.addDetachListener(observer::detach);

			return control;
		}
		ReactTextControl control = text(context, _label != null ? Resources.getInstance().getString(_label) : "");
		if (_tooltip != null) {
			setTooltipHtml(control, AbstractColumnDeclaration.tooltipHtml(_tooltip.execute()));
		}
		return control;
	}

	/**
	 * Sets the tooltip the {@link Config#getTooltip() tooltip expression} computes from the given
	 * input value.
	 */
	private void setTooltip(ReactTextControl control, Object value) {
		if (_tooltip == null) {
			return;
		}
		setTooltipHtml(control, AbstractColumnDeclaration.tooltipHtml(_tooltip.execute(value)));
	}

	private static void setTooltipHtml(ReactTextControl control, String html) {
		control.setTooltip(html, null, false);
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
