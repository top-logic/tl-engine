/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.control.ColorChooserSelectionControl;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;

/**
 * Lean color input control that renders via the {@code TLColorInput} React component.
 *
 * <p>
 * Works with plain hex {@link String} values (e.g. {@code "#FF0000"}) instead of
 * {@code FormField} objects. Manages the user's personal color palette for persistence.
 * </p>
 */
public class ViewColorInputControl extends ReactControl {

	private static final String VALUE = "value";

	private static final String EDITABLE = "editable";

	private static final String PALETTE = "palette";

	private static final String PALETTE_COLUMNS = "paletteColumns";

	private static final String DEFAULT_PALETTE = "defaultPalette";

	private static final String CAN_RESET = "canReset";

	private static final String COLOR_PALETTE_KEY = "colorPalette";

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewColorInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param value
	 *        The initial hex color value (e.g. "#FF0000"), may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewColorInputControl(ReactContext context, String value, boolean editable) {
		super(context, null, "TLColorInput");
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));

		List<String> defaultPalette = loadDefaultPalette();
		List<String> personalPalette = loadPersonalPalette();
		List<String> palette = personalPalette != null ? personalPalette : defaultPalette;

		ColorChooserSelectionControl.Config config = getColorConfig();
		putState(PALETTE, palette);
		putState(DEFAULT_PALETTE, defaultPalette);
		putState(PALETTE_COLUMNS, Integer.valueOf(config.getColumns()));
	}

	/**
	 * Sets the hex color value.
	 *
	 * @param value
	 *        The new hex color string, may be {@code null}.
	 */
	public void setValue(String value) {
		putState(VALUE, value);
	}

	/**
	 * Sets whether this field is editable.
	 *
	 * @param editable
	 *        {@code true} for editable, {@code false} for read-only.
	 */
	public void setEditable(boolean editable) {
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets whether the user can clear the color (reset to {@code null}).
	 *
	 * @param canReset
	 *        {@code true} to show a "Clear" button, {@code false} to hide it.
	 */
	public void setCanReset(boolean canReset) {
		putState(CAN_RESET, Boolean.valueOf(canReset));
	}

	/**
	 * Sets the callback that is invoked when the value changes from the client.
	 *
	 * @param callback
	 *        The callback, or {@code null} to remove.
	 */
	public void setValueCallback(ValueCallback callback) {
		_valueCallback = callback;
	}

	/**
	 * Handles value change events from the client.
	 */
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		String newValue = rawValue != null ? rawValue.toString() : null;
		putState(VALUE, newValue);
		if (_valueCallback != null) {
			_valueCallback.valueChanged(hexToColor(newValue));
		}
	}

	/**
	 * Handles palette change events from the client (drag-drop swap, reset).
	 */
	@ReactCommand("paletteChanged")
	void handlePaletteChanged(Map<String, Object> arguments) {
		@SuppressWarnings("unchecked")
		List<String> newPalette = (List<String>) arguments.get(PALETTE);
		if (newPalette != null) {
			putState(PALETTE, newPalette);
			savePersonalPalette(newPalette);
		}
	}

	private static ColorChooserSelectionControl.Config getColorConfig() {
		ColorChooserSelectionControl.Config config =
			ApplicationConfig.getInstance().getConfig(ColorChooserSelectionControl.Config.class);
		if (config == null) {
			config = TypedConfiguration.newConfigItem(ColorChooserSelectionControl.Config.class);
		}
		return config;
	}

	private static List<String> loadDefaultPalette() {
		ColorChooserSelectionControl.Config config = getColorConfig();
		int rows = config.getRows();
		int columns = config.getColumns();
		List<String> result = new ArrayList<>(rows * columns);
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				String cellName = Character.toString((char) ('A' + column)) + row;
				ColorChooserSelectionControl.Config.ColorItem item = config.getColors().get(cellName);
				result.add(item != null ? item.getHexValue() : null);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<String> loadPersonalPalette() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		return (List<String>) pc.getJSONValue(COLOR_PALETTE_KEY);
	}

	private static void savePersonalPalette(List<String> palette) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setJSONValue(COLOR_PALETTE_KEY, palette);
	}

	/**
	 * Converts a {@link Color} to a hex string for use as control value.
	 *
	 * @param color
	 *        The color, may be {@code null}.
	 * @return Hex string like "#FF0000", or {@code null}.
	 */
	public static String colorToHex(Color color) {
		return ColorChooserControl.toHtmlColor(color);
	}

	/**
	 * Converts a hex string to a {@link Color}.
	 *
	 * @param hex
	 *        The hex string like "#FF0000", may be {@code null}.
	 * @return The color, or {@code null}.
	 */
	public static Color hexToColor(String hex) {
		if (hex == null || hex.isEmpty()) {
			return null;
		}
		try {
			return Color.decode(hex);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
