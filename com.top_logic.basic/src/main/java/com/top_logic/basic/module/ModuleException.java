/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

/**
 * The class {@link ModuleException} is the superclass of all exception thrown
 * when the startup of some service failed for some reasons.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleException extends Exception {

	private final Class<? extends ManagedClass> service;

	public ModuleException(String message, Class<? extends ManagedClass> service) {
		super(message);
		this.service = service;
	}

	public ModuleException(String message, Throwable cause, Class<? extends ManagedClass> service) {
		super(message, cause);
		this.service = service;
	}

	/**
	 * Returns the service which produces the failure.
	 */
	public final Class<? extends ManagedClass> getFailedService() {
		return service;
	}
}
