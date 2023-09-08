/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleRuntimeException extends RuntimeException {

	public ModuleRuntimeException(Throwable ex) {
		super(ex);
	}

	public ModuleRuntimeException(String message, Throwable ex) {
		super(message, ex);
	}

	public ModuleRuntimeException(String message) {
		super(message);
	}
}

