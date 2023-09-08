/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.util.MetaObjectUtils;

/** 
 * This respository Maps Database-Tables to MetaObjects.
 *<p>
 *  It is intened to be used with a Single Connection to
 *  a Database, but does not verify this. 
 *</p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DBMORepository implements MORepository {

    /** Map of (DB-)MetaObject index by (table-)name. */
    private    Map<String, MetaObject>  metaMap;
    
    public DBMORepository() {
        this.metaMap     = new HashMap<>();
    }

	/**
	 * Helper function to create a MOAttribute for a column
	 * 
	 * @param sqlDialect
	 *        The corresponding helper for the database the given
	 *        {@link ResultSetMetaData} comes from.
	 * 
	 * @throws UnknownTypeException
	 *         in case an SQL-Type cannot be mapped.
	 */
    protected MOAttribute attributeForCol(DBHelper sqlDialect, ResultSetMetaData meta, int i)
        throws SQLException, UnknownTypeException  {
        
        String  name      = meta.getColumnName(i);
        String  label     = meta.getColumnLabel(i);
		DBType stype = DBType.fromSqlType(sqlDialect, meta.getColumnType(i), meta.getScale(i));
        // Argh, not implemented with mySQL ...
        // String  type      = meta.getColumnClassName(1);
        String  type      = sqlDialect.getColumnTypeName(meta,i);
        boolean mandatory = meta.isNullable(i) == ResultSetMetaData.columnNoNulls;
        int     size      = meta.getColumnDisplaySize(i);
        // int  prec      = meta.getPrecision(i);
        int     scale     = meta.getScale(i);
    
        MetaObject mType = MOPrimitive.getPrimitive(type);
        if (mType == null) {
            throw new UnknownTypeException(
                "Dont know how to create a primitive type for Column " + i + " '" + name + "' type:" + type);
        }

		return new MOAttributeImpl(label, name, mType, mandatory, !MOAttribute.IMMUTABLE, stype, size, scale);
    }
    
	@Override
	public MetaObject getTypeOrNull(final String typeName) {
		MetaObject result = metaMap.get(typeName);
		if (result != null) {
			return result;
		}

		try {
			MetaObject newType = createMetaObject(typeName);
			if (newType != null) {
				try {
					this.addMetaObject(newType);
					return newType;
				} catch (DuplicateTypeException e) {
					throw new UnreachableAssertion(e);
				}
            }
		} catch (SQLException ex) {
			Logger.error("Access to database failed.", ex, DBMORepository.class);
        }

		return null;
	}

    /**
	 * Similar to as <code>getMetaObject</code> but does not throw exception.
	 * 
	 * And does not try to create the MetaObject.
	 *
	 * @return null when no MetaObject with this name can be found.
	 */
    public DBTableMetaObject getDBTable (String name) 
    {
        return (DBTableMetaObject) metaMap.get(name);
    }

	/**
	 * Hook for a subclasses to eventually dynamically create a MetaObject.
	 * 
	 * @return always null here.
	 */
    protected MetaObject createMetaObject(String tableName) throws SQLException, UnknownTypeException {
        return null;
    }

    /**
     * inserts a MetaObject into the repository with the given name
     * 
     * @exception   DuplicateTypeException if a MetaObject for the given name already
     *				exists.
     */
	@Override
	public void addMetaObject (MetaObject aMetaObject) 
    	throws DuplicateTypeException {
    	String name = aMetaObject.getName();
		
		aMetaObject.freeze();
		
        metaMap.put(name, aMetaObject);
    }
        
    @Override
	public MetaObject getMOCollection (String kind, String typeName)
        throws UnknownTypeException {
        throw new UnknownTypeException("Not supported by this Respository");    
    }

    /**
     * Returns true if this MORepository contains the specified MetaObject
     * or supports it otherwise false will be returned.
     */
    @Override
	public boolean containsMetaObject (MetaObject aMetaObject) {
        return containsMetaObject(aMetaObject.getName());
    }
	
    /**
     * Returns true if this MORepository contains a MetaObject with the given name.
     */
    public boolean containsMetaObject (String aName) {
        return metaMap.containsKey(aName);
    }

    /**
     * Returns the names of all MetaObjects known in this repository.
     *
     * @return a List of Strings representing the MetaObjects' names.
     */
	@Override
	public List<String> getMetaObjectNames ()  {
	    List<String>     typeNames = new ArrayList<>(metaMap.keySet());
	    return typeNames;
	}

	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		return Collections.unmodifiableCollection(metaMap.values());
	}

    /**
     * Returns the number of MetaObjects known in this repository.
     */
    public int size()  {
        return metaMap.size();
    }

    /** Crate a column specification for a given MO/DBAttribute. 
     *
     * Examples:
     * <pre> IDENTIFIER INTEGER
     *  D_VALUE NUMBER(10,2)
     *  PHYSICAL_RESOURCE VARCHAR</pre>
     */
    public static void appendColumnSpec(
        DBHelper aDBHelper, PrintWriter out, DBAttribute dbAttr) {
        
		DBType type = dbAttr.getSQLType();
        int size = dbAttr.getSQLSize();
        int prec = dbAttr.getSQLPrecision();
		boolean mandatory = dbAttr.isSQLNotNull();
        boolean binary = dbAttr.isBinary();
        
		out.write(dbAttr.getDBName());
		out.write('\t');
		aDBHelper.appendDBType(out, type, dbAttr.getDBName(), size, prec, mandatory, binary);
    }

    /**
     * Create a table for a MOStructure / DBTableMetaObject.
     * 
     * This will fail in case the Primary keys are not in consecutive
     * Order (which indicates a bad design anyway ...)
     *
     * @param stm       Statement used to fire the statementes.
     * @param out       Write statemens here (when given)
     * @param table     must implement DBTableMetaObject, too.
     */
    public static void createDBTable(
        DBHelper aDBHelper, Statement stm, PrintWriter out, MOStructure table) throws SQLException {
        
        DBTableMetaObject   dbTable     = (DBTableMetaObject) table;
		List<DBAttribute> attrs = dbTable.getDBAttributes();
        String              tableName   = dbTable.getDBName();
        int                 size        = attrs.size();
        if (size == 0) {
            Logger.info("Table '" + tableName + "' has no columns, ignored", 
                    DBMORepository.class);
            return;
        }
        
        StringWriter wout = null;
        if (out == null) {
			wout = new StringWriter(128 + size << 5);
			out  = new PrintWriter(wout);
        }
		out.write("CREATE TABLE ");
		out.write(tableName);
		out.println(" (");
		out.write('\t');
		DBAttribute attr = attrs.get(0);
        appendColumnSpec(aDBHelper, out, attr);
        for (int i=1; i<size; i++)  {
			attr = attrs.get(i);
			out.println(',');
			out.write('\t');
            appendColumnSpec(aDBHelper, out, attr);
        }
		MOIndex primaryKey = dbTable.getPrimaryKey();
        // care about primary keys  
		if (primaryKey != null) { // no primary keys, well ...
			out.println(',');
			out.write("\tPRIMARY KEY (");
			List<DBAttribute> pKeys = primaryKey.getKeyAttributes();
			size = pKeys.size();
			out.print(pKeys.get(0).getDBName());
            for (int i=1; i<size; i++)  {
				out.write(',');
				out.print(pKeys.get(i).getDBName()); // TODO #18743: Add binary for Oracle, here
            }
			out.write(')');
        }  
        out.println();
		out.write(')');
		aDBHelper.appendTableOptions(out, dbTable.isPKeyStorage(), dbTable.getCompress());
		if (wout != null) {
			String statement = wout.toString();
	        try {
				// IGNORE FindBugs(SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE):
				// Dynamic SQL construction is necessary for DBMS abstraction. No user-input is
				// passed to the statement source.
	            stm.execute(statement);
	        }
	        catch (SQLException sqx) {
	            Logger.error(statement, sqx,  DBMORepository.class);
                throw sqx; // Re-Throw so Create tables will fail ...
	        }
		}
		else
			out.println(';');
    }

	/**
	 * Create the Tables for a given MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 * @param repository
	 *        all MetObjects will be considered.
	 * @param out
	 *        Write Statements here when given
	 */
    public void createDBTables(ConnectionPool connectionPool, MORepository repository, PrintWriter out) throws SQLException {
        
    	PooledConnection connection = null;
        Statement stm  = null;
        try {
			Collection<? extends MetaObject> types = repository.getMetaObjects();
            
            if (out == null) {
            	connection = connectionPool.borrowWriteConnection();
            	stm =  connection.createStatement();
            }
			for (MetaObject meta : types) {
            	if (meta instanceof MOStructure         
            			&& meta instanceof DBTableMetaObject
            			&& !MetaObjectUtils.isAbstract(meta)) {
            		createDBTable(connectionPool.getSQLDialect(), stm, out, (MOStructure) meta);
            	}
            }
            if (connection != null) {
            	connection.commit();
            }
        }
        finally {
        	if (connection != null) {
        		connectionPool.releaseWriteConnection(connection);
        	}
            if (stm != null) {
                stm.close();
            }
        }
    }

	/**
	 * Create the Tables for this MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
    public void createDBTables(ConnectionPool connectionPool) throws SQLException {
         createDBTables(connectionPool, this, null);
    }

	/**
	 * Create the Tables for a given MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 * @param repository
	 *        all MetObjects will be considered.
	 */
    public void dropDBTables(ConnectionPool connectionPool, MORepository repository, PrintWriter out, boolean logErrors) throws SQLException {
        
    	PooledConnection con = null;
        Statement stm  = null;
        String    type = null;
        try {
			Collection<? extends MetaObject> types = repository.getMetaObjects();
            
            if (out == null) {
            	con = connectionPool.borrowWriteConnection();
            	stm = con.createStatement();
            }
			for (MetaObject meta : types) {
            	if (meta instanceof MOStructure        
            			&& meta instanceof DBTableMetaObject
            			&& !MetaObjectUtils.isAbstract(meta)) {
            		String statement = "DROP TABLE " + 
            		((DBTableMetaObject) meta).getDBName();
            		if (stm == null ) {
            			assert out != null;
            			out.write(statement);
            			out.println(';');
            		}
            		else try {
            			stm.executeUpdate(statement);
            		}
            		catch (SQLException e) {
            			if (logErrors)
            				Logger.error(statement, e,  DBMORepository.class);
            		}
            	}
            }
            if (con != null) {
            	con.commit();
            }
        }
        finally {
        	if (con != null) {
            	connectionPool.releaseWriteConnection(con);
        	}
            if (stm != null) {
                stm.close();
            }
        }
    }

	/**
	 * Drop the Tables for this MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
	public void dropDBTables(ConnectionPool connectionPool) throws SQLException {
		dropDBTables(connectionPool, this, null, /* logErrors */ false);
	}

	/**
	 * Drop the Indexes for this MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
	public void dropDBIndexes(ConnectionPool connectionPool) throws SQLException {
		dropDBIndexes(connectionPool, this, null, /* logErrors */ false);
	}

	/**
	 * Drop the Indexes for the given Repository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
    public void dropDBIndexes(ConnectionPool connectionPool, MORepository aRepository) throws SQLException {
        dropDBIndexes(connectionPool, aRepository, null, /* logErrors */ false);
    }

	/**
	 * Create a Real Database Index for a given DBIndex.
	 * 
	 * Indexes with inMemory() flag will silently be ignored.
	 * 
	 * @param sqlDialect
	 *        The corresponding helper for the database the given
	 *        {@link Statement} comes from.
	 * 
	 * @param stm
	 *        Statement used to fire the statements.
	 * @param tableName
	 *        Name of table used to create the index for.
	 * @param aDBIndex
	 *        index used to create the SQL from.
	 */
    public void createDBIndex(DBHelper sqlDialect, Statement stm, PrintWriter out, String tableName, DBIndex aDBIndex) 
        throws SQLException {
        
        if (aDBIndex.isInMemory()) { // will not be handled here
            return;
        }
            
        List<DBAttribute> attribs = aDBIndex.getKeyAttributes();
        int size = attribs.size();

		StringWriter wout = null;
		if (out == null) {
			wout = new StringWriter(128 + size << 5);
			out = new PrintWriter(wout);
		}
		out.write("CREATE ");
        if (aDBIndex.isUnique()) {
			out.write("UNIQUE ");
        }
		out.write("INDEX ");
		out.write(aDBIndex.getDBName());
		out.write(" ON ");
		out.write(tableName);
		out.write('(');
        if (size == 0)  {
            Logger.warn("Index " + aDBIndex.getDBName() + " has no columns !"
                        , DBMORepository.class);
            return;
        }
		out.write(attribs.get(0).getDBName());
        for (int i =1; i < size; i++)  {
			out.write(',');
			out.write(attribs.get(i).getDBName());
        }
		out.write(')');
		out.write(sqlDialect.getAppendIndex(aDBIndex.getCompress())); 
		if (wout != null) {
			String statement = wout.toString();
			try {
				stm.executeUpdate(statement);
			}
			catch (SQLException sqx) {
				Logger.info("failed to createDBIndex() " + statement , sqx, DBMORepository.class);
			}
		}
		else
			out.println(';');
    }

	/**
	 * Drop the Database Index for a given DBIndex.
	 * 
	 * Indexes with inMemory() flag will silently be ignored.
	 * 
	 * @param sqlDialect
	 *        The corresponding helper for the database the given
	 *        {@link Statement} comes from.
	 * @param stm
	 *        Statement used to fire the statements.
	 * @param tableName
	 *        Name of table used to create the index for.
	 * @param aDBIndex
	 *        index used to create the SQL from.
	 */
    public void dropDBIndex(DBHelper sqlDialect, Statement stm, PrintWriter out, String tableName,  DBIndex aDBIndex, boolean logErrors) 
        throws SQLException {
        
        if (aDBIndex.isInMemory()) { // will not be handled here
            return;
        }
        
        String statement = sqlDialect.dropIndex(aDBIndex.getDBName(), tableName);

		if (out == null) {
			try {
				stm.executeUpdate(statement);
			}
			catch (SQLException ex) {
                if (logErrors)
                    Logger.error("Unable to drop index '" + aDBIndex.getName() + "' on '" + tableName + "'!", ex, this);
			}
		}
		else {
            out.print(statement);
			out.println(';');
        }
    }

	/**
	 * Create all Indexes for a MOStructure / DBTableMetaObject.
	 * 
	 * @param sqlDialect
	 *        The corresponding helper for the database the given
	 *        {@link Statement} comes from.
	 * @param stm
	 *        Statement used to fire the statements.
	 * @param table
	 *        must implement DBTableMetaObject, too.
	 */
    public void createDBIndexes(DBHelper sqlDialect, Statement stm, PrintWriter out, MOStructure table) throws SQLException {
        
        List indexes = table.getIndexes();
        if (indexes == null) {
            return;
        }
        int size = indexes.size();
        String tableName = ((DBTableMetaObject) table).getDBName();
        for (int i=0; i<size; i++)  {
            MOIndex midx = (MOIndex) indexes.get(i);
            if (midx instanceof DBIndex) {
                createDBIndex(sqlDialect, stm, out, tableName, (DBIndex) midx);
            }
        }    
    }

	/**
	 * Drop all Indexes for a MOStructure / DBTableMetaObject.
	 * 
	 * @param sqlDialect
	 *        The corresponding helper for the database the given
	 *        {@link Statement} comes from.
	 * @param stm
	 *        Statement used to fire the statements.
	 * @param table
	 *        must implement DBTableMetaObject, too.
	 */
    public void dropDBIndexes(DBHelper sqlDialect, Statement stm, PrintWriter out, MOStructure table, boolean logErrors) throws SQLException {
        
        List indexes = table.getIndexes();
        if (indexes == null) {
            return;
        }
        int size = indexes.size();
        String tableName = ((DBTableMetaObject) table).getDBName();
        for (int i=0; i<size; i++)  {
            MOIndex midx = (MOIndex) indexes.get(i);

            if (midx instanceof DBIndex) {
                dropDBIndex(sqlDialect, stm, out, tableName, (DBIndex) midx, logErrors);
            }
        }    
    }

	/**
	 * Create all Indexes for given MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 * @param repository
	 *        all MetObjects will be considered.
	 * @param out
	 *        Write Statements here when given
	 */
    public void createDBIndexes(ConnectionPool connectionPool, MORepository repository, PrintWriter out) throws SQLException {
        
    	PooledConnection statementCache = null;
        Statement stm  = null;
        try {
			Collection<? extends MetaObject> types = repository.getMetaObjects();

            if (out == null) {
            	statementCache = connectionPool.borrowWriteConnection();
            	stm = statementCache.createStatement();
            }
			for (MetaObject meta : types) {
                if (meta instanceof MOStructure &&
                    meta instanceof DBTableMetaObject && 
                    (! MetaObjectUtils.isAbstract(meta))) {
                    createDBIndexes(connectionPool.getSQLDialect(), stm, out, (MOStructure) meta);
                }
            }
        }
        finally {
        	if (statementCache != null) {
        		connectionPool.releaseWriteConnection(statementCache);
        	}
            if (stm != null) {
                stm.close();
            }
        }
    }

	/**
	 * Create all Indexes for this Repository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
    public void createDBIndexes(ConnectionPool connectionPool) throws SQLException {
        createDBIndexes(connectionPool, this, null);
    }

	/**
	 * Create all Indexes for the given Repository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 */
    public void createDBIndexes(ConnectionPool connectionPool, MORepository aRepository) throws SQLException {
        createDBIndexes(connectionPool, aRepository, null);
    }

	/**
	 * Drop all Indexes for given MORepository.
	 * 
	 * @param connectionPool
	 *        The pool to the connection to the database from. Must not be
	 *        <code>null</code>.
	 * @param repository
	 *        all MetObject will be considered.
	 */
    public void dropDBIndexes(ConnectionPool connectionPool, MORepository repository, PrintWriter out, boolean logErrors) throws SQLException {
        
    	PooledConnection statementCache = null;
        Statement stm = null;
        String    type = null;
        try {
			Collection<? extends MetaObject> types = repository.getMetaObjects();

            if (out == null) {
            	statementCache = connectionPool.borrowWriteConnection();
            	stm = statementCache.createStatement();
            }
			for (MetaObject meta : types) {
            	if (meta instanceof MOStructure         &&
            			meta instanceof DBTableMetaObject) {
            		dropDBIndexes(connectionPool.getSQLDialect(), stm, out, (MOStructure) meta, logErrors);
            	}
            }
        }
        finally {
        	if (statementCache != null) {
            	connectionPool.releaseWriteConnection(statementCache);
        	}
            if (stm != null) {
                stm.close();
            }
        }
    }

	/**
	 * Create a DataObject from a ResultSet and some MOStructureImpl.
	 * 
	 * (Optimized version using a ArrayAttrbutValue Container)
	 * @param pool
	 *        Database which was used to create the given result.
	 */
	public static DataObject extractDataObject(ResultSet res, MOStructureImpl table, ConnectionPool pool)
        throws SQLException {
        
		DefaultDataObject dataObject = new DefaultDataObject(table);
		table.readToCache(pool, res, DBAttribute.DEFAULT_DB_OFFSET, dataObject);
		return dataObject;
    }

	/**
	 * Create a DataObject from a ResultSet and some MetaData.
	 * 
	 * @param pool
	 *        Database which was used to create the given result.
	 */
	public static DataObject extractDataObject(ResultSet res, MetaObject table, ConnectionPool pool)
			throws DataObjectException, SQLException {
            
        if (table instanceof MOStructureImpl)
            return extractDataObject(res, (MOStructureImpl) table, pool);
        
        // Fallback to slower, more general method
        
        String[]    attrNames   = MetaObjectUtils.getAttributeNames(table);
        DataObject  result      = new DefaultDataObject(table);
        int         size        = attrNames.length;
        for (int i=0; i < size; i++) {
            Object o = res.getObject(i + 1);
            result.setAttributeValue(attrNames[i], o);
        }
        return result;
    }

	/**
	 * Create a DataObject from a ResultSet and assuming given tableName..
	 * 
	 * @param pool
	 *        Database which was used to create the given result.
	 */
	public DataObject extractDataObject(ResultSet res, String table, ConnectionPool pool)
			throws DataObjectException, SQLException {
        
		return extractDataObject(res, getMetaObject(table), pool);
    }

    
    /** Inject a DataObject into a PreparedStatement to UPDATE/INSERT all values.
     *
     * Such a Statment must either be <code>INSERT INTO table VALUES (?,?,?)</code>
     * or <code>UPDATE table SET col1=?,col2=?, ... WHERE pkey=? </code>. 
     *
     * @param table  the MetaObject describing the Table
     * @param values values matching the Table, but not necessary with the same MetaObject.
     */
    public void injectDataObject(PreparedStatement stm, MetaObject table, DataObject values) 
        throws DataObjectException, SQLException {
        
        String[]    attrNames   = MetaObjectUtils.getAttributeNames(table);
        int         size        = attrNames.length;
        for (int i=0; i < size; ) {
            Object o = values.getAttributeValue(attrNames[i++]);
            stm.setObject(i , o);
        }
    }

    @Override
	public void resolveReferences() throws DataObjectException {
    	// Ignore.
    }
    
	@Override
	public boolean multipleBranches() {
		return true;
	}

}
