/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.dob.DataObjectException;
import com.top_logic.event.logEntry.LogEntryConfiguration;
import com.top_logic.event.logEntry.LogEntryDisplayGroup;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestLogEventManager extends TestLogHelper {

	/** see top-logic.test.config.xml */
	private static String DISPLAY_GROUP = "testGroup";
    
	public void testDisplayGroup() throws DataObjectException {
        LogEntryConfiguration theConf  = LogEntryConfiguration.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		Wrapper theTrigger = createTrigger(kb, "testDisplayGroup-T");
		tx.commit();

        LogEntryDisplayGroup theGroup  = theConf.getDisplayGroup(theTrigger);
        LogEntryDisplayGroup theGroup2 = theConf.getDisplayGroup(theGroup.getName());
        
        assertSame(theGroup, theGroup2);
		int testBEventTypes = 3;// created,modified,deleted
		int wrapperEventTypes = 2;// created,modified
        assertEquals("Wrong number of event types for "+theGroup, 3, theGroup.getEventTypes().size());
		assertEquals("Wrong number of logentry types for " + theGroup, testBEventTypes + wrapperEventTypes, theGroup
			.getLogEntryTypes().size());
		assertEquals("Wrong number of logentry types for " + theGroup, 2,
			theGroup.getLogEntryTypes(MonitorEvent.CREATED).size());
		assertEquals("Wrong number of logentry types for " + theGroup, 2,
			theGroup.getLogEntryTypes(MonitorEvent.MODIFIED).size());
		assertEquals("Wrong number of logentry types for " + theGroup, 1,
			theGroup.getLogEntryTypes(MonitorEvent.DELETED).size());

    }
    
    public void testGetAllEvents(){
        LogEntryConfiguration theConf = LogEntryConfiguration.getInstance();
		Collection allEvents = theConf.getDisplayGroup(DISPLAY_GROUP).getEventTypes();
        assertEquals("Wrong number of default event types",3, allEvents.size());
    }
    
    public void testGetEventGroups(){
        LogEntryConfiguration theConf = LogEntryConfiguration.getInstance();
        Collection allGroups = theConf.getDisplayGroups();
		// contains testGroup and Person from top-logic.config.xml
        assertEquals("Wrong number of event groups",2, allGroups.size());
    }
    
    public void testCheck(){
        LogEntryConfiguration theConf = LogEntryConfiguration.getInstance();
        String txt="tst";
        assertFalse(theConf.check(txt,txt));
		assertTrue(theConf.check(KBTestMeta.TEST_B, MonitorEvent.MODIFIED));
    }
    
    /** Return the suite of Tests to perform */
    public static Test suite () {
		return suite(TestLogEventManager.class);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
