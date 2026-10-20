/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.container.ConfigPart;

/**
 * The {@link Inputs} of a command or an action, which is part of a configuration tree.
 *
 * <p>
 * Being a {@link ConfigPart}, the configuration knows where it is placed, so that the arguments of
 * the action chain it belongs to can be determined for it - for instance by the script editor,
 * which offers those arguments next to the inputs.
 * </p>
 */
public interface NamedInputs extends Inputs, ConfigPart {

	// Pure combination of the inputs property and the container reference.

}
