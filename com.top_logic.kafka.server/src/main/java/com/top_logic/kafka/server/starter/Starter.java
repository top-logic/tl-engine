/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.starter;

/**
 * Interface for services that may be started and stopped.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Starter {

	/**
	 * Starts the service
	 */
	void startup();

	/**
	 * Stops the service.
	 */
	void shutdown();

}

