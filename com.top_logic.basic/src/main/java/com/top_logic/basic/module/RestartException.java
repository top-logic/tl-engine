/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link RestartException} is thrown if
 * {@link ModuleUtil#restart(BasicRuntimeModule, Runnable) restarting a module}
 * failed. It additionally contains the dependent services which are tried to
 * restart.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RestartException extends ModuleException {

	private final Collection<BasicRuntimeModule<?>> allDependents;

	RestartException(String message, Throwable cause, Class<? extends ManagedClass> service, Collection<BasicRuntimeModule<?>> allDependents) {
		super(message, cause, service);
		this.allDependents = allDependents;
	}

	/**
	 * Returns a Collection of services which were started at the moment the
	 * restart was triggered. It is possible that this are not all started
	 * services, but each started service has some of the service in the
	 * returned collection as dependent (or inherited). 
	 */
	public Collection<BasicRuntimeModule<?>> getCurrentlyStartedDependents(){
		return Collections.unmodifiableCollection(allDependents);
	}
	
}

