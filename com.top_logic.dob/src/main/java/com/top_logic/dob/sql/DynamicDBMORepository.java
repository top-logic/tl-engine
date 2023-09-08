/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOStructureImpl;

/**
 * Extends the DBMORepository to dynamically create MetaObjects.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DynamicDBMORepository extends DBMORepository {

    /** We often need this metaData, so ... */
    protected   DatabaseMetaData dbMeta;
    
    /** MetaObjects are created on demand until this flag is set */
    protected    boolean    hasAllTypes;

	private ConnectionPool connectionPool;

    public DynamicDBMORepository(ConnectionPool connectionPool) throws SQLException {
        super();
        this.connectionPool = connectionPool;
        PooledConnection readConnection = connectionPool.borrowReadConnection();
        try {
        	dbMeta      = readConnection.getMetaData();
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
        hasAllTypes = false;
    }

    /**
     * Create a new  MetaObject based on the given TableName.
     *<p>
     * Due to Case mappings of the DB the name of the MetaObject
     * may not macth the name given !. This fucntion does not set
     * the Primary Key index, since this would reuquire a more expensive
     * lookup.
     *</p>
     * @param aName  name of a Table to create a MetaObject for.
     *
     * @throws SQLException based on the underlying DB.
     * @throws UnknownTypeException in case an SQL-Type cannot be mapped.
     */
    @Override
	protected MetaObject createMetaObject(String aName) throws SQLException, UnknownTypeException {
		PooledConnection connection = this.connectionPool.borrowWriteConnection();
		try {
			try (Statement stm = connection.createStatement()) {
				MOStructureImpl result = null;
				// Common trick to fetch the Meta-data only ...
				String detchMetaDataStmt = "SELECT * FROM " + aName + " WHERE 0=1";
				try (ResultSet res = stm.executeQuery(detchMetaDataStmt)) {
					ResultSetMetaData meta = res.getMetaData();
					int count = meta.getColumnCount();
					String dbName = meta.getTableName(1);
					result = new MOStructureImpl(aName, count);
					for (int i = 1; i <= count; i++) {
						try {
							result.addAttribute(attributeForCol(connectionPool.getSQLDialect(), meta, i));
						} catch (DuplicateAttributeException dax) {
							// may happen in case of strange Table definitions, mmh
							throw new UnknownTypeException(
								"Table '" + dbName + "' contains duplicate rows?", dax);
						}
					}

					// Now care about the Primary Keys ...
					handlePrimaryKey(result, dbName);
				}
				return result;
			}
    	} finally {
    		this.connectionPool.releaseWriteConnection(connection);
    	}
    }

	private void handlePrimaryKey(MOStructureImpl result, String dbName) {
		try {
			ResultSet metaRes = dbMeta.getPrimaryKeys(null, null, dbName);
			try {
				List<MOAttributeImpl> pKeyAttributes = new ArrayList<>();
				while (metaRes.next()) {
					String colName = metaRes.getString(4);
					int keyInd = metaRes.getInt(5);
					// Reserve space.
					while (pKeyAttributes.size() < keyInd) {
						pKeyAttributes.add(null);
					}
					MOAttributeImpl attr = (MOAttributeImpl) result.getAttribute(colName);
					pKeyAttributes.set(keyInd - 1, attr);
				}
				result.setPrimaryKey(pKeyAttributes.toArray(DBAttribute.NO_DB_ATTRIBUTES));
			} finally {
				metaRes.close();
			}
		} catch (SQLException ex) {
			Logger.warn("createMetaObject(): Unable to get the primary keys from result set!", ex, this);
		} catch (NoSuchAttributeException nsx) {
			Logger.fatal("createMetaObject(): Where is my attribute gone?", nsx, this);
		}
	}

    /**
     * Returns the names of all MetaObjects known in this repository.
     *
     * Dynamically look into the DB and extract all the types.
     *
     * @return a List of Strings representing the MetaObjects' names.
     */
    @Override
	public List getMetaObjectNames ()  {
        if (!hasAllTypes) {
            String    tName = "* Not Yet Set *";
            PooledConnection readConnection = this.connectionPool.borrowReadConnection();
            try {
            	DBHelper   theDBHelper = DBHelper.getDBHelper(readConnection);
                
                String currSchema = theDBHelper.getCurrentSchema(readConnection);
                
				try (ResultSet res =
					dbMeta.getTables(readConnection.getCatalog(), currSchema, "%", new String[] { "TABLE" })) {
                // Mhh, this will retrieve things Like VIEWS and other nasty things, too
                while (res.next())  {
                    // String catalog  = res.getString(1);
                    // String schema   = res.getString(2);
                    tName    = res.getString(3);
                    // String ttype    = res.getString(4);
                    // String remarks  = res.getString(5);
                    if (!containsMetaObjectAlready(tName) && !theDBHelper.isSystemTable(tName)) {
                        MetaObject theMO = createMetaObject(tName);
                        this.addMetaObject(theMO);
                    }
                }
                hasAllTypes = true;
				}
            }
            catch (SQLException sqx) {
                Logger.error("Failed to getMetaObjectNames() for '" + tName + "'", sqx, this);
            }
            finally  {
                connectionPool.releaseReadConnection(readConnection);
            }
        }
        return super.getMetaObjectNames();
    }
    

    /**
     * Returns true if this MORepository contains a MetaObject with the given name.
     */
    @Override
	public boolean containsMetaObject (String aName) {
        if (hasAllTypes) {
            return containsMetaObjectAlready(aName);
        }
        else 
          try {
            return getMetaObject(aName) != null;
        } catch (UnknownTypeException utex) {
            // ignored, this is ok
        }
        return false;
    }

	private boolean containsMetaObjectAlready(String aName) {
		return super.containsMetaObject(aName);
	}

}
