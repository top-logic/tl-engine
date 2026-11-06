/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.text.Format;
import java.text.NumberFormat;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.MandatoryIf;
import com.top_logic.basic.format.FormatFactory;
import com.top_logic.basic.format.NumberFormatResult;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.NumberDisplay;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.control.form.SliderRange;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.ui.FormatBase;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * {@link ReactFieldControlProvider} for integer and floating-point attributes.
 *
 * <p>
 * The value is displayed in and entered in the {@link Config#getFormat() format} of the provider,
 * or where it states none, in the {@link FieldSpec#getNumberFormat() format the field asks for} -
 * the attribute's format annotation, where it has one -, so a German user reads and types
 * {@code 12,5} where an English user reads and types {@code 12.5}.
 * </p>
 *
 * <p>
 * The {@link Config#getUnit() unit} of the provider, or where it states none, the
 * {@link FieldSpec#getUnit() unit of the field} - the attribute's {@link TLUnit} annotation, where
 * it has one -, is written after the value of an input and of a slider, and after the bounds of a
 * slider.
 * </p>
 *
 * <p>
 * The number is typed as text, unless the {@link Config#getDisplay() display} asks for a handle
 * travelling along a track between the {@link Config#getMin() smallest} and the
 * {@link Config#getMax() largest} value.
 * </p>
 *
 * <p>
 * The range of a slider is computed by TL-Script expressions. Without {@link Config#getInputs()
 * inputs}, the range is constant and computed once; a constant range a slider cannot travel is a
 * configuration error. With inputs, the range is computed from the values of the input channels
 * for each control and again whenever one of them changes; while that range cannot be travelled,
 * the slider is disabled. The unit of an input follows the input channels the same way.
 * </p>
 */
@Label("Number input")
@InApp
@TargetType({ TLTypeKind.INT, TLTypeKind.FLOAT })
public class NumberInputControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link NumberInputControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<NumberInputControlProvider>, NamedInputs {

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getMin()}. */
		String MIN = "min";

		/** Configuration name for {@link #getMax()}. */
		String MAX = "max";

		/** Configuration name for {@link #getStep()}. */
		String STEP = "step";

		/** Configuration name for {@link #getUnit()}. */
		String UNIT = "unit";

		/** Configuration name for {@link #getFormat()}. */
		String FORMAT = "format";

		@Override
		@ClassDefault(NumberInputControlProvider.class)
		Class<? extends NumberInputControlProvider> getImplementationClass();

		/**
		 * How the value is edited.
		 *
		 * <p>
		 * An input takes any number the format of the field reads, whatever its size. A slider
		 * offers the numbers of a range and is dragged rather than typed, which suits a value whose
		 * range is part of what it means; it needs the {@link #getMin() smallest} and the
		 * {@link #getMax() largest} value of that range.
		 * </p>
		 */
		@Name(DISPLAY)
		NumberDisplay getDisplay();

		/**
		 * The smallest value a slider can set, which it must be given.
		 *
		 * <p>
		 * A TL-Script expression over the {@link #getInputs() inputs}, which it uses as variables
		 * without declaring them: {@code 0} for a fixed bound, {@code $target['min']} for one read
		 * from a channel. Its result is the value the handle stands on at the left end of the
		 * track; a result that is no number leaves the slider without a range. An input has no
		 * range to travel and ignores it.
		 * </p>
		 *
		 * <p>
		 * Each value of the {@link #getInputs() inputs} is a variable named after its channel, or
		 * by the name given with {@code as}. The expressions of the range are computed again
		 * whenever one of these channels changes. Without inputs, they are constant.
		 * </p>
		 */
		@Name(MIN)
		@MandatoryIf(other = @Ref(DISPLAY), value = NumberDisplay.SLIDER_NAME)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getMin();

		/**
		 * The largest value a slider can set, which it must be given.
		 *
		 * <p>
		 * A TL-Script expression over the {@link #getInputs() inputs}, like the {@link #getMin()
		 * smallest} one. Its result is the value the handle stands on at the right end of the
		 * track, above the smallest one. An input has no range to travel and ignores it.
		 * </p>
		 */
		@Name(MAX)
		@MandatoryIf(other = @Ref(DISPLAY), value = NumberDisplay.SLIDER_NAME)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getMax();

		/**
		 * The distance between two values a slider can set, whole numbers where nothing is stated.
		 *
		 * <p>
		 * A TL-Script expression over the {@link #getInputs() inputs}, like the {@link #getMin()
		 * smallest} value. Its result is the grid the handle snaps to and must be above zero: a
		 * step of {@code 0.5} offers every half, a step of {@code 10} every tenth value of the
		 * range. An input has no grid and ignores it.
		 * </p>
		 */
		@Name(STEP)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getStep();

		/**
		 * The unit of the value, written after the value of an input or a slider and after the
		 * bounds of a slider.
		 *
		 * <p>
		 * A TL-Script expression over the {@link #getInputs() inputs}, like the {@link #getMin()
		 * smallest} value: {@code '%'} for a fixed unit, {@code $target['unit']} for one read from
		 * a channel. An internationalized text is written in the language of the user; an empty
		 * result writes no unit.
		 * </p>
		 *
		 * <p>
		 * A unit stated here wins over the {@link TLUnit unit annotation} of the attribute. Without
		 * one, the value is written with the unit of that annotation.
		 * </p>
		 */
		@Name(UNIT)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getUnit();

		/**
		 * The format the value is displayed in and entered in, such as {@code <format><decimal
		 * pattern="0.###"/></format>} for a number written with up to three decimal places.
		 *
		 * <p>
		 * The same {@link FormatBase#getDefinition() definition} as in the format annotation of a
		 * model attribute, written in the language of the user. A format stated here wins over that
		 * annotation for the input, the text beside the handle of a slider and its bounds. Without
		 * one, the value is written in the format of the attribute.
		 * </p>
		 */
		@Name(FORMAT)
		FormatBase getFormat();
	}

	private final NumberDisplay _display;

	private final List<InputRef> _inputs;

	private final QueryExecutor _min;

	private final QueryExecutor _max;

	private final QueryExecutor _step;

	private final QueryExecutor _unit;

	/**
	 * The definition of the {@link Config#getFormat() format}, {@code null} for the format of the
	 * field.
	 */
	private final FormatFactory _format;

	/**
	 * The range of every slider, when it does not depend on {@link Config#getInputs() inputs}.
	 */
	private final SliderRange _constantRange;

	/**
	 * Creates a {@link NumberInputControlProvider} taking the value as typed text.
	 */
	public NumberInputControlProvider() {
		_display = NumberDisplay.INPUT;
		_inputs = List.of();
		_min = null;
		_max = null;
		_step = null;
		_unit = null;
		_format = null;
		_constantRange = SliderRange.NONE;
	}

	/**
	 * Creates a configured {@link NumberInputControlProvider}.
	 */
	@CalledByReflection
	public NumberInputControlProvider(InstantiationContext context, Config config) {
		_display = config.getDisplay();
		_inputs = List.copyOf(config.getInputs());
		_min = InputScript.compile(config, Config.MIN);
		_max = InputScript.compile(config, Config.MAX);
		_step = InputScript.compile(config, Config.STEP);
		_unit = InputScript.compile(config, Config.UNIT);
		FormatBase format = config.getFormat();
		_format = format == null ? null : context.getInstance(format.getDefinition());
		if (_display == NumberDisplay.SLIDER && _inputs.isEmpty()) {
			_constantRange = range();
			checkConstantRange(context, _constantRange);
		} else {
			_constantRange = SliderRange.NONE;
		}
	}

	private static void checkConstantRange(InstantiationContext context, SliderRange range) {
		if (range.min() == null || range.max() == null) {
			context.error(
				"A slider needs the bounds of its range: '" + Config.MIN + "' and '" + Config.MAX + "'.");
		} else if (range.min().doubleValue() >= range.max().doubleValue()) {
			context.error("A slider needs a range to travel: '" + Config.MIN + "' (" + range.min()
				+ ") is not below '" + Config.MAX + "' (" + range.max() + ").");
		} else if (range.effectiveStep() <= 0) {
			context.error("A slider needs a step to move by: '" + Config.STEP + "' (" + range.effectiveStep()
				+ ") is not above zero.");
		}
	}

	/**
	 * How the value is edited.
	 */
	public NumberDisplay getDisplay() {
		return _display;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		Format format = format(field);
		String fieldUnit = field.getUnit();
		if (_display != NumberDisplay.SLIDER) {
			return createInput(context, model, format, fieldUnit);
		}
		if (_inputs.isEmpty()) {
			return createSlider(context, model, format, _constantRange, fieldUnit);
		}
		List<ViewChannel> channels = channels(context, "slider whose range");
		ReactSliderControl slider = createSlider(context, model, format, range(values(channels)), fieldUnit);
		follow(slider, channels, () -> slider.setRange(range(values(channels))));
		return slider;
	}

	/**
	 * Creates a slider writing the {@link Config#getUnit() unit} of this provider where it states
	 * one, the unit of the field otherwise.
	 */
	private ReactSliderControl createSlider(ReactContext context, FieldModel model, Format format,
			SliderRange range, String fieldUnit) {
		if (_unit != null) {
			return new ReactSliderControl(context, model, format, range);
		}
		return new ReactSliderControl(context, model, format, range, fieldUnit);
	}

	/**
	 * Creates the input taking the value as typed text, with the {@link Config#getUnit() unit} of
	 * this provider where it states one, the unit of the field otherwise.
	 */
	private ReactControl createInput(ReactContext context, FieldModel model, Format format, String fieldUnit) {
		ReactNumberInputControl input = new ReactNumberInputControl(context, model, format, fieldUnit);
		if (_unit == null) {
			return input;
		}
		if (_inputs.isEmpty()) {
			input.setUnit(text(_unit, new Object[0]));
			return input;
		}
		List<ViewChannel> channels = channels(context, "number input whose unit");
		input.setUnit(text(_unit, values(channels)));
		follow(input, channels, () -> input.setUnit(text(_unit, values(channels))));
		return input;
	}

	/**
	 * The format the value is written in: the {@link Config#getFormat() format} of this provider in
	 * the user's locale and time zone, or the format of the field where this provider states none.
	 *
	 * <p>
	 * A number read in the format of this provider is the kind of number the field holds: a
	 * fractional number for a field of fractional numbers, also where the text has no fraction, and
	 * a whole number for a field of whole numbers, which refuses a text with a fraction.
	 * </p>
	 */
	private Format format(FieldSpec field) {
		if (_format == null) {
			return FieldControlRegistry.numberFormat(field);
		}
		Format format = _format.newFormat(FormatterService.getInstance().getConfig(), ThreadContext.getTimeZone(),
			ThreadContext.getLocale());
		if (format instanceof NumberFormat number) {
			Class<?> valueType = PrimitiveTypeUtil.asNonPrimitive(field.getValueType());
			if (valueType == Double.class || valueType == Float.class) {
				return NumberFormatResult.DOUBLE.adapt(number);
			}
			if (valueType == Long.class) {
				return NumberFormatResult.LONG.adapt(number);
			}
		}
		return format;
	}

	/**
	 * The channels of the {@link Config#getInputs() inputs} in the view the control is created in.
	 *
	 * @param what
	 *        What reads the channels, for the message refusing a context that is no view.
	 */
	private List<ViewChannel> channels(ReactContext context, String what) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException("A " + what + " reads the channels of its '" + Config.INPUTS
				+ "' must be created in a view, but is created in: " + context);
		}
		return ChannelInputs.resolve(viewContext, _inputs);
	}

	/**
	 * Updates the given control whenever one of the given channels changes, as long as the control
	 * is displayed.
	 */
	private static void follow(ReactControl control, List<ViewChannel> channels, Runnable update) {
		ChannelListener listener = (sender, oldValue, newValue) -> update.run();
		for (ViewChannel channel : channels) {
			Registration registration = channel.addListener(listener);
			control.addCleanupAction(registration::dispose);
		}
	}

	private static Object[] values(List<ViewChannel> channels) {
		Object[] result = ChannelInputs.arguments(channels);
		for (int n = 0, cnt = result.length; n < cnt; n++) {
			result[n] = SearchExpression.normalizeValue(result[n]);
		}
		return result;
	}

	private SliderRange range(Object... inputValues) {
		return new SliderRange(number(_min, inputValues), number(_max, inputValues),
			number(_step, inputValues), text(_unit, inputValues));
	}

	private static Double number(QueryExecutor expr, Object[] inputValues) {
		if (expr == null) {
			return null;
		}
		Object result = expr.execute(inputValues);
		return result instanceof Number number ? Double.valueOf(number.doubleValue()) : null;
	}

	private static String text(QueryExecutor expr, Object[] inputValues) {
		if (expr == null) {
			return null;
		}
		return FieldControlService.unitText(expr.execute(inputValues));
	}

}
