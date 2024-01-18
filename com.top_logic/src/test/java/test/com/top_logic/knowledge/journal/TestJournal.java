/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.journal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.journal.ChangeJournalResultAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntryImpl;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.journal.JournalEntryImpl;
import com.top_logic.knowledge.journal.JournalLine;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.journal.JournalManager.Config;
import com.top_logic.knowledge.journal.JournalResult;
import com.top_logic.knowledge.journal.JournalResultAttributeEntry;
import com.top_logic.knowledge.journal.JournalResultEntry;
import com.top_logic.knowledge.journal.Journallable;
import com.top_logic.knowledge.journal.MessageJournalAttributeEntryImpl;
import com.top_logic.knowledge.journal.MessageJournalResultAttributeEntry;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItemUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractFlexWrapperJournalDelegate;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.Utils;

/**
 * TestCase for {@link JournalManager} related classes.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class TestJournal extends BasicTestCase {
    
	private static TLID PREPARE_0_ID;

	private static TLID PREPARE_1_ID;

	private static TLID PREPARE_2_ID;

	public static final int GET_JOURNAL_REPEAT = 10;
    
    static final String LONG_STRING = StringServices.getRandomString(new Random(1073741823L), 512);

    /** Time to sleep between Tests so that Threads can do their work. */
    public static final long SLEEP = 1000;
    
    /** Number of repeats used in Threads */
    public static final int REPEAT = 50;
    
     /** Time to sleep so the MySQL Connection will time out for sure */
    static private final int MYSQL_SLEEP = 1500;

    /** Time we wait until we assume a deadlock */
    public static final long JOIN_DELAY = (REPEAT + 10) * MYSQL_SLEEP;

    /**
     * Virtual attribute if {@link JournallableDocument}.
     */
	private static final String CONTAINER_ATTRIBUTE = "content";
    

	private static final Mapping<? super JournalResultEntry, ?> ENTRY_ID = new Mapping<>() {
		@Override
		public Object map(JournalResultEntry input) {
			return input.getIdentifier();
		}
	};
    
    /** this variable indicates that the threads used in getJournalTread should stop now. */
    boolean stopThreadTest;

    /** When true we add extra sleeps to provoke retries */
    boolean testRetry;
    
    
    /**
     * Create a new TestJournal.
     */
    public TestJournal(String name) {
        super(name);
    }

	public void testJournalDeletedAssociationTarget() throws InterruptedException {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        JournalManager jm = JournalManager.getInstance();
        
		// Create associated document.
        JournallableDocument d1;
        KnowledgeAssociation link;
		ObjectKey linkKey;
        {
        	Transaction tx = kb.beginTransaction();
			WebFolderFactory wfFactory = WebFolderFactory.getInstance();
			WebFolder folder =
				wfFactory.createNewWebFolder("webFolder", wfFactory.getAllowedFolderTypes().iterator().next());
            
			KnowledgeObject d1ko = createObject(kb, null);
			d1 = (JournallableDocument) WrapperFactory.getWrapper(d1ko);
			d1.setValue("name", "d1");

			link = kb.createAssociation(folder.tHandle(), d1ko, WebFolder.CONTENTS_ASSOCIATION);
			linkKey = link.tId();

        	tx.commit();
        }
        
		// Journal writes whole second into database. In unfortunate situations both commits have
		// same timestamp.
		long t1 = waitUntilNextSecond();
        
		// Wait another second. This is required to work around the MySQL 5.6 feature supporting
		// timestamps with millisecond precision, but by default only storing timestamps with
		// second precision, see http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html.
		// When creating a timestamp value with second precision, milliseconds now get rounded to
		// the next second.
		waitUntilNextSecond();

        // Delete target document.
        {
        	Transaction tx = kb.beginTransaction();

        	link.delete();
			
        	// Required to bring the object into the commit context.
			((CommitHandler) kb).addCommittable(d1);
        	
        	tx.commit();
        }

        jm.flush();
        
        long t2 = System.currentTimeMillis();
        
		List<JournalResult> journal = jm.getJournal(KBUtils.getWrappedObjectName(d1), kb.getName(), t1, t2);
        
        assertEquals("Exactly one commit expected.", 1, journal.size());
        
        JournalResult result = journal.get(0);
        
		JournalResultEntry d2Entry = CollectionUtil.find(result.entries, KBUtils.getWrappedObjectName(d1), ENTRY_ID);
		assertNotNull("Missing journal entry for d2.", d2Entry);
        
		assertTrue("Expected modification of (virtual) content attribute.", d2Entry.getAttributEntries().size() > 0);
        boolean virtualAttributeFound = false;
		for (JournalResultAttributeEntry entry : d2Entry.getAttributEntries()) {
			if (entry.getName().equals(CONTAINER_ATTRIBUTE)) {
				assertInstanceof(entry, ChangeJournalResultAttributeEntry.class);
				assertEquals(linkKey.getObjectName(),
					IdentifierUtil.fromExternalForm((String) ((ChangeJournalResultAttributeEntry) entry).getPre()));
				virtualAttributeFound = true;
        	}
        }
        assertTrue("Expected modification of (virtual) content attribute.", virtualAttributeFound);
    }

	private static KnowledgeObject createObject(KnowledgeBase kb, String name) throws DataObjectException {
		final KnowledgeObject resultKO = kb.createKnowledgeObject(JournallableDocument.OBJECT_NAME);
		if (name != null) {
			resultKO.setAttributeValue("name", name);
		}
		resultKO.setAttributeValue("size", (long) 0);
		resultKO.setAttributeValue("updateRevisionNumber", NextCommitNumberFuture.INSTANCE);
		return resultKO;
	}

	private long waitUntilNextSecond() throws InterruptedException {
		Calendar cal = CalendarUtil.createCalendar();
		int currentSecond = cal.get(Calendar.SECOND);
		while (true) {
			int milliSeond = cal.get(Calendar.MILLISECOND);
			Thread.sleep(1000 - milliSeond + 1);
			cal.setTimeInMillis(System.currentTimeMillis());
			if (cal.get(Calendar.SECOND) != currentSecond) {
				// should almost ever be the case
				break;
			}
		}
		return cal.getTimeInMillis();
	}
    
    public void testJournal() throws Exception {
        Runtime       rt    = Runtime.getRuntime();
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject theTicketKO1 = createObject(theKB, null);
		KnowledgeObject theTicketKO2 = createObject(theKB, null);
        JournalManager theJM = JournalManager.getInstance();
		JournallableDocument theWrapper1 = (JournallableDocument) WrapperFactory.getWrapper(theTicketKO1);
		JournallableDocument theWrapper2 = (JournallableDocument) WrapperFactory.getWrapper(theTicketKO2);
        try {
            theWrapper1.setValue("name", "t1");
            theWrapper2.setValue("name", "t2");
            theWrapper1.setValue("definitionId", "t1");
            theWrapper2.setValue("definitionId", LONG_STRING);
            theWrapper1.setValue("definitionVersion", "t1");
            theWrapper2.setValue("definitionVersion", "t2");
            long t1 = System.currentTimeMillis();
            Thread.sleep(SLEEP);

            theWrapper1.setValue("att1", "a1");
            theWrapper2.setValue("att1", LONG_STRING);
            rt.gc();
            Thread.sleep(SLEEP);
            assertTrue(theKB.commit()); // commit_id = 1
            
            Thread.sleep(SLEEP);
            long t2 = System.currentTimeMillis();
                         
            Thread.sleep(SLEEP);

            theWrapper1.setValue("att1", "b1");
            theWrapper2.setValue("att1", "b2");
            rt.gc();
            Thread.sleep(SLEEP);
            assertTrue(theKB.commit());  // commit_id = 2
            Thread.sleep(SLEEP);
            long t3 = System.currentTimeMillis();
            
            Thread.sleep(SLEEP);
            theWrapper1.setValue("att1", "c1");
            theWrapper2.setValue("att1", LONG_STRING);
            rt.gc();
            Thread.sleep(SLEEP);
            assertTrue(theKB.commit());  // commit_id = 3

            theJM.flush();
            Thread.sleep(SLEEP);
            long t4 = System.currentTimeMillis();
            
            List<JournalResult> theJournal;
            JournalResult       theJR;
            JournalResultEntry  theJRE;
            theJournal = 
                theJM.getJournal(KBUtils.getObjectName(theTicketKO1), theKB.getName());
            assertEquals("journal for ko1 has wrong size.", 3, theJournal.size());
            theJournal =
                theJM.getJournal(KBUtils.getObjectName(theTicketKO2), theKB.getName());
            assertEquals("journal for ko2 has wrong size.", 3, theJournal.size());
    
            //we assume there is always only one JRE in a JR! if this may change, adapt the entiries.get(0)!
            theJournal = theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO1),
                    theKB.getName(),t1-500,t2);
            assertEquals("journal for ko1 has wrong size.", 1, theJournal.size());
            theJR  = theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            Collection<JournalResultAttributeEntry> theAttributes = theJRE.getAttributEntries();
            JournalResultAttributeEntry theAtt;
            Iterator<JournalResultAttributeEntry> theIt = theAttributes.iterator();
            boolean foundAtt1 = false;
            boolean foundAtt2 = false;
            while (theIt.hasNext()) {
                theAtt = theIt.next();
                if (theAtt.getName().equals("att1")) {
                    ChangeJournalResultAttributeEntry theCAtt = (ChangeJournalResultAttributeEntry) theAtt;
                    foundAtt1 = true;
                    assertEquals(null, theCAtt.getPre());
                    assertEquals("a1", theCAtt.getPost());
                }
                if (theAtt.getName().equals("dummy")) {
                    MessageJournalResultAttributeEntry theCAtt = (MessageJournalResultAttributeEntry) theAtt;
                    foundAtt2 = true;
                    assertEquals("String", theCAtt.getType());
                }
            }
            assertTrue(foundAtt1);
            assertTrue(foundAtt2);
            theJournal =
                theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO2),
                    theKB.getName(),
                    t1,
                    t2);
            assertEquals("journal for ko2 has wrong size.", 1, theJournal.size());
            theJR  = theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            theAttributes = theJRE.getAttributEntries();
            foundAtt1 = false;
            while (theIt.hasNext()) {
                theAtt = theIt.next();
                if (theAtt.getName().equals("att1")) {
                    ChangeJournalResultAttributeEntry theCAtt = (ChangeJournalResultAttributeEntry) theAtt;
                    assertEquals(null, theCAtt.getPre());
                    assertEquals(LONG_STRING, theCAtt.getPost());
                }
                assertTrue(foundAtt1);
            }
    
            theJournal =
                theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO1),
                    theKB.getName(),
                    t2,
                    t3);
            // TODO TSA/KHA will fail because of rt.gc() above ...
            assertEquals("journal for ko1 has wrong size.", 1, theJournal.size());
            theJR =  theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            theAttributes = theJRE.getAttributEntries();
            ChangeJournalResultAttributeEntry theCAtt = null;
            for (JournalResultAttributeEntry theAtt2 : theAttributes) {
                 if (theAtt2 instanceof ChangeJournalResultAttributeEntry) {
                     theCAtt = (ChangeJournalResultAttributeEntry) theAtt2;
                 }
            }
            assertEquals(2, theAttributes.size());
            assertEquals("att1", theCAtt.getName());
            assertEquals("a1", theCAtt.getPre());
            assertEquals("b1", theCAtt.getPost());
    
            theJournal =
                theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO2),
                    theKB.getName(),
                    t2,
                    t3);
            assertEquals("journal for ko2 has wrong size.", 1, theJournal.size());
            theJR = theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            theAttributes = theJRE.getAttributEntries();
            for (JournalResultAttributeEntry theAtt2 : theAttributes) {
                if (theAtt2 instanceof ChangeJournalResultAttributeEntry) {
                     theCAtt = (ChangeJournalResultAttributeEntry) theAtt2;
                 }
            }
            assertEquals(2, theAttributes.size());
            assertEquals("att1", theCAtt.getName());
            assertEquals(LONG_STRING, theCAtt.getPre());
            assertEquals("b2"       , theCAtt.getPost());
    
            theJournal =
                theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO1),
                    theKB.getName(),
                    t3,
                    t4);
            assertEquals("journal for ko1 has wrong size.", 1, theJournal.size());
            theJR  = theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            theAttributes = theJRE.getAttributEntries();
            for (JournalResultAttributeEntry theAtt2 : theAttributes) {
                 if (theAtt2 instanceof ChangeJournalResultAttributeEntry) {
                     theCAtt = (ChangeJournalResultAttributeEntry) theAtt2;
                 }
            }
            assertEquals(2, theAttributes.size());
            assertEquals("att1", theCAtt.getName());
            assertEquals("b1", theCAtt.getPre());
            assertEquals("c1", theCAtt.getPost());
    
            theJournal =
                theJM.getJournal(
                    KBUtils.getObjectName(theTicketKO2),
                    theKB.getName(),
                    t3,
                    t4);
            assertEquals("journal for ko2 has wrong size.", 1, theJournal.size());
            theJR = theJournal.get(0);
            theJRE = CollectionUtil.getFirst(theJR.entries);
            theAttributes = theJRE.getAttributEntries();
            for (JournalResultAttributeEntry theAtt2 : theAttributes) {
                if (theAtt2 instanceof ChangeJournalResultAttributeEntry) {
                    theCAtt = (ChangeJournalResultAttributeEntry) theAtt2;
                 }
            }
            assertEquals(2, theAttributes.size());
            assertEquals("att1", theCAtt.getName());
            assertEquals("b2", theCAtt.getPre());
            assertEquals(LONG_STRING, theCAtt.getPost());
            
            theJM.removeJournal(KBUtils.getWrappedObjectName(theWrapper1));
            theJournal =
                theJM.getJournal(KBUtils.getObjectName(theTicketKO1), theKB.getName());
            assertEquals("journal for ko1 is not removed.", 0, theJournal.size());
            theJournal =
                theJM.getJournal(KBUtils.getObjectName(theTicketKO2), theKB.getName());
            assertEquals("journal for ko2 is not preserved.", 3, theJournal.size());
            
            theJM.removeJournal(KBUtils.getWrappedObjectName(theWrapper2));
            theJournal =
                theJM.getJournal(KBUtils.getObjectName(theTicketKO2), theKB.getName());
            assertEquals("journal for ko2 is not removed.", 0, theJournal.size());
        } finally {
            theWrapper1.tDelete();
            theWrapper2.tDelete();
            theKB.commit();
        }
        
    }
    
    /**
     * Test the Journal manager with two concurrent Threads.
     */
    public void testJournalThread() throws Throwable {
        // Push changes into journal
        JournalPusher thePusher = new JournalPusher();

        // Check for changes in journal
        JournalPuller thePuller = new JournalPuller();
        
        stopThreadTest = false;
        
        startTime();
        thePusher.start();
        thePuller.start();
        thePusher.join(JOIN_DELAY);
        thePuller.join(JOIN_DELAY);
        if (!testRetry)
            logTime("Pushing / Pulling " + REPEAT);
        
        boolean thePusherIsAlive = thePusher.isAlive();
        boolean thePullerIsAlive = thePuller.isAlive();
        
        if (thePusherIsAlive || thePullerIsAlive) {
            // in case of a dead lock no further testing is posible
            System.err.println("Deadlock in JournalManager!!!");
            System.exit(0);
        }
        
        if (thePusher.caught != null) {
            throw thePusher.caught;
        }
        if (thePuller.caught != null) {
            throw thePuller.caught;
        }
    }
    
    public void checkGetJournalEntry() throws Exception {
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
        String theKBName = theKB.getName();
        JournalManager theJM = JournalManager.getInstance();
        List<JournalResult>  theJournal;
        for (int i=0; i<GET_JOURNAL_REPEAT; i++) {
			theJournal = theJM.getJournal(PREPARE_0_ID, theKBName);
			assertEquals("journal for ko0 has wrong size.", 3, theJournal.size());
			theJournal = theJM.getJournal(PREPARE_1_ID, theKBName);
			assertEquals("journal for ko1 has wrong size.", 3, theJournal.size());
			theJournal = theJM.getJournal(PREPARE_2_ID, theKBName);
			assertEquals("journal for ko2 has wrong size.", 3, theJournal.size());
        }
    }
    
//    public void checkGetPostDataJournalEntry() throws Exception {
//        List theList0 = Arrays.asList(new String[] { "prepare0" });
//        List theList1 = Arrays.asList(new String[] { "prepare1" });
//        List theList2 = Arrays.asList(new String[] { "prepare2" });
//        KnowledgeBase theKB = EWETestSetup.getKnowledgeBase();
//        String theKBName = theKB.getName();
//        JournalManager theJM = JournalManager.getInstance();
//        List theJournal;
//        for (int i=0; i<GET_JOURNAL_REPEAT; i++) {
//            theJournal = theJM.getPostDataJournal(theList0,theKBName, 0, System.currentTimeMillis()+SLEEP);
//            assertEquals("journal for ko0 has wrong size.", 1, theJournal.size());
//            theJournal = theJM.getPostDataJournal(theList1,theKBName, 0, System.currentTimeMillis()+SLEEP);
//            assertEquals("journal for ko1 has wrong size.", 1, theJournal.size());
//            theJournal = theJM.getPostDataJournal(theList2,theKBName, 0, System.currentTimeMillis()+SLEEP);
//            assertEquals("journal for ko2 has wrong size.", 1, theJournal.size());
//        }
//    }
    
    
    
    public void testJournalLine() throws Exception {
        JournalLine theLine = new JournalLine("whoever", 5);
        JournalEntry theEntry;
        JournalAttributeEntry theAttribute;
		theEntry = new JournalEntryImpl(createId(), "Person", 5);
        theAttribute = new JournalAttributeEntryImpl("attA", "old", "new");
        theEntry.addAttribute(theAttribute);
		theAttribute = new JournalAttributeEntryImpl("attB", Integer.valueOf(1), Integer.valueOf(2));
        theEntry.addAttribute(theAttribute);
        theLine.add(theEntry);
		theEntry = new JournalEntryImpl(createId(), "Person", 5);
        theAttribute = new JournalAttributeEntryImpl("attC", "old", "new");
        theEntry.addAttribute(theAttribute);
		theAttribute = new JournalAttributeEntryImpl("attD", Long.valueOf(1), Long.valueOf(2));
        theEntry.addAttribute(theAttribute);
        theLine.add(theEntry);
        
        TestingJournalManager djm = internalCreateJournalManager();
        djm.journalLine(theLine);
        
        djm.flush();
		djm.terminate();
    }

	private TLID createId() {
		return PersistencyLayer.getKnowledgeBase().createID();
	}

	static JournalManager createJournalManager() throws SQLException, IOException, ConfigurationException {
		return internalCreateJournalManager();
	}

	public static TestingJournalManager internalCreateJournalManager()
			throws IOException, SQLException, ConfigurationException {
		Config config = getTestConfiguration(TestJournal.class);
		
		TestingJournalManager testingJournalManager =
			new TestingJournalManager(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		testingJournalManager.startUp();
		return testingJournalManager;
	}

	private static Config getTestConfiguration(Class<?> testClass)
			throws IOException, ConfigurationException {
		String resourceName = testClass.getSimpleName() + ".config.xml";
		final InputStream content = testClass.getResourceAsStream(resourceName);
		BinaryContent underlyingData;
		try {
			underlyingData = Utils.createBinaryContent(content);
		} finally {
			content.close();
		}

		ConfigurationDescriptor descr = TypedConfiguration.getConfigurationDescriptor(JournalManager.Config.class);
		Map<String, ConfigurationDescriptor> globalDescriptorsByName = Collections.singletonMap("instance", descr);
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, globalDescriptorsByName);
		reader.setSource(underlyingData);
		Config config = (Config) reader.read();

		return config;
	}

	/**
	 * {@link JournalManager} for testing.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
    protected static class TestingJournalManager extends JournalManager {
    	
		/**
		 * @param context
		 *        {@link InstantiationContext} context to instantiate sub configurations.
		 * @param config
		 *        Configuration for {@link TestingJournalManager}.
		 */
		public TestingJournalManager(InstantiationContext context, Config config) throws SQLException {
			super(context, config);
        }

        /** Make accessible for testing */
		@Override
		public void journalLine(JournalLine aLine) throws SQLException {
            super.journalLine(aLine); 
        }

		@Override
		protected void startUp() {
			super.startUp();
		}

    }

	public static class JournallableDocument extends AbstractBoundWrapper implements Journallable, Committable {

		public static final String OBJECT_NAME = "TestJournal";

		public JournallableDocument(KnowledgeObject ko) {
            super(ko);
        }

        @Override
		public JournalEntry getJournalEntry(Map someChanged, Map someCreated, Map someRemoved) {
            try {
                JDDelegate theDelegate = new JDDelegate();
                JournalEntry theEntry = theDelegate;
                theEntry.addAttribute(new MessageJournalAttributeEntryImpl(
                        "dummy", "String", "test", new Date().toString()));
                
                // Add virtual attribute entries from association deletions.
    			KnowledgeObject modifiedKO = this.tHandle();
    			for (Iterator it = someRemoved.values().iterator(); it.hasNext(); ) {
    				Object deletedObject = it.next();
					if (KnowledgeItemUtil.instanceOfKnowledgeAssociation(deletedObject)) {
    					KnowledgeAssociation deletedLink = (KnowledgeAssociation) deletedObject;
    					
						if (Utils.equals(KnowledgeItemUtil.getDestinationObject(someRemoved, deletedLink), modifiedKO)) {
							/* can not use object key direct because only primitive types can be
							 * journalled */
							Object preValue = IdentifierUtil.toExternalForm(deletedLink.tId().getObjectName());
							theEntry.addAttribute(new JournalAttributeEntryImpl(CONTAINER_ATTRIBUTE, preValue, null));
    					}
    				}
    			}
                
                return theEntry;
            } catch (Exception e) {
                // well, this is bad and should not happen.
                // Throw a NullPointer here to force the tests to fail
                throw (RuntimeException) new RuntimeException (
                    "Could not successfully generate journal entry.").initCause(e);
            }
        }

        @Override
		public String getJournalType() {
            return JDDelegate.getJournalType(this);
        }
        
        // inner inner class
        
        private class JDDelegate extends AbstractFlexWrapperJournalDelegate {
			public JDDelegate() {
                super();
            }
            @Override
			protected AbstractWrapper getWrapper() {
                return JournallableDocument.this;
            }
        }

		@Override
		public boolean prepare(CommitContext aContext) {
			return true;
		}

		@Override
		public boolean prepareDelete(CommitContext aContext) {
			return true;
		}

		@Override
		public boolean commit(CommitContext aContext) {
			return true;
		}

		@Override
		public boolean rollback(CommitContext aContext) {
			return true;
		}

		@Override
		public void complete(CommitContext aContext) {
			// ignore
		}
        
    }
    
    JournallableDocument getJournallableObject() throws Exception {
		KnowledgeObject theKO = createObject(KBSetup.getKnowledgeBase(), null);
		Wrapper theWrapper = WrapperFactory.getWrapper(theKO);
        JournallableDocument theJWrapper = (JournallableDocument) theWrapper;
        return theJWrapper;
    }
    
	static Test getActiveTestSuite() {
		TestSuite activeSuite = new ActiveTestSuite("TestJournal (Active)");
		activeSuite.addTest(TestUtils.tryEnrichTestnames(new TestJournal("checkGetJournalEntry"), "ActiveTestSuite 1"));
		activeSuite.addTest(TestUtils.tryEnrichTestnames(new TestJournal("checkGetJournalEntry"), "ActiveTestSuite 2"));
		activeSuite.addTest(TestUtils.tryEnrichTestnames(new TestJournal("checkGetJournalEntry"), "ActiveTestSuite 3"));
		
		class ActiveSuiteTestSetup extends ThreadContextSetup {
			
			public ActiveSuiteTestSetup(Test decorated) {
				super(decorated);
			}

			@Override
			protected void doSetUp() throws Exception {
				prepareJournalEntry();
			}

		    private void prepareJournalEntry() throws Exception {
		    	
		    	Runtime        rt    = Runtime.getRuntime();
		    	JournalManager theJM = JournalManager.getInstance();
				KnowledgeBase theKB = KBSetup.getKnowledgeBase();

				JournallableDocument theWrapper1;
				JournallableDocument theWrapper2;
				JournallableDocument theWrapper3;
				{
					final Transaction tx = theKB.beginTransaction();

					KnowledgeObject theTicketKO1 = createObject(theKB, "prepare0");
					PREPARE_0_ID = theTicketKO1.getObjectName();

					theWrapper1 = (JournallableDocument) WrapperFactory.getWrapper(theTicketKO1);
					theWrapper1.setValue("name", "t1");

					KnowledgeObject theTicketKO2 = createObject(theKB, "prepare1");
					PREPARE_1_ID = theTicketKO2.getObjectName();

					theWrapper2 = (JournallableDocument) WrapperFactory.getWrapper(theTicketKO2);
					theWrapper2.setValue("name", "t2");

					KnowledgeObject theTicketKO3 = createObject(theKB, "prepare2");
					PREPARE_2_ID = theTicketKO3.getObjectName();

					theWrapper3 = (JournallableDocument) WrapperFactory.getWrapper(theTicketKO3);
					theWrapper3.setValue("name", "t3");

					tx.commit();
				}
				{
					final Transaction tx = theKB.beginTransaction();
					theWrapper1.setValue("definitionId", "t1");
					theWrapper2.setValue("definitionId", "t2");
					theWrapper3.setValue("definitionId", "t3");
					theWrapper1.setValue("definitionVersion", "t1");
					theWrapper2.setValue("definitionVersion", LONG_STRING);
					theWrapper3.setValue("definitionVersion", "t3");
					rt.gc(); // This may clear the flexReferences and break this testcase
					Thread.sleep(SLEEP); // Let gc do its evil work ... ;-)
					tx.commit();

				}
		    	Thread.sleep(SLEEP);
				{
					final Transaction tx = theKB.beginTransaction();
					// TODO TSA/KHA this is not actually checked in checkGetJournalEntry
					theWrapper1.setValue("att1", "a1"); // add some new values
					theWrapper2.setValue("att1", "a2");
					theWrapper3.setValue("att1", "a3");
					theWrapper1.setValue("definitionId", "t1x"); // Change values, too
					theWrapper2.setValue("definitionId", LONG_STRING + "x");
					theWrapper3.setValue("definitionId", "t3x");
					rt.gc(); // This may clear the flexReferences and break this testcase
					Thread.sleep(SLEEP); // Let gc doe its evil work ... ;-)
					tx.commit();
				}
		    	Thread.sleep(SLEEP);
		    	theJM.flush();
		    	Thread.sleep(SLEEP);
		    } 
		    

			@Override
			protected void doTearDown() throws Exception {
				cleanupJournalEntry();
			}
			
		    private void cleanupJournalEntry() throws Exception {
		        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		        KnowledgeObject theTicketKO1 =
					theKB.getKnowledgeObject("Document", PREPARE_0_ID);
		        KnowledgeObject theTicketKO2 =
					theKB.getKnowledgeObject("Document", PREPARE_1_ID);
		        KnowledgeObject theTicketKO3 =
					theKB.getKnowledgeObject("Document", PREPARE_2_ID);
		        if (theTicketKO1 != null) theKB.delete(theTicketKO1);
		        if (theTicketKO2 != null) theKB.delete(theTicketKO2);
		        if (theTicketKO3 != null) theKB.delete(theTicketKO3);
		        assertTrue(theKB.commit());
		    }    

		}
		return new ActiveSuiteTestSetup(activeSuite);
	}

	private class JournalPusher extends Thread implements InContext {
        
        Throwable caught;

		public JournalPusher() {
			super("JournalPusher");
            this.setDaemon(true);
        }

		@Override
		public void run() {
			super.run();
			ThreadContextManager.inSystemInteraction(JournalPusher.class, this);
		}

        @Override
		public void inContext() {
            try {
                KnowledgeBase theKB = KBSetup.getKnowledgeBase();
                JournallableDocument theWrapper = getJournallableObject();
                theWrapper.setValue("name"            , "t1");
                theWrapper.setValue("definitionId"    , "t1");
                theWrapper.setValue("definitionVersion", "t1");
                assertTrue(theKB.commit());

                for (int i = 0; i<REPEAT; i++) {
                    theWrapper.setValue("att1", "a" + (i % 2));
                    assertTrue(theKB.commit());
                    if (testRetry) {
                        sleep(MYSQL_SLEEP);
                    }
                    if (stopThreadTest) 
                        break;
                }                    
            } catch (Exception e) {
                caught = e;
            }
        }
        
    }
    
	private class JournalPuller extends Thread implements InContext {

        Throwable caught;
        
        public JournalPuller() {
			super("JournalPuller");
            setDaemon(true);
        }

		@Override
		public void run() {
			super.run();
			ThreadContextManager.inSystemInteraction(JournalPuller.class, this);
		}

        @Override
		public void inContext() {
            try {
                KnowledgeBase theKB = KBSetup.getKnowledgeBase();

                JournalManager theJM = JournalManager.getInstance();
                JournallableDocument theWrapper = getJournallableObject();
                theWrapper.setValue("name", "t1");
                theWrapper.setValue("definitionId", "t1");
                theWrapper.setValue("definitionVersion", "t1");
                assertTrue(theKB.commit());

				TLID theKOId = KBUtils.getWrappedObjectName(theWrapper);
                String theKBId = theKB.getName();
                for (int i = 0; i<REPEAT; i++) {
                    theJM.getJournal(theKOId, theKBId);
                    if (testRetry) {
                        sleep(MYSQL_SLEEP);
                    }
                    if (stopThreadTest) 
                        break;
                }                    
            } catch (Exception e) {
                caught = e;
            }
        }
        
    }

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite() {
    	return JournalTestSetup.suite(TestJournal.class, new TestFactory() {
    		
			@SuppressWarnings("unused")
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				if (false) {
					return new TestJournal("testJournalDeletedAssociationTarget");
				} else {
					TestSuite suite = new TestSuite(suiteName);
					suite.addTest(new TestSuite(TestJournal.class, "Inner tests of " + TestJournal.class.getName()));
					suite.addTest(getActiveTestSuite());
					return suite;
				}
			}
		});
    }

    /** main function for direct testing.
     */
    public static void main(String[] args) {
        // SHOW_TIME = true;
        Logger.configureStdout();   // "INFO"
        junit.textui.TestRunner.run(suite());
    }
    

}
