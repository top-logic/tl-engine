/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * A recorded {@link ReactButtonControl} click.
 *
 * <p>
 * The click carries no arguments; the button is identified by its address alone, so the step
 * renders by the button's {@link ReactCommand#getTarget() label-derived target name}.
 * </p>
 */
@Label("Press button '{target}'")
public interface ClickCommand extends ReactCommand {

	// A click has no arguments; the target address is the whole invocation.

}
