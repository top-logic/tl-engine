/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Comparator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ReferenceDisplay;

/**
 * {@link ReactFieldControlProvider} for attributes that are edited by selecting from a set of
 * options.
 *
 * <p>
 * Wraps a {@link SelectFieldModel} (an {@link AttributeSelectFieldModel}) in a
 * {@link ReactDropdownSelectControl}, using {@link MetaResourceProvider} so that options and the
 * current selection are rendered with the label and the image their model registers, in both edit
 * and display mode.
 * </p>
 *
 * <p>
 * When configured for a special model datatype (e.g. {@code tl.util:Country}) via the
 * {@link FieldControlService} type map, an {@link #getConfiguredOptions() option provider} supplies
 * the selectable values. Without a configured option provider, options are derived from the
 * attribute itself (enumeration, reference, enum datatype, or a TL-Script options annotation).
 * </p>
 *
 * <p>
 * The options are offered in the shape the edited field asks for, which for a model attribute
 * follows its {@link ReferenceDisplay} or {@link ClassificationDisplay} annotation and is a list
 * that opens on demand where the attribute says nothing. A {@link Config#getDisplay() display} and
 * an {@link Config#getOrientation() orientation} configured here override what the field says, so a
 * single form field deviates from how the attribute is displayed elsewhere.
 * </p>
 *
 * @implNote What the field asks for is read from {@link FieldSpec#getSelectDisplay()} and
 *           {@link FieldSpec#getSelectOrientation()}.
 */
public class SelectControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration for {@link SelectControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<SelectControlProvider> {

		/** Configuration name for {@link #getOptionProvider()}. */
		String OPTION_PROVIDER = "option-provider";

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getOrientation()}. */
		String ORIENTATION = "orientation";

		/** Configuration name for {@link #hasFilter()}. */
		String FILTER = "filter";

		@Override
		@ClassDefault(SelectControlProvider.class)
		Class<? extends SelectControlProvider> getImplementationClass();

		/**
		 * The option source for the selectable values.
		 *
		 * <p>
		 * If unset, options are derived from the attribute's type (enumeration, reference, enum
		 * datatype) or its TL-Script options annotation.
		 * </p>
		 */
		@Name(OPTION_PROVIDER)
		@InstanceFormat
		OptionProvider getOptionProvider();

		/**
		 * The shape the options are offered in, or nothing to offer them the way the edited
		 * attribute asks for.
		 *
		 * <p>
		 * A list that opens on demand takes the room of one field whatever the number of options
		 * and is searched by typing, which suits a list of any length. A cloud of toggles and a bar
		 * of segments show every option at all times, so the value and what else could be chosen
		 * are read without opening anything - at the price of the room all options take, which
		 * makes them a choice for a handful of options rather than for a long list. A group of
		 * radio buttons shows every option the way a form marks a choice, with a checkbox for each
		 * option where several values can be chosen.
		 * </p>
		 *
		 * <p>
		 * Stated here, the shape holds for this field alone; stated as the {@link ReferenceDisplay}
		 * or {@link ClassificationDisplay} annotation of an attribute, it holds wherever that
		 * attribute is shown. Where neither states a shape, the options are offered in a list that
		 * opens on demand.
		 * </p>
		 */
		@Name(DISPLAY)
		@Nullable
		@NullDefault
		SelectDisplay getDisplay();

		/**
		 * The direction a group of radio buttons lays its options out in, or nothing to lay them
		 * out the way the edited attribute asks for.
		 *
		 * <p>
		 * One below the other, the options are scanned like a list; side by side, a few short
		 * options take a single line. Where neither this setting nor the annotation of the
		 * attribute states a direction, the options are laid out one below the other. The other
		 * shapes of the {@link #getDisplay() display} have no direction to choose and ignore it.
		 * </p>
		 */
		@Name(ORIENTATION)
		@Nullable
		@NullDefault
		Orientation getOrientation();

		/**
		 * Whether the list that opens on demand offers an input to filter its options by.
		 *
		 * <p>
		 * Without the input, the open list is operated by the keyboard alone: the arrow keys move
		 * through the options, and typing the beginning of a label jumps to the option it starts.
		 * That suits a short list whose labels the user knows. The shapes of the
		 * {@link #getDisplay() display} that show every option have nothing to filter and ignore
		 * it.
		 * </p>
		 */
		@Name(FILTER)
		@BooleanDefault(true)
		boolean hasFilter();
	}

	private final OptionProvider _optionProvider;

	private final SelectDisplay _display;

	private final Orientation _orientation;

	private final boolean _filter;

	/**
	 * Creates a {@link SelectControlProvider} without a configured option source (options are
	 * derived from the attribute), offering them in the shape each field asks for.
	 */
	public SelectControlProvider() {
		_optionProvider = null;
		_display = null;
		_orientation = null;
		_filter = true;
	}

	/**
	 * Creates a configured {@link SelectControlProvider}.
	 */
	@CalledByReflection
	public SelectControlProvider(InstantiationContext context, Config config) {
		_optionProvider = config.getOptionProvider();
		_display = config.getDisplay();
		_orientation = config.getOrientation();
		_filter = config.hasFilter();
	}

	/**
	 * The configured option source, or {@code null} to derive options from the attribute.
	 */
	public OptionProvider getConfiguredOptions() {
		return _optionProvider;
	}

	/**
	 * The shape the options are offered in, or {@code null} to follow what each field says.
	 */
	public SelectDisplay getDisplay() {
		return _display;
	}

	/**
	 * The direction a group of radio buttons lays its options out in, or {@code null} to follow
	 * what each field says.
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * Whether the list that opens on demand offers an input to filter its options by.
	 */
	public boolean hasFilter() {
		return _filter;
	}

	/**
	 * A selection is made on one control, however many options it accepts.
	 *
	 * <p>
	 * The selected values are the value of the {@link SelectFieldModel} the control is bound to, so
	 * a multi-valued field is picked from in one dropdown rather than through one dropdown per
	 * value.
	 * </p>
	 */
	@Override
	public boolean editsCollections() {
		return true;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		SelectFieldModel selectModel = (SelectFieldModel) model;
		// A resource provider rather than a label provider: an option is presented by its label and
		// its image, and the image is what a plain label provider cannot answer - the control drops
		// it for want of one. The two registries of LabelProviderService fall back to each other,
		// so a type registered only for its label is labelled exactly as before.
		LabelProvider labels = MetaResourceProvider.INSTANCE;
		Comparator<?> optionOrder = LabelComparator.newCachingInstance(labels);
		// An ordered attribute keeps the order the user gives its selection; an unordered one is
		// shown in the order of the options.
		SelectDisplay display = _display == null ? field.getSelectDisplay() : _display;
		Orientation orientation = _orientation == null ? field.getSelectOrientation() : _orientation;
		ReactDropdownSelectControl control = new ReactDropdownSelectControl(context, selectModel, labels,
			optionOrder, field.isOrdered(), display, orientation);
		if (!_filter) {
			control.setFilter(false);
		}
		return control;
	}

}
