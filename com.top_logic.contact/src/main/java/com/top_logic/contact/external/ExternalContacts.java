/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * DB accessor frontend for managing {@link ExternalContact}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalContacts implements Reloadable {

    public static final String USER_ID = "userID";

	/**
	 * White space pattern.
	 */
	private static final String WS = " *";
	
	/**
	 * Pattern for character introducing the {@link ExternalContact#getDivision() division}.
	 */
	private static final String DIVISION_PREFIX = "\\(";
	
	/**
	 * Pattern for character closing the {@link ExternalContact#getDivision() division}.
	 */
	private static final String DIVISION_SUFFIX = "\\)";
	
	/**
	 * Pattern for character signaling a name given as (last name, first name). 
	 */
	private static final String LAST_FIRST_SEPARATOR = ",";
	
	/**
	 * Pattern for character signaling a name given as (first name, last name).
	 */
	private static final String FIRST_LAST_SEPARATOR = " ";

	/**
	 * Syntax of a pattern for a family name with division.
	 */
	public static final Pattern LAST_DIVISION_PATTERN = 
		Pattern.compile("([^" + DIVISION_PREFIX + "]*)" + WS + DIVISION_PREFIX + WS + "([^" + DIVISION_SUFFIX + "]*)(?:" + WS + DIVISION_SUFFIX + WS + ")?");

	/**
	 * Syntax of a pattern for a family name with appended first name.
	 */
	public static final Pattern LAST_FIRST_PATTERN = 
		Pattern.compile("([^" + LAST_FIRST_SEPARATOR + "]*)" + WS + LAST_FIRST_SEPARATOR + WS + "(.*)");

	/**
	 * Syntax of a pattern for a first name with appended last name.
	 */
	public static final Pattern FIRST_LAST_PATTERN = 
    	Pattern.compile("([^" + FIRST_LAST_SEPARATOR + "]*)" + FIRST_LAST_SEPARATOR + WS + "(.*)");

	
	private static List<ExternalContact> cachedContacts;
	
	private final static ExternalContacts INSTANCE = new ExternalContacts();
    
    static {
        ReloadableManager.getInstance().addReloadable(INSTANCE);
    }
    
    @Override
	public String getDescription() {
        return "Cache for ExternalContacts";
    }
    
    @Override
	public String getName() {
        return "ExternalContactCache";
    }
    
    @Override
	public boolean reload() {
        resetContactCache();
        return true;
    }
    
    @Override
	public boolean usesXMLProperties() {
        return false;
    }
	
	/**
	 * Associates the given contact with the given object and attribute.
	 * 
	 * <p>
	 * If other contacts are already associated with the given object and
	 * attribute those assignments are removed. The assignment is valid from the
	 * given time <code>notBefore</code>, until it is removed by a call to
	 * another method of this class.
	 * </p>
	 * 
	 * @param objectId
	 *     The object, the given contact is associated with.
	 * @param attributeId
	 *     The attribute, the given contact is associated with.
	 * @param notBefore
	 *     The time when the given contact was associated with the given object 
	 *     and attribute.
	 * @param contact
	 *     The contact to associate with the given object and attribute. If it 
	 *     is <code>null</code>, all contact assignments are removed.
	 */
	public static void setContact(TLID objectId, TLID attributeId, Date notBefore, ExternalContact contact) throws SQLException {
		updateContacts(objectId, attributeId, notBefore, 
			contact == null ? Collections.EMPTY_SET : Collections.singleton(contact));
	}

	public static HashSet<ExternalContact> updateContacts(TLID objectId, TLID attributeId, Date notBefore, Set<ExternalContact> contacts) throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
		
		HashSet<ExternalContact> updated = new HashSet<>();
		Set<ExternalContact> keptContacts = removeCurrentAssignments(ctx, objectId, attributeId, notBefore, contacts);
		for (Iterator<ExternalContact> it = contacts.iterator(); it.hasNext();) {
			ExternalContact contact = it.next();
			if (keptContacts.contains(contact)) continue;

			assignContact(ctx, objectId, attributeId, notBefore, contact);
			updated.add(contact);
		}
		
		return updated;
	}
	
	private static void assignContact(CommitContext ctx, TLID objectId, TLID attributeId, Date notBefore, ExternalContact contact) throws SQLException {
		PooledConnection connection = ctx.getConnection();
		DBHelper sqlDialect = connection.getSQLDialect();
		try (PreparedStatement statement = connection.prepareStatement(procAssignContact(sqlDialect))) {
			int index = 1;
			IdentifierUtil.setId(statement, index++, objectId);
			IdentifierUtil.setId(statement, index++, attributeId);
			sqlDialect.setTimestamp(statement, index++, new Timestamp(notBefore.getTime()));
			index = setContact(statement, index, contact);
			statement.execute();
		}
	}

	private static Set<ExternalContact> removeCurrentAssignments(CommitContext ctx, TLID objectId, TLID attributeId, Date notBefore, Set except) throws SQLException {
		DBHelper sqlDialect = ctx.getConnection().getSQLDialect();
		PreparedStatement statement = ctx.getConnection().prepareStatement(procGetCurrentContacts(sqlDialect, true),
			ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		try {
        	int index = 1;
			IdentifierUtil.setId(statement, index++, objectId);
			IdentifierUtil.setId(statement, index++, attributeId);
        	statement.execute();

        	ResultSet result = statement.getResultSet();
        	
        	HashSet<ExternalContact> keptContacts = new HashSet<>();
        	while (result.next()) {
        		index = 1;
				ExternalContact assignedContact = fetchContact(sqlDialect, result, index);

        		if (except.contains(assignedContact)) {
        			keptContacts.add(assignedContact);
        		} else {
        			// Mark the current assignments as removed by setting the not_after date.
        			result.updateTimestamp("not_after", new Timestamp(notBefore.getTime()));
        			result.updateRow();
        		}
        	}
        	
        	return keptContacts;
		} finally {
			statement.close();
		}
	}
	
	public static Set<ExternalContact> getCurrentContacts(TLID objectId, TLID attributeId) throws SQLException {
		ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		DBHelper sqlDialect = pool.getSQLDialect();
        PooledConnection ctx = pool.borrowReadConnection();
		try {
			try (PreparedStatement statement = ctx.prepareStatement(procGetCurrentContacts(sqlDialect, false))) {
		        int index = 1;
				IdentifierUtil.setId(statement, index++, objectId);
				IdentifierUtil.setId(statement, index++, attributeId);
		        statement.execute();
		        
				try (ResultSet result = statement.getResultSet()) {
					return fetchContactSet(sqlDialect, result);
				}
		    }
		} finally {
		    pool.releaseReadConnection(ctx);
		}
	}

    public static Set<ExternalContact> getContactHistory(TLID objectId, TLID attributeId) throws SQLException {
        ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		DBHelper sqlDialect = pool.getSQLDialect();
        PooledConnection ctx = pool.borrowReadConnection();
    
        try {
			try (PreparedStatement statement = ctx.prepareStatement(procGetHistory(sqlDialect))) {
    			int index = 1;
				IdentifierUtil.setId(statement, index++, objectId);
				IdentifierUtil.setId(statement, index++, attributeId);
    			statement.execute();
    
				try (ResultSet result = statement.getResultSet()) {
					return fetchContactSet(sqlDialect, result);
				}
    		}
        } finally {
            pool.releaseReadConnection(ctx);
        }
    }
    /** 
     * Get the Person with the UNumber of the given contact
     * 
     * @param aContact	the ExternalContact
     * @return the Person or <code>null</code> if no matching person can be found
     */
    public static Person getPersonForExternalContact(ExternalContact aContact) {
    	if (aContact == null) {
    		return null;
    	}
    	
    	String theUNo = aContact.getUNumber();
    	if (!StringServices.isEmpty(theUNo)) {
    		return Person.byName(theUNo);
    	}
    	
    	return null;
    }
    
    /** 
     * Get the PersonContact with the UNumber of the given contact
     * 
     * @param aContact	the ExternalContact
     * @return the PersonContact or <code>null</code> if no matching PersonContact can be found
     */
    public static PersonContact getContactForExternalContact(ExternalContact aContact) {
    	if (aContact == null) {
    		return null;
    	}
    	
    	String theUNo = aContact.getUNumber();
    	if (!StringServices.isEmpty(theUNo)) {
    		Iterator theIt = ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE, USER_ID, theUNo, false).iterator();
    		while (theIt.hasNext()) {
    			PersonContact thePC = (PersonContact) theIt.next();
    			if (theUNo.equals(thePC.getValue(USER_ID))) {
    				return thePC;
    			}
    		}
    	}
    	
    	return null;
    }
	
	public static void dropObjectHistory(TLID objectId) throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
		PooledConnection connection = ctx.getConnection();
		
		try (PreparedStatement statement =
			connection.prepareStatement(procDropObjectHistory(connection.getSQLDialect()))) {
			int index = 1;
			IdentifierUtil.setId(statement, index++, objectId);
			statement.execute();
		}
	}

	public static void newContact(ExternalContact contact) throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
        
		PooledConnection connection = ctx.getConnection();
		try (PreparedStatement statement =
			connection.prepareStatement(procInsertIntoContacts(connection.getSQLDialect()))) {
			int index = 1;
			index = setContact(statement, index, contact);
			statement.execute();
		}
	}

	public static void dropContact(String uNumber, String aSystem) throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
		
		PooledConnection connection = ctx.getConnection();
		try (PreparedStatement statement =
			connection.prepareStatement(procRemoveFromContacts(connection.getSQLDialect()))) {
			int index = 1;
			statement.setString(index++, uNumber);
			statement.setString(index++, aSystem);
			statement.execute();
		}
	}
	
	public static void dropAllContacts() throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
		
		PooledConnection connection = ctx.getConnection();
		try (PreparedStatement statement =
			connection.prepareStatement(procRemoveAllContacts(connection.getSQLDialect()))) {
			statement.execute();
		}
	}
	
	public static void dropAllContactsBySysID(String aSysID) throws SQLException {
		KnowledgeBase db = PersistencyLayer.getKnowledgeBase();
		CommitContext ctx = KBUtils.createCommitContext(db);
		
		PooledConnection connection = ctx.getConnection();
		try (PreparedStatement statement =
			connection.prepareStatement(procRemoveAllContactsBySysid(connection.getSQLDialect()))) {
			// Fill the statement parameters.
			{
				int index = 1;
				statement.setString(index++, aSysID);
			}
			statement.execute();
		}
	}
	
	public static ExternalContact getContact(String uNumber, String aSystemID) throws SQLException {
	    ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		DBHelper sqlDialect = pool.getSQLDialect();
        PooledConnection ctx = pool.borrowReadConnection();
        try {
			try (PreparedStatement statement = ctx.prepareStatement(procGetContactByUNumber(sqlDialect))) {
    			// Fill the statement parameters.
    			{
    				int index = 1;
    				statement.setString(index++, uNumber);
    				statement.setString(index++, aSystemID);
    			}
    			statement.execute();
    
				try (ResultSet result = statement.getResultSet()) {
    			if (!result.next())
    				return null;
    
    			int index = 1;
				ExternalContact contact = fetchContact(sqlDialect, result, index);
    
    			assert !result.next() : "Exactly one match.";
    
    			return contact;
    		}
			}
        } finally {
            pool.releaseReadConnection(ctx);
        }
	}
	
	public static List<ExternalContact> getAllContacts() throws SQLException {
		ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		DBHelper sqlDialect = pool.getSQLDialect();
		PooledConnection ctx = pool.borrowReadConnection();
		try {
			
			try (PreparedStatement statement = ctx.prepareStatement(procGetAllContacts(sqlDialect))) {
				statement.execute();
				
				return fetchContactList(sqlDialect, statement);
			}
		} finally {
			pool.releaseReadConnection(ctx);
		}
	}

	public static List<String> getAllSystemIDs(boolean used) throws SQLException {
	    ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
        PooledConnection ctx = pool.borrowReadConnection();
        try {
            
			DBHelper sqlDialect = ctx.getSQLDialect();
			try (PreparedStatement statement =
				ctx.prepareStatement(used ? procGetAllUsedSysids(sqlDialect) : procGetAllSysids(sqlDialect))) {
    			statement.execute();
    
    			return fetchStringList(statement);
    		}
        } finally {
            pool.releaseReadConnection(ctx);
        }
	}
	
	public static List<ExternalContact> getAllContactsCached() throws SQLException {
		if (cachedContacts == null) {
			cachedContacts = getAllContacts();
		}
		
		return cachedContacts;
	}
	
	public static void resetContactCache() {
		cachedContacts = null;
	}
	
	/**
	 * Looks up matching {@link ExternalContact}s from the external contact table. 
	 * 
	 * <p>
	 * The given pattern may have any of the following froms:
	 * </p>
	 * 
	 * <ul>
	 * <li>first-name last-name</li>
	 * <li>last-name, first-name</li>
	 * <li>last-name (division)</li>
	 * <li>first-name last-name (division)</li>
	 * <li>last-name, first-name (division)</li>
	 * <li>last-name</li>
	 * <li>first-name</li>
	 * <li>division</li>
	 * <li>e-mail</li>
	 * <li>phone-number</li>
	 * <li>u-number</li>
	 * </ul>
	 */
	public static List<ExternalContact> getMatchingContacts(String pattern) throws SQLException, IOException {
	    ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		DBHelper sqlDialect = pool.getSQLDialect();
        PooledConnection ctx = pool.borrowReadConnection();
        try {
		
    		pattern = pattern.trim();
    		
    		Matcher lastDivision = LAST_DIVISION_PATTERN.matcher(pattern);
    		if (lastDivision.matches()) {
    			String firstNamePattern;
    			String lastNamePattern;
    			String divisionPattern;
    			{
    				int index = 1;
    				String name = lastDivision.group(index++).trim();
    
    				Matcher lastFirst = LAST_FIRST_PATTERN.matcher(name);
    				if (lastFirst.matches()) {
    					firstNamePattern = quotePattern(lastFirst.group(2));
    					lastNamePattern = quotePattern(lastFirst.group(1));
    				} else {
    					Matcher firstLast = FIRST_LAST_PATTERN.matcher(name);
    					if (firstLast.matches()) {
    						firstNamePattern = quotePattern(firstLast.group(1));
    						lastNamePattern = quotePattern(firstLast.group(2));
    					} else {
    						firstNamePattern = "%";
    						lastNamePattern = quotePattern(name);
    					}
    				}
    				
    				divisionPattern = quotePattern(lastDivision.group(index++));
    			}
    			
				try (PreparedStatement statement =
					ctx.prepareStatement(procMatchContactsNameDivision(sqlDialect))) {
    				// Fill the statement parameters.
    				{
    					int index = 1;
    					statement.setString(index++, firstNamePattern);
    					statement.setString(index++, lastNamePattern);
    					statement.setString(index++, divisionPattern);
    				}
    				statement.execute();
    
    				return fetchContactList(sqlDialect, statement);
    			}
    
    		} else {
    			Matcher lastFirst = LAST_FIRST_PATTERN.matcher(pattern);
    			if (lastFirst.matches()) {
    				int index = 1;
    				String lastNamePattern = quotePattern(lastFirst.group(index++));
    				String firstNamePattern = quotePattern(lastFirst.group(index++));
    
    				return getContactsByFirstAndFamilyName(ctx, firstNamePattern, lastNamePattern);
    			} else {
    				Matcher firstLast = FIRST_LAST_PATTERN.matcher(pattern);
    				if (firstLast.matches()) {
    					int index = 1;
    					String firstNamePattern = quotePattern(firstLast.group(index++));
    					String lastNamePattern = quotePattern(firstLast.group(index++));
    
    					return getContactsByFirstAndFamilyName(ctx, firstNamePattern, lastNamePattern);
    				} else {
    					String textPattern = quotePattern(pattern);
    
    					String uNumberPattern;
    					if (textPattern.length() > 0 && Character.isDigit(textPattern.charAt(0))) {
    						uNumberPattern = 'u' + textPattern;
    					} else {
    						uNumberPattern = textPattern;
    					}
    					
						try (PreparedStatement statement =
							ctx.prepareStatement(procMatchContactsAny(sqlDialect))) {
    						// Fill the statement parameters.
    						{
    							int index = 1;
    							statement.setString(index++, uNumberPattern);
    							statement.setString(index++, textPattern);
    							statement.setString(index++, textPattern);
    							statement.setString(index++, textPattern);
    							statement.setString(index++, textPattern);
    							statement.setString(index++, textPattern);
    							statement.setString(index++, textPattern);
    						}
    						statement.execute();
    						return fetchContactList(sqlDialect, statement);
    					}
    				}
    			}
    		}
        } finally {
            pool.releaseReadConnection(ctx);
        }
	}

	private static List<ExternalContact> getContactsByFirstAndFamilyName(PooledConnection ctx, String firstNamePattern,
			String lastNamePattern) throws SQLException, IOException {
		DBHelper sqlDialect = ctx.getSQLDialect();
		try (PreparedStatement statement = ctx.prepareStatement(procMatchContactsFirstLast(sqlDialect))) {
			// Fill the statement parameters.
			{
				int index = 1;
				statement.setString(index++, firstNamePattern);
				statement.setString(index++, lastNamePattern);
			}
			statement.execute();
			
			return fetchContactList(sqlDialect, statement);
		}
	}
	
	private static Set<ExternalContact> fetchContactSet(DBHelper sqlDialect, ResultSet result) throws SQLException {
		HashSet<ExternalContact> contacts = new HashSet<>();
		fetchContacts(sqlDialect, result, contacts);
		return contacts;
	}
	
	private static List<ExternalContact> fetchContactList(DBHelper sqlDialect, PreparedStatement statement)
			throws SQLException {
		ArrayList<ExternalContact> contacts = new ArrayList<>();
		try (ResultSet resultSet = statement.getResultSet()) {
			fetchContacts(sqlDialect, resultSet, contacts);
		}
		return contacts;
	}

	private static List<String> fetchStringList(PreparedStatement statement) throws SQLException {
		ArrayList<String> names     = new ArrayList<>();
		ResultSet         resultSet = statement.getResultSet();
		
		while(resultSet.next()) {
			names.add(resultSet.getString(1));
		}
		
		return names;
	}

	private static void fetchContacts(DBHelper sqlDialect, ResultSet result, Collection<ExternalContact> contacts)
			throws SQLException {
		int index;
		while (result.next()) {
			index = 1;
			ExternalContact contact = fetchContact(sqlDialect, result, index);

			contacts.add(contact);
		}
	}
	
	private static String quotePattern(String pattern) {
		return StringServices.replace(StringServices.replace(pattern, '%', "\\%"), '_', "\\_") + "%";
	}
	
	private static int setContact(PreparedStatement statement, int index, ExternalContact contact) throws SQLException {
		statement.setString(index++, contact.getUNumber());
		statement.setString(index++, contact.getFirstName());
		statement.setString(index++, contact.getFamilyName());
		statement.setString(index++, contact.getDivision());
		statement.setString(index++, contact.getEMail());
		statement.setString(index++, contact.getPhone());
		statement.setString(index++, contact.getSysId());
		return index;
	}

	private static ExternalContact fetchContact(DBHelper sqlDialect, ResultSet result, int index) throws SQLException {
        PlainExternalContact contact;
        if (result.getMetaData().getColumnCount() > 7) {
            contact = 
                new PlainExternalContactAssignment(
                    result.getString(index++),
                    result.getString(index++),
                    result.getString(index++),
                    result.getString(index++),
                    result.getString(index++),
                    result.getString(index++),
					sqlDialect.getTimestamp(result, index++),
					sqlDialect.getTimestamp(result, index++),
                    result.getString(index++)
                    );  
        }
        else {
            contact = 
    			new PlainExternalContact(
    				result.getString(index++),
    				result.getString(index++),
    				result.getString(index++),
    				result.getString(index++),
    				result.getString(index++),
    				result.getString(index++),
    				result.getString(index++));
        }
		return contact;
	}

	private static String procMatchContactsFirstLast(DBHelper sqlDialect) throws IOException {
		StringBuilder sql = new StringBuilder();
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitStart(sql, 10);
		}
		sql.append("SELECT ");
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitColumns(sql, 10);
		}
		sql.append(" * FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(firstName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? AND ");
		sql.append(familyName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ?");
		sql.append(" ORDER BY ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitLast(sql, 10);
		}
		return sql.toString();
	}

	private static String procMatchContactsNameDivision(DBHelper sqlDialect) throws IOException {
		StringBuilder sql = new StringBuilder();
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitStart(sql, 10);
		}
		sql.append("SELECT ");
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitColumns(sql, 10);
		}
		sql.append(" * FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(firstName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? AND ");
		sql.append(familyName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? AND ");
		sql.append(division(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ?");
		sql.append(" ORDER BY ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitLast(sql, 10);
		}
		return sql.toString();
	}

	private static String procMatchContactsAny(DBHelper sqlDialect) throws IOException {
		StringBuilder sql = new StringBuilder();
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitStart(sql, 10);
		}
		sql.append("SELECT ");
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitColumns(sql, 10);
		}
		sql.append(" * FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(unumber(sqlDialect));
		sql.append(" = ? OR ");
		sql.append(firstName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? OR ");
		sql.append(familyName(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? OR ");
		sql.append(division(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? OR ");
		sql.append(eMail(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? OR ");
		sql.append(phone(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ? OR ");
		sql.append(sysId(sqlDialect));
		sql.append(" ");
		sqlDialect.appendLikeCaseInsensitive(sql);
		sql.append(" ?");
		sql.append(" ORDER BY ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		if (sqlDialect.supportsLimitStop()) {
			sqlDialect.limitLast(sql, 10);
		}
		return sql.toString();
	}

	private static String procGetCurrentContacts(DBHelper sqlDialect, boolean forUpdate) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append(unumber(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		sql.append(", ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(division(sqlDialect));
		sql.append(", ");
		sql.append(eMail(sqlDialect));
		sql.append(", ");
		sql.append(phone(sqlDialect));
		sql.append(", ");
		sql.append(notBefore(sqlDialect));
		sql.append(", ");
		sql.append(notAfter(sqlDialect));
		sql.append(", ");
		sql.append(object(sqlDialect));
		sql.append(", ");
		sql.append(attribute(sqlDialect));
		sql.append(", ");
		sql.append(sysId(sqlDialect));
		sql.append(" FROM ");
		sql.append(externalContactAssignment(sqlDialect));
		if (forUpdate) {
			sql.append(sqlDialect.forUpdate1());
		}
		sql.append(" WHERE ");
		sql.append(object(sqlDialect));
		sql.append(" = ? AND ");
		sql.append(attribute(sqlDialect));
		sql.append(" = ? AND ");
		sql.append(notAfter(sqlDialect));
		sql.append(" IS NULL");
		if (forUpdate) {
			sql.append(sqlDialect.forUpdate2());
		}
		return sql.toString();
	}

	private static String procInsertIntoContacts(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" (");
		sql.append(unumber(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		sql.append(", ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(division(sqlDialect));
		sql.append(", ");
		sql.append(eMail(sqlDialect));
		sql.append(", ");
		sql.append(phone(sqlDialect));
		sql.append(", ");
		sql.append(sysId(sqlDialect));
		sql.append(")");
		sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?)");
		return sql.toString();
	}

	private static String procRemoveFromContacts(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(unumber(sqlDialect));
		sql.append(" = ? AND ");
		sql.append(sysId(sqlDialect));
		sql.append(" = ?");
		return sql.toString();
	}

	private static String procRemoveAllContacts(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(externalContactAll(sqlDialect));
		return sql.toString();
	}

	private static String procRemoveAllContactsBySysid(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(sysId(sqlDialect));
		sql.append(" = ?");
		return sql.toString();
	}

	private static String procDropObjectHistory(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(externalContactAssignment(sqlDialect));
		sql.append(" WHERE ");
		sql.append(object(sqlDialect));
		sql.append(" = ?");
		return sql.toString();
	}

	private static String procGetContactByUNumber(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT *");
		sql.append(" FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" WHERE ");
		sql.append(unumber(sqlDialect));
		sql.append(" = ? AND ");
		sql.append(sysId(sqlDialect));
		sql.append(" = ?");
		return sql.toString();
	}

	private static String procGetAllContacts(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" ORDER BY ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		return sql.toString();
	}

	private static String procGetAllSysids(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ");
		sql.append(sysId(sqlDialect));
		sql.append(" FROM ");
		sql.append(externalContactAll(sqlDialect));
		sql.append(" ORDER BY ");
		sql.append(sysId(sqlDialect));
		return sql.toString();
	}

	private static String procGetAllUsedSysids(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ");
		sql.append(sysId(sqlDialect));
		sql.append(" FROM ");
		sql.append(externalContactAssignment(sqlDialect));
		sql.append(" ORDER BY ");
		sql.append(sysId(sqlDialect));
		return sql.toString();
	}

	private static String procGetHistory(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append(unumber(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		sql.append(", ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(division(sqlDialect));
		sql.append(", ");
		sql.append(eMail(sqlDialect));
		sql.append(", ");
		sql.append(phone(sqlDialect));
		sql.append(", ");
		sql.append(notBefore(sqlDialect));
		sql.append(", ");
		sql.append(notAfter(sqlDialect));
		sql.append(", ");
		sql.append(sysId(sqlDialect));
		sql.append(" FROM ");
		sql.append(externalContactAssignment(sqlDialect));
		sql.append(" WHERE ");
		sql.append(object(sqlDialect));
		sql.append(" = ? AND ");
		sql.append(attribute(sqlDialect));
		sql.append(" = ?");
		sql.append(" ORDER BY ");
		sql.append(notBefore(sqlDialect));
		return sql.toString();
	}

	private static String procAssignContact(DBHelper sqlDialect) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		sql.append(externalContactAssignment(sqlDialect));
		sql.append(" (");
		sql.append(object(sqlDialect));
		sql.append(", ");
		sql.append(attribute(sqlDialect));
		sql.append(", ");
		sql.append(notBefore(sqlDialect));
		sql.append(", ");
		sql.append(unumber(sqlDialect));
		sql.append(", ");
		sql.append(firstName(sqlDialect));
		sql.append(", ");
		sql.append(familyName(sqlDialect));
		sql.append(", ");
		sql.append(division(sqlDialect));
		sql.append(", ");
		sql.append(eMail(sqlDialect));
		sql.append(", ");
		sql.append(phone(sqlDialect));
		sql.append(", ");
		sql.append(sysId(sqlDialect));
		sql.append(")");
		sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sql.toString();
	}

	private static String attribute(DBHelper sqlDialect) {
		return sqlDialect.columnRef("attribute");
	}

	private static String notAfter(DBHelper sqlDialect) {
		return sqlDialect.columnRef("not_after");
	}

	private static String notBefore(DBHelper sqlDialect) {
		return sqlDialect.columnRef("not_before");
	}

	private static String externalContactAll(DBHelper sqlDialect) {
		return sqlDialect.tableRef("EXTERNAL_CONTACT_ALL");
	}

	private static String externalContactAssignment(DBHelper sqlDialect) {
		return sqlDialect.tableRef("EXTERNAL_CONTACT_ASSIGNMENT");
	}

	private static String firstName(DBHelper sqlDialect) {
		return sqlDialect.columnRef("first_name");
	}

	private static String familyName(DBHelper sqlDialect) {
		return sqlDialect.columnRef("family_name");
	}

	private static String sysId(DBHelper sqlDialect) {
		return sqlDialect.columnRef("sys_id");
	}

	private static String unumber(DBHelper sqlDialect) {
		return sqlDialect.columnRef("u_number");
	}

	private static String object(DBHelper sqlDialect) {
		return sqlDialect.columnRef("object");
	}

	private static String phone(DBHelper sqlDialect) {
		return sqlDialect.columnRef("phone");
	}

	private static String eMail(DBHelper sqlDialect) {
		return sqlDialect.columnRef("e_mail");
	}

	private static String division(DBHelper sqlDialect) {
		return sqlDialect.columnRef("division");
	}

}
