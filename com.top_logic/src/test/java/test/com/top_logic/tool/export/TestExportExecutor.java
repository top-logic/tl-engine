/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import junit.framework.Test;

import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.export.Export;
import com.top_logic.tool.export.Export.State;
import com.top_logic.tool.export.ExportHandler;
import com.top_logic.tool.export.ExportHandlerRegistry;
import com.top_logic.tool.export.ExportQueueManager.ExportExecutor;
import com.top_logic.util.TLContext;

/**
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestExportExecutor extends AbstractExportTest {

	public static Test suite() {
		return suite(TestExportExecutor.class);
	}

	public void testExecutor() throws Exception {
		ExportHandler handler = ExportHandlerRegistry.getInstance().getHandler(EXPORT_HANDLER_ID);

		Export export1 = this.getExport(EXPORT_HANDLER_ID);
		Export export2 = this.getExport(WAITING_EXPORT_HANDLER_ID);
		Export export3 = this.getExport(FAILING_EXPORT_HANDLER_ID);

		assertTrue(export1.setStateQueued());
		Thread.sleep(100); // some time between queueing to get distict timestamps
		assertTrue(export2.setStateQueued());
		Thread.sleep(100);
		assertTrue(export3.setStateQueued());
		Thread.sleep(100);

		ExportExecutor exec = new ExportExecutor("exec", 100, handler.getExportTechnology(), handler
			.getExportDuration(), getExportRegistry());
		exec.start();
		try {
			// wait max 5 seconds fo the exec to be finished
			export1 = this.getExport(EXPORT_HANDLER_ID);
			int waitCount = 0;
			while (waitCount < 5 && State.FINISHED != export1.getState()) {
				Thread.sleep(1000);
				waitCount++;
				export1 = this.getExport(EXPORT_HANDLER_ID);
			}
			assertSame(State.FINISHED, export1.getState());

			export1 = this.getExport(WAITING_EXPORT_HANDLER_ID);
			waitCount = 0;
			while (waitCount < 5 && State.FINISHED != export1.getState()) {
				Thread.sleep(1000);
				waitCount++;
				export1 = this.getExport(WAITING_EXPORT_HANDLER_ID);
			}
			assertSame(State.FINISHED, export1.getState());

			export1 = this.getExport(FAILING_EXPORT_HANDLER_ID);
			waitCount = 0;
			while (waitCount < 5 && State.FAILED != export1.getState()) {
				Thread.sleep(1000);
				waitCount++;
				export1 = this.getExport(FAILING_EXPORT_HANDLER_ID);
			}
			assertSame(State.FAILED, export1.getState());

		}
		finally {
			exec.shutdown();
			exec.join(1000);
		}
		assertFalse(exec.isAlive());
	}

	public void testExecutorUser() throws Exception {
		Export export1 = this.getExport(EXPORT_HANDLER_ID);
		assertNull(export1.getPerson());

		TLContext.getContext().setCurrentPerson(PersonManager.getManager().getRoot());
		String theContextId = TLContext.getContext().getContextId();
		Export export2 = this.getExport(USER_EXPORT_HANDLER_ID);
		assertSame(PersonManager.getManager().getRoot(), export2.getPerson());

		assertTrue(export1.setStateQueued());
		Thread.sleep(100); // some time between queueing to get distict timestamps
		assertTrue(export2.setStateQueued());
		Thread.sleep(100);

		ExportHandler handler = ExportHandlerRegistry.getInstance().getHandler(EXPORT_HANDLER_ID);
		ExportExecutor exec =
			new ExportExecutor("exec", 10, handler.getExportTechnology(), handler.getExportDuration(),
				getExportRegistry());

		exec.start();

		try {
			Thread.sleep(2000);

			export1 = this.getExport(EXPORT_HANDLER_ID);
			int waitCount = 0;
			while (waitCount < 5 && State.FINISHED != export1.getState()) {
				Thread.sleep(1000);
				waitCount++;
				export1 = this.getExport(ERROR_EXPORT_HANDLER_ID);
			}
			assertSame(State.FINISHED, export1.getState());
			assertNull(export1.getPerson());

			export2 = this.getExport(USER_EXPORT_HANDLER_ID);
			waitCount = 0;
			while (waitCount < 5 && State.FINISHED != export2.getState()) {
				Thread.sleep(1000);
				waitCount++;
				export2 = this.getExport(USER_EXPORT_HANDLER_ID);
			}

			assertSame(State.FINISHED, export2.getState());
			assertSame(PersonManager.getManager().getRoot(), export2.getPerson());
			assertEquals(theContextId, TLContext.getContext().getContextId());

		}
		finally {
			exec.shutdown();
			exec.join(100);
		}
		assertFalse(exec.isAlive());
	}

}
