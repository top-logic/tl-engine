/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;
import test.com.top_logic.knowledge.KBSetup;

import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * Test of the states of a workspace whose container runs but whose application does not
 * ({@link WorkspaceStatus#APP_STOPPED}), through {@link ProjectWorkspaces} against a knowledge base
 * and a {@link RecordingWorkspaceProvider} answering the commands of the supervisor in the
 * container like {@link ApplicationWorkspaces#APP_CONTROL_COMMAND} does.
 */
public class TestWorkspaceApplicationState extends BasicTestCase {

	private static final String WORKSPACE_ID = "app-state-test-1";

	private static final String ARTIFACT_ID = "app-state-test";

	private static final String IMAGE = "tl-factory-workspace:0000000000000000";

	/**
	 * Line of the log of the application in the scripted workspace.
	 */
	private static final String APP_LOG_LINE = "ERROR it-broken-configuration";

	private static final long OPERATION_TIMEOUT_MILLIS = 30_000;

	private static final long POLL_MILLIS = 100;

	private static final int HTTP_OK = 200;

	private static final int HTTP_UNAVAILABLE = 503;

	/**
	 * What {@code status} of the scripted control command exits with.
	 */
	private volatile int _appStatus;

	/**
	 * What the scripted application answers.
	 */
	private volatile int _httpStatus;

	private HttpServer _server;

	private RecordingWorkspaceProvider _provider;

	private TLObject _project;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_server.createContext("/", exchange -> {
			exchange.sendResponseHeaders(_httpStatus, -1);
			exchange.close();
		});
		_server.start();
		_provider = new RecordingWorkspaceProvider()
			.setHostPort(_server.getAddress().getPort())
			.setExecHandler(this::exec);
		TestingProjectWorkspaces.setApplications(new ApplicationWorkspaces(
			WorkspaceService.getInstance().getApplications().getConfig(), _provider));
		_project = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "Application state test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, ARTIFACT_ID);
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			commit(() -> {
				TLObject workspace = FactoryModel.getWorkspace(_project);
				if (workspace != null) {
					workspace.tDelete();
				}
				_project.tDelete();
				return null;
			});
		} finally {
			TestingProjectWorkspaces.setApplications(null);
			_server.stop(0);
			super.tearDown();
		}
	}

	/**
	 * A start whose application fails ends in {@link WorkspaceStatus#APP_STOPPED} with the end of
	 * the application log in the output; Start then starts only the application, not the
	 * container; the application check follows the application stopping and answering again.
	 */
	public void testApplicationStopped() throws Exception {
		TLObject workspace = ProjectWorkspaces.commitNewWorkspace(_project, WORKSPACE_ID).workspace();
		_provider.create(new WorkspaceSpec(WORKSPACE_ID, IMAGE));
		setStatus(workspace, WorkspaceStatus.STOPPED);
		ProjectWorkspaces workspaces = ProjectWorkspaces.getInstance();

		// The application of the started container fails.
		_appStatus = ApplicationWorkspaces.APP_STATUS_FAILED;
		_httpStatus = HTTP_UNAVAILABLE;
		workspaces.start(_project);
		assertEquals(WorkspaceStatus.APP_STOPPED, awaitOperation(workspace));
		String log = FactoryModel.getString(workspace, FactoryModel.WORKSPACE_LOG);
		assertTrue(log, log.contains(APP_LOG_LINE));
		assertTrue(log, log.contains("Failed: "));
		assertNull(FactoryModel.getString(workspace, FactoryModel.WORKSPACE_PREVIEW_URL));
		assertEquals(1, count(RecordingWorkspaceProvider.START));

		// Start starts the application in the running container.
		workspaces.start(_project);
		assertEquals(WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertEquals("The container is not started again.", 1, count(RecordingWorkspaceProvider.START));
		assertTrue(_provider.calls().toString(), _provider.calls().contains(RecordingWorkspaceProvider.EXEC + " "
			+ WORKSPACE_ID + " " + ApplicationWorkspaces.APP_CONTROL_COMMAND + " " + ApplicationWorkspaces.APP_CONTROL_START));
		assertNotNull(FactoryModel.getString(workspace, FactoryModel.WORKSPACE_PREVIEW_URL));

		// The application stops without an operation of the control application, e.g. by the agent.
		_appStatus = ApplicationWorkspaces.APP_STATUS_FAILED;
		_httpStatus = HTTP_UNAVAILABLE;
		workspaces.checkApplications();
		assertEquals(WorkspaceStatus.APP_STOPPED, status(workspace));
		assertNull(FactoryModel.getString(workspace, FactoryModel.WORKSPACE_PREVIEW_URL));

		// A starting application changes nothing.
		_appStatus = ApplicationWorkspaces.APP_STATUS_NOT_RUNNING;
		workspaces.checkApplications();
		assertEquals(WorkspaceStatus.APP_STOPPED, status(workspace));

		// ... and answers again.
		_appStatus = ApplicationWorkspaces.APP_STATUS_RUNNING;
		_httpStatus = HTTP_OK;
		workspaces.checkApplications();
		assertEquals(WorkspaceStatus.RUNNING, status(workspace));
		assertNotNull(FactoryModel.getString(workspace, FactoryModel.WORKSPACE_PREVIEW_URL));
		assertEquals("The container is not touched by the checks.", 1, count(RecordingWorkspaceProvider.START));
		assertEquals(0, count(RecordingWorkspaceProvider.STOP));
	}

	/**
	 * Answers the commands of the supervisor like {@link ApplicationWorkspaces#APP_CONTROL_COMMAND}.
	 */
	private int exec(String workspaceId, ExecCommand command, ExecOutput output) {
		List<String> line = command.getCommand();
		if (line.size() < 2 || !line.get(0).equals(ApplicationWorkspaces.APP_CONTROL_COMMAND)) {
			return 0;
		}
		switch (line.get(1)) {
			case ApplicationWorkspaces.APP_CONTROL_STATUS: {
				int status = _appStatus;
				String state = status == ApplicationWorkspaces.APP_STATUS_RUNNING ? "running"
					: status == ApplicationWorkspaces.APP_STATUS_FAILED ? "failed" : "starting";
				output.stdout(ApplicationWorkspaces.APP_STATUS_PREFIX + state + "\n");
				return status;
			}
			case ApplicationWorkspaces.APP_CONTROL_LOG:
				output.stdout(APP_LOG_LINE + "\n");
				return 0;
			case ApplicationWorkspaces.APP_CONTROL_START:
				_appStatus = ApplicationWorkspaces.APP_STATUS_RUNNING;
				_httpStatus = HTTP_OK;
				output.stdout("url: http://localhost:8080/" + ARTIFACT_ID + "/\n");
				return 0;
			default:
				return 1;
		}
	}

	private int count(String operation) {
		String call = operation + " " + WORKSPACE_ID;
		return (int) _provider.calls().stream().filter(call::equals).count();
	}

	/**
	 * Waits until the operation running on the workspace has ended.
	 *
	 * @return The state it ended in.
	 */
	private static WorkspaceStatus awaitOperation(TLObject workspace) throws InterruptedException {
		long deadline = System.currentTimeMillis() + OPERATION_TIMEOUT_MILLIS;
		while (true) {
			WorkspaceStatus status = status(workspace);
			if (!status.isBusy()) {
				return status;
			}
			assertTrue("Operation did not end, state " + status + ": "
				+ FactoryModel.getString(workspace, FactoryModel.WORKSPACE_LOG),
				System.currentTimeMillis() < deadline);
			Thread.sleep(POLL_MILLIS);
		}
	}

	/**
	 * The state of the workspace, as last committed by any thread.
	 */
	private static WorkspaceStatus status(TLObject workspace) {
		// The operations and checks commit in threads of their own.
		HistoryUtils.updateSessionRevision();
		return FactoryModel.getStatus(workspace);
	}

	private static void setStatus(TLObject workspace, WorkspaceStatus status) {
		commit(() -> {
			FactoryModel.setStatus(workspace, status);
			return null;
		});
	}

	private static <T> T commit(Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Test"))) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * The tests of this class, with a knowledge base holding the model of the control application
	 * and the {@link TestingProjectWorkspaces} of the test configuration.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestWorkspaceApplicationState.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				// The configuration of the workspace image; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE,
				LLMProxyService.Module.INSTANCE,
				ProjectWorkspaces.Module.INSTANCE));
	}

}
