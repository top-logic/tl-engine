/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.persistancy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.knowledge.journal.ChangeJournalAttributeEntry;
import com.top_logic.knowledge.journal.ChangeJournalResultAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalResultAttributeEntry;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;

/**
 * Handle entries of type
 * {@link com.top_logic.knowledge.journal.ChangeJournalAttributeEntry}.
 * 
 * TODO TSA This code is partially duplicated in {@link AbstractFlexDataManager}
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public class ChangeEntryJournalPersistancyHandler implements
        JournalPersistancyHandler {
    
    protected static final byte STRING_TYPE   = 's';
    protected static final byte INTEGER_TYPE  = 'i';
    protected static final byte LONG_TYPE     = 'l';
    protected static final byte FLOAT_TYPE    = 'f';
    protected static final byte DOUBLE_TYPE   = 'd';
    protected static final byte BOOLEAN_TRUE  = 'T';
    protected static final byte BOOLEAN_FALSE = 'F';
    protected static final byte DATE_TYPE     = 'D';
    protected static final byte LINK_TYPE     = 'L';
    protected static final byte UNKNOWN_TYPE  = 'U';
    
    /** Used by subclasses only, but for completeness found here */
    protected static final byte NULL_TYPE     = 'N';
    
    /** The default numer of Attributes assumed */
    public static final int DEFAULT_SIZE = 16;

	@Override
	public void journal(long aCommitId, TLID anIdentifier,
			Collection<? extends JournalAttributeEntry> entries, PooledConnection connection)
			throws SQLException {

		for (JournalAttributeEntry journalAttributeEntry : entries) {
			journal(aCommitId, anIdentifier, journalAttributeEntry, connection);
		}
	}

	private void journal(long aCommitId, TLID anIdentifier,
            JournalAttributeEntry anEntry, PooledConnection aPSC) throws SQLException {
        // make a hard cast. If the type does not match this will give you the
        // propper attention.
        ChangeJournalAttributeEntry theEntry = (ChangeJournalAttributeEntry) anEntry;

		DBHelper sqlDialect = aPSC.getSQLDialect();
		String createJournalAttributePreSQLQuery = createCreateJournalAttributePreSQLQuery(sqlDialect);

		try (PreparedStatement thePreStatement = aPSC.prepareStatement(createJournalAttributePreSQLQuery)) {
			// aPreStatement.clearParameters();
			thePreStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(thePreStatement, 2, anIdentifier);
			thePreStatement.setString(3, theEntry.getName());
			store(thePreStatement, 4, theEntry.getPreValue());
			//aPreStatement.addBatch();
			thePreStatement.execute();
		}
		String createJournalAttributePostSQLQuery = createCreateJournalAttributePostSQLQuery(sqlDialect);
        
		try (PreparedStatement thePostStatement = aPSC.prepareStatement(createJournalAttributePostSQLQuery)) {
			// aPostStatement.clearParameters();
			thePostStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(thePostStatement, 2, anIdentifier);
			thePostStatement.setString(3, theEntry.getName());
			store(thePostStatement, 4, theEntry.getPostValue());
			//aPostStatement.addBatch();
			thePostStatement.execute();
		}
    }

	private String createCreateJournalAttributePostSQLQuery(DBHelper sqlDialect) {
		StringBuilder sb = new StringBuilder();

		sb.append("INSERT INTO ");
		sb.append(sqlDialect.tableRef("JOURNAL_ATTRIBUTE_POST"));
		sb.append(" VALUES(?,?,?,?,?,?,?,?)");

		return sb.toString();
	}

	private String createCreateJournalAttributePreSQLQuery(DBHelper sqlDialect) {
		StringBuilder sb = new StringBuilder();

		sb.append("INSERT INTO ");
		sb.append(sqlDialect.tableRef("JOURNAL_ATTRIBUTE_PRE"));
		sb.append(" VALUES(?,?,?,?,?,?,?,?)");
		
		return sb.toString();
	}

	/**
	 * Store the given single attribute using the Prepared statement.
	 * 
	 * @param ind
	 *        index where VAL_TYPE is found in pstm.
	 */
    private void store(PreparedStatement pstm, int ind, Object anObject) throws SQLException
    {
        if (anObject instanceof String) {
            pstm.setByte(ind, STRING_TYPE);
            String s = (String) anObject;
            pstm.setNull  (ind + 1, Types.INTEGER);
            pstm.setNull  (ind + 2, Types.DOUBLE);
            if (s.length() < 254) {
                pstm.setString(ind + 3, s);
                pstm.setNull  (ind + 4, Types.CLOB);
            }
            else {
                pstm.setNull  (ind + 3, Types.VARCHAR);
                pstm.setString(ind + 4, s);
            }
        }
        else if (anObject instanceof Number) {
            if (anObject instanceof Integer) {
                pstm.setByte  (ind    , INTEGER_TYPE);
                pstm.setInt   (ind + 1,((Integer) anObject).intValue());
                pstm.setNull  (ind + 2, Types.DOUBLE);
                pstm.setNull  (ind + 3, Types.VARCHAR);
                pstm.setNull  (ind + 4, Types.CLOB);
            }
            else if (anObject instanceof Double) {
                pstm.setByte  (ind,     DOUBLE_TYPE);
                pstm.setNull  (ind + 1, Types.INTEGER);
                pstm.setDouble(ind + 2,((Double) anObject).doubleValue());
                pstm.setNull  (ind + 3, Types.VARCHAR);
                pstm.setNull  (ind + 4, Types.CLOB);
            }
            else if (anObject instanceof Float) {
                pstm.setByte  (ind,     FLOAT_TYPE);
                pstm.setNull  (ind + 1, Types.INTEGER);
                pstm.setFloat (ind + 2,((Float) anObject).floatValue());
                pstm.setNull  (ind + 3, Types.VARCHAR);
                pstm.setNull  (ind + 4, Types.CLOB);
            }
            else if (anObject instanceof Long) {
                pstm.setByte  (ind,     LONG_TYPE);
                pstm.setLong  (ind + 1,((Long) anObject).longValue());
                pstm.setNull  (ind + 2, Types.DOUBLE);
                pstm.setNull  (ind + 3, Types.VARCHAR);
                pstm.setNull  (ind + 4, Types.CLOB);
            }
        }
        else if (anObject instanceof Date) {
            pstm.setByte  (ind,     DATE_TYPE);
            long l = ((Date) anObject).getTime();
            pstm.setLong  (ind + 1, l);
            pstm.setNull  (ind + 2, Types.DOUBLE);
            pstm.setNull  (ind + 3, Types.VARCHAR);
            pstm.setNull  (ind + 4, Types.CLOB);
        }
        else if (anObject instanceof Boolean) {
            if (((Boolean) anObject).booleanValue())
                pstm.setByte(ind, BOOLEAN_TRUE);
            else
                pstm.setByte(ind, BOOLEAN_FALSE);
            pstm.setNull  (ind + 1, Types.INTEGER);
            pstm.setNull  (ind + 2, Types.DOUBLE);
            pstm.setNull  (ind + 3, Types.VARCHAR);
            pstm.setNull  (ind + 4, Types.CLOB);
        }
        else if (anObject instanceof DataObject) {
            pstm.setByte  (ind,     LINK_TYPE);
            pstm.setNull  (ind + 1, Types.INTEGER);
            pstm.setNull  (ind + 2, Types.DOUBLE);
            DataObject theDO = (DataObject) anObject;
            if (anObject instanceof GenericDataObject) {
                GenericDataObject theEDo = (GenericDataObject) theDO;
                pstm.setString(ind + 3, 
                    theEDo.getMetaObjectName() + ' ' + theEDo.getIdentifier());
                
            } else {
                pstm.setString(ind + 3, 
                    theDO.tTable().getName() + ' ' + theDO.getIdentifier());
            }
            pstm.setNull  (ind + 4, Types.CLOB);
        }
        else {
            pstm.setByte  (ind,     NULL_TYPE);
            pstm.setNull  (ind + 1, Types.INTEGER);
            pstm.setNull  (ind + 2, Types.DOUBLE);
            pstm.setNull  (ind + 3, Types.VARCHAR);
            pstm.setNull  (ind + 4, Types.CLOB);
        }
    }

    @Override
	public Collection<JournalResultAttributeEntry> getJournal(long aCommitId, TLID anIdentifier,
            PooledConnection connection, DBHelper dbHelper) throws SQLException {

        GenericDataObject theDOPre;
        GenericDataObject theDOPost;
        //get pre values of commit
		String createGetAttributesPreSQLQuery = createGetAttributesPreSQLQuery(dbHelper);
		try (PreparedStatement theAttStatement = connection.prepareStatement(createGetAttributesPreSQLQuery)) {
			// theAttStatement.clearParameters();
			theAttStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(theAttStatement, 2, anIdentifier);
			try (ResultSet theAtts = theAttStatement.executeQuery()) {
			// are
			// the
			// pre
			// atts
			theDOPre = new GenericDataObject("", anIdentifier, DEFAULT_SIZE);
			while (theAtts.next()) {
				putResult(theDOPre.getMap(), theAtts, dbHelper, 3);
			}
			}
		}
        //get post values of commit
		String createGetAttributesPostSQLQuery = createGetAttributesPostSQLQuery(dbHelper);
		try (PreparedStatement theAttStatement = connection.prepareStatement(createGetAttributesPostSQLQuery)) {
			// theAttStatement.clearParameters();
			theAttStatement.setLong(1, aCommitId);
			IdentifierUtil.setId(theAttStatement, 2, anIdentifier);
			try (ResultSet theAtts = theAttStatement.executeQuery()) {
				// these are
			// the post
			// atts
			theDOPost = new GenericDataObject("", anIdentifier, DEFAULT_SIZE);
			while (theAtts.next()) {
				putResult(theDOPost.getMap(), theAtts, dbHelper, 3);
			}
			}
		}
        PrePostDataObjectHolder thePPDOH = new PrePostDataObjectHolder(theDOPre, theDOPost);
        Collection theAttNames = theDOPre.getMap().keySet();
		List<JournalResultAttributeEntry> theResult = new ArrayList<>(theAttNames.size());
        
        Iterator theIt = theAttNames.iterator();
        while (theIt.hasNext()) {
            String theAttName = (String) theIt.next();
            DOBasedChangeJournalResultAttributeEntry theAttEntry 
                = new DOBasedChangeJournalResultAttributeEntry(theAttName, thePPDOH);
            theResult.add(theAttEntry);
        }
        return theResult;
    }
    
	private String createGetAttributesPostSQLQuery(DBHelper dbHelper) {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(dbHelper.tableRef("JOURNAL_ATTRIBUTE_POST"));
		sb.append(" WHERE ");
		sb.append(dbHelper.columnRef("COMMIT_ID"));
		sb.append("=?");
		sb.append(" AND ");
		sb.append(dbHelper.columnRef("IDENTIFIER"));
		sb.append("=?");

		return sb.toString();
	}

	private String createGetAttributesPreSQLQuery(DBHelper dbHelper) {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(dbHelper.tableRef("JOURNAL_ATTRIBUTE_PRE"));
		sb.append(" WHERE ");
		sb.append(dbHelper.columnRef("COMMIT_ID"));
		sb.append("=?");
		sb.append(" AND ");
		sb.append(dbHelper.columnRef("IDENTIFIER"));
		sb.append("=?");

		return sb.toString();
	}

	/**
	 * Fill a GenericDataObject from a ResultSet. Mandatory entry-order is: - attribute name at
	 * position nameIndex - value type - LVAL - DVAL - SVAL - TVAL as when fetching via
	 * GET_ATTRIBUTES_PRE or ..._POST.
	 * 
	 * must be called from a synchronized context.
	 * 
	 * @param aMap
	 *        The Map of the dataObject to be filled.
	 * @param res
	 *        A ResultSet based on a DO_STORAGE.
	 * @param nameIndex
	 *        of resultSet's name-entry
	 */
    private void putResult(Map aMap, ResultSet res, DBHelper dbHelper, int nameIndex)
        throws SQLException {
        String name = res.getString(nameIndex);
        char val_tye = (char) res.getByte(nameIndex + 1);
        switch (val_tye) {
            case STRING_TYPE :
                String s = res.getString(nameIndex + 4); // SVAL
                if (s == null) {
					s = dbHelper.getClobValue(res, nameIndex + 5);
                }
                aMap.put(name, s);
                break;
            case INTEGER_TYPE :
				aMap.put(name, Integer.valueOf(res.getInt(nameIndex + 2))); // LVAL
                break;
            case LONG_TYPE :
				aMap.put(name, Long.valueOf(res.getLong(nameIndex + 2))); // LVAL
                break;
            case DATE_TYPE :
                long tmp = res.getLong(nameIndex + 2);
                aMap.put(name, new Date(tmp)); // LVAL
                break;
            case FLOAT_TYPE :
				aMap.put(name, Float.valueOf(res.getFloat(nameIndex + 3))); // DVAL
                break;
            case DOUBLE_TYPE :
				aMap.put(name, Double.valueOf(res.getDouble(nameIndex + 3))); // DVAL
                break;
            case BOOLEAN_TRUE :
                aMap.put(name, Boolean.TRUE);
                break;
            case BOOLEAN_FALSE :
                aMap.put(name, Boolean.FALSE);
                break;
            case LINK_TYPE :
                String theString = res.getString(nameIndex + 4); // SVAL
                aMap.put(name, theString);
                break;
            case NULL_TYPE :
                aMap.put(name, null);
                break;
            default :
                Logger.warn("Unknown Type '" + val_tye + "'", this);
        }
    }

    /**
     * This Class just holds the pre and the post data object for a DOBasedChangeJournalResultAttributeEntry.
     */
    private static class PrePostDataObjectHolder {
        public PrePostDataObjectHolder(DataObject aPre, DataObject aPost) {
            this.pre  = aPre;
            this.post = aPost;
        }
        DataObject pre;
        DataObject post;
    }
    
    /**
     * Holds the name and the data object holder for a single attribute.
     */
    private class DOBasedChangeJournalResultAttributeEntry implements ChangeJournalResultAttributeEntry {
        
        String                  name;
        PrePostDataObjectHolder holder;

        public DOBasedChangeJournalResultAttributeEntry(String aName, PrePostDataObjectHolder aHolder) {
            this.name   = aName;
            this.holder = aHolder;
        }
        
        /**
         * the name of the attribute journaled.
         */
        @Override
		public String getName() {
            return this.name;
        }
        
        /** 
         * Return classname of object found in post (previous) data 
         *
         * KHA what happens when getPost() returns null ?
         */
        @Override
		public String getType() {
            return this.getPost().getClass().toString();
        }

        @Override
		public Object getPre() {
            try {
                return this.holder.pre.getAttributeValue(this.name);
            } catch (NoSuchAttributeException e) {
                Logger.error("Attempt to access journal attribute "+this.name+" pre value failed.", e, this);
                return null;
            }
        }
        @Override
		public Object getPost() {
            try {
                return this.holder.post.getAttributeValue(this.name);
            } catch (NoSuchAttributeException e) {
                Logger.error("Attempt to access journal attribute "+this.name+" post value failed.", e, this);
                return null;
            }
        }
    }
}
