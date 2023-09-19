/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.base.security.password.hashing.VerificationResult;
import com.top_logic.base.security.util.SignatureService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.util.Computation;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMORepository;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;

/** 
 * This class setup the respository with user meta model.
 *
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:jochen.hiller@top-logic.com">Jochen Hiller</a>
 */
public class DBUserRepository {

	private final MOStructureImpl _dbUserMO;
	/**
	 * Column name of the {@link PersonAttributes#USER_NAME} attribute.
	 */
	public static final String DB_USER_NAME = "CN";

	/**
	 * Value used for "no password set" in the non-null password column.
	 */
	public static final String NO_PASSWORD = "*";
	
	/**
	 * The database name of the password identifier {@value PersonAttributes#PASSWORD}.
	 */
	static String DB_PASSWORD_IDENTIFIER = "USERPASSWORD";

	/** name of the user object. */
	public static String USER_OBJECT = "user";

	public DBUserRepository(MORepository repository) throws UnknownTypeException {
		this(getUserMetaObject(repository));
    }
    
	public DBUserRepository(MOStructureImpl dbUserMO) {
		super();
		_dbUserMO = dbUserMO;
	}

	/**
	 * Checks, whether the password for the given username is valid.
	 * 
	 * If there is no user with the given name or the value cannot be found, false will be returned.
	 * If the given user is <code>null</code>, this method will return <code>false</code>, no matter
	 * on the given password (it's not possible to authenticate nobody!). If the password is
	 * <code>null</code>, the system has to check, whether the set password is also
	 * <code>null</code> (so an empty password is not per default invalid!).
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} get the connection to the database. Must not be
	 *        <code>null</code>.
	 * @param login
	 *        The {@link LoginCredentials} to check.
	 * @return true, if password correct for user.
	 */
	public boolean checkPassword(KnowledgeBase kb, LoginCredentials login) throws SQLException {
		VerificationResult verificationResult =
			checkPassword(KBUtils.getConnectionPool(kb), login.getUsername(), login.getPassword());
		if (!verificationResult.success()) {
			return false;
		}
		if (verificationResult.hasUpdatedHash()) {
			/* It is not enough to use only the connection pool, because the update of the user data
			 * is connected to the commit process of the KnowledgBase, i.e. the update would not be
			 * made persistent until the next KB commit. */
			try (Transaction tx = kb.beginTransaction()) {
				Person person = login.getPerson();
				Person.getUser(person).setAttributeValue(PersonAttributes.PASSWORD, verificationResult.newHash());
				person.updateUserData();
				tx.commit();
			}
		}
		return true;
	}

	private VerificationResult checkPassword(ConnectionPool connectionPool, String aName, final char[] aPass)
			throws SQLException {
        if (aName == null) {
			return VerificationResult.FAILED;
        }
        // now check, whether the user exists
        String            thePass   = null;
        DBHelper          theHelper = connectionPool.getSQLDialect();
		int               retry     = theHelper.retryCount();

        while (retry-- > 0) {
            PooledConnection readConnection = connectionPool.borrowReadConnection();
			DBHelper sqlDialect = readConnection.getSQLDialect();
			try {
				try (PreparedStatement stmt = readConnection.prepareStatement(selectPasswordStmt(sqlDialect))) {
					stmt.setString(1, aName);
					try (ResultSet res = stmt.executeQuery()) {
						if (res.next()) {
							thePass = res.getString(1);
							if (NO_PASSWORD.equals(thePass)) {
								thePass = null;
							}
							if (res.next()) {
								Logger.error("Error duplicate cn in " + _dbUserMO.getDBName() + " Check Primary key",
									this);
								thePass = null;
							}
						}
					}
				}
            } catch (SQLException sqx) {
                if (retry > 0 && theHelper.canRetry(sqx)) {
                    readConnection.closeConnection(sqx);
                    continue;
                }
            	throw sqx;
			} finally {
                connectionPool.releaseReadConnection(readConnection);
            }
            break;
        }
        final String encryptedMessage = thePass;
		return ModuleUtil.INSTANCE.inModuleContext(SignatureService.Module.INSTANCE, new Computation<VerificationResult>() {
			@Override
			public VerificationResult run() {
				if (encryptedMessage == null) {
					return VerificationResult.FAILED;
				}
				return PasswordHashingService.getInstance().verify(aPass, encryptedMessage);
			}
		});
    }

	private String selectPasswordStmt(DBHelper sqlDialect) {
		return "SELECT " + sqlDialect.columnRef(DBUserRepository.DB_PASSWORD_IDENTIFIER) + " FROM "
			+ sqlDialect.tableRef(_dbUserMO.getDBName()) + " WHERE "
			+ sqlDialect.columnRef(DBUserRepository.DB_USER_NAME) + "=?";
	}

	/**
	 * Return a List of all User matching the CN of the given DataObject.
	 * 
	 * Will fall back to <code>searchUser(String)</code>
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 */
	public List<DataObject> searchUsers(ConnectionPool connectionPool, DataObject anUserDO) throws SQLException,
			DataObjectException {
        String cn = (String) anUserDO.getAttributeValue(PersonAttributes.USER_NAME);
        return searchUsers(connectionPool, cn);
    }

	/**
	 * Return a List of all User matching the CN of the given DataObject.
	 * 
	 * Will fall back to <code>searchUser(String)</code>
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 * 
	 * @return empty List in case no user is found.
	 */
	public List<DataObject> searchUsers(ConnectionPool connectionPool, String cn) throws DataObjectException,
			SQLException {
        
        // now check, whether the user exists
		DBHelper          theHelper = connectionPool.getSQLDialect();
        int               retry     = theHelper.retryCount(); 
        while (retry-- > 0) { 
            PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
                StringBuffer sql = new StringBuffer(64);    
                sql.append("SELECT * FROM ");
				sql.append(theHelper.tableRef(_dbUserMO.getDBName()));
				sql.append(" WHERE ");
				sql.append(theHelper.columnRef(DBUserRepository.DB_USER_NAME));
				sql.append(" ");
				sql.append(SQLH.createLIKE(cn));
				try (Statement stmt = readConnection.createStatement()) {
					try (ResultSet res = stmt.executeQuery(sql.toString())) {
						List<DataObject> allUserDOs = new ArrayList<>();
						while (res.next()) {
							allUserDOs.add(DBMORepository.extractDataObject(res, _dbUserMO, connectionPool));
						}
						return allUserDOs;
					}

                }
            } catch (SQLException sqlX) {
                readConnection.closeConnection(sqlX);
                if (retry > 0 && theHelper.canRetry(sqlX)) {
                    Logger.info("Could not query for user retry " + retry, this);
                    continue;
                }
                Logger.error("Could not query for user", sqlX, this);
                throw sqlX;
            } finally {
                connectionPool.releaseReadConnection(readConnection);
            }
        }
		return Collections.emptyList();
        
    }

	/**
	 * Return a List of all Users.
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 * 
	 * @return empty List in case no user is found.
	 */
	public List<DataObject> getAllUsers(ConnectionPool connectionPool) throws SQLException {
        
        // now check, whether the user exists
		DBHelper          theHelper = connectionPool.getSQLDialect();
        int               retry     = theHelper.retryCount(); 
        while (retry-- > 0) { 
        	PooledConnection readConnection = connectionPool.borrowReadConnection();
			DBHelper sqlDialect = readConnection.getSQLDialect();
        	try {
				try (PreparedStatement stmt =
					readConnection.prepareStatement("SELECT * FROM " + sqlDialect.tableRef(_dbUserMO.getDBName()))) {
					try (ResultSet res = stmt.executeQuery()) {
						ArrayList<DataObject> allUserDOs = new ArrayList<>(128);
						while (res.next()) {
							allUserDOs
								.add(DBMORepository.extractDataObject(res, _dbUserMO, connectionPool));
						}
						allUserDOs.trimToSize();
						return allUserDOs;
					} catch (SQLException sqlX) {
						// Better forget about the Connection
						readConnection.closeConnection(sqlX);
						if (retry > 0 && theHelper.canRetry(sqlX)) {
							Logger.info("Could not query for user retry " + retry, this);
							continue;
						}
						Logger.error("Could not query for user", sqlX, this);
						throw sqlX;
					}
				}
        	} finally {
        		connectionPool.releaseReadConnection(readConnection);
        	}
        }
		return Collections.emptyList();
        
    }

	/**
	 * Return the current number of Users.
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 */
    public int countUsers(ConnectionPool connectionPool) throws SQLException {
        
		MOStructureImpl theUserMO = _dbUserMO;
        DBHelper          theHelper = connectionPool.getSQLDialect();
        int               retry     = theHelper.retryCount();         
        while (retry-- > 0) {
            PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
                String sql = 
					"SELECT COUNT(*) FROM " + theHelper.tableRef(theUserMO.getDBName());
				try (PreparedStatement stmt = readConnection.prepareStatement(sql)) {
					try (ResultSet res = stmt.executeQuery(sql)) {
						if (res.next()) {
							return res.getInt(1);
						}
					}
				}
            } catch (SQLException sqx) {
                readConnection.closeConnection(sqx);
                if (retry >= 0 && theHelper.canRetry(sqx))
                    continue;
                throw sqx;
            } finally {
                connectionPool.releaseReadConnection(readConnection);
            }
        }
        return 0;  // never reached

    }

	/**
	 * Return a single User matching the given CN.
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 * 
	 * @return null in case no such user exists,
	 */
    public DataObject searchUser(ConnectionPool connectionPool, String cn) throws DataObjectException, SQLException {
        
        // now check, whether the user exists
        PooledConnection readConnection = connectionPool.borrowReadConnection();
		DBHelper sqlDialect = readConnection.getSQLDialect();
        try {
			StringBuffer sql = new StringBuffer(64);    
            sql.append("SELECT * FROM ");
			sql.append(sqlDialect.tableRef(_dbUserMO.getDBName()));
			sql.append(" WHERE ");
			sql.append(sqlDialect.columnRef(DBUserRepository.DB_USER_NAME));
			sql.append("='");
			sql.append(cn);
            sql.append('\'');
			try (Statement stmt = readConnection.createStatement()) {
				try (ResultSet res = stmt.executeQuery(sql.toString())) {
            		DataObject result = null;
            		if (res.next()) {
						result = DBMORepository.extractDataObject(res, _dbUserMO, connectionPool);
            			if (res.next()) {
            				Logger.error("Error duplicate cn in " 
            						+ _dbUserMO.getDBName() + " Check Primary key", this);
            				// result = null; better return it anyway, mmh 
            			}
            		}
            		return result;
				}
			}
        } catch (SQLException sqlx) {
            readConnection.closeConnection(null); 
            throw sqlx;
        } finally {
            connectionPool.releaseReadConnection(readConnection);
        }
    }

    /**
     * Create a new User user within the user repository.
     * @param anUserDO  The user to store in the Repository.
     */
	public boolean createUser(PooledConnection statementCache, DataObject anUserDO)
			throws SQLException, DataObjectException {
		List<DBAttribute> dbAttributes = _dbUserMO.getDBAttributes();
        int      size         = dbAttributes.size();
		String sql = SQLH.createInsert(statementCache.getSQLDialect(), _dbUserMO.getDBName(), size);
		try (PreparedStatement stmt = statementCache.prepareStatement(sql)) {
			stmt.clearParameters();
			injectFromDO(stmt, dbAttributes, 0, size, anUserDO);
			boolean success = 1 == stmt.executeUpdate();
			if (!success) {
				Logger.error("User not inserted?", DBUserRepository.class);
			}
			return success;
		}
    }
    
	/**
	 * HelperMethod Generic Insert/Update for all types of dataObejcts
	 */
	protected void injectFromDO(PreparedStatement stmt, List<DBAttribute> dbAttributes,
				int start, int size, DataObject aDO)
			throws SQLException {
		// we know these are all string attributes , this makes it easy ...
		for (int j = start, k = 1; j < size; j++, k++) {
			DBAttribute attr = dbAttributes.get(j);

			final String val = (String) aDO.getValue(attr.getAttribute());
			if (val != null)
				stmt.setString(k, val);
			else
				stmt.setNull(k, attr.getSQLType().sqlType);
		}
	}

    public void updateUser(ConnectionPool connectionPool, DataObject anUserDO) throws SQLException, DataObjectException {
        PooledConnection writeConnection = connectionPool.borrowWriteConnection();
        try {
            updateUser(writeConnection, anUserDO);
            writeConnection.commit();
		} finally {
        	connectionPool.releaseWriteConnection(writeConnection);
        }
    }

	public boolean updateUser(PooledConnection connection, DataObject userDO) throws NoSuchAttributeException,
			SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		// copy all attrs into prep statement, taken from inject
		List<DBAttribute> dbAttributes = _dbUserMO.getDBAttributes();
		int    size         = dbAttributes.size();

		StringBuffer sql = new StringBuffer(32 + size << 3);    
		sql.append("UPDATE ");
		sql.append(sqlDialect.tableRef(_dbUserMO.getDBName()));
		sql.append(" SET ");
		    
		// We know that DBUserRepository.DB_USER_NAME is first colum, so skip it
		DBAttribute dbAttr = dbAttributes.get(1);
		sql.append(sqlDialect.columnRef(dbAttr.getDBName()));
		sql.append("=?");
		for (int i = 2; i < size; i++) {
			dbAttr = dbAttributes.get(i);
		    sql.append(',');
			sql.append(sqlDialect.columnRef(dbAttr.getDBName()));
		    sql.append("=?");
         }
		sql.append(" WHERE ");
		sql.append(sqlDialect.columnRef(DBUserRepository.DB_USER_NAME));
		sql.append("='");
		sql.append(userDO.getAttributeValue(PersonAttributes.USER_NAME));
		sql.append('\'');

		try (PreparedStatement stmt = connection.prepareStatement(sql.toString());) {
			injectFromDO(stmt, dbAttributes, 1, size, userDO);
			boolean success = 1 == stmt.executeUpdate();
			if (!success) {
				Logger.error("User not updated?", DBUserRepository.class);
			}
			return success;
		}
	}

    /** 
     * Delete the given user DO from the Database
     * 
     * Will fall back to  <code>searchUser(String)</code>
     */
    public boolean deleteUser(PooledConnection statementCache, DataObject anUserDO) throws SQLException, DataObjectException {
        String cn = (String) anUserDO.getAttributeValue(PersonAttributes.USER_NAME);
        return deleteUser(statementCache, cn);
    }


    /** 
     * Delete the user identified by the given cn.
     * 
     * @return true when exactly one user was deleted. 
     */
    public boolean deleteUser(PooledConnection statementCache, String cn) throws SQLException {
		DBHelper sqlDialect = statementCache.getSQLDialect();

		try (PreparedStatement stmt =
			statementCache.prepareStatement(("DELETE FROM " + sqlDialect.tableRef(_dbUserMO.getDBName()) +
				" WHERE " + sqlDialect.columnRef(DBUserRepository.DB_USER_NAME) + "=?"))) {
			stmt.setString(1, cn);
			return 1 == stmt.executeUpdate();
		}
    }

	/**
	 * Rename a user from one Cn to some other.
	 * 
	 * @param connectionPool
	 *        The pool to get the connection to the database. Must not be
	 *        <code>null</code>.
	 * 
	 * @return true when rename succeeded.
	 */
    public boolean renameUser(ConnectionPool connectionPool, String oldCn, String newCn) throws SQLException {
        DBHelper          theHelper = connectionPool.getSQLDialect();
        int               retry     = theHelper.retryCount();
        while (retry-- > 0) {
            PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			DBHelper sqlDialect = writeConnection.getSQLDialect();
			try {
				String userNameColumnRef = sqlDialect.columnRef(DBUserRepository.DB_USER_NAME);
				String sql = "UPDATE " + sqlDialect.tableRef(_dbUserMO.getDBName())
					+ " SET " + userNameColumnRef + "=? WHERE " + userNameColumnRef + "=?";
                         
				try (PreparedStatement stmt = writeConnection.prepareStatement(sql);) {
					stmt.setString(1, newCn);
					stmt.setString(2, oldCn);
					return 1 == stmt.executeUpdate();
				}
            }
            catch (SQLException sqx) {
                if (retry > 0 && theHelper.canRetry(sqx)) {
                    writeConnection.closeConnection(sqx);
                    continue;
                }
            	throw sqx;
            } finally {
            	connectionPool.releaseWriteConnection(writeConnection);
            }
            // break; // unreachable
        }
        return false; // unreachable
       
    }

	public static MOStructureImpl getUserMetaObject(KnowledgeBase kb) throws UnknownTypeException {
		return getUserMetaObject(kb.getMORepository());
	}

	public static MOStructureImpl getUserMetaObject(MORepository moRepository) throws UnknownTypeException {
		return (MOStructureImpl) moRepository.getMetaObject(DBUserRepository.USER_OBJECT);
	}

}