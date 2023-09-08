/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import junit.framework.Test;

import com.top_logic.tool.export.Export;
import com.top_logic.tool.export.Export.State;
import com.top_logic.tool.export.ExportRun;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestExportRun extends AbstractExportTest {

    public static Test suite() {
        return suite(TestExportRun.class);
    }

    public void testExportRunFinished() throws Exception {
        Export export1 = this.getExport(EXPORT_HANDLER_ID);
        assertTrue(export1.setStateQueued());
        assertTrue(export1.setStateRunning());

        ExportRun run1 = new ExportRun(export1);
        run1.run();
        assertSame(State.FINISHED, export1.getState());
        assertNotNull(export1.getDocument());
		assertEquals("TestExportFile", export1.getDisplayNameKey().toString());
        assertEquals("txt", export1.getFileExtension());
    }

    public void testExportRunFailed() throws Exception {
        Export export1 = this.getExport(FAILING_EXPORT_HANDLER_ID);
        assertTrue(export1.setStateQueued());
        assertTrue(export1.setStateRunning());

        ExportRun run1 = new ExportRun(export1);
        run1.run();
        assertSame(State.FAILED, export1.getState());
        assertNull(export1.getDocument());
		assertEquals("test.export.failed", export1.getFailureKey().toString());
    }

    public void testExportRunException() throws Exception {
        Export export1 = this.getExport(ERROR_EXPORT_HANDLER_ID);
        assertTrue(export1.setStateQueued());
        assertTrue(export1.setStateRunning());

        ExportRun run1 = new ExportRun(export1);
        run1.run();
        assertSame(State.FAILED, export1.getState());
        assertNull(export1.getDocument());
		assertEquals("export.failed.unexpected", export1.getFailureKey().toString());
    }
}

