/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.view.form.AttributeFieldModel;

/**
 * Text input of a {@link Temperature}, entered in the unit of the object it belongs to.
 *
 * <p>
 * The field shows the {@link Temperature#format() display text} of its value, e.g.
 * <code>68.0 °F</code>. A typed text is read by {@link Temperature#parse(String, TemperatureUnit)}:
 * a number without unit is taken in the unit the value is displayed in, a number with a unit symbol
 * in that unit. The result is displayed in the unit of the field in any case.
 * </p>
 *
 * <p>
 * The unit of the field is the unit of its current value. A field without a value asks the
 * {@link TemperatureStorage} of the attribute it edits for the unit of the edited object. An
 * object that does not know its unit yet, a sensor that belongs to no site, has the unit
 * {@link TemperatureUnit#CELSIUS}: a number typed without unit is taken in degrees Celsius there,
 * and a temperature in another unit must be typed with its symbol. The same holds for a field that
 * edits no attribute at all.
 * </p>
 *
 * <p>
 * A text that is no temperature is rejected with {@link I18NConstants#ERROR_INVALID_TEMPERATURE__VALUE},
 * a temperature below absolute zero with {@link I18NConstants#ERROR_BELOW_ABSOLUTE_ZERO__VALUE}.
 * The rejected text stays in the field, while the field keeps its last accepted value.
 * </p>
 */
public class TemperatureInputControl extends ReactTextInputControl {

	/**
	 * Creates a {@link TemperatureInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding a {@link Temperature}.
	 */
	public TemperatureInputControl(ReactContext context, FieldModel model) {
		super(context, model);
	}

	@Override
	protected void applyRawClientValue(Object rawValue) {
		String text = rawValue == null ? "" : rawValue.toString().trim();
		if (text.isEmpty()) {
			setInputError(null);
			applyClientValue(null);
			return;
		}

		Temperature temperature = Temperature.parse(text, unit());
		if (temperature == null) {
			setInputError(I18NConstants.ERROR_INVALID_TEMPERATURE__VALUE.fill(text));
			return;
		}
		if (temperature.getCelsius() < Temperature.ABSOLUTE_ZERO) {
			setInputError(I18NConstants.ERROR_BELOW_ABSOLUTE_ZERO__VALUE.fill(text));
			return;
		}
		setInputError(null);
		applyClientValue(temperature);
	}

	/**
	 * The unit in which a number typed without unit is taken and the result is displayed.
	 */
	private TemperatureUnit unit() {
		FieldModel model = getFieldModel();
		if (model.getValue() instanceof Temperature current) {
			return current.getUnit();
		}
		if (model instanceof AttributeFieldModel attributeModel
			&& AttributeOperations.getStorageImplementation(attributeModel.getPart()) instanceof TemperatureStorage<?> storage) {
			return storage.getUnit(attributeModel.getObject());
		}
		return TemperatureUnit.CELSIUS;
	}

	/**
	 * Reports the given problem with the typed text on the field model, so that the field shows
	 * it.
	 */
	private void setInputError(ResKey error) {
		if (getFieldModel() instanceof AbstractFieldModel abstractModel) {
			abstractModel.setError(error);
		}
	}

}
