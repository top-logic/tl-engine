/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.WorkspaceLogFollow;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link WorkspaceLogFollow} with a provider whose log deliveries the test drives: the
 * published text follows the output, a change of the workspace channel follows the other
 * workspace, an ended delivery is started anew when the workspace changes, and nothing is
 * published after closing.
 */
public class TestWorkspaceLogFollow extends BasicTestCase {

	private static final String WORKSPACE_A = "log-follow-test-a";

	private static final String WORKSPACE_B = "log-follow-test-b";

	private TLObject _projectA;

	private TLObject _projectB;

	private TLObject _workspaceA;

	private TLObject _workspaceB;

	private LogProvider _provider;

	private List<Object> _published;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_projectA = project("log-follow-a");
		_projectB = project("log-follow-b");
		_workspaceA = ProjectWorkspaces.commitNewWorkspace(_projectA, WORKSPACE_A).workspace();
		_workspaceB = ProjectWorkspaces.commitNewWorkspace(_projectB, WORKSPACE_B).workspace();
		_provider = new LogProvider();
		_published = new CopyOnWriteArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		commit(() -> {
			_projectA.tDelete();
			_projectB.tDelete();
			return null;
		});
		super.tearDown();
	}

	/** The last lines of the output are published as they arrive. */
	public void testPublishesTail() {
		WorkspaceLogFollow follow = follow(2);
		follow.follow(_workspaceA);

		Delivery delivery = _provider.only();
		assertEquals(WORKSPACE_A, delivery._workspaceId);
		assertEquals(2, delivery._tailLines);
		delivery._output.stdout("Jetty start");
		delivery._output.stdout("ing\nStarted\n");
		delivery._output.stderr("Warning\n");
		assertEquals("Started\nWarning", last());
		follow.close();
		assertTrue(delivery._closed);
	}

	/** Another workspace on the channel ends the delivery of the first one and starts its own. */
	public void testFollowsAnotherWorkspace() {
		WorkspaceLogFollow follow = follow(10);
		follow.follow(_workspaceA);
		Delivery first = _provider.only();
		first._output.stdout("A\n");

		follow.follow(_workspaceB);
		assertTrue(first._closed);
		assertEquals("", last());
		Delivery second = _provider._deliveries.get(1);
		assertEquals(WORKSPACE_B, second._workspaceId);
		first._output.stdout("late output of A\n");
		assertEquals("", last());
		second._output.stdout("B\n");
		assertEquals("B", last());

		follow.follow(null);
		assertTrue(second._closed);
		assertEquals("", last());
		assertEquals(2, _provider._deliveries.size());
		follow.close();
	}

	/**
	 * A change of the workspace starts a delivery anew once the old one has ended, i.e. after the
	 * main process ended; a delivery still running is kept.
	 */
	public void testRefreshAfterEnd() {
		WorkspaceLogFollow follow = follow(10);
		follow.follow(_workspaceA);
		Delivery first = _provider.only();
		first._output.stdout("run 1\n");

		follow.refresh(_workspaceA);
		assertEquals("A running delivery is kept.", 1, _provider._deliveries.size());
		follow.refresh(_workspaceB);
		assertEquals("Another workspace is not followed.", 1, _provider._deliveries.size());

		first._ended = true;
		follow.refresh(_workspaceA);
		assertEquals(2, _provider._deliveries.size());
		_provider._deliveries.get(1)._output.stdout("run 2\n");
		assertEquals("run 2", last());
		follow.close();
	}

	/** A workspace without container has no log; nothing breaks. */
	public void testMissingContainer() {
		_provider._fail = true;
		WorkspaceLogFollow follow = follow(10);
		follow.follow(_workspaceA);
		assertEquals("", last());
		follow.close();
	}

	/** Nothing is followed and published after closing. */
	public void testClose() {
		WorkspaceLogFollow follow = follow(10);
		follow.close();
		follow.follow(_workspaceA);
		assertTrue(_provider._deliveries.isEmpty());
		assertTrue(_published.isEmpty());
	}

	private WorkspaceLogFollow follow(int tailLines) {
		return new WorkspaceLogFollow(_provider, tailLines, _published::add, Runnable::run);
	}

	private Object last() {
		return _published.get(_published.size() - 1);
	}

	private static TLObject project(String artifactId) {
		return commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, artifactId);
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, artifactId);
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
	}

	private static <T> T commit(Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("test"))) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/** One delivery of a log the test drives. */
	private static final class Delivery implements LogFollower {

		final String _workspaceId;

		final int _tailLines;

		final ExecOutput _output;

		volatile boolean _ended;

		volatile boolean _closed;

		Delivery(String workspaceId, int tailLines, ExecOutput output) {
			_workspaceId = workspaceId;
			_tailLines = tailLines;
			_output = output;
		}

		@Override
		public boolean awaitEnd(Duration timeout) {
			return _ended || _closed;
		}

		@Override
		public void close() {
			_closed = true;
		}
	}

	/** Provider recording the deliveries of logs. */
	private static final class LogProvider extends RecordingWorkspaceProvider {

		final List<Delivery> _deliveries = new CopyOnWriteArrayList<>();

		volatile boolean _fail;

		@Override
		public LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output) {
			if (_fail) {
				throw new IllegalStateException("No container of " + workspaceId);
			}
			Delivery delivery = new Delivery(workspaceId, tailLines, output);
			_deliveries.add(delivery);
			return delivery;
		}

		Delivery only() {
			assertEquals(1, _deliveries.size());
			return _deliveries.get(0);
		}
	}

	/**
	 * The suite, with a knowledge base holding the model of the control application.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestWorkspaceLogFollow.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				WorkspaceService.Module.INSTANCE,
				LLMProxyService.Module.INSTANCE));
	}

}
