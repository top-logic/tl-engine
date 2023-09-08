/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

/**
 * Dynamic dependency to a {@link ManagedClass}.
 * 
 * <p>
 * If a service optionally uses another service if it is available, it declares itself as
 * {@link ServiceDependency} of the other service and
 * {@link BasicRuntimeModule#addServiceDependency(ServiceDependency) registers} /
 * {@link BasicRuntimeModule#removeServiceDependency(ServiceDependency) deregisters} itself during
 * startup/shutdown.
 * </p>
 * 
 * @see BasicRuntimeModule#addServiceDependency(ServiceDependency)
 * @see BasicRuntimeModule#removeServiceDependency(ServiceDependency)
 */
public interface ServiceDependency<M extends ManagedClass> {

	/**
	 * Callback invoked, when the other service starts, or if it is already active, when the
	 * dependent service registers.
	 */
	void onConnect(M impl);

	/**
	 * Callback invokes, when the other service shuts down, or if it is still active, when the
	 * dependent service unregisters.
	 */
	void onDisconnect(M oldInstance);

}
