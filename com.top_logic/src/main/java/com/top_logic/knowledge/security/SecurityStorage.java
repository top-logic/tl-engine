/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.PropertyType;
import com.top_logic.base.cluster.PendingChangeException;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DB2Helper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.PostgreSQLHelper;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.SimpleCommitHandler;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.service.ThreadLocalCommitable;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.db.DBUtil;
import com.top_logic.util.db.DBUtil.ResultAsMapFromStringToWrapperList;

/**
 * Storage for permissions.
 * 
 * <p>
 * The storage contains tuples of the form (group, business object, role, reason) meaning "a group
 * has a role on a business object for a reason".
 * </p>
 * 
 * @see #startUp(AccessManager) For initialization with an {@link AccessManager} before usage.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SecurityStorage implements ConfiguredInstance<SecurityStorage.Config>, Reloadable {

	/**
	 * {@link TypedConfiguration} interface for the {@link SecurityStorage}.
	 */
	public interface Config extends PolymorphicConfiguration<SecurityStorage> {

		/** Property name of {@link #getDisableIndex()}. */
		String DISABLE_INDEX = "disable-index";

		/**
		 * @see SecurityStorage#disableIndex
		 */
		@Name(DISABLE_INDEX)
		boolean getDisableIndex();

	}

	/** Marker for the thread that starts the reload of this storage. */
	public static final Property<Boolean> RELOADING_THREAD =
		TypedAnnotatable.property(Boolean.class, "rebuild thread", Boolean.FALSE);

	/** Name of the {@link MetaObject} describing the {@link SecurityStorage}. */
	public static final String SECURITY_STORAGE_OBJECT_NAME = "SecurityStorage";

    /** The database table name for the security data. */
    public static final String TABLE_NAME = "SECURITY_STORAGE";

    /* The attributes of the security storage. */
    public static final String ATTRIBUTE_GROUP = "TARGET";
    public static final String ATTRIBUTE_BUSINESS_OBJECT = "BUSINESS_OBJECT";
    public static final String ATTRIBUTE_ROLE = "ROLE";
    public static final String ATTRIBUTE_REASON = "REASON";

    /* Properties for cluster manager */
    public static final String PROPERTY_AUTOUPDATE = "sec_store_autoupdate";
    public static final String PROPERTY_REBUILDING = "sec_store_rebuilding";

    /** The default index of the security storage. */
    public static final String DEFAULT_INDEX_NAME = "BORR_INDEX";

    /** The attributes of the security storage. */
    public static final String[] ATTRIBUTES = new String[] { ATTRIBUTE_GROUP, ATTRIBUTE_BUSINESS_OBJECT, ATTRIBUTE_ROLE, ATTRIBUTE_REASON };

    /** Special ID for hasRole association reason. */
	public static final Short REASON_HAS_ROLE = Short.valueOf((short) -1);

	/** Special ID for reason indicating a dirty storage. */
	public static final Short REASON_DIRTY = Short.valueOf((short) -2);

    /** Special ID for unknown reason. */
	public static final Short REASON_UNKNOWN = Short.valueOf(Short.MIN_VALUE);

	private static final Long DIRTY = Long.valueOf(-2);

    /** Special entry to indicate that security storage may be dirty and should be reloaded at next application startup. */
	public static final Object[] DIRTY_FLAG = new Object[] { DIRTY, DIRTY, DIRTY, REASON_DIRTY };

    /** Maximum objects to remove simultaneous within one SQL statement to avoid too large SQL statements. */
    public static final int MAX_LIST_LENGTH = 256;

    /** Saves the executor to execute SQL statements. */
    protected SecurityStorageExecutor executor;

    /**
     * Flag indicating whether to disable database index while reloading or not.
     */
    protected boolean disableIndex = false;

    /**
	 * Switch to enable or disable automatic storage update on relevant changes. This switch should
	 * be left on in normal case, but before great changes (e.g. a data import) this function should
	 * be disabled until the end of the changes. Don't forget to update the storage manually after
	 * the changes by calling the {@link #resetStorage()} method. This flag is turned on by default
	 * in the {@link #startUp(AccessManager)} method.
	 */
	boolean autoUpdate = false;

    /** Flag indicating whether the security storage is currently rebuilding its entries. */
	Boolean rebuildFlag = Boolean.FALSE;

    /**
     * @see #getAccessManager()
     */
	private AccessManager accessManager;

	private final Config _config;

	private final SecurityStorageClusterManagerListener _clusterManagerListener;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SecurityStorage}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * <p>
	 * <b>Call {@link #startUp(AccessManager)} to pass the {@link AccessManager} before using the
	 * {@link SecurityStorage}.</b>
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SecurityStorage(InstantiationContext context, Config config) {
		_config = config;
		executor = createExecutor(context);
		disableIndex = config.getDisableIndex();
		_clusterManagerListener = new SecurityStorageClusterManagerListener(this);
        initClusterManagerProperty();
    }

	private SecurityStorageExecutor createExecutor(InstantiationContext context) {
		try {
			return createExecutor(ConnectionPoolRegistry.getDefaultConnectionPool());
		} catch (SQLException ex) {
			String message = "Failed to initialize storage.";
			context.error(message, new StorageException(message, ex));
			return null;
		}
	}

	private void initClusterManagerProperty() {
		ClusterManager cm = ClusterManager.getInstance();
        if (!cm.isDeclared(PROPERTY_AUTOUPDATE)) {
        	cm.declareValue(PROPERTY_AUTOUPDATE, PropertyType.BOOLEAN);
        }
        if (!cm.isDeclared(PROPERTY_REBUILDING)) {
        	cm.declareValue(PROPERTY_REBUILDING, PropertyType.BOOLEAN);
        }
	}

	@Override
	public Config getConfig() {
		return _config;
	}

    /**
     * Returns the AccessManager which uses this {@link SecurityStorage}.
     */
    protected final AccessManager getAccessManager() {
    	return accessManager;
    }

    /**
     * Creates the SecurityStorageExecutor to use to execute SQL statements. Hook for
     * subclasses to create own SecurityStorageExecutors.
     *
     * @param connectionPool
     *        The connection pool to borrow DB connections from
     * @return the SecurityStorageExecutor to use
     * @throws SQLException
     *         if creation of the executor fails
     */
    protected SecurityStorageExecutor createExecutor(ConnectionPool connectionPool) throws SQLException {
		DBHelper sqlDialect = connectionPool.getSQLDialect();
		if (sqlDialect instanceof MySQLHelper) {
			return new SecurityStorageMySQLExecutor(connectionPool);
		}
		if (sqlDialect instanceof OracleHelper) {
			return new SecurityStorageOracleExecutor(connectionPool);
		}
		if (sqlDialect instanceof DB2Helper) {
			return new SecurityStorageDB2Executor(connectionPool);
		}
		if (sqlDialect instanceof PostgreSQLHelper) {
			return new SecurityStoragePostgreSQLExecutor(connectionPool);
		}
		// Add here more DBSpecific executors and maybe version checks
		return new SecurityStorageExecutor(connectionPool);
    }

    // Query interface:
    // ----------------

    // Requests

    /**
     * Checks, whether the given group has the given role on the given business object as
     * reason of the given reason. The parameters are given within a string array in order
     * as specified in the {@link #ATTRIBUTES} field. All parameters may be
     * <code>null</code> to indicate that a result from at least on of all objects of this
     * type is desired.
     *
     * @param aVector
     *            the parameter array, must be of length 4 or <code>null</code>
     * @return <code>true</code>, if an entry with the given parameters exists in the
     *         storage, <code>false</code> otherwise
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public boolean hasRole(Object[] aVector) throws StorageException {
        try {
            return executor.hasRole(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    /**
     * Checks whether one of the given groups has one of the given roles on one of the given
     * business objects. All parameters must be lists of IDs and must not be
     * <code>null</code>.
     *
     * @param aGroupIDList
     *            the list of group IDs to check for roles
     * @param aBusinessObjectIDList
     *            the list of business object IDs to check for roles
     * @param aRoleIDList
     *            the list of role IDs to check for
     * @return <code>true</code>, if one of the given groups has one of the given roles
     *         on one of the given business objects, <code>false</code> otherwise
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public boolean hasRole(Collection<TLID> aGroupIDList, Collection<TLID> aBusinessObjectIDList,
			Collection<TLID> aRoleIDList) throws StorageException {
        if (CollectionUtil.isEmptyOrNull(aGroupIDList) || CollectionUtil.isEmptyOrNull(aBusinessObjectIDList) || CollectionUtil.isEmptyOrNull(aRoleIDList)) {
            return false;
        }
        try {
            return executor.hasRoleFromCollection(aGroupIDList, aBusinessObjectIDList, aRoleIDList);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    /**
     * Gets all roles the given group has on the given business object for the given reason.
     * All parameters may be <code>null</code> to indicate that a result from all objects
     * of this type is desired. The parameters are given within a string array in order
     * as specified in the {@link #ATTRIBUTES} field. All parameters may be
     * <code>null</code> to indicate that a result from at least on of all objects of this
     * type is desired.
     *
     * @param aVector
     *            the parameter array, must be of length 4 or <code>null</code>
     * @return a (possible empty) list of BoundedRoles, the given group has on the given
     *         business object for the given reason
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public List<BoundedRole> getRoles(Object[] aVector) throws StorageException {
        try {
            return executor.getRoles(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Gets all roles the given groups have on the given business objects. All parameters
     * must be lists of IDs and must not be <code>null</code>.
     *
     * @param aGroupIDList
     *            the list of group IDs to get the roles for
     * @param aBusinessObjectIDList
     *            the list of business object IDs to get the roles for
     * @return a (possible empty) list of BoundedRoles, the given groups have on the given
     *         business objects
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public List<BoundedRole> getRoles(Collection<TLID> aGroupIDList, Collection<TLID> aBusinessObjectIDList)
			throws StorageException {
        if (CollectionUtil.isEmptyOrNull(aGroupIDList) || CollectionUtil.isEmptyOrNull(aBusinessObjectIDList)) {
            return Collections.emptyList();
        }
        try {
            return executor.getRolesFromCollection(aGroupIDList, aBusinessObjectIDList);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    /**
     * Gets all business objects, on which the given group has the given role. The
     * parameters are given within a string array in order as specified in the
     * {@link #ATTRIBUTES} field. All parameters may be <code>null</code> to indicate that a
     * result from at least on of all objects of this type is desired.
     *
     * @param aVector
     *        the parameter array, must be of length 4 or <code>null</code>
     * @return a (possible empty) list of strings (business object IDs), on which the given
     *         group has the given role.
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
	public List<TLID> getBusinessObjects(Object[] aVector) throws StorageException {
        try {
            return executor.getBusinessObjectIds(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Gets all business objects on which one of the given groups have one of the given
     * roles. All parameters must be lists of IDs and must not be <code>null</code>.
     *
     * @param aGroupIDList
     *        the list of group IDs to get the business objects for
     * @param aRoleIDList
     *        the list of role IDs to get the business objects for
     * @return a (possible empty) list of business objects, on which one of the given groups
     *         have one of the given roles
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
	public List<TLID> getBusinessObjectIds(Collection<TLID> aGroupIDList, Collection<TLID> aRoleIDList)
			throws StorageException {
        if (CollectionUtil.isEmptyOrNull(aGroupIDList) || CollectionUtil.isEmptyOrNull(aRoleIDList)) {
			return Collections.emptyList();
        }
        try {
			return executor.getBusinessObjectIdsFromCollection(aGroupIDList, aRoleIDList);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

	/**
	 * Gets all business objects from the given list on which one of the given groups has one of the
	 * given roles. All parameters must be lists of IDs and must not be <code>null</code>.
	 *
	 * @param aGroupIDList
	 *        the list of group IDs to get the business objects for
	 * @param aRoleIDList
	 *        the list of role IDs to get the business objects for
	 * @param aBusinessObjectIDList
	 *        the list of business object IDs to check
	 * @return a (possible empty) sublist of the given business objects list, on which one of the
	 *         given groups have one of the given roles
	 * @throws StorageException
	 *         if some error occurs while requesting the database
	 */
	public List<TLID> getBusinessObjectIds(Collection<TLID> aGroupIDList, Collection<TLID> aRoleIDList,
			List<TLID> aBusinessObjectIDList) throws StorageException {
		if (CollectionUtil.isEmptyOrNull(aGroupIDList) || CollectionUtil.isEmptyOrNull(aRoleIDList)
			|| CollectionUtil.isEmptyOrNull(aBusinessObjectIDList)) {
			return Collections.emptyList();
		}
		try {
			return executor.getBusinessObjectsIdsFromCollection(aGroupIDList, aRoleIDList, aBusinessObjectIDList);
		} catch (SQLException e) {
			throw new StorageException("Error while requesting the database.", e);
		}
	}


    /**
     * Gets all reasons, the given group has the given role on the given business object
     * for. The parameters are given within a string array in order as specified in the
     * {@link #ATTRIBUTES} field. All parameters may be <code>null</code> to indicate that
     * a result from at least on of all objects of this type is desired.
     *
     * @param aVector
     *            the parameter array, must be of length 4 or <code>null</code>
     * @return a (possible empty) list of strings (reason IDs), the given group has the
     *         given role on the given business object for
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public List<Integer> getReasons(Object[] aVector) throws StorageException {
        try {
            return executor.getReasons(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    /**
     * Gets all groups which have the given role on the given business object. The
     * parameters are given within a string array in order as specified in the
     * {@link #ATTRIBUTES} field. All parameters may be <code>null</code> to indicate that
     * a result from at least on of all objects of this type is desired.
     *
     * @param aVector
     *        the parameter array, must be of length 4 or <code>null</code>
     * @return a (possible empty) list of groups which have the given role on
     *         the given business object
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
	public List<Group> getGroups(Object[] aVector) throws StorageException {
        try {
            return executor.getGroups(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Gets all groups which have the given role on the given business object. All
     * parameters may be <code>null</code> to indicate that a result from all objects of
     * this type is desired.
     *
     * @return a (possible empty) list of groups which have the given role on
     *         the given business object
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
    public List<Group> getGroups(BoundObject aBusinessObject, BoundRole aRole) throws StorageException {
		return getGroups(new Object[] { null, aBusinessObject == null ? null : securityId(aBusinessObject),
			aRole == null ? null : securityId(aRole), null });
    }



    // Insert

    /**
     * Inserts multiple entries to the database.
     *
     * @param vectors
     *        a list of vectors as required by the {@link #insert(Object[])} method.
     * @return the amount of entries that have been inserted (= vectors.size() except the
     *         entries which were already in the database)
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
    protected int multiInsert(List<Object[]> vectors) throws StorageException {
        if (vectors == null) {
            throw new IllegalArgumentException("Illegal Arguments: vectors must not be null.");
        }
        if (vectors.isEmpty()) return 0;
        try {
            // Sort vectors and remove duplicates for better insert performance.
            // This requires that the primary key of the database table is
            // TARGET, BUSINESS_OBJECT, ROLE, REASON.
            Collections.sort(vectors, new Comparator<Object[]>() {
                @Override
				public int compare(Object[] vector1, Object[] vector2) {
                    return ArrayUtil.compare(vector1, vector2);
                }
            });
            removeDuplicates(vectors);
            return executor.multiInsertIgnore(vectors);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Removes duplicate entries from the given vector list (sets duplicate values to
     * <code>null</code>). This method requires that the given list is sorted.
     */
    private void removeDuplicates(List<Object[]> vectors) {
    	Object[] last = null;
        for (int i = 0, length = vectors.size(); i < length; i++) {
        	Object[] vector = vectors.get(i);
            if (ArrayUtil.equals(last, vector)) vectors.set(i, null);
            last = vector;
        }
    }

    /**
     * Deletes and inserts multiple entries to the database.
     *
     * @param deleteVectors
     *        a list of vectors as required by the {@link #remove(Object[])} method.
     * @param insertVectors
     *        a list of vectors as required by the {@link #insert(Object[])} method.
     * @return the amount of entries that have been changed (= deleteVectors.size() +
     *         insertVectors.size())
     * @throws StorageException
     *         if some error occurs while requesting the database
     */
    protected int multiDeleteAndInsert(List<Object[]> deleteVectors, List<Object[]> insertVectors) throws StorageException {
        if (deleteVectors == null || insertVectors == null) {
            throw new IllegalArgumentException("Illegal Arguments: vectors must not be null.");
        }
        if (deleteVectors.isEmpty() && insertVectors.isEmpty()) return 0;
        try {
            // Sort vectors and remove duplicates for better insert performance.
            // This requires that the primary key of the database table is
            // TARGET, BUSINESS_OBJECT, ROLE, REASON.
            Collections.sort(deleteVectors, new Comparator<Object[]>() {
                @Override
				public int compare(Object[] vector1, Object[] vector2) {
                    return ArrayUtil.compare(vector1, vector2);
                }
            });
            Collections.sort(insertVectors, new Comparator<Object[]>() {
                @Override
				public int compare(Object[] vector1, Object[] vector2) {
                    return ArrayUtil.compare(vector1, vector2);
                }
            });
            int theChangeCount = 0;
            theChangeCount += executor.multiDelete(deleteVectors);
            theChangeCount += executor.multiInsert(insertVectors);
            return theChangeCount;
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    /**
     * Inserts an entry to the database: The given group has the given role an the given
     * business object for the given reason. Each parameter must NOT be <code>null</code>.
     * The parameters are given within a string array in order as specified in the
     * {@link #ATTRIBUTES} field.
     *
     * @param aVector
     *            the parameter array, must be of length 4.
     * @return <code>true</code> if a entry has been inserted, <code>false</code> if the
     *         entry was already in the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public boolean insert(Object[] aVector) throws StorageException {
        if (aVector == null || aVector.length != 4 || aVector[0] == null || aVector[1] == null || aVector[2] == null || aVector[3] == null) {
            throw new IllegalArgumentException("Illegal Arguments: aVector must be of length 4 and no parameter must be null.");
        }
        try {
            return executor.insertIgnore(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Inserts an entry to the database: The given group has the given role an the given
     * business object for the given reason. Each parameter must NOT be <code>null</code>.
     *
     * @return <code>true</code> if a entry has been inserted, <code>false</code> if the
     *         entry was already in the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public boolean insert(TLID aGroupID, TLID aBusinessObjectID, TLID aRoleID, TLID aRuleID) throws StorageException {
        return insert(new Object[] {aGroupID, aBusinessObjectID, aRoleID, aRuleID});
    }

    /**
     * Inserts an entry to the database: The given group has the given role an the given
     * business object for the given reason. Each parameter must NOT be <code>null</code>.
     *
     * @return <code>true</code> if a entry has been inserted, <code>false</code> if the
     *         entry was already in the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public boolean insert(Group aGroup, Wrapper aBusinessObject, BoundRole aRole, String aRuleID) throws StorageException {
        if (aGroup == null || aBusinessObject == null || aRole == null || aRuleID == null) {
            throw new IllegalArgumentException("Illegal Arguments: No parameter must be null.");
        }
		return insert(new Object[] { securityId(aGroup), objectId(aBusinessObject),
			securityId(aRole), aRuleID });
    }

    /**
     * Inserts an entry from a hasRole association to the database: The given group has the
     * given role an the given business object for the hasRole reason. The parameter must
     * NOT be <code>null</code>.
     *
     * @param aKA
     *            the has role association to insert
     * @return <code>true</code> if a entry has been inserted, <code>false</code> if the
     *         entry was already in the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public boolean insert(KnowledgeAssociation/*<hasRole>*/ aKA) throws StorageException {
        if (aKA == null || !aKA.tTable().getName().equals(BoundedRole.HAS_ROLE_ASSOCIATION)) {
            throw new IllegalArgumentException("Illegal Arguments: The KA must be a KnowledgeAssociation of type " + BoundedRole.HAS_ROLE_ASSOCIATION);
        }
        try {
            KnowledgeObject owner = (KnowledgeObject) aKA.getAttributeValue(BoundedRole.ATTRIBUTE_OWNER);
			return insert(new Object[] {
				objectId(owner),
				objectId(aKA.getSourceObject()),
				objectId(aKA.getDestinationObject()),
				REASON_HAS_ROLE });
        }
        catch (Exception e) {
            throw new StorageException("Can't get necessary informations from the KA.", e);
        }
    }

    // Remove

    /**
     * Removes the entry with the given parameters from the database. Each parameter may be
     * <code>null</code> to indicate that all entries regardless of this parameter shall
     * be removed. The parameters are given within a string array in order as specified in
     * the {@link #ATTRIBUTES} field.
     *
     * @param aVector
     *            the parameter array
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public int remove(Object[] aVector) throws StorageException {
        try {
            return executor.remove(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Removes the entry with the given parameters from the database. The vector must be of
     * length 4, the first parameter must be <code>null</code>, all others must NOT be
     * <code>null</code>. The parameters are given within a string array in order as
     * specified in the {@link #ATTRIBUTES} field. This methods speeds up deleting entries
     * for all users.
     *
     * @param aVector
     *            the parameter array
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public int removeForAllTargets(Object[] aVector) throws StorageException {
        if (aVector == null || aVector[0] != null || aVector[1] == null || aVector[2] == null || aVector[3] == null) {
            Logger.error("Illegal Arguments: aVector must be of length 4, the first parameter must be null, all others must NOT be null.", SecurityStorage.class);
            return 0;
            //throw new IllegalArgumentException("Illegal Arguments: aVector must be of length 4, the first parameter must be null, all others must NOT be null.");
        }
        try {
            return executor.removeForAllTargets(aVector);
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }

    /**
     * Removes the entry with the given parameters from the database. Each parameter may be
     * <code>null</code> to indicate that all entries regardless of this parameter shall
     * be removed.
     *
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public int remove(TLID aGroupID, TLID aBusinessObjectID, TLID aRoleID, TLID aRuleID) throws StorageException {
        return remove(new Object[] {aGroupID, aBusinessObjectID, aRoleID, aRuleID});
    }

    /**
     * Removes the entry with the given parameters from the database. Each parameter may be
     * <code>null</code> to indicate that all entries regardless of this parameter shall
     * be removed.
     *
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public int remove(Group aGroup, Wrapper aBusinessObject, BoundRole aRole, String aRuleID) throws StorageException {
		return remove(new Object[] { aGroup == null ? null : securityId(aGroup),
			aBusinessObject == null ? null : objectId(aBusinessObject),
			aRole == null ? null : securityId(aRole), aRuleID });
    }

    /**
     * Removes an entry from a hasRole association from the database. The parameter must NOT
     * be <code>null</code>.
     *
     * @param aKA
     *            the has role association to remove
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public int remove(KnowledgeAssociation/*<hasRole>*/ aKA) throws StorageException {
        if (aKA == null || !aKA.tTable().getName().equals(BoundedRole.HAS_ROLE_ASSOCIATION)) {
            throw new IllegalArgumentException("Illegal Arguments: The KA must be a KnowledgeAssociation of type " + BoundedRole.HAS_ROLE_ASSOCIATION);
        }
        try {
			return remove(new Object[] {
				objectId(((KnowledgeItem) aKA.getAttributeValue(BoundedRole.ATTRIBUTE_OWNER))),
				objectId(aKA.getSourceObject()),
				objectId(aKA.getDestinationObject()),
				REASON_HAS_ROLE });
        }
        catch (Exception e) {
            throw new StorageException("Can't get necessary informations from the KA.", e);
        }
    }

    /**
     * Removes all entries with all business objects in the given collection.
     *
     * @param removedObjects
     *            a list of business objects to remove
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	public int removeObjects(Map<TLID, Object> removedObjects) throws StorageException {
        if (CollectionUtil.isEmptyOrNull(removedObjects)) return 0;
        int result = removeObjects(CollectionUtil.toList(removedObjects.keySet()));

        try {
			Object[] vector = new Object[4];
			for (Map.Entry<TLID, Object> entry : removedObjects.entrySet()) {
                Object value = entry.getValue();
				if (value instanceof KnowledgeObject
						&& ((KnowledgeObject) value).tTable().getName().equals(Group.OBJECT_NAME)) {
                    vector[0] = entry.getKey();
                    result += executor.remove(vector);
                }
            }
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
        return result;
    }

    /**
     * Removes all entries with all business objects in the given collection. The parameter
     * must NOT be <code>null</code> and must contain only business object IDs.
     *
     * @param aBusinessObjectIDList
     *            a list of business object IDs to remove
     * @return the amount of entries that have been deleted from the database
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
	private int removeObjects(List<TLID> aBusinessObjectIDList) throws StorageException {
        // If the list is too big, it will be split into lists with a maximum length
        // of MAX_LIST_LENGTH to avoid too large SQL statements.
        int size = aBusinessObjectIDList.size();

        try {
            int  result = 0;
            long start  = System.currentTimeMillis();
            if (size > MAX_LIST_LENGTH) {
                // Help the DB by pre-sorting the IDs for the chunks
				Collections.sort(aBusinessObjectIDList);
                for (int i = 0, next; i < size; i = next) {
                    next = i + MAX_LIST_LENGTH;
                    if (next > size) {
                        next = size;
                    }
                    result += executor.removeObjects(aBusinessObjectIDList.subList(i, next));
                }
            }
            else {
                result = executor.removeObjects(aBusinessObjectIDList);
            }
            long delta = System.currentTimeMillis() - start;
            if (delta > 1000) {
                Logger.warn("Tried to remove " + size + " objects from storage, actually removed " + result + " lines in " + DebugHelper.getTime(delta),
                    new Exception("Stacktrace for removeObjects"), SecurityStorage.class);
            }
            return result;
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }



    // Update

    /**
	 * Activates the security storage (starts the auto update function).
	 * 
	 * @see #shutDown()
	 */
	public void startUp(AccessManager accessManager) {
		this.accessManager = accessManager;
        ClusterManager cm = ClusterManager.getInstance();
		cm.addClusterMessageListener(_clusterManagerListener);

        Boolean value = cm.getLatestUnconfirmedValue(PROPERTY_AUTOUPDATE);
        if (value == null) {
            autoUpdate = true;
            cm.setValue(PROPERTY_AUTOUPDATE, Boolean.TRUE);
        }
        else {
            autoUpdate = value.booleanValue();
        }

        try {
            value = cm.getConfirmedValue(PROPERTY_REBUILDING);
            if (value == null) {
                rebuildFlag = Boolean.FALSE;
                cm.setValue(PROPERTY_REBUILDING, Boolean.FALSE);
            }
            else {
                rebuildFlag = value;
            }
        }
        catch (PendingChangeException e) {
            rebuildFlag = null;
        }
    }

	/**
	 * Shuts the {@link SecurityStorage} down.
	 * <p>
	 * Has to be called when this instance is no longer needed.
	 * </p>
	 * 
	 * @see #startUp(AccessManager)
	 */
	public void shutDown() {
		ClusterManager.getInstance().removeClusterMessageListener(_clusterManagerListener);
	}

    /**
     * Checks whether the security storage is empty (has no security entries).
     *
     * @return <code>true</code>, if the security storage has no security entries,
     *         <code>false</code> otherwise
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public boolean isEmpty() throws StorageException {
        try {
            return executor.isEmpty();
        }
        catch (SQLException e) {
            throw new StorageException("Error while requesting the database.", e);
        }
    }


    /**
     * Checks whether the security storage may be dirty and should be reloaded.
     *
     * @return <code>true</code>, if the security storage may be dirty (is empty or has
     *         the dirty flag set), <code>false</code> otherwise
     */
    public boolean isDirty() {
        try {
            return isEmpty() || hasRole(DIRTY_FLAG);
        }
        catch (Exception e) {
            Logger.warn("Failed to read dirty flag from security storage.", e, SecurityStorage.class);
            return true;
        }
    }


    /**
     * Sets the dirty state of the security storage which signals that the security storage
     * may be invalid and should be reloaded.
     *
     * @param dirty
     *        set (<code>true</code>) or remove (<code>false</code>) the dirty flag
     */
    protected void setDirty(boolean dirty) {
        SimpleCommitHandler committer = new SimpleCommitHandler(ConnectionPoolRegistry.getDefaultConnectionPool());
        committer.begin();
        try {
            begin(committer);
            if (dirty) {
                insert(DIRTY_FLAG);
            }
            else {
                remove(DIRTY_FLAG);
            }
            if (!committer.commit()) {
                Logger.warn("Failed to insert dirty flag into security storage.", SecurityStorage.class);
            }
        }
        catch (Exception e) {
            Logger.warn("Failed to insert dirty flag into security storage.", e, SecurityStorage.class);
            try {
                committer.rollback();
            }
            catch (SQLException ex) {
                Logger.error("Failed to rollback.", ex, SecurityStorage.class);
            }
        }
        finally {
            committer.close();
        }
    }


    /**
     * Clears all entries in this storage.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    protected void clearStorage() throws StorageException {
        try {
            executor.clearStorage();
        }
        catch (SQLException e) {
            throw new StorageException("Error while clearing the database.", e);
        }
    }



    /**
     * Clears and refreshes this storage by recomputing all roles from scratch.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    private void resetStorage() throws StorageException {
		StopWatch sw = StopWatch.createStartedWatch();
        Logger.info("Rebuilding security storage...", SecurityStorage.class);
        SimpleCommitHandler committer = new SimpleCommitHandler(ConnectionPoolRegistry.getDefaultConnectionPool());
        try {
            Logger.info("Clearing storage...", SecurityStorage.class);
            committer.begin();
            begin(committer);
            clearStorage();
            committer.commit();
            committer.close();

            Logger.info("Rebuilding storage...", SecurityStorage.class);
            setDirty(true);
            committer.begin();
            begin(committer);
            computeRoles();
            committer.commit();
            committer.close();

            Logger.info("Optimizing storage...", SecurityStorage.class);
            optimize();
            setDirty(false);
            if (!autoUpdate && !isEmpty()) setDirty(true);
        }
        catch (SQLException e) {
			Logger.error("Failed to commit changes after " + sw, e, SecurityStorage.class);
            throw new StorageException("Failed to commit changes", e);
        }
        catch (StorageException e) {
			Logger.error("Failed to rebuilt security storage after " + sw, e, SecurityStorage.class);
            throw e;
        }
        finally {
            committer.close();
        }
		Logger.info("Security storage was rebuilt successfully in " + sw, SecurityStorage.class);
    }



    /**
     * Creates the database entries with hasRole reason. Subclasses may extend this method
     * to create further entries. This method requires that the table is empty.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    private void computeRoles() throws StorageException {
        if (disableIndex) {
            Logger.info("Disabling database index...", SecurityStorage.class);
            try {
                executor.disableIndex();
            }
            catch (SQLException e) {
                throw new StorageException(e);
            }
        }
        try {
            Logger.info("Computing entries...", SecurityStorage.class);
            doComputeRoles();
            Logger.info("Computing of entries complete.", SecurityStorage.class);
        }
        finally {
            if (disableIndex) {
                Logger.info("Reenabling database index...", SecurityStorage.class);
                try {
                    executor.enableIndex();
                }
                catch (SQLException e) {
                    Logger.error("Failed to recreate database index.", e, SecurityStorage.class);
                }
            }
        }
    }



    /**
     * Creates the database entries with hasRole reason. Subclasses may extend this method
     * to create further entries. This method requires that the table is empty.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    protected void doComputeRoles() throws StorageException {
        Logger.info("Computing direct roles...", SecurityStorage.class);
		StopWatch sw = StopWatch.createStartedWatch();
        int counterEntries = 0;
        KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
		for (KnowledgeAssociation association : theKB.getAllKnowledgeAssociations(BoundedRole.HAS_ROLE_ASSOCIATION)) {
			if (insert(association))
				counterEntries++;

		}
		Logger.info("Done in " + sw + ". Computed entries: " + counterEntries + ".", SecurityStorage.class);
    }



    /**
     * Updates the security storage, if {@link #isAutoUpdate()} is enabled.
     *
     * @param aChangesInformation
     *            an Object containing all change informations needed for updates
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    public final void updateSecurity(Object aChangesInformation) throws StorageException {
        if (autoUpdate) internalUpdateSecurity(aChangesInformation);
    }

    /**
     * Updates the security storage. This is a hook for subclasses which may extend this
     * method to make updates.
     *
     * @param aChangesInformation
     *            an Object containing all change informations needed by subclasses for
     *            updates
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    protected void internalUpdateSecurity(Object aChangesInformation) throws StorageException {
        // Nothing to do here
    }



    /**
     * Analyzes / optimizes the table for faster access.
     *
     * @throws StorageException
     *             if some error occurs while requesting the database
     */
    protected void optimize() throws StorageException {
        try {
            executor.optimize();
        }
        catch (SQLException e) {
            throw new StorageException("Unable to analyze / optimize the database.", e);
        }
    }



    /**
     * Checks whether the security storage is currently rebuilding its entries.
     * If this method returns <code>true</code>, this implies that the storage is not
     * valid at this time and may return less roles than expected (never more!).
     *
     * @return <code>true</code>, if the security storage is currently rebuilding its
     *         entries, false otherwise
     */
    public boolean isRebuilding() {
        // pending change will be answered with true
        return rebuildFlag == null ? true : rebuildFlag.booleanValue();
    }

    /**
     * Gets the enabled state of the autoUpdate function.
     *
     * @return the enabled state of the autoUpdate function
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Switches the autoUpdate function on or off.
     *
     * @param autoUpdate
     *            the new state of the autoUpdate function.
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        ClusterManager.getInstance().setValue(PROPERTY_AUTOUPDATE, Boolean.valueOf(autoUpdate));
        if (!autoUpdate) setDirty(true);
    }



    /**
     * This method must be called before calling any write methods.
     *
     * @param aHandler
     *            the commit handler to get committed by
     * @throws IllegalArgumentException
     *             when CommitHandler does not use a {@link PooledConnection} or called
     *             twice with different handler before commit()/rollback()
     */
    public void begin(CommitHandler aHandler) {
        executor.begin(aHandler);
    }

    @Override
	public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
	public String getDescription() {
        return "Stores all roles in the application";
    }

    @Override
	public boolean usesXMLProperties() {
        return false;
    }

	/**
	 * Whether the current interaction is the trigger of {@link #reload()}.
	 */
	public static boolean isCurrentReloadingThread() {
		return ThreadContextManager.getInteraction().get(RELOADING_THREAD);
	}

    /**
     * Resets the storage and computes all roles from scratch.
     */
    @Override
	public boolean reload() {
        ClusterManager cm = ClusterManager.getInstance();
        cm.refetch();
        if (isRebuilding()) {
            Logger.warn("Cannot rebuild security storage because the storage is already rebuilding.", SecurityStorage.class);
            return false;
        }
        try {
            try {
                cm.setValueIfUnchanged(PROPERTY_REBUILDING, Boolean.TRUE);
            }
            catch (PendingChangeException e) {
                Logger.warn("Cannot rebuild security storage because the storage is already rebuilding.", SecurityStorage.class);
                return false;
            }
            if (cm.isClusterMode()) Logger.info("Waiting for response from other cluster nodes...", SecurityStorage.class);
            cm.waitForConfirmation(PROPERTY_REBUILDING);
			InteractionContext interaction = ThreadContextManager.getInteraction();
			interaction.set(RELOADING_THREAD, true);
			try {
				resetStorage();
			} finally {
				interaction.reset(RELOADING_THREAD);
			}
            return true;
        }
        catch (Exception e) {
            Logger.error("Failed to reload security storage.", e, SecurityStorage.class);
            return false;
        }
        finally {
            cm.setValue(PROPERTY_REBUILDING, Boolean.FALSE);
        }
    }



    /**
     * The SecurityStorageExecutor executes SQL statements on the database. This class uses no
     * database specific performance tunings.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class SecurityStorageExecutor extends ThreadLocalCommitable {

        /**
         * Creates a new SecurityStorageDBExecutor. Used internally only.
         *
         * @param connectionPool
         *            the prepared statement cache to use for read operations
         * @throws SQLException
         *             if the initialization of the storage fails
         */
        public SecurityStorageExecutor(ConnectionPool connectionPool) throws SQLException {
            super(connectionPool);
            
			String securityStorageTableRef = dbHelper.tableRef(SecurityStorage.TABLE_NAME);
			String attributeGroupColumnRef = dbHelper.columnRef(SecurityStorage.ATTRIBUTE_GROUP);
			String attributeRoleColumnRef = dbHelper.columnRef(SecurityStorage.ATTRIBUTE_ROLE);
			String attributeBusinessObjectColumnRef = dbHelper.columnRef(SecurityStorage.ATTRIBUTE_BUSINESS_OBJECT);
			String attributeReasonColumnRef = dbHelper.columnRef(SecurityStorage.ATTRIBUTE_REASON);

			SQL_HAS_ROLE_INFIX = "1 FROM " + securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_REMOVE_PREFIX = "DELETE FROM " + securityStorageTableRef + updateHint();
			SQL_REMOVE_STATEMENT = SQL_REMOVE_PREFIX + " WHERE " + attributeGroupColumnRef + "=? AND "
				+ attributeBusinessObjectColumnRef + "=? AND " + attributeRoleColumnRef + "=? AND "
				+ attributeReasonColumnRef + "=?";
			SQL_REMOVE_OBJECTS_PART_1 =
				SQL_REMOVE_PREFIX + " WHERE " + attributeBusinessObjectColumnRef + " IN ";
			SQL_REMOVE_FOR_ALL_TARGETS_STATEMENT =
				SQL_REMOVE_PREFIX + " WHERE " + attributeBusinessObjectColumnRef + "=? AND "
					+ attributeRoleColumnRef + "=? AND " + attributeReasonColumnRef + "=?";
			SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_1 =
				SQL_HAS_ROLE_INFIX + " WHERE " + attributeGroupColumnRef + " IN ";
			SQL_INSERT_STATEMENT = "INSERT INTO " + securityStorageTableRef + updateHint() + " VALUES (?, ?, ?, ?)";
			SQL_GET_ROLES_PREFIX = SQL_SELECT_PREFIX + "DISTINCT " + attributeRoleColumnRef + " FROM "
				+ securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_GET_REASONS_PREFIX = SQL_SELECT_PREFIX + "DISTINCT " + attributeReasonColumnRef + " FROM "
				+ securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_GET_BUSINESS_OBJECTS_PREFIX =
				SQL_SELECT_PREFIX + "DISTINCT " + attributeBusinessObjectColumnRef + " FROM "
					+ securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_GET_GROUPS_PREFIX = SQL_SELECT_PREFIX + "DISTINCT " + attributeGroupColumnRef + " FROM "
				+ securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_GET_GROUPS_BY_OBJECTS_PREFIX =
				"SELECT DISTINCT " + attributeBusinessObjectColumnRef + "," + attributeGroupColumnRef
					+ " FROM " + securityStorageTableRef + dbHelper.selectNoBlockHint();
			SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_1 =
				SQL_GET_ROLES_PREFIX + " WHERE " + attributeGroupColumnRef + " IN ";
			SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_1 =
				SQL_GET_BUSINESS_OBJECTS_PREFIX + " WHERE " + attributeGroupColumnRef + " IN ";
			SQL_CREATE_DEFAULT_INDEX =
				"CREATE INDEX " + dbHelper.columnRef(SecurityStorage.DEFAULT_INDEX_NAME) + " ON "
					+ securityStorageTableRef +
					" (" + attributeBusinessObjectColumnRef + "," + attributeRoleColumnRef + ","
					+ attributeReasonColumnRef + ")";
			SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_3 =
				" AND " + attributeBusinessObjectColumnRef + " IN ";
			SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_2 =
				" AND " + attributeRoleColumnRef + " IN ";
			SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_2 =
				" AND " + attributeBusinessObjectColumnRef + " IN ";
			SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_3 =
				" AND " + attributeRoleColumnRef + " IN ";
			SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_2 =
				" AND " + attributeBusinessObjectColumnRef + " IN ";
        }

		/**
		 * Creates the WHERE part of a SQL statement. All parameters except aBuffer may be
		 * <code>null</code> to indicate that a result of all objects of this type is desired.
		 * 
		 * @param aBuffer
		 *        the StringBuilder to write the WHERE part into; must not be <code>null</code>
		 * @param aVector
		 *        the parameter vector, must be of length 4 or <code>null</code>
		 */
		protected void appendWhereStatement(StringBuilder aBuffer, Object[] aVector) {
            if (aVector == null) return;
            int i = 0;
            if (aVector[0] != null) {
                aBuffer.append(i++ == 0 ? " WHERE " : " AND ");
				aBuffer.append(dbHelper.columnRef(ATTRIBUTE_GROUP)).append("=?");
            }
            if (aVector[1] != null) {
                aBuffer.append(i++ == 0 ? " WHERE " : " AND ");
				aBuffer.append(dbHelper.columnRef(ATTRIBUTE_BUSINESS_OBJECT)).append("=?");
            }
            if (aVector[2] != null) {
                aBuffer.append(i++ == 0 ? " WHERE " : " AND ");
				aBuffer.append(dbHelper.columnRef(ATTRIBUTE_ROLE)).append("=?");
            }
            if (aVector[3] != null) {
                aBuffer.append(i++ == 0 ? " WHERE " : " AND ");
				aBuffer.append(dbHelper.columnRef(ATTRIBUTE_REASON)).append("=?");
            }
        }

        /**
         * Checks the parameter array for valid values.
         *
         * @param aVector
         *            the parameter array to check
         * @throws SQLException
         *             if a parameter is not valid
         */
		protected void checkVector(Object[] aVector) throws SQLException {
            if (aVector == null) return;
            if (aVector.length != 4) {
                throw new SQLException("Illegal argument: The parameters vector must be null or of length 4.");
            }
        }

        /**
         * Checks the parameter array for valid values.
         *
         * @param aVector
         *            the parameter array to check
         * @throws SQLException
         *             if a parameter is not valid
         */
        protected void checkVectorNotNull(Object[] aVector) throws SQLException {
            if (aVector == null) return;
            if (aVector.length != 4) {
                throw new SQLException("Illegal argument: The parameters vector must be null or of length 4.");
            }
            for (int i = 0; i < aVector.length; i++) {
                if (aVector[i] == null) {
                    throw new SQLException("Illegal argument: The parameters must not be null.");
                }
            }
        }


        /** Maximum length of a string of the parameters. */
        public static final int MAX_LENGTH = 70;

        /* The SQL statements used by this class. */
        protected final String SQL_SELECT_PREFIX = "SELECT ";

        protected final String SQL_HAS_ROLE_INFIX;

        protected final String SQL_GET_ROLES_PREFIX;

        protected final String SQL_GET_REASONS_PREFIX;

        protected final String SQL_GET_BUSINESS_OBJECTS_PREFIX;

        protected final String SQL_GET_GROUPS_PREFIX;

        protected final String SQL_GET_GROUPS_BY_OBJECTS_PREFIX;

        protected final String SQL_REMOVE_PREFIX;

		/**
		 * SQL statement to insert rows into {@link SecurityStorage#TABLE_NAME}.
		 * 
		 * @see #SQL_INSERT_STATEMENT_PARAMETERS
		 */
        protected final String SQL_INSERT_STATEMENT;

		/**
		 * Number of parameters in {@link #SQL_INSERT_STATEMENT}.
		 * 
		 * @see #SQL_INSERT_STATEMENT
		 */
		protected final int SQL_INSERT_STATEMENT_PARAMETERS = 4;

		/**
		 * SQL statement to delete rows from{@link SecurityStorage#TABLE_NAME}.
		 * 
		 * @see #SQL_REMOVE_STATEMENT_PARAMETERS
		 */
        protected final String SQL_REMOVE_STATEMENT;
		/**
		 * Number of parameters in {@link #SQL_REMOVE_STATEMENT}.
		 * 
		 * @see #SQL_REMOVE_STATEMENT
		 */
        protected final int SQL_REMOVE_STATEMENT_PARAMETERS = 4;

        protected final String SQL_REMOVE_FOR_ALL_TARGETS_STATEMENT;

        protected final String SQL_REMOVE_OBJECTS_PART_1;

        protected final String SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_1;

        protected final String SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_2;

        protected final String SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_3;

        protected final String SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_1;

        protected final String SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_2;

        protected final String SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_1;

        protected final String SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_2;

        protected final String SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_3;

        protected final String SQL_CREATE_DEFAULT_INDEX;

        protected String updateHint() {
            return "";
        }

		public boolean hasRole(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
			dbHelper.limitStart(sb, 1);
            sb.append(SQL_SELECT_PREFIX);
            dbHelper.limitColumns(sb, 1);
            sb.append(SQL_HAS_ROLE_INFIX);
            appendWhereStatement(sb, aVector);
            dbHelper.limitLast(sb, 1);
            return DBUtil.executeQueryAsBoolean(sb.toString(), nonNullStorageValues(aVector));
        }

		public List<BoundedRole> getRoles(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_ROLES_PREFIX);
            appendWhereStatement(sb, aVector);
			return DBUtil.executeQueryAsWrapperList(sb.toString(), nonNullStorageValues(aVector), BoundedRole.OBJECT_NAME,
				BoundedRole.class);
        }

		public boolean hasRoleFromCollection(Collection<TLID> aGroupIDList, Collection<TLID> aBusinessObjectIDList,
				Collection<TLID> aRoleIDList) throws SQLException {
			StringBuilder sb = new StringBuilder(1024);
			dbHelper.limitStart(sb, 1);
            sb.append(SQL_SELECT_PREFIX);
            dbHelper.limitColumns(sb, 1);
            sb.append(SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_1);
			dbHelper.literalSet(sb, DBType.ID, aGroupIDList);
            sb.append(SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_2);
			dbHelper.literalSet(sb, DBType.ID, aBusinessObjectIDList);
            sb.append(SQL_HAS_ROLE_FROM_COLLECTION_STATEMENT_PART_3);
			dbHelper.literalSet(sb, DBType.ID, aRoleIDList);
            dbHelper.limitLast(sb, 1);
            return DBUtil.executeQueryAsBoolean(sb.toString());
        }

		public List<BoundedRole> getRolesFromCollection(Collection<TLID> aGroupIDList,
				Collection<TLID> aBusinessObjectIDList) throws SQLException {
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_1);
			dbHelper.literalSet(sb, DBType.ID, aGroupIDList);
            sb.append(SQL_GET_ROLES_FROM_COLLECTION_STATEMENT_PART_2);
			dbHelper.literalSet(sb, DBType.ID, aBusinessObjectIDList);
			return DBUtil.executeQueryAsWrapperList(sb.toString(), BoundedRole.OBJECT_NAME, BoundedRole.class);
        }

		public List<TLID> getBusinessObjectIdsFromCollection(Collection<TLID> aGroupIDList,
				Collection<TLID> aRoleIDList)
				throws SQLException {
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_1);
			dbHelper.literalSet(sb, DBType.ID, aGroupIDList);
            sb.append(SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_2);
			dbHelper.literalSet(sb, DBType.ID, aRoleIDList);
			return DBUtil.executeQueryAsIdList(sb.toString());
        }

		public List<TLID> getBusinessObjectsIdsFromCollection(Collection<TLID> aGroupIDList,
				Collection<TLID> aRoleIDList, List<TLID> aBusinessObjectIDList) throws SQLException {
			List<TLID> result = null;
			int inSize = dbHelper.getMaxSetSize();
            int low  = 0;
            int high = inSize;
            int size = aBusinessObjectIDList.size();
			while (low < size) {
            	if (high > size) high = size;
            	StringBuffer sb = new StringBuffer(4096);
            	sb.append(SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_1);
				dbHelper.literalSet(sb, DBType.ID, aGroupIDList);
            	sb.append(SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_2);
				dbHelper.literalSet(sb, DBType.ID, aRoleIDList);
            	sb.append(SQL_GET_BOS_FROM_COLLECTION_STATEMENT_PART_3);
				dbHelper.literalSet(sb, DBType.ID, aBusinessObjectIDList.subList(low, high));
				List<TLID> partResult = DBUtil.executeQueryAsIdList(sb.toString());
            	if (result == null) {
            		result = partResult;
				} else {
            		result.addAll(partResult);
            	}
            	low = high;
            	high += inSize;
			}
			return result == null ? Collections.<TLID> emptyList() : result;
        }

		public List<Integer> getReasons(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_REASONS_PREFIX);
            appendWhereStatement(sb, aVector);
			return DBUtil.executeQueryAsIntList(sb.toString(), nonNullStorageValues(aVector));
        }

		public List<TLID> getBusinessObjectIds(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_BUSINESS_OBJECTS_PREFIX);
            appendWhereStatement(sb, aVector);
			return DBUtil.executeQueryAsIdList(sb.toString(), nonNullStorageValues(aVector));
        }

		public List<Group> getGroups(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_GET_GROUPS_PREFIX);
            appendWhereStatement(sb, aVector);
			return DBUtil.executeQueryAsWrapperList(sb.toString(), nonNullStorageValues(aVector), Group.OBJECT_NAME,
				Group.class);
        }

        /**
         * This method provides an optimized {@link #getGroups(Object[])} for multiple objects
         * 
         * @param aVector      the partial vector to be extended by the given objects (the vector must contain already the role to request the groups) 
         * @param someObjects  the objects to request the groups
         * @return a {@link List} of {@link Group} for each {@link BoundObject}
         */
		public Map<TLID, List<Group>> getGroupsByObject(Object[] aVector, List<? extends BoundObject> someObjects)
				throws SQLException {
            checkVector(aVector);
            Map<TLID, List<Group>> theResult = new HashMap<>();
			int inSize = dbHelper.getMaxSetSize();
            int low  = 0;
            int high = inSize;
            int size = someObjects.size();
			while (low < size) {
            	if (high > size) high = size;
				StringBuilder sb = new StringBuilder(4096);
            	sb.append(SQL_GET_GROUPS_BY_OBJECTS_PREFIX);
            	appendWhereStatement(sb, aVector);
            	sb.append(" AND ");
				sb.append(dbHelper.columnRef(ATTRIBUTE_BUSINESS_OBJECT)).append(" in (");
            	boolean isInner = false;
            	for (Iterator<? extends BoundObject> theIt = someObjects.subList(low, high).iterator(); theIt.hasNext();) {
            		if (isInner) sb.append(','); else isInner = true;
					dbHelper.literal(sb, DBType.ID, theIt.next().getID());
            	}
            	sb.append(')');
				theResult.putAll(
					DBUtil.executeQuery(getWriteConnection(), sb.toString(), nonNullStorageValues(aVector),
					new ResultAsMapFromStringToWrapperList<>(Group.OBJECT_NAME, Group.class)));
            	low  += inSize;
            	high += inSize;
            }
			return theResult;
        }

		/**
		 * Inserts given vector and ignores duplicate.
		 * 
		 * @see #multiInsertIgnore(List)
		 */
        public boolean insertIgnore(Object[] aVector) throws SQLException {
            checkVectorNotNull(aVector);
            Connection writeConnection = getWriteConnection();
			Object[] storageValues = storageValues(aVector);
			DBUtil.executeUpdate(writeConnection, SQL_REMOVE_STATEMENT, storageValues);
			return DBUtil.executeUpdate(writeConnection, SQL_INSERT_STATEMENT, storageValues) > 0;
        }

		/**
		 * Inserts all vectors and ignores duplicates.
		 * 
		 * @see #insertIgnore(Object[])
		 */
        public int multiInsertIgnore(List<Object[]> vectors) throws SQLException {
        	Connection writeConnection = getWriteCache().getConnection();
			int maxBatchLength =
				dbHelper.getMaxBatchSize(Math.min(SQL_INSERT_STATEMENT_PARAMETERS, SQL_REMOVE_STATEMENT_PARAMETERS));
			try (PreparedStatement pstmDel = writeConnection.prepareStatement(SQL_REMOVE_STATEMENT);
					PreparedStatement pstmIns = writeConnection.prepareStatement(SQL_INSERT_STATEMENT)) {
                int result = 0, counter = 0;
                for (Object[] vector : vectors) {
                    if (vector == null) continue;
                    checkVectorNotNull(vector);
					Object[] storageValues = storageValues(vector);
					DBUtil.setVector(pstmDel, storageValues);
                    pstmDel.addBatch();
					DBUtil.setVector(pstmIns, storageValues);
                    pstmIns.addBatch();
                    counter++;
					if (counter >= maxBatchLength) {
                        pstmDel.executeBatch();
                        result += ArrayUtil.sum(pstmIns.executeBatch());
                        counter = 0;
                    }
                }
                if (counter > 0) {
                    pstmDel.executeBatch();
                    result += ArrayUtil.sum(pstmIns.executeBatch());
                }
                return result;
            }
        }

        public int multiInsert(List<Object[]> vectors) throws SQLException {
        	Connection writeConnection = getWriteCache().getConnection();
			try (PreparedStatement pstm = writeConnection.prepareStatement(SQL_INSERT_STATEMENT)) {
				int maxBatchLength = dbHelper.getMaxBatchSize(SQL_INSERT_STATEMENT_PARAMETERS);
                int result = 0, counter = 0;
                for (Object[] vector : vectors) {
                    if (vector == null) continue;
                    checkVectorNotNull(vector);
					DBUtil.setVector(pstm, storageValues(vector));
                    pstm.addBatch();
                    counter++;
					if (counter >= maxBatchLength) {
                        result += ArrayUtil.sum(pstm.executeBatch());
                        counter = 0;
                    }
                }
                if (counter > 0) {
                    result += ArrayUtil.sum(pstm.executeBatch());
                }
                return result;
            }
        }

        public int multiDelete(List<Object[]> vectors) throws SQLException {
            Connection writeConnection = getWriteCache().getConnection();
			try (PreparedStatement pstm = writeConnection.prepareStatement(SQL_REMOVE_STATEMENT)) {
				int maxBatchLength = dbHelper.getMaxBatchSize(SQL_REMOVE_STATEMENT_PARAMETERS);
                int result = 0, counter = 0;
                for (Object[] vector : vectors) {
                    if (vector == null) continue;
                    checkVectorNotNull(vector);
					DBUtil.setVector(pstm, storageValues(vector));
                    pstm.addBatch();
                    counter++;
					if (counter >= maxBatchLength) {
                        result += ArrayUtil.sum(pstm.executeBatch());
                        counter = 0;
                    }
                }
                if (counter > 0) {
                    result += ArrayUtil.sum(pstm.executeBatch());
                }
                return result;
            }
        }

        public Collection<DBIndex> disableIndex() throws SQLException {
            Connection writeConnection = getWriteConnection();
            DBTable table = new SchemaExtraction(writeConnection.getMetaData(), dbHelper).addTable(DBSchemaFactory.createDBSchema(), SecurityStorage.TABLE_NAME);
            Collection<DBIndex> indices = table.getIndices();
            for (DBIndex index : indices) {
				String dropSQL = dbHelper.dropIndex(index.getDBName(), SecurityStorage.TABLE_NAME);
				DBUtil.executeUpdate(writeConnection, dropSQL);
            }
            return indices;
        }

        public void enableIndex() throws SQLException {
            DBUtil.executeUpdate(getWriteConnection(), SQL_CREATE_DEFAULT_INDEX);
        }

		public int remove(Object[] aVector) throws SQLException {
            checkVector(aVector);
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_REMOVE_PREFIX);
            appendWhereStatement(sb, aVector);
            return DBUtil.executeUpdate(getWriteConnection(), sb.toString(), nonNullStorageValues(aVector));
        }

		public int removeForAllTargets(Object[] aVector) throws SQLException {
            checkVector(aVector);
            return DBUtil.executeUpdate(getWriteConnection(), SQL_REMOVE_FOR_ALL_TARGETS_STATEMENT, nonNullStorageValues(aVector));
        }

		public int removeObjects(Collection<TLID> aBusinessObjectIDList) throws SQLException {
			StringBuilder sb = new StringBuilder(1024);
            sb.append(SQL_REMOVE_OBJECTS_PART_1);
			dbHelper.literalSet(sb, DBType.ID, aBusinessObjectIDList);
            return DBUtil.executeUpdate(getWriteConnection(), sb.toString());
        }

        public boolean isEmpty() throws SQLException {
			StringBuilder sb = new StringBuilder(1024);
			dbHelper.limitStart(sb, 1);
            sb.append(SQL_SELECT_PREFIX);
            dbHelper.limitColumns(sb, 1);
            sb.append(SQL_HAS_ROLE_INFIX);
            dbHelper.limitLast(sb, 1);
            return !DBUtil.executeQueryAsBoolean(sb.toString());
        }

        public void clearStorage() throws SQLException {
            DBUtil.executeUpdate(getWriteConnection(), SQL_REMOVE_PREFIX);
        }

        public void optimize() throws SQLException {
            PooledConnection statementCache = this.borrowWriteConnection();
            try {
				try (Statement stm = statementCache.createStatement()) {
                    dbHelper.analyzeTable(stm, SecurityStorage.TABLE_NAME);
                }
            } finally {
                this.releaseWriteConnection(statementCache);
            }
        }

		/**
		 * Converts {@link TLID} objects in the given vector to their storage values.
		 */
		protected static Object[] storageValues(Object[] aVector) {
			if (aVector == null) {
				return null;
			}

			Object[] result = new Object[aVector.length];
			for (int i = 0; i < aVector.length; i++) {
				Object value = aVector[i];
				result[i] = storageValue(value);
			}
			return result;
		}

    }

	protected static Object securityId(BoundObject obj) {
		return storageValue(obj.getID());
	}

	protected static Object securityId(BoundRole obj) {
		return storageValue(obj.getID());
	}

	protected static Object objectId(TLObject obj) {
		return storageValue(KBUtils.getWrappedObjectName(obj));
	}

	protected static Object objectId(KnowledgeItem obj) {
		return storageValue(KBUtils.getObjectName(obj));
	}

    /**
     * Copies the given array to a new one, skipping <code>null</code> values.
     */
	protected static Object[] nonNullStorageValues(Object[] aVector) {
        if (aVector == null) {
        	return null;
        }
        
		int resultSize = 0;
		for (Object value : aVector) {
			if (value != null) {
				resultSize++;
			}
		}
		
		Object[] result = new Object[resultSize];
		int pos = 0;
		for (Object value : aVector) {
			if (value != null) {
				result[pos++] = storageValue(value);
			}
        }
        return result;
    }

	/**
	 * Converts the given value to its storage value.
	 */
	protected static Object storageValue(Object value) {
		if (value instanceof TLID) {
			TLID id = (TLID) value;
			return storageValue(id);
		} else {
			return value;
		}
	}

	/**
	 * Converts the given identifier to its storage value.
	 */
	protected static Object storageValue(TLID id) {
		return id.toStorageValue();
	}

}
