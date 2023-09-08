/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.persistancy;

import java.sql.SQLException;
import java.util.Collection;

import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalResultAttributeEntry;

/**
 * Provide a common interface to store journal attribute entries.
 * 
 * The handler is resposible to store and get the data for a 
 * certain type of attribute entry.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public interface JournalPersistancyHandler {

	/**
	 * Store the data for the given entry into the appropriate journal.
	 * 
	 * @param attributeEntries
	 *        the entries to store
	 * @param connection
	 *        the {@link PooledConnection} used to handle the statements
	 */
    public void journal(long aCommitId, TLID anIdentifier,
			Collection<? extends JournalAttributeEntry> attributeEntries, PooledConnection connection) throws SQLException;

	/**
	 * Reads the {@link JournalResultAttributeEntry entries} for a given object and commit number.
	 * 
	 * @param aCommitId
	 *        The revision number.
	 * @param anIdentifier
	 *        The object whose journal should be read.
	 * @param connection
	 *        Connection to the database.
	 * @param sqlDialect
	 *        The database abstraction layer.
	 * @return The {@link JournalResultAttributeEntry journal entries} for the given object in the
	 *         given revision.
	 */
    public Collection<JournalResultAttributeEntry> getJournal(long aCommitId, TLID anIdentifier,
			PooledConnection connection, DBHelper sqlDialect) throws SQLException;
}
