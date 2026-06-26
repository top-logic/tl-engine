/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Base type for the typed arguments of a {@link ReactCommand @ReactCommand}.
 *
 * <p>
 * A command handler may declare a subtype of this interface as its argument parameter instead of a
 * raw {@code Map<String, Object>}. The client JSON arguments bind into the typed instance (via the
 * same JSON property names declared by {@link com.top_logic.basic.config.annotation.Name @Name}),
 * and the handler reads typed getters. The one interface is then the single source for the call
 * arguments, the advertised {@link com.top_logic.basic.json.schema.model.Schema JSON schema} (built
 * from the {@link com.top_logic.basic.config.ConfigurationDescriptor descriptor}), and the
 * human-readable rendering of a recorded step.
 * </p>
 *
 * <p>
 * Dispatch still goes through
 * {@link ReactControl#executeCommand(String, java.util.Map)} — the typed arguments only change the
 * shape the handler receives, not the behavior path the browser exercises.
 * </p>
 */
public interface ReactCommandArguments extends ConfigurationItem {

	// Marker base; concrete commands declare their argument properties in subtypes.

}
