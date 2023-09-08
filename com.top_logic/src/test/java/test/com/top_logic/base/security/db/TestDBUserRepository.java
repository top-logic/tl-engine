/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.db;

import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.device.db.DBUserRepository;
import com.top_logic.base.security.password.hashing.VerificationResult;
import com.top_logic.base.security.util.SignatureService;
import com.top_logic.base.user.UserDataObject;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Testcase for the {@link com.top_logic.base.security.device.db.DBUserRepository}
 *
 * @author    <a href="mailto:jochen.hiller@top-logic.com">Jochen Hiller</a>
 */
public class TestDBUserRepository extends BasicTestCase {

    /** Time to sleep so the MySQL Connection will time out for sure */
    static private final int MYSQL_SLEEP = 1500;
    
	private PooledConnection statementCache;
	private ConnectionPool connectionPool;

	private KnowledgeBase _kb;

	public TestDBUserRepository(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		_kb = KBSetup.getKnowledgeBase();
		connectionPool = KBUtils.getConnectionPool(_kb);
		statementCache = connectionPool.borrowWriteConnection();
	}

	@Override
	protected void tearDown() throws Exception {
		connectionPool.releaseWriteConnection(statementCache);
		
		super.tearDown();
	}

	public void testLifecycleValid() throws Exception {
		DBUserRepository userRep = newDBUserRepository();

		Map map = new HashMap(32);
		map.put("username", "jhi");
		map.put(PersonAttributes.PASSWORD, sign("bup"));
		map.put("userrole", "role1");
		map.put("initials", "jhiinitials");
		map.put("givenName", "Jochen");
		map.put("sn", "Hiller");
		map.put("personalTitle", "aPersonalTitle");
		map.put("customerName", "aCustomerName");
		map.put("telephoneNumber", "aTelephoneNumber");
		map.put("mobile", "aMobile");
		map.put("otherTelephone", "anOtherTelephone");
		map.put("privateNumber", "aPrivateNumber");
		map.put("mail", "aMail");
		map.put("otherMailbox", "anOtherMailbox");
		map.put("mailNickname", "aMailNickname");
		map.put("displayName", "aDisplayName");
		map.put("description", "aDescription");

		// create first user
		UserDataObject userDO1 = new UserDataObject(map);
		userRep.createUser(this.statementCache, userDO1);
		statementCache.commit();

		// another user
		map.put("username"    , "jhi1");
		map.put(PersonAttributes.PASSWORD, sign("bubbeldidubbel"));
		UserDataObject userDO2 = new UserDataObject(map);
		userRep.createUser(this.statementCache, userDO2);
		statementCache.commit();

		// update user1
		userDO1.setAttributeValue("givenName", "JochenJochen");
		userRep.updateUser(connectionPool , userDO1);

		// query user1
		List<DataObject> l = userRep.searchUsers(connectionPool, userDO1);
		assertEquals(1, l.size());

		assertTrue(checkPassword(userRep, "jhi", "bup").success());
		assertTrue(checkPassword(userRep, "jhi1", "bubbeldidubbel").success());
        
        assertNotNull(userRep.searchUser(connectionPool , "jhi"));
        assertNotNull(userRep.searchUser(connectionPool, "jhi1"));
        assertNull   (userRep.searchUser(connectionPool, "jhhNicHier"));

        l = userRep.searchUsers(connectionPool, "jhi*");
        assertEquals(2, l.size());

		// delete user2
		userRep.deleteUser(this.statementCache, userDO2);
		statementCache.commit();

        l = userRep.searchUsers(connectionPool, "jhi*");
        assertEquals(1, l.size());

		// delete user1
		userRep.deleteUser(this.statementCache, userDO1);
		statementCache.commit();

		// query user1 again, may not exist anymore 
		l = userRep.searchUsers(connectionPool, "*");
		assertNull("Users left over: " + toString(l), index(l).get("jhi"));
	}

	private Map<String, DataObject> index(List<DataObject> users) throws NoSuchAttributeException {
		HashMap<String, DataObject> result = new HashMap<>();
		for (DataObject user : users) {
			result.put((String) user.getAttributeValue("username"), user);
		}
		return result;
	}

	private String toString(List<DataObject> users) throws NoSuchAttributeException {
		StringBuilder result = new StringBuilder();
		for (DataObject user : users) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(user.getAttributeValue("username"));
		}
		return result.toString();
	}

	/** Ruin the Connection (MySQL only) to check te retry logic */
    public void testRetry() throws Exception {
        if (!(connectionPool.getSQLDialect() instanceof MySQLHelper)) {
        	return;
        }

		DBUserRepository userRep = newDBUserRepository();
        
        {
        	PooledConnection writeConnection = connectionPool.borrowWriteConnection();
        	try {
        		// Tweak MySQL into using a very short Timeout
        		// This WILL affect global settings so do not use on a live DB !
        		Statement stm = writeConnection.createStatement();
        		stm.execute("SET GLOBAL wait_timeout=1"); 
        		stm.close();
        	} finally {
        		connectionPool.releaseWriteConnection(writeConnection);
        	}
        }

        try {
            Map map = new HashMap(32);
            map.put("username"    , "kha");
			map.put(PersonAttributes.PASSWORD, sign("miregal"));
            map.put("userrole"    , "rückwärts");
            map.put("initials"    , "kha");
            map.put("givenName"   , "Klaus");
            map.put("sn"          , "Halfmann");
            map.put("customerName", "Business Operation Systems GmbH");
            map.put("telephoneNumber", "aTelephoneNumber");
            map.put("mobile"        , "aMobile");
            map.put("otherTelephone", "anOtherTelephone");
            map.put("privateNumber" , "aPrivateNumber");
            map.put("mail"          , "aMail");
            map.put("otherMailbox"  , "anOtherMailbox");
            map.put("mailNickname"  ,  "kha");
            map.put("displayName"   , "Klaus Halfmann");
            map.put("description"   , "Nondescriptive");

            // create first user (with intially closed connection)
            UserDataObject userKHA1 = new UserDataObject(map);
            userRep.createUser(this.statementCache, userKHA1);
    		statementCache.commit();

            // another user
            map.put("username"    , "kha2");
			map.put(PersonAttributes.PASSWORD, sign("knubbeldoppel"));
            UserDataObject userKHA2 = new UserDataObject(map);
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
            userRep.createUser(this.statementCache, userKHA2);
    		statementCache.commit();

            // update user1
            userKHA1.setAttributeValue("givenName", "KlausKlaus");
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
            userRep.updateUser(connectionPool, userKHA1);

            // query user1
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
			List<DataObject> l = userRep.searchUsers(connectionPool, userKHA1);
            assertEquals(1, l.size());

			assertTrue(checkPassword(userRep, "kha", "miregal").success());
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
			assertTrue(checkPassword(userRep, "kha2", "knubbeldoppel").success());
            
            assertNotNull(userRep.searchUser(connectionPool, "kha"));
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
            assertNotNull(userRep.searchUser(connectionPool, "kha2"));
            assertNull   (userRep.searchUser(connectionPool, "khaIsWech"));

            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
            l = userRep.searchUsers(connectionPool, "kha*");
            assertEquals(2, l.size());

            // delete user2
            Thread.sleep(MYSQL_SLEEP);  // Provoke mySQL timeout
            userRep.deleteUser(this.statementCache, userKHA2);
    		statementCache.commit();

            l = userRep.searchUsers(connectionPool, "kha*");
            assertEquals(1, l.size());

            // delete user1
            userRep.deleteUser(this.statementCache, userKHA1);
    		statementCache.commit();

            // query user1 again, may not exist anymore 
            l = userRep.searchUsers(connectionPool, "*");
			Map<String, DataObject> index = index(l);
			assertNull("Users left over: " + toString(l), index.get("kha"));
			assertNull("Users left over: " + toString(l), index.get("kha2"));
		} finally {
        	PooledConnection writeConnection = connectionPool.borrowWriteConnection();
        	try {
        		Statement stm = writeConnection.createStatement();
        		stm.execute("SET GLOBAL wait_timeout=DEFAULT");
        		stm.close();    
        	} finally {
        		connectionPool.releaseWriteConnection(writeConnection);
        	}
		}
    }
  
	public void testInvalidArgs() throws Exception {
		DBUserRepository userRep = newDBUserRepository();
		try {
			userRep.createUser(this.statementCache, null);
			statementCache.commit();
			fail("Expected NullPointerException");
		} catch (NullPointerException ex) {
			// must happen
		}
		try {
			userRep.updateUser(connectionPool, null);
            fail("Expected NullPointerException");
		} catch (NullPointerException ex) {
			// must happen
		}
		try {
			userRep.deleteUser(this.statementCache, (DataObject) null);
			statementCache.commit();
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
			// must happen
		}
    	assertFalse(userRep.deleteUser(this.statementCache, (String) null));
	}

	public void testLifecycleInvalid() throws Exception {
		DBUserRepository userRep = newDBUserRepository();

		Map map = new HashMap(32);
		map.put("username", "xxx");
		map.put(PersonAttributes.PASSWORD, sign("bup"));
		map.put("userrole", "role1");
		map.put("initials", "jhiinitials");
		map.put("givenName", "Jochen");
		map.put("sn", "Hiller");
		map.put("personalTitle", "aPersonalTitle");
		map.put("customerName", "aCustomerName");
		map.put("telephoneNumber", "aTelephoneNumber");
		map.put("mobile", "aMobile");
		map.put("otherTelephone", "anOtherTelephone");
		// map.put("privateNumber", "aPrivateNumber"); // intentionally left out.
		map.put("mail", "aMail");
		map.put("otherMailbox", "anOtherMailbox");
		map.put("mailNickname", "aMailNickname");
		map.put("displayName", "aDisplayName");
		map.put("description", "aDescription");

		// create first user
		UserDataObject userDO1 = new UserDataObject(map);

		// try to delete non existing user 
		assertFalse(userRep.deleteUser(this.statementCache, userDO1));
		statementCache.commit();
        
		assertFalse(checkPassword(userRep, "xxx", "bup").success());
	}

	private VerificationResult checkPassword(DBUserRepository userRep, String user, String password) {
		return ReflectionUtils.executeMethod(userRep, "checkPassword",
			new Class[] { ConnectionPool.class, String.class, char[].class },
			new Object[] { connectionPool, user, password.toCharArray() }, VerificationResult.class);
	}

	private DBUserRepository newDBUserRepository() throws UnknownTypeException {
		return new DBUserRepository(_kb.getMORepository());
	}

	private static String sign(String password) {
		return SignatureService.getInstance().sign(password);
	}

	@SuppressWarnings("unused")
	public static Test suite() {
		Test t = new TestSuite(TestDBUserRepository.class);
		if (false) {
			t = TestSuite.createTest(TestDBUserRepository.class, "testLifecycleValid");
		}
		t = ServiceTestSetup.createSetup(t, Login.Module.INSTANCE);
		t = ServiceTestSetup.createSetup(t, SignatureService.Module.INSTANCE);
		return KBSetup.getSingleKBTest(t);
	}

}
