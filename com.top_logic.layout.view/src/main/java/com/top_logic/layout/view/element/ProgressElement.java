/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactProgressControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link UIElement} displaying a fraction as a bar with an optional label, via the
 * {@link ReactProgressControl}.
 *
 * <p>
 * The fraction is stated either directly, as a number between 0 and 1, or as the two counts it is
 * the ratio of - a "3 of 7 done", which is also the label such a bar carries unless another one is
 * given. The two forms exclude each other.
 * </p>
 *
 * <p>
 * A fraction that answers nothing at all is the indeterminate bar: it says that something is going
 * on without saying how far it has come, and the client sweeps it instead of filling a share of it.
 * A bar following an operation that learns its share only later therefore switches between the two
 * displays as the operation reports.
 * </p>
 *
 * <p>
 * Every expression is called with the current value of the {@link Config#getInput() input} channel,
 * and the bar is recomputed whenever that value changes, whenever the object on it changes, and
 * whenever an object of an {@link Config#getObservedTypes() observed type} is created, changed or
 * deleted - a bar counting the members of a collection follows a member joining or leaving it only
 * through the latter.
 * </p>
 */
@InApp
public class ProgressElement implements UIElement {

	/**
	 * Configuration for {@link ProgressElement}.
	 */
	@TagName("progress")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getFraction()}. */
		String FRACTION = "fraction";

		/** Configuration name for {@link #getDone()}. */
		String DONE = "done";

		/** Configuration name for {@link #getTotal()}. */
		String TOTAL = "total";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		@Override
		@ClassDefault(ProgressElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose value every expression of this element is called with, and whose change
		 * recomputes the bar.
		 *
		 * <p>
		 * A bar computed from the model as a whole needs none; its expressions are then called with
		 * no value.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * TL-Script expression answering the filled part of the bar, a number between 0 and 1.
		 *
		 * <p>
		 * Excludes the two counts, which state the same thing as a ratio. An expression that
		 * answers nothing at all - a bar over an operation that does not know how far it has come -
		 * leaves the bar without a share, which the client sweeps rather than fills.
		 * </p>
		 */
		@Name(FRACTION)
		@Nullable
		Expr getFraction();

		/**
		 * TL-Script expression answering how much is done, the numerator of the fraction.
		 *
		 * <p>
		 * Given together with the total, and never together with the fraction.
		 * </p>
		 */
		@Name(DONE)
		@Nullable
		Expr getDone();

		/**
		 * TL-Script expression answering how much there is in all, the denominator of the fraction.
		 *
		 * <p>
		 * Given together with what is done, and never together with the fraction. A total of zero
		 * leaves the bar empty.
		 * </p>
		 */
		@Name(TOTAL)
		@Nullable
		Expr getTotal();

		/**
		 * TL-Script expression answering the text displayed beside the bar.
		 *
		 * <p>
		 * Without one, a bar stated as two counts is labelled with them ("3 / 7") and a bar stated
		 * as a fraction carries no label.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		Expr getLabel();

		/**
		 * Types whose object changes (create / update / delete) recompute the bar, in addition to
		 * the object on the {@link #getInput() input} channel, which is always observed.
		 *
		 * <p>
		 * Configure this for a bar whose counts reach beyond the input object - one counting all
		 * objects of a type, or the members of a collection the input merely contains.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	/**
	 * What a {@link ProgressElement} displays.
	 *
	 * @param fraction
	 *        The filled part of the bar, between 0 and 1, or {@code null} for a bar without a
	 *        share, which the client sweeps rather than fills.
	 * @param label
	 *        The text beside the bar, or {@code null} for a bar without one.
	 */
	public record Progress(Double fraction, String label) {
		// Pure value type.
	}

	private final ChannelRef _inputRef;

	private final QueryExecutor _fraction;

	private final QueryExecutor _done;

	private final QueryExecutor _total;

	private final QueryExecutor _label;

	private final List<TLModelPartRef> _observedTypeRefs;

	/**
	 * Creates a new {@link ProgressElement} from configuration.
	 */
	@CalledByReflection
	public ProgressElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_fraction = QueryExecutor.compileOptional(config.getFraction());
		_done = QueryExecutor.compileOptional(config.getDone());
		_total = QueryExecutor.compileOptional(config.getTotal());
		_label = QueryExecutor.compileOptional(config.getLabel());
		_observedTypeRefs = config.getObservedTypes();

		if (_fraction != null) {
			if (_done != null || _total != null) {
				context.error("A <progress> states its fraction either directly ('" + Config.FRACTION
					+ "') or as the two counts it is the ratio of ('" + Config.DONE + "' and '" + Config.TOTAL
					+ "'), not both.");
			}
		} else if (_done == null || _total == null) {
			context.error("A <progress> requires either '" + Config.FRACTION + "' or both '" + Config.DONE
				+ "' and '" + Config.TOTAL + "'.");
		}
	}

	/**
	 * Creates a {@link ProgressElement}.
	 *
	 * @param input
	 *        The channel every expression is called with, {@code null} for a bar computed from the
	 *        model as a whole.
	 * @param fraction
	 *        The expression answering the filled part of the bar, {@code null} for a bar stated as
	 *        two counts.
	 * @param done
	 *        The expression answering how much is done, {@code null} for a bar stated as a
	 *        fraction.
	 * @param total
	 *        The expression answering how much there is in all, {@code null} for a bar stated as a
	 *        fraction.
	 * @param label
	 *        The expression answering the text beside the bar, {@code null} to leave the label to
	 *        what the bar is stated as.
	 * @param observedTypes
	 *        The types whose object changes recompute the bar.
	 */
	public ProgressElement(ChannelRef input, QueryExecutor fraction, QueryExecutor done, QueryExecutor total,
			QueryExecutor label, List<TLModelPartRef> observedTypes) {
		_inputRef = input;
		_fraction = fraction;
		_done = done;
		_total = total;
		_label = label;
		_observedTypeRefs = observedTypes;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = _inputRef == null ? null : context.resolveChannel(_inputRef);
		Progress initial = progressOf(input == null ? null : input.get());
		ReactProgressControl control =
			new ReactProgressControl(context, initial.fraction(), initial.label());

		Runnable update = () -> {
			Progress progress = progressOf(input == null ? null : input.get());
			control.setProgress(progress.fraction(), progress.label());
		};

		if (input != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> update.run();
			input.addListener(listener);
			control.addCleanupAction(() -> input.removeListener(listener));
		}

		ChannelObjectObserver observer = new ChannelObjectObserver(
			input == null ? List.of() : List.of(input), ObservedTypes.resolve(_observedTypeRefs), update);
		control.addAttachListener(() -> observer.attach(context.getModelScope()));
		control.addDetachListener(observer::detach);

		return control;
	}

	/**
	 * The progress displayed for the given input value.
	 *
	 * @param input
	 *        The value of the input channel, {@code null} for an element without one.
	 * @return The fraction and the label of the bar.
	 */
	public Progress progressOf(Object input) {
		String label = _label == null ? null : ValueLabel.label(_label.execute(input));
		if (_fraction != null) {
			return new Progress(fraction(_fraction.execute(input)), label);
		}
		return counted(number(_done.execute(input)), number(_total.execute(input)), label);
	}

	/**
	 * The progress the given two counts state.
	 *
	 * @param done
	 *        How much is done, the numerator of the fraction.
	 * @param total
	 *        How much there is in all; nothing in all leaves the bar empty rather than dividing by
	 *        it.
	 * @param label
	 *        The text beside the bar, or {@code null} to label it with the two counts, which is
	 *        what says what they count.
	 * @return The fraction and the label of the bar.
	 */
	public static Progress counted(double done, double total, String label) {
		double fraction = total > 0 ? done / total : 0d;
		return new Progress(Double.valueOf(fraction),
			label != null ? label : number(done) + " / " + number(total));
	}

	/**
	 * The share of the bar the given expression result states.
	 *
	 * @param value
	 *        What the fraction expression answered.
	 * @return The filled part of the bar, or {@code null} for anything that is no number - a bar
	 *         whose share is unknown, which the client sweeps rather than fills.
	 */
	public static Double fraction(Object value) {
		return value instanceof Number number ? Double.valueOf(number.doubleValue()) : null;
	}

	/**
	 * The given expression result as a number, zero for anything that is none.
	 */
	private static double number(Object value) {
		return value instanceof Number number ? number.doubleValue() : 0d;
	}

	/**
	 * A count as it appears in a label: a whole number without a fractional part.
	 */
	private static String number(double count) {
		return count == Math.rint(count) && !Double.isInfinite(count)
			? Long.toString((long) count) : Double.toString(count);
	}

}
