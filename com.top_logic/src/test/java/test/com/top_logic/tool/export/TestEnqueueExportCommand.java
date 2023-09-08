/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import java.util.Collections;

import junit.framework.Test;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.export.Export;
import com.top_logic.tool.export.ExportTableComponent;
import com.top_logic.tool.export.ExportTableComponent.EnqueueExportCommand;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestEnqueueExportCommand extends AbstractExportTest {

    public static Test suite() {
        return suite(TestEnqueueExportCommand.class);
    }

//    public void testHandleCommand() throws Exception {
//        EnqueueExportCommand command = new EnqueueExportCommand(EnqueueExportCommand.COMMAND_ID);
//        Export export1 = this.getExport(EXPORT_HANDLER_ID);
//        ExportTableComponent component = new ExportTableComponent(null);
//
//
//        HandlerResult result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.QUEUED, export1.getState());
//
//        result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.QUEUED, export1.getState());
//
//        assertTrue(export1.setStateRunning());
//        result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.RUNNING, export1.getState());
//
//        assertTrue(export1.setStateFailed("failed"));
//        result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.QUEUED, export1.getState());
//
//        assertTrue(export1.setStateFinished(null, "file", "txt"));
//        result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.QUEUED, export1.getState());
//
//        assertTrue(export1.setStateRunning());
//        result1 = command.handleCommand(null, null, Collections.singletonMap(ExportTableComponent.EXPORT, export1));
//        assertTrue(result1.isSuccess());
//        assertSame(State.RUNNING, export1.getState());
//    }

    public void testGetExecutabilityRule() throws Exception {
		EnqueueExportCommand command = EnqueueExportCommand.newInstanceExport(SimpleBoundCommandGroup.READ);
        Export export = this.getExport(EXPORT_HANDLER_ID);
        ExecutabilityRule rule = command.createExecutabilityRule();

		assertTrue(export.setStateFailed(ResKey.forTest("failed")));
		assertTrue(rule.isExecutable(null,
			null, Collections.<String, Object> singletonMap(ExportTableComponent.EXPORT, export)).isExecutable());

		assertTrue(export.setStateFinished(null, ResKey.forTest("file"), "txt"));
		assertTrue(rule.isExecutable(null,
			null, Collections.<String, Object> singletonMap(ExportTableComponent.EXPORT, export)).isExecutable());

        assertTrue(export.setStateQueued());
		assertTrue(rule.isExecutable(null,
			null, Collections.<String, Object> singletonMap(ExportTableComponent.EXPORT, export)).isDisabled());

        assertTrue(export.setStateRunning());
		assertTrue(rule.isExecutable(null,
			null, Collections.<String, Object> singletonMap(ExportTableComponent.EXPORT, export)).isDisabled());

		export.setStateFailed(ResKey.forTest("failed"));
    }
}

