/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.StringWriter;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;

/**
 * Derivations from a {@link ReactCommand} item: its dispatch arguments and its human-readable
 * description.
 *
 * <p>
 * The typed item is the single stored representation of a recorded step; both the wire/dispatch form
 * and the description are pure functions of it, computed on demand.
 * </p>
 */
public final class ReactCommands {

	private ReactCommands() {
		// Static utility.
	}

	/**
	 * The command's argument map — the form the {@code React} client sends and
	 * {@link ReactControl#executeCommand(String, Map)} dispatches: the item's properties as client
	 * JSON values (item-valued properties as their nested config-JSON), without the
	 * {@link ReactCommand#getAddress() address} and {@link ReactCommand#getName() name} envelope.
	 *
	 * @param command
	 *        The command item.
	 * @return The argument map (never {@code null}).
	 */
	public static Map<String, Object> arguments(ReactCommand command) {
		Map<String, Object> result = toJsonMap(command);
		result.remove(ReactCommand.ADDRESS);
		result.remove(ReactCommand.NAME);
		result.remove(ReactCommand.TARGET);
		return result;
	}

	/**
	 * A human-readable, localized description of the command: the item rendered through
	 * {@link ConfigLabelProvider}, whose template (the argument interface's label) interleaves
	 * descriptive text with the property values — so a recorded step reads as e.g. <em>Navigate to
	 * 'input-controls'</em> rather than the raw command and JSON. Item-valued properties (e.g. a row's
	 * {@link com.top_logic.layout.scripting.recorder.ref.ModelName}) render through their own labels.
	 *
	 * @param command
	 *        The command item.
	 * @return The description.
	 */
	public static String describe(ReactCommand command) {
		return new ConfigLabelProvider().getLabel(command);
	}

	/**
	 * The item's properties as a JSON value map (via {@link JsonConfigurationWriter} with the item's
	 * own descriptor, so no polymorphic type tag is emitted for the item itself).
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> toJsonMap(ReactCommand command) {
		try {
			StringWriter buffer = new StringWriter();
			try (JsonWriter json = new JsonWriter(buffer)) {
				new JsonConfigurationWriter(json).write(command.descriptor(), command);
			}
			return (Map<String, Object>) JSON.fromString(buffer.toString());
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				"Cannot serialize command '" + command.getName() + "' on '" + command.getAddress() + "'.", ex);
		}
	}
}
