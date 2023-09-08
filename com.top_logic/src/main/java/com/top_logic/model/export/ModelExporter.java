/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLModel;

/**
 * Algorithm that exports model instances to an external database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelExporter {

	/**
	 * Export instances described by the given {@link TLModel} from the given
	 * {@link KnowledgeBase} to the database represented by the given
	 * {@link ConnectionPool}.
	 * 
	 * @param model
	 *        The description of the (part of the) {@link TLModel} to export.
	 * @param kb
	 *        The {@link KnowledgeBase} to take runtime instances from.
	 * @param targetPool
	 *        The external database to export to.
	 * @throws SQLException
	 *         If accessing the external database fails.
	 */
	void exportInstances(TLModel model, KnowledgeBase kb, ConnectionPool targetPool) throws SQLException;

}
