/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.io.binary.TestFileBasedBinaryData;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;
import test.com.top_logic.knowledge.service.TestingCommittable;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.util.WrapperUtil;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Testcase for the {@link AbstractBoundWrapper}.
 *
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestFlexWrapper extends BasicTestCase {

    /** The number of Objects used to test mass Storage */
    public static final int MASS_STORAGE = 64;

    static final BinaryData SMALL_BINARY_DATA = BinaryDataFactory.createBinaryData("DummelDieDabbel".getBytes());

	static final File LONG_FILE =
		new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/knowledge/wrap/TestFlexWrapper.java");

	static final BinaryData LONG_BINARY_DATA = BinaryDataFactory.createBinaryData(LONG_FILE);
 
    /**
     * Constructor for TestFlexWrapper.
     *
     * @param name name of the test to execute
     */
    public TestFlexWrapper(String name) {
        super(name);
    }

	public void testChangeHistoricValue() throws DataObjectException {
		KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		if (!theKB.getHistoryManager().hasHistory()) {
			/* For compatibility with unversioning KnowledgeBase: This test is quite senseless when
			 * no history is available. */
			return;
		}
		FlexTestC c1;
		Revision createRevision;
		{
			Transaction createTX = theKB.beginTransaction();
			KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);
			c1 = (FlexTestC) WrapperFactory.getWrapper(theKO);
			c1.setValue("flexAttr", "value1");
			createTX.commit();
			createRevision = createTX.getCommitRevision();
		}

		Revision trafficRevision;
		{
			Transaction createTrafficTX = theKB.beginTransaction();
			theKB.createKnowledgeObject(KBTestMeta.TEST_C);
			createTrafficTX.commit();
			trafficRevision = createTrafficTX.getCommitRevision();
		}

		Transaction changeTX = theKB.beginTransaction();
		c1.setValue("flexAttr", "value2");
		changeTX.commit();

		{
			/* check change of historic wrapper that was changed in its revision can not be
			 * persistent */
			Wrapper historicWrapper = WrapperHistoryUtils.getWrapper(createRevision, c1);

			assertEquals("value1", historicWrapper.getValue("flexAttr"));
			try {
				Transaction historicChangeTX = theKB.beginTransaction();
				historicWrapper.setValue("flexAttr", "value3");
				historicChangeTX.commit();
				assertEquals("Historic value changed", "value1", historicWrapper.getValue("flexAttr"));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* check change of historic wrapper that was not changed in its revision can not be
			 * persistent */
			Wrapper historicWrapper = WrapperHistoryUtils.getWrapper(trafficRevision, c1);

			assertEquals("value1", historicWrapper.getValue("flexAttr"));
			try {
				Transaction historicChangeTX = theKB.beginTransaction();
				historicWrapper.setValue("flexAttr", "value3");
				historicChangeTX.commit();
				assertEquals("Historic value changed", "value1", historicWrapper.getValue("flexAttr"));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* Check change of transient changes in historic objects */
			Wrapper historicWrapper = WrapperHistoryUtils.getWrapper(trafficRevision, c1);
			assertEquals("value1", historicWrapper.getValue("flexAttr"));
			try {
				historicWrapper.setValue("flexAttr", "value3");
				assertEquals("Historic value changed", "value1", historicWrapper.getValue("flexAttr"));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* Check adding flex attributes to historic wrapper */
			Wrapper historicWrapper = WrapperHistoryUtils.getWrapper(trafficRevision, c1);
			assertNull(historicWrapper.getValue("newFlexAttr"));
			try {
				Transaction historicChangeTX = theKB.beginTransaction();
				historicWrapper.setValue("newFlexAttr", "value");
				historicChangeTX.commit();
				assertNull("Historic value changed", historicWrapper.getValue("newFlexAttr"));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* Check changing non flex value historic wrapper */
			Wrapper historicWrapper = WrapperHistoryUtils.getWrapper(trafficRevision, c1);
			assertNull(historicWrapper.getValue(KBTestMeta.TEST_C_NAME));
			try {
				Transaction historicChangeTX = theKB.beginTransaction();
				historicWrapper.setValue(KBTestMeta.TEST_C_NAME, "value");
				historicChangeTX.commit();
				assertNull("Historic value changed", historicWrapper.getValue(KBTestMeta.TEST_C_NAME));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
	}

    public void testDeleteObject() throws Exception {
        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);
		FlexTestC c1 = (FlexTestC) WrapperFactory.getWrapper(theKO);

        c1.setValue("attr1"   , "value1");
        c1.setValue("attr2"   , "value2");
        
        assertTrue (theKB.commit());
        
        c1.setValue("attr1"   , null);
        c1.setValue("attr2"   , "new value2");
        c1.setValue("attr3"   , "value3");

        assertTrue (theKB.commit());

        // Make sure that cache is dropped. This could provoke problems during delete.
        c1.refetchWrapper();
        
        c1.tDelete();

        assertTrue (theKB.commit());
    }
    
    /**
     * Regression for #1815
     */
    public void testDeleteEmptyFlexWrapper() throws Exception {
        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);
		FlexTestC c1 = (FlexTestC) WrapperFactory.getWrapper(theKO);

        c1.setValue("attr1"   , "value1");
        c1.setValue("attr2"   , Integer.valueOf(42));
        
        assertTrue (theKB.commit());
        
        c1.setValue("attr1"   , null);
        c1.setValue("attr2"   , null);

        assertTrue (theKB.commit());

        c1.tDelete();
        
        assertTrue (theKB.getName(), theKB.commit());
    }

    
    public void testDeleteNewObject() throws Exception {
    	KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);
		FlexTestC c1 = (FlexTestC) WrapperFactory.getWrapper(theKO);
    	
    	c1.setValue("attr1"   , "value1");
    	c1.setValue("attr2"   , "value2");
    	
    	c1.tDelete();
    	
    	assertTrue (theKB.commit());
    }
    
    public void testDeletedNotEqualsNew() throws Exception {
    	KnowledgeBase   kb = KBSetup.getKnowledgeBase();
    	
    	boolean commitNew = false;
    	
    	// Create object and delete it.
		KnowledgeObject c1ko = kb.createKnowledgeObject(KBTestMeta.TEST_C);
		FlexTestC c1 = (FlexTestC) WrapperFactory.getWrapper(c1ko);
		TLID c1Name = KBUtils.getObjectName(c1ko);
    	
    	c1.setValue("attr1"   , "value1");
    	c1.setValue("attr2"   , "value2");

		if (commitNew) {
    		assertTrue(kb.commit());
    	}
    	
    	int c1HashBeforeDelete = c1.hashCode();
    	
    	c1.tDelete();
    	
    	assertEquals("Hash code stable during delete.", c1HashBeforeDelete, c1.hashCode());
    	
    	// Re-allocate object with the same (simulating a well-known) ID.
    	KnowledgeObject c1NewKo = kb.createKnowledgeObject(c1Name, KBTestMeta.TEST_C);
		FlexTestC c1New = (FlexTestC) WrapperFactory.getWrapper(c1NewKo);
    	
    	int c1NewHashBeforeCommit = c1New.hashCode();
    	
    	assertTrue(kb.commit());
    	
    	assertEquals("Hash code stable during commit.", c1NewHashBeforeCommit, c1New.hashCode());
    }
    
    /** Check how rollback is handled. */
    public void testRollback() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

		Wrapper theWrapper = WrapperFactory.getWrapper(theKO);
        FlexTestC myDoc      = (FlexTestC) theWrapper;
        myDoc.setValue("flexValue"   , "wont survive");
        assertEquals  ("wont survive", myDoc.getValue("flexValue"));
        assertTrue (theKB.rollback());
		assertFalse(theKO.isAlive());

    }

    /** Check how a failed commitis handled. */
    public void testFailedCommit() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

		Wrapper theWrapper = WrapperFactory.getWrapper(theKO);
        FlexTestC myDoc      = (FlexTestC) theWrapper;
        assertTrue(theKB.commit());

        TestingCommittable tc = new TestingCommittable(
                /* allowPrep */ true, /* allowDel */ true, /* allowCom */ false, /* allowRol*/ false);
        try {
            myDoc.setValue("flexValue"   , "wont survive");
            assertEquals  ("wont survive", myDoc.getValue("flexValue"));

            ((CommitHandler)theKB).addCommittable(tc);

            assertFalse (theKB.commit());
            assertNull(myDoc.getValue("flexValue"));
            assertTrue (tc.wasPrepared);
            // assertTrue(tc.wasCommited);
            // cannot assert as rollback may happen later
            assertFalse(tc.wasCompleted);
            assertFalse(tc.wasDeleted);
            assertTrue (tc.wasRolledBack);
        } finally {
            myDoc.tDelete();
            theKB.commit();
        }

    }

    /** Check if reading a Flex Attribute result in a change in Object.*/
    public void testReadOnly() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

		Wrapper theWrapper = WrapperFactory.getWrapper(theKO);
        FlexTestC myDoc      = (FlexTestC) theWrapper;
        assertNull(myDoc.getValue("flexValue"));
        assertTrue (theKB.rollback());
    }

    /** Test handling on commit(). */
    public void testCommit() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        myDoc.setValue("flexValue", "to be commited");
        assertEquals("to be commited", myDoc.getValue("flexValue"));
        TestingCommittable tc = new TestingCommittable(/* allowPrep */ true,
                /* allowDel */ true, /* allowCom */ true, /* allowRol*/ true);
        ((CommitHandler)theKB).addCommittable(tc);

        assertTrue  (theKB.commit());
        assertEquals("to be commited", myDoc.getValue ("flexValue"));
        assertEquals("to be commited", myDoc.getString("flexValue"));

        // some generic Tests included here
        assertTrue(myDoc.getAllAttributeNames().contains("flexValue"));

        assertEquals( KBTestMeta.TEST_C, WrapperUtil.getMetaObjectName(myDoc));

        myDoc.tDelete();
        tc = new TestingCommittable(/* allowPrep */ true,
                /* allowDel */ true, /* allowCom */ true, /* allowRol*/ true);
        ((CommitHandler)theKB).addCommittable(tc);

        assertTrue (theKB.commit());

        assertTrue (tc.wasPrepared);
        assertTrue (tc.wasCommited);
        assertTrue (tc.wasCompleted);
        assertFalse(tc.wasDeleted);
        assertFalse(tc.wasRolledBack);
    }

    /** Test Using {@link BinaryData} */
    public void testBinaryData() throws Exception {
        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        myDoc.setValue("flexSmall", SMALL_BINARY_DATA);
        myDoc.setValue("flexLong" , LONG_BINARY_DATA);

        assertTrue  (theKB.commit());

        assertEquals(SMALL_BINARY_DATA, myDoc.getValue("flexSmall"));
        assertEquals(LONG_BINARY_DATA , myDoc.getValue("flexLong"));
        
        assertEquals( KBTestMeta.TEST_C, WrapperUtil.getMetaObjectName(myDoc));

        myDoc.tDelete();
        assertTrue (theKB.commit());
    }
    
    /** Test Using {@link BinaryData} assuming the original (Files) get Lost. */
    public void testLostBinaryData() throws Exception {
        File                tmpFile        = createNamedTestFile("testLostBinaryData.java");
        FileUtilities.copyFile(LONG_FILE, tmpFile);
		BinaryData tmpBinaryData = BinaryDataFactory.createBinaryData(tmpFile);
        
        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        myDoc.setValue("flexSmall", SMALL_BINARY_DATA);
        myDoc.setValue("flexLong" , tmpBinaryData);

        assertTrue  (theKB.commit());
        
        tmpFile.delete(); // break tmpBinaryData for testing

        assertEquals(SMALL_BINARY_DATA, myDoc.getValue("flexSmall"));
        assertEquals(LONG_BINARY_DATA , myDoc.getValue("flexLong"));
        
        TestFileBasedBinaryData.cleanupFiles();

        assertEquals(SMALL_BINARY_DATA, myDoc.getValue("flexSmall"));
        assertEquals(LONG_BINARY_DATA , myDoc.getValue("flexLong"));

        assertEquals( KBTestMeta.TEST_C, WrapperUtil.getMetaObjectName(myDoc));

        myDoc.tDelete();
        assertTrue (theKB.commit());
    }

    /** Test handling on (simulated, external) refetch. */
    public void testRefetchWrapper() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "mustBeSet");

		FlexTestC wrapper = (FlexTestC) WrapperFactory.getWrapper(theKO);
        String dynamicAttribute = "flexValue";
        String initialValue = "to be refetched";
        
		wrapper.setString(dynamicAttribute, initialValue);
        assertEquals(initialValue, wrapper.getValue(dynamicAttribute));
        
        wrapper.refetchWrapper();
        
        // The not yet committed local modification are still there. 
        assertEquals(initialValue, wrapper.getValue(dynamicAttribute));
        
        // Make the changes persistent.
        assertTrue(theKB.commit());
        
        wrapper.refetchWrapper();
        
        assertEquals(initialValue, wrapper.getValue(dynamicAttribute));
        
        wrapper.tDelete();
        
        assertTrue(theKB.rollback());
        
        // Implementation detail which no longer holds. After rollback, everything is still in place.
        //
        // assertEquals(null, wrapper.getValue(dynamicAttribute));

		wrapper.setString(dynamicAttribute, "new value");
        
        assertTrue(theKB.rollback());
        
        assertEquals(initialValue, wrapper.getValue(dynamicAttribute));
        
        
        // No longer holds: Must reget it since wrapper is invalid
		wrapper = (FlexTestC) WrapperFactory.getWrapper(theKO);
        
        wrapper.tDelete();
        assertTrue(theKB.commit());
        // assertEquals(null, myDoc.getValue("flexValue")); // gives nasty ERROR
    }

    public void testGetModified() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "mustBeSet");
        
		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        assertTrue(theKB.commit());

        Date creationDate = myDoc.getModified();
        assertNotNull("No modified attribute found.", creationDate);
        
        Thread.sleep(500);
        
        myDoc.setString("flexString", "testString");
        
        assertTrue(theKB.commit());
        
        Thread.sleep(500);
        
        Date modification1Date = myDoc.getModified();
        
        assertTrue(modification1Date.compareTo(creationDate) > 0);
        
        Thread.sleep(500);
        
        // reading is not modifying. getModified should still be the same, even with commit
        myDoc.getString("flexString");
        
        assertTrue(theKB.commit());
        Date modification2Date = myDoc.getModified();
        
        assertEquals(modification1Date, modification2Date);
        
        Thread.sleep(500);
        
        // one more change and commit...
        myDoc.setDate("flexDate", new Date());
        
        assertTrue(theKB.commit());
        
        Date modification3Date = myDoc.getModified();
        
        // there was a commit between the modification -> d1 < d2
        assertTrue(modification3Date.compareTo(modification2Date) > 0);
        
    }
    
    /**
	 * Check {@link AbstractBoundWrapper#getAllAttributeNames()} without DataObject and more.
	 */
    public void testGetAllAttributeNames() throws Exception {
        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        
        // else-Part: DataObject == null
        // getAllAttributeNames with an FlexWrapper with no DataObject will return a List of all Attribute-Names
        Set<String> gottenList = myDoc.getAllAttributeNames();
        
        List<String> myList = Arrays.asList(myDoc.tHandle().getAttributeNames());

        assertEquals(new HashSet<>(myList), gottenList);
        
        // if-Part: DataObject != null
        myDoc.setString("testString1", "testValue1");
        myDoc.setDate("testDate1", new Date());
        
        gottenList = myDoc.getAllAttributeNames();
        
        assertTrue(gottenList.contains("testString1"));
        assertTrue(gottenList.contains("testDate1"));
        assertTrue(gottenList.containsAll(myList));
        
        assertTrue  ("Failed to commit with KB: "+theKB.getName(), theKB.commit());
    }
    
    /** Simple Test for the Getters and Setters */
    public void testGetSet() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

        Date    aDate   = new Date();
		Float aFloat = Float.valueOf(Float.MAX_VALUE);
		Integer anInt1 = Integer.valueOf(Integer.MAX_VALUE);
		Integer anInt2 = Integer.valueOf(Integer.MIN_VALUE);
		Long aLong1 = Long.valueOf(Long.MAX_VALUE);
		Long aLong2 = Long.valueOf(Long.MIN_VALUE);
		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        myDoc.setBoolean("boolValue"    , true);
        myDoc.setBoolean("BooleanValue" , Boolean.FALSE);
        myDoc.setDate   ("DateValue"    , aDate);
        myDoc.setFloat  ("FloatValue"   , aFloat);
        myDoc.setInteger("intValue"     , anInt1.intValue());
        myDoc.setInteger("IntgerValue"  , anInt2);
        myDoc.setLong   ("longValue1"   , aLong1.longValue());
        myDoc.setLong   ("LongValue2"   , aLong2);

        assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
        assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt1.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt1             ,myDoc.getInteger("intValue"));
        assertEquals(anInt2             ,myDoc.getInteger("IntgerValue"));
        // cannot use "longValue" / "LongValue" with MySQL
        assertEquals(aLong1.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong1             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong2             ,myDoc.getLong   ("LongValue2"));

        assertTrue  (theKB.commit());

        assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
        assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt1.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt1             ,myDoc.getInteger("intValue"));
        assertEquals(aLong1.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong1             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong2             ,myDoc.getLong   ("LongValue2"));

        Set<String> allAttributeNames = myDoc.getAllAttributeNames();
        assertTrue(allAttributeNames.contains("boolValue"));
        assertTrue(allAttributeNames.contains("BooleanValue"));
        assertTrue(allAttributeNames.contains("DateValue"));
        assertTrue(allAttributeNames.contains("FloatValue"));
        assertTrue(allAttributeNames.contains("intValue"));
        assertTrue(allAttributeNames.contains("IntgerValue"));
        assertTrue(allAttributeNames.contains("longValue1"));
        assertTrue(allAttributeNames.contains("LongValue2"));
        assertTrue(allAttributeNames.contains(KBTestMeta.TEST_C_NAME));

        myDoc.tDelete();
        assertTrue (theKB.commit());
    }

    /** Test The Getters and Setters (twice). */
    public void testGetResSet() throws Exception {

        KnowledgeBase   theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = theKB.createKnowledgeObject(KBTestMeta.TEST_C);

        theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");

        Date    aDate1   = new Date();
		Float aFloat1 = Float.valueOf(Float.MAX_VALUE);
		Integer anInt11 = Integer.valueOf(Integer.MAX_VALUE);
		Integer anInt12 = Integer.valueOf(Integer.MIN_VALUE);
		Long aLong11 = Long.valueOf(Long.MAX_VALUE);
		Long aLong12 = Long.valueOf(Long.MIN_VALUE);

        Date    aDate2   = new Date();
		Float aFloat2 = Float.valueOf((float) Math.PI);
		Integer anInt21 = Integer.valueOf(0xDEADBEAF);
		Integer anInt22 = Integer.valueOf(9999999);
		Long aLong21 = Long.valueOf(7777L);
        Long    aLong22  = null;

		FlexTestC myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
        myDoc.setBoolean("boolValue"    , true);
        myDoc.setBoolean("BooleanValue" , Boolean.FALSE);
        myDoc.setDate   ("DateValue"    , aDate1);
        myDoc.setFloat  ("FloatValue"   , aFloat1);
        myDoc.setInteger("intValue"     , anInt11.intValue());
        myDoc.setInteger("IntgerValue"  , anInt12);
        myDoc.setLong   ("longValue1"   , aLong11.longValue());
        myDoc.setLong   ("LongValue2"   , aLong12);

        assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
        assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate1              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat1             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt11.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt11             ,myDoc.getInteger("intValue"));
        assertEquals(anInt12             ,myDoc.getInteger("IntgerValue"));
        assertEquals(aLong11.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong11             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong12             ,myDoc.getLong   ("LongValue2"));

        assertTrue  (theKB.commit());

		myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);

        assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
        assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate1              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat1             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt11.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt11             ,myDoc.getInteger("intValue"));
        assertEquals(anInt12             ,myDoc.getInteger("IntgerValue"));
        // cannnoit use "longValue" / "LongValue" with MySQL
        assertEquals(aLong11.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong11             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong12             ,myDoc.getLong   ("LongValue2"));

        myDoc.setBoolean("boolValue"    , false);
        myDoc.setBoolean("BooleanValue" , Boolean.TRUE);
        myDoc.setDate   ("DateValue"    , aDate2);
        myDoc.setFloat  ("FloatValue"   , aFloat2);
        myDoc.setInteger("intValue"     , anInt21.intValue());
        myDoc.setInteger("IntgerValue"  , anInt22);
        myDoc.setLong   ("longValue1"   , aLong21.longValue());
        myDoc.setLong   ("LongValue2"   , aLong22);

        assertEquals(Boolean.FALSE       ,myDoc.getBoolean("boolValue"));
        assertEquals(false               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.TRUE        ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate2              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat2             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt21.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt21             ,myDoc.getInteger("intValue"));
        assertEquals(anInt22             ,myDoc.getInteger("IntgerValue"));
        // cannnot use "longValue" / "LongValue" with MySQL
        assertEquals(aLong21.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong21             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong22             ,myDoc.getLong   ("LongValue2"));

        assertTrue  (theKB.commit());

        assertEquals(Boolean.FALSE       ,myDoc.getBoolean("boolValue"));
        assertEquals(false               ,myDoc.getBooleanValue("boolValue"));
        assertEquals(Boolean.TRUE        ,myDoc.getBoolean("BooleanValue"));
        assertEquals(aDate2              ,myDoc.getDate   ("DateValue"));
        assertEquals(aFloat2             ,myDoc.getFloat  ("FloatValue"));
        assertEquals(anInt21.intValue()  ,myDoc.getInt    ("intValue"));
        assertEquals(anInt21             ,myDoc.getInteger("intValue"));
        assertEquals(anInt22             ,myDoc.getInteger("IntgerValue"));
        // cannnoit use "longValue" / "LongValue" with MySQL
        assertEquals(aLong21.longValue() ,myDoc.getLongValue("longValue1"));
        assertEquals(aLong21             ,myDoc.getLong   ("longValue1"));
        assertEquals(aLong22             ,myDoc.getLong   ("LongValue2"));

        Set<String> allAttributeNames = myDoc.getAllAttributeNames();
        assertTrue(allAttributeNames.contains("boolValue"));
        assertTrue(allAttributeNames.contains("BooleanValue"));
        assertTrue(allAttributeNames.contains("DateValue"));
        assertTrue(allAttributeNames.contains("FloatValue"));
        assertTrue(allAttributeNames.contains("intValue"));
        assertTrue(allAttributeNames.contains("IntgerValue"));
        assertTrue(allAttributeNames.contains("longValue1"));
        assertTrue(allAttributeNames.contains(KBTestMeta.TEST_C_NAME));

        myDoc.tDelete();
        assertTrue (theKB.commit());
    }

    /** Test Mass Storage of FlexWrapper. */
    public void testMassStorage() throws Exception {

        KnowledgeBase   theKB    = KBSetup.getKnowledgeBase();
        KnowledgeObject theKOs[] = new KnowledgeObject[MASS_STORAGE];
        KnowledgeObject theKO;
        FlexTestC      myDoc;

        Date    aDate1   = new Date();
		Float aFloat1 = Float.valueOf(Float.MAX_VALUE);
		Integer anInt11 = Integer.valueOf(Integer.MAX_VALUE);
		Integer anInt12 = Integer.valueOf(Integer.MIN_VALUE);
		Long aLong11 = Long.valueOf(Long.MAX_VALUE);
		Long aLong12 = Long.valueOf(Long.MIN_VALUE);

        Date    aDate2   = new Date();
		Float aFloat2 = Float.valueOf((float) Math.PI);
		Integer anInt21 = Integer.valueOf(0xDEADBEAF);
		Integer anInt22 = Integer.valueOf(9999999);
		Long aLong21 = Long.valueOf(7777L);
		Long aLong22 = Long.valueOf(8888L);

        for (int i=0; i< MASS_STORAGE; i++) {
            theKOs[i] = theKO =
				theKB.createKnowledgeObject(KBTestMeta.TEST_C);
            theKO.setAttributeValue(KBTestMeta.TEST_C_NAME, "must not be null");
			myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);

            myDoc.setBoolean("boolValue"    , true);
            myDoc.setBoolean("BooleanValue" , Boolean.FALSE);
            myDoc.setDate   ("DateValue"    , aDate1);
            myDoc.setFloat  ("FloatValue"   , aFloat1);
            myDoc.setInteger("intValue"     , anInt11.intValue());
            myDoc.setInteger("IntgerValue"  , anInt12);
            myDoc.setLong   ("longValue1"   , aLong11.longValue());
            myDoc.setLong   ("LongValue2"   , aLong12);

            assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
            assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
            assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
            assertEquals(aDate1              ,myDoc.getDate   ("DateValue"));
            assertEquals(aFloat1             ,myDoc.getFloat  ("FloatValue"));
            assertEquals(anInt11.intValue()  ,myDoc.getInt    ("intValue"));
            assertEquals(anInt11             ,myDoc.getInteger("intValue"));
            assertEquals(anInt12             ,myDoc.getInteger("IntgerValue"));
            assertEquals(aLong11.longValue() ,myDoc.getLongValue("longValue1"));
            assertEquals(aLong11             ,myDoc.getLong   ("longValue1"));
            assertEquals(aLong12             ,myDoc.getLong   ("LongValue2"));
        }

        assertTrue  (theKB.commit());

        for (int i=0; i< MASS_STORAGE; i++) {
            theKO = theKOs[i];
			myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);
            // String msg = "For: " + theKO.getIdentifier();
            assertEquals(Boolean.TRUE       ,myDoc.getBoolean("boolValue"));
            assertEquals(true               ,myDoc.getBooleanValue("boolValue"));
            assertEquals(Boolean.FALSE      ,myDoc.getBoolean("BooleanValue"));
            assertEquals(aDate1              ,myDoc.getDate   ("DateValue"));
            assertEquals(aFloat1             ,myDoc.getFloat  ("FloatValue"));
            assertEquals(anInt11.intValue()  ,myDoc.getInt    ("intValue"));
            assertEquals(anInt11             ,myDoc.getInteger("intValue"));
            assertEquals(anInt12             ,myDoc.getInteger("IntgerValue"));
            assertEquals(aLong11.longValue() ,myDoc.getLongValue("longValue1"));
            assertEquals(aLong11             ,myDoc.getLong   ("longValue1"));
            assertEquals(aLong12             ,myDoc.getLong   ("LongValue2"));

            myDoc.setBoolean("boolValue"    , false);
            myDoc.setBoolean("BooleanValue" , Boolean.TRUE);
            myDoc.setDate   ("DateValue"    , aDate2);
            myDoc.setFloat  ("FloatValue"   , aFloat2);
            myDoc.setInteger("intValue"     , anInt21.intValue());
            myDoc.setInteger("IntgerValue"  , anInt22);
            myDoc.setLong   ("longValue1"   , aLong21.longValue());
            myDoc.setLong   ("LongValue2"   , aLong22);

            assertEquals(Boolean.FALSE       ,myDoc.getBoolean("boolValue"));
            assertEquals(false               ,myDoc.getBooleanValue("boolValue"));
            assertEquals(Boolean.TRUE        ,myDoc.getBoolean("BooleanValue"));
            assertEquals(aDate2              ,myDoc.getDate   ("DateValue"));
            assertEquals(aFloat2             ,myDoc.getFloat  ("FloatValue"));
            assertEquals(anInt21.intValue()  ,myDoc.getInt    ("intValue"));
            assertEquals(anInt21             ,myDoc.getInteger("intValue"));
            assertEquals(anInt22             ,myDoc.getInteger("IntgerValue"));
            // cannnot use "longValue" / "LongValue" with MySQL
            assertEquals(aLong21.longValue() ,myDoc.getLongValue("longValue1"));
            assertEquals(aLong21             ,myDoc.getLong   ("longValue1"));
            assertEquals(aLong22             ,myDoc.getLong   ("LongValue2"));
        }
        assertTrue  (theKB.commit());

        for (int i=0; i< MASS_STORAGE; i++) {
            theKO = theKOs[i];
			myDoc = (FlexTestC) WrapperFactory.getWrapper(theKO);

            assertEquals(Boolean.FALSE       ,myDoc.getBoolean("boolValue"));
            assertEquals(false               ,myDoc.getBooleanValue("boolValue"));
            assertEquals(Boolean.TRUE        ,myDoc.getBoolean("BooleanValue"));
            assertEquals(aDate2              ,myDoc.getDate   ("DateValue"));
            assertEquals(aFloat2             ,myDoc.getFloat  ("FloatValue"));
            assertEquals(anInt21.intValue()  ,myDoc.getInt    ("intValue"));
            assertEquals(anInt21             ,myDoc.getInteger("intValue"));
            assertEquals(anInt22             ,myDoc.getInteger("IntgerValue"));
            // cannnot use "longValue" / "LongValue" with MySQL
            assertEquals(aLong21.longValue() ,myDoc.getLongValue("longValue1"));
            assertEquals(aLong21             ,myDoc.getLong   ("longValue1"));
            assertEquals(aLong22             ,myDoc.getLong   ("LongValue2"));

            myDoc.tDelete();
        }
        assertTrue (theKB.commit());
    }

    /**
     * Return the suite of tests to perform.
     */
    public static Test suite () {
		// Start MimeTypes to get content type of FileBasedBinaryData based on a File.
		TestFactory testFactory = ServiceTestSetup.createStarterFactory(MimeTypes.Module.INSTANCE);
		return KBSetup.getSingleKBTest(TestFlexWrapper.class, testFactory);
    }

    /**
     * Main fucntion for direct execution.
     */
    public static void main(String[] args) {
        // Testcase will fail when calling this without dropping
        //              TRUNCATE TABLE DO_STORAGE;
        // SHOW_TIME             = true;     // for debugging
        // KBSetup.CREATE_TABLES = false;    // for debugging

        Logger.configureStdout();   // "INFO"

        junit.textui.TestRunner.run(suite());
    }

}
