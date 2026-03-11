/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.control.ColorChooserSelectionControl;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * React color input control with palette management.
 *
 * <p>
 * Renders the {@code TLColorInput} React component. The color value flows through
 * {@link FieldModel} (as {@link java.awt.Color}). Palette management (personal palette, drag-drop
 * reordering, reset) is handled by this control.
 * </p>
 */
public class ReactColorInputControl extends ReactFormFieldControl {

	private static final String PALETTE = "palette";

	private static final String DEFAULT_PALETTE = "defaultPalette";

	private static final String PALETTE_COLUMNS = "paletteColumns";

	private static final String COLOR_PALETTE_KEY = "colorPalette";

	/**
	 * Creates a new {@link ReactColorInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the color value (as {@link java.awt.Color}).
	 */
	public ReactColorInputControl(ReactContext context, FieldModel model) {
		super(context, model, "TLColorInput");

		// Override initial value with hex string representation.
		putState(VALUE, colorToHex(model.getValue()));

		initPalette();
	}

	private void initPalette() {
		List<String> defaultPalette = loadDefaultPalette();
		List<String> personalPalette = loadPersonalPalette();
		List<String> palette = personalPalette != null ? personalPalette : defaultPalette;

		ColorChooserSelectionControl.Config config = getColorConfig();
		putState(PALETTE, palette);
		putState(DEFAULT_PALETTE, defaultPalette);
		putState(PALETTE_COLUMNS, Integer.valueOf(config.getColumns()));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		// Convert Color to hex string for the React component.
		putState(VALUE, colorToHex(newValue));
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue == null) {
			return null;
		}
		return hexToColor(rawValue.toString());
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

	/**
	 * Converts a value to a hex string for the React component.
	 */
	private static String colorToHex(Object value) {
		if (value instanceof Color) {
			return ColorChooserControl.toHtmlColor((Color) value);
		}
		if (value instanceof String) {
			return (String) value;
		}
		return null;
	}

	/**
	 * Converts a hex string to a {@link Color}.
	 */
	static Color hexToColor(String hex) {
		if (hex == null || hex.isEmpty()) {
			return null;
		}
		try {
			return Color.decode(hex);
		} catch (NumberFormatException ex) {
			return null;
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
}
