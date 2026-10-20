/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.MandatoryIf;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.channel.NamedInputsVariables;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.Resources;

/**
 * {@link ReactFieldControlProvider} for integer and floating-point attributes.
 *
 * <p>
 * The value is displayed in and entered in the {@link FieldSpec#getNumberFormat() format the field
 * asks for} - the attribute's format annotation, where it has one - so a German user reads and types
 * {@code 12,5} where an English user reads and types {@code 12.5}.
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
 * the slider is disabled.
 * </p>
 */
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
		@ScriptContextVariables(NamedInputsVariables.class)
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
		@ScriptContextVariables(NamedInputsVariables.class)
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
		@ScriptContextVariables(NamedInputsVariables.class)
		Expr getStep();

		/**
		 * The unit of the value, written after the value and after the bounds of a slider.
		 *
		 * <p>
		 * A TL-Script expression over the {@link #getInputs() inputs}, like the {@link #getMin()
		 * smallest} value: {@code '%'} for a fixed unit, {@code $target['unit']} for one read from
		 * a channel. An internationalized text is written in the language of the user; an empty
		 * result writes no unit. An input ignores it.
		 * </p>
		 */
		@Name(UNIT)
		@ScriptContextVariables(NamedInputsVariables.class)
		Expr getUnit();
	}

	private final NumberDisplay _display;

	private final List<InputRef> _inputs;

	private final QueryExecutor _min;

	private final QueryExecutor _max;

	private final QueryExecutor _step;

	private final QueryExecutor _unit;

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
		_constantRange = SliderRange.NONE;
	}

	/**
	 * Creates a configured {@link NumberInputControlProvider}.
	 */
	@CalledByReflection
	public NumberInputControlProvider(InstantiationContext context, Config config) {
		_display = config.getDisplay();
		_inputs = List.copyOf(config.getInputs());
		List<String> names = InputRef.names(_inputs);
		_min = QueryExecutor.compileOptional(config.getMin(), names);
		_max = QueryExecutor.compileOptional(config.getMax(), names);
		_step = QueryExecutor.compileOptional(config.getStep(), names);
		_unit = QueryExecutor.compileOptional(config.getUnit(), names);
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
		Format format = FieldControlRegistry.numberFormat(field);
		if (_display != NumberDisplay.SLIDER) {
			return new ReactNumberInputControl(context, model, format);
		}
		if (_inputs.isEmpty()) {
			return new ReactSliderControl(context, model, format, _constantRange);
		}
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException("A slider whose range reads the channels of its '" + Config.INPUTS
				+ "' must be created in a view, but is created in: " + context);
		}
		List<ViewChannel> channels = new ArrayList<>(_inputs.size());
		for (InputRef input : _inputs) {
			channels.add(viewContext.resolveChannel(input.getChannel()));
		}
		ReactSliderControl slider = new ReactSliderControl(context, model, format, range(values(channels)));
		ChannelListener listener = (sender, oldValue, newValue) -> slider.setRange(range(values(channels)));
		for (ViewChannel channel : channels) {
			Registration registration = channel.addListener(listener);
			slider.addCleanupAction(registration::dispose);
		}
		return slider;
	}

	private static Object[] values(List<ViewChannel> channels) {
		Object[] result = new Object[channels.size()];
		for (int n = 0, cnt = result.length; n < cnt; n++) {
			result[n] = SearchExpression.normalizeValue(channels.get(n).get());
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
		Object result = expr.execute(inputValues);
		if (result == null) {
			return null;
		}
		String text = result instanceof ResKey key ? Resources.getInstance().getString(key) : result.toString();
		return text.isEmpty() ? null : text;
	}

}
