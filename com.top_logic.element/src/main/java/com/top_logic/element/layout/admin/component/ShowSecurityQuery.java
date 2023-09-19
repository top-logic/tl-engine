/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin.component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.db.DBUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Query for rows in the security storage.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ShowSecurityQuery {

	/** 
	 * Creates a {@link ShowSecurityQuery}.
	 */
	public ShowSecurityQuery() {
	}

	/** 
	 * Query the security storage for the given parameters.
	 * 
	 * @param    aPerson     The requested person, may be <code>null</code>.
	 * @param    aType       The requested business object type, may be <code>null</code>.
	 * @param    someBOs     The requested business objects, may be <code>null</code>.
	 * @param    someRoles   The requested roles, may be <code>null</code>.
	 * @return   The query result converted to a list of string arrays, never <code>null</code>.
	 */
	public List<Object[]> query(Person aPerson, Object aType, List<Wrapper> someBOs, List<BoundedRole> someRoles) {

		DBHelper sqlDialect;
		try {
			sqlDialect = ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect();
		} catch (SQLException ex) {
			Logger.error("Failed to execute DB query.", ex, ShowSecurityQuery.class);
			throw new RuntimeException(ex);
		}

		StringBuffer theQuery =
			new StringBuffer("SELECT DISTINCT * FROM ").append(sqlDialect.tableRef(SecurityStorage.TABLE_NAME));
        boolean      hasWhere = false;

		hasWhere = this.addGroupQuery(sqlDialect, aPerson, theQuery, hasWhere);
        hasWhere = this.addTypeQuery(aType, theQuery, hasWhere);
		hasWhere = this.addBusinessObjectsQuery(sqlDialect, someBOs, theQuery, hasWhere);
		hasWhere = this.addRolesQuery(sqlDialect, someRoles, theQuery, hasWhere);

        return this.query(theQuery);
	}

	/**
	 * Execute the given query.
	 * 
	 * @param aQuery
	 *        The query to be executed, must not be <code>null</code>.
	 * @return The query result converted to a list of string arrays, never <code>null</code>.
	 * @see DBUtil#executeQueryAsMatrix(String,DBType[])
	 */
	protected List<Object[]> query(StringBuffer aQuery) {
		try {
			return DBUtil.executeQueryAsMatrix(aQuery.toString(), new DBType[] { DBType.LONG, DBType.LONG, DBType.LONG,
				DBType.SHORT });
        }
        catch (SQLException ex) {
            throw new TopLogicException(ShowSecurityQuery.class, "query.failed", ex);
        }
	}

	/** 
	 * Add the given roles to the query string (when not null or empty).
	 * @param    someRoles    The requested roles, may be <code>null</code>.
	 * @param    aQuery       The query to be extended, must not be <code>null</code>.
	 * @param    hasWhere     <code>true</code> when where clause is already part of the query.
	 * 
	 * @return   <code>true</code> when where clause is now part of the query.
	 */
	protected boolean addRolesQuery(DBHelper sqlDialect, List<BoundedRole> someRoles, StringBuffer aQuery, boolean hasWhere) {
        if (!CollectionUtil.isEmptyOrNull(someRoles)) {
			return this.appendWrapperIDs(sqlDialect, aQuery, someRoles, SecurityStorage.ATTRIBUTE_ROLE, hasWhere);
        }
        else {
			return hasWhere;
        }
	}

	/** 
	 * Add the given business objects and their security parents to the query string (when not null or empty).
	 * @param    someBOs     The requested business objects, may be <code>null</code>.
	 * @param    aQuery      The query to be extended, must not be <code>null</code>.
	 * @param    hasWhere    <code>true</code> when where clause is already part of the query.
	 * 
	 * @return   <code>true</code> when where clause is now part of the query.
	 */
	protected boolean addBusinessObjectsQuery(DBHelper sqlDialect, List<Wrapper> someBOs, StringBuffer aQuery, boolean hasWhere) {
        if (!CollectionUtil.isEmptyOrNull(someBOs)) {
			// Adding security parents of objects also
			Set<Object> theBOs = new HashSet<>(someBOs);

			for (Wrapper theWrapper : someBOs) {
			    BoundObject theBO = theWrapper instanceof BoundObject ? ((BoundObject) theWrapper).getSecurityParent() : null;

			    while (theBO != null) {
			        theBOs.add(theBO);
			        theBO = theBO.getSecurityParent();
			    }
			}

			return this.appendWrapperIDs(sqlDialect, aQuery, CollectionUtil.dynamicCastView(Wrapper.class, theBOs),
				SecurityStorage.ATTRIBUTE_BUSINESS_OBJECT, hasWhere);
        }
        else {
			return hasWhere;
        }
	}

	/** 
	 * Add the given business object type to the query string (not supported at the moment).
	 * 
	 * @param    aType       The requested business object type, may be <code>null</code>.
	 * @param    aQuery      The query to be extended, must not be <code>null</code>.
	 * @param    hasWhere    <code>true</code> when where clause is already part of the query.
	 * @return   <code>true</code> when where clause is now part of the query.
	 */
	protected boolean addTypeQuery(Object aType, StringBuffer aQuery, boolean hasWhere) {
		// type filter in DB query is not possible at this time as business objects
		// are stored only with their IDs but not with their types.
		return hasWhere;
	}

	/** 
	 * Add the groups the given person is assigned too to the query string.
	 * @param    aPerson      The requested person, may be <code>null</code>.
	 * @param    aQuery       The query to be extended, must not be <code>null</code>.
	 * @param    hasWhere     <code>true</code> when where clause is already part of the query.
	 * 
	 * @return   <code>true</code> when where clause is now part of the query.
	 */
	protected boolean addGroupQuery(DBHelper sqlDialect, Person aPerson, StringBuffer aQuery, boolean hasWhere) {
        if (aPerson != null) {
			{
				Group      theGroup  = aPerson.getRepresentativeGroup();
				Set<Group> theGroups = new HashSet<>(CollectionUtil.dynamicCastView(Group.class, Group.getGroups(aPerson, true, true)));

				if (theGroup == null) {
				    Logger.error("Selected person (" + aPerson.getName() + ") has no representative group.", ShowSecurityQuery.class);
				}
				else {
				    theGroups.add(theGroup);
				}

				return this.appendWrapperIDs(sqlDialect, aQuery, theGroups, SecurityStorage.ATTRIBUTE_GROUP, hasWhere);
	        }
        }
        else {
			return hasWhere;
        }
	}

	/** 
	 * Append the given collection of wrappers as where clause to the query.
	 * @param    aQuery          The query to be extended, must not be <code>null</code>.
	 * @param    someWrappers    The wrappers to get the ID from, must not be <code>null</code>.
	 * @param    aColumn         Name of the requested column, must not be <code>null</code>.
	 * @param    hasWhere        <code>true</code> when where clause is already part of the query.
	 * 
	 * @return   <code>true</code> when where clause is now part of the query.
	 */
	protected boolean appendWrapperIDs(DBHelper sqlDialect, StringBuffer aQuery,
			Collection<? extends Wrapper> someWrappers, String aColumn, boolean hasWhere) {

		aQuery.append(hasWhere ? " AND " : " WHERE ").append(sqlDialect.columnRef(aColumn)).append(" IN ");

		List<Wrapper> wrapperBuffer = new ArrayList<>(someWrappers);
		List<TLID> groupIds = WrapperFactory.toObjectNamesInline(wrapperBuffer);
		sqlDialect.literalSet(aQuery, DBType.ID, groupIds);

		return true;
	}
}

