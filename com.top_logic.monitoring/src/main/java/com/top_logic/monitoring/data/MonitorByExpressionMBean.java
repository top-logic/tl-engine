/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

/**
 * Interface for a MBean with a configured TL-script expression and a method for external scripts.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface MonitorByExpressionMBean {
	/** The result of the configured script. */
	Object getResult();

	/** Executes an external script. */
	void compileExternalQuery(String script);

	/** The result of the external script. */
	Object getExternalResult();
}