/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.basic.check.CheckScoped;
import com.top_logic.layout.form.control.ButtonControl;

/**
 * The class {@link CommandModel} is the model for being displayed by a {@link ButtonControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandModel extends Command, CheckScoped, ButtonUIModel {

	// Pure sum interface.

}
