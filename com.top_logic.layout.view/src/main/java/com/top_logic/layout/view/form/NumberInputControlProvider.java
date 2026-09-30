/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.MandatoryIf;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.NumberDisplay;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;

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
 */
public class NumberInputControlProvider implements ReactFieldControlProvider {

	/**
	 * The {@link Config#getStep() step} of a slider that states none: the value moves by whole
	 * numbers.
	 */
	private static final double DEFAULT_STEP = 1.0;

	/**
	 * Configuration options for {@link NumberInputControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<NumberInputControlProvider> {

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getMin()}. */
		String MIN = "min";

		/** Configuration name for {@link #getMax()}. */
		String MAX = "max";

		/** Configuration name for {@link #getStep()}. */
		String STEP = "step";

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
		 * The value the handle stands on at the left end of the track. An input has no range to
		 * travel and ignores it.
		 * </p>
		 */
		@Name(MIN)
		@Nullable
		@NullDefault
		@MandatoryIf(other = @Ref(DISPLAY), value = NumberDisplay.SLIDER_NAME)
		@ComparisonDependency(comparison = Comparision.SMALLER, other = @Ref(MAX))
		Double getMin();

		/**
		 * The largest value a slider can set, which it must be given.
		 *
		 * <p>
		 * The value the handle stands on at the right end of the track, above the
		 * {@link #getMin() smallest} one. An input has no range to travel and ignores it.
		 * </p>
		 */
		@Name(MAX)
		@Nullable
		@NullDefault
		@MandatoryIf(other = @Ref(DISPLAY), value = NumberDisplay.SLIDER_NAME)
		Double getMax();

		/**
		 * The distance between two values a slider can set, whole numbers where nothing is stated.
		 *
		 * <p>
		 * The grid the handle snaps to: a step of {@code 0.5} offers every half, a step of
		 * {@code 10} every tenth value of the range. An input has no grid and ignores it.
		 * </p>
		 */
		@Name(STEP)
		@Nullable
		@NullDefault
		@Bound(comparison = Comparision.GREATER, value = 0)
		Double getStep();
	}

	private final NumberDisplay _display;

	private final Double _min;

	private final Double _max;

	private final Double _step;

	/**
	 * Creates a {@link NumberInputControlProvider} taking the value as typed text.
	 */
	public NumberInputControlProvider() {
		_display = NumberDisplay.INPUT;
		_min = null;
		_max = null;
		_step = null;
	}

	/**
	 * Creates a configured {@link NumberInputControlProvider}.
	 */
	@CalledByReflection
	public NumberInputControlProvider(InstantiationContext context, Config config) {
		_display = config.getDisplay();
		_min = config.getMin();
		_max = config.getMax();
		_step = config.getStep();
		if (_display == NumberDisplay.SLIDER && (_min == null || _max == null)) {
			// The constraints on the configuration are what keeps this from happening, see
			// Config#getMin(); a configuration built without them is rejected here.
			throw new IllegalArgumentException(
				"A slider needs the bounds of its range: '" + Config.MIN + "' and '" + Config.MAX + "'.");
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
		if (_display == NumberDisplay.SLIDER) {
			return new ReactSliderControl(context, model, format, _min.doubleValue(), _max.doubleValue(),
				_step == null ? DEFAULT_STEP : _step.doubleValue());
		}
		return new ReactNumberInputControl(context, model, format);
	}

}
