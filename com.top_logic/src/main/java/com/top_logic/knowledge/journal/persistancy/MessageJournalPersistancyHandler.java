/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.persistancy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalResultAttributeEntry;
import com.top_logic.knowledge.journal.MessageJournalAttributeEntry;
import com.top_logic.knowledge.journal.MessageJournalResultAttributeEntry;

/**
 * @author    <a href=mailto:tsa@top-logic.com>Theo Sattttteller</a>
 */
public class MessageJournalPersistancyHandler implements
        JournalPersistancyHandler {

    /**
     * TODO TSA Create a new MessageJournalPersistancyHandler ...
     */
    public MessageJournalPersistancyHandler() {
        super();
    }

	@Override
	public void journal(long aCommitId, TLID anIdentifier,
			Collection<? extends JournalAttributeEntry> entries, PooledConnection connection)
			throws SQLException {

		for (JournalAttributeEntry journalAttributeEntry : (Collection<JournalAttributeEntry>) entries) {
			journal(aCommitId, anIdentifier, journalAttributeEntry, connection);
		}
	}

	private void journal(long aCommitId, TLID anIdentifier,
            JournalAttributeEntry anEntry, PooledConnection aPSC)
            throws SQLException {
        // make a hard cast. If the type does not match this will give you the
        // propper attention.
        MessageJournalAttributeEntry theEntry = (MessageJournalAttributeEntry) anEntry;

		String createJournalAttributeSQLQuery = createCreateJournalAttributeSQLQuery(aPSC.getSQLDialect());
		try (PreparedStatement theStatement = aPSC.prepareStatement(createJournalAttributeSQLQuery)) {
			String theJournalMessage = theEntry.getJournalMessage();
			if (theJournalMessage.length() > 255) {
				Logger.warn("Encountered journal message too long, will be truncated to 255 characters." + " Commitid: " + aCommitId
						+ "; identifier: " + anIdentifier + "; name: " + theEntry.getName() + "; tpye" + theEntry.getType() + "; cause: "
						+ theEntry.getCause() + "; message: " + theJournalMessage, this);
				theJournalMessage = StringServices.minimizeString(theJournalMessage, 255, 252);
			}

			theStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(theStatement, 2, anIdentifier);
			theStatement.setString(3, theEntry.getName());
			theStatement.setString(4, theEntry.getType());
			theStatement.setString(5, theEntry.getCause());
			theStatement.setString(6, theJournalMessage);
			//aPostStatement.addBatch();
			theStatement.execute();
		}
    }

	private String createCreateJournalAttributeSQLQuery(DBHelper sqlDialect) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("INSERT INTO ");
		sb.append(sqlDialect.tableRef("JOURNAL_ATTRIBUTE_MESSAGE"));
		sb.append(" VALUES(?,?,?,?,?,?)");
		
		return sb.toString();
	}

	@Override
	public Collection<JournalResultAttributeEntry> getJournal(long aCommitId, TLID anIdentifier,
            PooledConnection connection, DBHelper dbHelper) throws SQLException {
		List<JournalResultAttributeEntry> theResult = new ArrayList<>();

        //get pre values of commit
		String getAttributesSQLQuery = createGetAttributesSQLQuery(dbHelper);
		try (PreparedStatement theAttStatement = connection.prepareStatement(getAttributesSQLQuery)) {
			// theAttStatement.clearParameters();
			theAttStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(theAttStatement, 2, anIdentifier);
			try (ResultSet theAtts = theAttStatement.executeQuery()) {

			while (theAtts.next()) {
				String theName = theAtts.getString(3);
				String theType = theAtts.getString(4);
				String theCause = theAtts.getString(5);
				String theMessage = theAtts.getString(6);
				MessageJournalResultAttributeEntry theAttEntry = new MessageJournalResultAttributeEntry(theName, theType, theCause, theMessage);
				theResult.add(theAttEntry);
			}
			}
		}
        return theResult;
    }

	private String createGetAttributesSQLQuery(DBHelper dbHelper) {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(dbHelper.tableRef("JOURNAL_ATTRIBUTE_MESSAGE"));
		sb.append(" WHERE ");
		sb.append(dbHelper.columnRef("COMMIT_ID"));
		sb.append("=?");
		sb.append(" AND ");
		sb.append(dbHelper.columnRef("IDENTIFIER"));
		sb.append("=?");
		
		return sb.toString();
	}

}
