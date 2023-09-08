/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.sql;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.sql.CommonConfiguredConnectionPool;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.PreparedQuery;
import com.top_logic.basic.sql.QueryPipedStreams;
import com.top_logic.basic.sql.SQLQuery;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.bean.BeanDataObject;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DataObjectImpl;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMORepository;
import com.top_logic.dob.sql.DynamicDBMORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.dsa.impl.AbstractDataSourceAdaptor;

/** 
 * A structured DataSourceAdaptor for SQL-Databases via JDBC.
 *<p>
 *  The Hierarachy for this DSA is fixed with 3 steps. e.g.
 *<pre>
 *  xxxdb://                 Root = the DB itself
 *  xxxdb://table            a whole table in the db
 *  xxxdb://table?condition  a (Set Of) records in the table using the (WHERE) condition
 *</pre>
 *</p>
 * TODO This is not Comittable
 * TODO (optionallly) use PrepStmCache and retry logic
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class JDBCDataSourceAdaptor extends AbstractDataSourceAdaptor {

	public interface Config extends PolymorphicConfiguration<JDBCDataSourceAdaptor> {

		String CONNECTION_POOL_PROPERTY = "connectionPool";

		@StringDefault(ConnectionPoolRegistry.DEFAULT_POOL_NAME)
		@Name(CONNECTION_POOL_PROPERTY)
		String getConnectionPool();
	}

    /** Delimiter between TableName and condition */
    public static final char    DELIMITER   = '?';

    /** used internally in delete() functions */
    public static final boolean WARN        = true;

    
    /** The repository we use to create the DataObjects */
    protected       DynamicDBMORepository  dbMO;
    
    /** MeaData describing the Database as a whole */
    protected       DataObject      dbMeta;

	private final ConnectionPool connectionPool;
	private final boolean shared;
    
	/**
	 * Creates a new {@link JDBCDataSourceAdaptor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link JDBCDataSourceAdaptor}.
	 * 
	 */
	public JDBCDataSourceAdaptor(InstantiationContext context, Config config) {
		String poolName = config.getConnectionPool();
		this.connectionPool = ConnectionPoolRegistry.getConnectionPool(poolName);
		this.shared = true;
		try {
			this.dbMO = new DynamicDBMORepository(this.connectionPool);
		} catch (SQLException ex) {
			context.error("Unable to create DynamicDBMORepository.", ex);
		}
	}

    /** This CTto may be used for Testing */
    public JDBCDataSourceAdaptor(DataSource ds) throws SQLException { 
        this.connectionPool = CommonConfiguredConnectionPool.createConfiguredPool(30, ds);
        this.shared = false;
        this.dbMO = new DynamicDBMORepository(this.connectionPool);
    }
    
    public JDBCDataSourceAdaptor(ConnectionPool connectionPool) throws SQLException {
        this.connectionPool = connectionPool;
        this.shared = true;
        this.dbMO = new DynamicDBMORepository(connectionPool);
	}

	/**
     * Should be called when testing to close() the embedded Connection.
     *
     * @throws DatabaseAccessException with the embedded SQLException
     */
    @Override
	public void close ()  throws DatabaseAccessException {
    	if (!shared) {
    		this.connectionPool.close();
    	}
    }
    
    /**
     * Structured -&gt; use Dataobjects , Unstructured -&gt; use Strams.
     *
     * @return  true - JDBC/SQL _is_ structured.
     */
    @Override
	public boolean isStructured () {
        return true;
    }

    /**
     * Does the element already exist?
     *
     * @param       path the name of the element
     * @return  true if the element exists
     */
    @Override
	public boolean exists (String path) {
        if (path.length() == 0) {
            return true;    // Database as a whole exists, well
        }
        int q = path.indexOf(DELIMITER);
        if (q < 0) { // no '?' in String
            return dbMO.containsMetaObject(path);
        }
        else { // Test with query
            String table = path.substring(0,q);
            if (dbMO.containsMetaObject(table)) {
            	String      where   = path.substring(q + 1);
            	boolean     result  = false;
            	String      sql     = "SELECT * FROM " + table + " WHERE " + where;
            	PooledConnection readConnection = this.connectionPool.borrowReadConnection();
                try  {
                	Statement stmt = readConnection.createStatement();
                	try {
                		ResultSet resultSet = stmt.executeQuery(sql);
                		try {
                			result  = resultSet.next();
                			return result;
                		} finally {
                			resultSet.close();
                		}
                	} finally {
                		stmt.close();
                	}
                }
                catch (SQLException sqx) { // bad query -> does not exist
                    Logger.info("exists(" + path + ") failed ", sqx, this);
                } finally {
                	connectionPool.releaseReadConnection(readConnection);
                }
            }
        }
        return false;
    }

	/**
     * Is the element an entry or a container?
     *
     * @param       path the name of the element
     * @return  true if it is a container
     *
     * @throws DatabaseAccessException usuallly as result of SQLException
     */
    @Override
	public boolean isContainer (String path)  throws DatabaseAccessException {

        if (path.length() == 0) {
            return true;    // Database as a whole is a container, well
        }

        int q = path.indexOf(DELIMITER);
        if (q < 0) { // no '?' in String
            return dbMO.containsMetaObject(path);
        }
        // else // a Query is never a container

        return false;
    }
    
    /**
     * Is the element an entry or a container?
     *
     * @param       path the name of the element
     * @return  true if it is an entry
     *
     * @throws DatabaseAccessException usuallly as result of SQLException
     */
    @Override
	public boolean isEntry (String path)  throws DatabaseAccessException {
        if (path.length() == 0) {
            return false;    // Database as a whole is never an entry
        }
        int q = path.indexOf(DELIMITER);
        if (q < 0) { // no '?' in String
            return false; // table as a whole is never any entry
        }
        else { // Test with query
            String table = path.substring(0,q);
            if (dbMO.containsMetaObject(table)) {
            	PooledConnection readConnection = this.connectionPool.borrowReadConnection();
                try  {
                    String      where   = path.substring(q + 1);
                    String      sql     = "SELECT * FROM " + table + " WHERE " + where;
                	Statement stmt = readConnection.createStatement();
                	try {
                		ResultSet res = stmt.executeQuery(sql);
                		try {
                			// Check for single Entry
                			boolean hasFirst = res.next();
							if (hasFirst) {
                				boolean hasSecond = res.next();
								return !hasSecond;
                			} else {
                				return false;
                			}
                		} finally {
                			res.close();
                		}
                	} finally {
                		stmt.close();
                	}
                }
                catch (SQLException sqx) { // bad query -> does not exist
                    Logger.info("exists(" + path + ") failed ", sqx, this);
                } finally {
                	connectionPool.releaseReadConnection(readConnection);
                }
            }
        }
        return false;
    }

    /**
     * Get the name of the element (entry/container)
     *
     * @param       path 	the name of the element
     * @return  	the name of the entry/container (not fully qualified)
     *
     */
    @Override
	public String getName (String path) {
        return path;
    }
    
    /**
     * Rename the element (entry/container)
     *
     * @param       oldPath 		the fully qualified name of the element
	 * @param       newName 	the new element name of the element
     * @return  	true, if renaming succeeded
     *
     * @throws NotSupportedException always
     */
    @Override
	public String rename (String oldPath, String newName)  throws NotSupportedException {
        throw new NotSupportedException("sorry not supported");
    }
	
    /**
     * Get the path (fully qualified name) of the element (entry/container)
     *
     * @param       name 	the name of the element
     * @return  	the path of the entry/container (not fully qualified)
     */
    public String getPath (String name)  throws NotSupportedException {
        return name;
    }

    /**
     * Get the name of the parent container of the given element (entry/container)
     *
     * @param       path 	the name of the element
     * @return  	the name of the parent container (fully qualified)
     */
    @Override
	public String getParent (String path) {
        
        if (path == null || path.length() == 0) {
            return path;    // Root is root is root is ....
        }
            
        int     pos  = path.indexOf(DELIMITER);
        if (pos > 0)  {
            String table = path.substring(0, pos);
            return table;
        }
        return "";  // Root
    }
    
    /** Internal helper fuction to return the index of the first BLOB (if any).
     *
     * This implementation does support only one BLOB per Tbale (as many DBs do, too).
     *
     *  @return 0 for no BLOB or anything > 0 for the index of the first BLOB.
     */
    protected int getFirstBLOB(MetaObject mo) {
        String names [] = MetaObjectUtils.getAttributeNames(mo);
        int    size = names.length;
        try {
            for (int i=0; i < size; i++) {
                String attr = names[i];
                MOAttribute mattr = MetaObjectUtils.getAttribute(mo, attr);
                if (mattr instanceof DBAttribute) {
					DBType type = ((DBAttribute) mattr).getSQLType();
                    switch (type)  {
						case BLOB:
						case CLOB:
							return i + 1;
						default:
							break;
                    }
                }
            }
        } catch (NoSuchAttributeException nsx) {
            Logger.error("getFirstBLOB() unexpected Exception", nsx, this);
        }
        return 0;
    }
    
    /**
     * Get an entry from a database.
     *
     * @param   path the name of the entry
     * @return  an InputStream for reading the entry's data
     *
     * @throws DatabaseAccessException - in case of DataBaseErrors
     */
    @Override
	public InputStream getEntry (String path) throws DatabaseAccessException {

        int q = path.indexOf(DELIMITER);
        if (q < 0) {  // no '?' in String
            return null; // cant get an InputStream  ...
        }

        String table = path.substring(0,q);
        try  {
            MetaObject mo = dbMO.getMetaObject(table);
            if (mo != null) {
                String      where   = path.substring(q + 1);
                String      sql     = "SELECT * FROM " + table + " WHERE " + where;
                PooledConnection readConnection = this.connectionPool.borrowReadConnection();
                try {
    				SQLQuery    query   = new SQLQuery(readConnection, sql);
                    ResultSet   res     = query.getResultSet();

                    InputStream result = null;
                    if (res.next()) {
                        int blob = getFirstBLOB(mo);
                        if (blob > 0) {
                            result = new QueryInputStream(query, blob);
                            if (res.wasNull()) {// Ugh, need special Handling here
                                result = null;
                            }
                            else { // query will be closed via Stream ....
                                query = null;
                            }
                        }
                        else {
							DataObject doResult = DBMORepository.extractDataObject(res, mo, this.connectionPool);
							result = new ByteArrayInputStream(DataObjectImpl.dump(doResult).getBytes());
                        }
                        if (res.next()) {
                            result = StreamUtilities.close(result);
                            Logger.info("getEntry() more than one Entry for '" + path + "'" , this);
                        }
                        if (query != null) {
                            query.close();
                        }
                    }
                    return result;
				} finally {
					connectionPool.releaseReadConnection(readConnection);
				}
            }
        }
        catch (SQLException sqx) {
            throw new DatabaseAccessException(sqx);
        }
        return null;
    }

    /**
     * Get an entry as DataObject from a database.
     *
     * @param   path the name of the entry (in the form tablename[?whereclause] where
     * 			the whereclause is a valid SAL where clause)
     * @return  A DataObject representing the entry, or null
     *
     * @throws DatabaseAccessException - when SQL access fails (table does not exist, where clause faulty etc.)
     */
    @Override
	public DataObject getObjectEntry (String path) throws DatabaseAccessException {
        // Test with query
        try  {
        	String where = "";
        	int q = path.indexOf(DELIMITER);
        	String table = (q >= 0) ? path.substring(0,q) : path;
        	if (q >= 0) {  // no '?' in String
        		where = " WHERE " + path.substring(q + 1);
        	}
        	
            MetaObject theMO = dbMO.getMetaObject(table);

            if (theMO != null) {
                DataObject  theResult  = null;
                String      theSQL     = "SELECT * FROM " + table + where;
                PooledConnection readConnection = this.connectionPool.borrowReadConnection();
                try {
                	Statement stmt = readConnection.createStatement();
                	try {
                		ResultSet theRes = stmt.executeQuery(theSQL);
                		try {
                            if (theRes.next()) {
                            	DOList theList = null;

								theResult = DBMORepository.extractDataObject(theRes, theMO, this.connectionPool);

                            	// If more result elements, build up a DOList.
                                while (theRes.next()) {
                                	if (theList == null) {
                                		theList = new DOList(MOCollectionImpl.createListType(theMO));
                                		theList.add(theResult);

                                		theResult = theList;
                                	}

									theList.add(DBMORepository.extractDataObject(theRes, theMO, this.connectionPool));
                                }
                            }              			
                            return theResult;
                			
                		} finally {
                			theRes.close();
                		}
                	} finally {
                		stmt.close();
                	}
				} finally {
					connectionPool.releaseReadConnection(readConnection);
				}
            }
        }
        catch (SQLException sqx) {
            throw new DatabaseAccessException(sqx);
        }
        return null;
    }

    /** Create an Insert statement to create an entry for the given MetaObject.
     *
     * @param table containes the Attributes of the Table.
     */
    protected PreparedQuery createInsertQuery(Connection con, MetaObject table) 
        throws SQLException  {
        String        attrs[] = MetaObjectUtils.getAttributeNames(table);
        int           size    = attrs.length;
        StringBuffer  query   = new StringBuffer(64 + size << 1); // * 2
        query.append("INSERT INTO ");
        size --;
        query.append(table.getName());
        query.append(" VALUES ( ");
        for (int i=0; i < size; i++) {  // INSERT INTO <tabel> VALUES (?,?, ....
            query.append("?,");
        }
        query.append("?)"); // ,?,?)

		// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING): Dynamic
		// SQL construction is necessary. No user-input is passed to the statement source.
        return new PreparedQuery(con, query.toString());
    }

    /** Create an Update statement to change a Tabe represented by the given MetaObject,
     *
     * @param table containes the Attributes of the Table.
     * @param where a condition used to update the table, should be some primary key ...
     */
    protected PreparedQuery createUpdateQuery(Connection con, MetaObject table, String where) 
        throws SQLException  {
        String        attrs[] = MetaObjectUtils.getAttributeNames(table);
        int           size    = attrs.length;
        StringBuffer  query   = new StringBuffer(64 + size << 6); // * 32
        query.append("UPDATE ");
        size --;
        query.append(table.getName());
        query.append(" SET ");
        for (int i=0; i < size; i++) {  // a=?,b=? ....
            String attrName = attrs[i];
            query.append(attrName);
            query.append("=?,");
        }
        query.append(attrs[size]); // ...y=?,z=?
        query.append("=? WHERE ");
        query.append(where);
        
		// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING): Dynamic
		// SQL construction is necessary for DBMS abstraction. No user-input is passed to the
		// statement source.
        return new PreparedQuery(con, query.toString());
    }

    /**
     * Put an entry in a database, will only work for special tables.
     *
     * The table must contain a BLOB <strong>and</strong> its Primary
     * key must be autumatically created.
     *
     * @param       path the name of the element
     * @param       data the physical data given as an OutputStream
     *
     * @throws NotSupportedException - don now how to implement this in a meaningfull way.
     */
    @Override
	public void putEntry (String path, InputStream data) throws DatabaseAccessException {
        
        int q = path.indexOf(DELIMITER);
        if (q < 0) { // no '?' in String
            throw new DatabaseAccessException("Need a WHERE part in the name");
        }

        String table = path.substring(0,q);
        try  {
            MetaObject mo = dbMO.getMetaObject(table);
            int blob = getFirstBLOB(mo);
            if (blob <= 0)  {
               throw new NotSupportedException(
                "putEntry is not supported for Table \"" + table + "\", no BLOB-Colums");
            }

            String          blobName= MetaObjectUtils.getAttributeNames(mo)[blob-1];
            String          where   = path.substring(q + 1);
            String          sql     = "UPDATE " + table + " SET " + blobName + "=? WHERE " + where;
            
            PooledConnection statementCache = this.connectionPool.borrowWriteConnection();
            try {
            	PreparedQuery   pQuery  = new PreparedQuery(statementCache, sql);
            	// available() is the best guess we can get (sorry ...)
            	pQuery.getPreparedStatement().setBinaryStream(1, data, data.available());
            	int num = pQuery.executeUpdate();
            	pQuery.close();
            	if (num == 0) {
            		statementCache.rollback();
            		throw new DatabaseAccessException("putEntry(" + path + ") failed");
            	}
            	else {
            		statementCache.commit();
            		if (num > 1) { // this may be what the caller actually wanted
            			Logger.info("putEntry(" + path + ", InputStream) changed " + num + " entries", this);
            		}
            	}
            } finally {
            	this.connectionPool.releaseWriteConnection(statementCache);
            }
        }
        catch (IOException iox) {
            throw new DatabaseAccessException(iox);
        }
        catch (SQLException sqx) {
            throw new DatabaseAccessException(sqx);
        }
    }
    
     /**
      * Put an entry in a container.
      *
      * @param    table         The container to be parent (a name of a Table)
      * @param    where         The name/id for the new entry (part of a WHERE statement)
      * @return   An Outputstream to the entry.
      *
      * @throws NotSupportedException - dont now how to implement this in a meaningfull way.
      */
     @Override
	public OutputStream putEntry (String table, String where)
                                                 throws DatabaseAccessException {
         try  {
             MetaObject mo = dbMO.getMetaObject(table);
             if (mo != null) {
                 int blob = getFirstBLOB(mo);
                 if (blob <= 0)  {
                     throw new DatabaseAccessException(
                         "getEntryOutputStream(" + table + ',' + where + 
                            ") not supported, does not contain a BLOB");
                 }
                 String          blobName= MetaObjectUtils.getAttributeNames(mo)[blob-1];
                 String          sql     = "UPDATE " + table + " SET " + blobName + "=? WHERE " + where;
                 
                 final PooledConnection connection = this.connectionPool.borrowWriteConnection();
                 
                 PreparedQuery   pQuery  = new PreparedQuery(connection, sql);
                 // Integer.MAX_VALUE is a bad hack but the JDBC-Specs are vague
                 // about this  
                 // (and it actually changed from mm-MySQL to MySQL-Connector)
                 return new QueryPipedStreams(pQuery, 1, Integer.MAX_VALUE) {
                	 @Override
					protected void close() throws InterruptedException {
                		 try {
                			 super.close();
                			 connection.commit();
                		 } catch (SQLException ex) {
							Logger.error("can not store things written to this stream!", ex, JDBCDataSourceAdaptor.class);
						} finally {
                			 connectionPool.releaseWriteConnection(connection);
                		 }
                	 }
                 }.getOutputStream();
             }
         }
         catch (SQLException sqx) {
             throw new DatabaseAccessException(sqx);
         }
         catch (IOException iox) {
             throw new DatabaseAccessException(iox);
         }
         return null;
     }

    /**
     * Put an entry in a database.
     *
     * @param       path the name of the element
     * @param       data The DataObejct holding the data.
     *
     * @throws DatabaseAccessException - in case something in the DB goes wrong.
     */
    @Override
	public void putEntry (String path, DataObject data) throws DatabaseAccessException {
        
        int q = path.indexOf(DELIMITER);
        if (q >= 0)  {
            throw new DatabaseAccessException("Can't insert with a WHERE clause");
        }

        String table = path;
        try  {
            MetaObject mo = dbMO.getMetaObject(table);
            if (mo == null)  {
                throw new DatabaseAccessException(table  + " is not a table in the Database");
            }

            PooledConnection writeConnection = this.connectionPool.borrowWriteConnection();
            try {
            	PreparedQuery pq    = createInsertQuery(writeConnection, mo);
            	dbMO.injectDataObject(pq.getPreparedStatement(), mo, data);
            	int num = pq.executeUpdate();
            	pq.close();
            	if (num == 0) {
            		writeConnection.rollback();
            		throw new DatabaseAccessException("putEntry(" + path + ") failed");
            	}
            	else{
            		writeConnection.commit();
            		if (num > 1) {// this may be what the caller actually wanted
            			Logger.info("putEntry(" + path + ") changed " + num + " entries", this);
            		}
            	}
            } finally {
            	this.connectionPool.releaseWriteConnection(writeConnection);
            }
        }
        catch (SQLException sqx) {
            throw new DatabaseAccessException(sqx);
        }
    }

    /**
     * Get an OutputStream for putting an entry in a database.
     *
	 * @param	path	the name of the element
     * @return 	an OutputStream to write data into the entry
     *
     * @throws DatabaseAccessException - in case something fails
     */
    @Override
	public OutputStream getEntryOutputStream (String path) throws DatabaseAccessException {

        int q = path.indexOf(DELIMITER);
        if (q < 0) { // no '?' in String
            return null; // cant get an InputStream  ...
        }

        String  table = path.substring(0,q);
        String  where = path.substring(q + 1);
        return putEntry(table, where);
    }
    
    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     * @param data the physical data given as an OutputStream
     *
     * @throws NotSupportedException - not yet implemented
     */
    @Override
	public String createEntry (String containerPath, String elementName, InputStream data) 
            throws NotSupportedException {
        throw new NotSupportedException("Dont know how to implement this.");
    }

    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     *
     * @return an Outputstream to the new entry
     *
     * @throws NotSupportedException - not yet implemented
     */
    @Override
	public OutputStream createEntry (String containerPath, String elementName) 
        throws NotSupportedException {
        throw new NotSupportedException("Dont know how to implement this.");
    }

    /**
     * Create the container in the current one.
     * 
     * @param elementName   the name/id for the new container
     *
     * @throws NotSupportedException alwaays - use DB-Tools to create new tables.
     */
    @Override
	public String createContainer (String containerPath, String elementName) throws NotSupportedException {
        throw new NotSupportedException("Please use your DB to create new tables");
    }

    /**
     * Create a new DataObject as entry for the container.
     *<p>
     *  You must call putEntry() to finally store the object in the database.
     *</p>
     * @param table     Name of the container (=table) to create the Object in.
     * @param elementName   always ignored here.
     *
     * @return a DataObject suiteable to hold the structured data.
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public DataObject createObjectEntry (String table, String elementName)
        throws DatabaseAccessException {
        
		{
            MetaObject mo = dbMO.getMetaObject(table);
            return new DefaultDataObject(mo);
        }
    }

    /**
     * Iternal helper fucntion fore delete() and deleteRecursively().
     *
     * @param   table the name of the table
     * @param   where part of a WHERE claose, null to indicate "delteAll"
     * @param   warn  true will warn when something else than just one elem ws delted
     *
     * @throws DatabaseAccessException - in case something goes wrong
     */
    protected void delete (String table, String where, boolean warn) throws DatabaseAccessException {

        try  {
            MetaObject mo = dbMO.getMetaObject(table);
            if (mo == null)  {
                throw new DatabaseAccessException(table  + " is not a table in the Database");
            }
            
            String sql;
            if (where != null) {
                sql   = "DELETE FROM " + table + " WHERE " + where;
            }
            else    
                sql   = "DELETE FROM " + table;
            
            int num;
            PooledConnection writeConnection = this.connectionPool.borrowWriteConnection();
            try {
            	Statement stmt = writeConnection.createStatement();
            	try {
            		num = stmt.executeUpdate(sql);
            	} finally {
            		stmt.close();
            	}
				writeConnection.commit();
            } finally {
            	this.connectionPool.releaseWriteConnection(writeConnection);
            }
            
            if (warn && num != 1) {// this may be what the caller actually wanted
                Logger.info("delete(" + table + ',' + where + ") deleted " + num + " entries", this);
            }
        }
        catch (SQLException sqx) {
            throw new DatabaseAccessException(sqx);
        }
        catch (UnknownTypeException utx) {
            throw new DatabaseAccessException(utx);
        }
    }

    /**
     * Delete an element from a database. If the element is a container, it is
     * deleted only if it is empty.
     *
     * @param   path the name of the element
     *
     * @throws DatabaseAccessException - in case something goes wrong
     */
    @Override
	public void delete (String path, boolean force) throws DatabaseAccessException {
        int q = path.indexOf(DELIMITER);
        if (q < 0)  {
            throw new DatabaseAccessException("Can't delete without a WHERE clause");
        }

        String table = path.substring(0,q);
        String where = path.substring(q + 1);
        delete (table, where, WARN);
    }

    /**
     * Delete the container and all sub-elements (works for an entry as well).
     * Can delete non-empty containers. For entries it is equivalent to delete(String).
     *
     * @param       path the name of the element
     *
     * @throws DatabaseAccessException - wehn name does not contain a WHERE part.
     */
    @Override
	public void deleteRecursively (String path) throws DatabaseAccessException {

        String table, where;

        int q = path.indexOf(DELIMITER);
        if (q < 0)  {
            table = path;
            where = null;
        } else  {
            table = path.substring(0,q);
            where = path.substring(q + 1);
        }
        delete (table, where, !WARN);
    }
    
    /**
     * Get the names of all Tables in the database.
     *
     * @throws DatabaseAccessException as result of an SQLExcpetion
     */
    public String[] getTableNames () throws DatabaseAccessException {
        PooledConnection readConnection = this.connectionPool.borrowReadConnection();
        try {
			DatabaseMetaData meta = readConnection.getMetaData();
			try (ResultSet allTables = meta.getTables(null, null, null, null)) {
				// This will include VIEWS, ALIASES and whatever else ...
				ArrayList result = new ArrayList();
				while (allTables.next()) {
					result.add(allTables.getString(3));
				}

				String sResult[] = new String[result.size()];
				return (String[]) result.toArray(sResult);
			}
        }
        catch (SQLException sqle) {
            throw new DatabaseAccessException(sqle);
        }
        finally {
            connectionPool.releaseReadConnection(readConnection);
        }
    }
    
    /**
     * Get the entries for a table with an (eventually empty) condition.
     *
     * @param condition a condition, if the condition is not empty then it must start with  " WHERE..."
     *
     * @throws DatabaseAccessException as result of an SQLExcpetion
     */
    public String[] getEntryNames (String table, String condition) throws DatabaseAccessException {
    	PooledConnection readConnection = this.connectionPool.borrowReadConnection();
        try {
            ArrayList           plist = new ArrayList(1);  // usually there is just on Primary key
			DatabaseMetaData    meta  = readConnection.getMetaData();
			try (ResultSet pkeys = meta.getPrimaryKeys("", "", table)) {
            while (pkeys.next()) {
                String  colname = pkeys.getString(4);
                // short   seq     = pkeys.getShort(5); // must no use this
                plist.add(colname); 
            }
			}
            int size = plist.size();
            if (size == 0) {
                throw new DatabaseAccessException("Cannot getEntryNames() without primary key(s)");
            }
                
            StringBuffer sql = new StringBuffer(64 + size << 4);
            sql.append("SELECT ");
            sql.append(plist.get(0));
            for (int i=1; i < size; i++) {
                sql.append(',');
                sql.append(plist.get(i));
            }
            sql.append(" FROM ");
            sql.append(table);
            sql.append(condition);
            SQLQuery  q      = new SQLQuery(readConnection, sql.toString());
            ResultSet res    = q.getResultSet();
            ArrayList result = new ArrayList();
            StringBuffer buf = new StringBuffer();    // recyled ...
            while (res.next()) {
                buf.setLength(0);
                buf.append(plist.get(0));
                buf.append("='");
                buf.append(res.getString(1));   // Will not always work, admitted
                buf.append('\'');
                for (int i=1; i < size; i++) {
                    sql.append(" AND ");
                    sql.append(plist.get(i));
                    buf.append("='");
                    buf.append(res.getString(i + 1));   // Will not always work, admitted
                    buf.append('\'');
                }
                result.add(buf.toString());
            }
            
            q.close();
            String sResult[] = new String[result.size()];
            return (String[]) result.toArray(sResult);
        }
        catch (SQLException sqle) {
            throw new DatabaseAccessException(sqle);
        } finally {
        	connectionPool.releaseReadConnection(readConnection);
        }
    }

    /**
     * Get the element names of the current container.
     *
     * @param       path the name of the container.
     * @return      an array of Strings with the fully qualified element names
     *
     * @throws DatabaseAccessException as result of an SQLExcpetion
     */
    @Override
	public String[] getEntryNames (String path) throws DatabaseAccessException {
    
        if (path == null || path.length() == 0) {
           return getTableNames();  // Entries for root
        }
        
        String  cond   = "";
        String  table;
        int     pos  = path.indexOf(DELIMITER);
        if (pos > 0)  {
            table = path.substring(0, pos);
            cond  = " WHERE " + path.substring(pos + 1);
        }
        else
            table = path;

        return getEntryNames(table, cond);  // Entries for Query
    }

    /**
     * Get the database specific properties of an element.
     *
     * @param       name the name of the element
     * @return  the database specific properties
     *
     * @throws DatabaseAccessException ususally with embedded SQLException
     */
    @Override
	public DataObject getProperties (String name) throws DatabaseAccessException {
        PooledConnection readConnection = this.connectionPool.borrowReadConnection();
        try {
        	if ( name == null || name.length() == 0) {
                // Return MetaData for complete DB
                if (dbMeta == null)  {
                    dbMeta = new BeanDataObject(readConnection.getMetaData());
                }
                return dbMeta;
            }
            int pos = name.indexOf(DELIMITER);
            if (pos > 0) {  // no need to use >= this would be /key ....
                name = name.substring(0,pos); // remove key
            }

            // Return MetaData for table given by name
			try (Statement stm = readConnection.createStatement()) {
				// Stupid trick to extract the MetaData only
				try (ResultSet res = stm.executeQuery("SELECT * FROM " + name + " WHERE 1=0")) {
					DataObject result = new BeanDataObject(res.getMetaData());
					return result;
				}
			}
        }
        catch (IntrospectionException iex) {
            throw new DatabaseAccessException(iex);
        }
        catch (SQLException sqle) {
            throw new DatabaseAccessException(sqle);
        }
        finally {
            connectionPool.releaseReadConnection(readConnection);
        }
    }

    /**
     * Set the database specific properties of an element.
     *
     * @param       path    the name of the element
     * @param       props   the database specific properties
     *
     * @throws NotSupportedException - dont know what to do here ?
     */
    @Override
	public void setProperties (String path, DataObject props) throws NotSupportedException {
        throw new NotSupportedException("Dont know what I should do here ?");
    }        
}
