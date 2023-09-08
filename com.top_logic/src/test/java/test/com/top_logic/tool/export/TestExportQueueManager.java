/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import junit.framework.Test;

import com.top_logic.tool.export.ExportQueueManager;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestExportQueueManager extends AbstractExportTest {
    public static Test suite() {
        return suite(TestExportQueueManager.class, ExportQueueManager.Module.INSTANCE);
    }

    public void testGetInstance() throws Exception {
        assertNotNull(ExportQueueManager.getInstance());
    }
}

