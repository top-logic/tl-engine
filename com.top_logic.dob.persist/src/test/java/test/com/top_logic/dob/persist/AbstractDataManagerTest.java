/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.CommitContextWrapper;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * Superclass for Tescases using a {@link com.top_logic.dob.persist.DataManager}.
 *
 * Since the class is actually abstract only the static methods are tested.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class AbstractDataManagerTest extends BasicTestCase {

    /** The count of the DataObjects that are created. */
    private static int DATA_OBJECT_COUNT = 100;

    /** Need two long (&gt; 255) Strings */
    protected static final String LSTRING1 = 
        " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need two long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}.";
    
    /** Need two lomg (&gt; 255) Strings */
    protected static final String LSTRING2 = 
        " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}."
    +   " Need another long (&gt; 255) Strings Testcase for the {@com.top_logic.mig.dataobjects.persist.DataManager}.";

    /**
     * Constructor for TestDataManager.
     * 
     * @param name name of function to execute for Testing.
     */
    public AbstractDataManagerTest(String name) {
        super(name);
    }

    /** On demand accessor for the DataManager. */
    protected DataManager getDataManager() throws Exception {
        return DataManager.getInstance();
    }
    
    protected Date getTestDate() {
                        // 2003-01-01
        return new Date(1041375600000L);
    }

    // actual Tescases
    
    /**
     * Test for a (simple) boolean store(DataObject)
     */
    public void testStoreDataObject() throws Exception {
    	DataManager storage = getDataManager();

		try {
            store(storage, null);
            fail("store of null values should fail!");
        }
        catch (Exception expected) {
            // Expected
        }

        DataObject theObject = createSimpleDO();
        try {
            store(storage, theObject);

            DataObject theObj2 = loadNotNull(theObject.tTable().getName(),
                                                   theObject.getIdentifier());

            assertEquals("Loaded wrong object (type is not equal)!", 
                              theObject.tTable().getName(), 
                              theObj2.tTable().getName());

            assertEquals("Loaded wrong object (IDs are not equal)!", 
                              theObject.getIdentifier(), 
                              theObj2.getIdentifier());
        }
        finally {
            storage.delete(theObject);
        }
    }

    /**
     * Test for a complex boolean store(DataObject)
     */
    public void testComplexStore() throws Exception {
    	DataManager storage = getDataManager();

        DataObject   theObject = this.createComplexDO();
		try {
            store(storage, theObject);

            DataObject theObj2 = loadNotNull(theObject.tTable().getName(),
                                                   theObject.getIdentifier());

            assertEquals("Loaded wrong object (type is not equal)!", 
                              theObject.tTable().getName(), 
                              theObj2.tTable().getName());

            assertEquals("Loaded wrong object (IDs are not equal)!", 
                              theObject.getIdentifier(), 
                              theObj2.getIdentifier());

            theObject.setAttributeValue("text4", "short");
            theObject.setAttributeValue("text5", null);
            store(storage, theObject); // actually an update
            theObject.setAttributeValue("text4", LSTRING1);
            theObject.setAttributeValue("text5", LSTRING2);
            store(storage, theObject); // actually an update

            theObj2 = loadNotNull(theObject.tTable().getName(),
                                                   theObject.getIdentifier());

            this.inspectComplexDO(theObj2);

            theObject.setAttributeValue("text4", null);
            theObject.setAttributeValue("text5", "this is not really long");
            store(storage, theObject); // actually another update
            theObject.setAttributeValue("text4", LSTRING1);
            theObject.setAttributeValue("text5", LSTRING2);
            store(storage, theObject); // actually another update

            theObj2 = loadNotNull(theObject.tTable().getName(),
                                                   theObject.getIdentifier());

            this.inspectComplexDO(theObj2);
        }
        finally {
            storage.delete(theObject);
        }
    }

    /**
     * Test for some wild permutation of loads and stores.
     */
    public void testPermute() throws Exception {
        DataManager storage = getDataManager();

        int        count     = 100;

		assertNotNull(storage);
        
        String[]   theKeys   = new String[] 
            {"att1",  "att2"           , "att4" , "att5"   , "att6", "att7"       ,"att8"};
        Object[]   theValues = new Object[] 
		{ "val1", Long.valueOf(55555), LSTRING2, LSTRING1, null, getTestDate(), Float.valueOf(1.2345f) };
        Random     rand      = new Random(0x140765DEADBEAFL);
        DataObject theObject = new TestDataObject(theKeys, theValues);
        List       theList   = Arrays.asList(theValues);
        int        len       = theValues.length;
        String     moName    = theObject.tTable().getName();
		TLID theId = theObject.getIdentifier();
        
        try {
            startTime();
            store(storage, theObject);

            for (int i=0; i < count; i++) {
                theObject = loadNotNull(moName, theId);

                assertEquals(theId, theObject.getIdentifier());
                for (int j = 0; j < len; j++) {
                    Object val = theList.get(j);
                    if (val != null) {
                        Object otherVal = theObject.getAttributeValue(theKeys[j]);
                        if (val instanceof DataObject)
                            assertEquals(((DataObject) val     ).getIdentifier()
                                       , ((DataObject) otherVal).getIdentifier());
                        else 
                            assertEquals("At " + i, val, theObject.getAttributeValue(theKeys[j]));
                    }
                    else {
                        Object nullVal =  theObject.getAttributeValue(theKeys[j]);
                        assertEquals(val, nullVal);
                    }
                }
                
                Collections.shuffle(theList, rand);
                for (int j = 0; j < len; j++) {
                    theObject.setAttributeValue(theKeys[j], theList.get(j));
                }
                store(storage, theObject);
            }
            this.logTime("Shuffeled load/store " + count + " times ");
        }
        finally {
        	if (theObject != null) {
        		storage.delete(theObject);
        	}
        }
    }
    static String filledWith(int len, char c) {
        char[] fillin = new char[len];
        Arrays.fill(fillin, c);    
        return new String(fillin);
    }

    /**
     * Test using UTF-8 data which will need extra space in the DB.
     * 
     * This is a regression test for #1854.
     */
    public void testEncoding() throws Exception {

        String s128 = filledWith(128,'\u20ac'); // EURO 
        String s254 = filledWith(254,'\u00c4'); // A-Umlaut
        String s511 = filledWith(511,'\u00df'); // SZLig
        
        String[]   theKeys   = new String[] {"s128", "s254", "s511"};
        Object[]   theValues = new Object[] {s128, s254, s511};

        TestDataObject theDO   = new TestDataObject(theKeys, theValues);
        String         moName  = theDO.getMetaObjectName();
		TLID theID = theDO.getIdentifier();
        DataObject     readDO;

        DataManager storage = getDataManager();
        try {
                     store(storage, theDO);
            readDO = loadNotNull(moName,theID);

            assertEquals(s128      , readDO.getAttributeValue( "s128"));
            assertEquals(s254      , readDO.getAttributeValue( "s254"));
            assertEquals(s511      , readDO.getAttributeValue( "s511"));
        }
        finally {
            storage.delete(theDO);
        }

        
    }
    /**
     * Test for Strings with varyiing length (are handled special ...)
     */
    public void testStrings1() throws Exception {
    	DataManager storage = getDataManager();

        String[]   theKeys   = new String[] {"string"};
        Object[]   theValues = new Object[] {null};

        TestDataObject theDO   = new TestDataObject(theKeys, theValues);
        String         moName  = theDO.getMetaObjectName();
		TLID theID = theDO.getIdentifier();
        DataObject     readDO;

		try {
                     store(storage, theDO);
            assertNotInStore(moName,theID);

            theDO.setAttributeValue("string", "short String");
                     store(storage, theDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("short String", readDO.getAttributeValue("string"));

            theDO.setAttributeValue("string", LSTRING1);
                     store(storage, theDO);
            readDO = loadNotNull(moName,theID);
            assertEquals(LSTRING1, readDO.getAttributeValue("string"));

            theDO.setAttributeValue("string", "longer String");
                    store(storage, theDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("longer String", readDO.getAttributeValue("string"));

            theDO.setAttributeValue("string", LSTRING2);
                     store(storage, theDO);
            readDO = loadNotNull(moName,theID);
            assertEquals(LSTRING2, readDO.getAttributeValue("string"));

            theDO.setAttributeValue("string", "");
                     store(storage, theDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("", readDO.getAttributeValue("string"));

        }
        finally {
            storage.delete(theDO);
        }
    }

    /**
     * Test for Strings longer then 4K (for Our Frieds at Oracle)
     */
    public void testStrings4() throws Exception {
    	DataManager storage = getDataManager();

        for (int i=1024 ; i < 10240; i += 1024 ) {
            StringBuffer theBuf = new StringBuffer(i);
            while (theBuf.length() < i) {
                theBuf.append("A String that will be repeatead until reaching a multiple of 1024 Bytes");
            }
            String theString1 = theBuf.toString();
            String theString2 = "Modified + " + theString1;
            String[]   theKeys   = new String[] {"string"};
            Object[]   theValues = new Object[] {theString1};

            TestDataObject theDO   = new TestDataObject(theKeys, theValues);
            String         moName  = theDO.getMetaObjectName();
			TLID theID = theDO.getIdentifier();
            DataObject     readDO;
			try {
                         store(storage, theDO);
                readDO = loadNotNull(moName,theID);
    
                assertEquals(theString1, readDO.getAttributeValue("string"));
    
                theDO.setAttributeValue("string", theString2);
                         store(storage, theDO);
                readDO = loadNotNull(moName,theID);
                assertEquals(theString2, readDO.getAttributeValue("string"));
            }
            finally {
                storage.delete(theDO);
            }
        }
    }

    /**
     * Test for Strings with varyiing length (are handled special ...)
     */
    public void testStrings2() throws Exception {

        String[]   theKeys   = new String[] {"string"};
        Object[]   theValues = new Object[] {null};

        TestDataObject theDO   = new TestDataObject(theKeys, theValues);
        String         moName  = theDO.getMetaObjectName();
		TLID theID = theDO.getIdentifier();
        DataObject     readDO;

        try {
                     store(getDataManager(), theDO);
            assertNotInStore(moName, theID);
            readDO = getDataManager().createDataObject(moName,theID,1);

            readDO.setAttributeValue("string", "short String");
                     store(getDataManager(), readDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("short String", readDO.getAttributeValue("string"));

            readDO.setAttributeValue("string", LSTRING1);
                     store(getDataManager(), readDO);
            readDO = loadNotNull(moName,theID);
            assertEquals(LSTRING1, readDO.getAttributeValue("string"));

            readDO.setAttributeValue("string", "longer String");
                    store(getDataManager(), readDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("longer String", readDO.getAttributeValue("string"));

            readDO.setAttributeValue("string", LSTRING2);
                     store(getDataManager(), readDO);
            readDO = loadNotNull(moName,theID);
            assertEquals(LSTRING2, readDO.getAttributeValue("string"));

            readDO.setAttributeValue("string", "");
                     store(getDataManager(), readDO);
            readDO = loadNotNull(moName,theID);
            assertEquals("", readDO.getAttributeValue("string"));

        }
        finally {
            getDataManager().delete(theDO);
        }
    }

	private void assertNotInStore(String type, TLID id) throws Exception {
		DataObject readDO = getDataManager().load(type,id);
		assertNull("No object expected in store: " + id, readDO); // nothing to read, all null
	}

    /**
     * Test two Strings with varyiing length (special for MBA etc .)
     */
    public void testStrings3() throws Exception {

        String[]   theKeys   = new String[] {"string1", "string2"};
        Object[]   theValues = new Object[] {null, "not Null"};

        TestDataObject theDO   = new TestDataObject(theKeys, theValues);
        String         moName  = theDO.tTable().getName();
		TLID theID = theDO.getIdentifier();
        DataObject     readDO;

        DataManager storage = getDataManager();
		try {
                     store(storage, theDO);
            readDO = loadNotNull(moName, theID);
            {
                Object nullVal = readDO.getAttributeValue("string1");
                assertNull(nullVal);
            }
            assertEquals("not Null", readDO.getAttributeValue("string2"));

            readDO.setAttributeValue("string1", "short String");
            readDO.setAttributeValue("string2", null);
                     store(storage, readDO);
            readDO = loadNotNull(moName, theID);
            assertEquals("short String", readDO.getAttributeValue("string1"));
            assertNull(readDO.getAttributeValue("string2"));

            readDO.setAttributeValue("string1", LSTRING1);
            readDO.setAttributeValue("string2", LSTRING2);
                     store(storage, readDO);
            readDO = loadNotNull(moName, theID);
            assertEquals(LSTRING1, readDO.getAttributeValue("string1"));
            assertEquals(LSTRING2, readDO.getAttributeValue("string2"));

            readDO.setAttributeValue("string1", "longer String");
            readDO.setAttributeValue("string2", null);
                    store(storage, readDO);
            readDO = loadNotNull(moName, theID);
            assertEquals("longer String", readDO.getAttributeValue("string1"));
            assertNull(readDO.getAttributeValue("string2"));

            readDO.setAttributeValue("string1", LSTRING2);
            readDO.setAttributeValue("string2", LSTRING1);
                     store(storage, readDO);
            readDO = loadNotNull(moName, theID);
            assertEquals(LSTRING2, readDO.getAttributeValue("string1"));
            assertEquals(LSTRING1, readDO.getAttributeValue("string2"));

            readDO.setAttributeValue("string1", null);
            readDO.setAttributeValue("string2", "");
                     store(storage, readDO);
            readDO = loadNotNull(moName, theID);
            assertNull(readDO.getAttributeValue("string1"));
            assertEquals("", readDO.getAttributeValue("string2"));

        }
        finally {
            storage.delete(theDO);
        }
    }

	protected DataObject loadNotNull(String type, TLID id) throws Exception {
		DataObject readDO = getDataManager().load(type, id);
		assertNotNull("Failed to load TestDataObject: " + id, readDO);
		return readDO;
	}

    /** Tests massive store and retrieve Methods.
     */
    public void testMassStore() throws Exception{

		ArrayList<TLID> theNames = new ArrayList<>(DATA_OBJECT_COUNT);
        try {
            this.putDataObjects(theNames);
            this.getValues     (theNames);
            this.setValues     (theNames);
        }
        finally {
            this.deleteObjects(theNames);
        }
    }
    
    /** Test Store with a PreparedStatementCache and Rollback. */
    public void testStore2() throws Exception {
        DataObject             theObject = this.createComplexDO();
        DataManager  storage   = getDataManager();
        ConnectionPool connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PooledConnection connection = connectionPool.borrowWriteConnection();
		try {
			CommitContext commitContext = new CommitContextWrapper(connection);
	        String                 moName    = theObject.tTable().getName(); 
	        try {
	            storage.store(theObject, commitContext);
	            commitContext.getConnection().rollback();  // This never happend !
	            assertNull(storage.load(moName,theObject.getIdentifier()));
	
	            storage.store(theObject, commitContext);
	            commitContext.getConnection().commit();  // Now its done 
	            
	            DataObject theObj2 = loadNotNull(moName,theObject.getIdentifier());
	
	            assertEquals("Loaded wrong object (type is not equal)!", 
	                              theObject.tTable().getName(), 
	                              theObj2.tTable().getName());
	
	            assertEquals("Loaded wrong object (IDs are not equal)!", 
	                              theObject.getIdentifier(), 
	                              theObj2.getIdentifier());
	
	            theObject.setAttributeValue("text4", "short");
	            theObject.setAttributeValue("text5", null);
	            storage.store(theObject, commitContext); // actually an update
	            commitContext.getConnection().commit();
	
	            theObject.setAttributeValue("text4", LSTRING1);
	            theObject.setAttributeValue("text5", LSTRING2);
	            storage.store(theObject, commitContext); // actually an update
	            commitContext.getConnection().commit();
	                        
	            theObj2 = loadNotNull(theObject.tTable().getName(),
	                                                   theObject.getIdentifier());
	
	            this.inspectComplexDO(theObj2);
	
	            theObject.setAttributeValue("text4", null);
	            theObject.setAttributeValue("text5", "this is not really long");
	            storage.store(theObject, commitContext); // actually another update
	            theObject.setAttributeValue("text4", LSTRING1);
	            theObject.setAttributeValue("text5", LSTRING2);
	            storage.store(theObject, commitContext); // actually another update
	             commitContext.getConnection().commit();
	
	            theObj2 = loadNotNull(theObject.tTable().getName(),
	                                                   theObject.getIdentifier());
	            this.inspectComplexDO(theObj2);
	        }
	        finally {
	            commitContext.getConnection().rollback();
	            storage.delete(theObject,commitContext);
	        }
		} finally {
			connectionPool.releaseWriteConnection(connection);
		}
    }
    
    /** Setting and than re-Setting values in the returned DO.
     * 
     * This confuses the GenericDO implementation. This will test
     * all possible combinations with New, Changed, Deleted 
     * (as far as possible)
     * <pre>
     *      N-C-D    N-D-C
     *      C-N-D    C-D-N
     *      D-C-N    D-N-C</pre> 
     */
    public void testTransientChanges() throws Exception {
        String[]   theKeys   = new String[] {"NCD",  "NDC" , "CND" , "CDN" , "DCN" , "DNC"};
        Object[]   theValues = new Object[] {null ,  null  , "CND" , "CDN" , "DCN"  ,"DNC"};

        DataObject theDO = new TestDataObject(theKeys, theValues);

        DataManager  storage   = getDataManager();
        String                 moName    = theDO.tTable().getName(); 
        try {
            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            
            theDO.setAttributeValue("NCD", "new");
            theDO.setAttributeValue("NCD", "changed");
            theDO.setAttributeValue("NCD", null);

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertNull(theDO.getAttributeValue("NCD"));

            theDO.setAttributeValue("NDC", "new");
            theDO.setAttributeValue("NDC", null);
            theDO.setAttributeValue("NDC", "changed");

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertEquals("changed", theDO.getAttributeValue("NDC"));

            theDO.setAttributeValue("CND", null);
            theDO.setAttributeValue("CND", "new");
            theDO.setAttributeValue("CND", null);

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertNull(theDO.getAttributeValue("CND"));

            theDO.setAttributeValue("CDN", "changed");
            theDO.setAttributeValue("CDN", null);
            theDO.setAttributeValue("CDN", "new");

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertEquals("new", theDO.getAttributeValue("CDN"));

            theDO.setAttributeValue("DCN", null);
            theDO.setAttributeValue("DCN", "changed"); // actually new, well
            theDO.setAttributeValue("DCN", "new");     // actually changed, well

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertEquals("new", theDO.getAttributeValue("DCN"));

            // actually the same as DCN but for the sake of completeness
            theDO.setAttributeValue("DNC", null);
            theDO.setAttributeValue("DNC", "new");
            theDO.setAttributeValue("DNC", "changed");

            store(storage, theDO);
            
            theDO = loadNotNull(moName,theDO.getIdentifier());
            assertEquals("changed", theDO.getAttributeValue("DNC"));
        }
        finally {
            if (theDO != null)
                storage.delete(theDO);
        }
    }


    // protected helper functions
    
    /**
     * Put DATA_OBJECT_COUNT count objects into the storage. 
     */
	protected void putDataObjects(ArrayList<TLID> aNames)
        throws Exception {
    	int i = 0;
        DataObject theDataObjects[] = new DataObject[DATA_OBJECT_COUNT];
        while (i < DATA_OBJECT_COUNT){
            DataObject theDataObject    = this.createBigDO();
            theDataObjects[i++]         = theDataObject;
            aNames.add(theDataObject.getIdentifier());
        }
        i = 0;
        
        this.startTime();
        while (i < DATA_OBJECT_COUNT){
            store(getDataManager(), theDataObjects[i++]);
        }
        this.logTime("Storing of  " + DATA_OBJECT_COUNT + " DataObjects");
     }

    /**
     * Create a DataObject with 258 attributes.
     * 
     * 64 Strings, 64 Integer, 64 Long, 64, java.util.Date
     */
    protected ExampleDataObject createBigDO() throws Exception {
        Map theValues = new HashMap(256);                 
        for (int i = 0; i < 64; i++){
            String string3i = to3String(i);
            theValues.put("Key_"   + string3i,
                          "Value_" + string3i);
        }
        for (int i = 64; i < 128; i++){
			theValues.put("Key_" + to3String(i), Integer.valueOf(i));
        }
        for (int i = 128; i < 192; i++){
			theValues.put("Key_" + to3String(i), Long.valueOf(i));
        }
        Date theDate = getTestDate(); 
        for (int i = 192; i < 256; i++){
            theValues.put("Key_" + to3String(i),theDate);
        }
        
        return new TestDataObject(theValues);         
    }

    /** Make an int with three digits, (allows bteer view in MySQL) */
    protected static String to3String(int i) {
        String result = Integer.toString(i);
        int l = result.length();
        if (l < 3) 
            result = "000".substring(0, 3-l) + result;
        
        return result;
    }

    protected String getText(int aLength) {
        StringBuffer theBuffer = new StringBuffer();
        int          theMax    = aLength;

        while ((theBuffer.length() < theMax) && (aLength > 0)) {
            theBuffer.append(aLength--).append(' ');
        }

        return (theBuffer.toString().substring(0, theMax));
    }

    /**
     * Test for (some of) the Values inserted in <code>createBigDO</code>.
     */
	protected void getValues(ArrayList<TLID> someIDs) throws Exception {
    	Random rand = new Random(0x42422424L);
        
        this.startTime();
        for (int i=0; i < DATA_OBJECT_COUNT; i++) {
            int        theIndex = rand.nextInt(DATA_OBJECT_COUNT);
			TLID theId = someIDs.get(theIndex);
            DataObject theDO    = loadNotNull("TestDataObject" , theId);
            
            assertEquals("Value_005"    , theDO.getAttributeValue("Key_005"));
			assertEquals(Integer.valueOf(66), theDO.getAttributeValue("Key_066"));
			assertEquals(Long.valueOf(190), theDO.getAttributeValue("Key_190"));
            assertEquals(Date.class     , theDO.getAttributeValue("Key_254").getClass());
        }
        this.logTime("Loading  of " + DATA_OBJECT_COUNT + " DataObjects");
    }

    /*
     * Test setting (some of) the Values inserted in <code>createBigDO</code>.
     */
	protected void setValues(ArrayList<TLID> someIDs) throws Exception {
    	DataManager storage = getDataManager();
    	
    	Random rand     = new Random(0x42472424L);
        Date    theDate  = new Date(0x123456789ABCDEFL);
		Integer theInt = Integer.valueOf(1077);
		Long theLong = Long.valueOf(0x7777777777777777L);
        
        this.startTime();
        for (int i=0; i < DATA_OBJECT_COUNT; i++) {
            int        theIndex = rand.nextInt(DATA_OBJECT_COUNT);
			TLID theId = someIDs.get(theIndex);
			DataObject theDO    = loadNotNull("TestDataObject" , theId);
            
            theDO.setAttributeValue("Key_004", "Value_004");
            theDO.setAttributeValue("Key_007", "Value-007");
            theDO.setAttributeValue("Key_017", null);
            theDO.setAttributeValue("Key_077", theInt);
            theDO.setAttributeValue("Key_177", theLong);
            theDO.setAttributeValue("Key_207", theDate);
            
            store(storage, theDO);
            theDO    = loadNotNull("TestDataObject" , theId);
            
            assertEquals("Value-007"    , theDO.getAttributeValue("Key_007"));
            assertNull(theDO.getAttributeValue("Key_017"));
            assertEquals(theInt         , theDO.getAttributeValue("Key_077"));
            assertEquals(theLong        , theDO.getAttributeValue("Key_177"));
            assertEquals(theDate        , theDO.getAttributeValue("Key_207"));
        }
        this.logTime("Storing  of " + DATA_OBJECT_COUNT + " DataObjects");
    }

    /*
     * Test setting (some of) the Values inserted in <code>createBigDO</code>.
     */
	protected void deleteObjects(ArrayList<TLID> someIDs) throws Exception {
    	DataManager storage = getDataManager();
        this.startTime();
        for (int i=0; i < DATA_OBJECT_COUNT; i++) {
			TLID theId = someIDs.get(i);
            DataObject theDO    = storage.load("TestDataObject" , theId);
            if (theDO != null) {
            	storage.delete(theDO);
            }
        }
        this.logTime("Deleting of " + DATA_OBJECT_COUNT + " DataObjects");
    }


    /** Create a simple DataObject for testing */
    protected ExampleDataObject createSimpleDO() throws Exception {
        String[] theKeys   = new String[] {"string1", "date2", "integer3",        "null", "true", "false" };
        Object[] theValues = new Object[] {"val1", getTestDate(), Integer.valueOf(42), null , Boolean.TRUE, Boolean.FALSE};

        return (new TestDataObject(theKeys, theValues));
    }

    /** Create a complex DataObject for testing */
    protected ExampleDataObject createComplexDO() throws Exception {
        DataObject theDO     = this.createSimpleDO();
        String[]   theKeys   = new String[] {"string1",  "long2"       , "do3" , "text4" , "text5"};
        Object[]   theValues = new Object[] {"val1"   , Long.valueOf(55555),  theDO, LSTRING1, LSTRING2};

        return (new TestDataObject(theKeys, theValues));
    }

    /** validate the content of the Object create in createSimpleDO() */
    protected void inspectSimpleDO(DataObject anObject) throws Exception {
        Object theValue = anObject.getAttributeValue("string1");

        assertNotNull("No attribute of type String loaded!", theValue);
        assertTrue("Encapsulated object is no String, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof String);

        theValue = anObject.getAttributeValue("date2");

        assertNotNull("No attribute of type Date loaded!", theValue);
        assertTrue("Encapsulated object is no Date, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof Date);

        theValue = anObject.getAttributeValue("integer3");

        assertNotNull("No attribute of type Integer loaded!", theValue);
        assertTrue("Encapsulated object is no Integer, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof Integer);

        assertEquals(Boolean.TRUE , anObject.getAttributeValue("true"));
        assertEquals(Boolean.FALSE, anObject.getAttributeValue("false"));
    }

    /** validate the content of the Object create in createComplexDO() */
    protected void inspectComplexDO(DataObject anObject) throws Exception {
        Object theValue = anObject.getAttributeValue("string1");

        assertNotNull("No attribute of type String loaded!", theValue);
        assertTrue("Encapsulated object is no String, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof String);

        theValue = anObject.getAttributeValue("long2");

        assertNotNull("No attribute of type Long loaded!", theValue);
        assertTrue("Encapsulated object is no Long, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof Long);

        theValue = anObject.getAttributeValue("do3");

        assertNotNull("No encapsulated DataObject loaded!", theValue);
        assertTrue("Encapsulated object is no DataObject, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof DataObject);

        this.inspectSimpleDO((DataObject) theValue);

        theValue = anObject.getAttributeValue("text4");

        assertTrue("Encapsulated object is no string, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof String);
        assertEquals(LSTRING1, theValue);

        theValue = anObject.getAttributeValue("text5");

        assertTrue("Encapsulated object is no string, but " + 
                        theValue.getClass().getName() + "!", 
                        theValue instanceof String);
        assertEquals(LSTRING2, theValue);
    }

	protected static class TestDataObject extends ExampleDataObject {
        
        /** A unique identifier based on idCount */
		private final TLID identifier = DummyIDFactory.createId();
        
        public TestDataObject(Map aMap) {
            super(aMap);
        }

        public TestDataObject(String[] someKeys, Object[] someValues) {
            super(someKeys, someValues);
        }

		/**
		 * Returns the identifier as {@link String} representation for testing.
		 */
        @Override
		public String toString () {
			return IdentifierUtil.toExternalForm(identifier);
        }

        @Override
		public String getMetaObjectName() {
            return ("TestDataObject");
        }
        
        @Override
		public TLID getIdentifier() {
            return identifier;
        }

    }

	protected static Test suite(Class testCase, DataManagerFactory dataManagerFactory, TestFactory testFactory) {
		return DOBTestSetup.createDOBTestSetup(DataManagerTestSetup.getDMTest(testCase, dataManagerFactory, testFactory));
	}

	public static void store(DataManager dataManager, DataObject anObject) throws SQLException {
		assertTrue(dataManager.store(anObject));
	}

}
