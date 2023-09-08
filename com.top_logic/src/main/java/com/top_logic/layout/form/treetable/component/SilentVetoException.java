/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;

/**
 * {@link VetoException} that provides no custom feedback at the UI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SilentVetoException extends VetoException {

	@Override
	public void process(WindowScope window) {
		// Noting to do.
	}

}