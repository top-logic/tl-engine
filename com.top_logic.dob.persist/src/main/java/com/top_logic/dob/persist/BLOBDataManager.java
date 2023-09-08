/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.persist;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * A DataManager that stores all Objects in a single, big, Blob.
 * 
 * The most promient usage is the encrypted dataManager which
 * will encrypt the Blobs.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BLOBDataManager extends AbstractDataManager {

    /** Sql String to insert values into INTO DATA_OBJECT_BLOB */
	private final String _insertStringSQLQuery;

    /** Sql String to update values into INTO DATA_OBJECT_BLOB */
	private final String _updateStringSQLQuery;
 
    /** Sql String to delte values from INTO DATA_OBJECT_BLOB */
	private final String _deleteStringSQLQuery;
             
    /** Sql String to fetch values from INTO DATA_OBJECT_BLOB */
	private final String _selectStringSQLQuery;
        
    /** Sql String to count the number of values in INTO DATA_OBJECT_BLOB */
	private final String _checkStringSQLQuery;

    /** Randomizer used optionally on writing the Objects */
    protected Random rand;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link BLOBDataManager}.
	 */
	public BLOBDataManager(InstantiationContext context, Config config) throws SQLException {
		super(context, config);

		String dataObjectBlobTableRef = dbHelper.tableRef("DATA_OBJECT_BLOB");
		String identifierColumnRef = dbHelper.columnRef("IDENTIFIER");
		String valColumnRef = dbHelper.columnRef("VAL");

		_insertStringSQLQuery = createInsertStringSQLQuery(dataObjectBlobTableRef);
		_updateStringSQLQuery = createUpdateStringSQLQuery(dataObjectBlobTableRef, identifierColumnRef, valColumnRef);
		_deleteStringSQLQuery = createDeleteStringSQLQuery(dataObjectBlobTableRef, identifierColumnRef);
		_selectStringSQLQuery = createSelectStringSQLQuery(dataObjectBlobTableRef, identifierColumnRef, valColumnRef);
		_checkStringSQLQuery = createCheckStringSQLQuery(dataObjectBlobTableRef, identifierColumnRef);
	}

	private String createCheckStringSQLQuery(String tableRef, String identifierColumnRef) {
		return "SELECT COUNT(*) FROM " + tableRef + " WHERE " + identifierColumnRef + "=?";
	}

	private String createSelectStringSQLQuery(String tableRef, String identifierColumnRef, String valColumnRef) {
		return "SELECT " + valColumnRef + " FROM " + tableRef + " WHERE " + identifierColumnRef + "=?";
	}

	private String createDeleteStringSQLQuery(String tableRef, String identifierColumnRef) {
		return "DELETE FROM " + tableRef + " WHERE " + identifierColumnRef + "=?";
	}

	private String createUpdateStringSQLQuery(String tableRef, String identifierColumnRef, String valColumnRef) {
		return "UPDATE " + tableRef + " SET " + valColumnRef + "=? WHERE " + identifierColumnRef + "=?";
	}

	private String createInsertStringSQLQuery(String dataObjectBlobTableRef) {
		return "INSERT INTO " + dataObjectBlobTableRef + " VALUES (?,?)";
	}
    
	/**
	 * @see com.top_logic.dob.persist.DataManager#createDataObject(String, TLID, int)
	 */
    @Override
	public DataObject createDataObject(String aType, TLID anID, int aSize) {
        return new BLOBDataObject(aSize, aType, anID);
    }

    /** Convert a BLOBDataObject into an InputStream */
    protected void setBytes(PreparedStatement pstm, int i, BLOBDataObject aDO) 
        throws SQLException {
        
        int size = aDO.getMap().size() << 5;    // * 32
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream(size)) {
			try (OutputStream os = handleOutput(bout);
					ObjectOutputStream oout = new ObjectOutputStream(os)) {
				aDO.writeObject(oout, rand);
			}
			byte[] bytes = bout.toByteArray();
			pstm.setBytes(i, bytes);
		} catch (IOException ex) {
			// actually not thrown as ByteArrayOutputStream does not throw it.
			throw new IOError(ex);
		}
        // System.out.println("Estimated " +  size + " was " + bytes.length);
    }
    
    /**
     * Allow subclasses (CryptDataManager) to modify the bytes.
     */
    protected OutputStream handleOutput(OutputStream os) {
        return os;
    }

    /**
     * Allow subclasses (CryptDataManager) to modify the bytes.
     */
    protected InputStream handleInput(InputStream is) {
        return is;
    }

    /**
     * Load the DataObject from the BLOB table. 
     * 
     * @param    aType    The type of the meta object of the data object.
     * @param    anID     The ID of the data object to be found.
     */
    @Override
	public DataObject load(String aType, TLID anID) throws SQLException {

        BLOBDataObject    theDO  = null;
        int               retry  = dbHelper.retryCount();
        while (retry-- > 0) {
            PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
				try (PreparedStatement pstm = readConnection.prepareStatement(_selectStringSQLQuery)) {
					IdentifierUtil.setId(pstm, 1, anID);
					try (ResultSet res = pstm.executeQuery()) {
						if (res.next()) {
							InputStream is = res.getBinaryStream(1);
							is = handleInput(is);
							ObjectInputStream ois = new ObjectInputStream(is);
							theDO = new BLOBDataObject(ois);
							if (0 == theDO.getMap().size())
								theDO = null; // no actual load
							else
								theDO.setIdentifier(anID);
						}
					}
				}
            }
            catch (SQLException  sqlx) {
                if (retry > 0) {
                	readConnection.closeConnection(sqlx);
                    continue;
                }
                Logger.error("failed to load(" + aType+ ',' + anID + ")", sqlx, this);
				throw sqlx;
            }
            catch (Exception xxx) {
                Logger.error("failed to load(" + aType+ ',' + anID + ")", xxx, this);
            }
            finally {
                connectionPool.releaseReadConnection(readConnection);
            }
            break;
        }
        return theDO;
    }

    /**
     * Delete the given DataObject from the Storage.
     */
    @Override
	public boolean delete(DataObject anObject) throws SQLException {
        
        boolean result = false;
        int     retry  = dbHelper.retryCount();
        while (retry -- > 0) {
            PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			try {
				try (PreparedStatement pstm = writeConnection.prepareStatement(_deleteStringSQLQuery)) {
					IdentifierUtil.setId(pstm, 1, anObject.getIdentifier());

					result = 1 == pstm.executeUpdate();
					// pCache.getConnection().commit(); // use Autocommit
				}
            }
            catch (SQLException e) {
                if (retry > 0) {
                	writeConnection.closeConnection(e);
                    continue;
                }
                Logger.error("failed to delete " + anObject, e, this);
				throw e;
            }
            finally {
            	connectionPool.releaseWriteConnection(writeConnection);
            }
            break;
        }
        return result;
    }

    /**
     * Delete the given data object within the given transaction context.
     * 
     * For this deleting, the method uses the given transaction.
     * 
     * @param    pCache      The prepared Statements to use.
     * @param    anObject    The object to be deleted.
     * @return   <code>true</code>, if deleting succeeds.
     * @throws   IllegalArgumentException    If the given object is <code>null</code>.
     */
    @Override
	public boolean delete(DataObject anObject, CommitContext pCache) 
                                   throws IllegalArgumentException, SQLException {

        try {
			try (PreparedStatement pstm = pCache.getConnection().prepareStatement(_deleteStringSQLQuery)) {
				IdentifierUtil.setId(pstm, 1, anObject.getIdentifier());

				return 1 == pstm.executeUpdate();
			}
        }
        catch (SQLException ex) {
			Logger.error("Unable to delete " + anObject, ex, this);
			throw ex;
        }
    }

    /** Add Stream and  Type and ID Handling to ExampleDataObject */
    private static class BLOBDataObject extends ExampleDataObject {

		private TLID id;

        private String type;

        /** Create a new BLOBDataObject with given Size for an Object with type and Id */
		public BLOBDataObject(int aSize, String aType, TLID anID) {
			super(aSize);

            if (anID == null)
                throw new NullPointerException("BLOBDataObject without ID");
            if (aType == null)
                throw new NullPointerException("BLOBDataObject without Type");
            this.type = aType;
			this.id = anID;
        }

        /** Copy CTor to allow conversion of any DataObject */
        public BLOBDataObject(DataObject copy) {
            this(copy.tTable().getName(), copy.getIdentifier(), copy);
        }
        
		public BLOBDataObject(String type, TLID id, NamedValues values) {
            String theNames[] = values.getAttributeNames();
            int    theSize    = theNames.length;
            
			this.map = new HashMap<>(theSize);
            this.id   = id;
            this.type = type;
            
            try {
                for (int i=0; i < theSize; i++) {
                    String theName  = theNames[i];
                    Object theValue = values.getAttributeValue(theName);
                    // Support nested Objects
                    if (theValue instanceof DataObject
                     && !(theValue instanceof  BLOBDataObject))
                        theValue = new BLOBDataObject((DataObject) theValue);
                    map.put(theName,theValue);
                }
            }
            catch (NoSuchAttributeException nsax) {
                Logger.error("Strange DataObject to copy from" + values
                    , nsax, this);
            }
		}


        /** CTor to create a DataObject from a ObjectStream */
        public BLOBDataObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            
            int size1 = in.readInt();
            int size2 = in.readInt();
            int size  = size2+size1;
			this.map = new HashMap<>(size);
            for (int i=0; i < size; i++) {
                Object val = readValue(in);
                String key = in.readUTF();
                if (val != null)
                    map.put(key, val);
            }
            type     = in.readUTF();     
         }

		// overriden methods from Object
        
        /** Return some reasonable String for Debugging */
        @Override
		public String toString() {
            return "BLOBDataObject " + type + ':' + id;
        }

        /** Read values using a special, short encoding.
         * 
         * This helps reducing memory footprint and allows
         * more secure Encryption. 
         */
        protected Object readValue(ObjectInputStream in) throws IOException, ClassNotFoundException { 
            
            byte b = in.readByte();
            switch (b) {
                case STRING_TYPE:
                    return in.readUTF();
                case INTEGER_TYPE:
					return Integer.valueOf(in.readInt());
                case DOUBLE_TYPE:
					return Double.valueOf(in.readDouble());
                case FLOAT_TYPE:
					return Float.valueOf(in.readFloat());
                case LONG_TYPE:
					return Long.valueOf(in.readLong());
                case DATE_TYPE:
                    return new Date(in.readLong());
                case BOOLEAN_TRUE:
                    return Boolean.TRUE;
                case BOOLEAN_FALSE:
                    return Boolean.FALSE;
                case LINK_TYPE: {
					TLID theId = IdentifierUtil.fromExternalForm(in.readUTF());
                    BLOBDataObject theDO = new BLOBDataObject(in);
                    theDO.setIdentifier(theId);
                    return theDO;
                }
                case NULL_TYPE:
                    return null;
                default:
                    return in.readObject();
            }
        }

		// Overwritten methods from ExampleDataObject

		/**
		 * @see com.top_logic.dob.DataObject#getIdentifier()
		 */
		@Override
		public TLID getIdentifier() {
			return (this.id);
		}

        /** Allow setting of our identifier.
         *
         * @param   anIdentifier   the new indetifier
         */
        @Override
		public void setIdentifier(TLID anIdentifier) {
            if (anIdentifier == null)
                throw new NullPointerException("BLOBDataObject without ID");
            id = anIdentifier;      
        }

        /**
         * @see com.top_logic.dob.simple.ExampleDataObject#getMetaObjectName()
         */
        @Override
		public String getMetaObjectName() {
            return (this.type);
        }
        
        // "Implemenation" of Serializable
        
        /** Write out this class (not being really serializable). */
        public void writeObject(ObjectOutputStream out, Random rand)
            throws IOException {
                
            int size = map.size();
            int diff = 255; 
            if (rand != null)
                diff = rand.nextInt(); 
            out.writeInt(diff);   
            out.writeInt(size-diff);   
			Iterator<String> keys;
            if (rand != null) {
				ArrayList<String> list = new ArrayList<>(map.keySet());
                Collections.shuffle(list, rand);
                keys = list.iterator();
            }
            else 
                keys = this.map.keySet().iterator();
            while (keys.hasNext()) {
				String key = keys.next();
                Object value = this.map.get(key);
                writeValue(value, out, rand);
                out.writeUTF   (key);
            }
            out.writeUTF(type);    
        }
        
        /** Write values using a special, short encoding.
         * 
         * This helps reducing memory footprint and allows
         * more secure Encryption. 
         */
        protected void writeValue(Object anObject, ObjectOutputStream out, Random rand) throws IOException { 
            if (anObject instanceof String) {
                out.writeByte(STRING_TYPE);
                out.writeUTF((String) anObject);
            }
            else if (anObject instanceof Number) {
                if (anObject instanceof Integer) {
                    out.writeByte(INTEGER_TYPE);
                    out.writeInt(((Integer) anObject).intValue());
                }
                else if (anObject instanceof Double) {
                    out.writeByte(DOUBLE_TYPE);
                    out.writeDouble(((Double) anObject).doubleValue());
                }
                else if (anObject instanceof Float) {
                    out.writeByte(FLOAT_TYPE);
                    out.writeFloat(((Float) anObject).floatValue());
                }
                else if (anObject instanceof Long) {
                    out.writeByte(LONG_TYPE);
                    out.writeLong(((Long) anObject).longValue());
                }
            }
            else if (anObject instanceof Date) {
                out.writeByte(DATE_TYPE);
                out.writeLong(((Date) anObject).getTime());
            }
            else if (anObject instanceof Boolean) {
                if (((Boolean) anObject).booleanValue())
                    out.writeByte(BOOLEAN_TRUE);
                else
                    out.writeByte(BOOLEAN_FALSE);
            }
            else if (anObject instanceof DataObject) {
                BLOBDataObject theDO;
                if (anObject instanceof BLOBDataObject)
                    theDO = (BLOBDataObject) anObject;
                else
                    theDO = new BLOBDataObject((DataObject) anObject);

                out.writeByte(LINK_TYPE);
				out.writeUTF(IdentifierUtil.toExternalForm(theDO.getIdentifier()));
                theDO.writeObject(out, rand);
            }
            else if (anObject == null)
                out.writeByte(NULL_TYPE);
            else 
            {
                out.writeByte(UNKNOWN_TYPE);
                out.writeObject(anObject);
            }
        }
    }

	@Override
	public boolean deleteValues(CommitContext context, String type, TLID id, NamedValues values) throws SQLException {
        try {
			try (PreparedStatement pstm = context.getConnection().prepareStatement(_deleteStringSQLQuery)) {
				IdentifierUtil.setId(pstm, 1, id);

				return 1 == pstm.executeUpdate();
			}
        }
        catch (SQLException ex) {
			Logger.error("Unable to delete: " + id, ex, this);
			throw ex;
        }
	}

	@Override
	protected boolean internalStore(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException {
		if (values == null)
            throw new NullPointerException();
        
        BLOBDataObject theDO;
        if (values instanceof BLOBDataObject)
            theDO = (BLOBDataObject) values;
        else
            theDO = new BLOBDataObject(type, id, values);

		TLID theId = theDO.getIdentifier();
		{
			int  count = 0;
			try (PreparedStatement pstm = context.getConnection().prepareStatement(_checkStringSQLQuery)) {
				IdentifierUtil.setId(pstm, 1, theId);
				try (ResultSet res = pstm.executeQuery()) {
					if (res.next())
						count = res.getInt(1);
				}
			}
			if (count == 0) { // does not exist, use insert
				try (PreparedStatement pstm = context.getConnection().prepareStatement(_insertStringSQLQuery)) {
					IdentifierUtil.setId(pstm, 1, theId);
					setBytes(pstm, 2, theDO);
					return 1 == pstm.executeUpdate();
				}
			} else { // exists, use update
				try (PreparedStatement pstm = context.getConnection().prepareStatement(_updateStringSQLQuery)) {
					setBytes(pstm, 1, theDO);
					IdentifierUtil.setId(pstm, 2, theId);
					return 1 == pstm.executeUpdate();
				}
			}
		}
	}
}
