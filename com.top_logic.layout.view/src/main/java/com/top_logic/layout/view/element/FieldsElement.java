/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} laying its content out as the fields of a form.
 *
 * <p>
 * A form surrounds its fields with a grid: it insets them from the container border, distributes
 * them over as many columns as the available width carries, and places each label beside its input
 * or above it depending on how wide the column it landed in is. This element is that grid on its
 * own, for inputs that belong to the view rather than to an object and therefore stand outside a
 * form: a {@link ValueInputElement} and everything else that displays the label-and-input chrome of
 * a field.
 * </p>
 *
 * <p>
 * A {@link FormElement} needs none of this, being such a grid already. A {@link FieldElement}, in
 * turn, still needs a form: this element lays fields out but carries no object for them to display.
 * </p>
 */
@InApp
public class FieldsElement extends ContainerElement {

	/**
	 * Configuration for {@link FieldsElement}.
	 */
	@TagName("fields")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getMaxColumns()}. */
		String MAX_COLUMNS = "max-columns";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		@Override
		@ClassDefault(FieldsElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The greatest number of columns the fields are laid out in.
		 *
		 * <p>
		 * How many of them are actually filled follows the available width: a narrow display shows
		 * fewer columns, and on a phone a single one.
		 * </p>
		 */
		@Name(MAX_COLUMNS)
		@IntDefault(3)
		int getMaxColumns();

		/**
		 * Where the fields render their labels relative to their inputs.
		 *
		 * <p>
		 * With {@link LabelPosition#AUTO}, each label stands beside its input where the column is
		 * wide enough for both and moves above it where it is not. {@link LabelPosition#SIDE} and
		 * {@link LabelPosition#TOP} fix the position whatever the width.
		 * </p>
		 *
		 * <p>
		 * A field stating a position of its own keeps it.
		 * </p>
		 */
		@Name(LABEL_POSITION)
		@ComplexDefault(LabelPosition.AutoDefault.class)
		LabelPosition getLabelPosition();
	}

	private final int _maxColumns;

	private final LabelPosition _labelPosition;

	/**
	 * Creates a new {@link FieldsElement} from configuration.
	 */
	@CalledByReflection
	public FieldsElement(InstantiationContext context, Config config) {
		super(context, config);

		_maxColumns = config.getMaxColumns();
		_labelPosition = layoutPosition(context, config.getLabelPosition());
	}

	/**
	 * The given {@link LabelPosition} if a grid of fields can take it, and the responsive
	 * {@link LabelPosition#AUTO} with an error reported if it is one only a single field can take.
	 */
	private static LabelPosition layoutPosition(InstantiationContext context, LabelPosition position) {
		switch (position) {
			case SIDE:
			case TOP:
			case AUTO:
				return position;
			default:
				context.error("The label position '" + position.getExternalName()
					+ "' is one a single field takes, not one a grid of fields lays its fields out in. Allowed are '"
					+ LabelPosition.SIDE.getExternalName() + "', '" + LabelPosition.TOP.getExternalName() + "' and '"
					+ LabelPosition.AUTO.getExternalName() + "'.");
				return LabelPosition.AUTO;
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> fields = createChildControls(context).stream()
			.map(field -> (ReactControl) field)
			.collect(Collectors.toList());

		// Whether a value can be changed is the field's own business - a value input marks itself
		// read-only - so the grid displays whatever state its fields are in.
		return new ReactFormLayoutControl(context, _maxColumns, _labelPosition, false, fields);
	}
}
