/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

/**
 * Interface for the MBean to monitor data of the database.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface MonitorDatabaseMBean {

	/** Returns the size of the used knowledge-base cache. */
	public int getSizeOfKnowledgebaseCache();

	/** Returns the last revision of the knowledge-base. */
	public String getLastRevision();
}
