/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import junit.framework.Test;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.export.Export;
import com.top_logic.tool.export.Export.State;
import com.top_logic.tool.export.ExportHandler;
import com.top_logic.tool.export.ExportHandlerRegistry;
import com.top_logic.tool.export.ExportRegistry;
import com.top_logic.tool.export.ExportRegistryFactory;
import com.top_logic.util.TLContext;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestExportRegistry extends AbstractExportTest {
    public static Test suite() {
		return suite(TestExportRegistry.class);
    }

    public void testGetInstance() throws Exception {
        ExportRegistry reg = ExportRegistryFactory.getExportRegistry();
        assertNotNull(reg);

        assertSame(reg, ExportRegistryFactory.getExportRegistry());
    }

    public void testGetExport() throws Exception {
        ExportRegistry reg = this.getExportRegistry();
        Object model = this.getModel();

        Export export1 = reg.getExport(EXPORT_HANDLER_ID, model);
        assertNotNull(export1);
        assertNull(export1.getPerson());

        Export export2 = reg.getExport(WAITING_EXPORT_HANDLER_ID, model);
        assertNotNull(export2);
        assertNull(export2.getPerson());
        assertNotEquals(export1, export2);

        Export export3 = reg.getExport(EXPORT_HANDLER_ID, model);
        assertEquals(export1, export3);

        TLContext.getContext().setCurrentPerson(PersonManager.getManager().getRoot());
        Export export4 = reg.getExport(USER_EXPORT_HANDLER_ID, model);
        assertNotNull(export4.getPerson());
        assertSame(PersonManager.getManager().getRoot(), export4.getPerson());
    }

    public void testGetQueuedExport() throws Exception {
        ExportRegistry reg = this.getExportRegistry();

        ExportHandler handler1 = ExportHandlerRegistry.getInstance().getHandler(SHORT_WORD_EXPORT_HANDLER_ID);
        ExportHandler handler2 = ExportHandlerRegistry.getInstance().getHandler(LONG_WORD_EXPORT_HANDLER_ID);
        ExportHandler handler3 = ExportHandlerRegistry.getInstance().getHandler(SHORT_PPT_EXPORT_HANDLER_ID);

        Export export1 = this.getExport(SHORT_WORD_EXPORT_HANDLER_ID);
        Export export2 = this.getExport(LONG_WORD_EXPORT_HANDLER_ID);
        Export export3 = this.getExport(SHORT_PPT_EXPORT_HANDLER_ID);

        // queue must be empty
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));

        // queue a single export
        // find exact match
        assertTrue(export1.setStateQueued());
        Export running1 = reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration());
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));
        assertNotNull(running1);
        assertSame(State.RUNNING, running1.getState());
        assertEquals(running1, this.getExport(SHORT_WORD_EXPORT_HANDLER_ID));

        // reset
		assertTrue(export1.setStateFailed(ResKey.forTest("error")));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // queue a single export
        // find similar match, by technology
        assertTrue(export1.setStateQueued());
        running1 = reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration());
        assertNotNull(running1);
        assertSame(State.RUNNING, running1.getState());
        assertEquals(running1, this.getExport(SHORT_WORD_EXPORT_HANDLER_ID));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // reset
		assertTrue(export1.setStateFailed(ResKey.forTest("error")));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // queue different exports, different technology
        // find exact matches
        assertTrue(export2.setStateQueued());
        Thread.sleep(100); // ensure that we get different timestamps
        assertTrue(export3.setStateQueued());
        Export running2 = reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration());
        Export running3 = reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration());
        assertNotNull(running2);
        assertNotNull(running3);
        assertSame(State.RUNNING, running2.getState());
        assertSame(State.RUNNING, running3.getState());
        assertEquals(running2, this.getExport(LONG_WORD_EXPORT_HANDLER_ID));
        assertEquals(running3, this.getExport(SHORT_PPT_EXPORT_HANDLER_ID));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // reset
		assertTrue(export2.setStateFailed(ResKey.forTest("error")));
		assertTrue(export3.setStateFailed(ResKey.forTest("error")));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // queue different exports, same technology, different duration
        // find exact matches
        assertTrue(export3.setStateQueued());
        Thread.sleep(10); // ensure that we get different timestamps
        assertTrue(export1.setStateQueued());
        running1 = reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration());
        running3 = reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration());
        assertSame(State.RUNNING, running1.getState());
        assertSame(State.RUNNING, running3.getState());
        assertEquals(running1, this.getExport(SHORT_WORD_EXPORT_HANDLER_ID));
        assertEquals(running3, this.getExport(SHORT_PPT_EXPORT_HANDLER_ID));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        // reset
		assertTrue(export1.setStateFailed(ResKey.forTest("error")));
		assertTrue(export3.setStateFailed(ResKey.forTest("error")));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));

        Export export4 = this.getExport(EXPORT_HANDLER_ID);
        Export export5 = this.getExport(LONG_WAITING_EXPORT_HANDLER_ID);
        ExportHandler handler4 = ExportHandlerRegistry.getInstance().getHandler(EXPORT_HANDLER_ID);
        ExportHandler handler5 = ExportHandlerRegistry.getInstance().getHandler(LONG_WAITING_EXPORT_HANDLER_ID);

        // queue different exports, same technology, same duration, different queue time
        // find by date, first in first out
        assertTrue(export4.setStateQueued());
        Thread.sleep(100); // ensure that we get different timestamps
        assertTrue(export5.setStateQueued());
        Export running4 = reg.getNextRunningExport(handler4.getExportTechnology(), handler4.getExportDuration());
        Export running5 = reg.getNextRunningExport(handler4.getExportTechnology(), handler4.getExportDuration());
        assertNotNull(running4);
        assertNotNull(running5);
        assertSame(State.RUNNING, running4.getState());
        assertSame(State.RUNNING, running5.getState());
        assertEquals(running4, this.getExport(EXPORT_HANDLER_ID));
        assertEquals(running5, this.getExport(LONG_WAITING_EXPORT_HANDLER_ID));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler4.getExportTechnology(), handler4.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler5.getExportTechnology(), handler5.getExportDuration()));

        // reset
		assertTrue(export4.setStateFailed(ResKey.forTest("error")));
		assertTrue(export5.setStateFailed(ResKey.forTest("error")));
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler4.getExportTechnology(), handler4.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler5.getExportTechnology(), handler5.getExportDuration()));

        // queue different exports, same technology, different duration
        // find similar match, by technology
        assertTrue(export1.setStateQueued());
        Thread.sleep(10); // ensure that we get different timestamps
        assertTrue(export2.setStateQueued());
        running1 = reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration());
        running2 = reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration());
        assertNotNull(running1);
        assertNotNull(running2);
        assertSame(State.RUNNING, running1.getState());
        assertSame(State.RUNNING, running2.getState());
        assertEquals(running1, this.getExport(SHORT_WORD_EXPORT_HANDLER_ID)); // first must be the one with same duration
        assertEquals(running2, this.getExport(LONG_WORD_EXPORT_HANDLER_ID));  // second must be the one with other duration
        assertNull(reg.getNextRunningExport(handler1.getExportTechnology(), handler1.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler2.getExportTechnology(), handler2.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler3.getExportTechnology(), handler3.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler4.getExportTechnology(), handler4.getExportDuration()));
        assertNull(reg.getNextRunningExport(handler5.getExportTechnology(), handler5.getExportDuration()));
		assertTrue(export1.setStateFailed(ResKey.forTest("error")));
		assertTrue(export2.setStateFailed(ResKey.forTest("error")));
    }
}

