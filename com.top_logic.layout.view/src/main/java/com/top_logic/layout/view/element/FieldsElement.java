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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
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
	public interface Config extends ContainerElement.Config, FormLayoutOptions {

		@Override
		@ClassDefault(FieldsElement.class)
		Class<? extends UIElement> getImplementationClass();

	}

	private final int _maxColumns;

	private final LabelPosition _labelPosition;

	private final String _cssClass;

	/**
	 * Creates a new {@link FieldsElement} from configuration.
	 */
	@CalledByReflection
	public FieldsElement(InstantiationContext context, Config config) {
		super(context, config);

		_maxColumns = config.getMaxColumns();
		_labelPosition = FormLayoutOptions.layoutPosition(context, config.getLabelPosition());
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> fields = createChildControls(context).stream()
			.map(field -> (ReactControl) field)
			.collect(Collectors.toList());

		// Whether a value can be changed is the field's own business - a value input marks itself
		// read-only - so the grid displays whatever state its fields are in.
		ReactFormLayoutControl result = new ReactFormLayoutControl(context, _maxColumns, _labelPosition, false, fields);
		result.setCssClass(_cssClass);
		return result;
	}
}
